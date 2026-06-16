package com.market.goods.service;

import com.market.goods.dto.AuditMerchantDTO;
import com.market.goods.dto.UserEditRoleDTO;
import com.market.goods.entity.User;
import com.market.goods.entity.Merchant;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.MerchantVO;
import com.market.goods.vo.OrderStatisticsVO;
import com.market.goods.vo.OrderVO;
import com.market.goods.vo.ProductVO;

/**
 * 管理员后台 Service 接口
 *
 * 所有方法仅 role=3 管理员可访问，由 SaTokenConfig + Controller 双重校验
 *
 * @author goods-market
 */
public interface AdminService {

    // ==================== 用户管理 ====================

    /**
     * 分页查询全部用户
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param phone    手机号模糊搜索（可选）
     * @param role     角色筛选（可选）
     * @param status   状态筛选（可选）
     * @return 用户分页列表（脱敏：不返回密码字段）
     */
    PageResult<User> listUsers(int pageNum, int pageSize, String phone, Integer role, Integer status);

    /**
     * 启用/禁用用户
     *
     * @param userId 目标用户ID
     * @param status 目标状态：0=禁用 1=启用
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 修改用户角色
     *
     * @param dto 包含 userId 和目标 role
     */
    void updateUserRole(UserEditRoleDTO dto);

    // ==================== 商家审核管理 ====================

    /**
     * 查询商家入驻申请列表（待审核）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 待审核商家列表
     */
    PageResult<MerchantVO> listPendingMerchants(int pageNum, int pageSize);

    /**
     * 审核商家入驻申请（通过/驳回）
     *
     * @param dto 审核参数（商家ID、审核结果、备注）
     */
    void auditMerchant(AuditMerchantDTO dto);

    /**
     * 全平台商家管理列表（含所有审核状态）
     *
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param shopName   店铺名模糊搜索（可选）
     * @param auditStatus 审核状态筛选（可选）
     * @return 商家分页列表
     */
    PageResult<MerchantVO> listAllMerchants(int pageNum, int pageSize, String shopName, Integer auditStatus);

    // ==================== 平台商品管控 ====================

    /**
     * 查看所有商品（管理员视角）
     *
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param name       商品名模糊搜索（可选）
     * @param merchantId 商家ID筛选（可选）
     * @param status     上下架状态筛选（可选）
     * @return 商品分页列表（含店铺名）
     */
    PageResult<ProductVO> listAllProducts(int pageNum, int pageSize, String name, Long merchantId, Integer status);

    /**
     * 下架违规商品
     *
     * @param productId 商品ID
     */
    void forceOffShelfProduct(Long productId);

    /**
     * 重新上架商品
     *
     * @param productId 商品ID
     */
    void forceOnShelfProduct(Long productId);

    /**
     * 删除违规商品（逻辑删除）
     *
     * @param productId 商品ID
     */
    void forceDeleteProduct(Long productId);

    // ==================== 全平台订单管理 ====================

    /**
     * 查询平台全部订单
     *
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param orderNo    订单号模糊搜索（可选）
     * @param status     订单状态筛选（可选）
     * @param merchantId 商家ID筛选（可选）
     * @return 订单分页列表
     */
    PageResult<OrderVO> listAllOrders(int pageNum, int pageSize, String orderNo, Integer status, Long merchantId);

    /**
     * 处理异常订单（管理员手动完成/取消）
     *
     * @param orderNo    订单编号
     * @param targetStatus 目标状态：2=已取消 3=已完成
     */
    void handleAbnormalOrder(String orderNo, Integer targetStatus);

    // ==================== 首页运营统计 ====================

    /**
     * 首页运营统计数据
     *
     * @return 包含用户/商家/商品/订单/金额等12项统计数据
     */
    OrderStatisticsVO getStatistics();
}
