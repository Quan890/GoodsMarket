package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.ProductCollect;
import com.market.goods.vo.CollectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品收藏表 Mapper 接口
 *
 * @author goods-market
 */
@Mapper
public interface CollectMapper extends BaseMapper<ProductCollect> {

    /**
     * 分页查询用户收藏列表（关联商品表获取最新商品信息）
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 收藏列表（CollectVO 包含商品名称、图片、价格等）
     */
    IPage<CollectVO> selectCollectPageWithProduct(Page<CollectVO> page, @Param("userId") Long userId);

    /**
     * 查询已取消收藏的记录（忽略逻辑删除过滤器）
     * 用于恢复收藏时查找 deleted=1 的记录
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 已删除的收藏记录（可能为null）
     */
    ProductCollect selectDeletedRecord(@Param("userId") Long userId, @Param("productId") Long productId);
}
