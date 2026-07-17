# 开发规范

> 项目：五鑫跑腿（Wuxin Paotui）  
> 当前版本：V1.0 Completed

## 一、分层规范

1. Controller 不写业务。
2. Service 写业务。
3. Mapper 只负责数据库。
4. Entity 不直接返回前端。
5. 统一使用 DTO 接收请求参数。
6. 统一使用 VO 返回前端数据。
7. 统一使用 Result 作为接口返回结构。
8. 统一使用 BusinessException 表示业务异常。

## 二、接口开发规范

新增接口必须更新：

- `API.md`
- `CHANGELOG.md`
- `PROJECT_STATUS.md`
- `POSTMAN.md`

接口开发要求：

- Controller 只接收参数、调用 Service、返回 Result。
- Service 负责业务校验、权限校验、事务、状态流转。
- Mapper 不写业务逻辑。
- 不直接返回 Entity。
- 不返回敏感字段。
- API 文档中的请求方式和路径必须以 Controller 实际映射为准。
- 每次版本收尾必须逐个检查所有 Controller 与 `API.md`。
- 文档不得记录当前 Controller 不存在的接口。

## 三、数据库变更规范

新增数据库字段必须：

- 更新 `DATABASE.md`
- 新增 SQL 升级脚本

禁止：

- 修改历史 SQL
- 启动程序时自动修改数据库
- 删除已有业务表
- 重建已有业务表
- 未确认字段含义时随意映射 Entity

数据库字段要求：

- Entity 和 Mapper 映射必须以真实数据库结构为准。
- 历史聊天记录、旧文档和字段命名推测不能替代数据库核对。
- 发现代码与数据库不一致时，应先记录风险并设计增量迁移。

## 四、异常规范

所有异常统一通过：

- `BusinessException`
- `GlobalExceptionHandler`

要求：

- 可预期业务异常不能返回 500。
- 未知系统异常统一返回 500。
- 不向前端暴露堆栈信息。

## 五、验收规范

开发完成必须执行：

1. Compile
2. Postman
3. Navicat
4. Docs

测试要求：

- 必须测试正常流程。
- 必须测试异常流程。
- 涉及用户数据时必须测试越权访问。
- 测试账号统一维护在 `TEST_ACCOUNT.md`。
- 测试环境和业务数据 ID 统一维护在 `TEST_ENVIRONMENT.md`。
- 测试账号密码或测试数据变化后必须立即同步文档。

## 六、版本收尾规范

每完成一个版本必须更新：

1. `README.md`
2. `API.md`
3. `DATABASE.md`
4. `PROJECT_STATUS.md`
5. `CHANGELOG.md`
6. `TEST_ACCOUNT.md`
7. `TEST_ENVIRONMENT.md`

版本完成后：

1. 确认 Compile、Postman、Navicat 和权限测试结果。
2. 更新项目状态和当前开发断点。
3. 提交 Git。
4. 仅为已经验收通过的稳定版本创建 Git Tag。

## 七、依据优先级

项目事实依据按以下顺序确认：

```text
真实数据库
↓
当前代码
↓
当前项目文档
↓
历史记录
```

接口路径必须以 Controller 为准，数据库字段必须以真实数据库为准。

## 八、固定要求

以后所有开发必须遵守本规范。
