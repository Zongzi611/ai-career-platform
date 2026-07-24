package com.careersail.controller;

import com.careersail.common.Result;
import com.careersail.dto.UserQueryDTO;
import com.careersail.entity.SysUser;
import com.careersail.service.SysUserService;
import com.careersail.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SysUserService sysUserService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") long page,
                           @RequestParam(defaultValue = "10") long size,
                           UserQueryDTO query) {
        var result = sysUserService.listUsers(page, size, query);
        return Result.ok(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize()
        ));
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.ok(sysUserService.getUserById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        // Map body fields to user using Hutool BeanUtil
        cn.hutool.core.bean.BeanUtil.fillBeanWithMap(body, user, true);
        @SuppressWarnings("unchecked")
        List<Integer> roleIdInts = (List<Integer>) body.get("roleIds");
        List<Long> roleIds = roleIdInts != null ? roleIdInts.stream().map(Long::valueOf).toList() : List.of();
        sysUserService.createUser(user, roleIds);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysUser user) {
        sysUserService.updateUser(id, user);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/roles")
    public Result<?> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Integer>> body) {
        List<Long> roleIds = body.get("roleIds").stream().map(Long::valueOf).toList();
        sysUserService.assignRoles(id, roleIds);
        return Result.ok("角色分配成功");
    }

    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id, "123456");
        return Result.ok("密码已重置为 123456");
    }
}
