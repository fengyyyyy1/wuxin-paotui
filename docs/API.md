# API 文档

> 项目：五鑫跑腿（Wuxin Paotui）  
> 当前版本：V1.8 骑手端与商家端
>
> 当前微信登录和手机号绑定支持本地固定映射 Mock 联调；真实 code2session 需要配置小程序 AppID 和 AppSecret，真实手机号接口尚未接入。

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
| 400 | 参数错误 | 购物车请求参数不合法 |
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
| 409 | 店铺已禁用 | 店铺或商家当前不可用 |
| 409 | 购物车没有已选商品 | 结算时没有已选购物车记录 |
| 409 | 商品信息已变化，请重新结算 | 预览后商品价格或状态发生变化 |
| 404 | 收货地址不存在 | 地址不存在、已删除或不属于当前用户 |
| 503 | 微信登录未启用 | 真实和 Mock 微信登录均关闭 |
| 500 | 微信登录配置错误 | 网关同时开启、生产启用 Mock 或配置缺失 |
| 400 | 微信登录凭证无效 | code 无效、过期或已经使用 |
| 502 | 微信登录失败，请稍后重试 | 微信接口网络、超时或响应异常 |
| 503 | 微信登录服务暂时不可用 | 微信接口连接或读取超时 |
| 502 | 微信登录响应异常 | 微信响应无法安全解析 |
| 403 | 当前账号已被禁用 | openid 对应用户状态不可登录 |
| 404 | 订单不存在或无权限 | 商家订单不存在、已删除或不属于当前店铺 |
| 409 | 订单未支付，商家不可操作 | 商品订单尚未支付 |
| 409 | 当前订单状态不允许商家操作 | 商家重复操作或状态不满足 |
| 403 | 无管理员权限 | 当前登录用户没有有效 ADMIN 角色 |
| 404 | 商家不存在 | 总控端查询或操作的商家不存在 |
| 409 | 当前商家审核状态不可操作 | 重复审核或审核状态不满足 |
| 409 | 当前商家状态不可操作 | 重复启用、重复禁用或状态不满足 |
| 503 | 微信手机号绑定未启用 | Mock 手机号网关默认关闭 |
| 403 | 生产环境禁止使用模拟微信手机号服务 | `prod` 环境误开 Mock |
| 400 | 微信手机号授权凭证无效 | Mock code 不在固定映射中 |
| 400 | 微信返回的手机号格式错误 | 网关结果不是中国大陆手机号 |
| 409 | 手机号已绑定其他用户 | 其他未删除用户已占用手机号 |
| 502 | 微信手机号服务异常，请稍后重试 | 手机号网关或更新结果异常 |
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
      "nickname": null,
      "avatar": null,
      "phone": "13800000000",
      "gender": 0
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

### 微信小程序登录

| 项 | 内容 |
| --- | --- |
| 接口名称 | 微信小程序登录 |
| 请求方式 | POST |
| URL | `/api/user/wechat/login` |
| Authorization | 不需要 |

请求参数：

```json
{
  "code": "wx.login返回的临时code"
}
```

前端只能提交临时 `code`，不得提交 `openid`、`unionid`、`sessionKey`、用户 ID 或 JWT。

本地 Mock 可用 code：

| code | 身份 |
| --- | --- |
| `mock-code-new-user` | 自动注册固定 Mock 微信用户 |
| `mock-code-new-user-repeat` | 与上一 code 映射到同一 openid，用于重复登录 |
| `mock-code-test001` | 创建独立 Mock 微信用户，不会绑定现有 `test001` |

成功返回：

```json
{
  "code": 200,
  "message": "微信登录成功",
  "data": {
    "token": "jwt-token",
    "userInfo": {
      "id": 7,
      "username": "wx_安全摘要",
      "nickname": null,
      "avatar": null,
      "phone": null,
      "gender": 0
    },
    "newUser": true
  }
}
```

说明：

- 首次 openid 登录自动创建用户，`newUser=true`。
- 再次使用同一 Mock 身份返回同一 `userId`，`newUser=false`。
- 自动用户名使用 openid 的 SHA-256 摘要，不直接暴露完整 openid。
- 自动用户密码保存随机 BCrypt 密文，不使用固定明文密码。
- `session_key`只在本次后端调用内存中使用，不持久化、不返回。
- 普通用户名密码登录保持兼容；本阶段不实现已有账号绑定、账号合并或手机号自动绑定。

异常返回：

| code | message |
| --- | --- |
| 400 | 参数错误 |
| 400 | 微信登录凭证无效 |
| 403 | 当前账号已被禁用 |
| 409 | 微信用户创建失败 |
| 500 | 微信登录配置错误 |
| 502 | 微信登录失败，请稍后重试 |
| 503 | 微信登录服务暂时不可用 |
| 502 | 微信登录响应异常 |
| 502 | 微信登录未返回用户标识 |
| 503 | 微信登录未启用 |

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
    "nickname": null,
    "avatar": null,
    "phone": "13800000000",
    "gender": 0
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 1002 | 用户不存在 |

### 获取用户资料

| 项 | 内容 |
| --- | --- |
| 接口名称 | 获取用户资料 |
| 请求方式 | GET |
| URL | `/api/user/profile` |
| Authorization | 需要 |

用户ID只从JWT对应的`UserContext`获取，不接收客户端userId。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 3,
    "username": "wx_安全摘要",
    "nickname": "悠悠球",
    "avatar": "/assets/images/default-avatar.svg",
    "phone": null,
    "gender": 0
  }
}
```

异常返回：`401 未登录或登录已过期`、`1002 用户不存在`。

### 修改用户资料

| 项 | 内容 |
| --- | --- |
| 接口名称 | 修改用户资料 |
| 请求方式 | PUT |
| URL | `/api/user/profile` |
| Authorization | 需要 |

请求：

```json
{
  "nickname": "悠悠球",
  "avatar": "/assets/images/default-avatar.svg",
  "gender": 0
}
```

参数规则：

- `nickname`可为空，最长30个字符。
- `avatar`可为空；真实数据库字段为`varchar(255)`，接口安全上限为255个字符。
- `gender`必填，只允许`0`、`1`、`2`。
- 接口只修改`nickname、avatar、gender`。
- 禁止修改`username、openid、unionid、phone、password、status`。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": "新增地址成功"
}
```

异常返回：`400/1004 参数错误`、`401 未登录或登录已过期`、`1002 用户不存在`。

### 绑定微信手机号

| 项 | 内容 |
| --- | --- |
| 接口名称 | 绑定微信手机号 |
| 请求方式 | POST |
| URL | `/api/user/phone/bind` |
| Authorization | 需要 |

用户 ID 只从 JWT 对应的 `UserContext` 获取。客户端只能提交微信手机号授权 `code`，不得提交 `userId` 或明文 `phone`。

请求：

```json
{
  "code": "mock-phone-code-13800000003"
}
```

参数规则：

- `code` 必填，去除首尾空格后使用，最长 128 个字符。
- 本地 Mock 仅支持固定 code，不根据任意字符串生成手机号。
- 服务端优先使用网关结果的 `purePhoneNumber`，并校验 `^1[3-9]\d{9}$`。
- 重复绑定当前手机号幂等成功；更换为未占用手机号时允许覆盖。
- 手机号已被其他未删除用户占用时拒绝绑定。

本地 Mock 映射：

| code | 手机号 |
| --- | --- |
| `mock-phone-code-13800000003` | `13800000003` |
| `mock-phone-code-13900000003` | `13900000003` |
| `mock-phone-code-invalid` | 返回业务异常 |

成功返回：

```json
{
  "code": 200,
  "message": "手机号绑定成功",
  "data": {
    "id": 3,
    "username": "wx_安全摘要",
    "nickname": "悠悠球",
    "avatar": "/assets/images/default-avatar.svg",
    "phone": "13800000003",
    "gender": 0
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 400/1004 | 参数错误 |
| 400 | 微信手机号授权凭证无效 |
| 400 | 微信返回的手机号格式错误 |
| 401 | 未登录或登录已过期 |
| 403 | 当前账号已被禁用 |
| 403 | 生产环境禁止使用模拟微信手机号服务 |
| 409 | 手机号已绑定其他用户 |
| 502 | 微信手机号服务异常，请稍后重试 |
| 503 | 微信手机号绑定未启用 |

当前只实现 Mock 手机号网关。真实微信 `getuserphonenumber` 需要 access_token 管理能力，后续接入时继续复用同一网关接口。

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
| URL | `/api/user/address` |
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

### 编辑地址

| 项 | 内容 |
| --- | --- |
| 接口名称 | 编辑地址 |
| 请求方式 | PUT |
| URL | `/api/user/address/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

请求体：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| receiverName | String | 是 | 收件人，最长30个字符 |
| receiverPhone | String | 是 | 11位手机号 |
| province | String | 否 | 省，最长30个字符 |
| city | String | 否 | 市，最长30个字符 |
| district | String | 否 | 区，最长30个字符 |
| detailAddress | String | 是 | 详细地址，最长120个字符 |
| latitude | BigDecimal | 否 | 纬度 |
| longitude | BigDecimal | 否 | 经度 |
| isDefault | Integer | 否 | 是否默认地址，`1`表示默认 |

业务规则：

- 用户只能修改自己的未删除地址。
- 地址不存在、已删除或不属于当前用户时返回业务异常。
- `isDefault=1`时自动取消当前用户其它默认地址。
- 同一用户最多一个默认地址。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": "修改地址成功"
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 地址不存在或无权操作 |
| 1004 | 参数错误 |

### 设置默认地址

| 项 | 内容 |
| --- | --- |
| 接口名称 | 设置默认地址 |
| 请求方式 | PUT |
| URL | `/api/user/address/{id}/default` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

业务规则：

- 用户只能将自己的未删除地址设为默认地址。
- 设置成功后自动取消当前用户其它默认地址。
- 同一用户最多一个默认地址。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": "设置默认地址成功"
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 地址不存在或无权操作 |

### 删除地址

| 项 | 内容 |
| --- | --- |
| 接口名称 | 删除地址 |
| 请求方式 | DELETE |
| URL | `/api/user/address/{id}` |
| Authorization | 需要 |

请求参数：路径参数 `id`。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": "删除地址成功"
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 地址不存在或无权操作 |

### 地址列表

| 项 | 内容 |
| --- | --- |
| 接口名称 | 地址列表 |
| 请求方式 | GET |
| URL | `/api/user/address/list` |
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

### 购物车结算预览

| 项 | 内容 |
| --- | --- |
| 接口名称 | 购物车结算预览 |
| 请求方式 | POST |
| URL | `/api/order/settlement/preview` |
| Authorization | 需要 |

请求参数：

```json
{
  "deliveryAddressId": 2
}
```

接口读取当前登录用户 `selected = 1` 且 `is_deleted = 0` 的购物车记录，实时校验收货地址、商品、分类、店铺、商家和库存。预览不会创建订单、扣减库存或修改购物车。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "storeId": 1,
    "storeName": "五鑫便利店",
    "deliveryAddressId": 2,
    "items": [
      {
        "productId": 2,
        "productName": "测试商品",
        "productImage": "product-image-url",
        "price": 1.00,
        "quantity": 2,
        "subtotal": 2.00,
        "stock": 10
      }
    ],
    "productAmount": 2.00,
    "deliveryFee": 0.00,
    "totalAmount": 2.00,
    "selectedProductCount": 2
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 401 | 未登录或登录已过期 |
| 404 | 收货地址不存在 |
| 404 | 商品不存在 |
| 409 | 购物车没有已选商品 |
| 409 | 购物车中已存在其他店铺商品 |
| 409 | 商品已下架 |
| 409 | 商品分类已禁用 |
| 409 | 店铺已禁用 |
| 409 | 店铺已停业 |
| 409 | 商品库存不足 |
| 1004 | 参数错误 |

### 购物车创建商品订单

| 项 | 内容 |
| --- | --- |
| 接口名称 | 购物车创建商品订单 |
| 请求方式 | POST |
| URL | `/api/order/create-from-cart` |
| Authorization | 需要 |

请求参数：

```json
{
  "deliveryAddressId": 2,
  "remark": "请尽快配送"
}
```

创建流程在同一事务内写入 `order_info`、原子扣减库存、写入 `order_item` 快照和 `order_log`，最后仅逻辑删除当前用户已选购物车项。商品订单默认 `orderType = 1`、`payStatus = 0`、`status = 0`，当前配送费为 `0.00`。

成功返回：

```json
{
  "code": 200,
  "message": "商品订单创建成功",
  "data": {
    "orderId": 3,
    "orderNo": "WX20260717150000123456",
    "orderType": 1,
    "storeId": 1,
    "productAmount": 2.00,
    "deliveryFee": 0.00,
    "totalAmount": 2.00,
    "payStatus": 0,
    "status": 0,
    "itemCount": 2
  }
}
```

异常返回除结算预览异常外，还可能返回：

| code | message |
| --- | --- |
| 409 | 商品信息已变化，请重新结算 |
| 409 | 商品订单创建失败 |

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
    "paymentNo": "PAY20260716170000123456",
    "orderType": 1,
    "orderTypeText": "商品订单",
    "storeId": 1,
    "storeName": "五鑫便利店",
    "productAmount": 2.00,
    "deliveryFee": 0.00,
    "totalAmount": 2.00,
    "items": [
      {
        "productId": 2,
        "productName": "测试商品",
        "productImage": "product-image-url",
        "productPrice": 1.00,
        "quantity": 2,
        "subtotal": 2.00
      }
    ]
  }
}
```

`orderType = 0` 为跑腿订单，`orderType = 1` 为商品订单。商品订单的 `items` 始终读取 `order_item` 下单快照，不使用 `merchant_product` 当前名称、图片或价格；跑腿订单原有字段继续兼容。

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

查询条件固定包含 `pay_status = 1`、`deleted = 0`。普通跑腿订单查询
`order_type = 0`（兼容历史 `NULL`）且 `status = 0` 的待接单订单；商品订单仅查询
`order_type = 1` 且 `status = 7` 的已出餐待骑手接单订单。未支付商品订单或
`status = 0` 的待商家接单商品订单不会返回。

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

### 骑手跑单排行榜

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手跑单排行榜 |
| 请求方式 | GET |
| URL | `/api/rider/ranking` |
| Authorization | 需要 |

请求参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| type | String | 否 | `today` | `today`、`week`、`month`、`total` |
| limit | Integer | 否 | `10` | 返回数量，范围 1～100 |

示例请求：

```http
GET /api/rider/ranking?type=today&limit=10
Authorization: Bearer <token>
```

统计规则：

- 仅统计 `status = 4`、`rider_id IS NOT NULL`、`deleted = 0` 的已完成订单。
- 今日、本周、本月使用 `finish_time` 左闭右开时间范围；累计榜不限制时间。
- 按完成单量降序、周期内最早完成时间升序、骑手 ID 升序稳定排序。
- `rank` 使用从 1 开始的连续顺序名次。

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "rank": 1,
      "riderId": 1,
      "riderUserId": 2,
      "riderName": "测试骑手",
      "avatar": null,
      "completedOrderCount": 8
    }
  ]
}
```

无符合条件订单时，`data` 返回空数组。

异常返回：

| code | message |
| --- | --- |
| 400 | 排行榜类型参数错误 |
| 400 | limit 必须在 1 到 100 之间 |
| 401 | 未登录或登录已过期 |

### 骑手个人跑单统计

| 项 | 内容 |
| --- | --- |
| 接口名称 | 骑手个人跑单统计 |
| 请求方式 | GET |
| URL | `/api/rider/{riderId}/statistics` |
| Authorization | 需要 |

请求参数：路径参数 `riderId`，对应 `rider_info.id`，必须大于 0。

示例请求：

```http
GET /api/rider/1/statistics
Authorization: Bearer <token>
```

成功返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "riderId": 1,
    "todayCompletedCount": 2,
    "weekCompletedCount": 5,
    "monthCompletedCount": 12,
    "totalCompletedCount": 36
  }
}
```

异常返回：

| code | message |
| --- | --- |
| 400 | 参数错误 |
| 401 | 未登录或登录已过期 |
| 404 | 骑手不存在 |

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

### 商家订单分页列表

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/merchant/order/page` |
| Authorization | 需要 |

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 最小 1 |
| pageSize | Integer | 否 | 10 | 1～100 |
| status | Integer | 否 | 无 | 订单状态筛选 |
| keyword | String | 否 | 无 | 订单号或商品快照名称，最长 100 |
| startTime | LocalDateTime | 否 | 无 | 创建时间起点，包含 |
| endTime | LocalDateTime | 否 | 无 | 创建时间终点，不包含 |

只查询当前登录用户所属有效商家、有效店铺的`order_type = 1`商品订单，按`create_time DESC, id DESC`排序。

示例：

```http
GET /api/merchant/order/page?pageNum=1&pageSize=10&status=0
Authorization: Bearer <merchant-token>
```

成功响应：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "orderId": 6,
        "orderNo": "WX202607180001",
        "status": 0,
        "statusName": "待商家接单",
        "payStatus": 1,
        "payStatusName": "已支付",
        "productAmount": 1.00,
        "deliveryFee": 1.00,
        "totalAmount": 2.00,
        "goodsSummary": "可乐 x2",
        "receiverName": "测试用户",
        "receiverPhone": "138****0003",
        "deliveryAddress": "测试省测试市测试区测试地址",
        "createTime": "2026-07-18T12:00:00",
        "payTime": "2026-07-18T12:01:00",
        "merchantAcceptTime": null,
        "readyTime": null
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

### 商家订单详情

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/merchant/order/{id}` |
| Authorization | 需要 |

只能查询当前商家店铺的商品订单。商品明细读取`order_item`快照，轨迹读取`order_log`，不会读取当前商品信息覆盖历史快照。

```http
GET /api/merchant/order/6
Authorization: Bearer <merchant-token>
```

成功响应在列表字段基础上增加：

```json
{
  "remark": "尽快配送",
  "merchantRejectTime": null,
  "merchantRejectReason": null,
  "items": [
    {
      "productId": 1,
      "productName": "可乐",
      "productImage": "product-url",
      "productPrice": 1.00,
      "quantity": 2,
      "subtotal": 2.00
    }
  ],
  "timeline": [
    {
      "oldStatus": 0,
      "oldStatusName": "待商家接单",
      "newStatus": 6,
      "newStatusName": "商家已接单，制作中",
      "operatorType": "MERCHANT",
      "remark": "商家接单",
      "createTime": "2026-07-18T12:05:00"
    }
  ]
}
```

越权、不存在、非商品订单或已删除统一返回`404 订单不存在或无权限`。

### 商家接单

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/merchant/order/{id}/accept` |
| Authorization | 需要 |
| 请求体 | 无 |

仅允许已支付且`status = 0`的当前店铺商品订单，原子更新为`status = 6`并写入`merchant_accept_time`和订单日志。

```json
{
  "code": 200,
  "message": "商家接单成功",
  "data": {
    "orderId": 6,
    "status": 6,
    "statusName": "商家已接单，制作中",
    "merchantAcceptTime": "2026-07-18T12:05:00",
    "readyTime": null,
    "rejectTime": null,
    "rejectReason": null
  }
}
```

### 商家拒单

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/merchant/order/{id}/reject` |
| Authorization | 需要 |

```json
{
  "reason": "商品暂时缺货"
}
```

`reason`去除首尾空格后长度必须为2～200。仅允许已支付且`status = 0`的当前店铺商品订单，原子更新为`status = 8`。

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

拒单成功只将订单从`status = 0`更新为`status = 8`，并写入
`merchant_reject_time`和`merchant_reject_reason`。不会把`pay_status`或
`payment_order`标记为已退款；真实退款尚未开发，当前状态表示“等待退款处理”。

### 商家出餐

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/merchant/order/{id}/ready` |
| Authorization | 需要 |
| 请求体 | 无 |

仅允许已支付且`status = 6`的当前店铺商品订单，原子更新为`status = 7`并写入`merchant_ready_time`和订单日志。商品订单从此状态开始进入骑手大厅。

```json
{
  "code": 200,
  "message": "商家出餐成功",
  "data": {
    "orderId": 6,
    "status": 7,
    "statusName": "已出餐，待骑手接单",
    "merchantAcceptTime": "2026-07-18T12:05:00",
    "readyTime": "2026-07-18T12:15:00",
    "rejectTime": null,
    "rejectReason": null
  }
}
```

响应中的 `merchantAcceptTime`、`readyTime`、`rejectTime` 和 `rejectReason`
均从更新后的订单真实数据读取。其中 API 字段`readyTime`对应数据库字段
`merchant_ready_time`。

商家订单操作异常：

| code | message |
| --- | --- |
| 400/1004 | 参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 商家尚未通过审核或已被禁用 |
| 404 | 商家信息不存在 |
| 404 | 店铺不存在 |
| 404 | 订单不存在或无权限 |
| 409 | 订单未支付，商家不可操作 |
| 409 | 当前订单状态不允许商家操作 |

## 七、公开店铺模块

以下接口当前阶段无需 Authorization。

V1.7-6用户小程序首页已复用本模块真实接口：推荐门店调用
`GET /api/store/list`，门店详情占位页调用`GET /api/store/{id}`。
首页Banner、核心服务入口和公益入口当前为本地静态展示数据，不对应新增后端接口。

V1.7-7用户小程序门店与商品浏览继续复用本模块真实接口：
门店详情调用`GET /api/store/{id}`，商品分类调用
`GET /api/store/{storeId}/categories`，商品列表调用
`GET /api/store/{storeId}/products`，商品详情调用
`GET /api/store/product/{id}`。本阶段未新增后端接口。

V1.7-7A商品链路审计结论：门店详情公开查询只要求商家审核通过、商家启用、
店铺启用且未删除；商品分类、商品列表和商品详情还要求店铺
`business_status = 1`。因此测试数据中若店铺为休息中，会出现门店详情正常但
商品分类和商品列表为空。当前测试库已将`storeId=1`设为营业中后完成复测。

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
过滤条件包含商家审核通过、商家启用、店铺启用、店铺营业、店铺未删除、
分类启用和分类未删除。

返回字段：`categoryId`、`categoryName`、`sort`。

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
过滤条件包含商家审核通过、商家启用、店铺启用、店铺营业、店铺未删除、
分类启用、分类未删除、商品上架、商品未删除和库存大于 0。

`PublicProductVO`字段：`productId`、`categoryId`、`categoryName`、`productName`、
`productImage`、`productDescription`、`price`、`originalPrice`、`stock`、`sales`、`sort`。
用户小程序V1.7-7不展示虚假销量、评分、优惠或购买成功状态。

### 公开商品详情

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/store/product/{id}` |
| Authorization | 不需要 |

只允许查询审核通过且启用的商家、启用且营业中的店铺、启用分类下已上架且有库存的商品。查询不到返回 `404 商品不存在`。

用户小程序V1.7-8起商品详情页“加入购物车”调用真实购物车接口；下单与支付仍在后续阶段完成。

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

### 全选或取消全选

| 项 | 内容 |
| --- | --- |
| 请求方式 | PUT |
| URL | `/api/cart/selected/all` |
| Authorization | 需要 |

```json
{
  "selected": 1
}
```

`selected` 只能为 0 或 1。接口只更新当前用户购物车中的有效商品，失效商品不参与全选、不计入金额和数量。

成功返回最新 `CartListVO`，用于前端刷新列表、合计金额和购物车角标。

### 删除购物车商品

| 项 | 内容 |
| --- | --- |
| 请求方式 | DELETE |
| URL | `/api/cart/{id}` |
| Authorization | 需要 |

仅逻辑删除当前用户自己的购物车记录。重复删除或记录不存在返回 `404 购物车不存在`。

### 清理失效商品

| 项 | 内容 |
| --- | --- |
| 请求方式 | DELETE |
| URL | `/api/cart/invalid` |
| Authorization | 需要 |

接口按购物车列表同一套实时校验规则识别 `invalidReason != null` 的商品，并仅逻辑删除当前用户自己的失效购物车记录。没有失效商品时同样返回成功。

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

## 九、支付模块

支付模块第一阶段只支持商品订单和本地 Mock 联调。所有接口使用服务端订单金额，前端不得提交金额、openid、appid、商户号、回调地址或支付单号。

### 创建 JSAPI 支付单

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/payment/wechat/jsapi` |
| Authorization | 需要 |

请求：

```json
{
  "orderId": 10
}
```

成功响应：

```json
{
  "code": 200,
  "message": "支付单创建成功",
  "data": {
    "paymentNo": "PAY20260717230000...",
    "timeStamp": "1784300400",
    "nonceStr": "mockNonce",
    "packageValue": "prepay_id=mock_prepay_xxx",
    "signType": "RSA",
    "paySign": "MOCK_SIGN_xxx"
  }
}
```

说明：

- 仅支持 `order_type = 1`、`status = 0`、`pay_status = 0` 的当前用户商品订单。
- 金额读取 `order_info.total_amount`，由服务端转换为整数分。
- 创建支付单后，`payment_order.status = 1`，不会直接修改订单 `pay_status`。
- 同一订单已有有效待支付流水时复用，不重复创建。

异常：`400 参数错误`、`401`、`404 订单不存在`、`409 已支付`、`409 当前订单类型暂不支持支付`、`409 支付金额无效或不一致`、`409 支付单创建失败`。

### Mock 确认支付成功

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/payment/mock/{paymentNo}/success` |
| Authorization | 需要 |
| 环境限制 | 仅 `wuxin.mock-payment.enabled=true` 时注册 |

成功后在同一事务中将支付流水更新为 `SUCCESS`，将订单 `pay_status` 更新为 `1`，并写入一次订单日志。重复调用幂等。

成功响应：

```json
{
  "code": 200,
  "message": "模拟支付成功",
  "data": {
    "orderId": 10,
    "orderNo": "WX20260717225900123456",
    "payStatus": 1,
    "paymentNo": "PAY20260717230000...",
    "paymentStatus": 2,
    "paymentStatusText": "支付成功",
    "transactionId": "MOCK_TXN_PAY...",
    "amountTotal": 350,
    "successTime": "2026-07-17T23:01:00"
  }
}
```

异常：`401`、`404 支付单不存在`、`409 当前支付状态不可操作`、`409 支付金额无效或不一致`。

### 查询订单支付状态

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/payment/order/{orderId}/status` |
| Authorization | 需要 |

仅允许查询当前用户自己的未删除订单。没有支付流水时仍返回订单真实 `payStatus`，支付流水字段为 `null`。

### 旧版模拟支付

`POST /api/order/pay/{id}`继续保留，仅用于开发测试。只有`wuxin.mock-payment.enabled=true`时允许调用，生产环境默认关闭；新测试应迁移到`payment_order + /api/payment/mock/{paymentNo}/success`流程。

### 微信支付回调规划

第二阶段计划路径为`POST /api/payment/wechat/notify`。第一阶段未注册该接口、未放行JWT，也没有任何假验签逻辑。真实实现必须使用官方SDK `NotificationParser`对原始请求验签、解密并校验金额后，才能调用统一支付确认事务。

## 十、总控端商家审核模块

所有接口均需要 JWT，并要求当前用户在`sys_user_role`中关联有效的
`ADMIN`角色。未登录返回`401`，无管理员权限返回`403`。

### 商家申请分页

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/admin/merchant/page` |
| Authorization | 管理员 JWT |

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| pageNum | Integer | 否 | 1 | 最小 1 |
| pageSize | Integer | 否 | 10 | 1～100 |
| auditStatus | Integer | 否 | 无 | 0 待审核、1 通过、2 驳回 |
| merchantStatus | Integer | 否 | 无 | 0 禁用、1 启用 |
| keyword | String | 否 | 无 | 商家名称、联系人或手机号，最长 100 |

固定过滤`merchant_info.is_deleted = 0`，按申请时间和商家 ID 倒序。

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "merchantId": 1,
        "userId": 2,
        "merchantName": "五鑫便利店",
        "contactName": "测试联系人",
        "contactPhone": "13800000000",
        "auditStatus": 0,
        "auditStatusText": "待审核",
        "merchantStatus": 1,
        "merchantStatusText": "启用",
        "storeId": 1,
        "storeName": "五鑫便利店",
        "storeStatus": 1,
        "storeStatusText": "启用",
        "businessStatus": 0,
        "businessStatusText": "休息中",
        "applyTime": "2026-07-18T19:00:00",
        "auditTime": null
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

### 商家申请详情

| 项 | 内容 |
| --- | --- |
| 请求方式 | GET |
| URL | `/api/admin/merchant/{merchantId}` |
| Authorization | 管理员 JWT |

返回商家申请资料、申请用户非敏感信息、店铺资料、审核管理员、审核时间、
审核备注和拒绝原因。不会返回密码、Token、openid或unionid。

不存在或已删除返回`404 商家不存在`。

### 审核通过

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/admin/merchant/{merchantId}/approve` |
| Authorization | 管理员 JWT |

```json
{
  "auditRemark": "审核通过，资料齐全，符合入驻要求。"
}
```

仅允许`audit_status = 0`。成功后：

- `audit_status = 1`
- `merchant_status = 1`
- `store_status = 1`
- 写入`audit_admin_id`、`audit_time`和`audit_remark`
- `business_status`保持原值，不自动营业
- 写入`merchant_audit_log`

写接口统一返回`AdminMerchantOperationVO`：

```json
{
  "code": 200,
  "message": "商家审核通过",
  "data": {
    "merchantId": 1,
    "auditStatus": 1,
    "auditStatusText": "审核通过",
    "merchantStatus": 1,
    "merchantStatusText": "启用",
    "storeStatus": 1,
    "storeStatusText": "启用",
    "businessStatus": 0,
    "businessStatusText": "休息中",
    "auditAdminId": 1,
    "auditTime": "2026-07-18T19:30:00",
    "auditRemark": "资料审核通过",
    "rejectReason": null,
    "operationTime": "2026-07-18T19:30:00"
  }
}
```

### 审核拒绝

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/admin/merchant/{merchantId}/reject` |
| Authorization | 管理员 JWT |

```json
{
  "reason": "营业执照信息不清晰"
}
```

`reason`去除首尾空格后长度为2～255。仅允许`audit_status = 0`。
成功后`audit_status = 2`、商家和店铺禁用、店铺停止营业，并写入审核人、
审核时间、拒绝原因和审核日志。

### 启用商家

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/admin/merchant/{merchantId}/enable` |
| Authorization | 管理员 JWT |
| 请求体 | 无 |

仅允许审核已通过且当前禁用的商家。成功后启用商家和店铺，但不自动把
`business_status`改为营业中，不修改历史审核结论。

### 禁用商家

| 项 | 内容 |
| --- | --- |
| 请求方式 | POST |
| URL | `/api/admin/merchant/{merchantId}/disable` |
| Authorization | 管理员 JWT |

```json
{
  "reason": "存在违规经营行为"
}
```

仅允许审核已通过且当前启用的商家。成功后：

- `merchant_status = 0`
- `store_status = 0`
- `business_status = 0`
- 不修改`audit_status`
- 不删除商家、店铺或历史订单
- 写入`merchant_audit_log`

四个写接口均使用原状态条件更新并处于事务中。重复或并发操作返回：

| code | message |
| --- | --- |
| 400/1004 | 参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 无管理员权限 |
| 404 | 商家不存在 |
| 404 | 店铺不存在 |
| 409 | 当前商家审核状态不可操作 |
| 409 | 当前商家状态不可操作 |

## V1.8骑手端补充接口

以下接口统一使用`Result<T>`并要求Bearer Token。`/api/admin/**`还会经过管理员权限拦截器。

### 提交骑手申请

`POST /api/rider/apply`

```json
{
  "realName": "测试骑手",
  "idCard": "500000199001011234",
  "idCardFront": "https://your-cos-domain/rider/front.jpg",
  "idCardBack": "https://your-cos-domain/rider/back.jpg"
}
```

申请人必须先绑定手机号。首次申请创建审核中资料；审核拒绝后可重新提交。成功返回`riderId`、`auditStatus`、`auditStatusText`和`applyTime`。

### 查询当前骑手资料

`GET /api/rider/profile`

返回用户基础信息、脱敏身份证号、身份证图片URL、审核状态、骑手状态、拒绝原因和申请/更新时间。完整身份证号不返回前端。

### 查询骑手配送详情

`GET /api/rider/order/{id}`

仅允许当前骑手查看自己已接订单。返回订单类型、店铺/商品摘要、取送联系人和地址、坐标、金额、支付状态、订单状态、商品明细和状态轨迹。

### 管理员骑手操作

| 方法 | URL | 请求体 | 规则 |
| --- | --- | --- | --- |
| POST | `/api/admin/rider/{id}/approve` | 无 | 仅审核中可通过，并启用骑手 |
| POST | `/api/admin/rider/{id}/reject` | `{"reason":"资料不完整"}` | 仅审核中可拒绝 |
| POST | `/api/admin/rider/{id}/enable` | 无 | 仅审核通过且已禁用可启用 |
| POST | `/api/admin/rider/{id}/disable` | `{"reason":"账号异常"}` | 仅审核通过且正常可禁用 |

`reason`去除首尾空格后长度为2至255。状态更新使用原状态条件保护并发。

### 骑手订单列表增强

`GET /api/rider/order/hall`和`GET /api/rider/order/my`在原字段基础上新增：

- `orderType`、`orderTypeText`
- `storeId`、`storeName`
- `pickupAddress`、`deliveryAddress`
- `goodsSummary`

`15_update_rider_application.sql`已于2026-07-22由用户人工执行。迁移后骑手申请、Profile、我的配送和订单详情真实接口已复测通过，不再出现`reject_reason`字段缺失错误。

`GET /api/rider/order/hall`要求当前用户是审核通过且已启用骑手；未申请、审核未通过或已禁用时返回业务码`403 当前用户不是骑手`。

当前`AdminRiderController`只提供审核、拒绝、启用和禁用四个写接口，尚未提供管理员骑手列表和详情GET接口。文档不得写入不存在的`GET /api/admin/rider`或`GET /api/admin/rider/{id}`。
