package com.careersail.controller;

import com.careersail.common.Result;
import com.careersail.dto.TeacherTaskDTO;
import com.careersail.entity.StudentReport;
import com.careersail.mapper.SysUserMapper;
import com.careersail.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping("/students")
    public Result<?> listStudents(@RequestParam(required = false) String className,
                                   @RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "10") long size) {
        var result = teacherService.listStudents(getCurrentUserId(), className, page, size);
        return Result.ok(Map.of("records", result.getRecords(), "total", result.getTotal(), "page", result.getCurrent(), "size", result.getSize()));
    }

    @PostMapping("/students")
    public Result<?> addStudent(@RequestBody Map<String, Object> body) {
        teacherService.addStudentToClass(
                body.get("className").toString(),
                Long.valueOf(body.get("studentId").toString()),
                getCurrentUserId());
        return Result.ok("添加成功");
    }

    @DeleteMapping("/students/{id}")
    public Result<?> removeStudent(@PathVariable Long id) {
        teacherService.removeStudentFromClass(id);
        return Result.ok("移除成功");
    }

    @GetMapping("/tasks")
    public Result<?> listTasks(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long size) {
        var result = teacherService.listTasks(getCurrentUserId(), page, size);
        return Result.ok(Map.of("records", result.getRecords(), "total", result.getTotal(), "page", result.getCurrent(), "size", result.getSize()));
    }

    @PostMapping("/tasks")
    public Result<?> createTask(@RequestBody TeacherTaskDTO dto) {
        teacherService.createTask(getCurrentUserId(), dto);
        return Result.ok("创建成功");
    }

    @PutMapping("/tasks/{id}")
    public Result<?> updateTask(@PathVariable Long id, @RequestBody TeacherTaskDTO dto) {
        teacherService.updateTask(id, dto);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/tasks/{id}")
    public Result<?> deleteTask(@PathVariable Long id) {
        teacherService.deleteTask(id);
        return Result.ok("删除成功");
    }

    @GetMapping("/tasks/{id}/progress")
    public Result<?> getTaskProgress(@PathVariable Long id) {
        return Result.ok(teacherService.getTaskProgress(id));
    }

    @GetMapping("/reports")
    public Result<?> listReports(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "10") long size) {
        var result = teacherService.listReports(getCurrentUserId(), page, size);
        return Result.ok(Map.of("records", result.getRecords(), "total", result.getTotal(), "page", result.getCurrent(), "size", result.getSize()));
    }

    @PostMapping("/reports")
    public Result<?> createReport(@RequestBody Map<String, Object> body) {
        teacherService.createReport(
                getCurrentUserId(),
                Long.valueOf(body.get("studentId").toString()),
                body.get("reportType").toString(),
                body.get("title").toString(),
                body.get("content").toString());
        return Result.ok("报告创建成功");
    }

    @GetMapping("/reports/{id}")
    public Result<StudentReport> getReport(@PathVariable Long id) {
        return Result.ok(teacherService.getReport(id));
    }

    @DeleteMapping("/reports/{id}")
    public Result<?> deleteReport(@PathVariable Long id) {
        teacherService.deleteReport(id);
        return Result.ok("删除成功");
    }

    @GetMapping("/export/students")
    public ResponseEntity<byte[]> exportStudents(@RequestParam(required = false) String className) {
        byte[] data = teacherService.exportStudents(getCurrentUserId(), className);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/student-detail/{studentId}")
    public Result<?> studentDetail(@PathVariable Long studentId) {
        return Result.ok(teacherService.getStudentDetail(studentId));
    }

    @GetMapping("/grade-overview")
    public Result<?> gradeOverview(@RequestParam(required = false) String college) {
        return Result.ok(teacherService.getGradeOverview(getCurrentUserId(), college));
    }

    @PostMapping("/import-students")
    public Result<?> importStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail(400, "文件为空");
        int count = teacherService.importStudentsFromExcel(getCurrentUserId(), file);
        return Result.ok(Map.of("imported", count, "message", "成功导入 " + count + " 名学生"));
    }

    @GetMapping("/task-stats")
    public Result<?> taskStats() {
        return Result.ok(teacherService.getTaskStats(getCurrentUserId()));
    }
}
