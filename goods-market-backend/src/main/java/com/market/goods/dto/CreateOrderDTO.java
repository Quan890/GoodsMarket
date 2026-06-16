package com.market.goods.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 下单提交入参
 *
 * 接口：POST /user/order/create
 * 说明：用户提交订单，包含收货信息和商品明细列表
 *
 * @author goods-market
 */
@Data
public class CreateOrderDTO {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @Size(max = 20, message = "收货人电话不能超过20个字符")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    @Size(max = 300, message = "收货地址不能超过300个字符")
    private String receiverAddress;

    /**
     * 订单备注（可选）
     */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    /**
     * 订单商品明细列表（至少一项）
     */
    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDTO> items;

    /**
     * 订单商品明细子项（嵌套DTO）
     */
    @Data
    public static class OrderItemDTO {

        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 购买数量
         */
        @Min(value = 1, message = "购买数量至少为1")
        private Integer quantity;
    }
}
