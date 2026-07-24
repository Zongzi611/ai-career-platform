package com.careersail.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.careersail.dto.CareerQueryDTO;
import com.careersail.entity.CareerInfo;
import com.careersail.vo.CareerVO;

import java.util.List;

public interface CareerInfoService {
    IPage<CareerVO> pageQuery(long page, long size, CareerQueryDTO query);
    CareerVO getById(Long id);
    void saveCareerInfo(CareerInfo careerInfo);
    void update(Long id, CareerInfo careerInfo);
    void delete(Long id);
    List<CareerVO> recommend(String major, String skills, int topK);
    List<String> getIndustries();
    void importFromExcel(List<CareerInfo> list);
    void reIndexAll();
}
