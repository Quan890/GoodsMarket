package com.market.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类视图对象（树形结构）
 *
 * 返回场景：首页分类导航、商品筛选
 * 一级分类包含 children 列表（二级分类）
 *
 * @author goods-market
 */
@Data
public class CategoryVO {

    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID（0=一级分类） */
    private Long parentId;

    /** 排序权重 */
    private Integer sortOrder;

    /** 分类图标 */
    private String icon;

    /** 二级分类列表（一级分类才有） */
    private List<CategoryVO> children;
}
