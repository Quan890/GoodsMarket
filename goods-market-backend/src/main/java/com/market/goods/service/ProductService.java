package com.market.goods.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Product;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.OrderVO;
import com.market.goods.vo.ProductVO;

/**
 * 商品模块 Service 接口
 *
 * @author goods-market
 */
public interface ProductService {

    /**
     * 商家新增商品（自动绑定当前登录商家ID）
     *
     * @param merchantId 当前登录商家ID
     * @param product    商品信息（前端传入的字段）
     */
    void addProduct(Long merchantId, Product product);

    /**
     * 商家编辑商品（校验归属，只能编辑自己名下商品）
     *
     * @param merchantId 当前登录商家ID
     * @param product    待更新的商品信息（必须包含 id）
     */
    void editProduct(Long merchantId, Product product);

    /**
     * 商家上下架商品（校验归属）
     *
     * @param merchantId 当前登录商家ID
     * @param productId  商品ID
     * @param status     目标状态：0=下架 1=上架
     */
    void changeProductStatus(Long merchantId, Long productId, Integer status);

    /**
     * 商家删除商品（逻辑删除，校验归属）
     *
     * @param merchantId 当前登录商家ID
     * @param productId  商品ID
     */
    void deleteProduct(Long merchantId, Long productId);

    /**
     * 商家分页查询自己名下所有商品
     *
     * @param merchantId 当前登录商家ID
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param status     上下架状态（可选）
     * @return 分页结果
     */
    PageResult<ProductVO> listMyProducts(Long merchantId, int pageNum, int pageSize, Integer status);

    /**
     * 商家查看自己商品产生的订单列表
     *
     * @param merchantId 当前登录商家ID
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param status     订单状态（可选）
     * @return 分页结果
     */
    PageResult<OrderVO> listMyProductOrders(Long merchantId, int pageNum, int pageSize, Integer status);

    /**
     * 公共商品分页列表查询（游客可访问）
     *
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param keyword    搜索关键词（可选，模糊匹配商品名）
     * @return 分页结果
     */
    PageResult<ProductVO> listPublicProducts(int pageNum, int pageSize, String keyword, Long categoryId);

    /**
     * 商品详情查询（游客可访问）
     *
     * @param productId 商品ID
     * @return 商品详情（含店铺名）
     */
    ProductVO getProductDetail(Long productId);
}
