package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.market.goods.dto.MerchantApplyDTO;
import com.market.goods.service.MerchantService;
import com.market.goods.util.Result;
import com.market.goods.vo.MerchantStatsVO;
import com.market.goods.vo.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商家模块 Controller
 *
 * 权限说明（配合 SaTokenConfig）：
 *   POST /merchant/apply     — 需要登录（角色 user/merchant/admin 均可提交申请）
 *   GET  /merchant/my/status — 需要登录（商家或管理员可查看）
 *
 * Sa-Token 拦截器规则：
 *   /merchant/** 匹配路径中排除了 /merchant/login 和 /merchant/register
 *   其余接口均需登录 + 角色为 merchant 或 admin
 *
 * @author goods-market
 */
@Tag(name = "商家模块", description = "商家入驻申请、状态查询")
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 提交商家入驻申请
     *
     * 权限：需要登录（普通用户 role=1 可提交，提交后等待管理员审核）
     * 请求：POST /api/merchant/apply
     * 请求头：Authorization: Bearer {token}
     * 参数：{"shopName": "xxx", "contactName": "xxx", "contactPhone": "138xxx", "address": "xxx", ...}
     *
     * 说明：
     *   - 一个用户只能申请一次，被驳回后可重新提交
     *   - 提交后不改变用户角色，审核通过后管理员升级 role=2（商家）
     */
    @Operation(summary = "提交商家入驻申请", description = "普通用户提交入驻申请，等待管理员审核")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody MerchantApplyDTO dto) {
        // 从 Sa-Token 会话中获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        merchantService.apply(userId, dto);
        return Result.ok(null, "入驻申请提交成功，请等待审核");
    }

    /**
     * 查看自己的入驻申请状态
     *
     * 权限：需要登录（商家或管理员）
     * 请求：GET /api/merchant/my/status
     * 请求头：Authorization: Bearer {token}
     *
     * 返回示例：
     * {
     *   "code": 200,
     *   "data": {
     *     "id": 1,
     *     "shopName": "好物小店",
     *     "auditStatus": 0,
     *     "auditStatusDesc": "待审核",
     *     "auditRemark": null,
     *     ...
     *   }
     * }
     */
    @Operation(summary = "查看入驻申请状态", description = "商家查看自己的入驻审核状态")
    @GetMapping("/my/status")
    public Result<MerchantVO> getMyStatus() {
        long userId = StpUtil.getLoginIdAsLong();
        MerchantVO vo = merchantService.getMyStatus(userId);
        return Result.ok(vo);
    }

    /**
     * 商家经营统计
     *
     * 权限：需要登录 + 商家/管理员角色
     * 请求：GET /api/merchant/stats
     *
     * 返回商家中心看板数据：商品数、在售数、各状态订单数、累计/今日销售额
     */
    @Operation(summary = "商家经营统计", description = "商家中心数据看板：商品/订单/销售额统计")
    @GetMapping("/stats")
    public Result<MerchantStatsVO> getMyStats() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(merchantService.getMyStats(userId));
    }
}
