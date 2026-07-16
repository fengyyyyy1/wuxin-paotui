# 数据库文档

> 数据库：`wuxin_paotui`  
> 当前版本：V0.7（开发中）

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
| nickname | 昵称 |
| avatar | 头像 |
| status | 用户状态 |
| create_time | 创建时间 |
| update_time | 更新时间 |
| is_deleted | 逻辑删除 |

索引：

| 索引 | 说明 |
| --- | --- |
| PRIMARY KEY(id) | 主键 |
| username 唯一索引 | 用户名唯一 |

说明：

- 普通账号登录使用 `username` 和 `password`。
- 密码使用 BCrypt 加密保存。

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

说明：

- `accept_time` 在骑手接单成功后写入。
- `finish_time` 预留给骑手完成配送。
- 订单查询统一过滤 `deleted = 0`。
- 新订单默认 `pay_status = 0`，支付成功后写入支付时间和支付单号。
- `status` 表示配送业务状态，`pay_status` 表示支付状态，两者独立。
- 骑手大厅和骑手接单均要求 `pay_status = 1`。

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

## 九、数据库升级历史

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
