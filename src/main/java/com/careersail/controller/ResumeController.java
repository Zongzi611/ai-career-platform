package com.careersail.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.ai.ResumeAiService;
import com.careersail.common.Result;
import com.careersail.entity.ResumeRecord;
import com.careersail.mapper.ResumeRecordMapper;
import com.careersail.mapper.SysUserMapper;
import com.careersail.util.FileTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeRecordMapper resumeRecordMapper;
    private final ResumeAiService resumeAiService;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @PostMapping("/optimize")
    public Result<?> optimize(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        String originalText = body.get("originalText").toString();
        String targetPosition = body.getOrDefault("targetPosition", "").toString();
        String title = body.getOrDefault("title", "未命名简历").toString();

        ResumeRecord record = new ResumeRecord();
        record.setUserId(userId);
        record.setTitle(title);
        record.setOriginalText(originalText);
        record.setTargetPosition(targetPosition);
        record.setStatus("DRAFT");
        resumeRecordMapper.insert(record);

        String optimized = resumeAiService.optimize(record, userId);

        record.setOptimizedText(optimized);
        record.setStatus("OPTIMIZED");
        resumeRecordMapper.updateById(record);

        return Result.ok(Map.of(
                "id", record.getId(),
                "originalText", originalText,
                "optimizedText", optimized,
                "status", "OPTIMIZED"
        ));
    }

    @GetMapping("/history")
    public Result<List<ResumeRecord>> history() {
        Long userId = getCurrentUserId();
        List<ResumeRecord> list = resumeRecordMapper.selectList(
                new LambdaQueryWrapper<ResumeRecord>()
                        .eq(ResumeRecord::getUserId, userId)
                        .orderByDesc(ResumeRecord::getCreateTime));
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<ResumeRecord> getById(@PathVariable Long id) {
        return Result.ok(resumeRecordMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        resumeRecordMapper.deleteById(id);
        return Result.ok("已删除");
    }

    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail(500,"文件为空");
        String filename = file.getOriginalFilename();
        if (file.getSize() > 10 * 1024 * 1024) return Result.fail(500,"文件不能超过10MB");
        try {
            String text = FileTextExtractor.extract(file);
            if (text.isBlank()) return Result.fail(500,"未能从文件中提取文字内容");
            return Result.ok(Map.of(
                    "filename", filename != null ? filename : "unknown",
                    "text", text,
                    "length", text.length()
            ));
        } catch (Exception e) {
            return Result.fail(500,"文件解析失败: " + e.getMessage());
        }
    }
}
