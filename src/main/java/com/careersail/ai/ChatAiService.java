package com.careersail.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.entity.AiChatHistory;
import com.careersail.entity.AssessmentResult;
import com.careersail.entity.SysUser;
import com.careersail.mapper.AiChatHistoryMapper;
import com.careersail.mapper.AssessmentResultMapper;
import com.careersail.mapper.SysUserMapper;
import com.careersail.vo.CareerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI 对话服务 — SSE 流式 + RAG 职业知识库 + 学生画像上下文
 * 优化版：RAG 异步预加载 + system/user 分层 Prompt
 */
@Slf4j
@Service
public class ChatAiService {

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;
    private final AiChatHistoryMapper chatHistoryMapper;
    private final RagService ragService;
    private final SysUserMapper sysUserMapper;
    private final AssessmentResultMapper assessmentResultMapper;

    public ChatAiService(ChatModel chatModel, PromptTemplateService promptTemplateService,
                         AiChatHistoryMapper chatHistoryMapper, RagService ragService,
                         SysUserMapper sysUserMapper, AssessmentResultMapper assessmentResultMapper) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.promptTemplateService = promptTemplateService;
        this.chatHistoryMapper = chatHistoryMapper;
        this.ragService = ragService;
        this.sysUserMapper = sysUserMapper;
        this.assessmentResultMapper = assessmentResultMapper;
    }

    /**
     * SSE streaming chat — 优化：RAG 异步预热 + system/user 分层
     */
    public Flux<String> streamChat(Long userId, String sessionId, String message, String major) {
        // 1. 并行启动 RAG 检索（异步，不阻塞主流程）
        CompletableFuture<List<CareerVO>> ragFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return ragService.searchSimilar(message, 3);
            } catch (Exception e) {
                return List.of();
            }
        });

        // 2. 构建轻量系统 Prompt（不含 RAG） + 加载历史
        String lightSystemPrompt = buildLightSystemPrompt(userId);
        List<AiChatHistory> history = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getSessionId, sessionId)
                        .orderByAsc(AiChatHistory::getCreateTime)
                        .last("LIMIT 6"));

        // 3. 保存用户消息
        saveMessage(userId, sessionId, "user", message);

        // 4. 构建对话消息（系统 + 历史 + 当前问题，不含 RAG）
        StringBuilder dialogBuilder = new StringBuilder();
        for (AiChatHistory h : history) {
            if ("user".equals(h.getRole())) {
                dialogBuilder.append("学生: ").append(h.getContent()).append("\n");
            } else {
                dialogBuilder.append("助手: ").append(h.getContent()).append("\n");
            }
        }
        dialogBuilder.append("学生: ").append(message);
        String dialog = dialogBuilder.toString();

        // 5. 等待 RAG 完成（最多 2 秒，超时则跳过）
        String ragContext = "";
        try {
            List<CareerVO> rags = ragFuture.get(2, java.util.concurrent.TimeUnit.SECONDS);
            if (!rags.isEmpty()) {
                StringBuilder rc = new StringBuilder("\n## 职业知识库参考\n");
                for (int i = 0; i < rags.size(); i++) {
                    CareerVO c = rags.get(i);
                    rc.append((i + 1)).append(". **").append(c.getPositionName())
                            .append("** 所需技能：").append(c.getSkillsRequired() != null ? c.getSkillsRequired() : "无")
                            .append("\n");
                }
                ragContext = rc.toString();
            }
        } catch (Exception e) {
            log.debug("RAG not ready, streaming without it");
        }

        // 6. 合并完整系统 Prompt + 发送
        String fullSystemPrompt = lightSystemPrompt + ragContext
                + "\n中文回复，300字内。信息不足则追问。";

        log.debug("Streaming chat: userId={}, sessionId={}, historySize={}, ragLen={}",
                userId, sessionId, history.size(), ragContext.length());

        // 使用 system() + user() 分层，让模型更高效处理
        return chatClient.prompt()
                .system(fullSystemPrompt)
                .user(dialog)
                .stream()
                .content()
                .doOnComplete(() -> log.info("Chat stream completed: sessionId={}", sessionId))
                .doOnError(e -> log.error("Chat stream error: sessionId={}, err={}", sessionId, e.getMessage()));
    }

    /**
     * 轻量系统 Prompt — 不含 RAG（RAG 异步追加）
     */
    private String buildLightSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(promptTemplateService.getChatPrompt());

        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            prompt.append("\n\n## 学生信息\n");
            if (user.getMajor() != null && !user.getMajor().isBlank()) {
                prompt.append("- 专业：").append(user.getMajor());
            }
            if (user.getGrade() != null && !user.getGrade().isBlank()) {
                prompt.append(" | 年级：").append(user.getGrade());
            }
            prompt.append("\n");

            // 测评结果（只取类型名，精简）
            List<AssessmentResult> results = assessmentResultMapper.selectList(
                    new LambdaQueryWrapper<AssessmentResult>()
                            .eq(AssessmentResult::getUserId, userId)
                            .eq(AssessmentResult::getIsAiGenerated, 1)
                            .orderByDesc(AssessmentResult::getCreateTime)
                            .last("LIMIT 2"));
            if (!results.isEmpty()) {
                prompt.append("- 测评：").append(results.stream()
                        .map(r -> r.getResultType() != null ? r.getResultType() : "")
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining("、"))).append("\n");
            }
        }
        return prompt.toString();
    }

    public void saveFullResponse(Long userId, String sessionId, String fullResponse) {
        saveMessage(userId, sessionId, "assistant", fullResponse);
    }

    private void saveMessage(Long userId, String sessionId, String role, String content) {
        AiChatHistory msg = new AiChatHistory();
        msg.setUserId(userId);
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        chatHistoryMapper.insert(msg);
    }
}
