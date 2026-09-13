package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Product;
import com.market.goods.entity.ProductCollect;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.CollectMapper;
import com.market.goods.mapper.ProductMapper;
import com.market.goods.service.CollectService;
import com.market.goods.util.PageUtil;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.CollectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品收藏模块 Service 实现类
 *
 * 核心设计：
 *   - product_collect 表通过逻辑删除实现"收藏/取消收藏"
 *   - 收藏时 insert 新记录（deleted=0）
 *   - 取消收藏时逻辑删除（UPDATE SET deleted=1）
 *   - 重复收藏时：检查是否存在 deleted=1 的记录，若有则恢复（UPDATE SET deleted=0）
 *   - 联合唯一索引 (user_id, product_id) 防止并发重复插入
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CollectMapper collectMapper;
    private final ProductMapper productMapper;

    /**
     * 收藏商品
     *
     * 流程：
     *   1. 校验商品是否存在
     *   2. 查询是否已有收藏记录（包含已取消收藏的 deleted=1 记录）
     *   3. 已取消的记录 → 恢复收藏（UPDATE SET deleted=0）
     *   4. 无记录 → 新增收藏（INSERT）
     *   5. 已收藏 → 提示"已收藏"
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCollect(Long userId, Long productId) {
        // 1. 校验商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 先查未删除的记录，判断是否已收藏
        ProductCollect existing = collectMapper.selectOne(
                new LambdaQueryWrapper<ProductCollect>()
                        .eq(ProductCollect::getUserId, userId)
                        .eq(ProductCollect::getProductId, productId)
                        .eq(ProductCollect::getDeleted, 0)
        );

        if (existing != null) {
            // 已收藏，不可重复收藏
            throw new BusinessException("已收藏该商品");
        }

        // 3. 查询是否存在已取消收藏的记录（deleted=1），使用自定义SQL忽略逻辑删除过滤
        ProductCollect deletedRecord = collectMapper.selectDeletedRecord(userId, productId);

        if (deletedRecord != null) {
            // 之前取消过收藏，恢复收藏（必须用原生 SQL：updateById 会被 @TableLogic
            // 附加 WHERE deleted=0，导致恢复静默失败）
            collectMapper.restoreDeletedRecord(deletedRecord.getId());
            log.info("恢复收藏商品：userId={}, productId={}", userId, productId);
        } else {
            // 无记录，新增收藏
            ProductCollect collect = new ProductCollect();
            collect.setUserId(userId);
            collect.setProductId(productId);
            collectMapper.insert(collect);
            log.info("收藏商品：userId={}, productId={}", userId, productId);
        }
    }

    /**
     * 取消收藏
     *
     * 逻辑删除：UPDATE product_collect SET deleted=1 WHERE user_id=? AND product_id=? AND deleted=0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCollect(Long userId, Long productId) {
        ProductCollect existing = collectMapper.selectOne(
                new LambdaQueryWrapper<ProductCollect>()
                        .eq(ProductCollect::getUserId, userId)
                        .eq(ProductCollect::getProductId, productId)
                        .eq(ProductCollect::getDeleted, 0)
        );
        if (existing == null) {
            throw new BusinessException("未收藏该商品，无法取消");
        }

        // 逻辑删除（@TableLogic 生效，实际执行 UPDATE SET deleted=1）
        collectMapper.deleteById(existing.getId());
        log.info("取消收藏商品：userId={}, productId={}", userId, productId);
    }

    /**
     * 分页查询个人收藏列表
     *
     * 使用 CollectMapper 自定义 XML SQL，关联 product 表获取最新商品信息
     */
    @Override
    public PageResult<CollectVO> listMyCollects(Long userId, int pageNum, int pageSize) {
        Page<CollectVO> page = PageUtil.buildPage(pageNum, pageSize);
        IPage<CollectVO> result = collectMapper.selectCollectPageWithProduct(page, userId);
        return PageUtil.toPageResult(result);
    }

    /**
     * 查询当前商品是否被本人收藏
     */
    @Override
    public boolean isCollected(Long userId, Long productId) {
        Long count = collectMapper.selectCount(
                new LambdaQueryWrapper<ProductCollect>()
                        .eq(ProductCollect::getUserId, userId)
                        .eq(ProductCollect::getProductId, productId)
                        .eq(ProductCollect::getDeleted, 0)
        );
        return count > 0;
    }
}
