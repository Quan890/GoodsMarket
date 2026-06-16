package com.market.goods.controller;

import com.market.goods.dto.AuditMerchantDTO;
import com.market.goods.dto.UserEditRoleDTO;
import com.market.goods.entity.User;
import com.market.goods.service.AdminService;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.util.Result;
import com.market.goods.vo.MerchantVO;
import com.market.goods.vo.OrderStatisticsVO;
import com.market.goods.vo.OrderVO;
import com.market.goods.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员后台 Controller
 *
 * 权限说明（配合 SaTokenConfig）：
 *   /admin/** 所有路径必须登录且角色为 管理员(3)
 *   SaTokenConfig 中已配置：
 *     SaRouter.match("/admin/**").notMatch("/admin/login")
 *       .check(r -> { StpUtil.checkLogin(); StpUtil.checkRole("admin"); });
 *
 * 越权访问：非管理员角色访问任何 /admin/** 接口，Sa-Token 拦截器自动抛出
 *          NotRoleException → GlobalExceptionHandler 返回 403 权限不足
 *
 * @author goods-market
 */
@Tag(name = "管理员后台", description = "用户管理、商家审核、商品管控、订单管理、运营统计")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== 用户管理 ====================

    /**
     * 分页查询全部用户
     *
     * 权限：仅管理员(role=3)
     * 请求：GET /api/admin/user/list?pageNum=1&pageSize=10&phone=138&role=1&status=1
     * 请求头：Authorization: Bearer {token}
     *
     * 返回的用户数据已脱敏（password=null）
     */
    @Operation(summary = "用户列表", description = "分页查询全部用户，支持手机号/角色/状态筛选")
    @GetMapping("/user/list")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        PageResult<User> result = adminService.listUsers(pageNum, pageSize, phone, role, status);
        return Result.ok(result);
    }

    /**
     * 启用/禁用用户
     *
     * 权限：仅管理员(role=3)
     * 请求：PUT /api/admin/user/status?userId=1001&status=0
     * 请求头：Authorization: Bearer {token}
     *
     * 禁用后该用户无法登录，不能禁用自己的账号
     */
    @Operation(summary = "启用/禁用用户", description = "修改用户账号状态")
    @PutMapping("/user/status")
    public Result<Void> updateUserStatus(
            @RequestParam Long userId,
            @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return Result.ok(null, status == 1 ? "用户已启用" : "用户已禁用");
    }

    /**
     * 修改用户角色
     *
     * 权限：仅管理员(role=3)
     * 请求：PUT /api/admin/user/role
     * 请求头：Authorization: Bearer {token}
     * 参数：{"userId": 1001, "role": 2}
     *
     * 角色值：0=游客 1=普通用户 2=商家 3=管理员
     * 典型场景：商家审核通过后将用户从 role=1 升级为 role=2
     */
    @Operation(summary = "修改用户角色", description = "管理员修改指定用户的角色")
    @PutMapping("/user/role")
    public Result<Void> updateUserRole(@Valid @RequestBody UserEditRoleDTO dto) {
        adminService.updateUserRole(dto);
        return Result.ok(null, "角色修改成功");
    }

    // ==================== 商家审核管理 ====================

    /**
     * 查询待审核商家列表
     *
     * 权限：仅管理员(role=3)
     * 请求：GET /api/admin/merchant/pending?pageNum=1&pageSize=10
     * 请求头：Authorization: Bearer {token}
     *
     * 只返回 audit_status=0（待审核）的商家记录
     */
    @Operation(summary = "待审核商家列表", description = "查询所有待审核的商家入驻申请")
    @GetMapping("/merchant/pending")
    public Result<PageResult<MerchantVO>> listPendingMerchants(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<MerchantVO> result = adminService.listPendingMerchants(pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 审核商家入驻申请（通过/驳回）
     *
     * 权限：仅管理员(role=3)
     * 请求：PUT /api/admin/merchant/audit
     * 请求头：Authorization: Bearer {token}
     * 参数：{"merchantId": 1, "auditStatus": 1, "auditRemark": "审核通过"}
     *
     * 通过(auditStatus=1)：商家审核状态改为通过 + 用户角色升级为商家(2)
     * 驳回(auditStatus=2)：商家审核状态改为驳回 + 必须填写 auditRemark
     */
    @Operation(summary = "审核商家入驻", description = "通过/驳回商家入驻申请")
    @PutMapping("/merchant/audit")
    public Result<Void> auditMerchant(@Valid @RequestBody AuditMerchantDTO dto) {
        adminService.auditMerchant(dto);
        return Result.ok(null, "审核操作完成");
    }

    /**
     * 全平台商家管理列表
     *
     * 权限：仅管理员(role=3)
     * 请求：GET /api/admin/merchant/list?pageNum=1&pageSize=10&shopName=好物&auditStatus=1
     * 请求头：Authorization: Bearer {token}
     *
     * 返回所有审核状态的商家，支持店铺名和审核状态筛选
     */
    @Operation(summary = "商家管理列表", description = "查询全平台商家，支持店铺名和审核状态筛选")
    @GetMapping("/merchant/list")
    public Result<PageResult<MerchantVO>> listAllMerchants(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) Integer auditStatus) {
        PageResult<MerchantVO> result = adminService.listAllMerchants(pageNum, pageSize, shopName, auditStatus);
        return Result.ok(result);
    }

    // ==================== 平台商品管控 ====================

    /**
     * 查看所有商品（管理员视角，含下架商品）
     *
     * 权限：仅管理员(role=3)
     * 请求：GET /api/admin/product/list?pageNum=1&pageSize=10&name=手机&merchantId=1&status=0
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "平台商品列表", description = "管理员查看所有商品，支持多条件筛选")
    @GetMapping("/product/list")
    public Result<PageResult<ProductVO>> listAllProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status) {
        PageResult<ProductVO> result = adminService.listAllProducts(pageNum, pageSize, name, merchantId, status);
        return Result.ok(result);
    }

    /**
     * 强制下架违规商品
     *
     * 权限：仅管理员(role=3)
     * 请求：PUT /api/admin/product/off-shelf?productId=1001
     * 请求头：Authorization: Bearer {token}
     *
     * 不校验商品归属，管理员拥有全局操作权限
     */
    @Operation(summary = "强制下架商品", description = "管理员下架违规商品")
    @PutMapping("/product/off-shelf")
    public Result<Void> forceOffShelfProduct(@RequestParam Long productId) {
        adminService.forceOffShelfProduct(productId);
        return Result.ok(null, "商品已下架");
    }

    /**
     * 强制删除违规商品
     *
     * 权限：仅管理员(role=3)
     * 请求：DELETE /api/admin/product/delete?productId=1001
     * 请求头：Authorization: Bearer {token}
     *
     * 逻辑删除，不校验归属
     */
    @Operation(summary = "强制删除商品", description = "管理员删除违规商品（逻辑删除）")
    @DeleteMapping("/product/delete")
    public Result<Void> forceDeleteProduct(@RequestParam Long productId) {
        adminService.forceDeleteProduct(productId);
        return Result.ok(null, "商品已删除");
    }

    /**
     * 重新上架商品
     *
     * 权限：仅管理员(role=3)
     * 请求：PUT /api/admin/product/on-shelf?productId=1001
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "重新上架商品", description = "管理员将已下架商品重新上架")
    @PutMapping("/product/on-shelf")
    public Result<Void> forceOnShelfProduct(@RequestParam Long productId) {
        adminService.forceOnShelfProduct(productId);
        return Result.ok(null, "商品已上架");
    }

    // ==================== 全平台订单管理 ====================

    /**
     * 查询平台全部订单
     *
     * 权限：仅管理员(role=3)
     * 请求：GET /api/admin/order/list?pageNum=1&pageSize=10&orderNo=GM&status=0&merchantId=1
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "平台订单列表", description = "管理员查看所有订单，支持多条件筛选")
    @GetMapping("/order/list")
    public Result<PageResult<OrderVO>> listAllOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long merchantId) {
        PageResult<OrderVO> result = adminService.listAllOrders(pageNum, pageSize, orderNo, status, merchantId);
        return Result.ok(result);
    }

    /**
     * 处理异常订单
     *
     * 权限：仅管理员(role=3)
     * 请求：PUT /api/admin/order/handle?orderNo=GM20260614&targetStatus=3
     * 请求头：Authorization: Bearer {token}
     *
     * targetStatus：2=已取消（回滚库存） 3=已完成
     * 场景：用户投诉、纠纷处理、僵尸订单清理
     */
    @Operation(summary = "处理异常订单", description = "管理员手动完成或取消异常订单")
    @PutMapping("/order/handle")
    public Result<Void> handleAbnormalOrder(
            @RequestParam String orderNo,
            @RequestParam Integer targetStatus) {
        adminService.handleAbnormalOrder(orderNo, targetStatus);
        return Result.ok(null, "订单处理完成");
    }

    // ==================== 首页运营统计 ====================

    /**
     * 首页运营统计数据
     *
     * 权限：仅管理员(role=3)
     * 请求：GET /api/admin/statistics
     * 请求头：Authorization: Bearer {token}
     *
     * 返回12项核心指标：
     *   订单维度：totalOrders, todayOrders, pendingPaymentOrders, pendingDeliveryOrders
     *   金额维度：totalAmount, todayAmount
     *   用户维度：totalUsers, todayUsers
     *   商家维度：totalMerchants, pendingAuditMerchants
     *   商品维度：totalProducts, todayProducts
     */
    @Operation(summary = "运营统计数据", description = "管理员首页Dashboard，返回12项核心指标")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getStatistics() {
        OrderStatisticsVO vo = adminService.getStatistics();
        return Result.ok(vo);
    }
}
