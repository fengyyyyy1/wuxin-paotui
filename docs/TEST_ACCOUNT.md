# 测试账号

> 当前版本：V1.0 Completed
> 当前数据库：`wuxin_paotui`

本文件统一维护项目测试账号。测试账号、密码、用户 ID 或身份发生变化时，必须同步修改本文档和 `TEST_ENVIRONMENT.md`。

## 一、管理员

| 字段 | 值 |
| --- | --- |
| username | `admin` |
| password | `123456` |
| userId | 待从真实数据库 `sys_user` 确认并回填 |

说明：

- 本轮使用 `admin` 完成地址越权和订单越权测试。
- 当前仓库和历史文档没有可靠记录管理员 `userId`，禁止根据自增顺序猜测。

## 二、普通用户

| 字段 | 值 |
| --- | --- |
| username | `test001` |
| password | `123456` |
| userId | `2` |

说明：`test001` 是本轮商品、购物车、商品订单和订单权限测试的主要普通用户。

## 三、骑手

| 字段 | 值 |
| --- | --- |
| username | `test001` |
| password | `123456` |
| userId | `2` |

当前骑手状态：

```text
audit_status = 1
rider_status = 1
```

说明：

- 当前普通用户和骑手复用 `test001`。
- `audit_status = 1` 表示审核通过。
- `rider_status = 1` 表示骑手启用。

## 四、Token 获取

接口：

```http
POST /api/user/login
```

普通用户及骑手请求示例：

```json
{
  "username": "test001",
  "password": "123456"
}
```

管理员请求示例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

Token 仅保存在当前 Postman 环境变量中：

```text
{{token}}
```

禁止将固定 Token 写入代码或提交到版本库。

## 五、维护要求

1. 修改测试账号密码时，必须同步修改本文档。
2. 修改账号身份、用户 ID 或骑手状态时，必须同步修改本文档。
3. 测试数据 ID 和最近一次测试时间统一维护在 `TEST_ENVIRONMENT.md`。
4. 文档记录必须以真实数据库和实际登录结果为准，不得依赖记忆。
