# 更新日志

## V0.8 Completed

日期：2026-07-17

### 新增

- 商家商品分类新增、修改、启禁用、逻辑删除和列表接口
- 商家商品新增、修改、上下架、逻辑删除和分页列表接口
- 公开店铺分类列表、商品列表和商品详情接口
- `merchant_category` 商品分类表和 `merchant_product` 商品表
- `CategoryStatusEnum` 和 `ProductStatusEnum`
- 商品分类、商品及库存相关业务异常码

### 安全与一致性

- 分类和商品管理统一从 `UserContext` 获取当前用户，不接收前端店铺或商家 ID
- 管理操作统一校验商家审核状态、商家状态、店铺状态和逻辑删除状态
- 所有修改和删除条件包含记录 ID、当前店铺 ID 与未删除条件
- 分类重名由 Service 预检和数据库唯一索引共同保障
- 公开接口仅返回启用分类下已上架、未删除且有库存的商品
- 新公开路由仅按 GET 方法精确放行

### 验收

- 商品分类管理接口人工测试通过
- 商品管理及上下架接口人工测试通过
- 公开分类、商品列表和商品详情接口人工测试通过
- `merchant_category`、`merchant_product` 表结构及索引经 Navicat 验证通过
- V0.8 SQL、Postman、Navicat 验证全部完成

## V0.7（开发中）

日期：2026-07-16

### 新增

- 商家申请入驻接口：`POST /api/merchant/apply`
- 我的商家资料接口：`GET /api/merchant/me`
- 店铺资料修改接口：`PUT /api/merchant/store`
- 营业状态修改接口：`PUT /api/merchant/store/business-status`
- 公开店铺列表：`GET /api/store/list`
- 公开店铺详情：`GET /api/store/{id}`
- `merchant_info` 商家主体表和 `merchant_store` 店铺表
- 商家审核、商家启用和店铺营业状态枚举

### 安全与一致性

- 商家复用现有 `sys_user`、JWT 和 `UserContext`
- 商家主体与店铺创建处于同一事务
- `user_id` 唯一索引和异常转换防止并发重复申请
- 店铺管理严格校验商家归属、审核和启用状态
- 公开路由仅按 GET 方法放行店铺列表和数字 ID 详情
- 公开店铺列表使用数据库联表分页

## V0.6（开发中）

日期：2026-07-16

### 新增

- 订单模拟支付接口：`POST /api/order/pay/{id}`
- 订单轨迹接口：`GET /api/order/timeline/{id}`
- 新增 `PaymentStatusEnum`：`0 未支付`、`1 已支付`
- `order_info` 新增 `pay_status`、`pay_time`、`payment_no`
- 新增支付状态查询索引和支付单号唯一索引
- 支付成功后写入 `0 → 0` 的订单日志
- 用户订单列表和详情返回支付状态信息
- 订单轨迹整合 `order_info`、`order_log`、`order_comment` 的真实时间数据
- 轨迹按时间升序排列并重新编号，不返回空时间节点

### 调整

- 新订单默认未支付，配送业务状态仍为待接单
- 骑手大厅只展示已支付待接单订单
- 骑手接单原子更新增加已支付条件
- 未支付订单直接接单返回 `409 订单未支付`

### 安全与一致性

- 模拟支付使用数据库原子条件更新，防止并发重复支付
- 支付单号采用时间戳、六位随机数、唯一索引和有限重试保障唯一
- 支付更新和订单日志写入处于同一事务
- 数据库升级只通过手动执行增量 SQL 完成

## V0.5（开发中）

日期：2026-07-16

### 新增

- 用户取消订单接口：`POST /api/order/cancel/{id}`
- 骑手放弃订单接口：`POST /api/rider/order/give-up/{id}`
- 用户评价订单接口：`POST /api/order/comment`
- 新增订单评价表 `order_comment` 和升级脚本 `05_create_order_comment.sql`
- 待接单订单支持由发布用户取消，状态从 `0` 原子更新为 `5`
- 已接单订单支持由原接单骑手放弃，状态从 `1` 原子回退为 `0`
- 取消成功后写入 `order_log`
- 放弃成功后清空 `rider_id`、`accept_time` 并写入 `order_log`
- 新增订单状态异常：`409 当前订单状态不可取消`
- 新增订单状态异常：`409 当前订单状态不可放弃`
- 新增评价状态异常：`409 当前订单状态不可评价`、`409 订单已评价`
- 评价成功后写入 `order_log`，状态记录为 `4 → 4`

### 安全与一致性

- 订单归属从 `UserContext` 获取，不接收前端 `userId`
- 使用带用户、状态和逻辑删除条件的数据库更新，防止重复取消
- 订单取消与日志写入处于同一事务
- 骑手放弃与日志写入处于同一事务，重复放弃不会重复写日志
- 评价与订单日志写入处于同一事务
- 通过 `order_id` 唯一索引保证一个订单只能评价一次
- Maven 依赖未修改

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
