package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Category;
import com.market.goods.entity.Merchant;
import com.market.goods.entity.Product;
import com.market.goods.enums.MerchantAuditEnum;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.CategoryMapper;
import com.market.goods.mapper.MerchantMapper;
import com.market.goods.mapper.OrderMapper;
import com.market.goods.mapper.ProductMapper;
import com.market.goods.service.ProductService;
import com.market.goods.util.PageUtil;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.OrderVO;
import com.market.goods.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品模块 Service 实现类
 *
 * 核心设计：
 *   - 所有商家操作方法的第一个参数为 merchantId（从 Sa-Token 会话中获取）
 *   - 每次操作前校验商品归属（merchantId 必须匹配），防止商家操作别人商品
 *   - 公共接口（listPublic / getDetail）无需登录，游客可访问
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final CategoryMapper categoryMapper;
    private final OrderMapper orderMapper;

    /**
     * 商家新增商品
     *
     * 业务备注：一个商家主营一类商品（如：服饰商家、数码商家等）
     * 图片上传：商品图片存放在项目 static/images/products/ 目录下，
     *          数据库中保存相对路径，如 "/images/products/xxx.jpg"
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(Long merchantId, Product product) {
        // 1. 校验商家是否存在且审核通过
        Merchant merchant = validateMerchant(merchantId);

        // 2. 绑定商家ID
        product.setMerchantId(merchantId);

        // 3. 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1);       // 默认上架
        }
        if (product.getSales() == null) {
            product.setSales(0);        // 初始销量=0
        }
        if (product.getSortOrder() == null) {
            product.setSortOrder(0);    // 默认排序权重
        }

        // 4. 插入数据库（version 由 MyBatis-Plus 自动初始化为 0）
        productMapper.insert(product);

        log.info("商家新增商品：merchantId={}, productId={}, name={}", merchantId, product.getId(), product.getName());
    }

    /**
     * 商家编辑商品（校验归属）
     *
     * 只能修改自己名下的商品，修改字段包括：名称、副标题、图片、价格、库存、分类等
     * 注意：不能修改 merchantId 和 version（version 由乐观锁自动管理）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editProduct(Long merchantId, Product product) {
        // 1. 校验商品归属
        Product existing = validateProductOwner(merchantId, product.getId());

        // 2. 更新允许修改的字段
        existing.setName(product.getName());
        existing.setSubtitle(product.getSubtitle());
        existing.setCategoryId(product.getCategoryId());
        existing.setMainImage(product.getMainImage());
        existing.setImages(product.getImages());
        existing.setDetail(product.getDetail());
        existing.setPrice(product.getPrice());
        existing.setOriginalPrice(product.getOriginalPrice());
        existing.setStock(product.getStock());
        existing.setSortOrder(product.getSortOrder());

        // 3. 使用 updateById 更新（自动携带 WHERE id=? AND version=? 乐观锁条件）
        productMapper.updateById(existing);

        log.info("商家编辑商品：merchantId={}, productId={}", merchantId, product.getId());
    }

    /**
     * 商家上下架商品（校验归属）
     *
     * @param status 0=下架 1=上架
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeProductStatus(Long merchantId, Long productId, Integer status) {
        // 1. 校验商品归属
        Product existing = validateProductOwner(merchantId, productId);

        // 2. 更新状态
        existing.setStatus(status);
        productMapper.updateById(existing);

        log.info("商家{}商品：merchantId={}, productId={}", status == 1 ? "上架" : "下架", merchantId, productId);
    }

    /**
     * 商家删除商品（逻辑删除，校验归属）
     *
     * 使用 MyBatis-Plus 逻辑删除：UPDATE product SET deleted=1 WHERE id=?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long merchantId, Long productId) {
        // 1. 校验商品归属
        validateProductOwner(merchantId, productId);

        // 2. 逻辑删除（@TableLogic 注解生效，实际执行 UPDATE SET deleted=1）
        productMapper.deleteById(productId);

        log.info("商家删除商品：merchantId={}, productId={}", merchantId, productId);
    }

    /**
     * 商家分页查询自己名下所有商品
     */
    @Override
    public PageResult<ProductVO> listMyProducts(Long merchantId, int pageNum, int pageSize, Integer status) {
        // 构建分页参数
        Page<Product> page = PageUtil.buildPage(pageNum, pageSize);

        // 构建查询条件：merchantId 精确匹配 + 可选 status 筛选
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getSortOrder)
                .orderByDesc(Product::getCreateTime);

        Page<Product> result = productMapper.selectPage(page, wrapper);

        // 转换为 ProductVO（实体 → VO，补充店铺名）
        return PageUtil.toPageResult(result, product -> {
            ProductVO vo = new ProductVO();
            vo.setId(product.getId());
            vo.setName(product.getName());
            vo.setSubtitle(product.getSubtitle());
            vo.setMainImage(product.getMainImage());
            vo.setImages(product.getImages());
            vo.setDetail(product.getDetail());
            vo.setPrice(product.getPrice());
            vo.setOriginalPrice(product.getOriginalPrice());
            vo.setStock(product.getStock());
            vo.setSales(product.getSales());
            vo.setStatus(product.getStatus());
            vo.setSortOrder(product.getSortOrder());
            vo.setMerchantId(product.getMerchantId());
            vo.setCreateTime(product.getCreateTime());
            return vo;
        });
    }

    /**
     * 商家查看自己商品产生的订单列表
     *
     * 使用 OrderMapper.selectOrderPageByAdmin 以 merchantId 为筛选条件
     */
    @Override
    public PageResult<OrderVO> listMyProductOrders(Long merchantId, int pageNum, int pageSize, Integer status) {
        Page<OrderVO> page = PageUtil.buildPage(pageNum, pageSize);

        // 组装查询参数：以 merchantId 为主条件
        Map<String, Object> params = new HashMap<>();
        params.put("merchantId", merchantId);
        if (status != null) {
            params.put("status", status);
        }

        IPage<OrderVO> result = orderMapper.selectOrderPageByAdmin(page, params);
        return PageUtil.toPageResult(result);
    }

    // ==================== 公共商品接口（游客可访问） ====================

    /**
     * 公共商品分页列表查询
     *
     * 只查询上架(status=1)且未删除的商品，关联商家店铺名
     */
    @Override
    public PageResult<ProductVO> listPublicProducts(int pageNum, int pageSize, String keyword, Long categoryId) {
        Page<ProductVO> page = PageUtil.buildPage(pageNum, pageSize);

        Map<String, Object> params = new HashMap<>();
        params.put("status", 1);    // 只查上架商品
        if (keyword != null && !keyword.isBlank()) {
            params.put("name", keyword.trim());
        }

        // 分类筛选：categoryId 可能是一级或二级分类
        if (categoryId != null) {
            // 判断是一级还是二级分类（parentId=0 的是一级）
            Category category = categoryMapper.selectById(categoryId);
            if (category != null && category.getParentId() == 0) {
                // 一级分类：筛选其下所有二级分类的商品
                params.put("parentCategoryId", categoryId);
            } else {
                // 二级分类：直接按 categoryId 筛选
                params.put("categoryId", categoryId);
            }
        }

        IPage<ProductVO> result = productMapper.selectProductPageByAdmin(page, params);
        return PageUtil.toPageResult(result);
    }

    /**
     * 商品详情查询（游客可访问）
     *
     * 关联商家店铺名，返回完整商品信息
     */
    @Override
    public ProductVO getProductDetail(Long productId) {
        // 查询商品（逻辑删除已由 MyBatis-Plus 全局配置处理）
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 转换为 VO
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setImages(product.getImages());
        vo.setDetail(product.getDetail());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setStock(product.getStock());
        vo.setSales(product.getSales());
        vo.setStatus(product.getStatus());
        vo.setSortOrder(product.getSortOrder());
        vo.setMerchantId(product.getMerchantId());
        vo.setCreateTime(product.getCreateTime());

        // 关联查询店铺名
        Merchant merchant = merchantMapper.selectById(product.getMerchantId());
        if (merchant != null) {
            vo.setShopName(merchant.getShopName());
        }

        return vo;
    }

    // ==================== 私有校验方法 ====================

    /**
     * 校验商家是否存在且审核通过
     *
     * @param merchantId 商家ID
     * @return 商家实体
     * @throws BusinessException 商家不存在或未通过审核时抛出
     */
    private Merchant validateMerchant(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商家信息不存在");
        }
        if (!MerchantAuditEnum.isApproved(merchant.getAuditStatus())) {
            throw new BusinessException("商家尚未通过审核，无法操作商品");
        }
        return merchant;
    }

    /**
     * 校验商品归属（商品必须属于当前商家）
     *
     * @param merchantId 当前登录商家ID
     * @param productId  待操作的商品ID
     * @return 商品实体
     * @throws BusinessException 商品不存在或不属于当前商家时抛出
     */
    private Product validateProductOwner(Long merchantId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作他人的商品");
        }
        return product;
    }
}
