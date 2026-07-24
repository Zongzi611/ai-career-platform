package com.careersail.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.careersail.dto.LoginRequest;
import com.careersail.dto.LoginResponse;
import com.careersail.dto.RegisterRequest;
import com.careersail.dto.UserQueryDTO;
import com.careersail.entity.SysUser;
import com.careersail.vo.UserVO;

import java.util.List;

/**
 * 用户服务
 */
public interface SysUserService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    UserVO getUserInfo(Long userId);

    IPage<UserVO> listUsers(long page, long size, UserQueryDTO query);

    UserVO getUserById(Long id);

    void createUser(SysUser user, List<Long> roleIds);

    void updateUser(Long id, SysUser user);

    void deleteUser(Long id);

    void assignRoles(Long userId, List<Long> roleIds);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void resetPassword(Long userId, String newPassword);

    void logout(String token);
}
