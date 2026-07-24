package com.careersail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careersail.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 系统配置 Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    @Select("SELECT * FROM sys_config WHERE config_key = #{key} AND status = 1")
    Optional<SysConfig> selectByKey(String key);
}
