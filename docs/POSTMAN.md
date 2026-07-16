# Postman 测试文档

> 当前版本：V0.5（开发中）

## 一、环境变量

| 变量 | 示例 | 说明 |
| --- | --- | --- |
| `host` | `http://localhost:8080` | 本地后端地址 |
| `token` | 登录后返回的 token | JWT Token |
| `orderId` | `2` | 当前测试订单 ID |
| `cancelOrderId` | `1` | 待取消的当前用户待接单订单 ID |

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

### 4. 我的订单

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

### 5. 订单详情

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

### 6. 骑手大厅

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
- 按创建时间倒序

### 7. 骑手接单

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

预期结果：

- 返回 `message = 接单成功`
- `order_info.status` 更新为 `1`
- `order_info.rider_id` 写入
- `order_info.accept_time` 写入
- `order_log` 写入订单日志

### 8. 骑手我的订单

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

### 9. 骑手完成配送

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

### 10. 用户确认收货

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

### 11. 用户取消订单

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
| 未知异常 | `500 服务器内部错误` |
