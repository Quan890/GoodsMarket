package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.goods.entity.Category;
import com.market.goods.mapper.CategoryMapper;
import com.market.goods.service.CategoryService;
import com.market.goods.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类模块 Service 实现类
 *
 * @author goods-market
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 获取分类树
     *
     * 流程：
     *   1. 查询所有未删除的分类
     *   2. 按 parentId 分组
     *   3. 一级分类挂载对应的二级分类 children
     */
    @Override
    public List<CategoryVO> getCategoryTree() {
        // 查询所有分类（按 sort_order 降序）
        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByDesc(Category::getSortOrder)
        );

        // 按 parentId 分组：key=parentId, value=该父分类下的所有子分类
        Map<Long, List<Category>> groupByParent = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        // 构建一级分类树（parentId=0 的是一级分类）
        List<Category> topCategories = groupByParent.getOrDefault(0L, new ArrayList<>());

        return topCategories.stream().map(top -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(top.getId());
            vo.setName(top.getName());
            vo.setParentId(top.getParentId());
            vo.setSortOrder(top.getSortOrder());
            vo.setIcon(top.getIcon());

            // 挂载二级分类
            List<Category> children = groupByParent.getOrDefault(top.getId(), new ArrayList<>());
            vo.setChildren(children.stream().map(child -> {
                CategoryVO childVo = new CategoryVO();
                childVo.setId(child.getId());
                childVo.setName(child.getName());
                childVo.setParentId(child.getParentId());
                childVo.setSortOrder(child.getSortOrder());
                childVo.setIcon(child.getIcon());
                return childVo;
            }).toList());

            return vo;
        }).toList();
    }
}
