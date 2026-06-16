package com.market.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类 — 对应 order_item 表
 *
 * 注意：product_name、product_image 为下单时的商品快照字段，
 *       即使商品后续修改了名称/图片，订单明细中仍保留下单时的值。
 *
 * @author goods-market
 */
@Data
@TableName("order_item")
public class OrderItem {

    /** 主键ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单ID（关联 order 表） */
    private Long orderId;

    /** 订单编号（冗余字段，方便查询） */
    private String orderNo;

    /** 商品ID */
    private Long productId;

    /** 商品名称（下单时快照） */
    private String productName;

    /** 商品图片（下单时快照） */
    private String productImage;

    /** 下单时单价 */
    private BigDecimal unitPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额 = unitPrice × quantity */
    private BigDecimal totalPrice;

    /** 逻辑删除：0=未删除 1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
