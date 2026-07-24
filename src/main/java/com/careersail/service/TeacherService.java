package com.careersail.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.careersail.dto.TeacherTaskDTO;
import com.careersail.entity.*;
import com.careersail.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TeacherService {
    IPage<UserVO> listStudents(Long teacherId, String className, long page, long size);
    void addStudentToClass(String className, Long studentId, Long teacherId);
    void removeStudentFromClass(Long id);
    IPage<TeacherTask> listTasks(Long teacherId, long page, long size);
    void createTask(Long teacherId, TeacherTaskDTO dto);
    void updateTask(Long id, TeacherTaskDTO dto);
    void deleteTask(Long id);
    List<Map<String, Object>> getTaskProgress(Long taskId);
    IPage<StudentReport> listReports(Long teacherId, long page, long size);
    void createReport(Long teacherId, Long studentId, String reportType, String title, String content);
    StudentReport getReport(Long id);
    void deleteReport(Long id);
    byte[] exportStudents(Long teacherId, String className);
    List<Map<String, Object>> getGradeOverview(Long teacherId, String college);
    Map<String, Object> getStudentDetail(Long studentId);
    int importStudentsFromExcel(Long teacherId, MultipartFile file);
    List<Map<String, Object>> getTaskStats(Long teacherId);
}
