package com.careersail.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.careersail.ai.ScoringAiService;
import com.careersail.common.BusinessException;
import com.careersail.common.Constants;
import com.careersail.common.ErrorCode;
import com.careersail.entity.AiTrainingRecord;
import com.careersail.entity.AiTrainingTask;
import com.careersail.mapper.AiTrainingRecordMapper;
import com.careersail.mapper.AiTrainingTaskMapper;
import com.careersail.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl extends ServiceImpl<AiTrainingTaskMapper, AiTrainingTask> implements TrainingService {

    private final AiTrainingTaskMapper taskMapper;
    private final AiTrainingRecordMapper recordMapper;
    private final ScoringAiService scoringAiService;

    @Override
    public IPage<AiTrainingTask> listTasks(long page, long size, String category, String difficulty) {
        LambdaQueryWrapper<AiTrainingTask> wrapper = new LambdaQueryWrapper<AiTrainingTask>()
                .eq(AiTrainingTask::getStatus, Constants.STATUS_ENABLED)
                .eq(category != null, AiTrainingTask::getCategory, category)
                .eq(difficulty != null, AiTrainingTask::getDifficulty, difficulty)
                .orderByDesc(AiTrainingTask::getCreateTime);
        return taskMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public AiTrainingTask getTask(Long id) {
        AiTrainingTask task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(ErrorCode.TRAINING_TASK_NOT_FOUND);
        return task;
    }

    @Override
    public void createTask(AiTrainingTask task) {
        task.setStatus(Constants.STATUS_ENABLED);
        taskMapper.insert(task);
    }

    @Override
    public void updateTask(Long id, AiTrainingTask task) {
        task.setId(id);
        taskMapper.updateById(task);
    }

    @Override
    public void deleteTask(Long id) {
        AiTrainingTask task = taskMapper.selectById(id);
        if (task != null) {
            task.setStatus(Constants.STATUS_DISABLED);
            taskMapper.updateById(task);
        }
    }

    @Override
    @Transactional
    public void submitAnswer(Long taskId, Long userId, String answer) {
        if (recordMapper.selectOne(new LambdaQueryWrapper<AiTrainingRecord>()
                .eq(AiTrainingRecord::getUserId, userId)
                .eq(AiTrainingRecord::getTaskId, taskId)) != null) {
            throw new BusinessException(ErrorCode.TRAINING_ALREADY_SUBMITTED);
        }
        AiTrainingRecord record = new AiTrainingRecord();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setUserAnswer(answer);
        record.setStatus(Constants.TRAINING_SUBMITTED);
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.insert(record);
    }

    @Override
    @Transactional
    public Map<String, Object> triggerScoring(Long recordId, Long userId) {
        AiTrainingRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (!record.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);

        AiTrainingTask task = taskMapper.selectById(record.getTaskId());

        ScoringAiService.ScoreResult result = scoringAiService.scoreAnswer(task, record.getUserAnswer());

        record.setAiScore(result.score());
        record.setAiFeedback(result.feedback());
        record.setStatus(Constants.TRAINING_SCORED);
        record.setScoreTime(LocalDateTime.now());
        recordMapper.updateById(record);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("score", result.score());
        map.put("feedback", result.feedback());
        map.put("status", Constants.TRAINING_SCORED);
        return map;
    }

    @Override
    public IPage<AiTrainingRecord> myRecords(Long userId, long page, long size) {
        return recordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<AiTrainingRecord>()
                        .eq(AiTrainingRecord::getUserId, userId)
                        .orderByDesc(AiTrainingRecord::getSubmitTime));
    }

    @Override
    public Map<String, Object> getRecord(Long recordId) {
        AiTrainingRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        AiTrainingTask task = taskMapper.selectById(record.getTaskId());
        Map<String, Object> map = new LinkedHashMap<>();
        BeanUtil.beanToMap(record, map, false, true);
        map.put("taskTitle", task != null ? task.getTitle() : "");
        return map;
    }

    @Override
    public IPage<AiTrainingRecord> listRecordsByTask(Long taskId, long page, long size) {
        return recordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<AiTrainingRecord>().eq(AiTrainingRecord::getTaskId, taskId));
    }
}
