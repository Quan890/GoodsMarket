package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.market.goods.dto.CartOperateDTO;
import com.market.goods.service.CartService;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.util.Result;
import com.market.goods.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车模块 Controller
 *
 * 权限说明（配合 SaTokenConfig）：
 *   所有接口均在 /user/** 路径下，需要登录 + 角色 user/merchant/admin
 *   游客（role=0）无法访问购物车接口
 *
 * @author goods-market
 */
@Tag(name = "购物车", description = "购物车增删改查")
@RestController
@RequestMapping("/user/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 新增商品到购物车
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：POST /api/user/cart/add
     * 请求头：Authorization: Bearer {token}
     * 参数：{"productId": 1001, "quantity": 2}
     *
     * 说明：
     *   - 如果购物车中已有该商品，数量会累加（不重复插入）
     *   - 自动校验商品是否存在、是否上架、库存是否充足
     */
    @Operation(summary = "新增购物车商品", description = "添加商品到购物车，已有则累加数量")
    @PostMapping("/add")
    public Result<Void> addToCart(@Valid @RequestBody CartOperateDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        cartService.addToCart(userId, dto);
        return Result.ok(null, "已加入购物车");
    }

    /**
     * 修改购物车商品数量
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：PUT /api/user/cart/quantity
     * 请求头：Authorization: Bearer {token}
     * 参数：{"productId": 1001, "quantity": 5}
     *
     * 说明：
     *   - quantity 为最终数量（不是增减量）
     *   - quantity=0 时自动删除该商品
     *   - 自动校验库存限制
     */
    @Operation(summary = "修改购物车数量", description = "设置商品数量，设为0则删除")
    @PutMapping("/quantity")
    public Result<Void> updateQuantity(@Valid @RequestBody CartOperateDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        cartService.updateQuantity(userId, dto);
        return Result.ok(null, "数量修改成功");
    }

    /**
     * 删除购物车单项
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：DELETE /api/user/cart/delete?productId=1001
     * 请求头：Authorization: Bearer {token}
     *
     * 说明：逻辑删除，不会真正删除记录
     */
    @Operation(summary = "删除购物车商品", description = "从购物车中移除指定商品")
    @DeleteMapping("/delete")
    public Result<Void> deleteCartItem(@RequestParam Long productId) {
        long userId = StpUtil.getLoginIdAsLong();
        cartService.deleteCartItem(userId, productId);
        return Result.ok(null, "已从购物车移除");
    }

    /**
     * 查询个人购物车列表
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：GET /api/user/cart/list?pageNum=1&pageSize=50
     * 请求头：Authorization: Bearer {token}
     *
     * 返回 CartVO：购物车ID、商品ID、商品名称、图片、价格、原价、数量、
     *             库存、选中状态、商品上下架状态、小计金额
     *
     * 说明：
     *   - 价格、库存为实时数据（从 product 表关联查询）
     *   - 下架商品仍会展示（productStatus=0），前端置灰处理
     *   - 小计 subtotal = price × quantity（SQL 层计算）
     */
    @Operation(summary = "购物车列表", description = "查询购物车商品列表，关联最新商品信息")
    @GetMapping("/list")
    public Result<PageResult<CartVO>> listMyCart(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        PageResult<CartVO> result = cartService.listMyCart(userId, pageNum, pageSize);
        return Result.ok(result);
    }
}
