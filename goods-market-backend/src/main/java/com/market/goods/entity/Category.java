package com.market.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类 — 对应 category 表
 *
 * 两级分类结构：parent_id=0 为一级分类，parent_id=一级分类ID 为二级分类
 *
 * @author goods-market
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID（0=一级分类） */
    private Long parentId;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    /** 分类图标URL */
    private String icon;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
