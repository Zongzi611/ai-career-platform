package com.careersail.service;

import com.careersail.vo.DashboardVO;

public interface DashboardService {
    DashboardVO getOverview(Long teacherId);
    DashboardVO getClassDashboard(Long teacherId, String className);
}
