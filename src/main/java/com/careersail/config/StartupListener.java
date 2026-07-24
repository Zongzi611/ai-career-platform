package com.careersail.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.ai.RagService;
import com.careersail.entity.CareerInfo;
import com.careersail.mapper.CareerInfoMapper;
import com.careersail.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * On startup, rebuild the in-memory vector index from MySQL career data.
 * SimpleVectorStore is in-memory, so it needs repopulation after each restart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupListener {

    private final CareerInfoMapper careerInfoMapper;
    private final RagService ragService;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildVectorIndex() {
        try {
            List<CareerInfo> all = careerInfoMapper.selectList(
                    new LambdaQueryWrapper<CareerInfo>()
                            .eq(CareerInfo::getStatus, Constants.STATUS_ENABLED));
            if (!all.isEmpty()) {
                ragService.reIndexAll(all);
                log.info("Vector index rebuilt on startup: {} career records indexed", all.size());
            }
        } catch (Exception e) {
            log.warn("Failed to rebuild vector index on startup (non-fatal): {}", e.getMessage());
        }
    }
}
