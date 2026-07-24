package com.careersail.ai;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiServiceFactory {

    private final ChatModel chatModel;
    private final PromptTemplateService promptTemplateService;

    @Getter
    private String activeProvider;

    public AiServiceFactory(ChatModel chatModel, PromptTemplateService promptTemplateService) {
        this.chatModel = chatModel;
        this.promptTemplateService = promptTemplateService;
    }

    @PostConstruct
    public void init() {
        this.activeProvider = promptTemplateService.getModelProvider();
        log.info("AI Service initialized with provider: {}, model: {}",
                activeProvider, promptTemplateService.getModelName());
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public void refresh() {
        this.activeProvider = promptTemplateService.getModelProvider();
        log.info("AI Service refreshed, now using provider: {}", activeProvider);
    }
}
