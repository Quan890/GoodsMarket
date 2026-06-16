package com.market.goods.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注销账户入参
 *
 * 接口：POST /user/delete-account
 */
@Data
public class DeleteAccountDTO {

    /** 确认密码（安全校验） */
    @NotBlank(message = "请输入密码确认操作")
    private String password;
}
