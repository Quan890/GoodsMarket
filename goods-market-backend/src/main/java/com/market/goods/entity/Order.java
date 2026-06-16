package com.market.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类 — 对应 order 表（MySQL 保留字，查询时需加反引号）
 *
 * status 字段含义：0=待支付 1=已支付 2=已取消 3=已完成
 * pay_method 字段含义：1=支付宝 2=微信
 *
 * @author goods-market
 */
@Data
@TableName("`order`")
public class Order {

    /** 主键ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单编号（全局唯一，由 OrderNoUtil 生成） */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 商家ID */
    private Long merchantId;

    /** 订单总金额（商品小计之和） */
    private BigDecimal totalAmount;

    /** 实付金额（扣除优惠后） */
    private BigDecimal payAmount;

    /**
     * 订单状态
     * 0=待支付 1=已支付 2=已取消 3=已完成
     */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /**
     * 支付方式
     * 1=支付宝 2=微信
     */
    private Integer payMethod;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 订单备注 */
    private String remark;

    /** 逻辑删除：0=未删除 1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
