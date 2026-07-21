# 测试环境

> 当前版本：V1.7 用户微信小程序
> 最近一次完整测试时间：2026-07-22

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
| V1.7数据库变更 | 无 |
| V1.7数据库状态 | 复用现有用户、微信登录、Profile、地址和门店表结构 |

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
| 店铺 ID | `1` | V1.7-7A商品链路复测店铺，需保持`store_status=1`、`business_status=1` |
| 分类 ID | `2` | V1.7-7A商品链路复测分类，需保持启用且未删除 |
| 商品 ID | `2` | V1.7商品链路复测商品，需保持上架、未删除且库存大于0 |
| 门店图 | `/assets/images/home/store-placeholder.svg` | 由无效测试值`logo-test-2`修正 |
| 商品图 | `/assets/images/product-placeholder.svg` | 由不可访问外链`https://test.com/test.jpg`修正 |

V1.7-7A已确认：若店铺`business_status=0`，公开分类和商品列表会为空；门店详情仍可能正常返回。
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

V1.7第一阶段当前结果：

- 已创建`wuxin-miniapp`用户微信小程序目录。
- 已完成微信原生小程序 + TypeScript基础工程。
- 已配置Vant Weapp、ESLint、Prettier和npm依赖管理。
- 已建立请求层、Token存储、Bearer Token注入和401跳转登录页。
- 已封装微信登录、当前用户和Profile接口。
- 已创建登录、首页、地址、订单和个人中心页面骨架。
- 小程序`npm run type-check`、`npm run lint`和`npm run build`已通过。
- 未启动后端，未调用8080接口，未执行SQL。

V1.7第二阶段当前结果：

- 微信登录页面与真实后端接口联调代码已完成。
- 登录成功后保存`token`、`userInfo`和`newUser`。
- App启动时可恢复本地登录状态。
- 首页、地址列表、订单列表和个人中心已接入统一登录保护。
- 个人中心已支持退出登录确认和认证信息清理。
- 请求层已统一处理HTTP 401和业务`code=401`。
- 本地Mock微信登录默认关闭，可通过微信开发者工具Storage临时开启。
- 小程序`npm run type-check`、`npm run lint`和`npm run build`已通过。
- 未启动后端，未调用8080接口，未执行SQL。

V1.7第三阶段当前结果：

- 首页进入后刷新`GET /api/user/profile`并展示用户资料。
- 个人中心展示头像、昵称、用户名、手机号和性别。
- 昵称为空显示“微信用户”，手机号为空显示“未绑定”。
- 已新增资料编辑页面，支持修改昵称和头像URL。
- 编辑资料调用`PUT /api/user/profile`，成功后刷新Profile并同步Storage和AuthState。
- App启动和页面鉴权使用`GET /api/user/profile`验证Token有效性。
- 小程序`npm run type-check`、`npm run lint`和`npm run build`已通过。
- 未启动后端，未调用8080接口，未执行SQL。

V1.7第四阶段当前结果：

- Profile页面已接入微信手机号绑定入口。
- 手机号为空时显示“立即绑定”，手机号已存在时显示脱敏号码。
- 小程序调用`wx.getPhoneNumber()`获取授权`code`，并提交`POST /api/user/phone/bind`。
- 本地开发继续使用`MockWeChatPhoneGateway`，默认测试code为`mock-phone-code-13800000003`。
- 绑定成功后刷新Profile，并同步Home、Storage、AuthState和`App.globalData`。
- 用户拒绝授权时提示“已取消手机号授权”，接口失败时展示后端`message`。
- 本阶段未开发短信验证码登录、腾讯云短信或手机号修改。
- 未修改后端，未修改数据库，未执行SQL，未启动后端，未调用8080接口。

V1.7第五阶段当前结果：

- 地址管理从个人中心“收货地址”进入。
- 已接入`GET /api/user/address/list`、`POST /api/user/address`和`DELETE /api/user/address/{id}`。
- 地址列表展示收件人、脱敏手机号、完整地址、默认标签和操作按钮。
- 默认地址置顶展示，空数据展示空状态。
- 新增地址支持收件人、手机号、省、市、区、详细地址和默认地址。
- 新增地址校验收件人、11位手机号和详细地址。
- 删除地址使用确认弹窗，成功后刷新列表。
- 页面返回和手动刷新均重新加载地址列表。
- 地址卡片、圆角、阴影、留白、按钮和底部固定新增按钮已完成基础美化。
- V1.7-5A已补齐`PUT /api/user/address/{id}`和`PUT /api/user/address/{id}/default`。
- 编辑已有地址和将已有地址设为默认已改为真实接口调用。
- 后端按当前JWT用户校验地址归属，并保证同一用户最多一个默认地址。
- 未修改数据库，未执行SQL，未启动后端，未调用8080接口。

V1.7第六阶段当前结果：

- 首页已升级为正式产品首页骨架。
- 顶部用户区域调用`GET /api/user/profile`刷新用户昵称、头像和手机号状态。
- 默认地址调用`GET /api/user/address/list`，优先展示默认地址摘要。
- 推荐门店调用真实公开接口`GET /api/store/list`，每页展示6条。
- 门店详情占位页调用真实公开接口`GET /api/store/{id}`，不调用商品接口。
- Banner、核心服务入口和公益入口为本地静态展示数据。
- 公益页不展示真实个人敏感信息，不收集求助内容，不提交救援订单。
- 搜索页为占位入口，不伪造后端搜索结果。
- 未新增后端接口，未修改数据库，未执行SQL，未启动后端，未调用8080接口。

V1.7第七阶段当前结果：

- 门店详情正式页已接入真实`GET /api/store/{id}`。
- 商品分类已接入真实`GET /api/store/{storeId}/categories`。
- 商品列表已接入真实`GET /api/store/{storeId}/products`。
- 商品详情已接入真实`GET /api/store/product/{id}`。
- 搜索页已接入真实`GET /api/store/list?keyword=...`门店关键词搜索。
- 店内商品支持分类切换、商品关键词搜索、分页加载、空状态和错误重试。
- 商品图片继续使用`normalizeImageUrl()`降级，本地默认商品图为`/assets/images/product-placeholder.svg`。
- V1.7-7阶段“加入购物车”仅提示V1.7-8开放，不调用购物车接口。
- 未新增后端接口，未修改数据库，未执行SQL，未启动后端，未调用8080接口。

V1.7-7A商品链路审计当前结果：

- 商品分类和商品列表为空的原因已确认：测试店铺`storeId=1`原为`business_status=0`，被公开商品链路过滤。
- 后端公开商品分类、商品列表和商品详情要求商家审核通过、商家启用、店铺启用、店铺营业、分类启用、商品上架且库存大于0。
- 已修正当前测试库测试数据：`merchant_store.id=1`设置为`business_status=1`。
- 复测`GET /api/store/1`返回`businessStatus=1`。
- 复测`GET /api/store/1/categories`返回`categoryId=2`。
- 复测`GET /api/store/1/products?pageNum=1&pageSize=10`返回`productId=2`。
- 复测`GET /api/store/product/2`返回商品详情。
- 未修改后端代码、前端代码、数据库结构或SQL文件。
- 当前断点：V1.7-7人工复测。

V1.7商品链路人工复测问题修复结果：

- 图片失败真实URL为`https://test.com/test.jpg`，来源为`merchant_product.id=2.product_image`测试数据。
- `merchant_store.id=1.store_logo`原值为`logo-test-2`，不是可访问图片地址。
- 已将`merchant_product.id=2.product_image`修正为`/assets/images/product-placeholder.svg`。
- 已将`merchant_store.id=1.store_logo`修正为`/assets/images/home/store-placeholder.svg`。
- 商品列表、商品详情、首页门店和搜索门店均已增加图片失败兜底保护，兜底后不再重复处理。
- 商品搜索栏已二次修复为两行布局：第一行只保留输入框，第二行放搜索和清空按钮。
- 清空按钮常驻占位，无关键词时禁用，聚焦和输入长关键词时不再改变输入框宽度。
- 生产环境商品图、门店图和头像必须统一使用腾讯云COS HTTPS地址，不使用`example.com`、`test.com`、`localhost`、`file:`或Windows本地路径。
- 当前断点：V1.7-7人工复测。

V1.7第八阶段购物车当前结果：

- 商品详情页已接入真实`POST /api/cart/add`。
- 购物车列表已接入真实`GET /api/cart/list`。
- 数量增减已接入真实`PUT /api/cart/update`。
- 单项选中/取消已接入真实`PUT /api/cart/selected`。
- 全选/取消全选已接入真实`PUT /api/cart/selected/all`。
- 删除商品已接入真实`DELETE /api/cart/{id}`。
- 清理失效商品已接入真实`DELETE /api/cart/invalid`。
- 清空购物车已接入真实`DELETE /api/cart/clear`。
- 商品详情、门店详情和个人中心购物车角标同步非敏感数量，不缓存可信金额。
- “我的”页面新增商家入驻和申请成为骑手占位入口，完整申请流程留到V1.8。
- 真实接口回归已覆盖空购物车、首次加购、重复加购、修改数量、单项选中、全选、取消全选、清理失效商品、删除和清空。
- 当前公开测试数据只有一个可用门店商品，跨店铺冲突需补充第二个可用门店商品后继续复测。
- V1.7-8完成时断点曾为V1.7-9提交订单，当前已暂停进入V1.7-8A人工复测。

V1.7-8A购物车交互重构当前结果：

- 门店商品列表支持直接快捷加购，未加入时显示圆形“+”，已加入时显示`[- 数量 +]`。
- 门店商品列表加购、增加、减少和删除均调用真实购物车接口，不只改前端数量。
- 门店详情页底部固定折叠购物车栏展示真实购物车数量和后端返回的已选合计金额。
- 点击底部购物车栏可打开底部购物车弹层。
- 购物车弹层支持商品查看、数量增减、删除、清空、遮罩关闭和关闭按钮关闭。
- 独立购物车页保留，并已调整为稳定横向商品卡片布局。
- 商品列表、底部栏、弹层、独立购物车页、商品详情页和个人中心角标统一通过真实购物车接口同步状态。
- 本阶段未开发订单创建、支付、配送费或优惠券。
- 当前断点：V1.7-8A人工复测。

V1.7-8B购物车UI重构当前结果：

- 本阶段只按《购物车设计规范》调整WXML/WXSS和少量UI适配，不修改购物车接口、数据库、后端或业务逻辑。
- 门店详情页购物车相关UI已统一页面背景`#F6F7F9`、16px卡片圆角、卡片阴影和16px页面左右留白。
- 门店商品卡、底部购物车栏、BottomSheet弹层和独立购物车页均按设计稿统一商品图片、数量控制器、按钮和层级。
- 商品图片统一为72×72，数量控制器高度统一为36px，Popup顶部圆角统一为24px。
- 当前仍未开发订单创建、支付、配送费或优惠券。
- 该阶段断点曾为：V1.7-8B人工复测。

V1.7-8C购物车前端布局彻底重构当前结果：

- 不继续在旧WXML/WXSS上局部修补，门店详情页和独立购物车页购物车布局已重新组织。
- 门店商品卡使用固定图片区、可收缩商品信息区、价格区和数量控制器，避免文本竖排、按钮覆盖和横向溢出。
- 门店底部购物车栏使用左侧购物车图标/角标、中间金额与已选数量、右侧绿色去结算按钮，并适配安全区域。
- BottomSheet弹层使用遮罩、顶部标题/清空/关闭、中间商品列表和底部金额/去结算结构。
- 独立购物车页使用门店卡、商品列表卡、选择框、72×72商品图、商品信息、数量控制器和底部结算栏。
- 本阶段未修改购物车接口、后端、数据库、SQL、业务规则、订单或支付逻辑。
- 当前断点：V1.7-8C人工复测。

V1.7本地小程序开发环境：

| 项 | 配置 |
| --- | --- |
| 小程序目录 | `wuxin-miniapp` |
| 后端本地地址 | `http://localhost:8080` |
| Mock微信登录 | `MOCK_WECHAT_LOGIN_ENABLED=true` |
| Mock微信手机号绑定 | `MOCK_WECHAT_PHONE_ENABLED=true` |
| 真实微信登录 | 本地Mock阶段使用`WECHAT_MINI_PROGRAM_ENABLED=false` |
| 小程序开发Mock常量 | `USE_MOCK_WECHAT_LOGIN=true`时使用固定Mock code |
| 小程序手机号Mock常量 | `USE_MOCK_WECHAT_PHONE_BIND=true`时使用固定Mock phone code |
| 首页门店接口 | `GET /api/store/list` |
| 门店详情接口 | `GET /api/store/{id}` |
| 商品分类接口 | `GET /api/store/{storeId}/categories` |
| 商品列表接口 | `GET /api/store/{storeId}/products` |
| 商品详情接口 | `GET /api/store/product/{id}` |
| 购物车接口 | `/api/cart/*`，包含加购、列表、数量、选中、全选、删除、清理失效和清空 |
| 首页临时静态数据 | Banner、核心服务入口、公益入口 |
| 微信开发者工具 | 导入`wuxin-miniapp`后执行“构建npm” |
| 小程序Mock Storage开关 | `WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_LOGIN=true` |
| 小程序手机号Mock Storage开关 | `WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_PHONE=true` |
| 默认Mock code | `mock-code-new-user` |
| 默认手机号Mock code | `mock-phone-code-13800000003` |

微信开发者工具本地联调要求：

1. 开发阶段勾选“不校验合法域名、web-view、TLS版本以及HTTPS证书”。
2. 生产环境不得继续关闭域名校验。
3. Mock微信登录仅用于本地开发联调，release环境强制禁用。
4. 如临时修改`miniprogram/config/env.ts`中的`USE_MOCK_WECHAT_LOGIN=true`做本地联调，上线前必须恢复生产配置，保持Mock关闭。
5. `MOCK_WECHAT_PHONE_ENABLED=true`和`USE_MOCK_WECHAT_PHONE_BIND=true`仅用于开发环境，正式上线必须关闭Mock手机号绑定。
6. 正式上线前必须将小程序`API_BASE_URL`替换为HTTPS合法域名。
7. 正式上线前必须替换或接入首页本地临时展示数据。
8. 微信开发者工具偶发`WAServiceMainContext.js Error: timeout`，在业务页面和Network请求正常时不作为阻塞项。

V1.7.3稳定性修复结果：

- 修复`pages/profile/edit`目录导入导致微信运行时白屏的问题。
- 修复`App.onLaunch`阶段`restoreSession()`访问`globalData`过早的问题。
- Home、Profile、Edit Profile页面已补充判空和本地默认头像。
- 小程序`npm run type-check`、`npm run lint`和`npm run build`已通过。

## 八、维护规则

1. 测试账号变化时同步更新 `TEST_ACCOUNT.md`。
2. 地址、订单、商品、店铺、分类或购物车 ID 变化时立即更新本文件。
3. 每次完整测试后更新测试时间、范围和结论。
4. 所有数据以真实数据库、Postman 返回和 Navicat 查询结果为准。
