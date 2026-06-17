# Repository Guidelines

## 项目结构与模块组织

AuthSphere 是自研 IAM 认证授权中心，项目介绍与详细设计以 `docs/` 下的设计文档为准。当前仓库是 Maven 父工程：根目录 `pom.xml` 定义 `com.authsphere:authsphere-parent`，打包类型为 `pom`；`README.md` 保存简要说明；`docs/` 保存需求、设计、开发计划和架构依据。

后续新增实现模块时，优先采用模块化单体结构，而不是直接拆分微服务。建议目录：

- `authsphere-*/src/main/java`：生产代码。
- `authsphere-*/src/main/resources`：配置、SQL、静态资源。
- `authsphere-*/src/test/java`：单元测试与集成测试。
- `docs/`：设计文档、接口约定、评审材料。

## 构建、测试与本地开发命令

- `mvn validate`：校验 Maven 工程模型和基础配置。
- `mvn test`：运行所有模块测试。
- `mvn package`：构建模块产物，输出到各模块 `target/`。
- `mvn clean`：清理 Maven 构建输出。

除非模块文档另有说明，命令均在仓库根目录执行。

## 编码风格与命名约定

Java 代码使用 4 空格缩进。类名使用 `PascalCase`，方法和字段使用 `camelCase`，常量使用 `UPPER_SNAKE_CASE`。包名统一放在 `com.authsphere` 下。模块名使用小写短横线，例如 `authsphere-core`、`authsphere-identity`。

领域命名应贴合设计文档中的核心概念：`Realm`、`Client`、`Account`、`Subject`、`Role`、`Policy`、`DataScope`、`Token`、`Audit`。不要把租户中心、应用中心主数据职责混入 IAM 内核；IAM 只保存引用快照、同步状态、认证上下文和授权绑定。

## 测试规范

测试文件放在对应模块的 `src/test/java`。单元测试命名为 `*Test`，集成测试可命名为 `*IT`。优先覆盖认证流程、授权决策、租户隔离、Token 校验、数据权限、审计记录等高风险逻辑。提交前至少运行 `mvn test`。

## 提交与 Pull Request 规范

当前历史只有 `Initial commit`，尚未形成严格提交规范。建议使用简短、祈使句式提交信息，可带范围，例如 `auth: add token validation service`、`docs: update IAM module plan`。

PR 应包含变更摘要、设计依据、测试结果和配置影响。涉及接口、数据库表、权限模型或登录流程的变更，应补充示例、迁移说明或回滚影响。

## 安全与配置提示

不要提交密钥、Token、私钥、Cookie 或环境专属凭据。认证、授权、主体关系、租户边界、数据权限和审计属于高风险区域，修改时必须结合 `docs/` 设计文档核对边界和影响面。


## 业务专业词描述
Account 解决“谁登录”
Subject 解决“以什么身份进入”
Realm 解决“账号属于哪个身份空间和安全策略”
Client 解决“登录哪个应用入口”
Role / Permission 解决“能做什么”
DataScope 解决“能看哪些数据”
Policy 解决“在什么条件下允许”

## 前端开发规范
进行前端页面相关操作时，**必须**使用本项目的 `ui-ux-pro-max` skill 来获取设计系统和代码规范，确保界面风格和用户体验的专业度与一致性。
