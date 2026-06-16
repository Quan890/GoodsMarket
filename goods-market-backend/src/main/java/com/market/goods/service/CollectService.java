package com.market.goods.service;

import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.CollectVO;

/**
 * 商品收藏模块 Service 接口
 *
 * @author goods-market
 */
public interface CollectService {

    /**
     * 收藏商品
     *
     * @param userId    当前登录用户ID
     * @param productId 商品ID
     */
    void addCollect(Long userId, Long productId);

    /**
     * 取消收藏
     *
     * @param userId    当前登录用户ID
     * @param productId 商品ID
     */
    void cancelCollect(Long userId, Long productId);

    /**
     * 分页查询个人收藏列表（关联商品信息）
     *
     * @param userId   当前登录用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 收藏列表（CollectVO 包含商品名称、图片、价格等）
     */
    PageResult<CollectVO> listMyCollects(Long userId, int pageNum, int pageSize);

    /**
     * 查询当前商品是否被本人收藏
     *
     * @param userId    当前登录用户ID
     * @param productId 商品ID
     * @return true=已收藏，false=未收藏
     */
    boolean isCollected(Long userId, Long productId);
}
