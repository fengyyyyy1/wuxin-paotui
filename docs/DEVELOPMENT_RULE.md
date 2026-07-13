# 开发规范

> 项目：五鑫跑腿（Wuxin Paotui）  
> 当前版本：V0.4

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

## 六、固定要求

以后所有开发必须遵守本规范。
