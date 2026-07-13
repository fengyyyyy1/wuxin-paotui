# API 文档

> 项目：五鑫跑腿（Wuxin Paotui）  
> 当前版本：V0.4

## 一、通用规范

### 请求格式

接口统一使用 JSON 请求和 JSON 响应。

需要登录的接口统一携带：

```http
Authorization: Bearer <token>
```

### 统一返回

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

### 通用异常

| code | message | 说明 |
| --- | --- | --- |
| 401 | 未登录或登录已过期 | Token 缺失或无效 |
| 403 | 当前用户不是骑手 | 当前用户没有骑手权限 |
| 404 | 订单不存在 | 订单不存在、已删除或无权访问 |
| 409 | 订单已被其他骑手接单 | 并发接单失败 |
| 409 | 当前订单状态不可接单 | 订单状态不允许当前操作 |
| 409 | 当前订单状态不可完成配送 | 订单状态不允许完成配送 |
| 409 | 当前订单状态不可确认收货 | 订单状态不允许确认收货 |
| 500 | 服务器内部错误 | 未知系统异常 |

## 二、用户模块

### 用户注册

| 项 | 内容 |
| --- | --- |
| 接口名称 | 用户注册 |
| 请求方式 | POST |
| URL | `/api/user/register` |
| Authorization | 不需要 |

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| phone | String | 是 | 手机号 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": "注册成功"
}
```

异常返回：

| code | message |
| --- | --- |
| 1001 | 用户名已存在 |
| 1004 | 参数错误 |

### 用户登录

| 项 | 内容 |
| --- | --- |
| 接口名称 | 用户登录 |
| 请求方式 | POST |
| URL | `/api/user/login` |
| Authorization | 不需要 |

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "jwt-token",
    "userInfo": {
      "id": 2,
      "username": "test001",
      "phone": "13800000000"
    }
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 1002 | 用户不存在 |
| 1003 | 密码错误 |
| 1004 | 参数错误 |

### 获取当前用户

| 项 | 内容 |
| --- | --- |
| 接口名称 | 获取当前用户 |
| 请求方式 | GET |
| URL | `/api/user/me` |
| Authorization | 需要 |

请求参数：无。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 2,
    "username": "test001",
    "phone": "13800000000"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1002 | 用户不存在 |

### 用户列表

| 项 | 内容 |
| --- | --- |
| 接口名称 | 用户列表 |
| 请求方式 | GET |
| URL | `/api/user/list` |
| Authorization | 需要 |

请求参数：无。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": []
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |

## 三、地址模块

### 新增地址

| 项 | 内容 |
| --- | --- |
| 接口名称 | 新增地址 |
| 请求方式 | POST |
| URL | `/api/address` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| receiverName | String | 是 | 收件人 |
| receiverPhone | String | 是 | 手机号 |
| province | String | 否 | 省 |
| city | String | 否 | 市 |
| district | String | 否 | 区 |
| detailAddress | String | 是 | 详细地址 |
| latitude | BigDecimal | 否 | 纬度 |
| longitude | BigDecimal | 否 | 经度 |
| isDefault | Integer | 否 | 是否默认地址 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": null
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1004 | 参数错误 |

### 修改地址

| 项 | 内容 |
| --- | --- |
| 接口名称 | 修改地址 |
| 请求方式 | PUT |
| URL | `/api/address/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`，请求体同新增地址。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": null
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1004 | 参数错误 |

### 删除地址

| 项 | 内容 |
| --- | --- |
| 接口名称 | 删除地址 |
| 请求方式 | DELETE |
| URL | `/api/address/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": null
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1004 | 参数错误 |

### 地址列表

| 项 | 内容 |
| --- | --- |
| 接口名称 | 地址列表 |
| 请求方式 | GET |
| URL | `/api/address/list` |
| Authorization | 需要 |

请求参数：无。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": []
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |

## 四、订单模块

### 发布订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 发布订单 |
| 请求方式 | POST |
| URL | `/api/order/create` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pickupAddressId | Long | 是 | 取件地址 ID |
| deliveryAddressId | Long | 是 | 收件地址 ID |
| goodsName | String | 是 | 物品名称 |
| goodsDescription | String | 否 | 物品描述 |
| weight | BigDecimal | 是 | 重量 |
| distance | BigDecimal | 是 | 距离 |
| price | BigDecimal | 是 | 价格 |
| remark | String | 否 | 备注 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": 2
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1004 | 参数错误 |

### 我的订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 我的订单 |
| 请求方式 | GET |
| URL | `/api/order/my` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 当前页 |
| pageSize | Integer | 否 | 10 | 每页数量，最大 50 |
| status | Integer | 否 | 无 | 订单状态 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 0
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |

### 订单详情

| 项 | 内容 |
| --- | --- |
| 接口名称 | 订单详情 |
| 请求方式 | GET |
| URL | `/api/order/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 2,
    "orderNo": "WX20260710183025123456",
    "status": 1,
    "statusText": "已接单"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 404 | 订单不存在 |

### 用户确认收货

| 项 | 内容 |
| --- | --- |
| 接口名称 | 用户确认收货 |
| 请求方式 | POST |
| URL | `/api/order/confirm/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

成功返回：

```json
{
  "code": 200,
  "message": "确认收货成功",
  "data": {
    "orderId": 2,
    "status": 4,
    "statusText": "已完成",
    "confirmTime": "2026-07-13T20:00:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 404 | 订单不存在 |
| 409 | 当前订单状态不可确认收货 |

## 五、骑手模块

### 骑手大厅

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手大厅 |
| 请求方式 | GET |
| URL | `/api/rider/order/hall` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 当前页 |
| pageSize | Integer | 否 | 10 | 每页数量，最大 50 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 0
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |

### 骑手接单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手接单 |
| 请求方式 | POST |
| URL | `/api/rider/order/accept/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

成功返回：

```json
{
  "code": 200,
  "message": "接单成功",
  "data": {
    "orderId": 2,
    "status": 1,
    "statusText": "已接单",
    "acceptTime": "2026-07-13T18:00:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 403 | 当前用户不是骑手 |
| 404 | 订单不存在 |
| 409 | 订单已被其他骑手接单 |
| 409 | 当前订单状态不可接单 |

### 骑手我的订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手我的订单 |
| 请求方式 | GET |
| URL | `/api/rider/order/my` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 当前页 |
| pageSize | Integer | 否 | 10 | 每页数量，最大 50 |
| status | Integer | 否 | 无 | 订单状态 |

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 0
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 403 | 当前用户不是骑手 |

### 骑手完成配送

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手完成配送 |
| 请求方式 | POST |
| URL | `/api/rider/order/finish/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
    "data": {
    "orderId": 2,
    "status": 3,
    "statusText": "待确认收货",
    "finishTime": "2026-07-13T19:00:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 403 | 当前用户不是骑手 |
| 404 | 订单不存在 |
| 409 | 当前订单状态不可完成配送 |
