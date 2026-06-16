package com.market.goods.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商家审核状态枚举
 *
 * 与 merchant 表 audit_status 字段一一对应：
 *   PENDING(0, "待审核")、APPROVED(1, "审核通过")、REJECTED(2, "驳回")
 *
 * @author goods-market
 */
@Getter
@AllArgsConstructor
public enum MerchantAuditEnum {

    /** 待审核 */
    PENDING(0, "待审核"),

    /** 审核通过 */
    APPROVED(1, "审核通过"),

    /** 驳回 */
    REJECTED(2, "驳回");

    /** 状态编码（与数据库 audit_status 字段对应） */
    private final int code;

    /** 状态描述 */
    private final String desc;

    /**
     * 根据编码查找枚举
     *
     * @param code 状态编码
     * @return 对应枚举，编码无效时返回 null
     */
    public static MerchantAuditEnum ofCode(int code) {
        for (MerchantAuditEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 判断是否审核通过
     */
    public static boolean isApproved(int code) {
        return APPROVED.code == code;
    }

    /**
     * 判断是否待审核
     */
    public static boolean isPending(int code) {
        return PENDING.code == code;
    }
}
