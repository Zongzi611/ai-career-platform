package com.careersail.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.careersail.entity.AiTrainingTask;
import com.careersail.entity.AiTrainingRecord;

import java.util.Map;

public interface TrainingService {
    IPage<AiTrainingTask> listTasks(long page, long size, String category, String difficulty);
    AiTrainingTask getTask(Long id);
    void createTask(AiTrainingTask task);
    void updateTask(Long id, AiTrainingTask task);
    void deleteTask(Long id);
    void submitAnswer(Long taskId, Long userId, String answer);
    Map<String, Object> triggerScoring(Long recordId, Long userId);
    IPage<AiTrainingRecord> myRecords(Long userId, long page, long size);
    Map<String, Object> getRecord(Long recordId);
    IPage<AiTrainingRecord> listRecordsByTask(Long taskId, long page, long size);
}
