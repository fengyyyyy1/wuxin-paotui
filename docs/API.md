# API 文档

> 项目：五鑫跑腿（Wuxin Paotui）  
> 当前版本：V0.9 Shopping Cart Completed
>
> V0.9 购物车正常流程与异常流程已全部通过测试，暂不包含购物车提交订单和 `order_item`。

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
| 400 | 参数错误 | V0.9 购物车请求参数不合法 |
| 401 | 未登录或登录已过期 | Token 缺失或无效 |
| 403 | 当前用户不是骑手 | 当前用户没有骑手权限 |
| 404 | 订单不存在 | 订单不存在、已删除或无权访问 |
| 409 | 订单已被其他骑手接单 | 并发接单失败 |
| 409 | 当前订单状态不可接单 | 订单状态不允许当前操作 |
| 409 | 当前订单状态不可完成配送 | 订单状态不允许完成配送 |
| 409 | 当前订单状态不可确认收货 | 订单状态不允许确认收货 |
| 409 | 当前订单状态不可取消 | 订单状态不允许取消 |
| 409 | 当前订单状态不可放弃 | 订单状态不允许骑手放弃 |
| 409 | 当前订单状态不可评价 | 订单尚未完成，不能评价 |
| 409 | 订单已评价 | 同一订单不能重复评价 |
| 409 | 订单已支付 | 订单不能重复支付 |
| 409 | 当前订单状态不可支付 | 配送业务状态不允许支付 |
| 409 | 订单未支付 | 未支付订单不能被骑手接单 |
| 409 | 当前用户已申请商家入驻 | 同一用户重复申请商家 |
| 404 | 商家信息不存在 | 当前用户没有商家资料 |
| 403 | 商家尚未通过审核或已被禁用 | 商家不能管理店铺 |
| 404 | 店铺不存在 | 店铺不存在或不可公开访问 |
| 404 | 商品分类不存在 | 分类不存在或不属于当前店铺 |
| 409 | 商品分类名称已存在 | 同一店铺分类重名 |
| 409 | 分类下存在商品，不能删除 | 分类仍有关联商品 |
| 409 | 商品分类已禁用 | 禁用分类下商品不能上架 |
| 404 | 商品不存在 | 商品不存在或不可公开访问 |
| 409 | 库存不足，商品不能上架 | 商品库存为 0 |
| 404 | 购物车不存在 | 购物车记录不存在、已删除或不属于当前用户 |
| 409 | 购物车中已存在其他店铺商品 | 单店铺购物车约束 |
| 409 | 商品已下架 | 商品不能加入或操作 |
| 409 | 商品库存不足 | 加购或修改后的数量超过当前库存 |
| 409 | 店铺已停业 | 店铺当前不可加入购物车 |
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

订单创建后默认 `status = 0`（待接单）、`payStatus = 0`（未支付）。未支付订单不会进入骑手大厅。

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1004 | 参数错误 |

### 模拟支付

| 项 | 内容 |
| --- | --- |
| 接口名称 | 订单模拟支付 |
| 请求方式 | POST |
| URL | `/api/order/pay/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`，必须大于 0。不需要请求体。

成功返回：

```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "orderId": 5,
    "paymentNo": "PAY20260716170000123456",
    "payStatus": 1,
    "payStatusText": "已支付",
    "amount": 10.00,
    "payTime": "2026-07-16T17:00:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 订单不存在 |
| 409 | 订单已支付 |
| 409 | 当前订单状态不可支付 |
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

订单记录包含 `payStatus`、`payStatusText`、`payTime`、`paymentNo`。

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
    "statusText": "已接单",
    "payStatus": 1,
    "payStatusText": "已支付",
    "payTime": "2026-07-16T17:00:00",
    "paymentNo": "PAY20260716170000123456"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 404 | 订单不存在 |

### 订单轨迹

| 项 | 内容 |
| --- | --- |
| 接口名称 | 查询订单轨迹 |
| 请求方式 | GET |
| URL | `/api/order/timeline/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`，必须大于 0。不需要请求体。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "orderId": 5,
    "orderNo": "WX20260716170000123456",
    "status": 4,
    "statusText": "已完成",
    "payStatus": 1,
    "payStatusText": "已支付",
    "timeline": [
      {
        "type": "ORDER_CREATED",
        "title": "订单已创建",
        "description": "订单创建成功",
        "time": "2026-07-16T17:00:00",
        "sort": 1
      },
      {
        "type": "ORDER_PAID",
        "title": "支付成功",
        "description": "订单支付成功",
        "time": "2026-07-16T17:01:00",
        "sort": 2
      }
    ]
  }
}
```

轨迹来源包括订单创建、支付、骑手接单、骑手完成配送、用户确认收货、用户取消、骑手放弃和用户评价。轨迹按时间升序返回，缺少对应数据时不返回该节点。

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 订单不存在 |
| 1004 | 参数错误 |

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

### 用户取消订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 用户取消订单 |
| 请求方式 | POST |
| URL | `/api/order/cancel/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`，必须大于 0。不需要请求体。

成功返回：

```json
{
  "code": 200,
  "message": "取消订单成功",
  "data": {
    "orderId": 1,
    "status": 5,
    "statusText": "已取消",
    "cancelTime": "2026-07-16T15:00:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 订单不存在 |
| 409 | 当前订单状态不可取消 |
| 1004 | 参数错误 |

### 用户评价订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 用户评价订单 |
| 请求方式 | POST |
| URL | `/api/order/comment` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderId | Long | 是 | 当前用户已完成订单 ID |
| score | Integer | 是 | 评分，范围 1～5 |
| content | String | 否 | 评价内容，最长 500 字 |
| anonymous | Integer | 否 | 是否匿名，`0` 否、`1` 是，默认 `0` |

请求示例：

```json
{
  "orderId": 4,
  "score": 5,
  "content": "配送速度很快，服务很好。",
  "anonymous": 0
}
```

成功返回：

```json
{
  "code": 200,
  "message": "评价成功",
  "data": {
    "commentId": 1,
    "orderId": 4,
    "score": 5,
    "commentTime": "2026-07-16T18:00:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 订单不存在 |
| 409 | 当前订单状态不可评价 |
| 409 | 订单已评价 |
| 1004 | 参数错误或评分、内容、匿名标识不合法 |

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

查询条件固定包含 `status = 0`、`pay_status = 1`、`deleted = 0`，未支付订单不会返回。

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

大厅订单记录包含 `payStatus = 1` 和 `payStatusText = 已支付`，不返回支付单号。

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
| 409 | 订单未支付 |

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

### 骑手放弃订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手放弃订单 |
| 请求方式 | POST |
| URL | `/api/rider/order/give-up/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`，必须大于 0。不需要请求体。

成功返回：

```json
{
  "code": 200,
  "message": "放弃订单成功",
  "data": {
    "orderId": 4,
    "status": 0,
    "statusText": "待接单",
    "giveUpTime": "2026-07-16T16:30:00"
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 403 | 当前用户不是骑手 |
| 404 | 订单不存在 |
| 409 | 当前订单状态不可放弃 |
| 1004 | 参数错误 |

## 六、商家模块

商家管理接口复用 `sys_user` 登录和 JWT，不提供独立商家账号。

### 商家申请入驻

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/merchant/apply` |
| Authorization | 需要 |

请求示例：

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

成功返回：

```json
{
  "code": 200,
  "message": "商家入驻申请提交成功",
  "data": {
    "merchantId": 1,
    "storeId": 1,
    "auditStatus": 0,
    "auditStatusText": "待审核",
    "applyTime": "2026-07-16T18:00:00"
  }
}
```

异常返回：`401`、`409 当前用户已申请商家入驻`、`1004 参数错误`。

### 我的商家资料

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/merchant/me` |
| Authorization | 需要 |

返回商家主体、审核状态和店铺资料，不返回身份证图片及逻辑删除字段。

异常返回：`401`、`404 商家信息不存在`、`404 店铺不存在`。

### 修改店铺资料

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/merchant/store` |
| Authorization | 需要 |

请求体包含 `storeName`、`storeLogo`、`storeDescription`、`storePhone`、地址、经纬度和营业时间。`storeName`、`storePhone`、`detailAddress` 必填。

成功返回：`200 更新店铺资料成功`。

异常返回：`401`、`403 商家尚未通过审核或已被禁用`、`404 商家信息不存在`、`404 店铺不存在`、`1004 参数错误`。

### 修改营业状态

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/merchant/store/business-status` |
| Authorization | 需要 |

```json
{
  "businessStatus": 1
}
```

`businessStatus` 只能为 `0`（休息中）或 `1`（营业中）。成功返回：`200 营业状态更新成功`。

异常返回：`401`、`403 商家尚未通过审核或已被禁用`、`404 商家信息不存在`、`404 店铺不存在`、`1004 参数错误`。

### 新增商品分类

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/merchant/category` |
| Authorization | 需要 |

请求体：

```json
{
  "categoryName": "饮料",
  "sort": 1
}
```

成功返回 `CategoryVO`，消息为 `新增商品分类成功`。同店铺分类重名返回 `409 商品分类名称已存在`。

### 修改商品分类

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/merchant/category/{id}` |
| Authorization | 需要 |

请求体包含必填的 `categoryName` 和可选的 `sort`。只能修改当前商家店铺内未删除分类。

成功消息：`修改商品分类成功`。不存在或不属于当前店铺返回 `404 商品分类不存在`。

### 修改商品分类状态

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/merchant/category/{id}/status` |
| Authorization | 需要 |

```json
{
  "status": 0
}
```

`status` 只能为 `0`（禁用）或 `1`（启用）。禁用分类不会删除分类下商品，公开接口不再返回该分类。

### 删除商品分类

| 项 | 内容 |
| --- | --- |
| 请求方式 | DELETE |
| URL | `/api/merchant/category/{id}` |
| Authorization | 需要 |

使用逻辑删除。分类下存在未删除商品时返回 `409 分类下存在商品，不能删除`。

### 商家商品分类列表

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/merchant/category/list` |
| Authorization | 需要 |

返回当前店铺全部未删除分类，包括禁用分类，按 `sort ASC, create_time ASC` 排序。

### 新增商品

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/merchant/product` |
| Authorization | 需要 |

```json
{
  "categoryId": 1,
  "productName": "可乐",
  "productImage": "product-url",
  "productDescription": "冰镇可乐",
  "price": 3.50,
  "originalPrice": 4.00,
  "stock": 100,
  "sort": 1
}
```

商品创建时默认下架、销量为 0。成功返回 `ProductVO`，消息为 `新增商品成功`。

### 修改商品

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/merchant/product/{id}` |
| Authorization | 需要 |

允许修改分类、名称、图片、介绍、价格、原价、库存和排序；不能修改店铺、销量、状态及删除标识。

### 商品上下架

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/merchant/product/{id}/status` |
| Authorization | 需要 |

```json
{
  "productStatus": 1
}
```

上架要求分类启用且库存大于 0。库存不足返回 `409 库存不足，商品不能上架`，分类禁用返回 `409 商品分类已禁用`。

### 删除商品

| 项 | 内容 |
| --- | --- |
| 请求方式 | DELETE |
| URL | `/api/merchant/product/{id}` |
| Authorization | 需要 |

删除时设置 `is_deleted = 1`、`product_status = 0`，成功消息为 `删除商品成功`。

### 商家商品列表

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/merchant/product/list` |
| Authorization | 需要 |

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 当前页 |
| pageSize | Integer | 否 | 10 | 每页数量，最大 50 |
| categoryId | Long | 否 | 无 | 分类筛选 |
| productStatus | Integer | 否 | 无 | 0 下架、1 上架 |
| keyword | String | 否 | 无 | 商品名称模糊查询 |

返回 `PageResultVO<ProductVO>`，按 `sort ASC, create_time DESC` 排序。

## 七、公开店铺模块

以下接口当前阶段无需 Authorization。

### 店铺列表

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/store/list` |
| Authorization | 不需要 |

请求参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 当前页 |
| pageSize | Integer | 否 | 10 | 每页数量，最大 50 |
| keyword | String | 否 | 无 | 店铺名称模糊查询 |
| district | String | 否 | 无 | 区县筛选 |
| businessStatus | Integer | 否 | 无 | 0 休息、1 营业 |

只返回审核通过、商家启用、店铺启用且未删除的店铺，按营业状态和创建时间倒序。

成功返回 `PageResultVO<StoreListVO>` 分页结构；非法营业状态返回 `1004 参数错误`。

### 店铺详情

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/store/{id}` |
| Authorization | 不需要 |

只返回审核通过、商家启用、店铺启用且未删除的店铺资料。

异常返回：`404 店铺不存在`、`1004 参数错误`。

### 公开商品分类列表

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/store/{storeId}/categories` |
| Authorization | 不需要 |

只返回营业中且可公开访问店铺内启用、未删除的分类，返回 `PublicCategoryVO` 列表。

### 公开商品列表

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/store/{storeId}/products` |
| Authorization | 不需要 |

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 当前页 |
| pageSize | Integer | 否 | 10 | 每页数量，最大 50 |
| categoryId | Long | 否 | 无 | 分类筛选 |
| keyword | String | 否 | 无 | 商品名称模糊查询 |

只返回营业中店铺的启用分类下已上架、未删除且库存大于 0 的商品，返回 `PageResultVO<PublicProductVO>`。

### 公开商品详情

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/store/product/{id}` |
| Authorization | 不需要 |

只允许查询审核通过且启用的商家、启用店铺、启用分类下已上架且有库存的商品。查询不到返回 `404 商品不存在`。

## 八、购物车模块

购物车接口全部需要登录，用户 ID 统一从 `UserContext` 获取。购物车不保存商品名称、图片和价格快照，查询时实时关联商品数据。

有效商品校验使用数据库真实字段：`merchant_category.status = 1` 表示分类启用，`merchant_store.business_status = 1` 表示店铺营业。

### 加入购物车

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/cart/add` |
| Authorization | 需要 |

```json
{
  "productId": 1,
  "quantity": 1
}
```

同一商品重复加入时累加数量。购物车已有其他店铺商品时返回 `409 购物车中已存在其他店铺商品`。

成功返回：

```json
{
  "code": 200,
  "message": "加入购物车成功",
  "data": {
    "cartId": 1,
    "storeId": 1,
    "storeName": "五鑫便利店",
    "productId": 1,
    "productName": "可乐",
    "productImage": "product-url",
    "price": 3.50,
    "stock": 100,
    "quantity": 1,
    "selected": 1,
    "productStatus": 1,
    "invalidReason": null,
    "subtotal": 3.50
  }
}
```

异常返回：`400 参数错误`、`401`、`404 商品不存在`、`409 商品已下架`、`409 商品分类已禁用`、`409 店铺已停业`、`409 商品库存不足`、`409 购物车中已存在其他店铺商品`。

### 查询购物车

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/cart/list` |
| Authorization | 需要 |

返回店铺信息、购物车商品、`selectedTotalAmount` 和 `selectedProductCount`。`selectedProductCount` 为有效且选中商品的数量合计。

商品删除、下架、分类禁用、店铺停业或库存不足时保留购物车记录，通过 `invalidReason` 标记；失效商品不计入选中总额和数量。

### 修改购物车数量

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/cart/update` |
| Authorization | 需要 |

```json
{
  "cartId": 1,
  "quantity": 2
}
```

只能修改当前用户自己的购物车，商品必须有效，数量不能超过最新库存。

### 修改选中状态

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/cart/selected` |
| Authorization | 需要 |

```json
{
  "cartId": 1,
  "selected": 1
}
```

`selected` 只能为 0 或 1。失效商品可以取消选中，但不能重新设置为选中。

### 删除购物车商品

| 项 | 内容 |
| --- | --- |
| 请求方式 | DELETE |
| URL | `/api/cart/{id}` |
| Authorization | 需要 |

仅逻辑删除当前用户自己的购物车记录。重复删除或记录不存在返回 `404 购物车不存在`。

### 清空购物车

| 项 | 内容 |
| --- | --- |
| 请求方式 | DELETE |
| URL | `/api/cart/clear` |
| Authorization | 需要 |

逻辑删除当前用户全部购物车记录，购物车为空时同样返回成功。

### V0.9 测试结果

正常流程已通过：加入购物车、重复商品累加、列表金额计算、修改数量、修改选中状态、逻辑删除、删除后重新加入和清空购物车。

异常流程已通过：未登录、非法参数、购物车不存在、跨用户操作、跨店铺加购、商品下架、分类禁用、店铺停业、库存不足及失效商品重新选中。
