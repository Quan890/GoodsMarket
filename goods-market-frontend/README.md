# 好物集市 — 前端项目

> 基于 Vue 3 + Element Plus + Pinia + Vue Router 的全栈电商前端

---

## 一、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 渐进式 JavaScript 框架 |
| Vite | 6.0+ | 下一代前端构建工具 |
| Vue Router | 4.5+ | 路由管理 |
| Pinia | 2.3+ | 状态管理 |
| Element Plus | 2.9+ | UI 组件库 |
| Axios | 1.7+ | HTTP 请求库 |
| Sass | 1.83+ | CSS 预处理器 |
| NProgress | 0.2+ | 路由切换进度条 |

---

## 二、环境要求

- **Node.js**：>= 18.0.0（推荐 20 LTS）
- **npm**：>= 9.0.0（随 Node.js 自带）
- **操作系统**：Windows / macOS / Linux 均可

验证版本：

```bash
node -v    # v18.x.x 或 v20.x.x
npm -v     # 9.x.x 或 10.x.x
```

---

## 三、项目启动

### 1. 安装依赖

```bash
cd goods-market-frontend
npm install
```

> 如遇网络问题，可使用淘宝镜像：
> ```bash
> npm install --registry=https://registry.npmmirror.com
> ```

### 2. 启动开发服务器

```bash
npm run dev
```

启动成功后浏览器自动打开 **http://localhost:5173**

> **前提**：后端 SpringBoot 服务需同时运行在 **http://localhost:8080**
> 开发环境通过 Vite proxy 将 `/api` 请求转发到后端

### 3. 打包构建

```bash
npm run build
```

构建产物输出到 `dist/` 目录，用于部署到 Nginx 或其他 Web 服务器。

### 4. 预览构建产物

```bash
npm run preview
```

---

## 四、后端接口地址配置

### 开发环境

文件：`.env.development`

```properties
VITE_API_BASE_URL=/api
```

开发环境通过 `vite.config.js` 中的 proxy 代理：

```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // ← 修改此处指向后端地址
      changeOrigin: true,
    }
  }
}
```

### 生产环境

文件：`.env.production`

```properties
VITE_API_BASE_URL=/api
```

生产环境由 Nginx 反向代理 `/api` 到后端服务，Nginx 配置示例：

```nginx
server {
    listen       80;
    server_name  your-domain.com;

    # 前端静态资源
    location / {
        root   /usr/share/nginx/html/dist;
        index  index.html;
        try_files $uri $uri/ /index.html;  # Vue History 模式必须
    }

    # 后端接口代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 五、项目结构

```
goods-market-frontend/
├── public/                          # 静态资源
├── src/
│   ├── api/                         # 接口请求模块（7 个，40+ 接口）
│   │   ├── user.js                  #   用户（登录/注册/信息）
│   │   ├── product.js               #   商品（列表/详情）
│   │   ├── collect.js               #   收藏（收藏/取消/列表）
│   │   ├── cart.js                  #   购物车（增删改查）
│   │   ├── order.js                 #   订单（创建/支付/列表）
│   │   ├── merchant.js              #   商家（入驻/商品管理/订单）
│   │   └── admin.js                 #   管理员（用户/商家/商品/订单/统计）
│   ├── assets/styles/               # 全局样式
│   │   └── index.scss
│   ├── router/index.js              # 路由配置 + 全局守卫（5 组 23 条路由）
│   ├── stores/                      # Pinia 状态仓库
│   │   ├── index.js                 #   统一导出入口
│   │   ├── user.js                  #   用户状态（token/角色/登录态）
│   │   └── cart.js                  #   购物车状态（列表/缓存/计算）
│   ├── utils/                       # 工具函数
│   │   ├── request.js               #   Axios 封装（拦截器/错误处理/401跳转）
│   │   └── common.js                #   通用工具（手机号校验/倒计时/金额格式化）
│   ├── views/                       # 页面组件（20+ 页面）
│   │   ├── home/                    #   首页 + 商品详情
│   │   ├── login/                   #   登录页
│   │   ├── user/                    #   用户中心（购物车/收藏/订单/结算/支付）
│   │   ├── merchant/                #   商家后台（入驻/商品/订单）
│   │   ├── admin/                   #   管理员后台（看板/用户/商家/商品/订单）
│   │   └── error/                   #   404 页面
│   ├── App.vue                      # 根组件（全局顶栏 + 路由出口）
│   └── main.js                      # 入口文件（Pinia/Router/ElementPlus/NProgress）
├── .env.development                 # 开发环境变量
├── .env.production                  # 生产环境变量
├── index.html                       # HTML 模板
├── package.json                     # 依赖配置
├── vite.config.js                   # Vite 构建配置
└── README.md                        # 本文档
```

---

## 六、功能测试流程

按以下顺序可完整测试全部功能：

### 第一阶段：游客浏览（无需登录）

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 访问 `http://localhost:5173` | 首页展示商品卡片列表 |
| 2 | 在搜索框输入关键词后回车 | 商品列表按关键词过滤 |
| 3 | 点击商品卡片 | 跳转商品详情页 |
| 4 | 观察详情页按钮区域 | 游客看不到收藏/加购按钮，显示"登录后购买" |
| 5 | 访问 `/user/cart` | 路由守卫拦截，自动跳转登录页 |

### 第二阶段：用户登录

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 6 | 点击顶栏"登录" | 进入登录页（渐变背景卡片） |
| 7 | 输入非法手机号（如 `123`） | 表单校验提示"手机号格式不正确" |
| 8 | 输入合法手机号，点击"获取验证码" | 按钮变为倒计时 `60s 后重新获取` |
| 9 | 输入验证码，点击登录 | 登录成功，跳转个人中心，顶栏显示用户信息 |
| 10 | 已登录状态访问 `/login` | 自动重定向到个人中心 |

### 第三阶段：购物下单全流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 11 | 回到首页，点击商品的"收藏"按钮 | 星标变为"已收藏"，弹出成功提示 |
| 12 | 点击"加入购物车" | 弹出成功提示，顶栏购物车角标 +1 |
| 13 | 进入购物车页面 | 显示已添加的商品，可修改数量 |
| 14 | 勾选商品，点击"去结算" | 跳转确认订单页 |
| 15 | 确认订单页点击"提交订单" | 创建成功，跳转收银台 |
| 16 | 收银台选择"模拟支付"，确认 | 支付成功，跳转我的订单 |
| 17 | 在"我的订单"页切换状态标签 | 按状态筛选订单列表 |
| 18 | 进入"我的收藏"页 | 展示收藏商品，可取消收藏 |

### 第四阶段：商家操作

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 19 | 顶栏用户菜单 → 商家入驻 | 进入驻申请页（需管理员先在后台将用户角色改为商家） |
| 20 | 填写店铺信息，提交申请 | 显示"审核中"状态 |
| 21 | 管理员审核通过后，刷新页面 | 显示"已通过"，可进入商品管理 |
| 22 | 商品管理 → 新增商品 | 填写商品信息（名称/价格/库存/图片URL）→ 新增成功 |
| 23 | 在商品列表点击"下架" | 二次确认后状态变为"已下架" |
| 24 | 切换到"店铺订单"页 | 展示与当前商家商品相关的订单 |

### 第五阶段：管理员后台

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 25 | 以管理员账号登录，访问 `/admin` | 进入后台布局（深色侧边栏 + 内容区） |
| 26 | 数据看板 | 展示用户/商家/商品/订单统计卡片 |
| 27 | 用户管理 → 搜索 → 修改角色 | 弹窗选择角色，确认后生效 |
| 28 | 商家审核 → 待审核列表 → 通过/驳回 | 驳回需填写原因 |
| 29 | 商品管控 → 搜索 → 强制下架/删除 | 二次确认后执行 |
| 30 | 订单管理 → 筛选 → 处理异常订单 | 选择目标状态，确认后变更 |

---

## 七、角色与权限说明

| 角色 | role 值 | 可访问页面 |
|------|---------|-----------|
| 游客 | 0 | 首页、商品详情、登录页 |
| 用户 | 1 | 个人中心、购物车、收藏、订单、结算、支付 |
| 商家 | 2 | 用户全部页面 + 商家中心、商品管理、店铺订单 |
| 管理员 | 3 | 管理后台全部页面（看板、用户、商家、商品、订单） |

权限控制：
- **前端**：路由守卫 `beforeEach` 校验 `meta.role`，不足则弹窗提示并跳转
- **后端**：Sa-Token 注解校验，接口层二次拦截

---

## 八、常见问题

### Q1：启动报错 `Module not found`

```bash
rm -rf node_modules package-lock.json
npm install
```

### Q2：页面空白，控制台报 404

确认后端 SpringBoot 服务已启动在 `http://localhost:8080`，且接口路径前缀为 `/api`。

### Q3：打包后部署到 Nginx 刷新页面 404

Nginx 需配置 `try_files` 支持 Vue History 模式：

```nginx
try_files $uri $uri/ /index.html;
```

### Q4：如何修改后端地址

- 开发环境：修改 `vite.config.js` 中 `proxy.target`
- 生产环境：修改 `.env.production` 中 `VITE_API_BASE_URL`，并配置 Nginx 代理

---

## 九、可用脚本

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动开发服务器（热更新） |
| `npm run build` | 打包构建（输出到 `dist/`） |
| `npm run preview` | 本地预览构建产物 |

---

**项目名称**：好物集市（Goods Market）
**最后更新**：2026 年 6 月
