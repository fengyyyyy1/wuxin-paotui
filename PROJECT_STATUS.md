# 五鑫跑腿（Wuxin Paotui）项目开发状态

> 最后更新时间：2026-07-09  
> 当前版本：V0.2  
> 项目负责人：一 李  
> 技术负责人：ChatGPT  
> 开发实现：Codex  

---

## 一、项目目标

开发一个真正可上线运营的微信跑腿平台，而不是课程设计。

技术栈：

- Spring Boot 3.x
- Java 21
- MyBatis Plus
- MySQL 8
- Redis（后续）
- JWT
- 微信小程序
- Vue3（后台管理）
- Docker（后续部署）
- Nginx（后续部署）

---

## 二、开发进度

### 基础框架

- [x] Spring Boot
- [x] Maven
- [x] MyBatis Plus
- [x] MySQL
- [x] 项目启动

### 用户认证模块（100%）

- [x] 用户注册
- [x] 用户登录
- [x] BCrypt 密码加密
- [x] JWT Token
- [x] JWT 登录拦截器
- [x] UserContext(ThreadLocal)
- [x] 获取当前登录用户
- [x] Result
- [x] ResultCode
- [x] Validation
- [x] GlobalExceptionHandler
- [x] BusinessException

### 地址模块（100%）

已完成接口：

- POST /api/user/address
- GET /api/user/address/list
- DELETE /api/user/address/{id}

已完成功能：

- 新增地址
- 查询地址
- 删除地址（逻辑删除）
- 默认地址
- 权限控制（只能操作自己的地址）

---

## 三、数据库

当前数据库：

wuxin_paotui

已确认继续沿用现有数据库，不重新设计数据库。

主要业务表：

- sys_user
- user_address
- order_info
- order_log
- shop_info
- rider_info
- payment_record
- system_config
- system_log

---

## 四、当前接口

### 用户接口

- POST /api/user/register
- POST /api/user/login
- GET /api/user/list
- GET /api/user/me

### 地址接口

- POST /api/user/address
- GET /api/user/address/list
- DELETE /api/user/address/{id}

---

## 五、项目规范

已建立：

- BCrypt 密码加密
- JWT 登录认证
- JWT 登录拦截器
- ThreadLocal(UserContext)
- GlobalExceptionHandler
- Validation 参数校验
- Result 统一返回
- ResultCode 统一状态码
- DTO
- VO

---

## 六、待优化项

1. /api/user/list 不能返回 password，后续应返回 UserInfoVO。
2. 新增接口建议返回新增对象 ID，而不是字符串。
3. Entity 不应直接返回前端，后续统一使用 VO。
4. 逐步完善 API 返回规范。
5. 后续补充 CHANGELOG.md、API.md、DATABASE.md。

---

## 七、下一阶段开发

当前下一阶段：

V0.3 订单模块

预计开发顺序：

1. 发布订单
2. 我的订单
3. 查看订单详情
4. 骑手接单
5. 骑手完成订单
6. 用户确认完成
7. 订单日志
8. 订单状态流转

---

## 八、固定开发流程

以后固定采用：

1. ChatGPT 设计架构
2. ChatGPT 编写 Codex 指令
3. Codex 开发
4. Postman 测试
5. Navicat 数据验证
6. ChatGPT 验收
7. 更新 PROJECT_STATUS.md

---

## 九、三方分工

项目负责人：一 李

负责：

- 开发
- 测试
- 决策

技术负责人：ChatGPT

负责：

- 架构设计
- 数据库设计
- API 规范
- 代码审核
- Bug 分析
- 安全设计
- 开发路线规划

开发工程师：Codex

负责：

- Java 代码
- SQL
- 接口开发
- 重构

---

## 十、下次继续方式

下次回来直接说：

继续开发五鑫跑腿

或者：

读取 PROJECT_STATUS.md 继续

即可继续开发。
