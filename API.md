# 好物集市 API 文档

**Base URL:** `http://localhost:8080/api`

**认证方式:** 请求头 `Authorization: Bearer <token>`

**统一响应格式:**
```json
{ "code": 200, "message": "success", "data": { ... } }
```

**分页响应格式:**
```json
{ "code": 200, "message": "success", "data": { "records": [], "total": 100, "current": 1, "size": 10, "pages": 10 } }
```

**序列化约定:**
- 雪花 ID（超出 JS 安全整数范围的 Long）序列化为**字符串**，小数值保持数字；
- `LocalDateTime` 统一序列化为 `yyyy-MM-dd HH:mm:ss`。

**订单状态:** 0=待支付，1=已支付（待发货），2=已取消，3=已完成，4=已发货（待收货）。

---

## 一、用户模块 `/user`

### 1.1 获取图形验证码

```
GET /api/user/captcha
```

无需登录。返回 `{ captchaToken, captchaImage (Base64) }`。

### 1.2 发送短信验证码

```
POST /api/user/send-code
Content-Type: application/json

{ "phone": "13800138000" }
```

60 秒内不可重复发送。

### 1.3 用户注册

```
POST /api/user/register
Content-Type: application/json

{ "phone": "13800138000", "code": "123456", "username": "myname", "password": "123456" }
```

### 1.4 短信验证码登录

```
POST /api/user/login
Content-Type: application/json

{ "phone": "13800138000", "code": "123456" }
```

首次登录自动注册（角色=游客）。返回 `{ token, userId, nickname, role, roleName }`。

### 1.5 密码登录

```
POST /api/user/login-password
Content-Type: application/json

{ "username": "admin", "password": "123456", "captchaCode": "abcd", "captchaToken": "xxx" }
```

需要先调用 `GET /user/captcha` 获取验证码。

### 1.6 找回密码

```
POST /api/user/reset-password
Content-Type: application/json

{ "phone": "13800138000", "code": "123456", "newPassword": "654321" }
```

### 1.7 获取当前用户信息

```
GET /api/user/info
Authorization: Bearer <token>
```

返回 `{ userId, username, nickname, phone, role, roleName, avatar }`。

### 1.8 退出登录

```
POST /api/user/logout
Authorization: Bearer <token>
```

### 1.9 注销账户

```
POST /api/user/delete-account
Authorization: Bearer <token>
Content-Type: application/json

{ "password": "123456" }
```

管理员不可注销。商家需先删除所有商品。

### 1.10 修改密码（需登录）

```
PUT /api/user/password
Authorization: Bearer <token>
Content-Type: application/json

{ "oldPassword": "123456", "newPassword": "new123456" }
```

校验原密码，新密码 6-20 位。修改成功后原 token 失效，需重新登录。

---

## 二、商品模块 `/product`

### 2.1 商品列表（公开）

```
GET /api/product/list?pageNum=1&pageSize=10&keyword=手机&categoryId=5
```

只返回上架商品。`keyword` 按商品名模糊搜索，`categoryId` 按分类筛选。

### 2.2 商品详情（公开）

```
GET /api/product/detail/{id}
```

返回完整商品信息 + 店铺名。

---

## 三、商家商品管理 `/merchant/product`

> 以下接口需要商家或管理员角色。

### 3.1 新增商品

```
POST /api/merchant/product/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "蓝牙耳机", "subtitle": "高清音质",
  "price": 199.00, "originalPrice": 299.00,
  "stock": 100, "mainImage": "/images/xxx.png",
  "categoryId": 6
}
```

### 3.2 编辑商品

```
PUT /api/merchant/product/edit
Authorization: Bearer <token>
Content-Type: application/json

{ "id": 10001, "name": "蓝牙耳机Pro", "price": 299.00 }
```

### 3.3 上架 / 下架

```
PUT /api/merchant/product/status?productId=10001&status=0
Authorization: Bearer <token>
```

`status`: 1=上架，0=下架。

### 3.4 删除商品

```
DELETE /api/merchant/product/delete/{id}
Authorization: Bearer <token>
```

逻辑删除。

### 3.5 我的商品列表

```
GET /api/merchant/product/my?pageNum=1&pageSize=10&status=1
Authorization: Bearer <token>
```

### 3.6 我的商品订单

```
GET /api/merchant/product/orders?pageNum=1&pageSize=10&status=1
Authorization: Bearer <token>
```

---

## 四、商家入驻 `/merchant`

### 4.1 提交入驻申请

```
POST /api/merchant/apply
Authorization: Bearer <token>
Content-Type: application/json

{
  "shopName": "好物小铺",
  "contactName": "张三",
  "contactPhone": "13800138000",
  "address": "北京市朝阳区xxx",
  "description": "精选好物，品质保证",
  "licenseNo": "91110000MA01ABCDE"
}
```

任何已登录用户均可提交。`shopName`、`contactName`、`contactPhone`、`address` 为必填。

### 4.2 查询申请状态

```
GET /api/merchant/my/status
Authorization: Bearer <token>
```

返回 `auditStatus`: 0=待审核，1=已通过，2=已驳回。

### 4.3 商家发货（需商家角色）

```
PUT /api/merchant/order/ship/{orderNo}
Authorization: Bearer <token>
```

本店铺已支付订单标记为已发货，详见 8.9。

### 4.4 商家经营统计（需商家角色）

```
GET /api/merchant/stats
Authorization: Bearer <token>
```

返回商家中心看板数据：`totalProducts`（商品总数）、`onSaleProducts`（在售数）、`totalOrders`、`todayOrders`、`pendingShipOrders`（待发货）、`shippedOrders`（已发货）、`completedOrders`、`totalSales`、`todaySales`（销售额，元）。

---

## 五、分类 `/category`

### 5.1 分类树

```
GET /api/category/tree
```

返回两级分类树（顶级 + 子分类）。

---

## 六、收藏 `/user/collect`

### 6.1 添加收藏

```
POST /api/user/collect/add?productId=10001
Authorization: Bearer <token>
```

### 6.2 取消收藏

```
DELETE /api/user/collect/cancel?productId=10001
Authorization: Bearer <token>
```

### 6.3 收藏列表

```
GET /api/user/collect/list?pageNum=1&pageSize=10
Authorization: Bearer <token>
```

### 6.4 检查是否已收藏

```
GET /api/user/collect/check?productId=10001
Authorization: Bearer <token>
```

返回 `{ "collected": true/false }`。

---

## 七、购物车 `/user/cart`

### 7.1 加入购物车

```
POST /api/user/cart/add
Authorization: Bearer <token>
Content-Type: application/json

{ "productId": 10001, "quantity": 2 }
```

已存在则累加数量。

### 7.2 修改数量

```
PUT /api/user/cart/quantity
Authorization: Bearer <token>
Content-Type: application/json

{ "productId": 10001, "quantity": 5 }
```

`quantity` 为 0 时删除该商品。

### 7.3 删除商品

```
DELETE /api/user/cart/delete?productId=10001
Authorization: Bearer <token>
```

### 7.4 购物车列表

```
GET /api/user/cart/list?pageNum=1&pageSize=50
Authorization: Bearer <token>
```

---

## 八、订单 `/user/order`

### 8.1 创建订单

```
POST /api/user/order/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverAddress": "北京市朝阳区xxx",
  "remark": "请尽快发货",
  "items": [
    { "productId": 10001, "quantity": 1 },
    { "productId": 20003, "quantity": 2 }
  ]
}
```

乐观锁扣减库存，事务保证一致性。**按商家拆单**：结算商品跨多个商家时，每个商家生成一个子订单，返回订单号**数组**（单商家返回 1 个元素）：

```json
{ "code": 200, "data": ["GM2026091310564100000116", "GM2026091310564100000231"] }
```

### 8.2 我的订单列表

```
GET /api/user/order/list?pageNum=1&pageSize=10&status=1
Authorization: Bearer <token>
```

`status`: 0=待支付，1=已支付（待发货），2=已取消，3=已完成，4=已发货（待收货）。列表含商品明细 `items`。

### 8.3 订单详情

```
GET /api/user/order/detail/{orderNo}
Authorization: Bearer <token>
```

返回订单信息 + 商品明细列表。买家校验归属，商家可查看自己店铺的订单。

### 8.4 取消订单

```
PUT /api/user/order/cancel/{orderNo}
Authorization: Bearer <token>
```

仅待支付状态可取消，自动回滚库存。

### 8.5 模拟支付（调试用）

```
POST /api/user/order/mock-pay
Authorization: Bearer <token>
Content-Type: application/json

{ "orderNo": "GM20260616000001", "payMethod": 2 }
```

`payMethod`: 1=支付宝，2=微信。条件更新保证幂等，重复支付返回失败。

### 8.6 微信支付下单

```
POST /api/user/order/wx-pay/{orderNo}
Authorization: Bearer <token>
```

返回微信支付 V3 预支付参数（未配置商户参数时返回失败）。

### 8.7 支付结果查询

```
GET /api/user/order/pay-result/{orderNo}
Authorization: Bearer <token>
```

返回 `{ orderNo, status, statusDesc, paid }`，支付后前端轮询使用。

### 8.8 确认收货

```
PUT /api/user/order/receipt/{orderNo}
Authorization: Bearer <token>
```

仅已发货（status=4）订单可确认，确认后变为已完成（status=3）。

### 8.9 商家发货（商家角色）

```
PUT /api/merchant/order/ship/{orderNo}
Authorization: Bearer <token>
```

仅本店铺已支付（status=1）订单可发货，发货后变为已发货（status=4）。

---

## 九、管理员模块 `/admin`

> 以下接口需要管理员角色（role=3）。

### 9.1 用户列表

```
GET /api/admin/user/list?pageNum=1&pageSize=10&phone=138&role=1&status=1
Authorization: Bearer <token>
```

### 9.2 启用 / 禁用用户

```
PUT /api/admin/user/status?userId=1001&status=0
Authorization: Bearer <token>
```

`status`: 1=启用，0=禁用。不能禁用自己。

### 9.3 修改用户角色

```
PUT /api/admin/user/role
Authorization: Bearer <token>
Content-Type: application/json

{ "userId": 1001, "role": 2 }
```

`role`: 0=游客，1=用户，2=商家，3=管理员。

### 9.4 待审核商家列表

```
GET /api/admin/merchant/pending?pageNum=1&pageSize=10
Authorization: Bearer <token>
```

### 9.5 审核商家

```
PUT /api/admin/merchant/audit
Authorization: Bearer <token>
Content-Type: application/json

{ "merchantId": 101, "auditStatus": 1, "auditRemark": "审核通过" }
```

`auditStatus`: 1=通过（升级用户角色为商家），2=驳回（必须填写 `auditRemark`）。

### 9.6 全部商家列表

```
GET /api/admin/merchant/list?pageNum=1&pageSize=10&shopName=好物&auditStatus=1
Authorization: Bearer <token>
```

### 9.7 全平台商品列表

```
GET /api/admin/product/list?pageNum=1&pageSize=10&name=手机&merchantId=101&status=1
Authorization: Bearer <token>
```

### 9.8 强制下架商品

```
PUT /api/admin/product/off-shelf?productId=10001
Authorization: Bearer <token>
```

### 9.9 重新上架商品

```
PUT /api/admin/product/on-shelf?productId=10001
Authorization: Bearer <token>
```

### 9.10 强制删除商品

```
DELETE /api/admin/product/delete?productId=10001
Authorization: Bearer <token>
```

### 9.11 全平台订单列表

```
GET /api/admin/order/list?pageNum=1&pageSize=10&orderNo=GM&status=1&merchantId=101
Authorization: Bearer <token>
```

### 9.12 处理异常订单

```
PUT /api/admin/order/handle?orderNo=GM20260616000001&targetStatus=3
Authorization: Bearer <token>
```

`targetStatus`: 2=取消（回滚库存），3=完成。

### 9.13 运营统计

```
GET /api/admin/statistics
Authorization: Bearer <token>
```

返回 12 项指标：`totalOrders, todayOrders, pendingPaymentOrders, pendingDeliveryOrders, totalAmount, todayAmount, totalUsers, todayUsers, totalMerchants, pendingAuditMerchants, totalProducts, todayProducts`。

---

## 十、通用上传 `/upload`

### 10.1 上传图片（需登录）

```
POST /api/upload/image
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <图片文件>
```

支持 jpg/jpeg/png/gif/webp，单文件 ≤ 10MB（application.yml 可调）。返回可访问的 URL：

```json
{ "code": 200, "data": { "url": "/api/images/upload/202609/3f2a...png" } }
```

文件保存在服务端 `uploads/images/yyyyMM/` 目录（`app.upload-dir` 可配置）。

---

## 状态码说明

| HTTP 状态码 | 业务 code | 说明 |
|------------|-----------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 参数校验失败 |
| 401 | 401 | 未登录或 token 过期 |
| 403 | 403 | 权限不足（角色不符） |
| 404 | 404 | 资源不存在 |
| 405 | 405 | 请求方法不支持 |
| 500 | 500 | 服务器内部错误 |

---

## 数据库表结构

| 表名 | 说明 |
|------|------|
| user | 用户表（id, username, password, nickname, phone, role, status） |
| merchant | 商家表（id, user_id, shop_name, audit_status） |
| product | 商品表（id, merchant_id, name, price, stock, sales, status） |
| category | 分类表（id, name, parent_id） |
| cart | 购物车表（user_id, product_id, quantity） |
| product_collect | 收藏表（user_id, product_id） |
| order | 订单表（id, order_no, user_id, merchant_id, status） |
| order_item | 订单明细表（order_id, product_id, unit_price, quantity） |
