package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Product;
import com.market.goods.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 商品表 Mapper 接口
 *
 * @author goods-market
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 乐观锁扣减库存
     *
     * 执行SQL：UPDATE product SET stock = stock - #{quantity}, sales = sales + #{quantity},
     *          version = version + 1, update_time = NOW()
     *          WHERE id = #{productId} AND version = #{version} AND deleted = 0 AND stock >= #{quantity}
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @param version   当前乐观锁版本号
     * @return 影响行数（0=扣减失败，版本不匹配或库存不足）
     */
    int deductStock(@Param("productId") Long productId,
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);

    /**
     * 乐观锁恢复库存（取消订单时回滚）
     *
     * @param productId 商品ID
     * @param quantity  恢复数量
     * @param version   当前乐观锁版本号
     * @return 影响行数
     */
    int restoreStock(@Param("productId") Long productId,
                     @Param("quantity") Integer quantity,
                     @Param("version") Integer version);

    /**
     * 管理员多条件分页查询商品（关联商家店铺名）
     *
     * @param page   分页参数
     * @param params 查询条件：name（商品名模糊）、merchantId（商家ID）、status（上下架状态）
     * @return 分页结果（ProductVO 包含 shopName）
     */
    IPage<ProductVO> selectProductPageByAdmin(Page<ProductVO> page, @Param("params") Map<String, Object> params);
}
