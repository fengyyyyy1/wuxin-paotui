# Postman 测试文档

> 当前版本：V1.4 商家订单管理模块
>
> 当前可进行本地固定映射Mock微信登录；真实微信联调需要小程序AppID和AppSecret。

## 一、环境变量

| 变量 | 示例 | 说明 |
| --- | --- | --- |
| `host` | `http://localhost:8080` | 本地后端地址 |
| `token` | 登录后返回的 token | JWT Token |
| `addressId` | `4` | `test001` 当前 V1.0 收货地址 ID |
| `orderId` | `2` | 当前测试订单 ID |
| `cancelOrderId` | `1` | 待取消的当前用户待接单订单 ID |
| `giveUpOrderId` | `4` | 当前骑手已接单且待放弃的订单 ID |
| `commentOrderId` | `4` | 当前用户已完成且未评价的订单 ID |
| `paymentOrderId` | `5` | 当前用户新发布的模拟支付订单 ID |
| `paymentNo` | 支付成功后返回 | 模拟支付单号 |
| `merchantId` | 商家申请后返回 | 当前用户商家主体 ID |
| `storeId` | 商家申请后返回 | 当前用户店铺 ID |
| `categoryId` | 分类创建后返回 | 当前测试商品分类 ID |
| `productId` | 商品创建后返回 | 当前测试商品 ID |
| `otherStoreProductId` | 其他店铺商品 ID | 跨店铺购物车冲突测试 |
| `cartId` | 加入购物车后返回 | 当前测试购物车记录 ID |
| `riderId` | 从 `rider_info.id` 查询 | V1.1 骑手个人统计 ID |
| `productOrderId` | 创建商品订单后返回 | V1.2待支付商品订单ID |
| `v12PaymentNo` | 创建JSAPI支付单后返回 | V1.2平台支付单号 |
| `wechatToken` | 微信登录后返回 | V1.3微信用户JWT |
| `wechatUserId` | 微信首次登录后返回 | V1.3微信测试用户ID |
| `merchantToken` | 商家账号登录后返回 | V1.4当前店铺商家JWT |
| `otherMerchantToken` | 其他商家账号登录后返回 | V1.4越权测试JWT |
| `merchantUsername` | 从真实数据库确认 | 当前商家登录名 |
| `merchantPassword` | 从TEST_ACCOUNT确认 | 当前商家测试密码 |
| `merchantOrderId` | 新建并支付的商品订单ID | 商家接单与出餐 |
| `merchantOrderNo` | 新建商品订单返回 | 商家订单号搜索 |
| `rejectMerchantOrderId` | 第二笔已支付商品订单ID | 商家拒单 |

登录后接口统一使用：

```http
Authorization: Bearer {{token}}
```

## 二、完整测试流程

### 1. 登录

请求：

```http
POST {{host}}/api/user/login
```

Authorization：不需要。

测试说明：

- 使用 `test001 / 123456` 登录。
- 将返回的 token 保存到 `{{token}}`。

预期结果：

- 返回 `code = 200`
- 返回 JWT Token

### 2. 新增地址

请求：

```http
POST {{host}}/api/user/address
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 新增当前登录用户地址。
- 必填收件人、手机号、详细地址。

预期结果：

- 返回 `code = 200`
- 数据库新增地址记录

### 3. 发布订单

请求：

```http
POST {{host}}/api/order/create
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 使用当前用户自己的取件地址和收件地址发布订单。

预期结果：

- 返回订单 ID
- `order_info.status = 0`
- `order_info.pay_status = 0`
- `order_info.pay_time` 和 `payment_no` 为 `NULL`
- 将订单 ID 保存到 `{{paymentOrderId}}`

### 4. 确认未支付订单不可接单

请求：

```http
GET {{host}}/api/rider/order/hall?pageNum=1&pageSize=50
```

Authorization：

```http
Bearer {{token}}
```

预期结果：

- 骑手大厅中不存在 `{{paymentOrderId}}`
- 直接请求 `POST {{host}}/api/rider/order/accept/{{paymentOrderId}}` 返回 `409 订单未支付`

### 5. 旧版模拟支付

请求：

```http
POST {{host}}/api/order/pay/{{paymentOrderId}}
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 不需要 Body。
- 订单必须属于当前用户、未支付且配送状态为 `0`。

预期结果：

- 返回 `message = 支付成功`
- 返回 `payStatus = 1`、`payStatusText = 已支付`
- 返回支付单号、订单金额和支付时间
- 将支付单号保存到 `{{paymentNo}}`
- 重复请求返回 `409 订单已支付`
- `order_log` 新增一条 `0 → 0` 的模拟支付日志

该接口仅用于历史开发测试，要求`wuxin.mock-payment.enabled=true`；生产环境默认关闭。

### 6. 我的订单

请求：

```http
GET {{host}}/api/order/my?pageNum=1&pageSize=10
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 查询当前用户发布的订单。
- 可追加 `status=0` 筛选。

预期结果：

- 返回分页结构
- 只返回当前用户订单
- 返回 `payStatus`、`payStatusText`、`payTime`、`paymentNo`

### 7. 订单详情

请求：

```http
GET {{host}}/api/order/2
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 查询当前用户自己的订单详情。

预期结果：

- 返回订单详情
- 不存在或无权限返回 `404 订单不存在`

### 8. 骑手大厅

请求：

```http
GET {{host}}/api/rider/order/hall?pageNum=1&pageSize=10
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 查询当前可由骑手接取且已支付的订单。

预期结果：

- 普通跑腿订单返回 `orderType = 0`（兼容历史 `NULL`）、`status = 0` 的订单
- 商品订单只返回 `orderType = 1`、`status = 7` 的已出餐订单
- 只返回 `payStatus = 1` 的订单
- 待商家接单的商品订单即使已支付也不会进入骑手大厅
- 按创建时间倒序

### 9. 骑手接单

请求：

```http
POST {{host}}/api/rider/order/accept/1
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 当前用户必须是骑手。
- 骑手需要满足 `audit_status = 1`、`rider_status = 1`。
- 订单必须已经支付。

预期结果：

- 返回 `message = 接单成功`
- `order_info.status` 更新为 `1`
- `order_info.rider_id` 写入
- `order_info.accept_time` 写入
- `order_log` 写入订单日志

### 10. 骑手我的订单

请求：

```http
GET {{host}}/api/rider/order/my?pageNum=1&pageSize=10
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 查询当前骑手已接订单。
- 可追加 `status=1` 筛选。

预期结果：

- 返回分页结构
- 返回当前骑手订单

### 11. 骑手完成配送

请求：

```http
POST {{host}}/api/rider/order/finish/2
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 当前用户必须是骑手。
- 订单必须属于当前骑手。
- 订单状态必须是 `1`（已接单）。

预期结果：

- 返回 `status = 3`
- 返回 `statusText = 待确认收货`
- 返回 `finishTime`
- `order_info.status` 更新为 `3`
- `order_info.finish_time` 写入
- `order_log` 写入完成配送日志

### 12. 用户确认收货

请求：

```http
POST {{host}}/api/order/confirm/2
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 当前用户必须是订单发布用户。
- 订单状态必须是 `3`（待确认收货）。
- 不需要 Body。

预期结果：

- 返回 `message = 确认收货成功`
- 返回 `status = 4`
- 返回 `statusText = 已完成`
- 返回 `confirmTime`
- `order_info.status` 更新为 `4`
- `order_info.update_time` 更新为确认时间
- `order_log` 写入用户确认收货日志

### 13. 用户取消订单

准备数据：

- 使用当前账号重新发布一个订单，确保订单属于当前用户且 `status = 0`。
- 将新订单 ID 保存到 `{{cancelOrderId}}`。

请求：

```http
POST {{host}}/api/order/cancel/{{cancelOrderId}}
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 不需要 Body。
- 仅当前用户发布且状态为 `0` 的订单允许取消。
- 使用相同订单 ID 再请求一次，验证重复取消保护。

预期结果：

- 首次请求返回 `message = 取消订单成功`
- 返回 `status = 5`、`statusText = 已取消` 和 `cancelTime`
- `order_info.status` 更新为 `5`
- `order_info.update_time` 与返回的 `cancelTime` 对应
- `order_log` 只新增一条用户取消日志
- 重复请求返回 `409 当前订单状态不可取消`

### 14. 骑手放弃订单

准备数据：

- 使用当前账号发布一个新订单，确保初始 `status = 0`。
- 使用当前骑手调用接单接口，使订单变为 `status = 1`。
- 将该订单 ID 保存到 `{{giveUpOrderId}}`。

请求：

```http
POST {{host}}/api/rider/order/give-up/{{giveUpOrderId}}
```

Authorization：

```http
Bearer {{token}}
```

测试说明：

- 不需要 Body。
- 当前账号必须对应审核通过且启用的骑手。
- 订单必须由当前骑手接取且状态为 `1`。
- 使用相同订单 ID 再请求一次，验证重复放弃保护。

预期结果：

- 首次请求返回 `message = 放弃订单成功`
- 返回 `status = 0`、`statusText = 待接单` 和 `giveUpTime`
- `order_info.rider_id`、`order_info.accept_time` 被清空
- 订单重新出现在骑手大厅
- `order_log` 只新增一条骑手放弃日志
- 重复请求返回 `409 当前订单状态不可放弃`

### 15. 用户评价订单

准备数据：

- 在 Navicat 手动执行 `wuxin-paotui-server/src/main/resources/sql/05_create_order_comment.sql`。
- 准备一个属于当前用户、`status = 4` 且尚未评价的订单。
- 将订单 ID 保存到 `{{commentOrderId}}`。

请求：

```http
POST {{host}}/api/order/comment
```

Authorization：

```http
Bearer {{token}}
```

Body：

```json
{
  "orderId": {{commentOrderId}},
  "score": 5,
  "content": "配送速度很快，服务很好。",
  "anonymous": 0
}
```

预期结果：

- 返回 `message = 评价成功`
- 返回评价 ID、订单 ID、评分和评价时间
- `order_comment` 新增一条评价
- `order_log` 新增一条 `4 → 4`、`operator_type = USER` 的评价日志
- 重复评价返回 `409 订单已评价`
- 非已完成订单返回 `409 当前订单状态不可评价`
- 评分不在 1～5、内容超过 500 字或匿名标识不合法时返回 `1004`

### 16. 订单轨迹

请求：

```http
GET {{host}}/api/order/timeline/{{orderId}}
```

Authorization：

```http
Bearer {{token}}
```

测试场景：

| 场景 | 预期轨迹 |
| --- | --- |
| 未支付订单 | 仅包含 `ORDER_CREATED` |
| 已支付待接单订单 | 包含 `ORDER_CREATED`、`ORDER_PAID` |
| 已完成订单 | 包含创建、支付、接单、配送完成、确认收货 |
| 已取消订单 | 包含创建和 `ORDER_CANCELLED`，已支付时还包含支付 |
| 骑手放弃订单 | 包含 `RIDER_GAVE_UP` |
| 已评价订单 | 包含 `ORDER_COMMENTED`，description 显示评分 |

检查要求：

- `timeline` 按 `time` 升序排列
- `sort` 从 1 连续递增
- 时间为空的节点不返回
- 其他用户的订单返回 `404 订单不存在`
- 不存在的订单返回 `404 订单不存在`
- 非法订单 ID 返回 `1004 参数错误`

### 17. 执行商家数据库升级

在 Navicat 手动执行：

```text
wuxin-paotui-server/src/main/resources/sql/07_create_merchant_store.sql
```

预期结果：创建 `merchant_info` 和 `merchant_store`，重复执行脚本不报错。

### 18. 商家申请入驻

请求：

```http
POST {{host}}/api/merchant/apply
Authorization: Bearer {{token}}
```

Body：

```json
{
  "merchantName": "五鑫便利店",
  "contactName": "李一",
  "contactPhone": "13800000000",
  "businessLicense": "license-url",
  "idCardFront": "front-url",
  "idCardBack": "back-url",
  "storeName": "五鑫便利店",
  "storeLogo": "logo-url",
  "storeDescription": "便利店、饮料和日常用品",
  "storePhone": "13800000000",
  "province": "重庆市",
  "city": "重庆市",
  "district": "渝北区",
  "detailAddress": "测试地址1号",
  "latitude": 29.0000000,
  "longitude": 106.0000000,
  "openTime": "08:00:00",
  "closeTime": "22:00:00"
}
```

预期结果：返回待审核状态及 `merchantId`、`storeId`；重复申请返回 `409 当前用户已申请商家入驻`。

### 19. 查询我的商家资料

```http
GET {{host}}/api/merchant/me
Authorization: Bearer {{token}}
```

预期结果：返回商家主体和店铺资料，不包含身份证图片及逻辑删除字段。

### 20. 手动通过商家审核

当前没有总控端审核接口，在 Navicat 执行：

```sql
UPDATE merchant_info
SET audit_status = 1,
    audit_remark = '测试审核通过'
WHERE id = {{merchantId}};
```

审核前调用店铺修改或营业状态接口应返回 `403 商家尚未通过审核或已被禁用`。

### 21. 修改店铺资料

```http
PUT {{host}}/api/merchant/store
Authorization: Bearer {{token}}
```

Body 使用完整店铺资料，不允许传入 `merchantId`、`businessStatus`、`storeStatus` 或审核状态。

预期结果：返回 `更新店铺资料成功`，且只能更新当前用户自己的店铺。

### 22. 修改营业状态

```http
PUT {{host}}/api/merchant/store/business-status
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "businessStatus": 1
}
```

预期结果：返回 `营业状态更新成功`；传入非 0/1 值返回 `1004`。

### 23. 公开店铺列表

```http
GET {{host}}/api/store/list?pageNum=1&pageSize=10&keyword=五鑫&district=渝北区&businessStatus=1
```

不携带 Authorization。预期返回审核通过、商家启用、店铺启用且未删除的分页数据。

### 24. 公开店铺详情

```http
GET {{host}}/api/store/{{storeId}}
```

不携带 Authorization。预期返回店铺详情；待审核、禁用、删除或不存在的店铺统一返回 `404 店铺不存在`。

### 25. 执行商品数据库升级

在 Navicat 手动执行：

```text
wuxin-paotui-server/src/main/resources/sql/08_create_product_tables.sql
```

预期结果：创建 `merchant_category` 和 `merchant_product`，重复执行脚本不报错。

### 26. 新增商品分类

```http
POST {{host}}/api/merchant/category
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "categoryName": "饮料",
  "sort": 1
}
```

预期结果：返回 `新增商品分类成功`，保存返回的 `categoryId`；同店铺重复名称返回 `409 商品分类名称已存在`。

### 27. 查询并修改商品分类

依次请求：

```http
GET {{host}}/api/merchant/category/list
PUT {{host}}/api/merchant/category/{{categoryId}}
PUT {{host}}/api/merchant/category/{{categoryId}}/status
Authorization: Bearer {{token}}
```

修改分类请求体使用 `{"categoryName":"饮品","sort":2}`，状态请求体使用 `{"status":0}` 或 `{"status":1}`。预期列表包含启用和禁用分类。

### 28. 新增商品

```http
POST {{host}}/api/merchant/product
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "categoryId": {{categoryId}},
  "productName": "可乐",
  "productImage": "product-url",
  "productDescription": "冰镇可乐",
  "price": 3.50,
  "originalPrice": 4.00,
  "stock": 100,
  "sort": 1
}
```

预期结果：商品默认 `productStatus = 0`、`sales = 0`，保存返回的 `productId`。

### 29. 查询并修改商品

```http
GET {{host}}/api/merchant/product/list?pageNum=1&pageSize=10&categoryId={{categoryId}}&productStatus=0&keyword=可乐
PUT {{host}}/api/merchant/product/{{productId}}
Authorization: Bearer {{token}}
```

修改请求体使用完整可修改字段。预期分页只返回当前店铺未删除商品，不能通过请求修改 `storeId`、`sales` 或商品状态。

### 30. 商品上下架

```http
PUT {{host}}/api/merchant/product/{{productId}}/status
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "productStatus": 1
}
```

预期结果：分类启用且库存大于 0 时返回 `商品上架成功`。分类禁用返回 `409 商品分类已禁用`，库存为 0 返回 `409 库存不足，商品不能上架`。

### 31. 公开浏览分类和商品

以下请求不携带 Authorization：

```http
GET {{host}}/api/store/{{storeId}}/categories
GET {{host}}/api/store/{{storeId}}/products?pageNum=1&pageSize=10&categoryId={{categoryId}}&keyword=可乐
GET {{host}}/api/store/product/{{productId}}
```

预期结果：只返回营业中店铺的启用分类下已上架、未删除且库存大于 0 的商品，不返回 `storeId`、逻辑删除和内部状态字段。

### 32. 删除保护和逻辑删除

1. 商品未删除时调用 `DELETE /api/merchant/category/{{categoryId}}`，预期返回 `409 分类下存在商品，不能删除`。
2. 调用 `DELETE /api/merchant/product/{{productId}}`，预期商品逻辑删除并下架。
3. 再删除分类，预期返回 `删除商品分类成功`。
4. 删除后管理列表和公开列表均不再返回对应记录。

### 33. 执行购物车数据库升级

在 Navicat 手动执行：

```text
wuxin-paotui-server/src/main/resources/sql/09_create_shopping_cart.sql
```

预期结果：创建 `shopping_cart` 及两个指定索引，重复执行脚本不报错。

### 34. 准备有效商品

确认测试商品满足：

- 商家审核通过且启用
- 店铺启用且 `merchant_store.business_status = 1`
- `merchant_category.status = 1` 且分类未删除
- 商品已上架且库存大于 0

### 35. 加入购物车

```http
POST {{host}}/api/cart/add
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "productId": {{productId}},
  "quantity": 1
}
```

预期结果：返回 `加入购物车成功`，保存 `cartId`，商品默认选中。

### 36. 重复加购与跨店铺约束

1. 再次加入相同商品，预期原记录数量累加，不新增重复记录。
2. 累加后数量超过库存，预期返回 `409 商品库存不足`。
3. 加入 `{{otherStoreProductId}}`，预期返回 `409 购物车中已存在其他店铺商品`。

### 37. 查询购物车

```http
GET {{host}}/api/cart/list
Authorization: Bearer {{token}}
```

检查：

- 返回最新店铺、商品、价格和库存
- `subtotal = price × quantity`
- `selectedTotalAmount` 仅统计有效且选中的商品
- `selectedProductCount` 为有效且选中商品数量合计

### 38. 修改购物车数量

```http
PUT {{host}}/api/cart/update
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "cartId": {{cartId}},
  "quantity": 2
}
```

预期结果：返回最新购物车商品；其他用户记录返回 `404 购物车不存在`，超过库存返回 409。

### 39. 修改选中状态

```http
PUT {{host}}/api/cart/selected
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "cartId": {{cartId}},
  "selected": 0
}
```

预期结果：选中状态更新。将商品下架或分类禁用后，失效商品不能重新选中。

### 40. 失效商品测试

分别在 Navicat 或商家接口调整商品状态、分类状态、店铺营业状态和库存，再查询购物车：

| 场景 | invalidReason |
| --- | --- |
| 商品删除 | 商品已删除 |
| 商品下架 | 商品已下架 |
| 分类禁用 | 商品分类已禁用 |
| 店铺停业 | 店铺已停业 |
| 库存小于购物车数量 | 商品库存不足 |

失效记录应继续保留，但不能计入选中总额。

### 41. 删除和清空购物车

```http
DELETE {{host}}/api/cart/{{cartId}}
DELETE {{host}}/api/cart/clear
Authorization: Bearer {{token}}
```

单个删除后重复删除应返回 `404 购物车不存在`。清空空购物车仍应返回成功。删除后重新加入同一商品应复用逻辑删除记录并正常成功。

### 42. V0.9 完整测试结果

正常流程：

| 测试项 | 结果 |
| --- | --- |
| 加入购物车 | 通过 |
| 同一商品数量累加 | 通过 |
| 列表实时商品数据与金额计算 | 通过 |
| 修改数量和选中状态 | 通过 |
| 单个逻辑删除和重复删除 | 通过 |
| 删除后重新加入 | 通过 |
| 清空购物车和空购物车清空 | 通过 |

异常流程：

| 测试项 | 结果 |
| --- | --- |
| 未登录访问 | `401`，通过 |
| 非法数量、选中状态和 ID | `400`，通过 |
| 购物车不存在或跨用户操作 | `404`，通过 |
| 跨店铺商品加入 | `409`，通过 |
| 商品下架 | `409`，通过 |
| 分类禁用 | `409`，通过 |
| 店铺停业 | `409`，通过 |
| 库存不足或累加超库存 | `409`，通过 |
| 失效商品重新选中 | 拒绝操作，通过 |

### 43. 执行 V1.0 数据库升级

在 Navicat 中选择 `wuxin_paotui` 数据库并执行：

```text
wuxin-paotui-server/src/main/resources/sql/10_create_order_item_and_update_order.sql
```

预期创建 `order_item`，扩展 `order_info` 的商品订单字段及查询索引。脚本重复执行不应报错，也不修改已有订单业务数据。

### 44. 准备 V1.0 结算数据

1. 使用 `POST /api/user/login` 获取并保存 `{{token}}`。
2. 确认 `{{addressId}}` 属于当前用户且未删除。
3. 确认商品已上架、分类启用、店铺启用且营业、商家审核通过且启用。
4. 调用 `POST /api/cart/add` 加入至少一个商品。
5. 如需验证未选商品保留，再加入同店铺第二个商品并通过 `PUT /api/cart/selected` 设置 `selected = 0`。

### 45. 购物车结算预览

```http
POST {{host}}/api/order/settlement/preview
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "deliveryAddressId": {{addressId}}
}
```

预期返回 `200`，`items` 只包含已选商品，`productAmount` 为商品小计之和，`deliveryFee = 0.00`，且库存、购物车和订单表均不发生变化。

### 46. 从购物车创建商品订单

```http
POST {{host}}/api/order/create-from-cart
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "deliveryAddressId": {{addressId}},
  "remark": "请尽快配送"
}
```

预期返回 `200 商品订单创建成功`。保存响应中的 `orderId`，并确认 `orderType = 1`、`payStatus = 0`、`status = 0`、`deliveryFee = 0.00`。

### 47. 商品订单详情与快照

```http
GET {{host}}/api/order/{{orderId}}
Authorization: Bearer {{token}}
```

预期返回店铺、金额和 `items`。在测试环境临时修改 `merchant_product` 的名称、图片或价格后再次查询，`items` 中的历史数据仍应保持下单时快照；验证后恢复商品数据。

### 48. 重复提交与购物车清理

创建成功后立即重复调用创建接口：

- 没有其他已选商品时返回 `409 购物车没有已选商品`
- 不新增重复订单、订单明细或订单日志
- 已选购物车项 `is_deleted = 1`
- 未选购物车项保持 `is_deleted = 0`
- 再次加入已结算商品时可恢复逻辑删除记录

### 49. 库存与事务回滚测试

1. 加入有效商品并完成结算预览。
2. 将商品库存调低到小于购物车数量。
3. 调用创建接口，预期返回 `409 商品库存不足`。
4. 确认没有残留 `order_info`、`order_item` 或 `order_log`，购物车仍保留。
5. 恢复商品库存。

并发测试可使用两个 Runner 请求同时提交同一用户购物车。预期最多一个请求成功，库存不为负数，只生成一套订单、明细和日志。

### 50. V1.0 Navicat 验证

```sql
SELECT id, order_no, user_id, order_type, store_id, delivery_address_id,
       price, product_amount, delivery_fee, total_amount, status, pay_status
FROM order_info
WHERE id = {{orderId}};

SELECT order_id, product_id, product_name, product_image,
       product_price, quantity, subtotal, is_deleted
FROM order_item
WHERE order_id = {{orderId}};

SELECT order_id, old_status, new_status, operator_id, operator_type, remark
FROM order_log
WHERE order_id = {{orderId}};

SELECT id, stock
FROM merchant_product
WHERE id = {{productId}};

SELECT id, product_id, quantity, selected, is_deleted
FROM shopping_cart
WHERE user_id = 2
ORDER BY id;
```

预期日志为 `old_status = 0`、`new_status = 0`、`operator_type = USER`、`remark = 用户从购物车创建商品订单`。

### 51. V1.0 完整测试结果

| 测试范围 | 结果 |
| --- | --- |
| 用户登录与 JWT | 通过 |
| 地址管理 | 通过 |
| 商品管理 | 通过 |
| 购物车 | 通过 |
| 商品订单 | 通过 |
| 骑手大厅与骑手接单 | 通过 |
| 我的订单与订单详情 | 通过 |
| 正常流程 | 通过 |
| 异常流程 | 通过 |
| 地址越权 | `404 收货地址不存在`，通过 |
| 订单越权 | 返回订单不存在或无权限，通过 |
| Navicat 数据一致性 | 通过 |

### 52. 执行 V1.1 排行榜索引升级

在 Navicat 中人工执行：

```text
wuxin-paotui-server/src/main/resources/sql/11_add_rider_ranking_index.sql
```

脚本只新增 `idx_order_status_deleted_finish_rider` 索引，不新增表、字段或测试数据，可重复执行。

### 53. 准备 V1.1 测试数据

先确认真实骑手和已完成订单，不得根据自增 ID 猜测：

```sql
SELECT id, user_id, real_name, audit_status, rider_status
FROM rider_info
ORDER BY id;

SELECT id, rider_id, status, finish_time, deleted
FROM order_info
WHERE rider_id IS NOT NULL
ORDER BY id DESC;
```

将存在的 `rider_info.id` 保存为 `{{riderId}}`。测试库完成订单不足时，仅在独立测试库准备最小数据：

- 至少 2 个真实 `rider_info` 骑手。
- 每个骑手至少 1 条 `status = 4`、`deleted = 0`、`finish_time` 非空的订单。
- 为验证时间边界，可分别准备今日、本周、本月之前的完成时间。
- 为验证过滤规则，可准备 `status != 4` 和 `deleted = 1` 的对照订单。
- 为验证稳定排序，使两个骑手完成单量相同，但最早 `finish_time` 不同。

不要在正式数据中直接插入或改造测试订单。

### 54. 今日排行榜查询成功

```http
GET {{host}}/api/rider/ranking?type=today&limit=10
Authorization: Bearer {{token}}
```

预期：`code = 200`；只统计当前自然日 `finish_time` 范围内的已完成未删除订单；`rank` 从 1 连续递增。

### 55. 本周、本月和累计排行榜

依次请求：

```http
GET {{host}}/api/rider/ranking?type=week&limit=10
GET {{host}}/api/rider/ranking?type=month&limit=10
GET {{host}}/api/rider/ranking?type=total&limit=10
Authorization: Bearer {{token}}
```

预期：

- 周榜统计周一 00:00:00 至下周一 00:00:00。
- 月榜统计本月 1 日 00:00:00 至下月 1 日 00:00:00。
- 累计榜统计全部 `status = 4`、`rider_id` 非空、`deleted = 0` 的订单。

### 56. limit 边界与默认值

依次请求：

```http
GET {{host}}/api/rider/ranking?type=today
GET {{host}}/api/rider/ranking?type=today&limit=1
GET {{host}}/api/rider/ranking?type=today&limit=100
GET {{host}}/api/rider/ranking?type=today&limit=0
GET {{host}}/api/rider/ranking?type=today&limit=101
```

预期：省略 `limit` 时按 10；`1` 和 `100` 成功；`0` 和 `101` 返回 `400`。

### 57. 非法排行榜类型

```http
GET {{host}}/api/rider/ranking?type=year&limit=10
Authorization: Bearer {{token}}
```

预期：`400 排行榜类型参数错误`。

### 58. 空榜与统计过滤

在没有符合当前时间范围订单的测试环境查询对应榜单，预期 `data = []`。

分别核对：

- `status != 4` 的订单不计入。
- `deleted = 1` 的订单不计入。
- 今日榜依据 `finish_time`，不依据 `create_time` 或 `accept_time`。

### 59. 稳定排序

准备两个完成单量相同的骑手后查询同一榜单。预期先比较周期内最早 `finish_time`，更早者在前；仍相同时 `riderId` 小者在前；名次仍为连续的 1、2、3。

### 60. 骑手个人统计

```http
GET {{host}}/api/rider/{{riderId}}/statistics
Authorization: Bearer {{token}}
```

预期返回 `todayCompletedCount`、`weekCompletedCount`、`monthCompletedCount`、`totalCompletedCount`，统计口径与排行榜一致。

不存在骑手测试：

```http
GET {{host}}/api/rider/999999999/statistics
Authorization: Bearer {{token}}
```

预期：`404 骑手不存在`。

### 61. Navicat 结果一致性

将以下时间替换为当前测试周期的真实左闭右开边界：

```sql
SELECT o.rider_id,
       COUNT(*) AS completed_order_count,
       MIN(o.finish_time) AS earliest_finish_time
FROM order_info o
WHERE o.status = 4
  AND o.rider_id IS NOT NULL
  AND o.deleted = 0
  AND o.finish_time >= '2026-07-17 00:00:00'
  AND o.finish_time < '2026-07-18 00:00:00'
GROUP BY o.rider_id
ORDER BY completed_order_count DESC,
         earliest_finish_time ASC,
         o.rider_id ASC
LIMIT 10;
```

将结果与今日排行榜逐项比较；周榜和月榜只替换时间边界，累计榜删除两个 `finish_time` 条件。

V1.1 人工验收结果：

| 编号 | 场景 | 当前状态 |
| --- | --- | --- |
| 1 | 今日排行榜成功 | 通过 |
| 2 | 本周排行榜成功 | 通过 |
| 3 | 本月排行榜成功 | 通过 |
| 4 | 累计排行榜成功 | 通过 |
| 5 | limit 默认值 | 通过 |
| 6 | limit=1 | 通过 |
| 7 | limit=100 | 通过 |
| 8 | limit=0 参数异常 | 通过 |
| 9 | limit=101 参数异常 | 通过 |
| 10 | 非法 type | 通过 |
| 11 | 无完成订单返回空数组 | 通过 |
| 12 | 只统计 status=4 | 通过 |
| 13 | 不统计逻辑删除订单 | 通过 |
| 14 | 今日榜按 finish_time | 通过 |
| 15 | 同单量稳定排序 | 通过 |
| 16 | 存在骑手个人统计 | 通过 |
| 17 | 不存在骑手 | 通过 |
| 18 | API 与数据库 SQL 一致 | 通过 |

### 62. 准备V1.2本地配置

参考`application-local.example.yml`创建未跟踪的`application-local.yml`，或设置：

```text
SPRING_PROFILES_ACTIVE=local
MOCK_PAYMENT_ENABLED=true
WECHAT_PAY_ENABLED=false
DB_URL=本地数据库连接
DB_USERNAME=本地数据库账号
DB_PASSWORD=本地数据库密码
```

不得在项目文件中写入真实商户密钥、证书或APIv3密钥。

### 63. 执行V1.2数据库脚本

在Navicat中人工执行：

```text
wuxin-paotui-server/src/main/resources/sql/12_create_payment_order.sql
```

确认`payment_order`、生成列`active_order_id`及全部索引创建成功。

### 64. 创建商品订单支付单

准备`order_type=1`、`status=0`、`pay_status=0`且`total_amount>0`的当前用户商品订单。

```http
POST {{host}}/api/payment/wechat/jsapi
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "orderId": {{productOrderId}}
}
```

预期返回非空`paymentNo、timeStamp、nonceStr、packageValue、signType、paySign`，将支付单号保存到`{{v12PaymentNo}}`。

```sql
SELECT payment_no, order_id, user_id, payment_channel, trade_type,
       amount_total, status, prepay_id, active_order_id
FROM payment_order
WHERE order_id = {{productOrderId}}
ORDER BY id DESC;

SELECT id, pay_status, pay_time, payment_no
FROM order_info
WHERE id = {{productOrderId}};
```

预期流水`status=1`，订单仍为`pay_status=0`。

### 65. 重复创建支付单

重复创建请求，预期复用同一`paymentNo`，数据库只有一条`CREATED/WAITING_PAY`有效流水。

### 66. Mock确认支付成功

```http
POST {{host}}/api/payment/mock/{{v12PaymentNo}}/success
Authorization: Bearer {{token}}
```

预期流水变为`SUCCESS(2)`，订单`pay_status=1`，正确写入`pay_time、payment_no`和一条“订单支付成功”日志。

### 67. 重复确认幂等

重复执行Mock确认，预期仍返回成功；流水、订单不重复更新，订单日志不增加第二条。

### 68. 查询支付状态

```http
GET {{host}}/api/payment/order/{{productOrderId}}/status
Authorization: Bearer {{token}}
```

预期返回订单支付状态、流水状态、支付单号、交易号、整数分金额和成功时间。查询没有支付流水的本人订单时正常返回订单`payStatus`，流水字段为`null`。

### 69. 权限、状态和金额测试

- 其他用户不能创建该订单支付单。
- 其他用户不能查询该订单支付状态。
- 其他用户不能确认该支付单。
- 已支付订单不能重复创建支付。
- `order_type=0`跑腿订单暂不支持新支付架构。
- `total_amount`为空、零或负数时拒绝创建。
- 不存在`paymentNo`返回`404 支付单不存在`。
- 流水金额与订单金额不一致时，确认失败且订单不更新。

### 70. 环境开关测试

设置`MOCK_PAYMENT_ENABLED=false`并重新启动人工测试环境：

- `POST /api/order/pay/{id}`返回`403 模拟支付未启用`。
- `POST /api/payment/mock/{paymentNo}/success`不注册。
- 真实微信支付未启用时，创建支付返回支付网关未启用。

第一阶段未注册`POST /api/payment/wechat/notify`，不得通过任意回调请求修改订单。

### 71. V1.2人工验收清单

| 编号 | 测试项 | 状态 |
| --- | --- | --- |
| 1 | 创建商品订单支付单成功 | 通过 |
| 2 | 返回paymentNo和JSAPI模拟参数 | 通过 |
| 3 | payment_order写入WAITING_PAY | 通过 |
| 4 | 创建支付不修改order_info.pay_status | 通过 |
| 5 | 重复创建复用有效支付单 | 通过 |
| 6 | 模拟确认支付成功 | 通过 |
| 7 | payment_order更新为SUCCESS | 通过 |
| 8 | order_info.pay_status更新为1 | 通过 |
| 9 | pay_time和payment_no正确 | 通过 |
| 10 | 订单日志只写一次 | 通过 |
| 11 | 重复确认幂等 | 通过 |
| 12 | 查询支付状态成功 | 通过 |
| 13 | 查询无支付流水订单 | 通过 |
| 14 | 非订单用户不能创建支付 | 通过 |
| 15 | 非订单用户不能查询支付 | 通过 |
| 16 | 非订单用户不能模拟确认 | 通过 |
| 17 | 已支付订单不能重复创建 | 通过 |
| 18 | 非商品订单暂不支持支付 | 通过 |
| 19 | 非法订单金额被拒绝 | 通过 |
| 20 | Mock关闭时旧接口不可用 | 通过 |
| 21 | Mock关闭时新确认接口不可用 | 通过 |
| 22 | 不存在paymentNo | 通过 |
| 23 | 流水与订单金额不一致 | 通过 |
| 24 | 并发请求不生成多条有效流水 | 通过 |

### 72. 准备V1.3本地配置

IDEA环境变量：

```text
MOCK_WECHAT_LOGIN_ENABLED=true
WECHAT_MINI_PROGRAM_ENABLED=false
```

两个开关默认均为`false`。`prod` Profile即使误开Mock也会返回配置错误。

### 73. Mock微信新用户首次登录

```http
POST {{host}}/api/user/wechat/login
Content-Type: application/json
```

```json
{
  "code": "mock-code-new-user"
}
```

该接口不携带旧JWT。预期返回`newUser=true`、JWT和用户信息，将token保存为`wechatToken`，用户ID保存为`wechatUserId`。

BCrypt回归检查：

- 自动注册原始随机密码使用单个UUID，为36个UTF-8字节。
- 不再出现`password cannot be more than 72 bytes`。
- `sys_user.password`应为60字符BCrypt密文，不得等于任何固定默认密码。

### 74. 重复登录幂等

分别重复使用：

```json
{
  "code": "mock-code-new-user"
}
```

```json
{
  "code": "mock-code-new-user-repeat"
}
```

两个code映射同一Mock openid。预期返回相同`userId`、`newUser=false`，数据库不新增第二个用户。

### 75. 微信JWT验证

```http
GET {{host}}/api/user/me
Authorization: Bearer {{wechatToken}}
```

预期返回`id、username、nickname、avatar、phone`，不得返回`password、openid、unionid、session_key、is_deleted`。

### 76. 网关关闭和冲突测试

两者都关闭：

```text
MOCK_WECHAT_LOGIN_ENABLED=false
WECHAT_MINI_PROGRAM_ENABLED=false
```

预期：`503 微信登录未启用`。

两者同时开启：

```text
MOCK_WECHAT_LOGIN_ENABLED=true
WECHAT_MINI_PROGRAM_ENABLED=true
```

预期：`500 微信登录配置错误`，不得调用微信网络。

### 77. 参数与无效code测试

空code：

```json
{
  "code": ""
}
```

预期参数错误。

无效Mock code：

```json
{
  "code": "invalid-mock-code"
}
```

预期：`400 微信登录凭证无效`。

### 78. 兼容与安全测试

- 普通`POST /api/user/login`继续成功。
- `POST /api/user/register`继续成功。
- 禁用微信用户后登录返回`403 当前账号已被禁用`。
- 并发调用同一Mock身份只生成一个`sys_user`。
- 自动用户密码是BCrypt密文，不是固定明文。
- 自动用户名唯一，且不包含完整openid。
- 应用日志不得出现AppSecret、完整code、session_key或完整openid。
- `mock-code-test001`创建独立微信用户，不得绑定现有`test001`。

### 79. V1.3 Navicat验证

查询微信用户：

```sql
SELECT
    id,
    username,
    openid,
    unionid,
    nickname,
    avatar,
    status,
    create_time,
    update_time,
    is_deleted
FROM sys_user
WHERE openid IS NOT NULL
ORDER BY id DESC;
```

验证openid唯一：

```sql
SELECT openid, COUNT(*) AS count
FROM sys_user
WHERE openid IS NOT NULL
  AND is_deleted = 0
GROUP BY openid
HAVING COUNT(*) > 1;
```

验证用户名唯一：

```sql
SELECT username, COUNT(*) AS count
FROM sys_user
GROUP BY username
HAVING COUNT(*) > 1;
```

检查密码不是明文：

```sql
SELECT id, username, password
FROM sys_user
WHERE openid IS NOT NULL
ORDER BY id DESC;
```

不要在公开截图或聊天中发送完整openid、unionid或密码哈希。

### 80. V1.3人工验收清单

| 编号 | 测试项 | 状态 |
| --- | --- | --- |
| 1 | Mock微信新用户首次登录成功 | 通过 |
| 2 | 返回JWT | 通过 |
| 3 | 首次返回newUser=true | 通过 |
| 4 | sys_user新增一条openid用户 | 通过 |
| 5 | password不是明文固定密码 | 通过 |
| 6 | username唯一且不暴露完整openid | 通过 |
| 7 | 相同Mock身份第二次登录 | 通过 |
| 8 | 第二次返回相同userId | 通过 |
| 9 | 第二次newUser=false | 通过 |
| 10 | 不重复新增sys_user | 通过 |
| 11 | 微信JWT调用/api/user/me | 通过 |
| 12 | 微信登录接口无需旧JWT | 通过 |
| 13 | 两个网关关闭 | 通过 |
| 14 | 两个网关同时开启 | 通过 |
| 15 | code为空 | 通过 |
| 16 | 无效Mock code | 通过 |
| 17 | 禁用用户拒绝登录 | 通过 |
| 18 | 普通密码登录兼容 | 通过 |
| 19 | 用户注册兼容 | 通过 |
| 20 | /api/user/me不泄露微信敏感字段 | 通过 |
| 21 | 并发同openid不生成重复用户 | 通过 |
| 22 | 日志无敏感信息 | 通过 |

本地单元回归已通过：

- `mock-code-new-user`首次登录只插入一条用户记录。
- 重复登录返回相同userId和`newUser=false`。
- 随机密码连续生成100次均为36个UTF-8字节且不重复。
- 保存对象中的密码为60字符BCrypt密文。

### 81. 微信用户登录

```http
POST {{host}}/api/user/wechat/login
Content-Type: application/json
```

```json
{
  "code": "mock-code-new-user"
}
```

保存返回token为`wechatToken`。当前人工验收用户为`userId=3`。

### 82. 获取Profile

```http
GET {{host}}/api/user/profile
Authorization: Bearer {{wechatToken}}
```

预期返回`id、username、nickname、avatar、phone、gender`，不返回微信标识、密码或删除状态。

### 83. 修改Profile

```http
PUT {{host}}/api/user/profile
Authorization: Bearer {{wechatToken}}
Content-Type: application/json
```

```json
{
  "nickname": "悠悠球",
  "avatar": "https://example.com/avatar.png",
  "gender": 0
}
```

预期返回`200 成功`。接口不得接收或修改username、openid、unionid、phone、password、status。

### 84. 再次获取Profile

重新执行`GET /api/user/profile`，确认昵称、头像和性别已更新，username和phone保持不变。

### 85. Profile异常测试

- 不携带JWT，预期`401`。
- `gender=-1`或`gender=3`，预期参数错误。
- `gender`缺失，预期参数错误。
- nickname超过30字符，预期参数错误。
- avatar超过真实数据库安全上限255字符，预期参数错误。
- 请求体携带username、openid、phone等额外字段不会修改对应数据库字段。

### 86. Profile人工验收清单

| 编号 | 测试项 | 状态 |
| --- | --- | --- |
| 1 | 微信JWT获取Profile | 通过 |
| 2 | Profile返回六个允许字段 | 通过 |
| 3 | 修改昵称和头像成功 | 通过 |
| 4 | 修改gender成功 | 通过 |
| 5 | 再次查询数据一致 | 通过 |
| 6 | 未登录返回401 | 通过 |
| 7 | gender范围校验 | 通过 |
| 8 | nickname长度校验 | 通过 |
| 9 | avatar长度校验 | 通过 |
| 10 | 禁止字段未被修改 | 通过 |

### 87. 准备微信手机号Mock配置

IDEA环境变量：

```text
MOCK_WECHAT_PHONE_ENABLED=true
```

`application.yml`默认值必须保持`false`。手机号绑定接口需要JWT，不加入`WebMvcConfig`白名单。`prod` Profile禁止使用Mock手机号网关。

先执行微信登录并保存`userId=3`对应JWT：

```http
POST {{host}}/api/user/wechat/login
Content-Type: application/json
```

```json
{
  "code": "mock-code-new-user"
}
```

### 88. 首次绑定手机号

```http
POST {{host}}/api/user/phone/bind
Authorization: Bearer {{wechatToken}}
Content-Type: application/json
```

```json
{
  "code": "mock-phone-code-13800000003"
}
```

预期返回`200 手机号绑定成功`，`data.phone=13800000003`，并返回`id、username、nickname、avatar、phone、gender`。

### 89. Profile回查

```http
GET {{host}}/api/user/profile
Authorization: Bearer {{wechatToken}}
```

预期`phone=13800000003`，其他用户资料保持不变。

### 90. 重复绑定幂等

再次提交：

```json
{
  "code": "mock-phone-code-13800000003"
}
```

预期仍返回成功，不新增用户、不修改其他字段。

### 91. 更换手机号

```json
{
  "code": "mock-phone-code-13900000003"
}
```

预期返回`phone=13900000003`。再次查询Profile，确认数据库回查一致；应用日志只能出现`139****0003`，不能出现完整授权code。

### 92. 手机号绑定异常测试

| 场景 | 操作 | 预期 |
| --- | --- | --- |
| 无效code | 提交`mock-phone-code-invalid` | `400 微信手机号授权凭证无效` |
| 空code | 提交空字符串或空白 | 参数错误 |
| code过长 | 提交超过128字符 | 参数错误 |
| 未登录 | 不携带JWT | `401 未登录或登录已过期` |
| Mock关闭 | `MOCK_WECHAT_PHONE_ENABLED=false` | `503 微信手机号绑定未启用` |
| 他人手机号 | 先由另一未删除用户占用目标手机号 | `409 手机号已绑定其他用户` |
| 生产误开Mock | `prod` Profile开启Mock | `403 生产环境禁止使用模拟微信手机号服务` |

冲突测试只使用测试数据，并在测试结束后恢复原值；不要修改数据库结构或增加唯一索引。

### 93. 手机号绑定Navicat验证

```sql
SELECT
    id,
    username,
    phone,
    nickname,
    avatar,
    gender,
    status,
    update_time,
    is_deleted
FROM sys_user
WHERE id = 3;
```

检查当前未删除用户是否存在重复手机号：

```sql
SELECT phone, COUNT(*) AS user_count
FROM sys_user
WHERE phone IS NOT NULL
  AND phone <> ''
  AND is_deleted = 0
GROUP BY phone
HAVING COUNT(*) > 1;
```

当前`idx_phone`是普通索引，以上查询用于验收和数据治理检查，不代表数据库具备手机号唯一约束。

### 94. 手机号绑定人工验收清单

| 编号 | 测试项 | 状态 |
| --- | --- | --- |
| 1 | 微信JWT首次绑定手机号成功 | 通过 |
| 2 | UserInfoVO返回正确手机号 | 通过 |
| 3 | Profile回查一致 | 通过 |
| 4 | 重复绑定相同手机号幂等成功 | 通过 |
| 5 | 更换为未占用手机号成功 | 通过 |
| 6 | 他人手机号冲突返回409 | 通过 |
| 7 | 无效code返回业务错误 | 通过 |
| 8 | Mock关闭返回503 | 通过 |
| 9 | 未登录返回401 | 通过 |
| 10 | 其他用户字段未被修改 | 通过 |
| 11 | 日志只记录脱敏手机号 | 通过 |
| 12 | 不出现服务器内部错误 | 通过 |

### 95. V1.4测试准备

完整配送链路按以下顺序执行：

```text
用户登录
→ 加入购物车
→ 创建店铺商品订单
→ 创建支付单
→ Mock支付成功
→ 商家订单分页
→ 商家订单详情
→ 商家接单
→ 商家出餐
→ 骑手大厅
→ 骑手接单
```

商家拒单必须另外创建一笔“已支付、待商家接单”的订单，不能把同一订单同时用于
完整配送链路和拒单链路。

先在Navicat执行：

```text
wuxin-paotui-server/src/main/resources/sql/13_update_order_for_merchant_management.sql
```

IDEA本地环境变量：

```text
MOCK_PAYMENT_ENABLED=true
WECHAT_PAY_ENABLED=false
```

确认以下测试数据真实存在：

- 普通下单用户及有效收货地址。
- 已审核通过且启用的商家、有效店铺。
- 店铺内启用分类、已上架且库存充足的商品。
- 审核通过且启用的骑手。
- 如需越权测试，准备第二个已审核商家账号。

不得根据自增顺序猜测`storeId、productId、merchantId`。

### 96. 获取普通用户Token

```http
POST {{host}}/api/user/login
Content-Type: application/json
```

```json
{
  "username": "test001",
  "password": "123456"
}
```

保存为`token`。

### 97. 获取商家Token

使用`TEST_ACCOUNT.md`中经真实数据库确认的商家账号：

```http
POST {{host}}/api/user/login
Content-Type: application/json
```

```json
{
  "username": "{{merchantUsername}}",
  "password": "{{merchantPassword}}"
}
```

保存为`merchantToken`。执行`GET /api/merchant/me`确认返回的`storeId`与商品所属店铺一致。

### 98. 创建并支付商品订单

先加入当前商家店铺商品：

```http
POST {{host}}/api/cart/add
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "productId": {{productId}},
  "quantity": 1
}
```

创建订单：

```http
POST {{host}}/api/order/create-from-cart
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "deliveryAddressId": {{addressId}},
  "remark": "V1.4商家订单验收"
}
```

保存返回`orderId`为`merchantOrderId`，保存`orderNo`为`merchantOrderNo`。

创建支付单：

```http
POST {{host}}/api/payment/wechat/jsapi
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "orderId": {{merchantOrderId}}
}
```

保存`paymentNo`，再执行：

```http
POST {{host}}/api/payment/mock/{{paymentNo}}/success
Authorization: Bearer {{token}}
```

预期`order_info.pay_status=1`，订单`status=0`，状态文字为“待商家接单”。

### 99. 商家订单分页

```http
GET {{host}}/api/merchant/order/page?pageNum=1&pageSize=10&status=0
Authorization: Bearer {{merchantToken}}
```

预期：

- 只返回当前商家店铺商品订单。
- 包含`merchantOrderId`。
- `statusName=待商家接单`。
- 收件手机号格式为`138****0003`。
- 按创建时间倒序。

继续测试：

```http
GET {{host}}/api/merchant/order/page?keyword={{merchantOrderNo}}
GET {{host}}/api/merchant/order/page?keyword=商品快照名称
GET {{host}}/api/merchant/order/page?startTime=2026-07-18T00:00:00&endTime=2026-07-19T00:00:00
```

`pageNum=0`、`pageSize=101`、非法`status`、开始时间不早于结束时间均应返回参数错误。

### 100. 商家订单详情

```http
GET {{host}}/api/merchant/order/{{merchantOrderId}}
Authorization: Bearer {{merchantToken}}
```

预期返回订单金额、脱敏手机号、收货地址、`order_item`商品快照和`order_log`轨迹，不返回密码、openid、unionid或JWT。

### 101. 出餐前骑手大厅隔离

```http
GET {{host}}/api/rider/order/hall?pageNum=1&pageSize=50
Authorization: Bearer {{token}}
```

预期商品订单`merchantOrderId`尚未出现。普通已支付待接单跑腿订单仍正常显示。

### 102. 商家接单

```http
POST {{host}}/api/merchant/order/{{merchantOrderId}}/accept
Authorization: Bearer {{merchantToken}}
```

预期：

- `status=6`
- `statusName=商家已接单，制作中`
- `merchantAcceptTime`非空
- `order_log`新增一条`MERCHANT`、`0 → 6`、`商家接单`

立即重复请求，预期`409 当前订单状态不允许商家操作`，日志不增加第二条。

### 103. 商家出餐

```http
POST {{host}}/api/merchant/order/{{merchantOrderId}}/ready
Authorization: Bearer {{merchantToken}}
```

预期：

- `status=7`
- `statusName=已出餐，待骑手接单`
- `merchantAcceptTime`为商家接单时的真实时间
- `readyTime`非空
- `rejectTime`和`rejectReason`读取订单真实数据，未拒单时为`null`
- `order_log`新增一条`6 → 7`、`商家出餐`

立即重复请求，预期`409`且日志不重复。

### 104. 出餐后骑手接单

重新查询骑手大厅，预期出现`merchantOrderId`。

```http
POST {{host}}/api/rider/order/accept/{{merchantOrderId}}
Authorization: Bearer {{token}}
```

预期商品订单`7 → 1`。骑手放弃该商品订单时应回到`status=7`，不能回到商家待接单状态。

### 105. 商家拒单

按步骤98再创建并支付第二笔商品订单，保存为`rejectMerchantOrderId`。
本轮人工验收使用`rejectMerchantOrderId = 8`，完整配送订单`7`不得复用。

```http
POST {{host}}/api/merchant/order/{{rejectMerchantOrderId}}/reject
Authorization: Bearer {{merchantToken}}
Content-Type: application/json
```

```json
{
  "reason": "商品暂时缺货"
}
```

预期：

- `status=8`
- `statusName=已关闭，待退款`
- `merchant_reject_time`和原因正确
- `pay_status`仍为`1`
- `payment_order`不得变成退款成功
- 订单日志新增`0 → 8`、`商家拒单：商品暂时缺货`

重复拒单返回`409`，日志不重复。空原因、1字符原因或超过200字符返回参数错误。

订单`8`人工验收返回：

```json
{
  "code": 200,
  "message": "商家拒单已受理，等待退款处理",
  "data": {
    "orderId": 8,
    "status": 8,
    "statusName": "已关闭，待退款",
    "merchantAcceptTime": null,
    "readyTime": null,
    "rejectTime": "2026-07-18T18:16:59.722493",
    "rejectReason": "商品暂时缺货"
  }
}
```

该结果仅表示订单已关闭并等待退款处理，真实退款功能尚未实现。

### 106. 权限与异常测试

- 普通用户Token访问`GET /api/merchant/order/page`，预期`404 商家信息不存在`。
- 其他商家Token查询或操作当前订单，预期`404 订单不存在或无权限`。
- 未支付商品订单接单或拒单，预期`409 订单未支付，商家不可操作`。
- 普通跑腿订单使用商家接口，预期`404 订单不存在或无权限`。
- 未接单商品订单直接出餐，预期`409 当前订单状态不允许商家操作`。
- 不携带JWT访问任意商家订单接口，预期`401`。

### 107. V1.4 Navicat验证

检查订单状态和商家时间：

```sql
SELECT
    id,
    order_no,
    order_type,
    store_id,
    status,
    pay_status,
    merchant_accept_time,
    merchant_ready_time,
    merchant_reject_time,
    merchant_reject_reason,
    rider_id,
    accept_time,
    update_time,
    deleted
FROM order_info
WHERE id IN ({{merchantOrderId}}, {{rejectMerchantOrderId}});
```

检查商家日志：

```sql
SELECT
    id,
    order_id,
    old_status,
    new_status,
    operator_id,
    operator_type,
    remark,
    create_time
FROM order_log
WHERE order_id IN ({{merchantOrderId}}, {{rejectMerchantOrderId}})
ORDER BY id;
```

检查商品快照：

```sql
SELECT
    order_id,
    product_id,
    product_name,
    product_price,
    quantity,
    subtotal,
    is_deleted
FROM order_item
WHERE order_id IN ({{merchantOrderId}}, {{rejectMerchantOrderId}})
ORDER BY order_id, id;
```

检查字段和索引：

```sql
SHOW COLUMNS FROM order_info LIKE 'merchant_%';
SHOW INDEX FROM order_info
WHERE Key_name = 'idx_order_store_type_deleted_create_time';
```

### 108. V1.4人工验收清单

人工验收时间：2026-07-18

完整配送链路验收订单：

| 项 | 值 |
| --- | --- |
| orderId | `7` |
| orderNo | `WX20260718173934516783` |
| paymentNo | `PAY20260718174303093cf8566e8141b1ae648649340679c0` |

拒单链路验收订单：

| 项 | 值 |
| --- | --- |
| orderId | `8` |
| orderNo | `WX20260718180441574851` |
| storeId | `1` |
| userId | `2` |
| paymentNo | `PAY2026071818153359038b94fc2a4cfc918b822dd4611cd1` |
| payTime | `2026-07-18 18:15:54` |
| rejectTime | `2026-07-18T18:16:59.722493` |
| 最终状态 | `8 已关闭，待退款` |

验收状态流：

```text
0 待商家接单
→ 6 商家已接单，制作中
→ 7 已出餐，待骑手接单
→ 1 骑手已接单
→ 3 待用户确认
→ 4 已完成

商家拒单：0 → 8
```

| 编号 | 测试项 | 状态 |
| --- | --- | --- |
| 1 | 执行13号增量SQL | 通过 |
| 2 | 商家订单分页与筛选 | 通过 |
| 3 | 店铺订单权限隔离 | 通过 |
| 4 | 商家订单详情和商品快照 | 通过 |
| 5 | 手机号脱敏 | 通过 |
| 6 | 商家接单与重复接单 | 通过 |
| 7 | 商家拒单与待退款状态 | 通过 |
| 8 | 商家出餐与重复出餐 | 通过 |
| 9 | 出餐前不进入骑手大厅 | 通过 |
| 10 | 出餐后进入骑手大厅 | 通过 |
| 11 | 普通跑腿订单兼容 | 通过 |
| 12 | 非商家与跨店铺越权 | 通过 |
| 13 | order_log不重复 | 通过 |
| 14 | Navicat字段与索引核对 | 通过 |

最终商家订单详情已确认：

- `merchantAcceptTime`正常存在
- `readyTime`正常存在
- 时间线包含用户创建订单、订单支付成功、商家接单、商家出餐和骑手接单
- 订单`8`的`merchant_reject_time`和`merchant_reject_reason`正确落库
- 真实退款尚未实现，拒单后`pay_status`仍为`1`

## 三、异常测试

| 场景 | 预期结果 |
| --- | --- |
| 未登录访问 | `401 未登录或登录已过期` |
| 非骑手访问骑手接口 | `403 当前用户不是骑手` |
| 订单不存在 | `404 订单不存在` |
| 重复接单 | `409 订单已被其他骑手接单` |
| 状态不可接单 | `409 当前订单状态不可接单` |
| 状态不可完成配送 | `409 当前订单状态不可完成配送` |
| 状态不可确认收货 | `409 当前订单状态不可确认收货` |
| 状态不可取消或重复取消 | `409 当前订单状态不可取消` |
| 状态不可放弃或重复放弃 | `409 当前订单状态不可放弃` |
| 非已完成订单评价 | `409 当前订单状态不可评价` |
| 重复评价 | `409 订单已评价` |
| 重复支付 | `409 订单已支付` |
| 业务状态不可支付 | `409 当前订单状态不可支付` |
| 未支付订单接单 | `409 订单未支付` |
| 查看其他用户订单轨迹 | `404 订单不存在` |
| 重复申请商家 | `409 当前用户已申请商家入驻` |
| 未申请商家 | `404 商家信息不存在` |
| 商家未审核或已禁用 | `403 商家尚未通过审核或已被禁用` |
| 店铺不可访问 | `404 店铺不存在` |
| 商品分类不存在或不属于当前店铺 | `404 商品分类不存在` |
| 同店铺分类重名 | `409 商品分类名称已存在` |
| 分类下存在商品时删除 | `409 分类下存在商品，不能删除` |
| 禁用分类商品上架 | `409 商品分类已禁用` |
| 商品不存在或不属于当前店铺 | `404 商品不存在` |
| 库存为 0 时商品上架 | `409 库存不足，商品不能上架` |
| 购物车不存在或不属于当前用户 | `404 购物车不存在` |
| 购物车数量、选中状态或 ID 不合法 | `400 参数错误` |
| 购物车存在其他店铺商品 | `409 购物车中已存在其他店铺商品` |
| 加购商品已下架 | `409 商品已下架` |
| 加购或修改数量超过库存 | `409 商品库存不足` |
| 加购店铺已停业 | `409 店铺已停业` |
| 结算时没有已选商品 | `409 购物车没有已选商品` |
| 收货地址不存在、已删除或不属于当前用户 | `404 收货地址不存在` |
| 结算时店铺或商家被禁用 | `409 店铺已禁用` |
| 预览后商品价格或状态变化 | `409 商品信息已变化，请重新结算` |
| 排行榜 type 非法 | `400 排行榜类型参数错误` |
| 排行榜 limit 不在 1～100 | `400 limit 必须在 1 到 100 之间` |
| 骑手个人统计 ID 不存在 | `404 骑手不存在` |
| Mock支付未启用 | `403 模拟支付未启用` |
| 支付单不存在 | `404 支付单不存在` |
| 支付状态不可操作 | `409 当前支付状态不可操作` |
| 支付金额非法或不一致 | `409 支付金额无效或不一致` |
| 非商品订单创建新支付 | `409 当前订单类型暂不支持支付` |
| 支付网关未启用或创建失败 | `409 支付单创建失败` |
| 微信登录未启用 | `503 微信登录未启用` |
| 微信网关配置冲突或缺失 | `500 微信登录配置错误` |
| 微信code无效 | `400 微信登录凭证无效` |
| 微信接口网络、超时或响应异常 | `502 微信登录失败，请稍后重试` |
| 微信接口连接或读取超时 | `503 微信登录服务暂时不可用` |
| 微信响应解析异常 | `502 微信登录响应异常` |
| 微信账号禁用 | `403 当前账号已被禁用` |
| Profile参数非法 | `400/1004 参数错误` |
| 未知异常 | `500 服务器内部错误` |
