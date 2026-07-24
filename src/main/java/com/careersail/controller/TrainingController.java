package com.careersail.controller;

import com.careersail.common.Result;
import com.careersail.entity.AiTrainingTask;
import com.careersail.entity.SysUser;
import com.careersail.entity.TeacherTask;
import com.careersail.mapper.SysUserMapper;
import com.careersail.mapper.TeacherTaskMapper;
import com.careersail.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final SysUserMapper sysUserMapper;
    private final TeacherTaskMapper teacherTaskMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping("/tasks")
    public Result<?> listTasks(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long size,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String difficulty) {
        var result = trainingService.listTasks(page, size, category, difficulty);
        return Result.ok(Map.of("records", result.getRecords(), "total", result.getTotal(), "page", result.getCurrent(), "size", result.getSize()));
    }

    @GetMapping("/tasks/{id}")
    public Result<AiTrainingTask> getTask(@PathVariable Long id) {
        return Result.ok(trainingService.getTask(id));
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Result<?> createTask(@RequestBody AiTrainingTask task) {
        task.setCreateBy(getCurrentUserId());
        trainingService.createTask(task);
        return Result.ok("创建成功");
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Result<?> updateTask(@PathVariable Long id, @RequestBody AiTrainingTask task) {
        trainingService.updateTask(id, task);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Result<?> deleteTask(@PathVariable Long id) {
        trainingService.deleteTask(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Map<String, Object> body) {
        Long taskId = Long.valueOf(body.get("taskId").toString());
        String answer = body.get("userAnswer").toString();
        trainingService.submitAnswer(taskId, getCurrentUserId(), answer);
        return Result.ok("提交成功");
    }

    @GetMapping("/assigned-tasks")
    public Result<?> getAssignedTasks() {
        Long userId = getCurrentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getClassName() == null) return Result.ok(List.of());
        // Get tasks assigned to this student's class or ALL
        List<TeacherTask> tasks = teacherTaskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeacherTask>()
                        .eq(TeacherTask::getStatus, "ACTIVE")
                        .and(w -> w.eq(TeacherTask::getTargetClass, user.getClassName())
                                 .or().eq(TeacherTask::getTargetClass, "ALL"))
                        .orderByDesc(TeacherTask::getCreateTime));
        return Result.ok(tasks);
    }

    @PostMapping("/score/{recordId}")
    public Result<?> triggerScoring(@PathVariable Long recordId) {
        return Result.ok(trainingService.triggerScoring(recordId, getCurrentUserId()));
    }

    @GetMapping("/my-records")
    public Result<?> myRecords(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long size) {
        var result = trainingService.myRecords(getCurrentUserId(), page, size);
        return Result.ok(Map.of("records", result.getRecords(), "total", result.getTotal(), "page", result.getCurrent(), "size", result.getSize()));
    }

    @GetMapping("/record/{id}")
    public Result<?> getRecord(@PathVariable Long id) {
        return Result.ok(trainingService.getRecord(id));
    }

    @GetMapping("/records/task/{taskId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Result<?> listRecordsByTask(@PathVariable Long taskId,
                                        @RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "100") long size) {
        var result = trainingService.listRecordsByTask(taskId, page, size);
        return Result.ok(Map.of("records", result.getRecords(), "total", result.getTotal()));
    }
}
