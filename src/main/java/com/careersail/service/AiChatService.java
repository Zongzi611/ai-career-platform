package com.careersail.service;

import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;

public interface AiChatService {
    String createSession(Long userId);
    Flux<String> streamChat(Long userId, String sessionId, String message);
    List<Map<String, Object>> getHistory(String sessionId);
    List<Map<String, Object>> getSessions(Long userId);
    void deleteSession(String sessionId);
}
