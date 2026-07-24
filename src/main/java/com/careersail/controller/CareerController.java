package com.careersail.controller;

import cn.hutool.core.bean.BeanUtil;
import com.careersail.common.Result;
import com.careersail.dto.CareerQueryDTO;
import com.careersail.entity.CareerInfo;
import com.careersail.service.CareerInfoService;
import com.careersail.vo.CareerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
public class CareerController {

    private final CareerInfoService careerInfoService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") long page,
                           @RequestParam(defaultValue = "10") long size,
                           CareerQueryDTO query) {
        var result = careerInfoService.pageQuery(page, size, query);
        return Result.ok(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize()
        ));
    }

    @GetMapping("/{id}")
    public Result<CareerVO> getById(@PathVariable Long id) {
        return Result.ok(careerInfoService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> create(@RequestBody CareerInfo careerInfo) {
        careerInfoService.saveCareerInfo(careerInfo);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@PathVariable Long id, @RequestBody CareerInfo careerInfo) {
        careerInfoService.update(id, careerInfo);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        careerInfoService.delete(id);
        return Result.ok("删除成功");
    }

    @GetMapping("/recommend")
    public Result<List<CareerVO>> recommend(
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String skills,
            @RequestParam(defaultValue = "10") int topK) {
        return Result.ok(careerInfoService.recommend(major, skills, topK));
    }

    @GetMapping("/industries")
    public Result<List<String>> industries() {
        return Result.ok(careerInfoService.getIndustries());
    }

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> reindex() {
        careerInfoService.reIndexAll();
        return Result.ok("重索引任务已启动");
    }
}
