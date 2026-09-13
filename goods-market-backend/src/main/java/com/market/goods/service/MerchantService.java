package com.market.goods.service;

import com.market.goods.dto.MerchantApplyDTO;
import com.market.goods.vo.MerchantStatsVO;
import com.market.goods.vo.MerchantVO;

/**
 * 商家模块 Service 接口
 *
 * @author goods-market
 */
public interface MerchantService {

    /**
     * 普通用户提交商家入驻申请
     *
     * @param userId  当前登录用户ID
     * @param dto     入驻申请信息
     */
    void apply(Long userId, MerchantApplyDTO dto);

    /**
     * 商家查看自己的入驻申请状态
     *
     * @param userId 当前登录用户ID
     * @return 商家信息视图（含审核状态描述）
     */
    MerchantVO getMyStatus(Long userId);

    /**
     * 商家经营统计（商家中心看板）
     *
     * @param userId 当前登录商家用户ID
     * @return 统计数据 VO
     */
    MerchantStatsVO getMyStats(Long userId);
}
