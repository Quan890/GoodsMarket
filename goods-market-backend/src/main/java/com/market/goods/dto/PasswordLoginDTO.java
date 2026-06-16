package com.market.goods.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码登录入参
 *
 * 接口：POST /user/login-password
 */
@Data
public class PasswordLoginDTO {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 图形验证码文本 */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /** 图形验证码 token（前端从 /user/captcha 获取） */
    @NotBlank(message = "验证码token不能为空")
    private String captchaToken;
}
