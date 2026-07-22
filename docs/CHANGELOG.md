# 更新日志

## V1.8 骑手端与商家端

日期：2026-07-22

### 新增内容

- 新增独立微信原生小程序`wuxin-rider-app`，完成登录、申请、审核状态、工作台、接单大厅、我的配送、配送详情、统计、排行榜和个人中心10个页面
- 新增独立微信原生小程序`wuxin-merchant-app`，完成登录、入驻、审核状态、工作台、订单、订单详情、店铺、分类、商品、商品编辑、经营概览和个人中心12个页面
- 双端统一实现Bearer Token、401退出、403提示、请求超时、分页、刷新、加载、空状态和错误状态
- 新增骑手申请、资料查询、配送详情和管理员审核/启停接口
- 骑手大厅与我的配送补充订单类型、店铺、取送地址和商品摘要
- 新增幂等增量脚本`15_update_rider_application.sql`，补充骑手拒绝原因和用户唯一索引

### 验证与边界

- Java 21环境下41项后端测试通过；骑手端与商家端Build、Lint、TypeScript检查通过
- 商家资料、订单、详情、分类、商品以及骑手大厅、排行榜、统计HTTP只读回归通过，未授权请求返回401
- 增量SQL未自动执行，因此骑手资料和我的配送HTTP回归被缺少`reject_reason`字段阻塞；需人工迁移后复测
- 本轮未改动生产数据，未执行订单接单、拒单、出餐、配送完成等写操作闭环；沿用历史Postman验收事实
- 骑手收益和商家财务营收没有真实专用接口，前端不伪造数据
- 两个新小程序尚需正式AppID、HTTPS合法域名和微信开发者工具人工验收
- 下一开发断点：总控端订单、用户、骑手与运营数据管理

## V2用户端全页面视觉回归修复

日期：2026-07-22

### 修复内容

- 完成用户微信小程序22个已注册页面的WXML与WXSS布局审计和视觉回归修复
- 统一页面、卡片、横向Flex内容、固定操作栏和安全区的布局基线，修复宽度挤压、横向溢出、逐字换行和内容遮挡
- 修复首页、门店详情、商品详情、地址管理、个人中心和购物车等重点页面的结构稳定性
- 统一公共商品卡、门店卡、数量步进器、页面状态和区块标题组件的尺寸与收缩规则
- 清理不适合当前微信小程序运行环境的Grid、负外边距、布局缩放和超宽计算写法
- 组件WXSS改用Class选择器，避免微信开发者工具的组件选择器警告
- 请求超时继续保持15秒限制，并将底层超时错误转换为用户可理解的“请求超时，请检查网络后重试”

### 业务边界

- 未修改后端、数据库、SQL、API、购物车规则、订单规则或支付流程
- 未增加Mock数据、重复页面或临时组件
- 微信开发者工具逐页视觉验收和真实网络超时接口确认仍需在人工复测环境完成

## V1.7 用户端整体重构

日期：2026-07-22

### 重构内容

- 重建用户端设计系统，统一品牌绿、强调色、字体、间距、圆角、阴影、按钮、卡片和状态样式
- 重构登录、首页、搜索、门店列表、门店详情、商品详情和购物车
- 首页新增真实附近门店、热门商品、推荐商品、猜你喜欢、下拉刷新和骨架屏
- 搜索新增本地搜索历史、基于真实门店的联想与热门词、最近浏览和空结果
- 重构地址列表与编辑页面，新增结算地址选择和微信地图选点入口
- 新增确认订单页面，接入真实结算预览与购物车创建商品订单接口
- 新增支付中、支付成功和支付失败页面，接入现有微信JSAPI支付接口
- 重构订单列表并新增订单详情、订单轨迹、取消订单、确认收货和继续支付入口
- 重构个人中心与资料编辑，保留微信手机号授权绑定和统一认证状态同步
- 商家入驻由占位页升级为真实申请表单，调用现有商家申请接口
- 将三个重复公益说明页合并为统一公益服务页面
- 新增关于五鑫和设置页面
- 新增通用页面状态、区块标题、门店卡、商品卡和数量步进器组件
- 移除旧空状态组件、重复公益页面、测试头像和未使用Vant依赖

### 业务边界

- 未修改Spring Boot后端、数据库、SQL或现有接口
- 不伪造后端不存在的评分、月售、优惠券、配送时长和骑手申请结果
- 骑手线上申请等待后端正式接口
- 真实微信支付等待生产商户配置、HTTPS合法域名和真机联调

## V1.7 用户微信小程序

日期：2026-07-21

### 第一阶段新增

- 创建`wuxin-miniapp`用户微信小程序目录
- 初始化微信原生小程序 + TypeScript基础工程
- 新增Vant Weapp、ESLint、Prettier和TypeScript依赖
- 新增微信开发者工具`project.config.json`
- 新增统一请求层，处理后端`Result<T>`结构
- 新增Bearer Token自动注入、Token持久化和401跳转登录页
- 新增`Result<T>`、`PageResult<T>`、`UserInfo`、`WeChatLoginRequest`和`WeChatLoginResponse`类型
- 封装`POST /api/user/wechat/login`、`GET /api/user/me`和`GET /api/user/profile`
- 新增微信登录、首页、地址列表、订单列表和个人中心页面骨架
- 新增通用空状态组件

### 边界

- 本阶段未开发完整地址、订单、购物车或支付页面
- 本阶段未开发骑手端、商家端、站长端或公益复杂功能
- 未修改后端业务代码，未执行SQL，未修改数据库

### 第二阶段新增

- 完成用户微信登录页面与真实后端接口联调代码
- 登录页增加品牌标题、登录说明、微信一键登录按钮、loading、防重复点击、错误提示和协议占位
- 登录流程调用`wx.login()`获取临时`code`，并调用`POST /api/user/wechat/login`
- 登录成功后保存`token`、`userInfo`和`newUser`
- App启动时恢复本地登录状态
- 新增轻量认证服务，提供`login`、`logout`、`restoreSession`、`clearSession`和`verifySession`
- 首页、地址列表、订单列表和个人中心统一使用`requireLogin()`保护
- 已登录访问登录页时自动跳转首页
- 请求层对HTTP 401和业务`code=401`统一清理登录态并跳转登录页
- 首页展示当前用户欢迎语、昵称和头像占位
- 个人中心展示头像、昵称、用户名和手机号状态，并支持退出登录确认
- 本地Mock微信登录支持开发工具Storage开关，默认关闭，release环境强制禁用
- 当前断点更新为V1.7-3用户Profile展示与编辑

### 第三阶段新增

- 首页进入后调用`GET /api/user/profile`刷新当前用户资料
- 首页展示头像、昵称、用户名和手机号状态
- 个人中心展示头像、昵称、用户名、手机号和性别
- 昵称为空统一显示“微信用户”，手机号为空统一显示“未绑定”
- 新增用户资料编辑页面
- 编辑资料页支持修改昵称和头像URL
- 编辑资料调用`PUT /api/user/profile`
- 更新成功后调用`GET /api/user/profile`刷新Storage和AuthState
- 登录状态校验改为使用`GET /api/user/profile`
- 退出登录继续清理`token`、`userInfo`和`newUser`
- 当前断点更新为V1.7-4手机号绑定

### 第三阶段边界

- 本阶段不开发手机号绑定
- 本阶段不开发地址、订单、购物车或支付
- 未修改后端业务代码，未执行SQL，未修改数据库

### V1.7.3 稳定性修复

- 修复`pages/profile/edit`使用`../../../api`目录导入导致微信运行时找不到`api.js`的问题
- 全项目排查小程序目录导入，统一使用显式`api/index`或具体文件路径
- 修复`App.onLaunch`阶段`restoreSession()`直接访问`getApp().globalData`可能报错的问题
- `clearAuth()`和`persistAuth()`增加`getApp()`生命周期兼容保护
- Home、Profile和Edit Profile页面避免初始渲染直接读取空`userInfo`字段
- 首页和个人中心默认头像改为本地资源`/assets/images/default-avatar.svg`
- 排除微信开发者工具生成的`miniprogram/miniprogram_npm`，避免ESLint扫描Vant构建产物
- 本次不新增业务功能，不修改后端、数据库或接口

### 第四阶段新增

- 完成微信手机号绑定入口与真实后端接口联调代码
- 封装`POST /api/user/phone/bind`
- Profile页面根据`phone`状态展示“立即绑定”或脱敏手机号
- 点击绑定时调用`wx.getPhoneNumber()`获取手机号授权`code`
- 本地开发模式继续复用`MockWeChatPhoneGateway`固定code映射
- 绑定成功后刷新Profile资料，并同步Home、Storage、AuthState和`App.globalData`
- 用户拒绝授权时提示“已取消手机号授权”
- 接口失败时展示后端`message`
- 当前断点更新为V1.7-5地址管理

### 第四阶段边界

- 本阶段不开发短信验证码登录
- 本阶段不开发腾讯云短信
- 本阶段不开发手机号修改
- 本阶段不开发地址、订单、购物车或支付
- 未修改后端业务代码，未执行SQL，未修改数据库

### 第五阶段新增

- 完成地址管理页面从个人中心进入
- 封装`GET /api/user/address/list`、`POST /api/user/address`和`DELETE /api/user/address/{id}`
- 新增统一`Address`和`AddressRequest`类型，后续下单可直接复用
- 地址列表展示收件人、脱敏手机号、完整地址、默认标签和操作按钮
- 默认地址在列表中置顶
- 新增地址页支持收件人、手机号、省、市、区、详细地址和默认地址
- 前端校验收件人、11位手机号和详细地址
- 删除地址增加确认弹窗，成功后刷新列表
- 页面返回和手动刷新均重新加载地址列表
- 完成地址卡片、圆角、阴影、留白、按钮和底部固定新增按钮的第一轮基础美化
- 当前断点更新为V1.7-6首页商家与服务入口

### 第五阶段边界

- 真实后端地址接口前缀为`/api/user/address`，不是`/api/address`
- 本阶段不开发订单、购物车、支付、骑手端、商家端或站长端功能
- 未修改后端业务代码，未执行SQL，未修改数据库

### V1.7-5A 地址模块后端补全

- 新增`PUT /api/user/address/{id}`编辑地址接口
- 新增`PUT /api/user/address/{id}/default`设置默认地址接口
- 地址新增、编辑和设置默认地址时自动取消当前用户其它默认地址
- 地址编辑、删除和设置默认地址均按当前JWT用户校验地址归属
- 地址不存在、已删除或不属于当前用户时返回统一业务异常
- 将地址新增、列表、编辑、删除和设置默认地址业务下沉到`UserAddressService`
- 补充地址DTO手机号、长度和必填校验
- 小程序地址编辑页改为真实调用编辑接口
- 小程序地址列表“设为默认”改为真实调用后端接口
- 本次不修改数据库结构，不新增SQL

### 第六阶段新增

- V1.7-6 首页商家与服务入口已完成
- 首页顶部展示用户昵称、本地默认头像、脱敏手机号和默认地址摘要
- Home页面进入和返回时刷新`GET /api/user/profile`与`GET /api/user/address/list`
- 新增搜索入口和搜索占位页，不伪造后端搜索结果
- 新增本地Banner轮播资源，包含品牌宣传、校园便捷生活和公益服务宣传
- 新增跑腿代取、商品配送、帮买服务和帮送服务入口
- 新增走失儿童公益信息、校园欺凌免费求助和紧急免费救援三个公益入口
- 推荐门店接入真实公开接口`GET /api/store/list`
- 门店详情占位页接入真实公开接口`GET /api/store/{id}`，仅展示门店基础信息
- 首页增加门店加载、空状态、错误重试和图片加载失败兜底
- 页面路由新增`/pages/search/index`、`/pages/store/detail/index`和三个公益说明页
- 当前断点更新为V1.7-7门店与商品列表/商品详情

### 第六阶段边界

- Banner、核心服务入口和公益入口当前为本地静态展示数据
- 公益入口不展示真实个人敏感信息，不收集求助内容，不提交救援订单
- 本阶段不开发商品列表、商品详情、购物车、支付或完整订单流程
- 未新增后端接口，未修改数据库结构，未新增SQL

### 第七阶段新增

- V1.7-7 门店与商品列表/商品详情已完成
- 门店详情页由占位页升级为正式只读页面
- 门店详情页展示真实门店图片、名称、营业状态、地址、电话、营业时间和简介
- 商品分类调用真实接口`GET /api/store/{storeId}/categories`
- 商品列表调用真实接口`GET /api/store/{storeId}/products`
- 商品详情调用真实接口`GET /api/store/product/{id}`
- 店内商品支持分类切换、关键词搜索、分页加载、空状态和失败重试
- 商品卡片展示图片、名称、简介、价格和库存状态
- 商品详情页展示图片、名称、价格、库存、分类、所属门店和购买前确认
- 搜索页接入真实门店关键词搜索`GET /api/store/list?keyword=...`
- 新增`ProductCategory`、`ProductItem`和`ProductListQuery`类型
- 新增商品图片占位资源`/assets/images/product-placeholder.svg`
- 金额统一使用两位小数展示
- 当前断点暂缓进入购物车，更新为V1.7-7A商品链路审计与修复

### 第七阶段边界

- 公开商品接口只返回上架、库存大于0、分类启用、店铺营业且商家有效的商品
- 已下架、已删除、售罄或不可公开商品在列表中不展示，详情查询返回业务错误
- 全局搜索当前只接入门店搜索；商品名称搜索已在具体门店内支持
- “加入购物车”按钮仅提示下一阶段开放，不伪造加入购物车成功
- 本阶段不开发购物车、下单、支付、优惠券、收藏、评论、虚假销量或评分
- 未新增后端接口，未修改数据库结构，未新增SQL

### V1.7-7A 商品数据链路审计与修复

- 暂停V1.7-8购物车开发，先处理V1.7-7人工验收发现的商品分类和商品列表为空问题
- 完成数据库、Mapper、Service、Controller、API和小程序前端全链路审计
- 确认`GET /api/store/{id}`门店详情正常，是因为公开店铺详情不要求`business_status=1`
- 确认`GET /api/store/{storeId}/categories`和`GET /api/store/{storeId}/products`为空，是因为公开商品链路要求店铺`business_status=1`
- 当前测试库`storeId=1`原为`store_status=1`但`business_status=0`，导致门店可见但分类和商品被过滤
- 修正当前测试数据：将`merchant_store.id=1`更新为`business_status=1`
- 复测`GET /api/store/1/categories`返回`categoryId=2`
- 复测`GET /api/store/1/products?pageNum=1&pageSize=10`返回`productId=2`
- 复测`GET /api/store/product/2`返回商品详情
- 未修改后端代码、前端代码、数据库结构或SQL文件
- 当前断点更新为V1.7-7A商品链路复测

### V1.7-7 图片与搜索栏稳定性修复

- 修复商品列表和商品详情图片外链不可访问时持续触发渲染层网络错误的问题
- 商品图片失败后统一兜底为`/assets/images/product-placeholder.svg`
- 门店图片失败后统一兜底为`/assets/images/home/store-placeholder.svg`
- 图片失败处理增加已兜底判断，避免占位图失败时重复触发错误循环
- `normalizeImageUrl()`拦截空值、Windows路径、`file:`、反斜杠路径、`localhost`、`127.0.0.1`、`example.com`和`test.com`等不可用于小程序图片展示的地址
- 当前测试数据已修正：`merchant_product.id=2`的`product_image`从`https://test.com/test.jpg`改为本地商品占位图
- 当前测试数据已修正：`merchant_store.id=1`的`store_logo`从`logo-test-2`改为本地门店占位图
- 商品搜索栏由不稳定的自适应按钮布局改为稳定横向布局
- 搜索输入框固定`flex: 1`与`min-width: 0`，搜索和清空按钮使用固定宽度
- 去除搜索栏原生按钮默认边框和`::after`伪元素，避免聚焦和输入时挤压输入框
- 生产环境图片规范：商品图、门店图和头像必须统一使用腾讯云COS HTTPS地址，不使用测试外链、localhost、file或Windows本地路径
- 当前断点保持为V1.7-7人工复测

### V1.7-7 商品搜索栏二次修复

- 再次修复门店详情页商品搜索框在小屏聚焦后被按钮挤压的问题
- 根因是单行布局中输入框、搜索按钮、清空按钮、gap和容器padding的最小宽度总和仍会在清空按钮出现后压缩输入框
- 最终采用两行布局：第一行只放商品搜索输入框，第二行放搜索和清空按钮
- 清空按钮常驻占位，无关键词时禁用，避免输入过程中因按钮显隐造成布局跳动
- 输入框继续保持`width: 100%`、`min-width: 0`和`box-sizing: border-box`
- 搜索与清空按钮保持同高，不使用绝对定位，不覆盖输入框
- 当前断点保持为V1.7-7人工复测

### 第八阶段新增

- V1.7-8 用户购物车闭环已完成
- 后端补齐全选/取消全选接口`PUT /api/cart/selected/all`
- 后端补齐清理失效商品接口`DELETE /api/cart/invalid`
- 小程序新增购物车API、类型和页面`/pages/cart/index`
- 商品详情页“加入购物车”改为调用真实接口`POST /api/cart/add`
- 购物车页面支持列表、数量增减、单项选中、全选/取消全选、删除、清理失效商品和清空购物车
- 商品详情、门店详情和“我的”页面新增购物车入口与真实角标同步
- 跨店铺加购冲突时弹窗提示用户清空原购物车后再加入当前商品
- “我的”页面新增商家入驻和申请成为骑手正式占位入口，完整流程留到V1.8
- 底部“去结算”按钮仅提示V1.7-9开放，不创建订单、不调用支付
- V1.7-8完成时断点曾更新为V1.7-9提交订单，后续因人工检查先进入V1.7-8A交互重构

### V1.7-8A 购物车交互重构

- 暂停进入V1.7-9，先修复购物车交互和布局不符合真实点餐/选购场景的问题
- 门店商品列表新增快捷加购控件，未加入时显示圆形“+”，已加入时显示`[- 数量 +]`
- 门店商品列表加购、增减和删除均调用真实购物车接口，不在前端伪造数量
- 门店详情页底部由悬浮购物车按钮重构为固定折叠购物车栏，展示真实数量和后端返回合计金额
- 新增门店详情页底部购物车弹层，支持查看商品、增减数量、删除和清空购物车
- 弹层出现时使用遮罩并支持点击遮罩或关闭按钮收起，商品较多时弹层内部滚动
- 独立购物车页商品卡片调整为稳定横向结构，修复选择框、图片、商品信息和数量控制器挤压错乱
- 统一通过`refreshCartDetail()`获取真实购物车详情，同步门店列表、底部栏、弹层、独立购物车页、角标和Storage非敏感数量
- 当前仍未开发订单创建、支付、配送费、优惠券或真实结算
- 当前断点更新为V1.7-8A人工复测

### V1.7-8B 购物车 UI 重构

- 暂停进入V1.7-9，按用户提供的《购物车设计规范》进行购物车UI重构
- 保持购物车接口、数据库、业务规则、状态同步、订单和支付逻辑不变
- 门店详情页统一页面背景为`#F6F7F9`，卡片圆角、阴影、留白和商品卡层级按设计稿调整
- 门店商品卡统一商品图片尺寸为72×72，数量控制器高度为36px，按钮和价格样式按设计稿调整
- 门店底部购物车栏按设计稿重构为左侧购物车图标/角标、中间金额与已选数量、右侧绿色去结算按钮
- 购物车BottomSheet按设计稿统一24px顶部圆角、顶部标题/清空/关闭、中间商品列表和底部金额/去结算布局
- 独立购物车页按设计稿调整背景、卡片、72×72商品图、固定选择框、数量控制器和底部结算栏
- 当前断点更新为V1.7-8B人工复测

### V1.7-8C 购物车前端布局彻底重构

- 暂停进入V1.7-9，不继续在旧WXML/WXSS上局部打补丁
- 门店详情页商品卡、底部购物车栏和BottomSheet弹层重建为清晰稳定的DOM层级
- 独立购物车页重建为`cart-page`、`cart-store-card`、`cart-list`、`cart-item`和`cart-footer`布局
- 商品卡统一使用固定图片区、可收缩文本区、价格区和数量控制器，避免小屏挤压和竖排文字
- 商品图片统一按72×72设计尺寸实现，卡片圆角、阴影、留白和按钮高度按设计稿规范校正
- 底部购物车栏和独立购物车页底部结算栏均适配安全区域
- 保持购物车接口、后端、数据库、业务规则、状态同步、订单和支付逻辑不变
- 当前断点更新为V1.7-8C人工复测

### V1.7-8D Design System V1.0 与门店详情页 UI 全量重建

- 建立五鑫小程序 Design System V1.0，新增设计变量、基础布局、文字层级和公共视觉组件样式
- `app.wxss`引入Design System，原`.page`、`.card`、`.title`和`.subtitle`保留为legacy全局样式
- 门店详情页`index.wxml`和`index.wxss`废弃旧结构后从空文件重新创建
- 新门店详情页结构拆分为`store-hero`、`store-toolbar`、`store-products`、底部购物车栏和BottomSheet
- 新搜索框使用Design System搜索样式，不依赖`van-button`
- 分类栏、商品卡、数量控制器、底部购物车栏和BottomSheet按新Design System重建
- 新增自绘SVG线性搜索图标和购物车图标，未使用第三方图标资源
- 保留原有TypeScript业务逻辑、接口、购物车规则、搜索、分类筛选和图片兜底处理
- 本阶段未重建首页UI，未重建独立购物车页
- 当前断点更新为V1.7-8D门店详情页人工视觉验收

## V1.6 总控管理后台

日期：2026-07-18

### 完成状态

- V1.6 总控管理后台开发完成，人工验收通过
- 管理员可以通过后台网页完成商家查询、搜索筛选、详情查看、审核通过、审核拒绝、启用和禁用
- 前端 Element Plus 按需加载优化已完成

### 第一阶段新增

- 创建`wuxin-admin-web`
- 初始化Vue 3、TypeScript和Vite项目
- 集成Vue Router、Pinia、Axios和Element Plus
- 配置ESLint、Prettier、TypeScript检查和生产构建
- 配置`VITE_API_BASE_URL=/api`和开发代理
- 建立后端`Result<T>`、登录、分页和商家管理TypeScript类型
- 建立Bearer Token自动注入与刷新恢复
- 建立401清理Token并跳转登录、403管理员权限提示
- 建立管理员状态Store和基础路由守卫
- 封装管理员登录及六个商家管理接口
- 新增登录、后台首页、商家管理、404和后台布局基础页面

### 第二阶段新增

- 管理员登录页接入真实`POST /api/user/login`
- 登录成功后保存Token和用户基础信息
- 登录成功后通过`GET /api/admin/merchant/page?pageNum=1&pageSize=1`验证管理员权限
- 非管理员账号登录后清理Token和用户信息，并提示当前账号无管理员权限
- 刷新页面后恢复Token并重新验证管理员权限
- 路由守卫接入管理员权限校验，未登录或无权限跳转登录页
- Axios统一处理HTTP和业务`401`、`403`
- 后台首页展示当前管理员、V1.6版本状态和商家管理入口
- 管理后台README补充本地人工联调说明

### 第三阶段新增

- 商家管理页接入真实`GET /api/admin/merchant/page`
- 支持审核状态、商家状态和关键词筛选
- 支持服务端分页、每页数量切换、手动刷新和重置筛选
- 商家列表展示商家ID、商家名称、店铺名称、联系人、联系电话、审核状态、商家状态、营业状态和申请时间
- 审核状态、商家状态和营业状态统一使用状态映射和Element Plus Tag展示
- 补充加载状态、空数据状态、请求失败提示和重新加载入口
- 使用请求序号和卸载保护处理列表请求竞态
- 新增`/merchants/:merchantId`详情占位路由
- 查看详情、审核和查看审核信息仅作为下一阶段入口，不调用详情、审核、拒绝、启用或禁用接口
- 管理后台README补充商家列表人工联调步骤

### 第四阶段新增

- 商家详情页接入真实`GET /api/admin/merchant/{merchantId}`
- 新增只读商家详情页面，替换上一阶段详情占位页
- 详情页展示商家基础信息、审核信息、店铺信息、营业信息和资质信息
- 支持返回列表、手动刷新、loading、错误提示和不存在数据处理
- 列表使用`AdminMerchantSummary`，详情使用`AdminMerchantDetail`
- 提取商家状态Tag映射供列表和详情复用
- 详情页不提供审核、拒绝、启用、禁用、编辑或图片上传操作
- 管理后台README补充商家详情人工联调步骤

### 第五阶段新增

- 商家详情页接入审核通过、审核拒绝、启用和禁用操作
- 审核通过调用`POST /api/admin/merchant/{merchantId}/approve`，请求字段使用`auditRemark`
- 审核拒绝调用`POST /api/admin/merchant/{merchantId}/reject`，请求字段使用`reason`
- 商家启用调用`POST /api/admin/merchant/{merchantId}/enable`，按真实Controller定义不发送请求体
- 商家禁用调用`POST /api/admin/merchant/{merchantId}/disable`，请求字段使用`reason`
- 根据`auditStatus`和`merchantStatus`控制详情页操作按钮展示
- 使用Element Plus Dialog完成审核备注、拒绝原因和禁用原因输入
- 操作提交增加loading、防重复提交、成功提示和失败提示
- 操作成功后重新请求详情接口刷新数据，不在前端硬编码成功状态
- 管理后台README补充商家审核与状态操作人工联调步骤

### V1.6-6 优化

- 管理后台性能优化，Element Plus由全量注册调整为自动按需引入
- 新增`unplugin-auto-import`和`unplugin-vue-components`依赖
- 移除`app.use(ElementPlus)`和全量Element Plus样式引入
- API工程结构优化，商家接口按查询和操作方法整理
- 类型结构优化，列表VO和详情VO分离，请求类型独立保留
- 状态工具整理，审核、商家、店铺和营业状态统一映射
- 清理重复详情API别名和页面分散状态判断
- 构建优化后500kB chunk warning已消除

### 当前状态

- V1.6第一阶段基础架构已完成
- V1.6管理员真实登录闭环已完成
- V1.6-3商家管理分页列表已完成
- V1.6-4商家详情页面已完成
- V1.6-5商家审核与状态操作联调已完成
- V1.6-6管理后台性能优化和代码整理已完成
- 管理员登录测试通过
- 普通用户访问后台403测试通过
- 商家申请入驻流程测试通过
- 审核通过测试通过
- 审核拒绝测试通过
- 商家禁用测试通过
- 商家启用测试通过
- 列表到详情跳转测试通过
- 当前断点为V1.7用户微信小程序开发
- V1.6总控管理后台已完成并通过人工验收

### 验收数据

- `merchantId=4`：商家审核通过测试，`audit_status=1`，`audit_admin_id=1`，`audit_remark=材料准确 允许通过`
- `merchantId=5`：商家审核拒绝测试，`audit_status=2`，`audit_admin_id=1`，`reject_reason=营业执照信息不清晰，请重新上传。`

### 环境配置

- 后端数据库密码配置为`spring.datasource.password=${DB_PASSWORD:}`
- 本地 IDEA 通过环境变量`DB_PASSWORD=123456`注入
- 生产环境通过服务器环境变量配置，避免数据库密码进入代码仓库

## V1.5 总控端商家审核模块

日期：2026-07-18

### 新增

- `GET /api/admin/merchant/page`
- `GET /api/admin/merchant/{merchantId}`
- `POST /api/admin/merchant/{merchantId}/approve`
- `POST /api/admin/merchant/{merchantId}/reject`
- `POST /api/admin/merchant/{merchantId}/enable`
- `POST /api/admin/merchant/{merchantId}/disable`
- `AdminMerchantController`、Service、Mapper、DTO和VO
- 复用真实数据库已有的`sys_role`与`sys_user_role`最小RBAC表并补充唯一索引
- `/api/admin/**`管理员权限拦截器
- `merchant_audit_log`商家审核操作日志
- `merchant_info.audit_admin_id`、`audit_time`和`reject_reason`
- `14_create_admin_merchant_audit.sql`

### 安全与一致性

- 管理员权限按JWT用户ID实时查询角色，不依赖固定用户ID或前端传参
- 未登录返回401，普通用户和普通商家访问总控接口返回403
- 分页和详情不返回密码、Token、openid或unionid
- 审核通过、拒绝、启用和禁用均使用原状态条件更新及事务
- 并发重复操作只有一次成功，不重复写审核日志
- 审核通过不自动把店铺设为营业中
- 审核拒绝和商家禁用会禁用店铺并设置为休息中
- 商家状态管理不删除商家、店铺或历史订单
- 审核日志与订单日志分表管理
- 清理`application.properties`中的硬编码数据库密码，统一改为环境变量占位

### 当前状态

- 代码、自动化测试、SQL和文档已完成
- 14号SQL已在当前测试数据库人工执行，ADMIN角色及管理员授权已验证
- 商家分页、详情、审核通过、审核拒绝、启用和禁用已通过Postman人工验收
- 审核字段、店铺状态和`merchant_audit_log`已通过Navicat核对
- 普通用户访问`/api/admin/**`返回`403 无管理员权限`
- 审核通过请求字段由`remark`统一为`auditRemark`
- V1.5总控端商家管理后端已完成并通过人工验收
- 未执行Git Commit

## V1.4 商家订单管理模块

日期：2026-07-18

### 新增

- 商家订单分页接口和订单详情接口
- 商家接单、拒单和出餐接口
- `MerchantOrderController`、`MerchantOrderService`及实现
- 商家订单查询DTO、拒单DTO和商家订单VO
- 商家订单聚合查询Mapper
- `order_info`商家接单、出餐、拒单时间及拒单原因字段
- 商家订单分页复合索引
- 增量脚本`13_update_order_for_merchant_management.sql`
- 订单状态`6 商家已接单制作中`
- 订单状态`7 已出餐待骑手接单`
- 订单状态`8 已关闭待退款`

### 安全与兼容

- 商家身份和店铺归属只从JWT及`UserContext`解析
- 商家只能查询和操作自己店铺的商品订单
- 越权统一返回`订单不存在或无权限`
- 商家操作要求商品订单已支付
- 接单、拒单和出餐均使用带原状态条件的原子更新
- 重复和并发操作不会重复写订单日志
- 商家端收件手机号统一脱敏
- 商品详情读取`order_item`快照
- 商品订单出餐后才进入骑手大厅
- 普通跑腿订单骑手大厅与接单流程保持兼容
- 商品骑手放弃后回到已出餐待骑手接单状态

### 退款边界

- 商家拒单执行`0 → 8`，状态为已关闭待退款
- 不修改`pay_status`为未支付
- 不修改支付流水为退款成功
- 后续真实微信支付第二阶段必须实现退款申请和退款结果同步

### 当前状态

- 代码、自动化测试、文档和增量SQL已完成
- 增量SQL、Postman和Navicat人工验收已通过
- 订单`7`已完成创建、Mock支付、商家接单、商家出餐、骑手大厅和骑手接单链路
- 订单`8`已完成创建、Mock支付和商家拒单链路，状态由`0 → 8`
- 订单`8`的`merchant_reject_time`和`merchant_reject_reason`已通过Navicat验证
- 商家订单详情已确认接单时间、出餐时间和订单时间线完整
- 修复商家出餐成功响应未回填`merchantAcceptTime`的问题，商家时间字段统一读取订单真实数据
- 真实退款尚未实现，拒单后保持已支付并等待后续退款处理
- V1.4商家订单管理验收收尾完成

## V1.3 微信用户体系

日期：2026-07-18

### 新增

- `POST /api/user/wechat/login`
- `WeChatMiniProgramProperties`与`MockWeChatLoginProperties`
- 微信小程序网关抽象、路由和真实 code2session 实现
- 固定 code/openid 映射的本地 Mock 微信网关
- 微信用户自动注册、JWT 返回和 `newUser`标识
- openid 唯一索引冲突后的并发回查幂等处理
- `UserInfoVO`补充昵称和头像
- `GET /api/user/profile`
- `PUT /api/user/profile`
- `UpdateUserProfileDTO`及昵称、头像、性别校验
- `UserInfoVO`补充gender
- Profile只允许更新`nickname、avatar、gender`
- `POST /api/user/phone/bind`
- `BindWechatPhoneDTO`
- `WeChatPhoneGateway`、`WeChatPhoneGatewayRouter`和`WeChatPhoneResult`
- 固定 code/手机号映射的`MockWeChatPhoneGateway`
- 当前用户手机号绑定、重复绑定幂等和更换手机号
- 其他未删除用户手机号占用检查
- 手机号绑定服务和JWT拦截回归测试

### 安全

- AppSecret仅通过环境变量注入，所有网关默认关闭
- code不缓存，session_key不持久化、不返回、不记录
- 自动用户名不暴露完整 openid
- 自动用户密码使用随机 BCrypt 密文
- Mock登录在`prod` Profile中禁止使用
- JWT白名单仅新增精确路径`/api/user/wechat/login`
- 手机号绑定接口保持JWT保护，用户ID只从`UserContext`获取
- Mock手机号网关默认关闭，`prod`环境禁止使用
- 不接受客户端明文手机号，只信任网关返回结果
- 授权code不写入日志，手机号日志统一脱敏

### 修复

- 修复微信自动注册原始随机密码为73个UTF-8字节，超过BCrypt 72字节上限的问题
- 随机原始密码改为单个UUID，每个用户独立生成，固定为36个UTF-8字节
- BCrypt编码前增加UTF-8字节长度保护，生成异常转换为统一业务错误
- 新增首次Mock登录、重复登录、单用户插入和BCrypt密文回归测试

### 数据库

- 复用真实`sys_user.openid/unionid/nickname/avatar`
- Profile复用`nickname/avatar/gender/phone`，不修改数据库结构
- 手机号绑定复用`sys_user.phone`与普通索引`idx_phone`
- 复用`uk_openid`、`uk_username`和`idx_phone`
- 不新增SQL，不修改数据库结构

### 当前状态

- BCrypt 72字节问题已修复，单元回归测试通过
- Mock微信登录已通过人工验收
- Profile接口已通过Postman人工验收
- 微信手机号绑定已通过Postman与Navicat人工验收
- V1.3本轮人工验收全部完成
- 未接入真实微信支付
- 已有账号绑定、账号合并留待后续版本

### 人工验收

- 微信Mock登录：通过
- 微信Profile：通过
- 微信手机号绑定：通过
- Postman验证：通过
- Navicat验证：通过

## V1.2 微信支付模块（第一阶段）

日期：2026-07-18

### 新增

- `payment_order`支付流水表和`12_create_payment_order.sql`
- 支付流水状态枚举、Entity、Mapper及金额元转分工具
- `WeChatPayProperties`和`MockPaymentProperties`
- 支付网关抽象、路由器和无外部网络的`MockPaymentGateway`
- `POST /api/payment/wechat/jsapi`
- `POST /api/payment/mock/{paymentNo}/success`
- `GET /api/payment/order/{orderId}/status`
- 统一支付成功事务及重复确认幂等处理
- 官方`wechatpay-java:0.2.17`依赖

### 安全与兼容

- 新架构仅允许服务端可靠计价的商品订单
- 创建支付单不直接修改订单支付状态
- Mock接口和旧模拟支付接口均受环境开关保护
- 真实微信支付默认关闭，未创建SDK客户端、未读取私钥
- 微信回调第一阶段不注册，避免无验签请求误改订单
- 配置迁移为环境变量占位符，本地配置和密钥文件加入Git忽略
- 未修改订单履约状态语义，未实现退款

### 当前状态

- 第一阶段代码与文档完成，并通过人工 Mock 支付链路验收
- Maven Compile已通过
- SQL、Postman和Navicat验证已通过
- 未连接真实微信支付

### 人工验收

- Mock 支付创建成功
- 支付流水状态更新成功
- 订单支付状态同步成功
- 重复支付确认幂等验证通过
- 订单支付日志仅写入一次

## V1.1

日期：2026-07-17

### 新增

- 骑手今日、本周、本月和累计排行榜
- 骑手个人统计接口 `GET /api/rider/{riderId}/statistics`
- 排行榜 SQL 分组聚合和个人统计条件聚合优化
- 排行榜索引 `idx_order_status_deleted_finish_rider`
- `RiderRankingVO`、`RiderStatisticsVO`
- `RiderRankingService` 及其实现
- 幂等索引升级脚本 `11_add_rider_ranking_index.sql`

### 统计与安全

- 仅统计 `status = 4`、骑手 ID 非空且未逻辑删除的真实订单
- 今日、本周、本月统一使用 `finish_time` 左闭右开时间范围
- 排名按完成单量、最早完成时间、骑手 ID 稳定排序
- 排行榜名称优先使用骑手真实姓名，其次用户昵称，最后使用骑手 ID 兜底
- 两个接口继续受现有 JWT 拦截器保护
- 聚合查询不加载全部订单、不拼接用户 SQL、不产生 N+1 查询

### 当前状态

- V1.1 骑手跑单排行榜模块已完成
- Java 21 Maven Compile 已通过
- 索引 SQL 已人工执行并验证
- 今日、本周、本月、累计榜和骑手个人统计已通过人工测试
- 参数校验、limit 范围和 SQL 统计结果已通过人工验证

## V1.0 Completed

日期：2026-07-17

### 新增

- 新增购物车结算预览接口 `POST /api/order/settlement/preview`
- 新增购物车创建商品订单接口 `POST /api/order/create-from-cart`
- 新增 `order_item` 商品订单明细快照表
- 新增 `OrderTypeEnum`，区分跑腿订单和商品订单
- 商品订单详情返回店铺、金额和 `order_item` 快照信息
- `order_info` 兼容新增 `order_type`、`store_id`、`product_amount`、`delivery_fee`、`total_amount`
- 新增幂等数据库脚本 `10_create_order_item_and_update_order.sql`

### 业务与安全

- 预览和创建订单复用同一套地址、商品、分类、店铺、商家和库存校验
- 商品价格从 `merchant_product` 实时读取，创建后写入 `order_item` 快照
- 订单主表、库存扣减、明细快照、订单日志和购物车清理处于同一事务
- 库存通过带库存及业务状态条件的原子 SQL 扣减，防止超卖
- 创建成功后仅逻辑删除当前用户已选购物车项，未选中项继续保留
- 商品订单创建后保持未支付状态，不进入骑手大厅
- 商品订单详情仅允许订单所属用户查看，历史商品信息不依赖当前商品数据

### 当前状态

- Maven Compile 已通过
- SQL、Postman 和 Navicat 验证已通过
- 正常流程、异常流程和越权测试已通过
- V1.0 已完成

### V1.0 收尾

新增：

- 地址越权测试，确认其他用户不能使用不属于自己的收货地址
- 订单越权测试，确认其他用户不能查看不属于自己的订单
- 测试环境统一文档 `TEST_ENVIRONMENT.md`
- 测试账号统一管理规范
- V1.0 开发经验和版本收尾规范

修正：

- API 文档路径与 Controller 实际映射保持一致
- 地址接口路径统一为 `/api/user/address`
- 删除 API 文档中当前 Controller 不存在的修改地址接口
- 项目状态更新为 `V1.0 Completed`

## V0.9 Shopping Cart Completed

日期：2026-07-17

### 新增

- 购物车表 `shopping_cart` 和增量脚本 `09_create_shopping_cart.sql`
- 加入购物车、购物车列表、修改数量和选中状态接口
- 单个商品逻辑删除和清空购物车接口
- `CartItemVO`、`CartListVO` 实时购物车返回结构
- 购物车不存在、跨店铺、商品下架、库存不足和店铺停业异常码
- 购物车参数校验统一返回 `400`，不改变已有模块的 `1004` 兼容行为

### 业务与安全

- 当前用户统一从 `UserContext` 获取，不接收前端用户 ID
- 购物车只保存用户、店铺、商品、数量和选中状态，不保存商品快照
- 购物车列表实时关联商品、分类、店铺和商家状态
- 失效商品保留并返回 `invalidReason`，不计入选中总额
- 数据库用户行锁串行化同一用户写操作，保障单店铺约束和重复加购累加
- 同一商品删除后重新加购复用逻辑删除记录
- 所有购物车写操作处于事务中

### 数据库字段修正

- 商品分类启用状态统一使用 `merchant_category.status`
- 店铺营业状态统一使用 `merchant_store.business_status`

### 完成与验收

- 购物车六个接口测试全部通过
- 正常流程：加入、累加、查询、改数量、改选中、删除、重新加入、清空全部通过
- 异常流程：401、400、404、跨店铺、下架、禁用、停业、库存不足全部通过
- `shopping_cart` 表、唯一索引、普通索引及逻辑删除经 Navicat 验证通过
- V0.9 正式标记为 Completed

## V0.8 Completed

日期：2026-07-17

### 新增

- 商家商品分类新增、修改、启禁用、逻辑删除和列表接口
- 商家商品新增、修改、上下架、逻辑删除和分页列表接口
- 公开店铺分类列表、商品列表和商品详情接口
- `merchant_category` 商品分类表和 `merchant_product` 商品表
- `CategoryStatusEnum` 和 `ProductStatusEnum`
- 商品分类、商品及库存相关业务异常码

### 安全与一致性

- 分类和商品管理统一从 `UserContext` 获取当前用户，不接收前端店铺或商家 ID
- 管理操作统一校验商家审核状态、商家状态、店铺状态和逻辑删除状态
- 所有修改和删除条件包含记录 ID、当前店铺 ID 与未删除条件
- 分类重名由 Service 预检和数据库唯一索引共同保障
- 公开接口仅返回启用分类下已上架、未删除且有库存的商品
- 新公开路由仅按 GET 方法精确放行

### 验收

- 商品分类管理接口人工测试通过
- 商品管理及上下架接口人工测试通过
- 公开分类、商品列表和商品详情接口人工测试通过
- `merchant_category`、`merchant_product` 表结构及索引经 Navicat 验证通过
- V0.8 SQL、Postman、Navicat 验证全部完成

## V0.7（开发中）

日期：2026-07-16

### 新增

- 商家申请入驻接口：`POST /api/merchant/apply`
- 我的商家资料接口：`GET /api/merchant/me`
- 店铺资料修改接口：`PUT /api/merchant/store`
- 营业状态修改接口：`PUT /api/merchant/store/business-status`
- 公开店铺列表：`GET /api/store/list`
- 公开店铺详情：`GET /api/store/{id}`
- `merchant_info` 商家主体表和 `merchant_store` 店铺表
- 商家审核、商家启用和店铺营业状态枚举

### 安全与一致性

- 商家复用现有 `sys_user`、JWT 和 `UserContext`
- 商家主体与店铺创建处于同一事务
- `user_id` 唯一索引和异常转换防止并发重复申请
- 店铺管理严格校验商家归属、审核和启用状态
- 公开路由仅按 GET 方法放行店铺列表和数字 ID 详情
- 公开店铺列表使用数据库联表分页

## V0.6（开发中）

日期：2026-07-16

### 新增

- 订单模拟支付接口：`POST /api/order/pay/{id}`
- 订单轨迹接口：`GET /api/order/timeline/{id}`
- 新增 `PaymentStatusEnum`：`0 未支付`、`1 已支付`
- `order_info` 新增 `pay_status`、`pay_time`、`payment_no`
- 新增支付状态查询索引和支付单号唯一索引
- 支付成功后写入 `0 → 0` 的订单日志
- 用户订单列表和详情返回支付状态信息
- 订单轨迹整合 `order_info`、`order_log`、`order_comment` 的真实时间数据
- 轨迹按时间升序排列并重新编号，不返回空时间节点

### 调整

- 新订单默认未支付，配送业务状态仍为待接单
- 骑手大厅只展示已支付待接单订单
- 骑手接单原子更新增加已支付条件
- 未支付订单直接接单返回 `409 订单未支付`

### 安全与一致性

- 模拟支付使用数据库原子条件更新，防止并发重复支付
- 支付单号采用时间戳、六位随机数、唯一索引和有限重试保障唯一
- 支付更新和订单日志写入处于同一事务
- 数据库升级只通过手动执行增量 SQL 完成

## V0.5（开发中）

日期：2026-07-16

### 新增

- 用户取消订单接口：`POST /api/order/cancel/{id}`
- 骑手放弃订单接口：`POST /api/rider/order/give-up/{id}`
- 用户评价订单接口：`POST /api/order/comment`
- 新增订单评价表 `order_comment` 和升级脚本 `05_create_order_comment.sql`
- 待接单订单支持由发布用户取消，状态从 `0` 原子更新为 `5`
- 已接单订单支持由原接单骑手放弃，状态从 `1` 原子回退为 `0`
- 取消成功后写入 `order_log`
- 放弃成功后清空 `rider_id`、`accept_time` 并写入 `order_log`
- 新增订单状态异常：`409 当前订单状态不可取消`
- 新增订单状态异常：`409 当前订单状态不可放弃`
- 新增评价状态异常：`409 当前订单状态不可评价`、`409 订单已评价`
- 评价成功后写入 `order_log`，状态记录为 `4 → 4`

### 安全与一致性

- 订单归属从 `UserContext` 获取，不接收前端 `userId`
- 使用带用户、状态和逻辑删除条件的数据库更新，防止重复取消
- 订单取消与日志写入处于同一事务
- 骑手放弃与日志写入处于同一事务，重复放弃不会重复写日志
- 评价与订单日志写入处于同一事务
- 通过 `order_id` 唯一索引保证一个订单只能评价一次
- Maven 依赖未修改

## V0.4

日期：2026-07-13

### 新增

- 我的订单接口：`GET /api/order/my`
- 订单详情接口：`GET /api/order/{id}`
- BusinessException 业务异常规范
- GlobalExceptionHandler 统一异常处理
- OrderStatusEnum 订单状态枚举
- 骑手大厅接口：`GET /api/rider/order/hall`
- 骑手接单接口：`POST /api/rider/order/accept/{id}`
- 骑手我的订单接口：`GET /api/rider/order/my`
- 骑手完成配送接口：`POST /api/rider/order/finish/{id}`
- 用户确认收货接口：`POST /api/order/confirm/{id}`
- 订单日志写入：骑手接单成功后写入 `order_log`
- 订单日志写入：骑手完成配送成功后写入 `order_log`
- 订单日志写入：用户确认收货成功后写入 `order_log`
- 数据库升级：`order_info.accept_time`、`order_info.finish_time`
- 订单状态定义修正：`3` 调整为待确认收货，`4` 调整为已完成，`5` 调整为已取消。

### 修复

- 修复 `RiderInfoEntity` 字段映射错误。
- 删除 `RiderInfoEntity` 中不存在的 `status`、`deleted`。
- 修复 `order_log` 缺少 `operator_type` 的问题。
- 精简 SQL 升级脚本，当前只升级 `order_info`。
- 修复 BusinessException 返回 500 的问题。

### 状态码

| code | 说明 |
| --- | --- |
| 403 | 当前用户不是骑手 |
| 404 | 订单不存在 |
| 409 | 订单状态冲突 |
| 500 | 服务器内部错误 |

## V0.3

日期：2026-07-10

### 新增

- 用户注册
- 用户登录
- JWT 登录认证
- BCrypt 密码加密
- 地址管理
- 发布订单
- Result 统一返回
- ResultCode 状态码
- Validation 参数校验
