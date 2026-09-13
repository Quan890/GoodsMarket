package com.market.goods.service;

import com.market.goods.dto.*;

import java.util.Map;

/**
 * 用户模块 Service 接口
 *
 * @author goods-market
 */
public interface UserService {

    /** 发送短信验证码 */
    void sendVerificationCode(SendCodeDTO dto);

    /** 手机号 + 验证码登录（自动注册） */
    Map<String, Object> login(LoginDTO dto);

    /** 密码登录 */
    Map<String, Object> loginByPassword(PasswordLoginDTO dto);

    /** 用户注册 */
    void register(RegisterDTO dto);

    /** 找回密码（重置密码） */
    void resetPassword(ResetPasswordDTO dto);

    /** 修改密码（需登录，校验旧密码） */
    void changePassword(Long userId, ChangePasswordDTO dto);

    /** 注销账户 */
    void deleteAccount(Long userId, DeleteAccountDTO dto);

    /** 获取用户信息 */
    Map<String, Object> getUserInfo(Long userId);

    /** 退出登录 */
    void logout();
}
