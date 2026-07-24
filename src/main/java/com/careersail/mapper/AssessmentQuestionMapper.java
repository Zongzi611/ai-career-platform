package com.careersail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careersail.entity.AssessmentQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AssessmentQuestionMapper extends BaseMapper<AssessmentQuestion> {
    @Select("SELECT * FROM assessment_question WHERE type_id = #{typeId} ORDER BY sort_order")
    List<AssessmentQuestion> selectByTypeId(Long typeId);
}
