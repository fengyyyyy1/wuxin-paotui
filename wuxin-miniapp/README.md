# 五鑫跑腿用户微信小程序

V1.7-8D Design System V1.0 与门店详情页 UI 全量重建已完成代码侧改造，当前包含基础工程、请求层、认证状态、微信登录闭环、Profile、手机号绑定、地址管理、首页门店推荐、门店详情、商品分类、商品列表、商品详情、门店搜索和购物车。

## 技术栈

- 微信原生小程序
- TypeScript
- Vant Weapp
- ESLint
- Prettier

## 本地开发

1. 后端通过 IDEA 启动，端口为 `8080`。
2. IDEA 本地运行配置中确认数据库密码环境变量：
   - `DB_PASSWORD=123456`
3. 本地 Mock 微信登录时，后端环境变量设置：
   - `MOCK_WECHAT_LOGIN_ENABLED=true`
   - `WECHAT_MINI_PROGRAM_ENABLED=false`
4. 本地 Mock 微信手机号绑定时，后端环境变量设置：
   - `MOCK_WECHAT_PHONE_ENABLED=true`
5. 小程序目录执行：

```bash
npm install
```

6. 使用微信开发者工具导入 `wuxin-miniapp`。
7. 在微信开发者工具中执行“工具 -> 构建 npm”。
8. 开发阶段勾选“不校验合法域名、web-view、TLS版本以及HTTPS证书”。
9. 本地开发基础 API 地址在 `miniprogram/config/env.ts` 中配置为 `http://localhost:8080`。

生产环境不得关闭域名校验，不得使用本地Mock登录或Mock手机号绑定。

## 微信登录联调

默认模式：

- 点击“微信一键登录”。
- 小程序调用`wx.login()`获取临时`code`。
- 小程序只提交`code`到`POST /api/user/wechat/login`。
- 登录成功后保存`token`、`userInfo`和`newUser`。

本地Mock模式：

1. 后端设置：

```text
MOCK_WECHAT_LOGIN_ENABLED=true
WECHAT_MINI_PROGRAM_ENABLED=false
```

2. 微信开发者工具Storage设置：

```text
WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_LOGIN=true
```

3. 默认Mock code为：

```text
mock-code-new-user
```

Mock模式默认关闭，并且小程序release环境强制禁用。

不要在文档或代码中记录真实 Token、密码、AppSecret、openid 或 session_key。

## 当前页面

- `/pages/login/index`：微信登录页
- `/pages/home/index`：首页用户信息、默认地址、服务入口、公益入口和推荐门店
- `/pages/search/index`：门店关键词搜索，商品搜索在门店内完成
- `/pages/cart/index`：购物车，支持真实列表、数量、选中、删除、清理失效商品和清空
- `/pages/store/detail/index`：门店详情、商品分类和商品列表
- `/pages/product/detail/index`：商品详情，展示真实商品基础信息并支持加入购物车
- `/pages/apply/merchant/index`：商家入驻占位入口，完整流程留到V1.8
- `/pages/apply/rider/index`：申请成为骑手占位入口，完整流程留到V1.8
- `/pages/public/missing/index`：走失儿童公益信息说明页
- `/pages/public/bullying/index`：校园欺凌免费求助说明页
- `/pages/public/rescue/index`：紧急免费救援说明页
- `/pages/address/list/index`：地址列表，支持真实接口加载、删除和刷新
- `/pages/address/edit/index`：新增地址和编辑地址入口
- `/pages/order/list/index`：订单列表骨架，已接入登录保护
- `/pages/profile/index`：个人中心资料展示和退出登录
- `/pages/profile/edit/index`：编辑资料，支持修改昵称和头像URL

## Profile联调

- 首页和个人中心进入时会调用`GET /api/user/profile`刷新资料。
- 编辑资料提交`PUT /api/user/profile`。
- 本阶段仅支持输入头像URL，不上传图片。
- 修改成功后会刷新Profile，并同步Storage与AuthState。

## 手机号绑定联调

- Profile页面手机号为空时显示“立即绑定”。
- 真实模式点击后调用`wx.getPhoneNumber()`，只提交授权`code`到`POST /api/user/phone/bind`。
- 本地Mock模式使用固定code`mock-phone-code-13800000003`。
- 绑定成功后刷新Profile，并同步Home、Storage、AuthState和`App.globalData`。
- 手机号已存在时显示脱敏号码，不提供手机号修改入口。
- 本阶段不开发短信验证码登录、腾讯云短信或图片上传。

## 地址管理联调

- 个人中心点击“收货地址”进入地址列表。
- 地址列表调用`GET /api/user/address/list`。
- 新增地址调用`POST /api/user/address`。
- 编辑地址调用`PUT /api/user/address/{id}`。
- 设置默认地址调用`PUT /api/user/address/{id}/default`。
- 删除地址调用`DELETE /api/user/address/{id}`。
- 地址卡片展示收件人、脱敏手机号、完整地址和默认标签。
- 默认地址置顶展示。
- 新增地址支持收件人、手机号、省、市、区、详细地址和默认地址。
- 返回列表和手动刷新都会重新加载地址。
- 后端保证同一用户最多一个默认地址，并禁止操作他人地址。

## 首页商家与服务入口

真实接口数据：

- 用户资料：`GET /api/user/profile`
- 默认地址：`GET /api/user/address/list`
- 推荐门店：`GET /api/store/list`
- 门店详情：`GET /api/store/{id}`
- 商品分类：`GET /api/store/{storeId}/categories`
- 商品列表：`GET /api/store/{storeId}/products`
- 商品详情：`GET /api/store/product/{id}`

本地静态展示数据：

- Banner轮播
- 跑腿代取、商品配送、帮买服务、帮送服务入口
- 走失儿童公益信息、校园欺凌免费求助、紧急免费救援入口

门店与商品浏览：

- 门店详情页展示真实门店基础信息、营业状态、地址、联系电话和简介。
- 商品分类导航使用真实分类数据；无分类时保留“全部商品”。
- 商品列表支持分类切换、店内关键词搜索、分页加载、空状态和错误重试。
- 商品详情展示商品图片、名称、价格、库存、分类、所属门店和购买前确认。
- 搜索页已接入真实门店关键词搜索；全局商品搜索暂未实现。
- 商品图片、门店图片和头像统一使用`normalizeImageUrl()`处理异常路径，商品默认图为`/assets/images/product-placeholder.svg`。
- V1.7-7A确认商品为空不是小程序请求路径问题，而是测试店铺`business_status=0`被后端公开商品链路过滤；当前测试库`storeId=1`已修正为营业中。
- V1.7-7人工复测修复了商品图片外链不可访问和商品搜索栏按钮遮挡输入框问题。
- 商品搜索栏已二次调整为两行布局，输入框独占第一行，搜索和清空按钮位于第二行；清空按钮常驻占位，避免小屏聚焦和输入时挤压输入框。
- 商品图片失败兜底为`/assets/images/product-placeholder.svg`，门店图片失败兜底为`/assets/images/home/store-placeholder.svg`，兜底后不重复处理。
- 生产环境图片必须统一使用腾讯云COS HTTPS地址，不使用测试外链、本机地址、`file:`或Windows路径。

## 购物车联调

真实接口：

- 加入购物车：`POST /api/cart/add`
- 查询购物车：`GET /api/cart/list`
- 修改数量：`PUT /api/cart/update`
- 单项选中：`PUT /api/cart/selected`
- 全选/取消全选：`PUT /api/cart/selected/all`
- 删除商品：`DELETE /api/cart/{id}`
- 清理失效商品：`DELETE /api/cart/invalid`
- 清空购物车：`DELETE /api/cart/clear`

页面能力：

- 商品详情页点击“加入购物车”调用真实接口，默认数量为1。
- 门店商品列表支持快捷加购，未加入时显示圆形“+”，已加入时显示`[- 数量 +]`。
- 门店详情页底部固定折叠购物车栏展示真实数量和后端返回的已选合计金额。
- 点击底部栏可打开购物车弹层，弹层支持数量增减、删除、清空和关闭。
- 同一商品重复加入由后端合并数量，不新增重复记录。
- 购物车页面展示门店、商品、当前价格、库存、数量、小计、失效原因和底部合计。
- 独立购物车页保留，用于从“我的购物车”进入完整管理。
- 失效商品不参与全选、不参与合计，支持单独删除和一键清理。
- 跨店铺加购冲突时弹窗提示清空原购物车后再加入当前商品。
- 商品详情、门店详情、底部购物车栏、弹层、独立购物车页和个人中心统一同步真实购物车状态。
- “去结算”当前仅提示“提交订单功能将在 V1.7-9 开放”，不创建订单。
- V1.7-8B按购物车设计稿统一页面背景、卡片圆角、阴影、72×72商品图、36px数量控制器、BottomSheet 24px顶部圆角和品牌绿色去结算按钮。
- V1.7-8C不再沿用旧购物车布局补丁，已重建门店商品卡、底部购物车栏、BottomSheet弹层和独立购物车页布局，重点处理长文本、两位数量、小屏宽度和安全区域。
- V1.7-8D建立五鑫Design System V1.0，并将门店详情页WXML/WXSS废弃旧结构后全量重建；当前等待微信开发者工具人工视觉验收。
- 独立购物车页尚未按V1.7-8D Design System重建，首页UI尚未开始。

人工联调建议：

1. 后端通过 IDEA 启动，确认 `MOCK_WECHAT_LOGIN_ENABLED=true`。
2. 小程序执行 `npm run build` 后在微信开发者工具构建 npm。
3. 使用 Mock 微信登录进入首页。
4. 进入门店详情和商品详情，点击加入购物车。
5. 在门店商品列表直接点击“+”，测试列表数量、底部栏和弹层同步刷新。
6. 点击底部购物车栏打开弹层，测试增减、删除、清空和遮罩关闭。
7. 回到独立购物车测试数量增减、选中、全选、删除、清理失效商品和清空。
8. 进入“我的”，确认“我的购物车”“商家入驻”“申请成为骑手”入口可打开。

当前边界：

- 公益页不展示真实个人敏感信息，不收集求助内容，不提交救援订单。
- 商家入驻和骑手申请仅提供正式占位入口，不提交申请、不收集资质材料。
- 支付和真实提交订单留到V1.7-9及后续阶段。
- 当前不声明门店详情页已通过视觉验收，需在微信开发者工具完成截图或人工确认。

上线前检查：

- `API_BASE_URL`必须切换为HTTPS合法域名。
- 必须关闭Mock微信登录和Mock手机号绑定。
- 必须替换或接入首页本地临时展示数据。
- 微信开发者工具偶发`WAServiceMainContext.js Error: timeout`，在业务页面和Network请求正常时不作为阻塞项。

## 稳定性修复

V1.7.3已修复：

- 小程序运行时不稳定解析目录导入的问题，API统一显式导入`api/index`或具体文件。
- `App.onLaunch`阶段`getApp().globalData`可能不可用的问题。
- 页面初始渲染时空`userInfo`导致白屏的风险。
- 默认头像使用本地资源`/assets/images/default-avatar.svg`。

本地Mock开发可以临时设置：

```ts
USE_MOCK_WECHAT_LOGIN = true
USE_MOCK_WECHAT_PHONE_BIND = true
```

上线前必须恢复生产配置，保持Mock关闭。也可以优先使用微信开发者工具Storage开关：

```text
WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_LOGIN=true
WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_PHONE=true
```

## 检查命令

```bash
npm run type-check
npm run lint
npm run build
```

当前断点：V1.7-8D 门店详情页人工视觉验收。
