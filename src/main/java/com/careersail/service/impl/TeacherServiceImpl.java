package com.careersail.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.careersail.common.BusinessException;
import com.careersail.common.Constants;
import com.careersail.common.ErrorCode;
import com.careersail.dto.TeacherTaskDTO;
import com.careersail.entity.*;
import com.careersail.mapper.*;
import com.careersail.service.TeacherService;
import com.careersail.util.ExcelUtil;
import com.careersail.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ClassStudentMapper classStudentMapper;
    private final TeacherTaskMapper teacherTaskMapper;
    private final StudentReportMapper studentReportMapper;
    private final AssessmentRecordMapper assessmentRecordMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final AssessmentTypeMapper assessmentTypeMapper;
    private final AiTrainingRecordMapper trainingRecordMapper;
    private final AiTrainingTaskMapper aiTrainingTaskMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationMapper notificationMapper;

    @Override
    public IPage<UserVO> listStudents(Long teacherId, String className, long page, long size) {
        LambdaQueryWrapper<ClassStudent> csWrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isBlank()) csWrapper.eq(ClassStudent::getClassName, className);
        csWrapper.eq(ClassStudent::getTeacherId, teacherId);
        List<Long> studentIds = classStudentMapper.selectList(csWrapper)
                .stream().map(ClassStudent::getStudentId).toList();

        if (studentIds.isEmpty()) {
            return new Page<UserVO>(page, size).setRecords(List.of()).setTotal(0);
        }

        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, studentIds);
        Page<SysUser> userPage = sysUserMapper.selectPage(new Page<>(page, size), userWrapper);
        return userPage.convert(u -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(u, vo);
            vo.setRoles(sysRoleMapper.selectRolesByUserId(u.getId())
                    .stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
            return vo;
        });
    }

    @Override
    @Transactional
    public void addStudentToClass(String className, Long studentId, Long teacherId) {
        if (classStudentMapper.selectOne(new LambdaQueryWrapper<ClassStudent>()
                .eq(ClassStudent::getClassName, className)
                .eq(ClassStudent::getStudentId, studentId)) != null) {
            throw new BusinessException(ErrorCode.CLASS_STUDENT_EXISTS);
        }
        ClassStudent cs = new ClassStudent();
        cs.setClassName(className);
        cs.setStudentId(studentId);
        cs.setTeacherId(teacherId);
        classStudentMapper.insert(cs);
    }

    @Override
    public void removeStudentFromClass(Long id) {
        classStudentMapper.deleteById(id);
    }

    @Override
    public IPage<TeacherTask> listTasks(Long teacherId, long page, long size) {
        return teacherTaskMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<TeacherTask>()
                        .eq(TeacherTask::getTeacherId, teacherId)
                        .orderByDesc(TeacherTask::getCreateTime));
    }

    @Override
    public void createTask(Long teacherId, TeacherTaskDTO dto) {
        TeacherTask task = new TeacherTask();
        BeanUtil.copyProperties(dto, task);
        task.setTeacherId(teacherId);
        task.setStatus(Constants.TASK_ACTIVE);
        teacherTaskMapper.insert(task);

        // Notify affected students
        try {
            List<ClassStudent> students = classStudentMapper.selectList(
                    new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getTeacherId, teacherId));
            if (task.getTargetClass() != null && !"ALL".equals(task.getTargetClass())) {
                students = students.stream().filter(s -> task.getTargetClass().equals(s.getClassName())).toList();
            }
            for (ClassStudent cs : students) {
                Notification n = new Notification();
                n.setUserId(cs.getStudentId());
                n.setTitle("新任务：" + task.getTitle());
                n.setMessage("老师给你下发了一个" + ("ASSESSMENT".equals(task.getTaskType())?"测评":"实训") + "任务，请及时完成。");
                notificationMapper.insert(n);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void updateTask(Long id, TeacherTaskDTO dto) {
        TeacherTask task = teacherTaskMapper.selectById(id);
        if (task == null) throw new BusinessException(ErrorCode.TEACHER_TASK_NOT_FOUND);
        BeanUtil.copyProperties(dto, task);
        task.setId(id);
        teacherTaskMapper.updateById(task);
    }

    @Override
    public void deleteTask(Long id) {
        TeacherTask task = teacherTaskMapper.selectById(id);
        if (task != null) {
            task.setStatus(Constants.TASK_CLOSED);
            teacherTaskMapper.updateById(task);
        }
    }

    @Override
    public List<Map<String, Object>> getTaskProgress(Long taskId) {
        TeacherTask task = teacherTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ErrorCode.TEACHER_TASK_NOT_FOUND);

        List<ClassStudent> classStudents = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassName, task.getTargetClass()));
        List<Map<String, Object>> progress = new ArrayList<>();
        for (ClassStudent cs : classStudents) {
            SysUser student = sysUserMapper.selectById(cs.getStudentId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", cs.getStudentId());
            item.put("studentName", student != null ? student.getRealName() : "");
            // Check if student completed the referenced task
            boolean completed = false;
            if ("ASSESSMENT".equals(task.getTaskType())) {
                completed = assessmentRecordMapper.selectCount(new LambdaQueryWrapper<AssessmentRecord>()
                        .eq(AssessmentRecord::getUserId, cs.getStudentId())
                        .eq(AssessmentRecord::getTypeId, task.getRefId())
                        .eq(AssessmentRecord::getStatus, Constants.ASSESSMENT_COMPLETED)) > 0;
            } else if ("TRAINING".equals(task.getTaskType())) {
                completed = trainingRecordMapper.selectCount(new LambdaQueryWrapper<AiTrainingRecord>()
                        .eq(AiTrainingRecord::getUserId, cs.getStudentId())
                        .eq(AiTrainingRecord::getTaskId, task.getRefId())
                        .eq(AiTrainingRecord::getStatus, Constants.TRAINING_SCORED)) > 0;
            }
            item.put("completed", completed);
            progress.add(item);
        }
        return progress;
    }

    @Override
    public IPage<StudentReport> listReports(Long teacherId, long page, long size) {
        // Get teacher's students
        List<Long> studentIds = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getTeacherId, teacherId))
                .stream().map(ClassStudent::getStudentId).toList();

        if (studentIds.isEmpty()) {
            return new Page<StudentReport>(page, size).setRecords(List.of()).setTotal(0);
        }

        // Combine: teacher-created reports + student AI assessment results
        List<StudentReport> allReports = new ArrayList<>();

        // 1. Teacher-created reports
        allReports.addAll(studentReportMapper.selectList(
                new LambdaQueryWrapper<StudentReport>().eq(StudentReport::getTeacherId, teacherId)));

        // 2. Student AI assessment results (auto-generate report entries)
        List<AssessmentResult> results = assessmentResultMapper.selectList(
                new LambdaQueryWrapper<AssessmentResult>()
                        .in(AssessmentResult::getUserId, studentIds)
                        .eq(AssessmentResult::getIsAiGenerated, 1)
                        .orderByDesc(AssessmentResult::getCreateTime));
        for (AssessmentResult r : results) {
            SysUser student = sysUserMapper.selectById(r.getUserId());
            StudentReport sr = new StudentReport();
            sr.setId(r.getId() + 100000); // offset to avoid ID collision
            sr.setStudentId(r.getUserId());
            sr.setTeacherId(teacherId);
            sr.setReportType("ASSESSMENT_SUMMARY");
            sr.setReportTitle((student != null ? student.getRealName() : "学生") + " 的测评报告");
            sr.setReportContent(r.getReportText() != null ? r.getReportText() : "测评完成，结果类型：" + r.getResultType());
            sr.setCreateTime(r.getCreateTime());
            allReports.add(sr);
        }

        // Sort by create time descending and paginate
        allReports.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        int total = allReports.size();
        int from = (int) ((page - 1) * size);
        int to = Math.min(from + (int) size, total);
        List<StudentReport> paged = from < total ? allReports.subList(from, to) : List.of();

        Page<StudentReport> resultPage = new Page<>(page, size);
        resultPage.setRecords(paged);
        resultPage.setTotal(total);
        return resultPage;
    }

    @Override
    public void createReport(Long teacherId, Long studentId, String reportType, String title, String content) {
        StudentReport report = new StudentReport();
        report.setStudentId(studentId);
        report.setTeacherId(teacherId);
        report.setReportType(reportType);
        report.setReportTitle(title);
        report.setReportContent(content);
        studentReportMapper.insert(report);
    }

    @Override
    public StudentReport getReport(Long id) {
        // Check if this is an assessment-result-backed report (ID > 100000)
        if (id > 100000) {
            Long resultId = id - 100000;
            AssessmentResult result = assessmentResultMapper.selectById(resultId);
            if (result == null) throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
            SysUser student = sysUserMapper.selectById(result.getUserId());
            StudentReport sr = new StudentReport();
            sr.setId(id);
            sr.setStudentId(result.getUserId());
            sr.setReportType("ASSESSMENT_SUMMARY");
            sr.setReportTitle((student != null ? student.getRealName() : "学生") + " 的测评报告");
            sr.setReportContent(result.getReportText() != null ? result.getReportText() : "测评结果：" + result.getResultType());
            sr.setCreateTime(result.getCreateTime());
            return sr;
        }
        StudentReport report = studentReportMapper.selectById(id);
        if (report == null) throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        return report;
    }

    @Override
    public void deleteReport(Long id) {
        studentReportMapper.deleteById(id);
    }

    @Override
    public byte[] exportStudents(Long teacherId, String className) {
        var page = listStudents(teacherId, className, 1, 9999);
        List<Map<String, Object>> data = page.getRecords().stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("realName", u.getRealName());
            map.put("studentNo", u.getStudentNo());
            map.put("college", u.getCollege());
            map.put("major", u.getMajor());
            map.put("className", u.getClassName());
            return map;
        }).collect(Collectors.toList());
        return ExcelUtil.exportToExcel(data,
                new String[]{"姓名", "学号", "学院", "专业", "班级"},
                new String[]{"realName", "studentNo", "college", "major", "className"});
    }

    @Override
    public List<Map<String, Object>> getGradeOverview(Long teacherId, String college) {
        List<Long> studentIds = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getTeacherId, teacherId))
                .stream().map(ClassStudent::getStudentId).toList();
        if (studentIds.isEmpty()) return List.of();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long sid : studentIds) {
            SysUser s = sysUserMapper.selectById(sid);
            if (s == null) continue;
            if (college != null && !college.isBlank() && !college.equals(s.getCollege())) continue;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", sid);
            row.put("realName", s.getRealName());
            row.put("studentNo", s.getStudentNo());
            row.put("college", s.getCollege());
            row.put("major", s.getMajor());
            row.put("className", s.getClassName());

            // MBTI result
            AssessmentResult mbti = assessmentResultMapper.selectOne(
                    new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, sid).eq(AssessmentResult::getTypeId, 1L).orderByDesc(AssessmentResult::getCreateTime).last("LIMIT 1"));
            row.put("mbti", mbti != null ? mbti.getResultType() : "-");

            // Holland result
            AssessmentResult holland = assessmentResultMapper.selectOne(
                    new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, sid).eq(AssessmentResult::getTypeId, 2L).orderByDesc(AssessmentResult::getCreateTime).last("LIMIT 1"));
            row.put("holland", holland != null ? holland.getResultType() : "-");

            // Training stats
            List<AiTrainingRecord> trains = trainingRecordMapper.selectList(
                    new LambdaQueryWrapper<AiTrainingRecord>().eq(AiTrainingRecord::getUserId, sid));
            long doneCount = trains.stream().filter(t -> t.getAiScore() != null).count();
            double avgScore = trains.stream().filter(t -> t.getAiScore() != null).mapToInt(AiTrainingRecord::getAiScore).average().orElse(0);
            row.put("trainingDone", doneCount);
            row.put("trainingTotal", trains.size());
            row.put("avgScore", trains.isEmpty() ? "-" : String.format("%.0f", avgScore));

            // Assigned task completion
            long assignedTotal = teacherTaskMapper.selectCount(new LambdaQueryWrapper<TeacherTask>()
                    .eq(TeacherTask::getStatus, "ACTIVE")
                    .and(w -> w.eq(TeacherTask::getTargetClass, "ALL").or().eq(TeacherTask::getTargetClass, s.getClassName())));
            row.put("assignedTotal", assignedTotal);

            result.add(row);
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> getStudentDetail(Long studentId) {
        SysUser user = sysUserMapper.selectById(studentId);
        if (user == null) return Map.of();
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", user.getId()); d.put("realName", user.getRealName());
        d.put("studentNo", user.getStudentNo()); d.put("college", user.getCollege());
        d.put("major", user.getMajor()); d.put("className", user.getClassName());
        d.put("grade", user.getGrade());
        List<AssessmentResult> ars = assessmentResultMapper.selectList(
                new LambdaQueryWrapper<AssessmentResult>().eq(AssessmentResult::getUserId, studentId).orderByDesc(AssessmentResult::getCreateTime));
        List<Map<String, Object>> al = new ArrayList<>();
        for (AssessmentResult r : ars) {
            AssessmentType t = assessmentTypeMapper.selectById(r.getTypeId());
            Map<String, Object> am = new LinkedHashMap<>();
            am.put("typeName", t != null ? t.getName() : ""); am.put("resultType", r.getResultType());
            am.put("reportText", r.getReportText()); am.put("isAiGenerated", r.getIsAiGenerated());
            am.put("createTime", r.getCreateTime()); al.add(am);
        }
        d.put("assessments", al);
        List<AiTrainingRecord> trs = trainingRecordMapper.selectList(
                new LambdaQueryWrapper<AiTrainingRecord>().eq(AiTrainingRecord::getUserId, studentId).orderByDesc(AiTrainingRecord::getSubmitTime));
        List<Map<String, Object>> tl = new ArrayList<>();
        for (AiTrainingRecord tr : trs) {
            AiTrainingTask tk = aiTrainingTaskMapper.selectById(tr.getTaskId());
            Map<String, Object> tm = new LinkedHashMap<>();
            tm.put("taskTitle", tk != null ? tk.getTitle() : "任务#"+tr.getTaskId());
            tm.put("aiScore", tr.getAiScore()); tm.put("aiFeedback", tr.getAiFeedback());
            tm.put("status", tr.getStatus()); tm.put("submitTime", tr.getSubmitTime()); tl.add(tm);
        }
        d.put("trainings", tl);
        return d;
    }

    public int importStudentsFromExcel(Long teacherId, MultipartFile file) {
        int count = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            SysRole studentRole = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, Constants.ROLE_STUDENT))
                    .stream().findFirst().orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String realName = getCell(row, 0);
                String studentNo = getCell(row, 1);
                String college = getCell(row, 2);
                String major = getCell(row, 3);
                String className = getCell(row, 4);
                String grade = getCell(row, 5);
                if (realName.isBlank()) continue;

                String username = "stu" + studentNo.replaceAll("[^0-9]", "");
                if (sysUserMapper.selectByUsername(username).isPresent()) {
                    username = username + "_" + System.currentTimeMillis() % 10000;
                }

                SysUser user = new SysUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(studentNo != null && studentNo.length() >= 6 ? studentNo.substring(studentNo.length() - 6) : "123456"));
                user.setRealName(realName);
                user.setStudentNo(studentNo);
                user.setCollege(college);
                user.setMajor(major);
                user.setClassName(className);
                user.setGrade(grade);
                user.setStatus(1);
                sysUserMapper.insert(user);

                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(studentRole.getId());
                sysUserRoleMapper.insert(ur);

                ClassStudent cs = new ClassStudent();
                cs.setClassName(className != null ? className : "");
                cs.setStudentId(user.getId());
                cs.setTeacherId(teacherId);
                classStudentMapper.insert(cs);

                count++;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CAREER_IMPORT_FAILED, "导入失败: " + e.getMessage());
        }
        return count;
    }

    @Override
    public List<Map<String, Object>> getTaskStats(Long teacherId) {
        List<TeacherTask> tasks = teacherTaskMapper.selectList(
                new LambdaQueryWrapper<TeacherTask>()
                        .eq(TeacherTask::getTeacherId, teacherId)
                        .eq(TeacherTask::getStatus, "ACTIVE")
                        .orderByDesc(TeacherTask::getCreateTime));

        List<Map<String, Object>> stats = new ArrayList<>();
        for (TeacherTask task : tasks) {
            List<ClassStudent> students = classStudentMapper.selectList(
                    new LambdaQueryWrapper<ClassStudent>()
                            .eq(ClassStudent::getTeacherId, teacherId));
            if (task.getTargetClass() != null && !"ALL".equals(task.getTargetClass())) {
                students = students.stream().filter(s -> task.getTargetClass().equals(s.getClassName())).toList();
            }

            long completed = 0;
            for (ClassStudent cs : students) {
                boolean done = false;
                if ("ASSESSMENT".equals(task.getTaskType())) {
                    done = assessmentRecordMapper.selectCount(new LambdaQueryWrapper<AssessmentRecord>()
                            .eq(AssessmentRecord::getUserId, cs.getStudentId())
                            .eq(AssessmentRecord::getTypeId, task.getRefId())) > 0;
                } else {
                    done = trainingRecordMapper.selectCount(new LambdaQueryWrapper<AiTrainingRecord>()
                            .eq(AiTrainingRecord::getUserId, cs.getStudentId())
                            .eq(AiTrainingRecord::getTaskId, task.getRefId())) > 0;
                }
                if (done) completed++;
            }

            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("taskId", task.getId());
            stat.put("title", task.getTitle());
            stat.put("taskType", task.getTaskType());
            stat.put("targetClass", task.getTargetClass());
            stat.put("totalStudents", students.size());
            stat.put("completed", completed);
            stat.put("percent", students.isEmpty() ? 0 : Math.round((double) completed / students.size() * 100));
            stats.add(stat);
        }
        return stats;
    }

    private String getCell(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}
