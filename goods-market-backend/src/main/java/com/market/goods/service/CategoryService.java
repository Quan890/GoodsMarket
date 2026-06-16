package com.market.goods.service;

import com.market.goods.vo.CategoryVO;

import java.util.List;

/**
 * 分类模块 Service 接口
 *
 * @author goods-market
 */
public interface CategoryService {

    /**
     * 获取分类树（一级分类 + 二级分类列表）
     *
     * @return 一级分类列表，每个一级分类内含 children 二级分类列表
     */
    List<CategoryVO> getCategoryTree();
}
