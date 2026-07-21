# 五鑫跑腿用户微信小程序

用户端整体重构代码已完成。项目使用微信原生小程序和TypeScript，保持现有后端接口、Token、地址、购物车、订单及支付业务规则不变。

## 技术栈

- 微信原生小程序
- TypeScript
- 微信原生组件
- ESLint
- Prettier
- npm依赖管理

## 页面

TabBar：

- 首页：`/pages/home/index`
- 订单：`/pages/order/list/index`
- 我的：`/pages/profile/index`

主要业务页面：

- 登录、搜索、门店列表、门店详情、商品详情、购物车
- 确认订单、订单详情、支付中、支付成功、支付失败
- 地址列表、地址编辑、用户资料编辑
- 商家入驻、骑手申请、公益服务、关于五鑫、设置

`app.json`当前注册22个页面。

## 真实业务接口

- 微信登录与资料：`/api/user/*`
- 地址管理：`/api/user/address/*`
- 公开门店与商品：`/api/store/*`
- 购物车：`/api/cart/*`
- 结算、商品订单和订单查询：`/api/order/*`
- 微信JSAPI支付：`/api/payment/*`
- 商家入驻：`POST /api/merchant/apply`

首页热门商品、推荐商品和猜你喜欢由真实公开门店商品接口组合，不使用虚构业务数据。后端没有评分、月售、优惠券和配送时长字段，页面不伪造这些信息。

## 本地开发

```bash
npm install
npm run type-check
npm run lint
npm run build
```

使用微信开发者工具导入`wuxin-miniapp`。当前开发API地址位于`miniprogram/config/env.ts`。

本地开发可按`docs/TEST_ENVIRONMENT.md`启用微信登录和手机号绑定Mock配置。生产环境必须关闭所有Mock开关，并将API地址配置为微信合法HTTPS域名。

## 认证与状态

- 请求层自动注入`Authorization: Bearer <token>`。
- HTTP或业务`401`会清理认证信息并跳转登录页。
- Token、用户资料和购物车角标使用统一Storage key和服务层同步。
- 不保存用户密码、真实Token、AppSecret、session_key或完整openid到代码仓库。

## 上线前边界

- 骑手线上申请暂无后端提交接口，当前页面只展示申请条件与真实边界。
- 真实微信支付需要生产AppID、商户号、API证书、回调地址和真机联调。
- 图片生产地址应使用稳定的腾讯云COS HTTPS域名。
- 提交审核前必须在微信开发者工具和真机完成所有页面、路由、支付及授权流程验收。
