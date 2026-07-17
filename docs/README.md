# 五鑫跑腿（Wuxin Paotui）

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![MyBatis Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-blue)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![JWT](https://img.shields.io/badge/JWT-Authentication-red)
![Git](https://img.shields.io/badge/Git-Version_Control-black)

## 项目简介

五鑫跑腿（Wuxin Paotui）是一套基于 Spring Boot 构建的企业级跑腿服务平台。

项目采用前后端分离架构，围绕用户、骑手、订单、商家等核心业务进行设计，注重系统的可维护性、扩展性、安全性以及后续持续迭代能力。

项目严格按照真实企业开发流程进行开发，涵盖接口设计、数据库设计、权限认证、业务流程、接口文档、数据库文档、版本管理等完整的软件工程实践。

最终目标是完成一套具备实际运营能力的跑腿服务平台，并支持部署上线。

## 项目特点

- 企业级项目架构
- Spring Boot 3
- MyBatis Plus
- JWT 身份认证
- BCrypt 密码加密
- RESTful API
- MyBatis Plus 分页
- 统一异常处理
- 数据库增量升级
- Git 版本管理
- 完整开发文档体系
- 企业级开发流程

## 项目目标

打造一套高性能、高可用、可扩展、可持续迭代的企业级跑腿服务平台。

后续支持：

- 微信小程序
- 管理后台
- Redis
- OSS
- Docker
- Nginx
- 微信支付
- 腾讯云部署

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Spring Boot、Java 21、MyBatis Plus |
| 数据库 | MySQL 8 |
| 认证 | JWT、BCrypt |
| 构建 | Maven |
| 规划 | Redis、Vue3 Admin、微信小程序、Docker、Nginx、OSS、微信支付、腾讯云部署 |

## 已完成模块

### 用户模块

- 用户注册
- 用户登录
- JWT 登录认证
- BCrypt 密码加密
- 获取当前登录用户

### 地址模块

- 新增地址
- 删除地址
- 查询地址
- 默认地址

### 订单模块

- 发布订单
- 我的订单
- 查看订单详情
- 用户确认收货
- 用户取消订单
- 用户评价订单
- 订单模拟支付
- 订单轨迹
- 订单状态枚举
- 统一异常处理

### 骑手模块

- 骑手大厅
- 骑手接单
- 骑手我的订单
- 骑手完成配送
- 骑手放弃订单
- 今日、本周、本月和累计跑单排行榜
- 骑手个人跑单统计

### 商家与店铺基础模块

- 商家申请入驻
- 我的商家资料
- 修改店铺资料
- 修改营业状态
- 公开店铺列表
- 公开店铺详情
- 商品分类管理
- 商品管理
- 公开商品分类、商品列表和商品详情

### 购物车模块

- 加入购物车与重复商品数量累加
- 单店铺购物车约束
- 实时商品信息与失效原因
- 修改数量和选中状态
- 逻辑删除和清空购物车

### 订单结算模块

- 购物车结算预览
- 商品订单事务创建
- 商品库存原子扣减
- `order_item` 商品信息快照
- 商品订单详情兼容
- 仅清理已选购物车商品

### 支付模块（V1.2 第一阶段）

- `payment_order`支付流水
- JSAPI支付单创建接口
- 支付状态查询
- 本地Mock支付网关
- 模拟支付成功确认
- 支付成功事务与幂等处理
- 旧模拟支付生产环境保护
- 微信支付API v3官方SDK依赖

当前阶段仅用于本地Mock联调，尚未连接真实微信支付。

## 当前开发进度

| 项 | 状态 |
| --- | --- |
| 当前版本 | V1.2 微信支付模块（第一阶段） |
| 开发状态 | 基础架构与Mock联调代码完成，等待人工SQL、Postman和Navicat验收 |
| 用户模块 | 已完成 |
| 地址模块 | 已完成 |
| 订单模块 | 部分完成 |
| 骑手模块 | 部分完成 |
| 商家与店铺基础模块 | 已完成 |
| 商品分类与商品管理 | 已完成 |
| V0.8 人工测试 | SQL、Postman、Navicat 全部通过 |
| 购物车模块 | Completed |
| V0.9 正常流程测试 | 全部通过 |
| V0.9 异常流程测试 | 全部通过 |
| 购物车结算与订单快照 | 已完成并通过人工测试 |
| 骑手跑单排行榜 | 已完成并通过人工测试 |
| 支付基础架构与Mock联调 | 代码完成，待人工验收 |

## 项目目录

```text
src/main/java
├── controller    # 接口入口
├── service       # 业务接口
├── service/impl  # 业务实现
├── mapper        # 数据库访问
├── entity        # 数据库实体
├── dto           # 请求参数对象
├── vo            # 前端返回对象
├── config        # 项目配置
├── interceptor   # 登录拦截器
└── utils         # 工具类

src/main/resources
├── application.yml / application.properties
├── application-local.example.yml
└── sql           # 数据库升级脚本
```

## 快速启动

### 环境要求

- JDK 21
- MySQL 8
- Maven

### 启动步骤

1. 创建数据库 `wuxin_paotui`。
2. 保留现有本地数据库配置；支付配置通过环境变量注入，可参考`application-local.example.yml`创建不跟踪的`application-local.yml`。
3. 导入数据库初始化脚本和增量 SQL。
4. 运行启动类：

```text
WuxinPaotuiServerApplication
```

## 项目文档

| 文档 | 说明 |
| --- | --- |
| README.md | 项目首页 |
| PROJECT_STATUS.md | 项目状态与当前断点 |
| API.md | 接口文档 |
| DATABASE.md | 数据库文档 |
| CHANGELOG.md | 更新日志 |
| POSTMAN.md | Postman 测试说明 |
| TEST_ACCOUNT.md | 测试账号 |
| TEST_ENVIRONMENT.md | 统一测试环境、测试数据和验收记录 |
| DEVELOPMENT_RULE.md | 开发规范 |
| CODEX_RULE.md | 项目协作规范 |

## 开发规范

- Controller 不写业务。
- Service 负责业务。
- Mapper 负责数据库。
- Entity 不直接返回前端。
- 统一使用 DTO 接收请求参数。
- 统一使用 VO 返回前端数据。
- 统一使用 BusinessException 处理业务异常。
- 统一使用 Result 返回接口数据。
- 所有接口路径必须以 Controller 实际映射为准。
- 所有数据库字段必须以真实数据库结构为准。
- 代码、接口、数据库、测试环境发生变化时必须同步更新文档。
- 测试账号统一维护在 `TEST_ACCOUNT.md`。
- 测试数据和环境统一维护在 `TEST_ENVIRONMENT.md`。

每完成一个版本必须更新：

1. `README.md`
2. `API.md`
3. `DATABASE.md`
4. `PROJECT_STATUS.md`
5. `CHANGELOG.md`
6. `TEST_ACCOUNT.md`
7. `TEST_ENVIRONMENT.md`

## V1.0 开发经验

1. 历史聊天记录不能作为项目真实依据，必须重新核对当前代码、数据库和文档。
2. 所有接口必须以 Controller 实际映射为准。
3. 所有字段必须以真实数据库结构为准。
4. 测试账号和密码不能依赖记忆，必须维护统一文档。
5. 每完成一个稳定版本应立即提交 Git。
6. Git Tag 必须对应已经完成验收的稳定版本。

## 后续开发计划

- V1.2第二阶段：真实微信JSAPI下单
- 官方SDK回调验签、解密和主动查单
- 支付与退款规则
- 商家订单
- Redis
- OSS
- Docker
- Nginx
- 微信小程序
- 后台管理

## License

仅用于学习与企业项目实践。
