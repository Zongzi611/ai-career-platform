package com.careersail.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.careersail.common.BusinessException;
import com.careersail.common.ErrorCode;
import com.careersail.entity.SysConfig;
import com.careersail.mapper.SysConfigMapper;
import com.careersail.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    @Cacheable(value = "sysConfig", key = "#key")
    public String getConfigValue(String key) {
        return sysConfigMapper.selectByKey(key)
                .map(SysConfig::getConfigValue)
                .orElse(null);
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public List<SysConfig> listConfigs() {
        return sysConfigMapper.selectList(null);
    }

    @Override
    @CacheEvict(value = "sysConfig", key = "#key")
    public void updateConfig(Long id, String configValue) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) throw new BusinessException(ErrorCode.CONFIG_NOT_FOUND);
        config.setConfigValue(configValue);
        sysConfigMapper.updateById(config);
    }

    @Override
    @CacheEvict(value = "sysConfig", allEntries = true)
    public void refreshCache() {
        // Cache cleared via annotation
    }
}
