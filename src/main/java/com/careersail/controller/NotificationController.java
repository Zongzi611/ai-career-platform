package com.careersail.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.careersail.common.Result;
import com.careersail.entity.Notification;
import com.careersail.mapper.NotificationMapper;
import com.careersail.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping
    public Result<?> list() {
        Long userId = getCurrentUserId();
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime)
                        .last("LIMIT 20"));
        long unread = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        return Result.ok(Map.of("list", list, "unread", unread));
    }

    @PutMapping("/read-all")
    public Result<?> readAll() {
        Long userId = getCurrentUserId();
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, 1));
        return Result.ok("已读");
    }

    @PutMapping("/{id}/read")
    public Result<?> readOne(@PathVariable Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null) { n.setIsRead(1); notificationMapper.updateById(n); }
        return Result.ok("已读");
    }
}
