package com.careersail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careersail.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND status = 1")
    Optional<SysUser> selectByUsername(String username);
}
