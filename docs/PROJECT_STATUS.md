# 五鑫跑腿（Wuxin Paotui）

项目开发状态

---

最后更新时间：2026-07-22

当前版本：V1.7 用户微信小程序

项目状态：V1.7 用户微信小程序开发中，V1.7-8C购物车前端布局彻底重构完成，等待人工复测。

包含：

- [x] 最小 RBAC 管理员权限
- [x] 商家申请分页与详情
- [x] 商家审核通过与拒绝
- [x] 商家启用与禁用
- [x] 审核原子更新与操作日志
- [x] 14号 SQL 人工执行
- [x] Postman 与 Navicat 人工验收
- [x] 创建`wuxin-admin-web`
- [x] Vue 3 + TypeScript + Vite基础工程
- [x] Axios、Token、路由守卫和后台基础页面
- [x] 管理员登录页面与真实登录接口联调
- [x] 商家管理分页列表与真实接口联调
- [x] 商家详情页面与真实接口联调
- [x] 商家审核与状态操作联调
- [x] 管理后台前端性能优化和代码整理
- [x] 前端 Element Plus 按需加载优化
- [x] 人工验收通过
- [x] 完整商家管理页面
- [x] 创建`wuxin-miniapp`
- [x] 微信原生小程序 + TypeScript 基础工程
- [x] Vant Weapp、ESLint、Prettier和npm依赖管理
- [x] 小程序请求层、Bearer Token注入和401处理
- [x] 微信登录、首页、地址、订单和个人中心页面骨架
- [x] V1.7第一阶段TypeScript、ESLint和构建检查通过
- [x] V1.7第二阶段微信登录页面与真实接口联调代码完成
- [x] Token、userInfo和newUser本地保存
- [x] 登录状态恢复、退出登录和受保护页面访问控制
- [x] V1.7第三阶段用户Profile展示与编辑完成
- [x] 首页和个人中心进入时刷新`GET /api/user/profile`
- [x] 编辑资料页支持修改昵称和头像URL
- [x] V1.7.3稳定性修复：目录导入、生命周期兼容、页面判空和本地默认头像
- [x] V1.7第四阶段微信手机号绑定完成
- [x] Profile绑定入口、手机号脱敏展示和绑定成功后资料同步
- [x] V1.7第五阶段地址列表、新增地址、删除地址和新增默认地址完成
- [x] 地址管理页面基础美化和个人中心入口完成
- [x] V1.7-5A地址编辑和设置默认地址后端接口补齐
- [x] 小程序地址编辑和设默认已改为真实接口调用
- [x] V1.7-6首页商家与服务入口完成
- [x] 首页接入真实Profile、默认地址、公开门店列表和门店详情接口
- [x] 首页Banner、核心服务入口和三个公益入口完成本地静态展示
- [x] 搜索占位页、公益说明页和门店详情只读页已注册路由
- [x] V1.7-7门店与商品列表/商品详情完成
- [x] 门店详情页接入真实分类、商品列表和商品详情接口
- [x] 搜索页接入真实门店关键词搜索
- [x] V1.7-7A商品数据链路审计与测试数据修复完成
- [x] storeId=1商品分类、商品列表和商品详情公开链路已恢复
- [x] V1.7-7商品图片失败兜底和搜索栏遮挡二次修复完成
- [x] V1.7-8购物车闭环完成
- [x] V1.7-8A购物车交互重构完成
- [x] V1.7-8B购物车UI重构完成
- [x] V1.7-8C购物车前端布局彻底重构完成

---

## 一、项目简介

本项目是企业级跑腿服务平台。

项目采用 Spring Boot + MyBatis Plus + MySQL 技术栈，按照真实互联网项目标准持续开发。

最终目标：完成微信小程序上线运营。

## 二、当前完成模块

### 用户模块

- [x] 用户注册
- [x] 用户登录
- [x] BCrypt
- [x] JWT
- [x] 获取当前用户
- [x] 微信小程序 code 登录
- [x] 微信用户自动注册
- [x] 本地 Mock 微信登录
- [x] 获取用户Profile
- [x] 修改昵称、头像和性别
- [x] 微信手机号授权code绑定
- [x] 本地Mock手机号网关

### 地址模块

- [x] 新增地址
- [x] 修改地址
- [x] 删除地址
- [x] 地址列表
- [x] 默认地址
- [x] 地址权限控制

### 订单模块

- [x] 发布订单
- [x] 我的订单
- [x] 查看订单详情
- [x] OrderStatusEnum
- [x] BusinessException
- [x] GlobalExceptionHandler
- [x] 用户确认收货
- [x] 用户取消订单
- [x] 用户评价订单
- [x] PaymentStatusEnum
- [x] 订单模拟支付
- [x] 订单轨迹

### 骑手模块

- [x] 骑手大厅
- [x] 骑手接单
- [x] 骑手我的订单
- [x] 骑手完成配送
- [x] 骑手放弃订单
- [x] 今日、本周、本月和累计跑单排行榜
- [x] 骑手个人跑单统计

### 商家与店铺模块

- [x] 商家申请入驻
- [x] 我的商家资料
- [x] 修改店铺资料
- [x] 修改营业状态
- [x] 公开店铺列表
- [x] 公开店铺详情
- [x] 商品分类管理
- [x] 商品管理
- [x] 公开商品分类
- [x] 公开商品列表
- [x] 公开商品详情
- [x] 商家订单分页与详情
- [x] 商家接单
- [x] 商家拒单并标记待退款
- [x] 商家出餐
- [x] 商品订单骑手大厅准入控制

### 总控端模块

- [x] 管理员 RBAC 权限校验
- [x] 商家申请分页与详情
- [x] 商家审核通过与拒绝
- [x] 商家启用与禁用
- [x] 商家审核日志

### 购物车模块

- [x] 加入购物车
- [x] 查询购物车
- [x] 修改数量
- [x] 修改选中状态
- [x] 删除购物车商品
- [x] 清空购物车
- [x] 单店铺约束与失效商品处理

### 订单结算模块

- [x] 购物车结算预览
- [x] 商品订单事务创建
- [x] 商品库存原子扣减
- [x] `order_item` 商品快照
- [x] 商品订单详情兼容
- [x] V1.0 Postman 验证
- [x] V1.0 Navicat 验证

### 支付模块

- [x] payment_order支付流水模型
- [x] 支付流水状态枚举
- [x] 支付网关抽象与Mock网关
- [x] 创建JSAPI支付单
- [x] 查询支付状态
- [x] Mock支付成功确认
- [x] 支付成功事务与幂等
- [x] 旧模拟支付环境保护
- [x] 第一阶段SQL、Postman和Navicat人工验收
- [ ] 真实微信JSAPI下单
- [ ] 微信回调验签与解密
- [ ] 主动查单

### 微信小程序登录模块

- [x] code2session网关抽象
- [x] 真实微信网关
- [x] 固定映射Mock网关
- [x] openid自动注册和重复登录
- [x] 并发首次登录幂等
- [x] JWT返回与精确白名单
- [x] Postman验证
- [x] Navicat验证
- [ ] 真实微信环境联调

### 微信用户Profile模块

- [x] GET `/api/user/profile`
- [x] PUT `/api/user/profile`
- [x] 仅允许修改昵称、头像和性别
- [x] 用户ID仅从UserContext获取
- [x] Postman验证

### 微信手机号绑定模块

- [x] POST `/api/user/phone/bind`
- [x] 手机号网关抽象与路由
- [x] 固定code/手机号Mock映射
- [x] 当前用户、账号状态和手机号格式校验
- [x] 相同手机号幂等绑定
- [x] 更换手机号与其他用户占用检查
- [x] Mock默认关闭和生产环境保护
- [x] 脱敏日志和自动化测试
- [x] Postman与Navicat人工验证

## 三、数据库状态

数据库：

```text
wuxin_paotui
```

主要业务表：

- `sys_user`
- `user_address`
- `order_info`
- `order_log`
- `rider_info`
- `order_comment`
- `merchant_info`
- `merchant_store`
- `merchant_category`
- `merchant_product`
- `shopping_cart`
- `order_item`
- `payment_order`

数据库升级：

`order_info` 新增字段：

- `accept_time`
- `finish_time`

`order_info` 新增索引：

- `idx_order_rider_status_deleted`
- `idx_order_status_deleted_create_time`

V0.5 新增：

- `order_comment` 订单评价表
- `uk_order_comment_order_id` 订单唯一评价索引
- 升级脚本 `05_create_order_comment.sql`

V0.6 新增：

- `order_info.pay_status`
- `order_info.pay_time`
- `order_info.payment_no`
- `idx_order_pay_status_deleted_create_time`
- `uk_order_payment_no`
- 升级脚本 `06_update_order_payment.sql`

V0.7 新增：

- `merchant_info` 商家主体表
- `merchant_store` 店铺表
- 商家与用户、店铺与商家一对一唯一索引
- 升级脚本 `07_create_merchant_store.sql`

V0.8 新增：

- `merchant_category` 商品分类表
- `merchant_product` 商品表
- 店铺分类名称唯一索引及商品查询索引
- 升级脚本 `08_create_product_tables.sql`

V0.9 新增：

- `shopping_cart` 购物车表
- 用户商品唯一索引和用户店铺查询索引
- 升级脚本 `09_create_shopping_cart.sql`

V1.0 已升级：

- `order_info.order_type`
- `order_info.store_id`
- `order_info.product_amount`
- `order_info.delivery_fee`
- `order_info.total_amount`
- `order_item` 商品订单明细快照表
- `idx_order_type_user_deleted_create_time`
- `idx_order_store_status_deleted_create_time`
- 升级脚本 `10_create_order_item_and_update_order.sql`

V1.1 已完成升级：

- `idx_order_status_deleted_finish_rider`
- 升级脚本 `11_add_rider_ranking_index.sql`
- 不新增业务表和业务字段

V1.2第一阶段已完成升级：

- `payment_order`
- 有效支付单生成列`active_order_id`
- 支付号、交易号、通知号唯一索引
- 升级脚本`12_create_payment_order.sql`
- 已人工执行并通过支付流水、订单状态和日志幂等验证

## 四、当前测试数据

测试用户：

| 字段 | 值 |
| --- | --- |
| username | `test001` |
| password | `123456` |
| userId | `2` |

当前身份：

- 普通用户
- 骑手

当前订单：

| 订单 ID | 状态 |
| --- | --- |
| id=1 | 待接单 |
| id=2 | 已接单 |
| id=6 | 商品订单，Mock支付成功 |

V1.2支付验收：

| 字段 | 值 |
| --- | --- |
| orderId | `6` |
| paymentNo | `PAY20260718001905b85f724b684044e8a34221a040ab4ab9` |
| amountTotal | `200`分 |
| payment_order.status | `2 SUCCESS` |
| order_info.pay_status | `1 已支付` |

## 五、当前接口完成情况

### 用户

- [x] 注册
- [x] 登录
- [x] 微信小程序登录
- [x] 获取当前用户

### 地址

- [x] 新增
- [x] 删除
- [x] 修改
- [x] 查询

### 订单

- [x] 发布订单
- [x] 我的订单
- [x] 订单详情
- [x] 用户确认收货
- [x] 用户取消订单
- [x] 用户评价订单
- [x] 订单模拟支付
- [x] 订单轨迹

### 骑手

- [x] 大厅
- [x] 接单
- [x] 我的订单
- [x] 完成配送
- [x] 放弃订单
- [x] 跑单排行榜
- [x] 个人跑单统计

### 商家

- [x] 申请入驻
- [x] 我的商家资料
- [x] 修改店铺资料
- [x] 修改营业状态

### 店铺

- [x] 公开店铺列表
- [x] 公开店铺详情
- [x] 公开商品分类
- [x] 公开商品列表
- [x] 公开商品详情

### 商品管理

- [x] 分类新增、修改、状态、删除和列表
- [x] 商品新增、修改、上下架、删除和分页列表

### 购物车

- [x] 加入、列表、数量、选中、删除和清空
- [x] 实时商品数据、失效原因和选中金额统计

### 订单结算

- [x] 结算预览
- [x] 购物车创建商品订单
- [x] 商品订单详情快照

## 六、当前开发断点（最重要）

当前开发断点：

```text
V1.7-7 人工复测。
```

V1.5接口：

```http
GET /api/admin/merchant/page
GET /api/admin/merchant/{merchantId}
POST /api/admin/merchant/{merchantId}/approve
POST /api/admin/merchant/{merchantId}/reject
POST /api/admin/merchant/{merchantId}/enable
POST /api/admin/merchant/{merchantId}/disable
```

当前版本：

```text
V1.6 总控管理后台（已完成并通过人工验收）
```

后续完整版本顺序、功能边界和上线标准统一以`ROADMAP.md`为准。

## 七、待开发模块

- [x] 商品分类
- [x] 商品管理
- [x] 购物车
- [x] order_item
- [x] 购物车提交订单
- [x] V1.0 人工验收
- [x] 骑手跑单排行榜代码
- [x] V1.1 SQL、Postman 与 Navicat 人工验收
- [x] V1.2第一阶段支付代码
- [x] V1.2第一阶段人工验收
- [x] V1.3微信小程序登录代码
- [x] V1.3微信小程序登录人工验收
- [x] V1.3微信用户Profile代码
- [x] V1.3微信用户Profile人工验收
- [x] V1.3微信手机号绑定代码
- [x] V1.3微信手机号绑定人工验收
- [x] 商家订单代码
- [x] V1.4 商家订单SQL、Postman和Navicat人工验收
- [x] 总控端商家审核代码
- [x] V1.5 总控端SQL、Postman和Navicat人工验收
- [x] V1.6第一阶段前端基础架构
- [x] V1.6管理员登录真实接口联调
- [x] V1.6商家管理分页列表与真实接口联调
- [x] V1.6商家详情页面与真实接口联调
- [x] V1.6审核操作联调
- [x] V1.6前端性能优化和代码整理
- [x] V1.6商家管理完整页面
- [ ] V1.7 用户微信小程序开发
- [ ] Redis
- [ ] OSS
- [ ] Docker
- [ ] Nginx
- [ ] Vue Admin
- [ ] 微信小程序

## 八、本版本完成内容（V0.4）

- [x] 我的订单
- [x] 订单详情
- [x] BusinessException
- [x] 统一异常
- [x] OrderStatusEnum
- [x] 骑手大厅
- [x] 骑手接单
- [x] 骑手我的订单
- [x] 骑手完成配送
- [x] 用户确认收货
- [x] 订单日志
- [x] 数据库升级

## 九、V0.5 开发内容

- [x] 用户取消订单接口
- [x] 订单取消原子条件更新
- [x] 用户取消订单日志
- [x] 骑手放弃订单接口
- [x] 骑手放弃原子条件更新
- [x] 骑手放弃订单日志
- [x] 用户评价订单接口
- [x] `order_comment` 增量建表脚本
- [x] 一单一评唯一约束
- [x] 用户评价订单日志
- [ ] Postman 验证
- [ ] Navicat 验证

## 十、V0.6 开发内容

- [x] PaymentStatusEnum
- [x] 订单创建默认未支付
- [x] 模拟支付接口
- [x] 支付原子条件更新与幂等控制
- [x] 支付单号唯一约束
- [x] 支付订单日志
- [x] 骑手大厅过滤未支付订单
- [x] 骑手接单禁止未支付订单
- [x] `06_update_order_payment.sql`
- [x] 订单轨迹接口
- [x] 多数据源时间轴组装
- [ ] SQL 验证
- [ ] Postman 验证
- [ ] Navicat 验证

## 十一、V0.7 开发内容

- [x] `merchant_info`、`merchant_store` 增量建表脚本
- [x] 商家申请和并发重复申请保护
- [x] 商家主体与店铺同事务创建
- [x] 商家资料和店铺管理接口
- [x] 公开店铺联表分页与详情
- [x] GET 店铺查询精确公开放行
- [ ] SQL 验证
- [ ] Postman 验证
- [ ] Navicat 验证

## 十二、V0.8 开发内容

- [x] `merchant_category`、`merchant_product` 增量建表脚本
- [x] 商家分类管理接口
- [x] 商家商品管理接口
- [x] 商品上下架规则
- [x] 公开分类、商品列表和商品详情
- [x] 分类名称并发重复保护
- [x] GET 商品查询精确公开放行
- [x] SQL 验证
- [x] Postman 验证
- [x] Navicat 验证

## 十三、V0.9 开发内容

- [x] `shopping_cart` 增量建表脚本
- [x] 加入购物车与重复商品累加
- [x] 单店铺购物车约束
- [x] 实时商品查询与失效原因
- [x] 数量和选中状态修改
- [x] 单个逻辑删除与清空购物车
- [x] 事务和当前用户权限控制
- [x] Maven Compile
- [x] SQL 验证
- [x] Postman 验证
- [x] Navicat 验证
- [x] 正常流程测试
- [x] 异常流程测试

## 十四、V1.0 开发内容

- [x] `order_item` 商品订单明细快照表脚本
- [x] `order_info` 商品订单兼容字段脚本
- [x] 购物车结算预览接口
- [x] 购物车创建商品订单接口
- [x] 结算公共校验
- [x] 商品库存原子扣减
- [x] 订单、明细、库存、日志和购物车同一事务
- [x] 已选购物车逻辑删除与未选项保留
- [x] 商品订单详情快照兼容
- [x] Maven Compile
- [x] SQL 验证
- [x] Postman 验证
- [x] Navicat 验证
- [x] 正常流程测试
- [x] 异常流程测试

## 十五、V1.1 骑手跑单排行榜模块

- [x] 今日榜、自然周榜、自然月榜和累计榜
- [x] `limit` 默认 10、范围 1～100
- [x] `finish_time` 左闭右开时间统计
- [x] 已完成订单单次 SQL 分组聚合
- [x] 完成单量、最早完成时间和骑手 ID 稳定排序
- [x] 连续顺序名次
- [x] 骑手个人四项条件聚合统计
- [x] 真实姓名、用户昵称和骑手 ID 展示兜底
- [x] JWT 登录访问控制
- [x] 排行榜查询索引幂等升级脚本
- [x] Java 21 Maven Compile
- [x] 参数校验与 limit 范围校验
- [x] SQL 验证
- [x] Postman 验证
- [x] Navicat 验证
- [x] 人工验收

## 十六、V1.2微信支付模块（第一阶段）

- [x] `payment_order`建表脚本
- [x] `CREATED/WAITING_PAY/SUCCESS/CLOSED/FAILED`支付流水状态
- [x] 有效支付单数据库唯一约束
- [x] 微信支付配置属性和Mock环境开关
- [x] 官方`wechatpay-java:0.2.17`依赖
- [x] 支付网关抽象与Mock实现
- [x] 商品订单JSAPI支付单创建
- [x] 有效待支付流水复用
- [x] Mock支付成功确认接口
- [x] 订单支付状态查询
- [x] 支付成功统一事务
- [x] 重复确认幂等和订单日志防重复
- [x] 旧模拟支付生产环境保护
- [x] 环境变量配置和密钥文件Git忽略
- [x] Java 21 Maven Compile
- [x] SQL验证
- [x] Postman验证
- [x] Navicat验证
- [x] 人工验收

人工 Mock 支付链路验收结果：

- [x] 支付单创建成功，订单金额按整数分保存
- [x] 支付流水由 `WAITING_PAY(1)` 更新为 `SUCCESS(2)`
- [x] `transaction_id`、`payer_total`、`success_time`、`notify_id` 写入正确
- [x] 订单 `pay_status`、`payment_no`、`pay_time` 同步正确
- [x] 订单支付日志仅写入一次
- [x] 重复支付确认幂等验证通过

安全边界：

- 当前只支持本地Mock联调，不调用微信平台。
- 新架构只支持服务端可靠计算金额的商品订单。
- 未注册微信回调路径，不存在假验签或任意回调改订单逻辑。
- 第二阶段必须使用官方SDK验签、解密，不能相信小程序前端支付成功回调。

## 十七、V1.3微信用户体系

- [x] 微信小程序配置属性
- [x] 真实code2session网关和超时控制
- [x] 固定Mock code/openid映射
- [x] 真实与Mock网关冲突检查
- [x] 生产Profile禁止Mock登录
- [x] openid查询与自动注册
- [x] 摘要用户名和随机BCrypt密码
- [x] 自动注册密码严格限制在BCrypt 72字节以内
- [x] `uk_openid`并发首次登录保护
- [x] 重复登录返回同一用户
- [x] JWT和用户信息返回
- [x] 精确JWT白名单
- [x] 普通账号密码登录兼容
- [x] Java 21 Maven Compile
- [x] 首次与重复Mock登录单元回归测试
- [x] Postman验证
- [x] Navicat验证
- [ ] 真实微信code2session验证

Profile开发状态：

- [x] 查询当前用户资料
- [x] 修改昵称、头像和性别
- [x] 禁止修改账号、微信标识、手机号、密码和状态
- [x] `UserInfoVO`补充gender
- [x] Profile Postman人工验证

手机号绑定开发状态：

- [x] 微信手机号授权code DTO
- [x] 手机号网关抽象、路由和固定Mock实现
- [x] 绑定、更换、幂等和其他用户占用校验
- [x] 中国大陆手机号格式校验
- [x] JWT保护、UserContext用户ID和脱敏日志
- [x] 服务与网关自动化测试
- [x] 手机号绑定Postman与Navicat人工验证

本阶段不实现已有账号绑定、账号合并、真实微信手机号网关或真实微信支付。

首次人工测试发现并已修复：

- 原随机密码由两个UUID和冒号组成，共73个UTF-8字节。
- BCrypt仅接受不超过72字节的原始密码，因此首次自动注册返回500。
- 修复后使用单个UUID，固定36个UTF-8字节，并在编码前进行字节上限检查。
- 首次与重复Mock登录已通过人工测试。

## 十八、V1.4商家订单管理模块

- [x] 商家订单分页列表
- [x] 订单号和商品名称搜索
- [x] 状态与创建时间筛选
- [x] 当前商家店铺权限隔离
- [x] 商家订单详情与商品快照
- [x] 收件手机号脱敏
- [x] 商家接单原子状态流转
- [x] 商家拒单并进入待退款
- [x] 商家出餐原子状态流转
- [x] 每次有效状态变化写入订单日志
- [x] 商品订单出餐后才进入骑手大厅
- [x] 商品骑手放弃后返回待骑手接单
- [x] 普通跑腿订单骑手流程兼容
- [x] 增量SQL脚本
- [x] 自动化测试
- [x] Maven Compile
- [x] SQL人工执行
- [x] Postman人工验收
- [x] Navicat人工验收

状态增量：

```text
6 = 商家已接单，制作中
7 = 已出餐，待骑手接单
8 = 已关闭，待退款
```

商家拒单不会修改支付流水为退款成功，后续必须接入真实微信退款。

人工验收订单：

```text
orderId = 7
orderNo = WX20260718173934516783
paymentNo = PAY20260718174303093cf8566e8141b1ae648649340679c0

orderId = 8
orderNo = WX20260718180441574851
paymentNo = PAY2026071818153359038b94fc2a4cfc918b822dd4611cd1
```

已验收商品订单状态流程：

```text
0 待商家接单
→ 6 商家已接单，制作中
→ 7 已出餐，待骑手接单
→ 1 骑手已接单
→ 3 待用户确认
→ 4 已完成

商家拒单：0 → 8
```

订单详情已确认 `merchantAcceptTime`、`readyTime` 和完整时间线正常。

拒单验收已确认订单`8`在支付后由`status = 0`原子更新为`status = 8`，
`merchant_reject_time`和`merchant_reject_reason`正确落库，支付状态仍为已支付。
真实退款功能尚未实现，当前仅标记“已关闭，待退款”，等待后续退款处理。

## 十九、V1.5总控端商家审核模块

- [x] 管理员权限审计
- [x] `sys_role`和`sys_user_role`最小 RBAC
- [x] `/api/admin/merchant`统一管理员接口
- [x] 商家申请分页、状态筛选和关键词查询
- [x] 商家申请详情与申请人非敏感信息
- [x] 审核通过原子条件更新
- [x] 审核拒绝原子条件更新
- [x] 审核通过不自动设置营业中
- [x] 商家启用与禁用
- [x] 禁用后店铺禁用并停止营业
- [x] `merchant_audit_log`独立操作日志
- [x] 并发审核保护
- [x] 自动化测试
- [x] 14号数据库升级脚本
- [x] Navicat人工执行与字段、索引核对
- [x] Postman管理员权限和业务流程验收

权限方案：

```text
JWT userId
→ sys_user_role
→ sys_role.role_code = ADMIN
→ 允许访问 /api/admin/**
```

JWT不保存管理员角色，角色变更可立即生效。迁移脚本不自动给任何账号授予管理员权限，
必须在Navicat确认真实管理员账号后人工授权。

人工验收结果：

- [x] `14_create_admin_merchant_audit.sql`已在当前测试数据库执行
- [x] `ADMIN`角色已初始化，`admin`用户`userId=1`已绑定该角色
- [x] 管理员登录、商家分页和详情查询通过
- [x] `merchantId=2`、`storeId=2`审核通过，审核字段与`APPROVE`日志正确
- [x] `merchantId=2`重复审核返回409
- [x] `merchantId=3`待审核时不能启用
- [x] `merchantId=3`、`storeId=3`审核拒绝，状态与`REJECT`日志正确
- [x] `merchantId=1`禁用和重新启用通过，重新启用后`business_status=0`
- [x] 普通用户访问`/api/admin/**`返回`403 无管理员权限`

验收结论：V1.5总控端商家管理后端已完成并通过人工验收。

## 二十、V1.6总控管理后台前端

状态：总控管理后台开发完成，人工验收通过。

完成功能：

- [x] 管理员登录
- [x] RBAC 权限控制
- [x] ADMIN 角色验证
- [x] 商家分页列表
- [x] 商家搜索和筛选
- [x] 商家详情页面
- [x] 商家审核通过
- [x] 商家审核拒绝
- [x] 商家启用
- [x] 商家禁用
- [x] 店铺状态同步
- [x] 前端 Element Plus 按需加载优化

第一阶段已完成：

- [x] 创建`wuxin-admin-web`
- [x] Vue 3、TypeScript和Vite项目初始化
- [x] Vue Router、Pinia、Axios和Element Plus
- [x] ESLint与Prettier
- [x] `/api`开发代理配置
- [x] 后端`Result<T>`统一处理
- [x] Bearer Token自动注入和刷新恢复
- [x] 401清理Token并跳转登录页
- [x] 403提示无管理员权限
- [x] 管理员状态Store和基础路由守卫
- [x] 管理员登录与六个商家管理API封装
- [x] 登录、后台首页、商家管理和404基础页面
- [x] 后台侧边栏、顶部栏和退出登录
- [x] TypeScript、ESLint和生产构建检查

第二阶段已完成：

- [x] 登录页调用真实`POST /api/user/login`
- [x] 登录请求字段使用`username`和`password`
- [x] 复用后端`token`和`userInfo`返回结构
- [x] 登录成功后保存Token和用户基础信息
- [x] 使用`GET /api/admin/merchant/page?pageNum=1&pageSize=1`验证管理员权限
- [x] 非管理员登录后清理Token和用户信息，并提示当前账号无管理员权限
- [x] 刷新页面后重新验证管理员权限
- [x] 401统一清理认证状态并跳转登录页
- [x] 403统一提示无管理员权限并跳转登录页
- [x] 后台首页展示当前管理员和V1.6版本状态，不展示虚假统计数据

第三阶段已完成：

- [x] 商家管理页接入真实`GET /api/admin/merchant/page`
- [x] 仅使用后端真实支持的`pageNum`、`pageSize`、`auditStatus`、`merchantStatus`和`keyword`
- [x] 不伪造`businessStatus`或`merchantId`筛选能力
- [x] 支持审核状态、商家状态和关键词查询
- [x] 支持分页、每页数量切换、手动刷新和重置筛选
- [x] 列表展示商家ID、商家名称、店铺名称、联系人、联系电话、审核状态、商家状态、营业状态和申请时间
- [x] 状态使用统一映射和Element Plus Tag展示
- [x] 请求加载、空数据和错误重试处理
- [x] 使用请求序号和卸载保护避免旧请求覆盖新结果
- [x] 新增`/merchants/:merchantId`详情占位路由，本阶段不调用详情接口
- [x] 审核相关按钮仅占位提示，不调用审核、拒绝、启用或禁用接口

第四阶段已完成：

- [x] 审计`GET /api/admin/merchant/{merchantId}`真实返回字段
- [x] 商家详情页接入真实`GET /api/admin/merchant/{merchantId}`
- [x] 详情类型独立使用`AdminMerchantDetail`，列表继续使用`AdminMerchantSummary`
- [x] 详情页展示商家基础信息、审核信息、店铺信息、营业信息和资质信息
- [x] 支持返回列表、刷新、loading、错误提示和不存在数据处理
- [x] 详情页只读，不提供审核、拒绝、启用、禁用、编辑或图片上传操作
- [x] 列表与详情共用商家状态Tag映射

第五阶段已完成：

- [x] 商家详情页接入审核通过、审核拒绝、启用和禁用操作
- [x] 审核通过请求字段使用后端真实DTO字段`auditRemark`
- [x] 审核拒绝和禁用请求字段使用后端真实DTO字段`reason`
- [x] 启用接口按真实Controller定义提交空请求体，不伪造`reason`
- [x] 根据`auditStatus`和`merchantStatus`控制操作按钮展示
- [x] 使用Element Plus Dialog输入审核备注或操作原因
- [x] 操作提交提供loading、防重复提交、成功提示和失败提示
- [x] 操作成功后重新请求详情接口刷新数据，不在前端硬编码成功状态

第六阶段已完成：

- [x] Element Plus由全量注册调整为自动按需引入
- [x] 新增`unplugin-auto-import`和`unplugin-vue-components`
- [x] 移除`app.use(ElementPlus)`和全量样式引入
- [x] API层整理为查询和操作两类方法，保留纯HTTP职责
- [x] 列表类型调整为`AdminMerchantPageVO`，详情类型调整为`AdminMerchantDetailVO`
- [x] 请求类型保持`ApproveMerchantRequest`、`RejectMerchantRequest`和`MerchantStatusOperationRequest`
- [x] 审核、商家、店铺和营业状态统一由`merchantStatus.ts`处理
- [x] 清理重复详情API别名和页面内分散状态判断
- [x] TypeScript、ESLint、生产构建和`git diff --check`通过
- [x] 构建已消除500kB chunk warning

当前边界：

- V1.6总控管理后台已完成并通过人工验收。
- V1.7用户小程序目录已创建，第一阶段基础工程、请求层、认证基础和页面骨架已完成。

人工验收结果：

- [x] 管理员登录测试
- [x] 普通用户访问后台403测试
- [x] 商家申请入驻流程测试
- [x] 审核通过测试
- [x] 审核拒绝测试
- [x] 商家禁用测试
- [x] 商家启用测试
- [x] 列表到详情跳转测试

人工验收测试数据：

```text
merchantId=4
- 商家审核通过测试
- audit_status=1
- audit_admin_id=1
- audit_remark=材料准确 允许通过

merchantId=5
- 商家审核拒绝测试
- audit_status=2
- audit_admin_id=1
- reject_reason=营业执照信息不清晰，请重新上传。
```

本地环境配置：

```properties
spring.datasource.password=${DB_PASSWORD:}
```

IDEA 本地环境变量：

```text
DB_PASSWORD=123456
```

开发环境使用 IDEA 环境变量注入数据库密码，生产环境通过服务器环境变量配置，避免数据库密码进入代码仓库。

当前固定断点：V1.7-8C 人工复测。

## 二十一、V1.7用户微信小程序

第一阶段已完成：

- [x] 创建`wuxin-miniapp`
- [x] 使用微信原生小程序和TypeScript
- [x] 集成Vant Weapp依赖声明
- [x] 配置ESLint、Prettier和TypeScript检查
- [x] 配置微信开发者工具`project.config.json`
- [x] 建立统一请求层，处理后端`Result<T>`结构
- [x] 自动注入`Authorization: Bearer <token>`
- [x] `401`清理Token并跳转登录页
- [x] 建立Token和用户信息本地存储常量
- [x] 封装`POST /api/user/wechat/login`
- [x] 封装`GET /api/user/me`
- [x] 封装`GET /api/user/profile`
- [x] 创建微信登录、首页、地址列表、订单列表和个人中心页面骨架
- [x] 创建通用空状态组件

第二阶段已完成：

- [x] 微信登录页展示品牌标题、登录说明、微信一键登录按钮和协议提示占位
- [x] 点击登录后调用`wx.login()`获取临时`code`
- [x] 小程序仅向后端提交`code`，不提交`openid`、`unionid`、`sessionKey`或`userId`
- [x] 调用真实后端`POST /api/user/wechat/login`
- [x] 成功后保存`token`、`userInfo`和`newUser`
- [x] App启动时恢复本地登录态
- [x] 已登录访问登录页时跳转首页
- [x] 首页、地址列表、订单列表和个人中心使用统一`requireLogin()`保护
- [x] `401`自动清理认证信息并跳转登录页
- [x] 个人中心展示基础资料和手机号绑定状态
- [x] 退出登录前二次确认，确认后清理认证信息并返回登录页
- [x] 本地Mock微信登录提供开发工具Storage开关，默认关闭且release环境强制禁用

第三阶段已完成：

- [x] App启动恢复`token`、`userInfo`和`newUser`
- [x] 登录状态校验改为调用`GET /api/user/profile`
- [x] 首页进入后自动刷新Profile并展示头像、昵称、用户名和手机号状态
- [x] 昵称为空时统一显示“微信用户”
- [x] 头像为空时统一显示默认头像占位
- [x] 个人中心展示头像、昵称、用户名、手机号和性别
- [x] 手机号为空时显示“未绑定”，不展示`null`或`undefined`
- [x] 新增编辑资料页面
- [x] 编辑资料页支持修改昵称和头像URL
- [x] 调用`PUT /api/user/profile`更新资料
- [x] 更新成功后调用`GET /api/user/profile`同步Storage和AuthState
- [x] 后端返回错误时展示后端`message`

V1.7.3稳定性修复已完成：

- [x] 修复`pages/profile/edit`目录导入导致白屏的问题
- [x] 小程序代码不再使用`../../../api`这类目录导入
- [x] `restoreSession()`兼容`App.onLaunch`中过早访问`getApp()`的场景
- [x] `clearAuth()`和`persistAuth()`兼容`getApp()`尚不可用的场景
- [x] Home、Profile和Edit Profile页面增加必要判空，避免初始渲染白屏
- [x] 默认头像改为本地资源，不引用外部占位图片域名
- [x] ESLint忽略微信开发者工具生成的`miniprogram/miniprogram_npm`
- [x] TypeScript、ESLint、Build和`git diff --check`通过

第四阶段已完成：

- [x] 封装`POST /api/user/phone/bind`
- [x] 小程序通过`wx.getPhoneNumber()`获取手机号授权`code`
- [x] 本地开发继续复用`MockWeChatPhoneGateway`
- [x] Profile页面在手机号为空时显示“立即绑定”
- [x] Profile页面在手机号已存在时显示脱敏号码并禁止重复点击
- [x] 绑定成功后刷新Profile资料
- [x] 绑定成功后同步Home、Storage、AuthState和`App.globalData`
- [x] 用户拒绝授权时提示“已取消手机号授权”
- [x] 接口失败时展示后端`message`
- [x] 未开发短信验证码登录、腾讯云短信或手机号修改

第五阶段已完成：

- [x] 审计真实后端地址接口，当前地址前缀为`/api/user/address`
- [x] 封装`GET /api/user/address/list`
- [x] 封装`POST /api/user/address`
- [x] 封装`DELETE /api/user/address/{id}`
- [x] 新增统一`Address`和`AddressRequest`类型，供后续下单复用
- [x] 地址列表展示收件人、脱敏手机号、完整地址和默认标签
- [x] 默认地址置顶展示
- [x] 空数据展示空状态
- [x] 新增地址表单支持收件人、手机号、省、市、区、详细地址和默认地址
- [x] 新增地址前端校验收件人、11位手机号和详细地址
- [x] 编辑地址调用`PUT /api/user/address/{id}`
- [x] 设置默认地址调用`PUT /api/user/address/{id}/default`
- [x] 后端保证同一用户最多一个默认地址
- [x] 后端禁止修改、删除或设置他人地址
- [x] 删除地址二次确认后刷新列表
- [x] 返回地址列表时自动刷新
- [x] 个人中心新增“收货地址”入口
- [x] 地址卡片、留白、阴影、按钮和底部固定新增按钮完成第一轮基础美化

V1.7-5A后端补全结果：

- [x] 新增`PUT /api/user/address/{id}`编辑地址接口
- [x] 新增`PUT /api/user/address/{id}/default`设置默认地址接口
- [x] `UserAddressController`仅保留接口入口和统一Result返回
- [x] `UserAddressService`补充新增、列表、编辑、设置默认和删除方法
- [x] `UserAddressServiceImpl`使用事务处理默认地址唯一性
- [x] `UserAddressDTO`补充手机号格式和字段长度校验
- [x] 不修改数据库结构，不新增SQL

V1.7-6首页商家与服务入口已完成：

- [x] 首页顶部展示用户昵称、本地默认头像、脱敏手机号和默认地址摘要
- [x] Home.onShow刷新`GET /api/user/profile`和`GET /api/user/address/list`
- [x] 新增搜索入口和搜索占位页，不伪造搜索结果
- [x] 新增本地Banner轮播资源，不引用外部占位图片
- [x] 新增跑腿代取、商品配送、帮买服务和帮送服务入口
- [x] 新增走失儿童公益信息、校园欺凌免费求助和紧急免费救援入口
- [x] 推荐门店接入真实`GET /api/store/list`
- [x] 门店详情占位页只读接入真实`GET /api/store/{id}`
- [x] 商品列表、商品详情、购物车、支付和订单详情留到后续阶段
- [x] 首页加载、空状态、错误重试和图片失败兜底已处理
- [x] 不新增后端接口，不修改数据库，不新增SQL

V1.7-7门店与商品列表/商品详情已完成：

- [x] 门店详情正式页展示门店名称、图片、营业状态、地址、电话、营业时间和简介
- [x] 商品分类调用真实`GET /api/store/{storeId}/categories`
- [x] 商品列表调用真实`GET /api/store/{storeId}/products`
- [x] 商品详情调用真实`GET /api/store/product/{id}`
- [x] 店内商品支持分类切换、关键词搜索、分页加载、空状态和失败重试
- [x] 商品卡片展示图片、名称、说明、价格和库存
- [x] 商品详情展示商品图片、名称、价格、库存、分类、所属门店和购买前确认
- [x] 搜索页接入真实`GET /api/store/list?keyword=...`门店搜索
- [x] 图片继续复用`normalizeImageUrl()`，本地默认商品图为`/assets/images/product-placeholder.svg`
- [x] V1.7-7阶段“加入购物车”仅提示下一阶段开放，不伪造加入成功
- [x] 不新增后端接口，不修改数据库，不新增SQL

V1.7-7A商品数据链路审计与修复已完成：

- [x] 完成数据库、Mapper、Service、Controller、API和小程序前端全链路审计
- [x] 确认后端公开分类和商品接口会额外过滤`merchant_store.business_status = 1`
- [x] 确认`GET /api/store/{id}`仅要求商家审核通过、商家启用、店铺启用且未删除，因此门店详情可正常返回
- [x] 确认商品为空的真正原因是测试数据`storeId=1`的`business_status=0`
- [x] 未发现小程序门店详情页`storeId`、`categoryId`、分页参数、请求地址或数据解析错误
- [x] 未修改后端代码、前端代码、数据库结构或SQL文件
- [x] 已修正当前测试库测试数据：`merchant_store.id=1`更新为`business_status=1`
- [x] Postman/实际接口验证`GET /api/store/1/categories`返回`categoryId=2`
- [x] Postman/实际接口验证`GET /api/store/1/products?pageNum=1&pageSize=10`返回`productId=2`
- [x] Postman/实际接口验证`GET /api/store/product/2`返回商品详情

V1.7-7商品链路人工复测问题修复已完成：

- [x] 确认商品图片失败真实URL为`https://test.com/test.jpg`
- [x] 确认门店图片测试值为`logo-test-2`，前端会归一化为本地门店占位图
- [x] `normalizeImageUrl()`拦截明显不适合小程序图片展示的本地路径、测试域名和占位域名
- [x] 商品列表和商品详情图片加载失败后兜底为`/assets/images/product-placeholder.svg`
- [x] 门店图片加载失败后兜底为`/assets/images/home/store-placeholder.svg`
- [x] 图片失败处理增加已兜底判断，避免持续刷渲染层网络错误
- [x] 当前测试库`merchant_product.id=2`的`product_image`已由`https://test.com/test.jpg`修正为`/assets/images/product-placeholder.svg`
- [x] 当前测试库`merchant_store.id=1`的`store_logo`已由`logo-test-2`修正为`/assets/images/home/store-placeholder.svg`
- [x] 商品搜索栏二次修复为两行布局，第一行输入框、第二行搜索和清空按钮
- [x] 清空按钮常驻占位，无关键词时禁用，避免输入时按钮显隐引发布局跳动
- [x] 搜索和清空按钮移除默认边框，不使用绝对定位覆盖输入框
- [x] 生产环境图片必须统一使用腾讯云COS HTTPS地址

本阶段未开发：

- [ ] 完整订单列表和详情页面
- [ ] 购物车页面
- [ ] 支付页面
- [ ] 骑手端、商家端、站长端或公益复杂功能
- [ ] 短信验证码登录、腾讯云短信和手机号修改

## 二十二、V1.7-8 用户购物车

本阶段已完成：

- [x] 商品详情页接入真实 `POST /api/cart/add`
- [x] 同商品重复加入由后端合并数量
- [x] 新增购物车页面 `/pages/cart/index`
- [x] 购物车列表接入 `GET /api/cart/list`
- [x] 数量增减接入 `PUT /api/cart/update`
- [x] 单项选中和取消选中接入 `PUT /api/cart/selected`
- [x] 全选和取消全选接入 `PUT /api/cart/selected/all`
- [x] 删除单个商品接入 `DELETE /api/cart/{id}`
- [x] 清理失效商品接入 `DELETE /api/cart/invalid`
- [x] 清空购物车接入 `DELETE /api/cart/clear`
- [x] 跨店铺加购冲突时提示用户清空原购物车后重试
- [x] 商品、门店和个人中心新增购物车入口与真实角标同步
- [x] “我的”页新增商家入驻和申请成为骑手正式占位入口
- [x] 失效商品不参与全选、不计入合计金额和已选数量
- [x] 底部结算按钮仅提示 V1.7-9 开放，不创建订单

本阶段未开发：

- [ ] 真实提交订单
- [ ] 支付
- [ ] 优惠券
- [ ] 商家入驻资料提交
- [ ] 骑手申请资料提交

真实接口回归结果：

- 空购物车、首次加购、重复加购合并、修改数量、单项选中、全选、取消全选、清理失效商品、删除单项和清空购物车均已通过。
- 当前公开测试数据只有一个可用门店商品，跨店铺冲突需在补充第二个可用门店商品后继续复测。

## 二十三、V1.7-8A 购物车交互重构

本阶段已完成：

- [x] 门店商品列表支持直接快捷加购
- [x] 商品列表未加入购物车时显示圆形“+”
- [x] 商品列表已加入购物车时显示`[- 数量 +]`
- [x] 门店商品列表加购、增加、减少和删除均调用真实后端购物车接口
- [x] 门店详情页新增固定底部折叠购物车栏
- [x] 底部栏展示真实购物车数量和后端返回的已选合计金额
- [x] 底部栏购物车为空时展示空状态，去结算禁用
- [x] 点击底部栏打开底部购物车弹层
- [x] 弹层支持商品查看、数量增减、删除、清空和关闭
- [x] 弹层内部滚动，避免遮挡页面底部栏
- [x] 独立购物车页重构为稳定横向商品卡片布局
- [x] 购物车状态统一通过`refreshCartDetail()`同步

本阶段未开发：

- [ ] 订单创建
- [ ] 支付
- [ ] 配送费计算
- [ ] 优惠券

## 二十四、V1.7-8B 购物车 UI 重构

本阶段已完成：

- [x] 按购物车设计稿重构门店详情页购物车相关UI
- [x] 按设计稿统一页面背景`#F6F7F9`
- [x] 按设计稿统一卡片16px圆角和`0 4px 16px rgba(0,0,0,.06)`阴影
- [x] 商品图片统一为72×72
- [x] 数量控制器统一为36px高度
- [x] 底部购物车栏按设计稿重构
- [x] BottomSheet购物车弹层按设计稿重构
- [x] 独立购物车页按设计稿重构
- [x] 保持购物车接口、数据库、业务规则和状态同步不变

本阶段未开发：

- [ ] 订单创建
- [ ] 支付
- [ ] 配送费计算
- [ ] 优惠券

该阶段断点曾为：V1.7-8B 人工复测。

## 二十五、V1.7-8C 购物车前端布局彻底重构

本阶段已完成：

- [x] 不再沿用旧购物车WXML/WXSS局部补丁，门店详情页购物车相关布局已重新组织
- [x] 门店商品卡重建为图片区、商品信息区、库存信息、价格和数量控制器的稳定横向结构
- [x] 门店底部购物车栏重建为购物车图标与角标、金额/数量摘要和绿色去结算按钮
- [x] BottomSheet购物车弹层重建为遮罩、弹层主体、顶部标题/清空/关闭、中间列表和底部结算栏
- [x] 独立购物车页重建为门店卡、商品列表卡、可结算商品/失效商品和底部结算栏
- [x] 商品图片按72×72设计尺寸展示，卡片圆角、阴影、留白、按钮高度和品牌色按设计稿规范校正
- [x] 长商品名、长金额和两位数量通过`min-width: 0`、固定控制区和文本截断处理，避免竖排和覆盖
- [x] 底部购物车栏、BottomSheet和独立购物车底部栏均保留安全区域适配
- [x] 保持购物车接口、数据库、后端和业务逻辑不变

本阶段未开发：

- [ ] 订单创建
- [ ] 支付
- [ ] 配送费计算
- [ ] 优惠券

当前固定断点：V1.7-8C 人工复测。

## 二十六、V1.0 验收结果

本轮已完成：

- [x] 用户登录（JWT）
- [x] 地址管理
- [x] 商品管理
- [x] 购物车
- [x] 商品订单
- [x] 骑手大厅
- [x] 骑手接单
- [x] 我的订单
- [x] 订单详情
- [x] 权限校验
- [x] 越权测试

验收结论：

```text
V1.0 测试全部通过。
```

## 二十七、本轮安全测试

### 地址越权测试

测试结果：通过。

测试过程：`admin` 使用 `test001` 的地址 `id = 4` 创建订单。

返回结果：

```json
{
  "code": 404,
  "message": "收货地址不存在",
  "data": null
}
```

说明：地址归属校验正常，其他用户不能使用不属于自己的地址创建订单。

### 订单越权测试

测试结果：通过。

测试过程：`admin` 查询 `test001` 的订单详情。

返回结果：订单不存在或无权限访问。

说明：订单归属校验正常，接口不会向其他用户暴露订单是否真实存在。

## 二十八、项目规范

项目分层：

```text
Controller
↓
Service
↓
Mapper
↓
Entity
↓
VO
```

统一规范：

- [x] Result
- [x] BusinessException
- [x] GlobalExceptionHandler
- [x] DTO 接收请求参数
- [x] VO 返回前端数据
- [x] Entity 不直接返回前端

## 二十九、开发流程

固定流程：

```text
需求设计
↓
开发实现
↓
Compile
↓
自动化测试
↓
Postman
↓
Navicat
↓
文档同步
↓
文档审计
↓
Git Commit
```
