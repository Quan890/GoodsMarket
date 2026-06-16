package com.market.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品收藏实体类 — 对应 product_collect 表
 *
 * 联合唯一索引 (user_id, product_id) 防止同一用户重复收藏同一商品
 * 收藏/取消收藏通过逻辑删除实现：取消收藏时将 deleted 设为 1，再次收藏时更新为 0
 *
 * @author goods-market
 */
@Data
@TableName("product_collect")
public class ProductCollect {

    /** 主键ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 逻辑删除：0=未删除 1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
