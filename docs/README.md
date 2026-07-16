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
- 订单状态枚举
- 统一异常处理

### 骑手模块

- 骑手大厅
- 骑手接单
- 骑手我的订单
- 骑手完成配送
- 骑手放弃订单

## 当前开发进度

| 项 | 状态 |
| --- | --- |
| 当前版本 | V0.5（开发中） |
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

## 后续开发计划

- 支付
- Redis
- OSS
- Docker
- Nginx
- 微信小程序
- 后台管理

## License

仅用于学习与企业项目实践。
