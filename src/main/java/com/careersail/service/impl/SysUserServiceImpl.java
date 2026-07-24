package com.careersail.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.careersail.common.BusinessException;
import com.careersail.common.Constants;
import com.careersail.common.ErrorCode;
import com.careersail.config.JwtTokenProvider;
import com.careersail.dto.LoginRequest;
import com.careersail.dto.LoginResponse;
import com.careersail.dto.RegisterRequest;
import com.careersail.dto.UserQueryDTO;
import com.careersail.entity.SysRole;
import com.careersail.entity.SysUser;
import com.careersail.entity.SysUserRole;
import com.careersail.mapper.SysRoleMapper;
import com.careersail.mapper.SysUserMapper;
import com.careersail.mapper.SysUserRoleMapper;
import com.careersail.service.SysUserService;
import com.careersail.util.RedisUtil;
import com.careersail.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PASSWORD_ERROR));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), roleCodes);
        long expiresIn = 24 * 60 * 60; // 24 hours in seconds

        UserVO userVO = buildUserVO(user, roleCodes);
        return LoginResponse.of(token, expiresIn, userVO);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (sysUserMapper.selectByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTS);
        }

        SysUser user = new SysUser();
        BeanUtil.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        sysUserMapper.insert(user);

        // Assign role based on request (student by default, teacher if specified)
        String roleCode = "teacher".equalsIgnoreCase(request.getRole())
                ? Constants.ROLE_TEACHER
                : Constants.ROLE_STUDENT;

        SysRole targetRole = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode)
        ).stream().findFirst().orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(targetRole.getId());
        sysUserRoleMapper.insert(userRole);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        List<String> roles = sysRoleMapper.selectRolesByUserId(userId)
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        return buildUserVO(user, roles);
    }

    @Override
    public IPage<UserVO> listUsers(long page, long size, UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                   .like(StringUtils.hasText(query.getRealName()), SysUser::getRealName, query.getRealName())
                   .like(StringUtils.hasText(query.getMajor()), SysUser::getMajor, query.getMajor())
                   .like(StringUtils.hasText(query.getClassName()), SysUser::getClassName, query.getClassName())
                   .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = sysUserMapper.selectPage(new Page<>(page, size), wrapper);
        return userPage.convert(u -> {
            List<String> roles = sysRoleMapper.selectRolesByUserId(u.getId())
                    .stream().map(SysRole::getRoleCode).collect(Collectors.toList());
            return buildUserVO(u, roles);
        });
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        List<String> roles = sysRoleMapper.selectRolesByUserId(id)
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        return buildUserVO(user, roles);
    }

    @Override
    @Transactional
    public void createUser(SysUser user, List<Long> roleIds) {
        if (sysUserMapper.selectByUsername(user.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTS);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        sysUserMapper.insert(user);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void updateUser(Long id, SysUser updateUser) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        updateUser.setId(id);
        // Don't update password through this method
        updateUser.setPassword(null);
        sysUserMapper.updateById(updateUser);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setStatus(Constants.STATUS_DISABLED);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        if (sysUserMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // Delete existing
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // Insert new
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            sysUserRoleMapper.insert(ur);
        }
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    @Override
    public void logout(String token) {
        // Add token to Redis blacklist with TTL = remaining validity
        long remaining = jwtTokenProvider.getExpiration(token).getTime() - System.currentTimeMillis();
        if (remaining > 0) {
            redisUtil.set(Constants.REDIS_TOKEN_BLACKLIST_PREFIX + token, "1", remaining, TimeUnit.MILLISECONDS);
        }
        SecurityContextHolder.clearContext();
    }

    private UserVO buildUserVO(SysUser user, List<String> roles) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        vo.setRoles(roles);
        return vo;
    }
}
