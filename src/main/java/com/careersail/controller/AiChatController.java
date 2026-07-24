package com.careersail.controller;

import com.careersail.common.Result;
import com.careersail.entity.SysUser;
import com.careersail.mapper.SysUserMapper;
import com.careersail.service.AiChatService;
import com.careersail.service.impl.AiChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;
    private final AiChatServiceImpl aiChatServiceImpl;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @PostMapping("/session")
    public Result<Map<String, String>> createSession() {
        return Result.ok(Map.of("sessionId", aiChatService.createSession(getCurrentUserId())));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String sessionId, @RequestParam String message) {
        Long userId = getCurrentUserId();
        AtomicReference<String> fullResponse = new AtomicReference<>("");
        return aiChatService.streamChat(userId, sessionId, message)
                .doOnNext(chunk -> {
                    // 收集纯文本（Spring AI M6 已自带 SSE 外层格式，此处只收集内容）
                    String text = chunk.startsWith("data:") ? chunk.substring(5).trim() : chunk;
                    if (!text.isBlank()) {
                        fullResponse.updateAndGet(v -> v + text);
                    }
                })
                .doOnComplete(() -> {
                    String response = fullResponse.get();
                    if (!response.isBlank()) {
                        aiChatServiceImpl.saveAssistantMessage(userId, sessionId, response);
                    }
                });
                // 注意：Spring AI M6 ChatClient.stream().content() 已自带 data: 前缀
                // 不再额外 .map() 包装，避免双重 data:data:
    }

    @GetMapping("/sessions")
    public Result<?> getSessions() {
        return Result.ok(aiChatService.getSessions(getCurrentUserId()));
    }

    @GetMapping("/history/{sessionId}")
    public Result<?> getHistory(@PathVariable String sessionId) {
        return Result.ok(aiChatService.getHistory(sessionId));
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<?> deleteSession(@PathVariable String sessionId) {
        aiChatService.deleteSession(sessionId);
        return Result.ok("已删除");
    }
}
