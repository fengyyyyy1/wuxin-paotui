# 测试环境

> 当前版本：V1.6 总控管理后台
> 最近一次完整测试时间：2026-07-18

本文件是项目测试环境、测试数据和验收断点的统一记录。以后所有测试环境变化必须维护本文件。

## 一、数据库

| 项目 | 当前值 |
| --- | --- |
| 数据库名称 | `wuxin_paotui` |
| 数据库类型 | MySQL 8 |
| V1.0 升级脚本 | `10_create_order_item_and_update_order.sql` |
| V1.0 数据库状态 | 已完成升级并通过人工验证 |
| V1.1 升级脚本 | `11_add_rider_ranking_index.sql` |
| V1.1 数据库状态 | 已执行并通过人工验证 |
| V1.2升级脚本 | `12_create_payment_order.sql` |
| V1.2数据库状态 | 已人工执行并通过Mock支付验证 |
| V1.3数据库变更 | 无，复用现有sys_user字段与索引 |
| V1.4升级脚本 | `13_update_order_for_merchant_management.sql` |
| V1.4数据库状态 | 已人工执行并通过Navicat验证 |
| V1.5升级脚本 | `14_create_admin_merchant_audit.sql` |
| V1.5数据库状态 | 已在当前测试数据库人工执行并通过Navicat验证 |
| V1.6数据库变更 | 无 |
| V1.6数据库状态 | 复用V1.5总控端后端表结构，管理后台前端人工验收通过 |

## 二、本地环境配置

后端数据库密码使用环境变量注入：

```properties
spring.datasource.password=${DB_PASSWORD:}
```

IDEA 本地运行配置：

```text
DB_PASSWORD=123456
```

开发环境使用 IDEA 环境变量注入，生产环境通过服务器环境变量配置，避免数据库密码进入代码仓库。

## 三、当前测试账号

| 身份 | username | password | userId |
| --- | --- | --- | --- |
| 管理员 | `admin` | `123456` | `1`，已绑定`ADMIN`角色 |
| 普通用户 | `test001` | `123456` | `2` |
| 骑手 | `test001` | `123456` | `2` |
| 商家 | `test001` | `123456` | `2` |
| 其他商家 | 待从真实数据库确认 | 待确认 | 待确认 |

账号详情及维护规则见 `TEST_ACCOUNT.md`。

## 四、测试数据

### 地址

| 地址 ID | 所属用户 | 用途 | 状态 |
| --- | --- | --- | --- |
| `4` | `test001`（userId=2） | V1.0 商品订单及地址越权测试 | 有效 |
| `2` | 历史测试数据 | 取件地址 | 使用前需在数据库确认 |
| `3` | 历史测试数据 | 收件地址 | 使用前需在数据库确认 |

### 订单

| 订单 ID | 用途 | 状态 |
| --- | --- | --- |
| `1` | 历史待接单测试订单 | 使用前需在数据库确认 |
| `2` | 历史已接单测试订单 | 使用前需在数据库确认 |
| `6` | V1.2商品订单Mock支付完整流程 | 已支付 |

### 商品、店铺和分类

| 数据 | 当前 ID | 维护说明 |
| --- | --- | --- |
| 商品 ID | 未在历史文档保存 | 从 `merchant_product` 或 Postman `productId` 回填 |
| 店铺 ID | 未在历史文档保存 | 从 `merchant_store` 或 Postman `storeId` 回填 |
| 分类 ID | 未在历史文档保存 | 从 `merchant_category` 或 Postman `categoryId` 回填 |

未记录的 ID 不得根据数据库自增顺序猜测。

### 骑手排行榜

| 数据 | 当前值 | 维护说明 |
| --- | --- | --- |
| 骑手 ID | 待从 `rider_info.id` 查询后回填 | 不得使用 `user_id` 代替 |
| 已完成订单 | 待从 `order_info` 查询后回填 | 必须满足 `status = 4`、`deleted = 0` |
| 完成时间 | 待从 `order_info.finish_time` 确认 | 今日、周、月榜的唯一时间依据 |
| V1.1 测试状态 | 已通过人工测试 | 排行榜接口、参数和 SQL 统计均通过 |

确认测试数据：

```sql
SELECT id, user_id, real_name, audit_status, rider_status
FROM rider_info
ORDER BY id;

SELECT id, rider_id, status, finish_time, deleted
FROM order_info
WHERE rider_id IS NOT NULL
ORDER BY id DESC;
```

### V1.2支付联调

| 数据 | 当前值 |
| --- | --- |
| 模式 | 本地Mock，不连接真实微信 |
| `MOCK_PAYMENT_ENABLED` | 人工测试时设置为`true` |
| `WECHAT_PAY_ENABLED` | `false` |
| 商品订单ID | `6` |
| paymentNo | `PAY20260718001905b85f724b684044e8a34221a040ab4ab9` |
| amountTotal | `200`分 |
| payment_order状态 | `2 SUCCESS` |
| order_info支付状态 | `pay_status=1` |
| payment_order SQL | 已人工执行 |
| Postman状态 | V1.2第一阶段已通过人工验收 |

V1.2支付Mock允许测试账号`openid`为空，不需要为支付测试修改`sys_user.openid`。

### V1.3微信登录联调

| 数据 | 当前值 |
| --- | --- |
| `MOCK_WECHAT_LOGIN_ENABLED` | 人工测试时设置为`true` |
| `WECHAT_MINI_PROGRAM_ENABLED` | 本地Mock测试时为`false` |
| 首次登录code | `mock-code-new-user` |
| 重复身份code | `mock-code-new-user-repeat` |
| 新微信测试用户ID | `3` |
| openid | `mock_o***user`（脱敏） |
| 微信登录状态 | 已通过人工验收 |
| Profile状态 | GET、PUT、参数校验和数据回查已通过 |

### V1.3微信手机号绑定联调

| 数据 | 当前值 |
| --- | --- |
| `MOCK_WECHAT_PHONE_ENABLED` | 默认`false`，人工测试时设置为`true` |
| 测试用户ID | `3` |
| 首次绑定code | `mock-phone-code-13800000003` |
| 更换手机号code | `mock-phone-code-13900000003` |
| 无效code | `mock-phone-code-invalid` |
| 数据库变更 | 无，复用`sys_user.phone` |
| 当前状态 | Postman与Navicat人工验收通过 |

不得在本文档记录真实AppSecret、session_key、完整openid或完整unionid。

### V1.4商家订单联调

| 数据 | 当前值 |
| --- | --- |
| 商家userId | `2` |
| merchantId | `1` |
| storeId | `1`（V1.4验收订单） |
| 其他商家userId | 越权测试前确认 |
| 商品ID | 待从当前商家店铺商品确认 |
| 收货地址ID | `4`，使用前再次确认归属 |
| 商家接单订单ID | `7` |
| 商家拒单订单ID | `8` |
| SQL状态 | 13号升级脚本已执行 |
| Postman状态 | 人工验收通过 |
| Navicat状态 | 人工验收通过 |

测试订单必须通过正常购物车、创建商品订单和Mock支付流程生成，不直接伪造正式业务数据。

### V1.5总控端商家审核联调

| 数据 | 当前值 |
| --- | --- |
| 管理员username | `admin` |
| 管理员userId | `1` |
| 管理员角色 | 已绑定`ADMIN` |
| 审核通过账号 | `merchant_audit_01` |
| 审核通过merchantId/storeId | `2 / 2` |
| 审核拒绝账号 | `merchant_audit_02` |
| 审核拒绝merchantId/storeId | `3 / 3` |
| 启用/禁用merchantId | `1` |
| SQL状态 | 14号升级脚本已人工执行 |
| Postman状态 | 人工验收通过 |
| Navicat状态 | 人工验收通过 |
| 权限测试 | 普通用户访问`/api/admin/**`返回403 |

两笔申请必须来自不同普通测试账号。不得把同一申请同时用于审核通过和审核拒绝，
不得在文档或仓库保存管理员Token。

`merchantId=1`已完成禁用和重新启用测试，重新启用后`business_status=0`；
`merchantId=2`用于审核通过，`merchantId=3`用于审核拒绝。测试数据用途不得混用。

### V1.6总控管理后台联调

| 数据 | 当前值 |
| --- | --- |
| 管理员username | `admin` |
| 管理员userId | `1` |
| 管理员角色 | 已绑定`ADMIN` |
| 审核通过merchantId | `4` |
| 审核通过结果 | `audit_status=1`，`audit_admin_id=1`，`audit_remark=材料准确 允许通过` |
| 审核拒绝merchantId | `5` |
| 审核拒绝结果 | `audit_status=2`，`audit_admin_id=1`，`reject_reason=营业执照信息不清晰，请重新上传。` |
| 前端状态 | V1.6总控管理后台开发完成，人工验收通过 |

V1.6人工验收通过项：

1. 管理员登录测试
2. 普通用户访问后台403测试
3. 商家申请入驻流程测试
4. 审核通过测试
5. 审核拒绝测试
6. 商家禁用测试
7. 商家启用测试
8. 列表到详情跳转测试

## 五、购物车状态

V1.0 验收后的业务规则：

- 创建商品订单后，仅已选购物车记录逻辑删除。
- 未选购物车记录继续保留。
- 已结算商品再次加入购物车时，可以恢复逻辑删除记录。
- 当前具体 `cartId` 未在历史文档保存，测试前从 `shopping_cart` 或 Postman 环境确认。

## 六、Token 获取

最近一次 Token 获取接口：

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

登录成功后将返回值保存到当前 Postman 环境变量：

```text
{{token}}
```

Token 会过期，不在文档中保存固定 Token。

微信登录Token获取接口：

```http
POST /api/user/wechat/login
```

## 七、最近一次完整测试

测试日期：2026-07-18。

测试范围：

- 用户登录与 JWT
- 地址管理
- 商品管理
- 购物车
- 商品订单
- 骑手大厅
- 骑手接单
- 我的订单
- 订单详情
- 地址归属权限
- 订单归属权限
- 正常流程
- 异常流程

测试结论：

```text
V1.0 全部测试通过。
```

V1.1 骑手跑单排行榜模块已完成并通过人工验收。

V1.1 人工测试结果：

| 测试项 | 结果 |
| --- | --- |
| 累计总榜 | 通过 |
| 今日榜 | 通过 |
| 本周榜 | 通过 |
| 本月榜 | 通过 |
| 骑手个人统计 | 通过 |
| type 参数校验 | 通过 |
| limit 范围校验 | 通过 |
| SQL 统计正确性 | 通过 |

V1.2第一阶段当前结果：

- Java 21 Maven Compile已通过。
- 未连接真实微信支付。
- `12_create_payment_order.sql`已人工执行。
- 商品订单`id=6`完成Mock支付，流水金额`200`分。
- 支付流水为`SUCCESS(2)`，订单`pay_status=1`。
- 重复确认未重复写订单日志，幂等验证通过。

V1.3当前结果：

- 微信小程序登录代码已完成。
- 首次Mock登录曾因73字节随机原始密码超过BCrypt 72字节上限而失败。
- 随机原始密码已改为单个UUID，固定36个UTF-8字节。
- 首次/重复Mock登录与BCrypt单元回归测试已通过。
- Mock微信登录已通过Postman和Navicat人工验收。
- Profile GET、PUT、参数校验和数据回查已通过人工验收。
- 微信手机号绑定代码、自动化测试、Postman与Navicat人工验收均已完成。
- 真实code2session尚未使用真实AppID和AppSecret联调。

V1.4当前结果：

- 商家订单分页、详情、接单、拒单和出餐代码已完成。
- 商品订单骑手大厅准入规则已完成。
- 商家订单与骑手兼容自动化测试已通过。
- `13_update_order_for_merchant_management.sql`已人工执行。
- 订单`7`已通过商家接单、出餐和骑手接单完整链路验收。
- 独立订单`8`已通过支付后商家拒单验收，状态进入`8 已关闭，待退款`。
- Postman和Navicat人工验收均已通过。
- 真实退款尚未实现，拒单订单等待后续退款处理。

V1.6当前结果：

- 总控管理后台开发完成，并通过人工验收。
- 管理员登录、ADMIN角色验证和普通用户403权限测试通过。
- 商家分页列表、搜索筛选、详情页和列表到详情跳转测试通过。
- 商家审核通过、审核拒绝、禁用和启用测试通过。
- 店铺状态同步已在验收流程中确认。
- Element Plus按需加载优化已完成，前端构建检查通过。

## 八、维护规则

1. 测试账号变化时同步更新 `TEST_ACCOUNT.md`。
2. 地址、订单、商品、店铺、分类或购物车 ID 变化时立即更新本文件。
3. 每次完整测试后更新测试时间、范围和结论。
4. 所有数据以真实数据库、Postman 返回和 Navicat 查询结果为准。
