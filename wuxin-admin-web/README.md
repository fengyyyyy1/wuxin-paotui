# 五鑫跑腿总控管理后台

V1.6 总控管理后台前端，技术栈为 Vue 3、TypeScript、Vite、Vue Router、
Pinia、Axios 和 Element Plus。

## 环境

- Node.js 24
- npm 11

## 命令

```bash
npm install
npm run dev
npm run type-check
npm run lint
npm run build
```

## 环境变量

开发和生产环境均使用相对 API 前缀：

```text
VITE_API_BASE_URL=/api
```

开发服务器将`/api`代理至`http://localhost:8080`。生产环境由网关或 Nginx
转发，不在前端配置中写死后端主机。

## 当前范围

- Axios 统一请求与后端`Result<T>`处理
- Bearer Token 注入和本地恢复
- 401 清理登录状态并跳转登录页
- 403 明确提示无管理员权限
- 管理员状态 Store 和基础路由守卫
- 登录、后台首页、商家管理和 404 基础页面
- 管理员登录与商家管理 API 封装

当前断点：管理员登录页面与真实登录接口联调。
