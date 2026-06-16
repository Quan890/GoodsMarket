package com.market.goods.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * 与 order 表 status 字段一一对应：
 *   UNPAID(0, "待支付")、PAID(1, "已支付")、CANCELLED(2, "已取消")、COMPLETED(3, "已完成")
 *
 * 状态流转：
 *   待支付 → 已支付（用户付款）
 *   待支付 → 已取消（用户主动取消 / 超时未支付系统自动取消）
 *   已支付 → 已完成（用户确认收货 / 系统自动确认）
 *   已支付 → 已取消（仅退款场景，需业务层校验）
 *
 * @author goods-market
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    /** 待支付 */
    UNPAID(0, "待支付"),

    /** 已支付 */
    PAID(1, "已支付"),

    /** 已取消 */
    CANCELLED(2, "已取消"),

    /** 已完成 */
    COMPLETED(3, "已完成");

    /** 状态编码（与数据库 status 字段对应） */
    private final int code;

    /** 状态描述 */
    private final String desc;

    /**
     * 根据编码查找枚举
     *
     * @param code 状态编码
     * @return 对应枚举，编码无效时返回 null
     */
    public static OrderStatusEnum ofCode(int code) {
        for (OrderStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 判断订单是否可以支付（仅待支付状态可支付）
     */
    public static boolean canPay(int code) {
        return UNPAID.code == code;
    }

    /**
     * 判断订单是否可以取消（待支付或已支付状态可取消）
     */
    public static boolean canCancel(int code) {
        return UNPAID.code == code || PAID.code == code;
    }

    /**
     * 判断订单是否已完成
     */
    public static boolean isCompleted(int code) {
        return COMPLETED.code == code;
    }
}
