package com.careersail.service;

import com.careersail.dto.AssessmentSubmitDTO;
import com.careersail.entity.AssessmentType;
import com.careersail.entity.AssessmentQuestion;
import com.careersail.vo.AssessmentResultVO;

import java.util.List;

public interface AssessmentService {
    List<AssessmentType> listTypes();
    List<AssessmentQuestion> getQuestionsWithOptions(Long typeId);
    Long startAssessment(Long typeId, Long userId);
    void submitAnswer(AssessmentSubmitDTO dto, Long userId);
    AssessmentResultVO completeAssessment(Long recordId, Long userId);
    AssessmentResultVO getResult(Long resultId);
    List<AssessmentResultVO> myResults(Long userId);
    AssessmentResultVO generateAiReport(Long resultId, Long userId);
}
