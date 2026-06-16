package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.market.goods.service.CollectService;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.util.Result;
import com.market.goods.vo.CollectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品收藏模块 Controller
 *
 * 权限说明（配合 SaTokenConfig）：
 *   所有接口均在 /user/** 路径下，需要登录 + 角色 user/merchant/admin
 *   游客（role=0）无法访问收藏接口
 *
 * @author goods-market
 */
@Tag(name = "商品收藏", description = "收藏商品、取消收藏、收藏列表")
@RestController
@RequestMapping("/user/collect")
@RequiredArgsConstructor
public class CollectController {

    private final CollectService collectService;

    /**
     * 收藏商品
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：POST /api/user/collect/add?productId=1001
     * 请求头：Authorization: Bearer {token}
     *
     * 说明：重复收藏会提示"已收藏该商品"，之前取消过的收藏会自动恢复
     */
    @Operation(summary = "收藏商品", description = "收藏指定商品，重复收藏会提示已收藏")
    @PostMapping("/add")
    public Result<Void> addCollect(@RequestParam Long productId) {
        long userId = StpUtil.getLoginIdAsLong();
        collectService.addCollect(userId, productId);
        return Result.ok(null, "收藏成功");
    }

    /**
     * 取消收藏
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：DELETE /api/user/collect/cancel?productId=1001
     * 请求头：Authorization: Bearer {token}
     *
     * 说明：通过逻辑删除实现，不会真正删除记录，再次收藏时恢复
     */
    @Operation(summary = "取消收藏", description = "取消收藏指定商品")
    @DeleteMapping("/cancel")
    public Result<Void> cancelCollect(@RequestParam Long productId) {
        long userId = StpUtil.getLoginIdAsLong();
        collectService.cancelCollect(userId, productId);
        return Result.ok(null, "取消收藏成功");
    }

    /**
     * 分页查询个人收藏列表
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：GET /api/user/collect/list?pageNum=1&pageSize=10
     * 请求头：Authorization: Bearer {token}
     *
     * 返回 CollectVO：收藏记录ID、商品ID、商品名称、图片、价格、收藏时间等
     */
    @Operation(summary = "收藏列表", description = "分页查询个人收藏商品列表")
    @GetMapping("/list")
    public Result<PageResult<CollectVO>> listMyCollects(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        long userId = StpUtil.getLoginIdAsLong();
        PageResult<CollectVO> result = collectService.listMyCollects(userId, pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 查询当前商品是否被本人收藏
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：GET /api/user/collect/check?productId=1001
     * 请求头：Authorization: Bearer {token}
     *
     * 返回示例：{"code":200, "data":{"collected":true}}
     * 用途：商品详情页展示收藏状态（已收藏显示实心❤，未收藏显示空心♡）
     */
    @Operation(summary = "检查是否已收藏", description = "查询当前用户是否已收藏指定商品")
    @GetMapping("/check")
    public Result<Map<String, Boolean>> checkCollected(@RequestParam Long productId) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean collected = collectService.isCollected(userId, productId);
        Map<String, Boolean> data = new HashMap<>();
        data.put("collected", collected);
        return Result.ok(data);
    }
}
