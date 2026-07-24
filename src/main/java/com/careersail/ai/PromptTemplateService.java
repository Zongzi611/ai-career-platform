package com.careersail.ai;

import cn.hutool.core.util.StrUtil;
import com.careersail.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 提示词模板服务 — 从 sys_config 表读取 AI 提示词并渲染变量
 */
@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final SysConfigService sysConfigService;

    /**
     * 获取原始提示词
     */
    public String getPrompt(String configKey) {
        return sysConfigService.getConfigValue(configKey, "");
    }

    /**
     * 渲染提示词 — 替换 {{variable}} 占位符
     */
    public String renderPrompt(String configKey, Map<String, String> variables) {
        String template = getPrompt(configKey);
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        return StrUtil.format(template, variables);
    }

    // Convenience methods for known prompts
    public String getChatPrompt() {
        return getPrompt("ai.chat.prompt");
    }

    public String getScoringPrompt() {
        return getPrompt("ai.scoring.prompt");
    }

    public String getReportPrompt() {
        return getPrompt("ai.report.prompt");
    }

    public String getModelProvider() {
        return sysConfigService.getConfigValue("ai.model.provider", "ollama");
    }

    public String getModelName() {
        return sysConfigService.getConfigValue("ai.model.name", "qwen:7b");
    }
}
