package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.market.goods.dto.*;
import com.market.goods.service.UserService;
import com.market.goods.util.CaptchaUtil;
import com.market.goods.util.RedisUtil;
import com.market.goods.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户模块 Controller
 *
 * 公开接口：send-code, login, login-password, register, reset-password, captcha, logout
 * 需登录接口：delete-account, logout
 *
 * @author goods-market
 */
@Tag(name = "用户模块", description = "登录、注册、验证码、找回密码、注销账户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RedisUtil redisUtil;

    // ==================== 图形验证码 ====================

    /**
     * 获取图形验证码
     *
     * 权限：公开访问
     * 请求：GET /api/user/captcha
     *
     * 返回：{ captchaToken, captchaImage (Base64) }
     * 前端将 captchaToken 和用户输入的 captchaCode 一起提交到登录/注册接口
     */
    @Operation(summary = "获取图形验证码", description = "返回验证码图片Base64和token")
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        String code = CaptchaUtil.generateCode();
        String image = CaptchaUtil.generateImage(code);
        String token = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis，5 分钟过期
        redisUtil.set(CaptchaUtil.CAPTCHA_PREFIX + token, code,
                CaptchaUtil.CAPTCHA_EXPIRE_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

        Map<String, String> data = new HashMap<>();
        data.put("captchaToken", token);
        data.put("captchaImage", image);
        return Result.ok(data);
    }

    // ==================== 发送短信验证码 ====================

    /**
     * 发送短信验证码
     *
     * 权限：公开访问
     * 请求：POST /api/user/send-code
     */
    @Operation(summary = "发送短信验证码", description = "60秒内不可重复发送")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.sendVerificationCode(dto);
        return Result.ok(null, "验证码发送成功");
    }

    // ==================== 注册 ====================

    /**
     * 用户注册（手机号 + 短信验证码 + 用户名 + 密码）
     *
     * 权限：公开访问
     * 请求：POST /api/user/register
     */
    @Operation(summary = "用户注册", description = "手机号+短信验证码+用户名+密码注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok(null, "注册成功");
    }

    // ==================== 登录 ====================

    /**
     * 手机号 + 短信验证码登录（首次自动注册）
     *
     * 权限：公开访问
     * 请求：POST /api/user/login
     */
    @Operation(summary = "短信验证码登录", description = "手机号+验证码登录，首次自动注册")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    /**
     * 用户名 + 密码登录（需图形验证码）
     *
     * 权限：公开访问
     * 请求：POST /api/user/login-password
     */
    @Operation(summary = "密码登录", description = "用户名+密码+图形验证码登录")
    @PostMapping("/login-password")
    public Result<Map<String, Object>> loginByPassword(@Valid @RequestBody PasswordLoginDTO dto) {
        return Result.ok(userService.loginByPassword(dto));
    }

    // ==================== 找回密码 ====================

    /**
     * 找回密码（手机号 + 短信验证码 + 新密码）
     *
     * 权限：公开访问
     * 请求：POST /api/user/reset-password
     */
    @Operation(summary = "找回密码", description = "手机号+短信验证码重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.ok(null, "密码重置成功，请重新登录");
    }

    // ==================== 用户信息 ====================

    /**
     * 获取当前登录用户信息
     *
     * 权限：需要登录
     * 请求：GET /api/user/info
     *
     * 用于前端刷新页面后恢复用户状态
     */
    @Operation(summary = "获取用户信息", description = "根据token获取当前登录用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userService.getUserInfo(userId));
    }

    // ==================== 修改密码 ====================

    /**
     * 修改密码（需登录）
     *
     * 权限：需要登录
     * 请求：PUT /api/user/password
     * 参数：{"oldPassword": "xxx", "newPassword": "yyy"}
     *
     * 校验原密码通过后更新为新密码，并强制退出登录（需重新登录）
     */
    @Operation(summary = "修改密码", description = "校验原密码后修改密码，修改后需重新登录")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        userService.changePassword(userId, dto);
        return Result.ok(null, "密码修改成功，请重新登录");
    }

    // ==================== 注销账户 ====================

    /**
     * 注销账户（需登录）
     *
     * 权限：需要登录
     * 请求：POST /api/user/delete-account
     *
     * 业务规则：
     *   - 管理员不能注销
     *   - 商家需先删除所有商品
     *   - 需输入密码确认
     */
    @Operation(summary = "注销账户", description = "管理员不可注销，商家需先删除商品")
    @PostMapping("/delete-account")
    public Result<Void> deleteAccount(@Valid @RequestBody DeleteAccountDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        userService.deleteAccount(userId, dto);
        return Result.ok(null, "账户已注销");
    }

    // ==================== 退出登录 ====================

    /**
     * 退出登录
     *
     * 权限：需要登录
     * 请求：POST /api/user/logout
     */
    @Operation(summary = "退出登录", description = "注销当前Sa-Token会话")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.ok(null, "退出成功");
    }
}
