package com.market.goods.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送短信验证码入参
 *
 * 接口：POST /user/send-code
 * 说明：向指定手机号发送6位数字验证码，有效期5分钟，60秒内不可重复发送
 *
 * @author goods-market
 */
@Data
public class SendCodeDTO {

    /**
     * 手机号（11位中国大陆手机号）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
