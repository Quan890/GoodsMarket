# 好物集市（Goods Market）

一个全栈电商系统，包含用户端、商家端和管理后台。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21 + Spring Boot 4.1.0 + MyBatis-Plus 3.5.11 |
| 前端 | Vue 3.5 + Vite 6 + Element Plus 2.9 + Pinia 2.3 |
| 数据库 | MySQL 8.x + Redis |
| 认证 | Sa-Token 1.42.0（JWT + Redis Session） |
| 支付 | 微信支付 V3 SDK |
| 短信 | 阿里云短信 SDK |
| API 文档 | Knife4j 4.5.0（OpenAPI 3） |

## 环境要求

| 工具 | 版本要求 |
|------|---------|
| JDK | 21+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Maven | 3.8+ |

## 快速启动

### 1. 克隆项目

```bash
git clone <仓库地址>
cd GoodsMarket
```

### 2. 初始化数据库

```bash
# 登录 MySQL，依次执行建表和导入数据
mysql -u root -p < sql/goods_market_db.sql
mysql -u root -p goods_market_db < sql/seed_data.sql
```

### 3. 启动 Redis

```bash
# Windows（确保 Redis 服务已启动）
redis-server

# 或确认 Redis 已在后台运行
redis-cli ping
# 返回 PONG 即正常
```

### 4. 启动后端

```bash
cd goods-market-backend

# 修改数据库和 Redis 连接信息（如需要）
# vim src/main/resources/application.yml

mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，API 前缀 `/api`。

### 5. 启动前端

```bash
cd goods-market-frontend

npm install
npm run dev
```

前端启动在 `http://localhost:5173`，自动代理 `/api` 到后端。

### 6. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | 前端页面 |
| http://localhost:8080/doc.html | API 文档（Knife4j） |

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| merchant_digital | 123456 | 商家 |
| merchant_fashion | 123456 | 商家 |
| merchant_snack | 123456 | 商家 |
| user_li | 123456 | 普通用户 |

## 项目结构

```
GoodsMarket/
├── sql/                          # 数据库脚本
│   ├── goods_market_db.sql       # 建库建表
│   └── seed_data.sql             # 测试数据
├── goods-market-backend/         # 后端 Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/market/goods/
│       ├── config/               # 配置类（Sa-Token、Jackson、跨域）
│       ├── controller/           # 控制器（8 个模块）
│       ├── service/              # 业务接口
│       ├── service/impl/         # 业务实现
│       ├── mapper/               # MyBatis Mapper 接口
│       ├── entity/               # 数据库实体
│       ├── dto/                  # 请求参数对象
│       ├── vo/                   # 响应视图对象
│       ├── enums/                # 枚举（角色、订单状态、审核状态）
│       ├── exception/            # 异常处理
│       └── util/                 # 工具类（分页、验证码、短信、支付）
├── goods-market-frontend/        # 前端 Vue 3
│   ├── package.json
│   └── src/
│       ├── api/                  # 接口封装（axios）
│       ├── assets/               # 静态资源、样式
│       ├── router/               # 路由 + 权限守卫
│       ├── stores/               # Pinia 状态管理
│       ├── utils/                # 工具（请求封装、通用函数）
│       └── views/                # 页面组件
│           ├── home/             # 首页、商品详情
│           ├── login/            # 登录、注册、找回密码
│           ├── user/             # 用户中心、购物车、收藏、订单
│           ├── merchant/         # 商家中心、商品管理、店铺订单
│           ├── admin/            # 管理后台（数据看板、用户/商家/商品/订单管理）
│           └── error/            # 404 页面
└── README.md
```

## 配置说明

### 数据库配置

`goods-market-backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/goods_market_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:        # 无密码留空
      database: 0
```

### 外部服务（可选）

| 服务 | 配置项 | 说明 |
|------|--------|------|
| 阿里云短信 | `aliyun.sms.*` | 需要 AccessKey，未配置时使用模拟模式（验证码打印到控制台） |
| 微信支付 | `wechat.pay.*` | 需要商户号和证书，未配置时支付功能不可用 |

## 角色权限

| 角色 | 编码 | 可访问模块 |
|------|------|-----------|
| 游客 | 0 | 公开页面（首页、商品详情） |
| 用户 | 1 | + 个人中心、购物车、收藏、订单 |
| 商家 | 2 | + 商家中心、商品管理、店铺订单 |
| 管理员 | 3 | + 管理后台（用户/商家/商品/订单管理、数据统计） |

## API 文档

启动后端后访问 Knife4j 文档：http://localhost:8080/doc.html

完整 API 列表见 [API.md](./API.md)。
