# 更新日志

## V1.3 微信用户体系

日期：2026-07-18

### 新增

- `POST /api/user/wechat/login`
- `WeChatMiniProgramProperties`与`MockWeChatLoginProperties`
- 微信小程序网关抽象、路由和真实 code2session 实现
- 固定 code/openid 映射的本地 Mock 微信网关
- 微信用户自动注册、JWT 返回和 `newUser`标识
- openid 唯一索引冲突后的并发回查幂等处理
- `UserInfoVO`补充昵称和头像
- `GET /api/user/profile`
- `PUT /api/user/profile`
- `UpdateUserProfileDTO`及昵称、头像、性别校验
- `UserInfoVO`补充gender
- Profile只允许更新`nickname、avatar、gender`
- `POST /api/user/phone/bind`
- `BindWechatPhoneDTO`
- `WeChatPhoneGateway`、`WeChatPhoneGatewayRouter`和`WeChatPhoneResult`
- 固定 code/手机号映射的`MockWeChatPhoneGateway`
- 当前用户手机号绑定、重复绑定幂等和更换手机号
- 其他未删除用户手机号占用检查
- 手机号绑定服务和JWT拦截回归测试

### 安全

- AppSecret仅通过环境变量注入，所有网关默认关闭
- code不缓存，session_key不持久化、不返回、不记录
- 自动用户名不暴露完整 openid
- 自动用户密码使用随机 BCrypt 密文
- Mock登录在`prod` Profile中禁止使用
- JWT白名单仅新增精确路径`/api/user/wechat/login`
- 手机号绑定接口保持JWT保护，用户ID只从`UserContext`获取
- Mock手机号网关默认关闭，`prod`环境禁止使用
- 不接受客户端明文手机号，只信任网关返回结果
- 授权code不写入日志，手机号日志统一脱敏

### 修复

- 修复微信自动注册原始随机密码为73个UTF-8字节，超过BCrypt 72字节上限的问题
- 随机原始密码改为单个UUID，每个用户独立生成，固定为36个UTF-8字节
- BCrypt编码前增加UTF-8字节长度保护，生成异常转换为统一业务错误
- 新增首次Mock登录、重复登录、单用户插入和BCrypt密文回归测试

### 数据库

- 复用真实`sys_user.openid/unionid/nickname/avatar`
- Profile复用`nickname/avatar/gender/phone`，不修改数据库结构
- 手机号绑定复用`sys_user.phone`与普通索引`idx_phone`
- 复用`uk_openid`、`uk_username`和`idx_phone`
- 不新增SQL，不修改数据库结构

### 当前状态

- BCrypt 72字节问题已修复，单元回归测试通过
- Mock微信登录已通过人工验收
- Profile接口已通过Postman人工验收
- 微信手机号绑定已通过Postman与Navicat人工验收
- V1.3本轮人工验收全部完成
- 未接入真实微信支付
- 已有账号绑定、账号合并留待后续版本

### 人工验收

- 微信Mock登录：通过
- 微信Profile：通过
- 微信手机号绑定：通过
- Postman验证：通过
- Navicat验证：通过

## V1.2 微信支付模块（第一阶段）

日期：2026-07-18

### 新增

- `payment_order`支付流水表和`12_create_payment_order.sql`
- 支付流水状态枚举、Entity、Mapper及金额元转分工具
- `WeChatPayProperties`和`MockPaymentProperties`
- 支付网关抽象、路由器和无外部网络的`MockPaymentGateway`
- `POST /api/payment/wechat/jsapi`
- `POST /api/payment/mock/{paymentNo}/success`
- `GET /api/payment/order/{orderId}/status`
- 统一支付成功事务及重复确认幂等处理
- 官方`wechatpay-java:0.2.17`依赖

### 安全与兼容

- 新架构仅允许服务端可靠计价的商品订单
- 创建支付单不直接修改订单支付状态
- Mock接口和旧模拟支付接口均受环境开关保护
- 真实微信支付默认关闭，未创建SDK客户端、未读取私钥
- 微信回调第一阶段不注册，避免无验签请求误改订单
- 配置迁移为环境变量占位符，本地配置和密钥文件加入Git忽略
- 未修改订单履约状态语义，未实现退款

### 当前状态

- 第一阶段代码与文档完成，并通过人工 Mock 支付链路验收
- Maven Compile已通过
- SQL、Postman和Navicat验证已通过
- 未连接真实微信支付

### 人工验收

- Mock 支付创建成功
- 支付流水状态更新成功
- 订单支付状态同步成功
- 重复支付确认幂等验证通过
- 订单支付日志仅写入一次

## V1.1

日期：2026-07-17

### 新增

- 骑手今日、本周、本月和累计排行榜
- 骑手个人统计接口 `GET /api/rider/{riderId}/statistics`
- 排行榜 SQL 分组聚合和个人统计条件聚合优化
- 排行榜索引 `idx_order_status_deleted_finish_rider`
- `RiderRankingVO`、`RiderStatisticsVO`
- `RiderRankingService` 及其实现
- 幂等索引升级脚本 `11_add_rider_ranking_index.sql`

### 统计与安全

- 仅统计 `status = 4`、骑手 ID 非空且未逻辑删除的真实订单
- 今日、本周、本月统一使用 `finish_time` 左闭右开时间范围
- 排名按完成单量、最早完成时间、骑手 ID 稳定排序
- 排行榜名称优先使用骑手真实姓名，其次用户昵称，最后使用骑手 ID 兜底
- 两个接口继续受现有 JWT 拦截器保护
- 聚合查询不加载全部订单、不拼接用户 SQL、不产生 N+1 查询

### 当前状态

- V1.1 骑手跑单排行榜模块已完成
- Java 21 Maven Compile 已通过
- 索引 SQL 已人工执行并验证
- 今日、本周、本月、累计榜和骑手个人统计已通过人工测试
- 参数校验、limit 范围和 SQL 统计结果已通过人工验证

## V1.0 Completed

日期：2026-07-17

### 新增

- 新增购物车结算预览接口 `POST /api/order/settlement/preview`
- 新增购物车创建商品订单接口 `POST /api/order/create-from-cart`
- 新增 `order_item` 商品订单明细快照表
- 新增 `OrderTypeEnum`，区分跑腿订单和商品订单
- 商品订单详情返回店铺、金额和 `order_item` 快照信息
- `order_info` 兼容新增 `order_type`、`store_id`、`product_amount`、`delivery_fee`、`total_amount`
- 新增幂等数据库脚本 `10_create_order_item_and_update_order.sql`

### 业务与安全

- 预览和创建订单复用同一套地址、商品、分类、店铺、商家和库存校验
- 商品价格从 `merchant_product` 实时读取，创建后写入 `order_item` 快照
- 订单主表、库存扣减、明细快照、订单日志和购物车清理处于同一事务
- 库存通过带库存及业务状态条件的原子 SQL 扣减，防止超卖
- 创建成功后仅逻辑删除当前用户已选购物车项，未选中项继续保留
- 商品订单创建后保持未支付状态，不进入骑手大厅
- 商品订单详情仅允许订单所属用户查看，历史商品信息不依赖当前商品数据

### 当前状态

- Maven Compile 已通过
- SQL、Postman 和 Navicat 验证已通过
- 正常流程、异常流程和越权测试已通过
- V1.0 已完成

### V1.0 收尾

新增：

- 地址越权测试，确认其他用户不能使用不属于自己的收货地址
- 订单越权测试，确认其他用户不能查看不属于自己的订单
- 测试环境统一文档 `TEST_ENVIRONMENT.md`
- 测试账号统一管理规范
- V1.0 开发经验和版本收尾规范

修正：

- API 文档路径与 Controller 实际映射保持一致
- 地址接口路径统一为 `/api/user/address`
- 删除 API 文档中当前 Controller 不存在的修改地址接口
- 项目状态更新为 `V1.0 Completed`

## V0.9 Shopping Cart Completed

日期：2026-07-17

### 新增

- 购物车表 `shopping_cart` 和增量脚本 `09_create_shopping_cart.sql`
- 加入购物车、购物车列表、修改数量和选中状态接口
- 单个商品逻辑删除和清空购物车接口
- `CartItemVO`、`CartListVO` 实时购物车返回结构
- 购物车不存在、跨店铺、商品下架、库存不足和店铺停业异常码
- 购物车参数校验统一返回 `400`，不改变已有模块的 `1004` 兼容行为

### 业务与安全

- 当前用户统一从 `UserContext` 获取，不接收前端用户 ID
- 购物车只保存用户、店铺、商品、数量和选中状态，不保存商品快照
- 购物车列表实时关联商品、分类、店铺和商家状态
- 失效商品保留并返回 `invalidReason`，不计入选中总额
- 数据库用户行锁串行化同一用户写操作，保障单店铺约束和重复加购累加
- 同一商品删除后重新加购复用逻辑删除记录
- 所有购物车写操作处于事务中

### 数据库字段修正

- 商品分类启用状态统一使用 `merchant_category.status`
- 店铺营业状态统一使用 `merchant_store.business_status`

### 完成与验收

- 购物车六个接口测试全部通过
- 正常流程：加入、累加、查询、改数量、改选中、删除、重新加入、清空全部通过
- 异常流程：401、400、404、跨店铺、下架、禁用、停业、库存不足全部通过
- `shopping_cart` 表、唯一索引、普通索引及逻辑删除经 Navicat 验证通过
- V0.9 正式标记为 Completed

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
