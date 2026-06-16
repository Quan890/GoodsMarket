package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.goods.entity.Merchant;
import com.market.goods.entity.Product;
import com.market.goods.mapper.MerchantMapper;
import com.market.goods.service.ProductService;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.util.Result;
import com.market.goods.vo.OrderVO;
import com.market.goods.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品模块 Controller
 *
 * 权限分层：
 *   —— 公开接口（游客可访问） ——
 *   GET  /product/list               — 商品分页列表
 *   GET  /product/detail/{id}        — 商品详情
 *
 *   —— 商家接口（需登录 + 商家/管理员角色） ——
 *   POST   /merchant/product/add     — 新增商品
 *   PUT    /merchant/product/edit    — 编辑商品
 *   PUT    /merchant/product/status  — 上下架商品
 *   DELETE /merchant/product/delete/{id} — 删除商品
 *   GET    /merchant/product/my      — 查询我的商品
 *   GET    /merchant/product/orders  — 查询我的商品订单
 *
 * @author goods-market
 */
@Tag(name = "商品模块", description = "商品CRUD、公开查询、商家商品管理")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MerchantMapper merchantMapper;

    /**
     * 根据当前登录用户ID获取商家ID
     *
     * Sa-Token 的 loginId 是用户ID（user.id），但商品表用的是商家ID（merchant.id）
     * 需要通过 user_id 关联查询 merchant 表获取正确的商家ID
     */
    private Long getCurrentMerchantId() {
        long userId = StpUtil.getLoginIdAsLong();
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null) {
            throw new com.market.goods.exception.BusinessException("商家信息不存在，请先完成入驻申请");
        }
        return merchant.getId();
    }

    // ==================== 公开商品接口（游客可访问） ====================

    /**
     * 商品分页列表查询（公开）
     *
     * 权限：游客可访问（无需 token）
     * 请求：GET /api/product/list?pageNum=1&pageSize=10&keyword=手机
     *
     * 只返回上架(status=1)且未删除的商品，关联商家店铺名
     */
    @Operation(summary = "商品分页列表", description = "游客可访问，支持关键词搜索和分类筛选")
    @GetMapping("/product/list")
    public Result<PageResult<ProductVO>> listProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        PageResult<ProductVO> result = productService.listPublicProducts(pageNum, pageSize, keyword, categoryId);
        return Result.ok(result);
    }

    /**
     * 商品详情查询（公开）
     *
     * 权限：游客可访问（无需 token）
     * 请求：GET /api/product/detail/1001
     *
     * 返回完整商品信息 + 店铺名
     */
    @Operation(summary = "商品详情", description = "游客可访问，返回完整商品信息和店铺名")
    @GetMapping("/product/detail/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        ProductVO vo = productService.getProductDetail(id);
        return Result.ok(vo);
    }

    // ==================== 商家商品管理接口 ====================

    /**
     * 商家新增商品
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：POST /api/merchant/product/add
     * 请求头：Authorization: Bearer {token}
     *
     * 说明：
     *   - merchantId 从 Sa-Token 会话中自动获取（不信任前端传入）
     *   - 一个商家主营一类商品（业务约定，非强制校验）
     *   - 商品图片存放在项目 static/images/products/ 目录，数据库保存相对路径
     */
    @Operation(summary = "商家新增商品", description = "自动绑定商家ID，新增商品默认上架")
    @PostMapping("/merchant/product/add")
    public Result<Void> addProduct(@RequestBody Product product) {
        long merchantId = getCurrentMerchantId();
        productService.addProduct(merchantId, product);
        return Result.ok(null, "商品新增成功");
    }

    /**
     * 商家编辑商品
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：PUT /api/merchant/product/edit
     * 请求头：Authorization: Bearer {token}
     *
     * 校验：商品必须属于当前商家，否则返回"无权操作他人的商品"
     */
    @Operation(summary = "商家编辑商品", description = "校验商品归属，只能编辑自己名下商品")
    @PutMapping("/merchant/product/edit")
    public Result<Void> editProduct(@RequestBody Product product) {
        long merchantId = getCurrentMerchantId();
        productService.editProduct(merchantId, product);
        return Result.ok(null, "商品编辑成功");
    }

    /**
     * 商家上下架商品
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：PUT /api/merchant/product/status?productId=1001&status=0
     * 请求头：Authorization: Bearer {token}
     *
     * @param productId 商品ID
     * @param status    目标状态：0=下架 1=上架
     */
    @Operation(summary = "商家上下架商品", description = "校验商品归属，设置上架/下架状态")
    @PutMapping("/merchant/product/status")
    public Result<Void> changeProductStatus(
            @RequestParam Long productId,
            @RequestParam Integer status) {
        long merchantId = getCurrentMerchantId();
        productService.changeProductStatus(merchantId, productId, status);
        return Result.ok(null, status == 1 ? "上架成功" : "下架成功");
    }

    /**
     * 商家删除商品（逻辑删除）
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：DELETE /api/merchant/product/delete/1001
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "商家删除商品", description = "逻辑删除，校验商品归属")
    @DeleteMapping("/merchant/product/delete/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        long merchantId = getCurrentMerchantId();
        productService.deleteProduct(merchantId, id);
        return Result.ok(null, "商品删除成功");
    }

    /**
     * 商家分页查询自己名下所有商品
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：GET /api/merchant/product/my?pageNum=1&pageSize=10&status=1
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "商家查询我的商品", description = "分页查询当前商家名下所有商品")
    @GetMapping("/merchant/product/my")
    public Result<PageResult<ProductVO>> listMyProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        long merchantId = getCurrentMerchantId();
        PageResult<ProductVO> result = productService.listMyProducts(merchantId, pageNum, pageSize, status);
        return Result.ok(result);
    }

    /**
     * 商家查看自己商品产生的订单列表
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：GET /api/merchant/product/orders?pageNum=1&pageSize=10&status=1
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "商家查看商品订单", description = "分页查询当前商家名下商品产生的订单")
    @GetMapping("/merchant/product/orders")
    public Result<PageResult<OrderVO>> listMyProductOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        long merchantId = getCurrentMerchantId();
        PageResult<OrderVO> result = productService.listMyProductOrders(merchantId, pageNum, pageSize, status);
        return Result.ok(result);
    }
}
