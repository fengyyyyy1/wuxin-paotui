# 五鑫跑腿（Wuxin Paotui）

项目开发状态

---

最后更新时间：2026-07-17

当前版本：V0.8 Completed

项目状态：开发中

---

## 一、项目简介

本项目是企业级跑腿服务平台。

项目采用 Spring Boot + MyBatis Plus + MySQL 技术栈，按照真实互联网项目标准持续开发。

最终目标：完成微信小程序上线运营。

## 二、当前完成模块

### 用户模块

- [x] 用户注册
- [x] 用户登录
- [x] BCrypt
- [x] JWT
- [x] 获取当前用户

### 地址模块

- [x] 新增地址
- [x] 修改地址
- [x] 删除地址
- [x] 地址列表
- [x] 默认地址
- [x] 地址权限控制

### 订单模块

- [x] 发布订单
- [x] 我的订单
- [x] 查看订单详情
- [x] OrderStatusEnum
- [x] BusinessException
- [x] GlobalExceptionHandler
- [x] 用户确认收货
- [x] 用户取消订单
- [x] 用户评价订单
- [x] PaymentStatusEnum
- [x] 订单模拟支付
- [x] 订单轨迹

### 骑手模块

- [x] 骑手大厅
- [x] 骑手接单
- [x] 骑手我的订单
- [x] 骑手完成配送
- [x] 骑手放弃订单

### 商家与店铺模块

- [x] 商家申请入驻
- [x] 我的商家资料
- [x] 修改店铺资料
- [x] 修改营业状态
- [x] 公开店铺列表
- [x] 公开店铺详情
- [x] 商品分类管理
- [x] 商品管理
- [x] 公开商品分类
- [x] 公开商品列表
- [x] 公开商品详情

## 三、数据库状态

数据库：

```text
wuxin_paotui
```

主要业务表：

- `sys_user`
- `user_address`
- `order_info`
- `order_log`
- `rider_info`
- `order_comment`
- `merchant_info`
- `merchant_store`
- `merchant_category`
- `merchant_product`

数据库升级：

`order_info` 新增字段：

- `accept_time`
- `finish_time`

`order_info` 新增索引：

- `idx_order_rider_status_deleted`
- `idx_order_status_deleted_create_time`

V0.5 新增：

- `order_comment` 订单评价表
- `uk_order_comment_order_id` 订单唯一评价索引
- 升级脚本 `05_create_order_comment.sql`

V0.6 新增：

- `order_info.pay_status`
- `order_info.pay_time`
- `order_info.payment_no`
- `idx_order_pay_status_deleted_create_time`
- `uk_order_payment_no`
- 升级脚本 `06_update_order_payment.sql`

V0.7 新增：

- `merchant_info` 商家主体表
- `merchant_store` 店铺表
- 商家与用户、店铺与商家一对一唯一索引
- 升级脚本 `07_create_merchant_store.sql`

V0.8 新增：

- `merchant_category` 商品分类表
- `merchant_product` 商品表
- 店铺分类名称唯一索引及商品查询索引
- 升级脚本 `08_create_product_tables.sql`

## 四、当前测试数据

测试用户：

| 字段 | 值 |
| --- | --- |
| username | `test001` |
| password | `123456` |
| userId | `2` |

当前身份：

- 普通用户
- 骑手

当前订单：

| 订单 ID | 状态 |
| --- | --- |
| id=1 | 待接单 |
| id=2 | 已接单 |

## 五、当前接口完成情况

### 用户

- [x] 注册
- [x] 登录
- [x] 获取当前用户

### 地址

- [x] 新增
- [x] 删除
- [x] 修改
- [x] 查询

### 订单

- [x] 发布订单
- [x] 我的订单
- [x] 订单详情
- [x] 用户确认收货
- [x] 用户取消订单
- [x] 用户评价订单
- [x] 订单模拟支付
- [x] 订单轨迹

### 骑手

- [x] 大厅
- [x] 接单
- [x] 我的订单
- [x] 完成配送
- [x] 放弃订单

### 商家

- [x] 申请入驻
- [x] 我的商家资料
- [x] 修改店铺资料
- [x] 修改营业状态

### 店铺

- [x] 公开店铺列表
- [x] 公开店铺详情
- [x] 公开商品分类
- [x] 公开商品列表
- [x] 公开商品详情

### 商品管理

- [x] 分类新增、修改、状态、删除和列表
- [x] 商品新增、修改、上下架、删除和分页列表

## 六、当前开发断点（最重要）

当前状态：

```text
V0.8 商品分类与商品管理已完成，SQL、Postman 和 Navicat 人工验证全部通过。
```

本次接口：

```http
POST /api/merchant/category
PUT /api/merchant/category/{id}
PUT /api/merchant/category/{id}/status
DELETE /api/merchant/category/{id}
GET /api/merchant/category/list
POST /api/merchant/product
PUT /api/merchant/product/{id}
PUT /api/merchant/product/{id}/status
DELETE /api/merchant/product/{id}
GET /api/merchant/product/list
GET /api/store/{storeId}/categories
GET /api/store/{storeId}/products
GET /api/store/product/{id}
```

下一步：

```text
购物车和商家商品订单
```

开发顺序：

```text
购物车
↓
商家商品订单
```

## 七、待开发模块

- [x] 商品分类
- [x] 商品管理
- [ ] 购物车
- [ ] 用户购买商品
- [ ] 商家订单
- [ ] 总控端商家审核
- [ ] 微信支付
- [ ] Redis
- [ ] OSS
- [ ] Docker
- [ ] Nginx
- [ ] Vue Admin
- [ ] 微信小程序

## 八、本版本完成内容（V0.4）

- [x] 我的订单
- [x] 订单详情
- [x] BusinessException
- [x] 统一异常
- [x] OrderStatusEnum
- [x] 骑手大厅
- [x] 骑手接单
- [x] 骑手我的订单
- [x] 骑手完成配送
- [x] 用户确认收货
- [x] 订单日志
- [x] 数据库升级

## 九、V0.5 开发内容

- [x] 用户取消订单接口
- [x] 订单取消原子条件更新
- [x] 用户取消订单日志
- [x] 骑手放弃订单接口
- [x] 骑手放弃原子条件更新
- [x] 骑手放弃订单日志
- [x] 用户评价订单接口
- [x] `order_comment` 增量建表脚本
- [x] 一单一评唯一约束
- [x] 用户评价订单日志
- [ ] Postman 验证
- [ ] Navicat 验证

## 十、V0.6 开发内容

- [x] PaymentStatusEnum
- [x] 订单创建默认未支付
- [x] 模拟支付接口
- [x] 支付原子条件更新与幂等控制
- [x] 支付单号唯一约束
- [x] 支付订单日志
- [x] 骑手大厅过滤未支付订单
- [x] 骑手接单禁止未支付订单
- [x] `06_update_order_payment.sql`
- [x] 订单轨迹接口
- [x] 多数据源时间轴组装
- [ ] SQL 验证
- [ ] Postman 验证
- [ ] Navicat 验证

## 十一、V0.7 开发内容

- [x] `merchant_info`、`merchant_store` 增量建表脚本
- [x] 商家申请和并发重复申请保护
- [x] 商家主体与店铺同事务创建
- [x] 商家资料和店铺管理接口
- [x] 公开店铺联表分页与详情
- [x] GET 店铺查询精确公开放行
- [ ] SQL 验证
- [ ] Postman 验证
- [ ] Navicat 验证

## 十二、V0.8 开发内容

- [x] `merchant_category`、`merchant_product` 增量建表脚本
- [x] 商家分类管理接口
- [x] 商家商品管理接口
- [x] 商品上下架规则
- [x] 公开分类、商品列表和商品详情
- [x] 分类名称并发重复保护
- [x] GET 商品查询精确公开放行
- [x] SQL 验证
- [x] Postman 验证
- [x] Navicat 验证

## 十三、项目规范

项目分层：

```text
Controller
↓
Service
↓
Mapper
↓
Entity
↓
VO
```

统一规范：

- [x] Result
- [x] BusinessException
- [x] GlobalExceptionHandler
- [x] DTO 接收请求参数
- [x] VO 返回前端数据
- [x] Entity 不直接返回前端

## 十四、开发流程

固定流程：

```text
需求设计
↓
开发实现
↓
Compile
↓
Postman
↓
Navicat
↓
更新 docs
↓
Git Commit（后续）
```
