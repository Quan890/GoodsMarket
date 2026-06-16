package com.market.goods.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图形验证码工具类
 *
 * 生成 4 位随机字母+数字验证码图片（Base64 编码）
 * 验证码存储在 Redis 中，key 为 token，有效期 5 分钟
 *
 * @author goods-market
 */
public class CaptchaUtil {

    private CaptchaUtil() {}

    /** 验证码字符集（去掉容易混淆的 0/O/1/I/l） */
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    /** 验证码长度 */
    private static final int CODE_LENGTH = 4;

    /** 图片宽度 */
    private static final int WIDTH = 120;

    /** 图片高度 */
    private static final int HEIGHT = 40;

    /** 验证码 Redis key 前缀 */
    public static final String CAPTCHA_PREFIX = "captcha:";

    /** 验证码有效期（秒） */
    public static final int CAPTCHA_EXPIRE_SECONDS = 300;

    /**
     * 生成随机验证码文本
     */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片（Base64 编码）
     *
     * @param code 验证码文本
     * @return Base64 编码的 PNG 图片（可直接用于 <img src="data:image/png;base64,...">）
     */
    public static String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 随机干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(randomColor(150, 200));
            int x1 = ThreadLocalRandom.current().nextInt(WIDTH);
            int y1 = ThreadLocalRandom.current().nextInt(HEIGHT);
            int x2 = ThreadLocalRandom.current().nextInt(WIDTH);
            int y2 = ThreadLocalRandom.current().nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码字符
        Font font = new Font("Arial", Font.BOLD, 28);
        g.setFont(font);
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(20, 120));
            int x = 10 + i * 26;
            int y = 30 + ThreadLocalRandom.current().nextInt(-5, 6);
            // 随机旋转
            double theta = Math.toRadians(ThreadLocalRandom.current().nextInt(-20, 21));
            g.rotate(theta, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-theta, x, y);
        }

        // 随机噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(randomColor(100, 200));
            int x = ThreadLocalRandom.current().nextInt(WIDTH);
            int y = ThreadLocalRandom.current().nextInt(HEIGHT);
            g.fillOval(x, y, 3, 3);
        }

        g.dispose();

        // 转为 Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    private static Color randomColor(int min, int max) {
        int r = ThreadLocalRandom.current().nextInt(min, max);
        int g = ThreadLocalRandom.current().nextInt(min, max);
        int b = ThreadLocalRandom.current().nextInt(min, max);
        return new Color(r, g, b);
    }
}
