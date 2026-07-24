package com.careersail.controller;

import com.careersail.common.Result;
import com.careersail.mapper.SysUserMapper;
import com.careersail.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping("/overview")
    public Result<?> overview() {
        return Result.ok(dashboardService.getOverview(getCurrentUserId()));
    }

    @GetMapping("/class")
    public Result<?> classDashboard(@RequestParam String className) {
        return Result.ok(dashboardService.getClassDashboard(getCurrentUserId(), className));
    }
}
