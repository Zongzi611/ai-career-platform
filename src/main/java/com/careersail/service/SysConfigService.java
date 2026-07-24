package com.careersail.service;

import com.careersail.entity.SysConfig;
import java.util.List;

public interface SysConfigService {
    String getConfigValue(String key);
    String getConfigValue(String key, String defaultValue);
    List<SysConfig> listConfigs();
    void updateConfig(Long id, String configValue);
    void refreshCache();
}
