# 测试环境

> 当前版本：V1.2 微信支付模块（第一阶段）
> 最近一次完整测试时间：2026-07-17

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
| V1.2数据库状态 | 待人工执行和验证 |

## 二、当前测试账号

| 身份 | username | password | userId |
| --- | --- | --- | --- |
| 管理员 | `admin` | `123456` | 待从 `sys_user` 确认并回填 |
| 普通用户 | `test001` | `123456` | `2` |
| 骑手 | `test001` | `123456` | `2` |

账号详情及维护规则见 `TEST_ACCOUNT.md`。

## 三、测试数据

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
| V1.0 商品订单 ID | 商品订单完整流程 | 本轮实际 ID 未在历史文档保存，需从 `order_info` 或 Postman 环境回填 |

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
| 商品订单ID | 创建测试商品订单后回填 |
| paymentNo | 创建支付单后回填 |
| payment_order SQL | 待人工执行 |
| Postman状态 | 24项测试待人工验收 |

第一阶段Mock允许测试账号`openid`为空，不得向`sys_user`写入伪造openid。

## 四、购物车状态

V1.0 验收后的业务规则：

- 创建商品订单后，仅已选购物车记录逻辑删除。
- 未选购物车记录继续保留。
- 已结算商品再次加入购物车时，可以恢复逻辑删除记录。
- 当前具体 `cartId` 未在历史文档保存，测试前从 `shopping_cart` 或 Postman 环境确认。

## 五、Token 获取

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

## 六、最近一次完整测试

测试日期：2026-07-17。

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
- SQL、Postman和Navicat等待人工验收。

## 七、维护规则

1. 测试账号变化时同步更新 `TEST_ACCOUNT.md`。
2. 地址、订单、商品、店铺、分类或购物车 ID 变化时立即更新本文件。
3. 每次完整测试后更新测试时间、范围和结论。
4. 所有数据以真实数据库、Postman 返回和 Navicat 查询结果为准。
