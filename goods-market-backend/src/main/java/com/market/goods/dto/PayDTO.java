package com.market.goods.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付请求入参
 *
 * 接口：POST /user/order/pay
 * 说明：用户对已创建的订单发起支付，选择支付方式后生成支付参数返回前端
 *
 * @author goods-market
 */
@Data
public class PayDTO {

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /**
     * 支付方式：1=支付宝 2=微信
     */
    @NotNull(message = "支付方式不能为空")
    private Integer payMethod;
}
