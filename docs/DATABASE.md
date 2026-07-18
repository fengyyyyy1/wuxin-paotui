# 数据库文档

> 数据库：`wuxin_paotui`  
> 当前版本：V1.3 微信用户体系

## 一、sys_user

作用：用户基础信息表。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 用户 ID |
| username | 用户名 |
| password | BCrypt 加密密码 |
| phone | 手机号 |
| openid | 微信 openid |
| unionid | 微信 unionid，可为空 |
| nickname | 昵称 |
| avatar | 头像 |
| gender | 性别：0 未知、1 男、2 女 |
| status | 用户状态 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_username(username) | 用户名唯一 |
| uk_openid(openid) | 微信 openid 唯一，允许多个 NULL |
| idx_phone(phone) | 手机号查询 |

说明：

- 普通账号登录使用 `username` 和 `password`。
- 密码使用 BCrypt 加密保存。
- 微信登录按 `openid` 查询且过滤 `is_deleted = 0`。
- 自动注册用户使用摘要用户名和随机 BCrypt 密码，不保存 `session_key`。
- `unionid`当前没有索引，也不用于自动合并账号。
- 微信手机号绑定继续写入现有 `phone varchar(20)`，不新增字段。
- 绑定前按 `phone`、`is_deleted = 0` 排除当前用户检查占用；重复绑定当前手机号幂等成功。
- `idx_phone`是普通索引，不提供数据库唯一约束。当前业务层的“先检查、再更新”不能彻底消除并发绑定相同手机号的竞态，后续数据治理阶段需单独评估唯一约束。

## 二、user_address

作用：用户地址表。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 地址 ID |
| user_id | 用户 ID |
| receiver_name | 收件人 |
| receiver_phone | 收件人手机号 |
| province | 省 |
| city | 市 |
| district | 区 |
| detail_address | 详细地址 |
| latitude | 纬度 |
| longitude | 经度 |
| is_default | 是否默认地址 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| user_id | 建议按用户查询地址 |

说明：

- 地址只能由所属用户操作。
- 删除使用逻辑删除。

## 三、order_info

作用：订单主表。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 订单 ID |
| order_no | 订单号 |
| user_id | 下单用户 ID |
| rider_id | 接单骑手 ID |
| pickup_address_id | 取件地址 ID |
| delivery_address_id | 收件地址 ID |
| goods_name | 物品名称 |
| goods_description | 物品描述 |
| weight | 重量 |
| distance | 距离 |
| price | 价格 |
| status | 订单状态 |
| remark | 备注 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| accept_time | 接单时间 |
| finish_time | 完成时间 |
| pay_status | 支付状态，0 未支付、1 已支付 |
| pay_time | 支付时间 |
| payment_no | 支付单号 |
| order_type | 订单类型：0 跑腿订单、1 商品订单 |
| store_id | 商品订单所属店铺 ID |
| product_amount | 商品金额 |
| delivery_fee | 配送费 |
| total_amount | 应付总金额 |
| deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_order_no(order_no) | 订单号唯一 |
| idx_order_rider_status_deleted | 骑手订单查询 |
| idx_order_status_deleted_create_time | 骑手大厅查询 |
| idx_order_pay_status_deleted_create_time | 按支付状态查询订单 |
| uk_order_payment_no(payment_no) | 支付单号唯一 |
| idx_order_type_user_deleted_create_time(order_type, user_id, deleted, create_time) | 用户按订单类型查询 |
| idx_order_store_status_deleted_create_time(store_id, status, deleted, create_time) | 店铺订单状态查询 |
| idx_order_status_deleted_finish_rider(status, deleted, finish_time, rider_id) | V1.1 骑手跑单排行榜统计 |

说明：

- `accept_time` 在骑手接单成功后写入。
- `finish_time` 预留给骑手完成配送。
- 订单查询统一过滤 `deleted = 0`。
- 新订单默认 `pay_status = 0`，支付成功后写入支付时间和支付单号。
- `status` 表示配送业务状态，`pay_status` 表示支付状态，两者独立。
- 骑手大厅和骑手接单均要求 `pay_status = 1`。
- 跑腿订单 `order_type = 0`，继续使用原有取件、收件和物品字段。
- 商品订单 `order_type = 1`，金额拆分写入 `product_amount`、`delivery_fee`、`total_amount`。
- 为兼容现有支付逻辑，商品订单同时将 `total_amount` 写入 `price`。
- 骑手排行榜只统计 `status = 4`、`rider_id IS NOT NULL`、`deleted = 0` 的订单。
- 今日、本周和本月榜以 `finish_time` 为统计时间，使用 `>= startTime`、`< endTime`，不对字段使用日期函数。
- 累计榜统计全部已完成订单，不限制 `finish_time` 时间范围。

### 订单类型

| order_type | 说明 |
| --- | --- |
| 0 | 跑腿订单 |
| 1 | 商品订单 |

### 支付状态

| pay_status | 说明 |
| --- | --- |
| 0 | 未支付 |
| 1 | 已支付 |

### 订单状态

| status | 说明 |
| --- | --- |
| 0 | 待接单 |
| 1 | 已接单 |
| 2 | 配送中 |
| 3 | 待确认收货 |
| 4 | 已完成 |
| 5 | 已取消 |

## 四、order_log

作用：订单状态流转日志表。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 日志 ID |
| order_id | 订单 ID |
| old_status | 原状态 |
| new_status | 新状态 |
| operator_id | 操作人 ID |
| operator_type | 操作人类型 |
| remark | 备注 |
| create_time | 创建时间 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| order_id | 建议按订单查询日志 |

说明：

- 骑手接单成功后写入日志。
- `operator_type = RIDER` 表示骑手操作。
- 用户取消订单成功后写入 `0 → 5` 状态日志，`operator_type = USER`。
- 骑手放弃订单成功后写入 `1 → 0` 状态日志，`operator_type = RIDER`。
- 用户评价订单成功后写入 `4 → 4` 状态日志，`operator_type = USER`。
- 用户模拟支付成功后写入 `0 → 0` 状态日志，`operator_type = USER`。

## 五、order_comment

作用：订单评价表，一个订单只能评价一次。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 评价 ID |
| order_id | 订单 ID |
| user_id | 评价用户 ID |
| rider_id | 被评价骑手 ID |
| score | 评分，范围 1～5 |
| content | 评价内容，最长 500 字 |
| is_anonymous | 是否匿名，0 否、1 是 |
| create_time | 评价时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_order_comment_order_id(order_id) | 订单唯一评价索引 |

说明：

- 仅订单发布用户可以评价自己的已完成订单。
- `order_id` 唯一索引负责防止并发重复评价。
- 默认 `is_anonymous = 0`、`is_deleted = 0`。

## 六、rider_info

作用：骑手信息表。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 骑手 ID |
| user_id | 关联用户 ID |
| real_name | 真实姓名 |
| id_card | 身份证号 |
| id_card_front | 身份证正面 |
| id_card_back | 身份证反面 |
| audit_status | 审核状态 |
| rider_status | 骑手状态 |
| create_time | 创建时间 |
| update_time | 更新时间 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| user_id | 建议按用户查询骑手 |

说明：

- 当前约定 `audit_status = 1` 表示审核通过。
- 当前约定 `rider_status = 1` 表示骑手启用。
- `RiderInfoEntity` 不包含 `status`、`deleted`。

## 七、merchant_info

作用：记录商家主体、联系人和审核信息，通过 `user_id` 关联 `sys_user.id`。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 商家主体 ID |
| user_id | 关联用户 ID |
| merchant_name | 商家主体名称 |
| contact_name | 联系人姓名 |
| contact_phone | 联系人手机号 |
| business_license | 营业执照图片地址 |
| id_card_front | 身份证正面地址 |
| id_card_back | 身份证反面地址 |
| audit_status | 审核状态：0 待审核、1 通过、2 驳回 |
| audit_remark | 审核意见 |
| merchant_status | 商家状态：0 禁用、1 启用 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_merchant_user_id(user_id) | 一个用户只能申请一个商家主体 |
| idx_merchant_audit_status(audit_status, merchant_status, is_deleted) | 审核及启用状态查询 |

## 八、merchant_store

作用：记录店铺展示、地址和营业信息，通过 `merchant_id` 关联 `merchant_info.id`。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 店铺 ID |
| merchant_id | 商家主体 ID |
| store_name | 店铺名称 |
| store_logo | 店铺 Logo |
| store_description | 店铺简介 |
| store_phone | 店铺联系电话 |
| province / city / district | 行政区划 |
| detail_address | 详细地址 |
| latitude / longitude | 经纬度 |
| business_status | 营业状态：0 休息、1 营业 |
| open_time / close_time | 营业时间 |
| store_status | 店铺状态：0 禁用、1 启用 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_store_merchant_id(merchant_id) | 一个商家主体只能拥有一个店铺 |
| idx_store_business_status(business_status, store_status, is_deleted) | 营业及启用状态查询 |

## 九、merchant_category

作用：记录店铺商品分类，通过 `store_id` 关联 `merchant_store.id`。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 商品分类 ID |
| store_id | 所属店铺 ID |
| category_name | 分类名称，最长 50 字 |
| sort | 排序，数值越小越靠前 |
| status | 状态：0 禁用、1 启用 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_category_store_name(store_id, category_name) | 同一店铺分类名称唯一 |
| idx_category_store_status_sort(store_id, status, is_deleted, sort) | 店铺分类状态及排序查询 |

说明：

- 分类默认启用，删除使用逻辑删除。
- 禁用分类不会删除分类下商品，但公开接口不返回该分类。
- 分类下存在未删除商品时不允许删除分类。

## 十、merchant_product

作用：记录店铺商品，通过 `store_id` 关联店铺，通过 `category_id` 关联商品分类。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 商品 ID |
| store_id | 所属店铺 ID |
| category_id | 所属分类 ID |
| product_name | 商品名称，最长 100 字 |
| product_image | 商品图片地址 |
| product_description | 商品介绍，最长 500 字 |
| price | 销售价格，必须大于 0 |
| original_price | 原价，可为空，有值时必须大于 0 |
| stock | 库存，不能小于 0 |
| sales | 销量，默认 0 |
| product_status | 商品状态：0 下架、1 上架 |
| sort | 排序 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| idx_product_store_status_sort(store_id, product_status, is_deleted, sort) | 店铺商品状态和排序查询 |
| idx_product_category_status(category_id, product_status, is_deleted) | 分类商品查询 |
| idx_product_store_name(store_id, product_name) | 店铺商品名称查询 |

说明：

- 商品创建时默认下架，销量默认为 0。
- 商品上架要求分类启用且库存大于 0。
- 商品删除为逻辑删除，并同步设置为下架。

## 十一、shopping_cart

作用：记录用户购物车商品及数量，通过 `user_id`、`store_id`、`product_id` 关联用户、店铺和商品。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 购物车 ID |
| user_id | 用户 ID |
| store_id | 店铺 ID |
| product_id | 商品 ID |
| quantity | 商品数量，默认 1 |
| selected | 是否选中：0 否、1 是 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| uk_user_product_deleted(user_id, product_id, is_deleted) | 防止同一用户出现重复有效商品记录 |
| idx_user_store_deleted(user_id, store_id, is_deleted) | 用户单店铺购物车查询 |

说明：

- 购物车不保存商品名称、图片或价格快照。
- 查询购物车时实时关联 `merchant_product`、`merchant_category`、`merchant_store`。
- 同一用户同一时间只允许保留一个店铺的有效购物车商品。
- 商品失效时保留购物车记录，通过接口返回失效原因。
- 商品价格快照在创建商品订单时写入 `order_item`。
- 分类有效状态字段统一为 `merchant_category.status`。
- 店铺营业状态字段统一为 `merchant_store.business_status`。

## 十二、order_item

作用：保存商品订单下单时的商品名称、图片、价格、数量和小计快照，历史订单查询不依赖商品当前数据。

主要字段：

| 字段 | 说明 |
| --- | --- |
| id | 订单明细 ID |
| order_id | 关联 `order_info.id` |
| product_id | 下单时商品 ID |
| product_name | 商品名称快照 |
| product_image | 商品图片快照 |
| product_price | 商品单价快照 |
| quantity | 购买数量 |
| subtotal | 商品小计快照 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| idx_order_item_order_deleted(order_id, is_deleted) | 查询订单商品快照 |
| idx_order_item_product_deleted(product_id, is_deleted) | 按商品查询订单明细 |

说明：

- `product_name`、`product_image`、`product_price` 均为下单时快照。
- 商品后续改名、换图或调价不会改变历史订单详情。
- 当前版本不增加外键约束，由业务事务保证订单主表与明细一致。

## 十三、payment_order

作用：保存平台支付流水。第一阶段用于本地 Mock 联调，后续兼容微信支付 API v3 JSAPI。

主要字段：

| 字段 | 说明 |
| --- | --- |
| payment_no | 平台支付单号，未来作为微信 `out_trade_no` |
| order_id / order_no / user_id | 订单与付款用户 |
| payment_channel | `MOCK` 或 `WECHAT` |
| trade_type | 当前为 `JSAPI` |
| appid / mchid / openid | 微信支付身份信息，Mock 可为空 |
| amount_total | 应付金额，单位分 |
| status | 支付流水状态 |
| prepay_id | 预支付ID |
| transaction_id | 渠道交易号 |
| payer_total | 实付金额，单位分 |
| success_time / expire_time | 支付成功和失效时间 |
| notify_id / notify_body_hash | 回调幂等标识与原文摘要 |
| error_code / error_message | 脱敏后的支付错误 |
| version | 乐观版本 |
| deleted / active_order_id | 逻辑删除与有效支付单约束 |

支付流水状态：

| status | 说明 |
| --- | --- |
| 0 | CREATED |
| 1 | WAITING_PAY |
| 2 | SUCCESS |
| 3 | CLOSED |
| 4 | FAILED |

索引：

- `uk_payment_no(payment_no)`
- `uk_transaction_id(transaction_id)`
- `uk_notify_id(notify_id)`
- `uk_active_order_id(active_order_id)`
- `idx_payment_order_id(order_id, deleted, create_time)`
- `idx_payment_user_id(user_id, deleted, create_time)`
- `idx_payment_status_expire(status, expire_time)`

`active_order_id`为MySQL生成列：仅当`deleted=0`且状态为`CREATED/WAITING_PAY`时返回`order_id`，否则返回`NULL`，保证一个订单最多一个有效支付单，同时允许失败或关闭后重新创建。

金额规则：商品订单从`order_info.total_amount`读取人民币元，Java使用`BigDecimal.movePointRight(2).setScale(0, UNNECESSARY).intValueExact()`转换为整数分。

## 十四、数据库升级历史

### V1.3

- 真实 `sys_user` 已包含 `openid`、`unionid`、`nickname`、`avatar`，无需新增字段。
- 微信手机号绑定复用现有 `sys_user.phone`。
- 已确认 `username varchar(50)`、`password`允许 NULL、`openid varchar(64)`。
- 已确认索引 `uk_openid`、`uk_username`和`idx_phone`。
- 手机号绑定不修改 `idx_phone`，不新增唯一索引。
- 本版本不新增 SQL，不修改历史 SQL，不写入伪造 openid。

### V1.2 第一阶段

- 新增`payment_order`支付流水表。
- 新增有效支付单生成列和支付号、交易号、通知号唯一索引。
- 新增脚本：`wuxin-paotui-server/src/main/resources/sql/12_create_payment_order.sql`。
- 脚本已人工执行，`payment_order`创建成功并通过 Mock 支付链路验证。

### V1.1

- 不新增业务表和业务字段。
- 新增排行榜聚合查询索引 `idx_order_status_deleted_finish_rider(status, deleted, finish_time, rider_id)`。
- 升级脚本：`11_add_rider_ranking_index.sql`。
- 脚本通过 `information_schema.STATISTICS` 同时检查索引名称和完整列顺序，避免同列不同名的重复索引，可重复执行。
- 升级脚本已人工执行，索引及排行榜 SQL 统计结果已验证通过。

### V1.0

- 新增 `order_item` 商品订单明细快照表。
- `order_info` 兼容新增 `order_type`、`store_id`、`product_amount`、`delivery_fee`、`total_amount`。
- 新增索引 `idx_order_type_user_deleted_create_time`。
- 新增索引 `idx_order_store_status_deleted_create_time`。
- 新增脚本：`wuxin-paotui-server/src/main/resources/sql/10_create_order_item_and_update_order.sql`。
- 脚本包含 `CREATE TABLE IF NOT EXISTS` 和字段、索引存在性检查，已在 Navicat 执行并通过结构及数据验证。
- 不修改已有订单数据；旧订单通过 `order_type` 默认值 `0` 保持跑腿订单语义。

### V0.9

- 新增 `shopping_cart` 购物车表。
- 新增唯一索引 `uk_user_product_deleted(user_id, product_id, is_deleted)`。
- 新增索引 `idx_user_store_deleted(user_id, store_id, is_deleted)`。
- 新增脚本：`wuxin-paotui-server/src/main/resources/sql/09_create_shopping_cart.sql`。
- 脚本使用 `CREATE TABLE IF NOT EXISTS`，已在 Navicat 执行并完成表结构、索引和逻辑删除验证。

### V0.8

- 新增 `merchant_category` 和 `merchant_product`。
- 新增店铺分类名称唯一索引，以及分类、商品状态和排序查询索引。
- 新增脚本：`wuxin-paotui-server/src/main/resources/sql/08_create_product_tables.sql`。
- 脚本使用 `CREATE TABLE IF NOT EXISTS`，已在 Navicat 手动执行并通过结构及数据验证。

### V0.7

- 新增 `merchant_info` 和 `merchant_store`。
- 新增商家用户唯一索引、商家审核状态索引、商家店铺唯一索引和店铺营业状态索引。
- 新增脚本：`wuxin-paotui-server/src/main/resources/sql/07_create_merchant_store.sql`。
- 脚本使用 `CREATE TABLE IF NOT EXISTS`，需要在 Navicat 手动执行。

### V0.6

- `order_info` 新增 `pay_status`、`pay_time`、`payment_no`。
- 新增索引 `idx_order_pay_status_deleted_create_time(pay_status, deleted, create_time)`。
- 新增唯一索引 `uk_order_payment_no(payment_no)`。
- 新增幂等升级脚本：`wuxin-paotui-server/src/main/resources/sql/06_update_order_payment.sql`。
- 脚本不包含业务数据更新语句，需要在 Navicat 手动执行。

### V0.5

- 用户取消订单复用现有 `status = 5`（已取消）和 `update_time` 字段。
- 骑手放弃订单复用现有字段，状态从 `1` 回退为 `0`，并清空 `rider_id`、`accept_time`。
- 新增 `order_comment` 表。
- 新增唯一索引 `uk_order_comment_order_id(order_id)`。
- 新增升级脚本：`wuxin-paotui-server/src/main/resources/sql/05_create_order_comment.sql`。

### V0.4

升级内容：

- `order_info` 新增 `accept_time`
- `order_info` 新增 `finish_time`
- 新增索引 `idx_order_rider_status_deleted`
- 新增索引 `idx_order_status_deleted_create_time`

升级脚本：

```text
sql/04_update_rider_accept_order.sql
```

说明：

- 当前升级脚本只升级 `order_info`。
- 不修改 `rider_info`。
- 不修改 `order_log`。
- 不修改已有数据。
