package com.market.goods.config;

import com.market.goods.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 *
 * 启用 Spring @Scheduled 注解，定义定时执行的任务。
 * 当前包含：
 *   - 超时未支付订单自动关闭（每5分钟执行一次）
 *
 * @author goods-market
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final OrderService orderService;

    /**
     * 每5分钟检查一次超时未支付订单
     *
     * cron 表达式：秒 分 时 日 月 星期
     * "0 * /5 * * * ?" = 每隔5分钟的第0秒执行
     *
     * 逻辑：查询创建超过30分钟且状态为待支付(0)的订单，
     *       批量关闭并回滚商品库存
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void closeTimeoutOrders() {
        try {
            orderService.closeTimeoutOrders();
        } catch (Exception e) {
            log.error("定时任务执行异常：closeTimeoutOrders - {}", e.getMessage(), e);
        }
    }
}
