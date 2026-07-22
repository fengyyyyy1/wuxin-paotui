# 测试账号

> 当前版本：V1.5 总控端商家审核模块
> 当前数据库：`wuxin_paotui`

本文件统一维护项目测试账号。测试账号、密码、用户 ID 或身份发生变化时，必须同步修改本文档和 `TEST_ENVIRONMENT.md`。

## 一、管理员

| 字段 | 值 |
| --- | --- |
| username | `admin` |
| password | `123456` |
| userId | `1` |

说明：

- 本轮使用 `admin` 完成地址越权和订单越权测试。
- 管理员`userId=1`已通过真实数据库只读查询确认。
- V1.5管理员权限不再依据用户名或固定用户ID，而是要求该账号关联有效的`ADMIN`角色。
- 14号SQL已执行，`admin`已绑定`ADMIN`角色并通过管理员权限测试。

管理员角色验证：

```sql
SELECT u.id, u.username, r.role_code, ur.create_time
FROM sys_user u
INNER JOIN sys_user_role ur ON ur.user_id = u.id
INNER JOIN sys_role r ON r.id = ur.role_id
WHERE u.username = 'admin'
  AND u.is_deleted = 0;
```

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

## 四、商家测试账号

V1.4商家订单已通过人工验收。当前商家账号已通过真实数据库只读查询确认：

| 字段 | 值 |
| --- | --- |
| username | `test001` |
| password | `123456` |
| userId | `2` |
| merchantId | `1` |
| storeId | `1` |
| audit_status | 必须为`1` |
| merchant_status | 必须为`1` |
| store_status | 必须为`1` |

越权测试还需准备第二个审核通过的商家账号。禁止根据数据库自增顺序猜测账号或ID。

V1.5审核通过和审核拒绝必须分别使用两个新的普通测试账号提交独立商家申请。
当前`merchantId=1`已经审核通过，只适合测试禁用和重新启用，不能用于待审核流程。

V1.5测试账号用途：

| username | 用途 | Token记录 |
| --- | --- | --- |
| `admin` | 管理员登录、权限和商家管理测试 | 不记录 |
| `merchant_audit_01` | 提交审核通过测试申请 | 不记录 |
| `merchant_audit_02` | 提交审核拒绝测试申请 | 不记录 |

测试申请已分别生成`merchantId=2/storeId=2`和`merchantId=3/storeId=3`。
本文档不新增或保存这些账号的Token。

本轮验收记录：

| 项 | 值 |
| --- | --- |
| 验收状态 | V1.4商家订单管理人工验收通过 |
| orderId | `7` |
| orderNo | `WX20260718173934516783` |
| paymentNo | `PAY20260718174303093cf8566e8141b1ae648649340679c0` |
| 验收用途 | 商家接单、出餐、骑手接单完整链路 |

拒单验收使用独立订单，不能与完整配送链路复用：

| 项 | 值 |
| --- | --- |
| orderId | `8` |
| orderNo | `WX20260718180441574851` |
| storeId | `1` |
| userId | `2` |
| paymentNo | `PAY2026071818153359038b94fc2a4cfc918b822dd4611cd1` |
| 拒单结果 | `status = 8`，已关闭、待退款 |

确认SQL：

```sql
SELECT
    u.id AS user_id,
    u.username,
    m.id AS merchant_id,
    m.audit_status,
    m.merchant_status,
    s.id AS store_id,
    s.store_name,
    s.store_status
FROM sys_user u
INNER JOIN merchant_info m ON m.user_id = u.id
INNER JOIN merchant_store s ON s.merchant_id = m.id
WHERE u.is_deleted = 0
  AND m.is_deleted = 0
  AND s.is_deleted = 0
ORDER BY m.id;
```

## 五、Mock微信登录账号

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

## 六、Token 获取

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

商家Token同样通过`POST /api/user/login`获取，使用本节已确认的真实商家账号。

## 七、维护要求

1. 修改测试账号密码时，必须同步修改本文档。
2. 修改账号身份、用户 ID 或骑手状态时，必须同步修改本文档。
3. 测试数据 ID 和最近一次测试时间统一维护在 `TEST_ENVIRONMENT.md`。
4. 文档记录必须以真实数据库和实际登录结果为准，不得依赖记忆。
5. 管理员授权变化时必须同步本文档和`TEST_ENVIRONMENT.md`，不得记录固定Token。

## 八、V1.8双端联调身份

| 端 | 登录账号 | 真实身份与ID | 说明 |
| --- | --- | --- | --- |
| 骑手端 | `test001 / 123456` | `userId=2`、`riderId=1` | 当前数据库唯一骑手，已审核启用 |
| 商家端 | `test001 / 123456` | `merchantId=1`、`storeId=1` | 当前测试账号同时拥有商家与骑手身份 |
| 总控端 | `admin / 123456` | 管理员角色 | 用于`/api/admin/rider/**`权限回归 |

Token继续通过`POST /api/user/login`动态获取，不写入代码或文档。正式微信端身份需在配置正式AppID后验证，当前`touristappid`不能用于发布。
