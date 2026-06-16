/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : goods_market_db

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 16/06/2026 23:05:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `checked` tinyint NOT NULL DEFAULT 1 COMMENT '是否选中：0=未选中 1=已选中',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2066889419482370051 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (1, 2001, 10001, 1, 1, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `cart` VALUES (2, 2001, 20003, 2, 1, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `cart` VALUES (3, 2001, 30001, 1, 0, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `cart` VALUES (4, 2002, 10002, 1, 1, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `cart` VALUES (5, 2002, 40001, 3, 1, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `cart` VALUES (2066883062523944961, 1, 10001, 1, 1, 0, '2026-06-16 21:58:18', '2026-06-16 21:58:18');
INSERT INTO `cart` VALUES (2066886432928206849, 2066883848716869633, 20002, 1, 1, 1, '2026-06-16 22:11:41', '2026-06-16 22:23:58');
INSERT INTO `cart` VALUES (2066887006230843394, 2066883848716869633, 10003, 1, 1, 1, '2026-06-16 22:13:58', '2026-06-16 22:23:58');
INSERT INTO `cart` VALUES (2066887036870234114, 2066883848716869633, 30003, 1, 1, 1, '2026-06-16 22:14:05', '2026-06-16 22:23:58');
INSERT INTO `cart` VALUES (2066887872232984578, 2066883848716869633, 40005, 1, 1, 1, '2026-06-16 22:17:24', '2026-06-16 22:23:58');
INSERT INTO `cart` VALUES (2066889419482370050, 2066883848716869633, 10005, 1, 1, 1, '2026-06-16 22:23:33', '2026-06-16 22:23:58');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID，0=顶级分类',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '数码电器', 0, 100, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (2, '服装服饰', 0, 90, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (3, '美妆护肤', 0, 80, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (4, '食品零食', 0, 70, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (5, '手机', 1, 10, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (6, '耳机', 1, 9, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (7, '智能穿戴', 1, 8, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (8, '男装', 2, 10, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (9, '女装', 2, 9, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (10, '童装', 2, 8, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (11, '面部护肤', 3, 10, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (12, '彩妆', 3, 9, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (13, '香水', 3, 8, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (14, '坚果炒货', 4, 10, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (15, '糕点饼干', 4, 9, NULL, 0, '2026-06-16 17:15:20');
INSERT INTO `category` VALUES (16, '饮品冲调', 4, 8, NULL, 0, '2026-06-16 17:15:20');

-- ----------------------------
-- Table structure for merchant
-- ----------------------------
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名称',
  `shop_logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺Logo',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺描述',
  `license_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `license_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业执照图片',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '经营地址',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0=待审核 1=审核通过 2=驳回',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2066885747373412355 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant
-- ----------------------------
INSERT INTO `merchant` VALUES (101, 1001, '数码优品旗舰店', NULL, '专注数码产品，品质保证，全国联保', '91110000MA01ABCDE', NULL, '张经理', '13800001111', '深圳市南山区科技园路1号', 1, '审核通过', 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `merchant` VALUES (102, 1002, '衣品风尚专营店', NULL, '潮流服饰，精选面料，穿出你的风格', '91310000MA01FGHIJ', NULL, '李经理', '13800002222', '广州市天河区体育西路100号', 1, '审核通过', 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `merchant` VALUES (103, 1003, '美颜工坊旗舰店', NULL, '大牌美妆，正品保障，美丽从这里开始', '91440000MA01KLMNO', NULL, '王经理', '13800003333', '上海市静安区南京西路200号', 1, '审核通过', 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `merchant` VALUES (104, 1004, '吃货小铺食品店', NULL, '精选各地美食零食，好吃不贵，吃货必囤', '91500000MA01PQRST', NULL, '赵经理', '13800004444', '成都市武侯区科华北路50号', 1, '审核通过', 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `merchant` VALUES (2066885747373412354, 2066883848716869633, 'quan', NULL, 'dfawefdsfdfwefdsfgdsger', NULL, NULL, 'quanlei', '18736234543', 'yns', 2, 'cs', 0, '2026-06-16 22:08:58', '2026-06-16 22:08:58');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0=待支付 1=已支付 2=已取消 3=已完成',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `pay_method` tinyint NULL DEFAULT NULL COMMENT '支付方式：1=支付宝 2=微信',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2066889521311682563 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (50001, 'GM20260615000001', 2001, 101, 7999.00, 7999.00, 3, '2026-06-15 10:30:00', 2, '张小明', '13900001111', '北京市朝阳区建国路88号', NULL, 0, '2026-06-16 17:15:20', '2026-06-16 21:41:06');
INSERT INTO `order` VALUES (50002, 'GM20260614000002', 2002, 102, 897.00, 897.00, 3, '2026-06-14 14:20:00', 1, '李小红', '13900002222', '上海市浦东新区陆家嘴环路1000号', '请用礼品包装', 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `order` VALUES (50003, 'GM20260616000003', 2003, 103, 417.00, 417.00, 2, NULL, NULL, '王大力', '13900003333', '广州市天河区体育西路200号', NULL, 0, '2026-06-16 17:15:20', '2026-06-16 17:50:00');
INSERT INTO `order` VALUES (50004, 'GM20260613000004', 2001, 104, 119.80, 119.80, 2, NULL, NULL, '张小明', '13900001111', '北京市朝阳区建国路88号', NULL, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `order` VALUES (2066889521311682562, 'GM2026061622235700000125', 2066883848716869633, 101, 7071.90, 7071.90, 3, '2026-06-16 22:24:00', 2, 'huan', '15253677281', 'yunshdf', '测试', 0, '2026-06-16 22:23:57', '2026-06-16 22:23:57');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（快照）',
  `product_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片（快照）',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '下单时单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '小计金额',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2066889521370402821 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 50001, 'GM20260615000001', 10001, 'iPhone 15 Pro', '/api/images/Electronics/03a0828dbcbf74c7133a4ae1f95cf993.png', 7999.00, 1, 7999.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (2, 50002, 'GM20260614000002', 20001, '商务休闲夹克', '/api/images/Cosmetics/a.png', 399.00, 1, 399.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (3, 50002, 'GM20260614000002', 20002, '法式优雅连衣裙', '/api/images/Cosmetics/fwe.png', 299.00, 1, 299.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (4, 50002, 'GM20260614000002', 20004, '儿童卡通T恤', '/api/images/Cosmetics/kz1.png', 79.00, 1, 79.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (5, 50003, 'GM20260616000003', 30001, '玻尿酸保湿精华液', '/api/images/Clothing/2b8e92c98c34000e7c77044a508f03f9.png', 159.00, 1, 159.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (6, 50003, 'GM20260616000003', 30002, '持妆哑光口红', '/api/images/Clothing/487738e7bc97c2a7f4843613115b4553.png', 99.00, 1, 99.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (7, 50003, 'GM20260616000003', 30003, '清透防晒霜SPF50+', '/api/images/Clothing/6d702bf7c1a071bffa6d23747f976cd4.png', 129.00, 1, 129.00, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (8, 50004, 'GM20260613000004', 40001, '每日坚果混合装', '/api/images/Snacks/1b435e07a0d8754cf9bfd7a0433016ce.png', 69.90, 1, 69.90, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (9, 50004, 'GM20260613000004', 40003, '冻干速溶咖啡', '/api/images/Snacks/3295cb76081b39c7d974d78ec33de953.png', 59.90, 1, 59.90, 0, '2026-06-16 17:15:20');
INSERT INTO `order_item` VALUES (2066889521341042690, 2066889521311682562, 'GM2026061622235700000125', 10005, '无线蓝牙音箱', '/api/images/Electronics/42197530b7188e0a68b21923f5240978.png', 599.00, 1, 599.00, 0, '2026-06-16 22:23:57');
INSERT INTO `order_item` VALUES (2066889521341042691, 2066889521311682562, 'GM2026061622235700000125', 40005, '芝士奶盖绿茶', '/api/images/Snacks/370358e6ef345da06865c804f0a06451.png', 45.90, 1, 45.90, 0, '2026-06-16 22:23:58');
INSERT INTO `order_item` VALUES (2066889521370402818, 2066889521311682562, 'GM2026061622235700000125', 30003, '清透防晒霜SPF50+', '/api/images/Clothing/6d702bf7c1a071bffa6d23747f976cd4.png', 129.00, 1, 129.00, 0, '2026-06-16 22:23:58');
INSERT INTO `order_item` VALUES (2066889521370402819, 2066889521311682562, 'GM2026061622235700000125', 10003, '智能手表 Ultra', '/api/images/Electronics/0b1daffd5f18f561da8d0fb80ac5d657.png', 5999.00, 1, 5999.00, 0, '2026-06-16 22:23:58');
INSERT INTO `order_item` VALUES (2066889521370402820, 2066889521311682562, 'GM2026061622235700000125', 20002, '法式优雅连衣裙', '/api/images/Cosmetics/fwe.png', 299.00, 1, 299.00, 0, '2026-06-16 22:23:58');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `subtitle` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `main_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品图片集（JSON数组）',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情（富文本）',
  `price` decimal(10, 2) NOT NULL COMMENT '售价',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=下架 1=上架',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（防超卖）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (10001, 101, 'iPhone 15 Pro', '钛金属设计 A17 Pro芯片 支持USB-C', 1, '/api/images/Electronics/03a0828dbcbf74c7133a4ae1f95cf993.png', '[\"/api/images/Electronics/03a0828dbcbf74c7133a4ae1f95cf993.png\",\"/api/images/Electronics/05a7b0a20126888c299f2928db727418.png\"]', NULL, 7998.00, 8999.00, 200, 1532, 1, 1, 100, 0, '2026-06-16 17:15:20', '2026-06-16 21:24:38');
INSERT INTO `product` VALUES (10002, 101, 'AirPods Pro 2', '自适应降噪 个性化空间音频 MagSafe充电', 6, '/api/images/Electronics/05a7b0a20126888c299f2928db727418.png', '[\"/api/images/Electronics/05a7b0a20126888c299f2928db727418.png\",\"/api/images/Electronics/0b1daffd5f18f561da8d0fb80ac5d657.png\"]', NULL, 1599.00, 1899.00, 500, 2876, 1, 0, 95, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `product` VALUES (10003, 101, '智能手表 Ultra', '钛合金表壳 双频GPS 深度计 血氧监测', 7, '/api/images/Electronics/0b1daffd5f18f561da8d0fb80ac5d657.png', '[\"/api/images/Electronics/0b1daffd5f18f561da8d0fb80ac5d657.png\",\"/api/images/Electronics/1e38c89a03f0925d50875c7ad234a2b8.png\"]', NULL, 5999.00, 6499.00, 149, 857, 1, 1, 90, 0, '2026-06-16 17:15:20', '2026-06-16 22:23:57');
INSERT INTO `product` VALUES (10004, 101, '降噪头戴式耳机', '高保真音质 主动降噪 40小时续航', 6, '/api/images/Electronics/1e38c89a03f0925d50875c7ad234a2b8.png', '[\"/api/images/Electronics/1e38c89a03f0925d50875c7ad234a2b8.png\",\"/api/images/Electronics/42197530b7188e0a68b21923f5240978.png\"]', NULL, 2499.00, 2999.00, 300, 1245, 1, 0, 85, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `product` VALUES (10005, 101, '无线蓝牙音箱', '360度环绕立体声 IPX7防水 户外便携', 6, '/api/images/Electronics/42197530b7188e0a68b21923f5240978.png', '[\"/api/images/Electronics/42197530b7188e0a68b21923f5240978.png\",\"/api/images/Electronics/b9e9c7612a90ee0d2174e5267c744167.png\"]', NULL, 599.00, 799.00, 799, 3422, 1, 3, 80, 0, '2026-06-16 17:15:20', '2026-06-16 22:23:57');
INSERT INTO `product` VALUES (20001, 102, '商务休闲夹克', '春秋新款 简约百搭 防风防水面料', 8, '/api/images/Cosmetics/a.png', '[\"/api/images/Cosmetics/a.png\",\"/api/images/Cosmetics/at.png\"]', NULL, 399.00, 599.00, 600, 2134, 0, 3, 100, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (20002, 102, '法式优雅连衣裙', '碎花印花 收腰设计 轻盈飘逸 春夏必备', 9, '/api/images/Cosmetics/fwe.png', '[\"/api/images/Cosmetics/fwe.png\",\"/api/images/Cosmetics/gdzs.png\"]', NULL, 299.00, 459.00, 399, 1877, 1, 1, 95, 0, '2026-06-16 17:15:20', '2026-06-16 22:23:57');
INSERT INTO `product` VALUES (20003, 102, '潮流连帽卫衣', '宽松廓形 纯棉面料 情侣款可选', 8, '/api/images/Cosmetics/at.png', '[\"/api/images/Cosmetics/at.png\",\"/api/images/Cosmetics/fase.png\"]', NULL, 199.00, 299.00, 1000, 4521, 1, 0, 90, 1, '2026-06-16 17:15:20', '2026-06-16 21:41:25');
INSERT INTO `product` VALUES (20004, 102, '儿童卡通T恤', 'A类纯棉 亲肤透气 可爱印花 不褪色', 10, '/api/images/Cosmetics/kz1.png', '[\"/api/images/Cosmetics/kz1.png\",\"/api/images/Cosmetics/raw.png\"]', NULL, 79.00, 129.00, 2000, 6234, 1, 0, 85, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (20005, 102, '复古牛仔外套', '做旧水洗 经典版型 四季百搭', 8, '/api/images/Cosmetics/hre.png', '[\"/api/images/Cosmetics/hre.png\",\"/api/images/Cosmetics/weqt.png\"]', NULL, 259.00, 399.00, 450, 1567, 1, 0, 80, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (30001, 103, '玻尿酸保湿精华液', '深层补水 持久保湿 敏感肌适用 30ml', 11, '/api/images/Clothing/2b8e92c98c34000e7c77044a508f03f9.png', '[\"/api/images/Clothing/2b8e92c98c34000e7c77044a508f03f9.png\",\"/api/images/Clothing/3642a9573ccc0d26542db3fc9214707a.png\"]', NULL, 159.00, 239.00, 1201, 8975, 1, 1, 100, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (30002, 103, '持妆哑光口红', '雾面质感 不沾杯 显色持久 多色可选', 12, '/api/images/Clothing/487738e7bc97c2a7f4843613115b4553.png', '[\"/api/images/Clothing/487738e7bc97c2a7f4843613115b4553.png\",\"/api/images/Clothing/4d6df977aef108e46e5b07622a93246a.png\"]', NULL, 99.00, 159.00, 2001, 12542, 1, 1, 95, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (30003, 103, '清透防晒霜SPF50+', '轻薄不油腻 养肤防晒 全波段防护 50ml', 11, '/api/images/Clothing/6d702bf7c1a071bffa6d23747f976cd4.png', '[\"/api/images/Clothing/6d702bf7c1a071bffa6d23747f976cd4.png\",\"/api/images/Clothing/78b6b2ef3b7b98bccef2eb8999d81301.png\"]', NULL, 129.00, 189.00, 800, 5432, 1, 2, 90, 0, '2026-06-16 17:15:20', '2026-06-16 22:23:57');
INSERT INTO `product` VALUES (30004, 103, '花漾淡香水', '清新花果调 持久留香 日常通勤适用 50ml', 13, '/api/images/Clothing/8ede3bd7e9655add513ab07fe8e11a75.png', '[\"/api/images/Clothing/8ede3bd7e9655add513ab07fe8e11a75.png\",\"/api/images/Clothing/b731b0e844c7c594362856984d552230.png\"]', NULL, 299.00, 459.00, 350, 2345, 1, 0, 85, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (30005, 103, '烟酰胺美白面膜', '提亮肤色 补水保湿 28ml*10片装', 11, '/api/images/Clothing/d5da0717fb66f2bb24bb5ff75c7adb4c.png', '[\"/api/images/Clothing/d5da0717fb66f2bb24bb5ff75c7adb4c.png\",\"/api/images/Clothing/e6812ac805fcdac6a09636be08b252f3.jpg\"]', NULL, 89.00, 139.00, 1500, 9876, 1, 0, 80, 0, '2026-06-16 17:15:20', '2026-06-16 18:15:27');
INSERT INTO `product` VALUES (40001, 104, '每日坚果混合装', '750g罐装 6种坚果+3种果干 每日一袋营养均衡', 14, '/api/images/Snacks/1b435e07a0d8754cf9bfd7a0433016ce.png', '[\"/api/images/Snacks/1b435e07a0d8754cf9bfd7a0433016ce.png\",\"/api/images/Snacks/1d81ffdc1985ee08a3d257ee43f19380.png\"]', NULL, 69.90, 99.90, 3000, 15678, 1, 0, 100, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `product` VALUES (40002, 104, '手工曲奇饼干礼盒', '黄油曲奇 抹茶曲奇 巧克力曲奇 三种口味', 15, '/api/images/Snacks/1d81ffdc1985ee08a3d257ee43f19380.png', '[\"/api/images/Snacks/1d81ffdc1985ee08a3d257ee43f19380.png\",\"/api/images/Snacks/3295cb76081b39c7d974d78ec33de953.png\"]', NULL, 49.90, 79.90, 1500, 8765, 1, 0, 95, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `product` VALUES (40003, 104, '冻干速溶咖啡', '100%阿拉比卡 冷萃工艺 3秒速溶 20条装', 16, '/api/images/Snacks/3295cb76081b39c7d974d78ec33de953.png', '[\"/api/images/Snacks/3295cb76081b39c7d974d78ec33de953.png\",\"/api/images/Snacks/336440e3c3178ee1b4d7cf03b9ac4361.png\"]', NULL, 59.90, 89.90, 2000, 11234, 1, 0, 90, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `product` VALUES (40004, 104, '麻辣牛肉干', '精选牛后腿肉 麻辣鲜香 独立小包装 250g', 14, '/api/images/Snacks/336440e3c3178ee1b4d7cf03b9ac4361.png', '[\"/api/images/Snacks/336440e3c3178ee1b4d7cf03b9ac4361.png\",\"/api/images/Snacks/370358e6ef345da06865c804f0a06451.png\"]', NULL, 39.90, 59.90, 1800, 7654, 1, 0, 85, 0, '2026-06-16 17:15:20', NULL);
INSERT INTO `product` VALUES (40005, 104, '芝士奶盖绿茶', '新西兰芝士 现萃绿茶 冷热皆宜 6杯装', 16, '/api/images/Snacks/370358e6ef345da06865c804f0a06451.png', '[\"/api/images/Snacks/370358e6ef345da06865c804f0a06451.png\",\"/api/images/Snacks/5fc91fdb0000480146df0f6a143a9f64.png\"]', NULL, 45.90, 69.90, 1199, 5433, 1, 1, 80, 0, '2026-06-16 17:15:20', '2026-06-16 22:23:57');

-- ----------------------------
-- Table structure for product_collect
-- ----------------------------
DROP TABLE IF EXISTS `product_collect`;
CREATE TABLE `product_collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2066889416718323715 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_collect
-- ----------------------------
INSERT INTO `product_collect` VALUES (1, 2001, 10001, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (2, 2001, 10003, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (3, 2001, 30002, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (4, 2002, 20002, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (5, 2002, 40003, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (6, 2003, 10001, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (7, 2003, 20001, 0, '2026-06-16 17:15:20');
INSERT INTO `product_collect` VALUES (2066886436661137410, 2066883848716869633, 20002, 0, '2026-06-16 22:11:42');
INSERT INTO `product_collect` VALUES (2066886850391478274, 2066883848716869633, 40003, 0, '2026-06-16 22:13:21');
INSERT INTO `product_collect` VALUES (2066887003080921090, 2066883848716869633, 10003, 0, '2026-06-16 22:13:57');
INSERT INTO `product_collect` VALUES (2066887038673784833, 2066883848716869633, 30003, 0, '2026-06-16 22:14:06');
INSERT INTO `product_collect` VALUES (2066887870572040194, 2066883848716869633, 40005, 0, '2026-06-16 22:17:24');
INSERT INTO `product_collect` VALUES (2066889416718323714, 2066883848716869633, 10005, 0, '2026-06-16 22:23:33');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色：0=游客 1=普通用户 2=商家 3=管理员',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2066883848716869634 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '系统管理员', NULL, '13700000000', 'admin@market.com', 3, 1, 0, '2026-06-16 21:36:28', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (1001, 'merchant_digital', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '数码优品旗舰店', NULL, '13800001111', 'digital@shop.com', 2, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (1002, 'merchant_fashion', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '衣品风尚专营店', NULL, '13800002222', 'fashion@shop.com', 2, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (1003, 'merchant_beauty', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '美颜工坊旗舰店', NULL, '13800003333', 'beauty@shop.com', 2, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (1004, 'merchant_snack', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '吃货小铺食品店', NULL, '13800004444', 'snack@shop.com', 2, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (2001, 'user_zhang', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '张小明', NULL, '13900001111', 'zhang@user.com', 1, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (2002, 'user_li', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '李小红', NULL, '13900002222', 'li@user.com', 1, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (2003, 'user_wang', '$2b$10$l3y9aoQQl2vex3i3rOJmOuYxZtUMvzg2ymO6MLVH3S8q5lFGPs21q', '王大力', NULL, '13900003333', 'wang@user.com', 1, 1, 0, '2026-06-16 17:15:20', '2026-06-16 21:55:16');
INSERT INTO `user` VALUES (2066883848716869633, 'quan', '$2a$10$5Y2pd4Gwcl6SmPnIEUxtdOIpWXpco6WeHApBjJRrpZEBKcBUga7o6', 'quan', NULL, '15123672912', NULL, 1, 1, 0, '2026-06-16 22:01:25', '2026-06-16 22:01:25');

SET FOREIGN_KEY_CHECKS = 1;
