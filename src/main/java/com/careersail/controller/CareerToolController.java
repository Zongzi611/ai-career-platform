package com.careersail.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.ai.LearningPathService;
import com.careersail.ai.SkillGapService;
import com.careersail.common.Result;
import com.careersail.entity.CareerBookmark;
import com.careersail.entity.CareerInfo;
import com.careersail.mapper.CareerBookmarkMapper;
import com.careersail.mapper.CareerInfoMapper;
import com.careersail.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/career-tools")
@RequiredArgsConstructor
public class CareerToolController {

    private final CareerBookmarkMapper bookmarkMapper;
    private final CareerInfoMapper careerInfoMapper;
    private final SkillGapService skillGapService;
    private final LearningPathService learningPathService;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    // ====== Bookmark APIs ======

    @PostMapping("/bookmark/{careerId}")
    public Result<?> addBookmark(@PathVariable Long careerId) {
        Long userId = getCurrentUserId();
        if (bookmarkMapper.selectOne(new LambdaQueryWrapper<CareerBookmark>()
                .eq(CareerBookmark::getUserId, userId)
                .eq(CareerBookmark::getCareerId, careerId)) != null) {
            return Result.ok("已收藏");
        }
        CareerBookmark bm = new CareerBookmark();
        bm.setUserId(userId);
        bm.setCareerId(careerId);
        bookmarkMapper.insert(bm);
        return Result.ok("收藏成功");
    }

    @DeleteMapping("/bookmark/{careerId}")
    public Result<?> removeBookmark(@PathVariable Long careerId) {
        Long userId = getCurrentUserId();
        bookmarkMapper.delete(new LambdaQueryWrapper<CareerBookmark>()
                .eq(CareerBookmark::getUserId, userId)
                .eq(CareerBookmark::getCareerId, careerId));
        return Result.ok("已取消收藏");
    }

    @GetMapping("/bookmarks")
    public Result<?> getBookmarks() {
        Long userId = getCurrentUserId();
        List<CareerBookmark> bms = bookmarkMapper.selectList(
                new LambdaQueryWrapper<CareerBookmark>()
                        .eq(CareerBookmark::getUserId, userId)
                        .orderByDesc(CareerBookmark::getCreateTime));
        if (bms.isEmpty()) return Result.ok(List.of());
        List<Long> ids = bms.stream().map(CareerBookmark::getCareerId).toList();
        List<CareerInfo> careers = careerInfoMapper.selectBatchIds(ids);
        // Preserve bookmark order
        Map<Long, CareerInfo> map = careers.stream().collect(Collectors.toMap(CareerInfo::getId, c -> c));
        List<CareerInfo> ordered = ids.stream().map(map::get).filter(Objects::nonNull).toList();
        return Result.ok(ordered);
    }

    @GetMapping("/bookmark/ids")
    public Result<?> getBookmarkIds() {
        Long userId = getCurrentUserId();
        List<CareerBookmark> bms = bookmarkMapper.selectList(
                new LambdaQueryWrapper<CareerBookmark>()
                        .eq(CareerBookmark::getUserId, userId));
        return Result.ok(bms.stream().map(CareerBookmark::getCareerId).toList());
    }

    // ====== Skill Gap API ======

    @GetMapping("/salary-stats")
    public Result<?> salaryStats() {
        List<CareerInfo> all = careerInfoMapper.selectList(
                new LambdaQueryWrapper<CareerInfo>().eq(CareerInfo::getStatus, 1));

        // By industry
        Map<String, List<CareerInfo>> byIndustry = all.stream().collect(Collectors.groupingBy(c -> c.getIndustry() != null ? c.getIndustry() : "其他"));
        List<Map<String, Object>> industryStats = byIndustry.entrySet().stream().map(e -> {
            double avgMin = e.getValue().stream().mapToInt(CareerInfo::getSalaryMin).average().orElse(0);
            double avgMax = e.getValue().stream().mapToInt(CareerInfo::getSalaryMax).average().orElse(0);
            return Map.<String, Object>of("industry", e.getKey(), "count", e.getValue().size(), "avgMin", Math.round(avgMin), "avgMax", Math.round(avgMax));
        }).sorted((a,b) -> Integer.compare((int)b.get("count"), (int)a.get("count"))).toList();

        // By demand level
        Map<String, Long> demandCounts = all.stream().collect(Collectors.groupingBy(c -> c.getDemandLevel() != null ? c.getDemandLevel() : "UNKNOWN", Collectors.counting()));

        // Top 10 salary positions
        List<Map<String, Object>> topSalary = all.stream()
                .sorted((a,b) -> Integer.compare(b.getSalaryMax(), a.getSalaryMax()))
                .limit(10)
                .map(c -> Map.<String, Object>of("name", c.getPositionName(), "industry", c.getIndustry(), "min", c.getSalaryMin(), "max", c.getSalaryMax()))
                .toList();

        return Result.ok(Map.of("industryStats", industryStats, "demandCounts", demandCounts, "topSalary", topSalary, "total", all.size()));
    }

    @GetMapping("/learning-path/{careerId}")
    public Result<?> learningPath(@PathVariable Long careerId) {
        Long userId = getCurrentUserId();
        return Result.ok(learningPathService.generate(careerId, userId));
    }

    @GetMapping("/skill-gap/{careerId}")
    public Result<?> analyzeSkillGap(@PathVariable Long careerId) {
        Long userId = getCurrentUserId();
        String analysis = skillGapService.analyze(userId, careerId);
        CareerInfo career = careerInfoMapper.selectById(careerId);
        return Result.ok(Map.of(
                "careerId", careerId,
                "positionName", career != null ? career.getPositionName() : "",
                "requiredSkills", career != null ? career.getSkillsRequired() : "",
                "analysis", analysis
        ));
    }
}
