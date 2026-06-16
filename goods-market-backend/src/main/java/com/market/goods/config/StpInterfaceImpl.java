package com.market.goods.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.goods.entity.User;
import com.market.goods.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 自定义权限认证接口实现
 *
 * Sa-Token 调用 StpUtil.checkRole() / StpUtil.checkPermission() 时，
 * 会回调本类的方法来获取当前登录用户的角色和权限列表。
 *
 * 本项目使用角色体系：
 *   guest(0) / user(1) / merchant(2) / admin(3)
 *
 * Sa-Token 配置中 checkRole 使用的标识：
 *   "user"     → 普通用户(role=1)
 *   "merchant" → 商家(role=2)
 *   "admin"    → 管理员(role=3)
 *
 * @author goods-market
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    /**
     * 返回指定用户拥有的角色列表
     *
     * Sa-Token 执行 StpUtil.checkRole("user") 时回调此方法，
     * 将返回的角色列表与要求的角色做匹配判断。
     *
     * 实现逻辑：根据 loginId（即用户ID）查询数据库中的 role 字段，
     * 映射为字符串角色名返回。
     *
     * @param loginId 用户ID（即 StpUtil.login(userId) 时传入的值）
     * @param roleType 角色类型标识（本项目未使用多角色体系，传入 null）
     * @return 角色标识列表，如 ["user"] 或 ["merchant"]
     */
    @Override
    public List<String> getRoleList(Object loginId, String roleType) {
        List<String> roles = new ArrayList<>();

        // 优先从 session 读取（doLogin 时已存储，性能更好）
        String cachedRole = (String) StpUtil.getSession().get("role");
        if (cachedRole != null) {
            roles.add(cachedRole);
            return roles;
        }

        // session 中没有时，从数据库查询（兜底方案）
        try {
            Long userId = Long.parseLong(loginId.toString());
            User user = userMapper.selectById(userId);
            if (user != null && user.getRole() != null) {
                String roleName = switch (user.getRole()) {
                    case 3 -> "admin";
                    case 2 -> "merchant";
                    case 1 -> "user";
                    default -> "guest";
                };
                roles.add(roleName);
                // 缓存到 session，避免后续请求重复查库
                StpUtil.getSession().set("role", roleName);
            }
        } catch (Exception e) {
            // 查询失败返回空列表，checkRole 会拦截
        }

        return roles;
    }

    /**
     * 返回指定用户拥有的权限列表
     *
     * 本项目仅使用角色校验（checkRole），未细化到权限粒度（checkPermission），
     * 返回空列表即可。
     *
     * @param loginId 用户ID
     * @param permissionType 权限类型标识（本项目未使用）
     * @return 权限标识列表（空）
     */
    @Override
    public List<String> getPermissionList(Object loginId, String permissionType) {
        return new ArrayList<>();
    }
}
