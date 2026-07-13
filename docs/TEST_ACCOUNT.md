# 测试账号

> 当前版本：V0.4

## 一、普通用户

| 字段 | 值 |
| --- | --- |
| username | `test001` |
| password | `123456` |
| userId | `2` |

当前身份：

- 普通用户
- 骑手

## 二、骑手状态

```text
audit_status = 1
rider_status = 1
```

说明：

- `audit_status = 1` 表示审核通过。
- `rider_status = 1` 表示骑手启用。

## 三、地址

| 地址 ID | 说明 |
| --- | --- |
| id=2 | 取件地址 |
| id=3 | 收件地址 |

## 四、订单

| 订单 ID | 状态 | 说明 |
| --- | --- | --- |
| id=1 | 待接单 | 可用于骑手大厅、骑手接单测试 |
| id=2 | 已接单 | `rider_id`、`accept_time` 已写入 |

## 五、Token 获取方式

接口：

```http
POST /api/user/login
```

请求示例：

```json
{
  "username": "test001",
  "password": "123456"
}
```

Postman 保存：

```text
{{token}}
```
