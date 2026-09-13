package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.goods.dto.MerchantApplyDTO;
import com.market.goods.entity.Merchant;
import com.market.goods.entity.Order;
import com.market.goods.entity.Product;
import com.market.goods.entity.User;
import com.market.goods.enums.MerchantAuditEnum;
import com.market.goods.enums.OrderStatusEnum;
import com.market.goods.enums.UserRoleEnum;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.MerchantMapper;
import com.market.goods.mapper.OrderMapper;
import com.market.goods.mapper.ProductMapper;
import com.market.goods.mapper.UserMapper;
import com.market.goods.service.MerchantService;
import com.market.goods.vo.MerchantStatsVO;
import com.market.goods.vo.MerchantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 商家模块 Service 实现类
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    /**
     * 普通用户提交商家入驻申请
     *
     * 流程：
     *   1. 校验用户是否已有商家记录（防重复申请）
     *   2. 创建商家记录，审核状态=待审核(0)
     *   3. 注意：不修改用户角色，等管理员审核通过后才升级为商家(2)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyDTO dto) {
        // 1. 校验是否已存在商家记录（一个用户只能申请一次）
        Merchant existing = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, userId)
        );
        if (existing != null) {
            // 已有记录：判断状态给出不同提示
            if (MerchantAuditEnum.isPending(existing.getAuditStatus())) {
                throw new BusinessException("您已提交入驻申请，请等待审核");
            }
            if (MerchantAuditEnum.isApproved(existing.getAuditStatus())) {
                throw new BusinessException("您已是入驻商家，无需重复申请");
            }
            // 被驳回的情况，允许重新提交（更新原记录）
            existing.setShopName(dto.getShopName());
            existing.setShopLogo(dto.getShopLogo());
            existing.setDescription(dto.getDescription());
            existing.setLicenseNo(dto.getLicenseNo());
            existing.setLicenseImg(dto.getLicenseImg());
            existing.setContactName(dto.getContactName());
            existing.setContactPhone(dto.getContactPhone());
            existing.setAddress(dto.getAddress());
            existing.setAuditStatus(MerchantAuditEnum.PENDING.getCode());  // 重置为待审核
            existing.setAuditRemark(null);                                  // 清空驳回备注
            merchantMapper.updateById(existing);
            log.info("商家重新提交入驻申请：userId={}, merchantId={}", userId, existing.getId());
            return;
        }

        // 2. 创建新的商家入驻记录
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(dto, merchant);
        merchant.setUserId(userId);
        merchant.setAuditStatus(MerchantAuditEnum.PENDING.getCode());  // 待审核

        merchantMapper.insert(merchant);
        log.info("商家入驻申请提交成功：userId={}, merchantId={}, shopName={}", userId, merchant.getId(), dto.getShopName());
    }

    /**
     * 商家查看自己的入驻申请状态
     */
    @Override
    public MerchantVO getMyStatus(Long userId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, userId)
        );
        if (merchant == null) {
            throw new BusinessException("您尚未提交入驻申请");
        }

        // 转换为 VO
        MerchantVO vo = new MerchantVO();
        BeanUtils.copyProperties(merchant, vo);

        // 填充审核状态中文描述
        MerchantAuditEnum auditEnum = MerchantAuditEnum.ofCode(merchant.getAuditStatus());
        vo.setAuditStatusDesc(auditEnum != null ? auditEnum.getDesc() : "未知");

        return vo;
    }

    /**
     * 商家经营统计（商家中心看板）
     */
    @Override
    public MerchantStatsVO getMyStats(Long userId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null) {
            throw new BusinessException("商家信息不存在，请先完成入驻申请");
        }

        Long merchantId = merchant.getId();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        MerchantStatsVO vo = new MerchantStatsVO();

        // ========== 商品统计 ==========
        vo.setTotalProducts(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId)));
        vo.setOnSaleProducts(productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getStatus, 1)));

        // ========== 订单统计 ==========
        vo.setTotalOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getMerchantId, merchantId)));
        vo.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .ge(Order::getCreateTime, todayStart)));
        vo.setPendingShipOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, OrderStatusEnum.PAID.getCode())));
        vo.setShippedOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, OrderStatusEnum.SHIPPED.getCode())));
        vo.setCompletedOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())));

        // ========== 金额统计（已支付/已发货/已完成订单的实付金额之和） ==========
        List<Integer> salesStatuses = Arrays.asList(
                OrderStatusEnum.PAID.getCode(),
                OrderStatusEnum.SHIPPED.getCode(),
                OrderStatusEnum.COMPLETED.getCode());
        BigDecimal totalSales = orderMapper.selectSumPayAmount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .in(Order::getStatus, salesStatuses));
        vo.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);

        BigDecimal todaySales = orderMapper.selectSumPayAmount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .in(Order::getStatus, salesStatuses)
                        .ge(Order::getCreateTime, todayStart));
        vo.setTodaySales(todaySales != null ? todaySales : BigDecimal.ZERO);

        return vo;
    }
}
