package com.market.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类 — 对应 product 表
 *
 * 注意：version 字段为乐观锁版本号，更新库存时 MyBatis-Plus 会自动附加 WHERE version = ?
 *       若版本号不匹配则更新失败，业务层需捕获并提示"操作频繁，请重试"
 *
 * @author goods-market
 */
@Data
@TableName("product")
public class Product {

    /** 主键ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 商家ID */
    private Long merchantId;

    /** 商品名称 */
    private String name;

    /** 副标题 */
    private String subtitle;

    /** 分类ID */
    private Long categoryId;

    /** 主图URL */
    private String mainImage;

    /** 商品图片集（JSON 数组字符串） */
    private String images;

    /** 商品详情（富文本 HTML） */
    private String detail;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /**
     * 商品状态
     * 0=下架 1=上架
     */
    private Integer status;

    /**
     * 乐观锁版本号（防超卖）
     * 每次 UPDATE 成功后自动 +1，更新时 WHERE 条件携带 version 值
     */
    @Version
    private Integer version;

    /** 排序权重（数值越大越靠前） */
    private Integer sortOrder;

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
