package com.market.goods.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情视图对象（带商品明细列表）
 *
 * 返回场景：订单列表页、订单详情页
 * 说明：一个订单对应一个商家，一个订单可包含多个商品（OrderItemVO 列表）
 *
 * @author goods-market
 */
@Data
public class OrderVO {

    /** 订单ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID（买家） */
    private Long userId;

    /** 商家ID */
    private Long merchantId;

    /** 店铺名称 */
    private String shopName;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /**
     * 订单状态
     * 0=待支付 1=已支付 2=已取消 3=已完成
     */
    private Integer status;

    /**
     * 订单状态描述（前端直接展示，无需再做枚举映射）
     * "待支付" / "已支付" / "已取消" / "已完成"
     */
    private String statusDesc;

    /** 支付时间 */
    private LocalDateTime payTime;

    /**
     * 支付方式
     * 1=支付宝 2=微信
     */
    private Integer payMethod;

    /**
     * 支付方式描述
     * "支付宝" / "微信"
     */
    private String payMethodDesc;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 订单备注 */
    private String remark;

    /** 订单创建时间 */
    private LocalDateTime createTime;

    /** 订单商品明细列表 */
    private List<OrderItemVO> items;

    /**
     * 订单商品明细子项（内嵌VO）
     */
    @Data
    public static class OrderItemVO {

        /** 订单明细ID */
        private Long id;

        /** 商品ID */
        private Long productId;

        /** 商品名称（下单时快照） */
        private String productName;

        /** 商品图片（下单时快照） */
        private String productImage;

        /** 下单时单价 */
        private BigDecimal unitPrice;

        /** 购买数量 */
        private Integer quantity;

        /** 小计金额 */
        private BigDecimal totalPrice;
    }
}
