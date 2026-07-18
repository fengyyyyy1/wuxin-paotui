# 测试账号

> 当前版本：V1.3 微信用户体系
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

## 四、Mock微信登录账号

| 项 | 值 |
| --- | --- |
| 首次登录code | `mock-code-new-user` |
| 重复身份code | `mock-code-new-user-repeat` |
| 测试用户ID | `3` |
| username | `wx_81e9fe4cdaded2877536f2e26ea529aa` |
| openid | `mock_o***user`（脱敏） |
| 验收状态 | Mock首次及重复登录已通过 |
| Profile状态 | GET、PUT、参数校验和数据回查已通过 |
| 手机号绑定状态 | Postman与Navicat人工验收通过 |

说明：

- 两个code映射同一个本地Mock微信身份。
- `mock-code-test001`不会绑定已有`test001`，调用后会创建独立微信用户。
- 不在本文档记录真实AppSecret、session_key、完整openid或完整unionid。

Mock手机号授权 code：

| code | 手机号 | 用途 |
| --- | --- | --- |
| `mock-phone-code-13800000003` | `13800000003` | 首次绑定与幂等测试 |
| `mock-phone-code-13900000003` | `13900000003` | 更换手机号测试 |
| `mock-phone-code-invalid` | 无 | 无效凭证异常测试 |

手机号授权 code 仅用于本地 Mock。测试时需设置 `MOCK_WECHAT_PHONE_ENABLED=true`，默认配置保持关闭。

## 五、Token 获取

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

微信用户Token通过以下接口获取：

```http
POST /api/user/wechat/login
```

禁止将固定 Token 写入代码或提交到版本库。

## 六、维护要求

1. 修改测试账号密码时，必须同步修改本文档。
2. 修改账号身份、用户 ID 或骑手状态时，必须同步修改本文档。
3. 测试数据 ID 和最近一次测试时间统一维护在 `TEST_ENVIRONMENT.md`。
4. 文档记录必须以真实数据库和实际登录结果为准，不得依赖记忆。
