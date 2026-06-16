package com.market.goods.util;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * 微信支付 V3 工具类
 *
 * 基于微信支付官方 Java SDK（wechatpay-java）封装：
 *   1. 初始化 RSAAutoCertificateConfig（自动更新微信平台证书）
 *   2. 提供签名、验签、回调报文解析能力
 *
 * 懒加载机制：
 *   - Spring 启动时检查配置是否为占位符，如果是则跳过初始化（config=null）
 *   - 首次调用支付相关方法时才真正初始化（此时需确保配置正确 + 私钥文件存在）
 *   - 开发阶段未配置真实商户号时，项目可正常启动，仅支付功能不可用
 *
 * @author goods-market
 */
@Slf4j
@Component
public class WxPayV3Util {

    /** 微信支付配置（未配置时为 null） */
    private RSAAutoCertificateConfig config;

    /** 商户号 */
    private final String mchId;

    /** 私钥文件路径 */
    private final String privateKeyPath;

    /** 商户API证书序列号 */
    private final String mchSerialNo;

    /** APIv3 密钥 */
    private final String apiV3Key;

    /** 是否已初始化 */
    private volatile boolean initialized = false;

    public WxPayV3Util(
            @Value("${wechat.pay.mch-id:}") String mchId,
            @Value("${wechat.pay.mch-serial-no:}") String mchSerialNo,
            @Value("${wechat.pay.private-key-path:}") String privateKeyPath,
            @Value("${wechat.pay.api-v3-key:}") String apiV3Key) {
        this.mchId = mchId;
        this.mchSerialNo = mchSerialNo;
        this.privateKeyPath = privateKeyPath;
        this.apiV3Key = apiV3Key;

        // 启动时不初始化，避免配置为占位符时导致项目启动失败
        // 首次调用支付方法时再初始化（懒加载）
        if (isConfigReady()) {
            log.info("微信支付配置已检测到，将在首次使用时初始化");
        } else {
            log.info("微信支付配置未填写（占位符），支付功能暂不可用，项目正常启动");
        }
    }

    /**
     * 检查配置是否已填写真实值（非占位符、非空）
     */
    private boolean isConfigReady() {
        return mchId != null && !mchId.isBlank()
                && !mchId.contains("YOUR_")
                && !mchId.equals("1600000000")
                && mchSerialNo != null && !mchSerialNo.isBlank()
                && !mchSerialNo.contains("YOUR_")
                && privateKeyPath != null && !privateKeyPath.isBlank()
                && apiV3Key != null && !apiV3Key.isBlank()
                && !apiV3Key.contains("YOUR_");
    }

    /**
     * 懒加载初始化（首次使用时调用）
     */
    private synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        if (!isConfigReady()) {
            throw new RuntimeException("微信支付未配置，请在 application.yml 中填写 wechat.pay.* 真实参数");
        }

        try {
            // 读取商户私钥
            PrivateKey privateKey = loadPrivateKeyFromPem(privateKeyPath);

            // 构建自动证书配置
            this.config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(mchId)
                    .privateKey(privateKey)
                    .merchantSerialNumber(mchSerialNo)
                    .apiV3Key(apiV3Key)
                    .build();

            this.initialized = true;
            log.info("微信支付V3配置初始化成功：mchId={}", mchId);

        } catch (Exception e) {
            log.error("微信支付V3配置初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("微信支付初始化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取商户号
     */
    public String getMchId() {
        return mchId;
    }

    /**
     * 创建 Native 支付预下单
     *
     * @param outTradeNo  商户订单号
     * @param totalFen    金额（单位：分）
     * @param description 商品描述
     * @param notifyUrl   回调地址
     * @return Map 包含 codeUrl（二维码链接）
     */
    public Map<String, Object> createNativePrepay(String outTradeNo, int totalFen,
                                                   String description, String notifyUrl) {
        ensureInitialized();
        try {
            com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
            com.wechat.pay.java.service.payments.nativepay.model.Amount amount =
                    new com.wechat.pay.java.service.payments.nativepay.model.Amount();
            amount.setTotal(totalFen);
            amount.setCurrency("CNY");
            request.setAmount(amount);
            request.setMchid(mchId);
            request.setDescription(description);
            request.setOutTradeNo(outTradeNo);
            request.setNotifyUrl(notifyUrl);

            com.wechat.pay.java.service.payments.nativepay.NativePayService service =
                    new com.wechat.pay.java.service.payments.nativepay.NativePayService.Builder()
                            .config(config)
                            .build();
            com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse response =
                    service.prepay(request);

            Map<String, Object> result = new HashMap<>();
            result.put("codeUrl", response.getCodeUrl());
            return result;
        } catch (Exception e) {
            log.error("微信Native支付预下单失败：outTradeNo={}, error={}", outTradeNo, e.getMessage(), e);
            throw new RuntimeException("微信支付预下单失败", e);
        }
    }

    /**
     * 解析微信支付回调通知（返回 Map）
     */
    public Map<String, String> parseNotifyToMap(HttpServletRequest request) {
        ensureInitialized();
        try {
            Transaction transaction = parseNotify(request);
            Map<String, String> result = new HashMap<>();
            result.put("out_trade_no", transaction.getOutTradeNo());
            result.put("trade_state", transaction.getTradeState().name());
            result.put("transaction_id", transaction.getTransactionId());
            // 返回微信实际支付金额（单位：分），用于与订单金额比对防篡改
            if (transaction.getAmount() != null) {
                result.put("total", String.valueOf(transaction.getAmount().getTotal()));
            }
            return result;
        } catch (Exception e) {
            log.error("解析微信支付回调失败：{}", e.getMessage(), e);
            throw new RuntimeException("解析微信支付回调失败", e);
        }
    }

    /**
     * 解析微信支付回调通知报文
     */
    public Transaction parseNotify(HttpServletRequest request) throws Exception {
        ensureInitialized();
        String signature = request.getHeader("Wechatpay-Signature");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String serial = request.getHeader("Wechatpay-Serial");
        String body = readRequestBody(request);

        log.info("收到微信支付回调：serial={}, timestamp={}", serial, timestamp);

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body)
                .build();

        NotificationParser parser = new NotificationParser(config);
        return parser.parse(requestParam, Transaction.class);
    }

    /**
     * 从 PEM 文件读取商户私钥
     */
    private PrivateKey loadPrivateKeyFromPem(String pemPath) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(pemPath);
        if (is == null) {
            throw new RuntimeException("私钥文件不存在：" + pemPath + "，请将商户私钥放到 resources/cert/ 目录下");
        }

        String pemContent = new String(is.readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(pemContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 读取 HttpServletRequest 请求体
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
