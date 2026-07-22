# 五鑫跑腿骑手端

V1.8 骑手微信小程序，使用微信原生小程序和 TypeScript 开发。

## 页面

- 登录、骑手申请、审核状态
- 工作台、接单大厅、我的配送、配送详情
- 跑单统计、排行榜、个人中心

## 本地运行

```bash
npm install
npm run type-check
npm run lint
npm run build
```

在微信开发者工具中导入本目录。当前 `project.config.json` 已配置测试
AppID；骑手端和商家端生产发布前必须分别配置独立AppID，并把
`miniprogram/config/env.ts` 的接口地址替换为已备案的 HTTPS 合法域名。

本地联调默认连接 `http://localhost:8080`。骑手申请和资料接口依赖
`wuxin-paotui-server/src/main/resources/sql/15_update_rider_application.sql`，
该脚本已于2026-07-22由用户在Navicat中人工执行并通过结构、接口回归；
应用仍不会自动迁移数据库。

## 业务边界

- 所有订单、状态、统计和排名均来自真实后端接口。
- 当前后端没有骑手收益接口，页面不会估算或伪造收益。
- 工作台在线/休息开关仅控制本机界面，不写入服务端。
- 身份证图片使用 HTTPS URL 字段；当前没有图片上传接口。
