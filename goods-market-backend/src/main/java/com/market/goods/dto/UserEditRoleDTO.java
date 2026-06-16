package com.market.goods.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员修改用户角色入参
 *
 * 接口：PUT /admin/user/role
 * 说明：管理员修改指定用户的角色（如提升为商家、降级为普通用户等）
 *
 * @author goods-market
 */
@Data
public class UserEditRoleDTO {

    /**
     * 目标用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 目标角色：0=游客 1=普通用户 2=商家 3=管理员
     */
    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色值不合法")
    @Max(value = 3, message = "角色值不合法")
    private Integer role;
}
