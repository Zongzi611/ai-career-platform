package com.careersail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.ai.ChatAiService;
import com.careersail.entity.AiChatHistory;
import com.careersail.entity.SysUser;
import com.careersail.mapper.AiChatHistoryMapper;
import com.careersail.mapper.SysUserMapper;
import com.careersail.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ChatAiService chatAiService;
    private final AiChatHistoryMapper chatHistoryMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public String createSession(Long userId) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public Flux<String> streamChat(Long userId, String sessionId, String message) {
        SysUser user = sysUserMapper.selectById(userId);
        String major = user != null ? user.getMajor() : null;
        return chatAiService.streamChat(userId, sessionId, message, major);
    }

    public void saveAssistantMessage(Long userId, String sessionId, String fullResponse) {
        chatAiService.saveFullResponse(userId, sessionId, fullResponse);
    }

    @Override
    public List<Map<String, Object>> getHistory(String sessionId) {
        return chatHistoryMapper.selectList(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getSessionId, sessionId)
                        .orderByAsc(AiChatHistory::getCreateTime))
                .stream().map(h -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("role", h.getRole());
                    map.put("content", h.getContent());
                    map.put("createTime", h.getCreateTime());
                    return map;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSessions(Long userId) {
        // Get all sessions for user, ordered by most recent first
        List<AiChatHistory> all = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<AiChatHistory>()
                        .select(AiChatHistory::getSessionId, AiChatHistory::getCreateTime,
                                AiChatHistory::getRole, AiChatHistory::getContent)
                        .eq(AiChatHistory::getUserId, userId)
                        .orderByDesc(AiChatHistory::getCreateTime));

        // Dedupe by sessionId, keep first occurrence (most recent message)
        // Also capture the first user message as the session title
        Set<String> seen = new LinkedHashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiChatHistory h : all) {
            if (seen.add(h.getSessionId())) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("sessionId", h.getSessionId());
                map.put("createTime", h.getCreateTime());
                result.add(map);
            }
        }

        // For each session, find the first user message as title
        for (Map<String, Object> session : result) {
            String sid = (String) session.get("sessionId");
            AiChatHistory firstUserMsg = chatHistoryMapper.selectOne(
                    new LambdaQueryWrapper<AiChatHistory>()
                            .eq(AiChatHistory::getSessionId, sid)
                            .eq(AiChatHistory::getRole, "user")
                            .orderByAsc(AiChatHistory::getCreateTime)
                            .last("LIMIT 1"));
            String title = firstUserMsg != null && firstUserMsg.getContent() != null
                    ? (firstUserMsg.getContent().length() > 20
                        ? firstUserMsg.getContent().substring(0, 20) + "..."
                        : firstUserMsg.getContent())
                    : "新对话";
            session.put("title", title);

            // Count messages
            Long msgCount = chatHistoryMapper.selectCount(
                    new LambdaQueryWrapper<AiChatHistory>()
                            .eq(AiChatHistory::getSessionId, sid));
            session.put("messageCount", msgCount);
        }

        return result;
    }

    @Override
    public void deleteSession(String sessionId) {
        chatHistoryMapper.delete(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getSessionId, sessionId));
    }
}
