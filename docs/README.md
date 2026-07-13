# 五鑫跑腿（Wuxin Paotui）

## 项目简介

五鑫跑腿（Wuxin Paotui）是一套基于 Spring Boot 的企业级校园跑腿平台。

项目目标不是课程设计，而是按照真实互联网项目标准持续开发，最终完成部署上线。

采用前后端分离架构，后续将支持微信小程序、管理后台、微信支付、Redis 缓存、Docker 部署等能力。

## 项目特点

- 企业级项目结构
- Spring Boot 3
- MyBatis Plus
- JWT 登录认证
- BCrypt 密码加密
- 统一异常处理
- 统一返回结构
- VO / DTO 分层
- MyBatis Plus 分页
- 数据库增量升级
- Postman 接口测试
- 完整开发文档

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Spring Boot、Java 21、MyBatis Plus |
| 数据库 | MySQL 8 |
| 认证 | JWT、BCrypt |
| 构建 | Maven |
| 规划 | Redis、Vue3 Admin、微信小程序、Docker、Nginx、OSS、微信支付 |

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
- 订单状态枚举
- 统一异常处理

### 骑手模块

- 骑手大厅
- 骑手接单
- 骑手我的订单

## 当前开发进度

| 项 | 状态 |
| --- | --- |
| 当前版本 | V0.4 |
| 开发状态 | 持续开发中 |
| 用户模块 | 已完成 |
| 地址模块 | 已完成 |
| 订单模块 | 部分完成 |
| 骑手模块 | 部分完成 |

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
└── sql           # 数据库升级脚本
```

## 快速启动

### 环境要求

- JDK 21
- MySQL 8
- Maven

### 启动步骤

1. 创建数据库 `wuxin_paotui`。
2. 修改 `application.yml` 或 `application.properties` 中的数据库连接配置。
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
| DEVELOPMENT_RULE.md | 开发规范 |

## 开发规范

- Controller 不写业务。
- Service 负责业务。
- Mapper 负责数据库。
- Entity 不直接返回前端。
- 统一使用 DTO 接收请求参数。
- 统一使用 VO 返回前端数据。
- 统一使用 BusinessException 处理业务异常。
- 统一使用 Result 返回接口数据。

## 后续开发计划

- 骑手完成配送
- 用户确认收货
- 取消订单
- 评价
- 支付
- Redis
- OSS
- Docker
- Nginx
- 微信小程序
- 后台管理

## License

仅用于学习与企业项目实践。
