# Postman 测试文档

> 当前版本：V0.9 Shopping Cart Completed
>
> V0.9 购物车正常流程和异常流程已全部通过测试，不包含购物车提交订单和 `order_item`。

## 一、环境变量

| 变量 | 示例 | 说明 |
| --- | --- | --- |
| `host` | `http://localhost:8080` | 本地后端地址 |
| `token` | 登录后返回的 token | JWT Token |
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
POST {{host}}/api/address
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

### 5. 模拟支付

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

- 查询所有待接单订单。

预期结果：

- 返回 `status = 0` 的订单
- 只返回 `payStatus = 1` 的订单
- 支付后的 `{{paymentOrderId}}` 可以被查询到
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
| 未知异常 | `500 服务器内部错误` |
