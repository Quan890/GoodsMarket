package com.market.goods.controller;

import com.market.goods.service.CategoryService;
import com.market.goods.util.Result;
import com.market.goods.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类模块 Controller
 *
 * 权限：公开访问（游客可浏览分类）
 *
 * @author goods-market
 */
@Tag(name = "商品分类", description = "分类树查询（公开）")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类树（一级 + 二级）
     *
     * 权限：公开访问
     * 请求：GET /api/category/tree
     *
     * 返回示例：
     * [
     *   {
     *     "id": 1, "name": "服装", "parentId": 0,
     *     "children": [
     *       {"id": 101, "name": "T恤", "parentId": 1},
     *       {"id": 102, "name": "卫衣", "parentId": 1}
     *     ]
     *   },
     *   ...
     * ]
     */
    @Operation(summary = "分类树", description = "获取一级+二级分类树形结构")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> tree = categoryService.getCategoryTree();
        return Result.ok(tree);
    }
}
