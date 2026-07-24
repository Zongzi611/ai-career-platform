package com.careersail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careersail.entity.AssessmentOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AssessmentOptionMapper extends BaseMapper<AssessmentOption> {
    @Select("SELECT * FROM assessment_option WHERE question_id = #{questionId} ORDER BY sort_order")
    List<AssessmentOption> selectByQuestionId(Long questionId);
}
