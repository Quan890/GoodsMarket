package com.market.goods.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机号验证码登录入参
 *
 * 接口：POST /user/login
 * 说明：手机号 + 短信验证码登录/注册，首次登录自动创建账号
 *
 * @author goods-market
 */
@Data
public class LoginDTO {

    /**
     * 手机号（11位中国大陆手机号）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 短信验证码（6位数字）
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    private String code;
}
