# 五鑫跑腿部署指南

最后更新时间：2026-07-22

本文档用于V2.0上线冲刺阶段的首次部署准备。V2.0-P0-01已完成生产配置分层、日志治理、三端API地址环境化和生产环境变量示例。当前项目仍存在真实微信支付、退款和对象存储等P0阻塞。

## 一、部署前提

- Linux服务器，建议2核4G起步。
- Java 21。
- MySQL 8。
- Nginx。
- HTTPS证书和正式域名。
- 微信小程序正式AppID和AppSecret。
- 微信支付商户号、商户API证书、商户API私钥、商户证书序列号和APIv3 Key。
- 腾讯云COS或同等级对象存储，用于资质、头像、门店和商品图片。
- 能访问生产域名的微信开发者工具和真机。

## 二、当前部署风险

- 真实微信支付网关未实现，开启`WECHAT_PAY_ENABLED=true`后当前代码会找不到`WECHAT`网关。
- 微信支付回调接口未注册，支付成功无法由微信侧可靠通知后端。
- 微信退款未实现，已支付订单取消或拒单不能正式退款。
- 上传接口未实现，资质和图片目前只能手填HTTPS URL。
- 三个小程序已按微信环境自动切换API地址，但`https://test-api.待配置域名`和`https://api.待配置域名`必须上线前替换为真实域名。
- 生产日志已关闭Mapper DEBUG，仍需确认服务器`LOG_PATH`权限和日志保留策略。

## 三、SQL执行顺序

首次部署必须在空库或确认兼容的库上按顺序执行：

1. `sql/01_sys_user.sql`
2. `wuxin-paotui-server/src/main/resources/sql/02_update_sys_user_for_register.sql`
3. `wuxin-paotui-server/src/main/resources/sql/03_update_order_info_for_create_order.sql`
4. `wuxin-paotui-server/src/main/resources/sql/04_update_rider_accept_order.sql`
5. `wuxin-paotui-server/src/main/resources/sql/05_create_order_comment.sql`
6. `wuxin-paotui-server/src/main/resources/sql/06_update_order_payment.sql`
7. `wuxin-paotui-server/src/main/resources/sql/07_create_merchant_store.sql`
8. `wuxin-paotui-server/src/main/resources/sql/08_create_product_tables.sql`
9. `wuxin-paotui-server/src/main/resources/sql/09_create_shopping_cart.sql`
10. `wuxin-paotui-server/src/main/resources/sql/10_create_order_item_and_update_order.sql`
11. `wuxin-paotui-server/src/main/resources/sql/11_add_rider_ranking_index.sql`
12. `wuxin-paotui-server/src/main/resources/sql/12_create_payment_order.sql`
13. `wuxin-paotui-server/src/main/resources/sql/13_update_order_for_merchant_management.sql`
14. `wuxin-paotui-server/src/main/resources/sql/14_create_admin_merchant_audit.sql`
15. `wuxin-paotui-server/src/main/resources/sql/15_update_rider_application.sql`
16. `wuxin-paotui-server/src/main/resources/sql/16_create_admin_console.sql`

执行后至少回查：`sys_user`、`user_address`、`order_info`、`order_item`、`order_log`、`merchant_info`、`merchant_store`、`merchant_product`、`shopping_cart`、`rider_info`、`payment_order`、`system_config`、`admin_operation_log`。

## 四、后端生产环境变量

上线时不要依赖默认值，必须显式配置：

```bash
SERVER_PORT=8080
DB_URL=jdbc:mysql://生产MySQL:3306/wuxin_paotui?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
DB_USERNAME=生产账号
DB_PASSWORD=生产密码

WECHAT_MINI_PROGRAM_ENABLED=true
WECHAT_MINI_PROGRAM_APP_ID=正式小程序AppID
WECHAT_MINI_PROGRAM_APP_SECRET=正式小程序AppSecret
MOCK_WECHAT_LOGIN_ENABLED=false
MOCK_WECHAT_PHONE_ENABLED=false

WECHAT_PAY_ENABLED=true
WECHAT_PAY_APP_ID=正式小程序AppID
WECHAT_PAY_MCH_ID=商户号
WECHAT_PAY_MERCHANT_SERIAL_NUMBER=商户证书序列号
WECHAT_PAY_PRIVATE_KEY_PATH=/secure/path/apiclient_key.pem
WECHAT_PAY_API_V3_KEY=APIv3Key
WECHAT_PAY_NOTIFY_URL=https://api.example.com/api/payment/wechat/notify
MOCK_PAYMENT_ENABLED=false

OBJECT_STORAGE_ENABLED=false
OBJECT_STORAGE_PROVIDER=COS
OBJECT_STORAGE_REGION=replace-with-region
OBJECT_STORAGE_BUCKET=replace-with-bucket
OBJECT_STORAGE_SECRET_ID=replace-with-secret-id
OBJECT_STORAGE_SECRET_KEY=replace-with-secret-key
OBJECT_STORAGE_PUBLIC_BASE_URL=https://cdn.待配置域名
```

注意：当前真实微信支付网关和回调尚未完成，上述支付变量只能作为上线目标配置，不能代表当前代码已经可收款。

完整安全示例见：`deploy/env/application-prod.env.example`。

生产启动方式：

```bash
SPRING_PROFILES_ACTIVE=prod java -jar wuxin-paotui-server.jar
```

开发启动方式：

```bash
SPRING_PROFILES_ACTIVE=dev java -jar wuxin-paotui-server.jar
```

`prod`启动时会强制检查`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`和`SERVER_PORT`。真实微信登录或真实微信支付启用时，再检查对应微信变量；Mock模式不会要求尚未启用的真实支付变量。

## 五、三端API地址

三端统一在`miniprogram/config/env.ts`配置：

| 微信环境 | API地址 |
| --- | --- |
| 开发版 develop | `http://localhost:8080` |
| 体验版 trial | `https://test-api.待配置域名` |
| 正式版 release | `https://api.待配置域名` |

上线前必须将`待配置域名`替换为真实HTTPS域名，并在微信公众平台配置合法请求域名。

## 六、构建命令

### 后端

```bash
cd wuxin-paotui-server
./mvnw.cmd -q compile
./mvnw.cmd -q test
```

Windows开发机使用`mvnw.cmd`，Linux服务器使用`./mvnw`。

### 管理后台

```bash
cd wuxin-admin-web
npm install
npm run build
npm run lint
npm run type-check
```

### 用户端小程序

```bash
cd wuxin-miniapp
npm install
npm run build
npm run lint
npm run type-check
```

### 商家端小程序

```bash
cd wuxin-merchant-app
npm install
npm run build
npm run lint
npm run type-check
```

### 骑手端小程序

```bash
cd wuxin-rider-app
npm install
npm run build
npm run lint
npm run type-check
```

## 七、Nginx与HTTPS

生产建议结构：

- `https://api.example.com` 反向代理到 Spring Boot。
- `https://admin.example.com` 部署管理后台静态资源。
- 小程序后台配置合法请求域名为 `https://api.example.com`。
- 支付回调域名必须公网可访问且证书有效。

上线前必须验证：

- TLS证书链完整。
- HTTP自动跳转HTTPS。
- 上传文件大小限制满足资质图片需求。
- 后端真实IP和代理头处理符合日志审计要求。

## 八、微信小程序后台配置

- 配置request合法域名。
- 配置uploadFile合法域名。
- 配置downloadFile合法域名。
- 配置业务域名。
- 配置用户隐私保护指引。
- 配置服务类目、客服联系方式和审核说明。
- 支付能力需绑定商户号并配置支付回调域名。

## 九、上线验证

### 用户端

- 微信真实登录。
- 手机号授权。
- 首页配置、Banner、公告实时读取。
- 跑腿下单、商品下单、购物车、地址、支付、确认收货和评价。

### 商家端

- 微信真实登录。
- 入驻申请。
- 后台审核后状态刷新。
- 接单、拒单、出餐。

### 骑手端

- 微信真实登录。
- 骑手申请。
- 后台审核后进入接单大厅。
- 接单、完成配送。

### 管理端

- 管理员登录。
- Dashboard数据变化。
- 订单、用户、商家、骑手、商品、财务、配置和日志中心查询。
- 修改客服电话、公告、Banner和费用配置后，用户端无需发版即可读取。

## 十、回滚建议

- 每次上线前备份数据库。
- 保留上一版后端Jar和管理后台静态包。
- 小程序正式发布前保留上一版线上版本。
- 如支付、订单或登录出现P0故障，优先回滚后端并暂停小程序发布。

## 十一、上线前最低通过标准

- P0问题全部关闭。
- 后端测试、四端Build、Lint、TypeScript全部通过。
- 真机完成用户、商家、骑手、后台四端闭环。
- 微信支付和退款小额真实交易通过。
- 微信审核材料完整。
- 生产日志不输出敏感信息。
- 数据库备份和回滚路径明确。
