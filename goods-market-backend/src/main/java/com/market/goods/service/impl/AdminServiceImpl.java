package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.dto.AuditMerchantDTO;
import com.market.goods.dto.UserEditRoleDTO;
import com.market.goods.entity.*;
import com.market.goods.enums.MerchantAuditEnum;
import com.market.goods.enums.OrderStatusEnum;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.*;
import com.market.goods.service.AdminService;
import com.market.goods.util.OrderItemFiller;
import com.market.goods.util.PageUtil;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.MerchantVO;
import com.market.goods.vo.OrderStatisticsVO;
import com.market.goods.vo.OrderVO;
import com.market.goods.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员后台 Service 实现类
 *
 * 权限前提：所有方法由 AdminController 调用，Controller 层已通过 Sa-Token 校验 role=3
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemFiller orderItemFiller;

    // ==================== 用户管理 ====================

    /**
     * 分页查询全部用户
     *
     * 使用 UserMapper 自定义 XML SQL，支持 phone/role/status 多条件筛选
     * 返回的 User 对象由 MyBatis-Plus 自动映射，password 字段虽查出但前端不应展示
     */
    @Override
    public PageResult<User> listUsers(int pageNum, int pageSize, String phone, Integer role, Integer status) {
        Page<User> page = PageUtil.buildPage(pageNum, pageSize);

        Map<String, Object> params = new HashMap<>();
        if (phone != null && !phone.isBlank()) {
            params.put("phone", phone.trim());
        }
        if (role != null) {
            params.put("role", role);
        }
        if (status != null) {
            params.put("status", status);
        }

        Page<User> result = (Page<User>) userMapper.selectUserPageByAdmin(page, params);

        // 脱敏处理：清除密码字段，防止泄露
        result.getRecords().forEach(user -> user.setPassword(null));

        return PageUtil.toPageResult(result);
    }

    /**
     * 启用/禁用用户
     *
     * 禁用后该用户无法登录（UserServiceImpl.login 中校验 status=0 抛异常）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 防止管理员禁用自己的账号（比较当前登录管理员ID与目标用户ID）
        long currentAdminId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        if (user.getId().equals(currentAdminId) && status == 0) {
            throw new BusinessException("不能禁用自己的账号");
        }

        user.setStatus(status);
        userMapper.updateById(user);
        log.info("管理员修改用户状态：userId={}, status={}", userId, status);
    }

    /**
     * 修改用户角色
     *
     * 典型场景：
     *   - 普通用户(1) → 商家(2)：商家审核通过后由管理员升级
     *   - 商家(2) → 普通用户(1)：商家违规降级
     *   - 普通用户(1) → 管理员(3)：超级管理员授权（谨慎操作）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(UserEditRoleDTO dto) {
        // 防止管理员修改自己的角色（误操作降级会导致失去后台权限）
        long currentAdminId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        if (dto.getUserId().equals(currentAdminId)) {
            throw new BusinessException("不能修改自己的角色");
        }

        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Integer oldRole = user.getRole();
        user.setRole(dto.getRole());
        userMapper.updateById(user);
        log.info("管理员修改用户角色：userId={}, {} → {}", dto.getUserId(), oldRole, dto.getRole());
    }

    // ==================== 商家审核管理 ====================

    /**
     * 查询待审核商家列表
     *
     * 只查询 audit_status=0（待审核）的商家记录
     */
    @Override
    public PageResult<MerchantVO> listPendingMerchants(int pageNum, int pageSize) {
        Page<Merchant> page = PageUtil.buildPage(pageNum, pageSize);

        Map<String, Object> params = new HashMap<>();
        params.put("auditStatus", MerchantAuditEnum.PENDING.getCode());

        Page<Merchant> result = (Page<Merchant>) merchantMapper.selectMerchantPageByAdmin(page, params);

        return PageUtil.toPageResult(result, this::convertToMerchantVO);
    }

    /**
     * 审核商家入驻申请
     *
     * 流程：
     *   1. 审核通过(auditStatus=1)：
     *      - 更新商家审核状态
     *      - 将关联用户的角色从 普通用户(1) 升级为 商家(2)
     *   2. 审核驳回(auditStatus=2)：
     *      - 更新商家审核状态 + 驳回原因
     *      - 不修改用户角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditMerchant(AuditMerchantDTO dto) {
        Merchant merchant = merchantMapper.selectById(dto.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商家记录不存在");
        }
        if (!MerchantAuditEnum.isPending(merchant.getAuditStatus())) {
            throw new BusinessException("该申请已处理，当前状态："
                    + MerchantAuditEnum.ofCode(merchant.getAuditStatus()).getDesc());
        }

        // 驳回时先校验必须填写备注（校验通过后再执行更新）
        if (dto.getAuditStatus() == MerchantAuditEnum.REJECTED.getCode()) {
            if (dto.getAuditRemark() == null || dto.getAuditRemark().isBlank()) {
                throw new BusinessException("驳回时必须填写审核备注");
            }
        }

        // 更新商家审核状态
        merchant.setAuditStatus(dto.getAuditStatus());
        merchant.setAuditRemark(dto.getAuditRemark());
        merchantMapper.updateById(merchant);

        // 审核通过 → 升级用户角色为商家(2)
        if (MerchantAuditEnum.isApproved(dto.getAuditStatus())) {
            User user = userMapper.selectById(merchant.getUserId());
            if (user != null) {
                user.setRole(2);    // 商家
                userMapper.updateById(user);
                log.info("商家审核通过，用户角色升级：userId={}, role=1→2", user.getId());
            }
        }

        log.info("商家审核完成：merchantId={}, result={}", dto.getMerchantId(),
                MerchantAuditEnum.ofCode(dto.getAuditStatus()).getDesc());
    }

    /**
     * 全平台商家管理列表（含所有审核状态）
     */
    @Override
    public PageResult<MerchantVO> listAllMerchants(int pageNum, int pageSize, String shopName, Integer auditStatus) {
        Page<Merchant> page = PageUtil.buildPage(pageNum, pageSize);

        Map<String, Object> params = new HashMap<>();
        if (shopName != null && !shopName.isBlank()) {
            params.put("shopName", shopName.trim());
        }
        if (auditStatus != null) {
            params.put("auditStatus", auditStatus);
        }

        Page<Merchant> result = (Page<Merchant>) merchantMapper.selectMerchantPageByAdmin(page, params);

        return PageUtil.toPageResult(result, this::convertToMerchantVO);
    }

    // ==================== 平台商品管控 ====================

    /**
     * 查看所有商品（管理员视角，含下架商品）
     *
     * 使用 ProductMapper 自定义 XML SQL，关联商家店铺名
     */
    @Override
    public PageResult<ProductVO> listAllProducts(int pageNum, int pageSize, String name, Long merchantId, Integer status) {
        Page<ProductVO> page = PageUtil.buildPage(pageNum, pageSize);

        Map<String, Object> params = new HashMap<>();
        if (name != null && !name.isBlank()) {
            params.put("name", name.trim());
        }
        if (merchantId != null) {
            params.put("merchantId", merchantId);
        }
        if (status != null) {
            params.put("status", status);
        }

        IPage<ProductVO> result = productMapper.selectProductPageByAdmin(page, params);
        return PageUtil.toPageResult(result);
    }

    /**
     * 管理员强制下架违规商品
     *
     * 不校验商品归属，管理员拥有全局操作权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceOffShelfProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(0);   // 下架
        productMapper.updateById(product);
        log.info("管理员强制下架商品：productId={}, name={}", productId, product.getName());
    }

    /**
     * 管理员重新上架商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceOnShelfProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(1);   // 上架
        productMapper.updateById(product);
        log.info("管理员重新上架商品：productId={}, name={}", productId, product.getName());
    }

    /**
     * 管理员强制删除违规商品（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceDeleteProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        productMapper.deleteById(productId);
        log.info("管理员强制删除商品：productId={}, name={}", productId, product.getName());
    }

    // ==================== 全平台订单管理 ====================

    /**
     * 查询平台全部订单
     *
     * 使用 OrderMapper 自定义 XML SQL，支持 orderNo/status/merchantId 多条件筛选
     */
    @Override
    public PageResult<OrderVO> listAllOrders(int pageNum, int pageSize, String orderNo, Integer status, Long merchantId) {
        Page<OrderVO> page = PageUtil.buildPage(pageNum, pageSize);

        Map<String, Object> params = new HashMap<>();
        if (orderNo != null && !orderNo.isBlank()) {
            params.put("orderNo", orderNo.trim());
        }
        if (status != null) {
            params.put("status", status);
        }
        if (merchantId != null) {
            params.put("merchantId", merchantId);
        }

        IPage<OrderVO> result = orderMapper.selectOrderPageByAdmin(page, params);

        // 填充状态描述 + 商品明细（列表页展示用）
        result.getRecords().forEach(this::fillOrderStatusDesc);
        orderItemFiller.fill(result.getRecords());
        return PageUtil.toPageResult(result);
    }

    /**
     * 处理异常订单（管理员手动完成/取消）
     *
     * 场景：
     *   - 已支付(1) → 已完成(3)：用户收货确认、系统纠纷处理
     *   - 待支付(0) → 已取消(2)：手动关闭僵尸订单
     *
     * 取消时自动回滚商品库存（乐观锁）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAbnormalOrder(String orderNo, Integer targetStatus) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验状态流转合法性
        if (targetStatus == OrderStatusEnum.CANCELLED.getCode()) {
            // 取消订单：只有待支付和已支付可取消
            if (!OrderStatusEnum.canCancel(order.getStatus())) {
                throw new BusinessException("当前状态不允许取消："
                        + OrderStatusEnum.ofCode(order.getStatus()).getDesc());
            }
            // 回滚库存
            rollbackOrderStock(orderNo);
        } else if (targetStatus == OrderStatusEnum.COMPLETED.getCode()) {
            // 完成订单：只有已支付可完成
            if (order.getStatus() != OrderStatusEnum.PAID.getCode()) {
                throw new BusinessException("只有已支付订单才能标记为已完成");
            }
        } else {
            throw new BusinessException("不支持的目标状态：" + targetStatus);
        }

        order.setStatus(targetStatus);
        orderMapper.updateById(order);
        log.info("管理员处理异常订单：orderNo={}, targetStatus={}", orderNo, targetStatus);
    }

    // ==================== 首页运营统计 ====================

    /**
     * 首页运营统计数据
     *
     * 汇总查询：用户数、商家数、商品数、订单数、金额等12项指标
     * 使用 MyBatis-Plus LambdaQueryWrapper 条件查询 + count/selectList 聚合
     */
    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO vo = new OrderStatisticsVO();

        // 今日起始时间（00:00:00）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // ========== 用户统计 ==========
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTodayUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)
        ));

        // ========== 商家统计 ==========
        vo.setTotalMerchants(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getAuditStatus, MerchantAuditEnum.APPROVED.getCode())
        ));
        vo.setPendingAuditMerchants(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getAuditStatus, MerchantAuditEnum.PENDING.getCode())
        ));

        // ========== 商品统计 ==========
        vo.setTotalProducts(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)
        ));
        vo.setTodayProducts(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().ge(Product::getCreateTime, todayStart)
        ));

        // ========== 订单统计 ==========
        vo.setTotalOrders(orderMapper.selectCount(null));
        vo.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart)
        ));
        vo.setPendingPaymentOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode())
        ));
        vo.setPendingDeliveryOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatusEnum.PAID.getCode())
        ));

        // ========== 金额统计 ==========
        // 已完成订单的实付金额总和
        BigDecimal totalAmount = calculateTotalAmount(null);
        vo.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

        // 今日已完成订单的实付金额总和
        BigDecimal todayAmount = calculateTotalAmount(todayStart);
        vo.setTodayAmount(todayAmount != null ? todayAmount : BigDecimal.ZERO);

        return vo;
    }

    // ==================== 私有方法 ====================

    /**
     * 计算已完成订单的实付金额总和
     *
     * @param startTime 起始时间（null=全部时间，非null=从该时间起）
     * @return 金额总和
     */
    private BigDecimal calculateTotalAmount(LocalDateTime startTime) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode());
        if (startTime != null) {
            wrapper.ge(Order::getCreateTime, startTime);
        }

        // 使用 SQL 聚合查询求和，避免将所有订单加载到内存
        return orderMapper.selectSumPayAmount(wrapper);
    }

    /**
     * 回滚订单中所有商品的库存（乐观锁）
     */
    private void rollbackOrderStock(String orderNo) {
        var items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo)
        );
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                int affected = productMapper.restoreStock(
                        product.getId(), item.getQuantity(), product.getVersion()
                );
                if (affected == 0) {
                    log.warn("管理员处理订单回滚库存失败（并发冲突）：productId={}, quantity={}",
                            item.getProductId(), item.getQuantity());
                }
            }
        }
    }

    /**
     * Merchant → MerchantVO 转换
     */
    private MerchantVO convertToMerchantVO(Merchant merchant) {
        MerchantVO vo = new MerchantVO();
        BeanUtils.copyProperties(merchant, vo);

        // 填充审核状态中文描述
        MerchantAuditEnum auditEnum = MerchantAuditEnum.ofCode(merchant.getAuditStatus());
        vo.setAuditStatusDesc(auditEnum != null ? auditEnum.getDesc() : "未知");

        return vo;
    }

    /**
     * 填充订单状态描述
     */
    private void fillOrderStatusDesc(OrderVO vo) {
        OrderStatusEnum statusEnum = OrderStatusEnum.ofCode(vo.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");
        if (vo.getPayMethod() != null) {
            vo.setPayMethodDesc(vo.getPayMethod() == 1 ? "支付宝" : "微信");
        }
    }
}
