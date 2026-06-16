package com.market.goods.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * 与 user 表 role 字段一一对应：
 *   GUEST(0, "游客")、USER(1, "普通用户")、MERCHANT(2, "商家")、ADMIN(3, "管理员")
 *
 * 使用示例：
 *   UserRoleEnum.USER.getCode()        → 1
 *   UserRoleEnum.USER.getDesc()        → "普通用户"
 *   UserRoleEnum.ofCode(2)             → MERCHANT
 *
 * @author goods-market
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    /** 游客 — 仅可浏览公开商品 */
    GUEST(0, "游客"),

    /** 普通用户 — 可浏览、收藏、下单 */
    USER(1, "普通用户"),

    /** 商家 — 可管理店铺和商品 */
    MERCHANT(2, "商家"),

    /** 管理员 — 拥有全部权限 */
    ADMIN(3, "管理员");

    /** 角色编码（与数据库 role 字段对应） */
    private final int code;

    /** 角色描述 */
    private final String desc;

    /**
     * 根据编码查找枚举
     *
     * @param code 角色编码
     * @return 对应枚举，编码无效时返回 null
     */
    public static UserRoleEnum ofCode(int code) {
        for (UserRoleEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 判断给定编码是否为管理员
     */
    public static boolean isAdmin(int code) {
        return ADMIN.code == code;
    }

    /**
     * 判断给定编码是否为商家或管理员（拥有商家级权限）
     */
    public static boolean isMerchantOrAbove(int code) {
        return code >= MERCHANT.code;
    }
}
