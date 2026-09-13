package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.dto.CartOperateDTO;
import com.market.goods.entity.Cart;
import com.market.goods.entity.Product;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.CartMapper;
import com.market.goods.mapper.ProductMapper;
import com.market.goods.service.CartService;
import com.market.goods.util.PageUtil;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.CartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 购物车模块 Service 实现类
 *
 * 核心设计：
 *   - 新增商品时如果购物车中已存在该商品，则累加数量（不重复插入）
 *   - 修改数量时如果新数量 ≤ 0，则逻辑删除该条记录
 *   - 查询时关联 product 表获取最新商品信息（名称、价格、库存可能变化）
 *   - 小计金额由 SQL 层计算（price × quantity）
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    /**
     * 新增商品到购物车
     *
     * 流程：
     *   1. 校验商品是否存在且上架
     *   2. 校验库存是否充足
     *   3. 查询购物车是否已有该商品
     *   4. 已有 → 累加数量
     *   5. 没有 → 新增记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long userId, CartOperateDTO dto) {
        Long productId = dto.getProductId();
        Integer quantity = dto.getQuantity();

        // 1. 校验商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架，无法加入购物车");
        }

        // 2. 校验库存
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足，当前库存：" + product.getStock());
        }

        // 3. 查询购物车是否已有该商品（含已逻辑删除的记录，避免重复插入）
        Cart existing = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
                        .eq(Cart::getDeleted, 0)
        );

        if (existing != null) {
            // 4. 已有该商品 → 累加数量
            int newQuantity = existing.getQuantity() + quantity;
            // 校验累加后是否超出库存
            if (newQuantity > product.getStock()) {
                throw new BusinessException("超出库存限制，购物车中已有 "
                        + existing.getQuantity() + " 件，库存仅剩 " + product.getStock() + " 件");
            }
            existing.setQuantity(newQuantity);
            cartMapper.updateById(existing);
            log.info("购物车累加数量：userId={}, productId={}, newQuantity={}", userId, productId, newQuantity);
        } else {
            // 5. 检查是否存在已删除的购物车记录，若有则恢复
            // （必须用原生 SQL 恢复：updateById 会被 @TableLogic 附加 WHERE deleted=0，
            //   待恢复记录恰好 deleted=1，导致恢复静默失败）
            Cart deletedItem = cartMapper.selectDeletedItem(userId, productId);
            if (deletedItem != null) {
                cartMapper.restoreDeletedItem(deletedItem.getId(), quantity);
                log.info("恢复购物车商品：userId={}, productId={}, quantity={}", userId, productId, quantity);
            } else {
                // 6. 无记录 → 新增
                Cart cart = new Cart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setQuantity(quantity);
                cart.setChecked(1);     // 默认选中
                cartMapper.insert(cart);
                log.info("新增购物车商品：userId={}, productId={}, quantity={}", userId, productId, quantity);
            }
        }
    }

    /**
     * 修改购物车商品数量
     *
     * 流程：
     *   1. 校验购物车记录是否存在
     *   2. 新数量 ≤ 0 → 逻辑删除该条记录
     *   3. 新数量 > 0 → 校验库存后更新数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, CartOperateDTO dto) {
        Long productId = dto.getProductId();
        Integer quantity = dto.getQuantity();

        // 1. 查询购物车记录
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
        );
        if (cart == null) {
            throw new BusinessException("购物车中无该商品");
        }

        if (quantity <= 0) {
            // 2. 数量 ≤ 0 → 逻辑删除（@TableLogic 生效，实际执行 UPDATE SET deleted=1）
            cartMapper.deleteById(cart.getId());
            log.info("购物车删除商品（数量设为0）：userId={}, productId={}", userId, productId);
        } else {
            // 3. 校验库存
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            if (quantity > product.getStock()) {
                throw new BusinessException("超出库存限制，当前库存：" + product.getStock());
            }
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
            log.info("购物车修改数量：userId={}, productId={}, quantity={}", userId, productId, quantity);
        }
    }

    /**
     * 删除购物车单项
     *
     * 逻辑删除：UPDATE cart SET deleted=1 WHERE id=? AND user_id=?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long userId, Long productId) {
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
        );
        if (cart == null) {
            throw new BusinessException("购物车中无该商品");
        }

        cartMapper.deleteById(cart.getId());
        log.info("购物车删除商品：userId={}, productId={}", userId, productId);
    }

    /**
     * 查询个人购物车列表
     *
     * 使用 CartMapper 自定义 XML SQL，关联 product 表获取最新商品信息
     * 返回的 CartVO 包含：商品名称、图片、价格、库存、小计等
     */
    @Override
    public PageResult<CartVO> listMyCart(Long userId, int pageNum, int pageSize) {
        Page<CartVO> page = PageUtil.buildPage(pageNum, pageSize);
        IPage<CartVO> result = cartMapper.selectCartPageWithProduct(page, userId);
        return PageUtil.toPageResult(result);
    }
}
