package com.careersail.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.careersail.common.BusinessException;
import com.careersail.common.Constants;
import com.careersail.common.ErrorCode;
import com.careersail.dto.CareerQueryDTO;
import com.careersail.entity.CareerInfo;
import com.careersail.mapper.CareerInfoMapper;
import com.careersail.service.CareerInfoService;
import com.careersail.ai.RagService;
import com.careersail.vo.CareerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerInfoServiceImpl extends ServiceImpl<CareerInfoMapper, CareerInfo> implements CareerInfoService {

    private final CareerInfoMapper careerInfoMapper;
    private final RagService ragService;

    @Override
    public IPage<CareerVO> pageQuery(long page, long size, CareerQueryDTO query) {
        LambdaQueryWrapper<CareerInfo> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getPositionName()), CareerInfo::getPositionName, query.getPositionName())
                   .eq(StringUtils.hasText(query.getIndustry()), CareerInfo::getIndustry, query.getIndustry())
                   .like(StringUtils.hasText(query.getMajorMatch()), CareerInfo::getMajorMatch, query.getMajorMatch())
                   .eq(StringUtils.hasText(query.getDemandLevel()), CareerInfo::getDemandLevel, query.getDemandLevel());
            if (StringUtils.hasText(query.getKeyword())) {
                wrapper.and(w -> w.like(CareerInfo::getPositionName, query.getKeyword())
                                  .or().like(CareerInfo::getIndustry, query.getKeyword())
                                  .or().like(CareerInfo::getSkillsRequired, query.getKeyword())
                                  .or().like(CareerInfo::getDescription, query.getKeyword()));
            }
        }
        wrapper.eq(CareerInfo::getStatus, Constants.STATUS_ENABLED)
               .orderByDesc(CareerInfo::getCreateTime);
        return careerInfoMapper.selectPage(new Page<>(page, size), wrapper)
                .convert(this::toVO);
    }

    @Override
    public CareerVO getById(Long id) {
        CareerInfo info = careerInfoMapper.selectById(id);
        if (info == null) throw new BusinessException(ErrorCode.CAREER_NOT_FOUND);
        return toVO(info);
    }

    @Override
    @Transactional
    public void saveCareerInfo(CareerInfo careerInfo) {
        careerInfo.setStatus(Constants.STATUS_ENABLED);
        careerInfoMapper.insert(careerInfo);
        // Async sync to Chroma
        ragService.addDocumentAsync(careerInfo);
    }

    @Override
    @Transactional
    public void update(Long id, CareerInfo updateInfo) {
        CareerInfo info = careerInfoMapper.selectById(id);
        if (info == null) throw new BusinessException(ErrorCode.CAREER_NOT_FOUND);
        updateInfo.setId(id);
        careerInfoMapper.updateById(updateInfo);
        // Re-index in Chroma
        CareerInfo updated = careerInfoMapper.selectById(id);
        ragService.updateDocumentAsync(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CareerInfo info = careerInfoMapper.selectById(id);
        if (info == null) throw new BusinessException(ErrorCode.CAREER_NOT_FOUND);
        info.setStatus(Constants.STATUS_DISABLED);
        careerInfoMapper.updateById(info);
        ragService.deleteDocumentAsync(id);
    }

    @Override
    public List<CareerVO> recommend(String major, String skills, int topK) {
        String query = (major != null ? major : "") + " " + (skills != null ? skills : "");
        if (query.trim().isEmpty()) {
            // Return top entries if no query provided
            return careerInfoMapper.selectList(
                    new LambdaQueryWrapper<CareerInfo>()
                            .eq(CareerInfo::getStatus, Constants.STATUS_ENABLED)
                            .orderByDesc(CareerInfo::getCreateTime)
                            .last("LIMIT " + topK))
                    .stream().map(this::toVO).collect(Collectors.toList());
        }
        return ragService.searchSimilar(query, topK);
    }

    @Override
    public List<String> getIndustries() {
        return careerInfoMapper.selectList(
                new LambdaQueryWrapper<CareerInfo>()
                        .select(CareerInfo::getIndustry)
                        .eq(CareerInfo::getStatus, Constants.STATUS_ENABLED)
                        .groupBy(CareerInfo::getIndustry))
                .stream().map(CareerInfo::getIndustry).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void importFromExcel(List<CareerInfo> list) {
        for (CareerInfo info : list) {
            info.setStatus(Constants.STATUS_ENABLED);
            careerInfoMapper.insert(info);
            ragService.addDocumentAsync(info);
        }
    }

    @Override
    public void reIndexAll() {
        List<CareerInfo> all = careerInfoMapper.selectList(
                new LambdaQueryWrapper<CareerInfo>().eq(CareerInfo::getStatus, Constants.STATUS_ENABLED));
        ragService.reIndexAll(all);
    }

    private CareerVO toVO(CareerInfo info) {
        CareerVO vo = new CareerVO();
        BeanUtil.copyProperties(info, vo);
        return vo;
    }
}
