package com.careersail.controller;

import com.careersail.common.BusinessException;
import com.careersail.common.ErrorCode;
import com.careersail.common.Result;
import com.careersail.entity.SysUser;
import com.careersail.dto.LoginRequest;
import com.careersail.dto.RegisterRequest;
import com.careersail.mapper.SysUserMapper;
import com.careersail.security.CurrentUser;
import com.careersail.service.SysUserService;
import com.careersail.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SysUserMapper sysUserMapper;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(sysUserService.login(request));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return Result.ok("注册成功");
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        sysUserService.logout(token);
        return Result.ok("已登出");
    }

    @GetMapping("/info")
    public Result<UserVO> info() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        String username = auth.getName();
        Long userId = sysUserMapper.selectByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getId();
        return Result.ok(sysUserService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody SysUser updateUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Long userId = sysUserMapper.selectByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getId();
        updateUser.setId(userId);
        updateUser.setPassword(null);
        updateUser.setUsername(null);
        updateUser.setStatus(null);
        sysUserMapper.updateById(updateUser);
        return Result.ok("个人信息已更新");
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Long userId = sysUserMapper.selectByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getId();
        sysUserService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.ok("密码修改成功");
    }
}
