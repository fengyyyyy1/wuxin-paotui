# 更新日志

## V0.5（开发中）

日期：2026-07-16

### 新增

- 用户取消订单接口：`POST /api/order/cancel/{id}`
- 待接单订单支持由发布用户取消，状态从 `0` 原子更新为 `5`
- 取消成功后写入 `order_log`
- 新增订单状态异常：`409 当前订单状态不可取消`

### 安全与一致性

- 订单归属从 `UserContext` 获取，不接收前端 `userId`
- 使用带用户、状态和逻辑删除条件的数据库更新，防止重复取消
- 订单取消与日志写入处于同一事务
- 本版本未修改数据库表结构、SQL 脚本和 Maven 依赖

## V0.4

日期：2026-07-13

### 新增

- 我的订单接口：`GET /api/order/my`
- 订单详情接口：`GET /api/order/{id}`
- BusinessException 业务异常规范
- GlobalExceptionHandler 统一异常处理
- OrderStatusEnum 订单状态枚举
- 骑手大厅接口：`GET /api/rider/order/hall`
- 骑手接单接口：`POST /api/rider/order/accept/{id}`
- 骑手我的订单接口：`GET /api/rider/order/my`
- 骑手完成配送接口：`POST /api/rider/order/finish/{id}`
- 用户确认收货接口：`POST /api/order/confirm/{id}`
- 订单日志写入：骑手接单成功后写入 `order_log`
- 订单日志写入：骑手完成配送成功后写入 `order_log`
- 订单日志写入：用户确认收货成功后写入 `order_log`
- 数据库升级：`order_info.accept_time`、`order_info.finish_time`
- 订单状态定义修正：`3` 调整为待确认收货，`4` 调整为已完成，`5` 调整为已取消。

### 修复

- 修复 `RiderInfoEntity` 字段映射错误。
- 删除 `RiderInfoEntity` 中不存在的 `status`、`deleted`。
- 修复 `order_log` 缺少 `operator_type` 的问题。
- 精简 SQL 升级脚本，当前只升级 `order_info`。
- 修复 BusinessException 返回 500 的问题。

### 状态码

| code | 说明 |
| --- | --- |
| 403 | 当前用户不是骑手 |
| 404 | 订单不存在 |
| 409 | 订单状态冲突 |
| 500 | 服务器内部错误 |

## V0.3

日期：2026-07-10

### 新增

- 用户注册
- 用户登录
- JWT 登录认证
- BCrypt 密码加密
- 地址管理
- 发布订单
- Result 统一返回
- ResultCode 状态码
- Validation 参数校验
