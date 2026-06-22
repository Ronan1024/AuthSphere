# 身份域管理接口优化设计

## 1. 目标

本次只覆盖身份域管理前端的 4 个页面：

- 身份域列表
- 创建身份域
- 编辑身份域
- 身份域详情

目标是让前端页面基于后端真实数据完成展示与编辑，不再依赖前端硬编码推断，也不再要求通过独立策略对象才能回显身份域的认证与安全配置。

本次允许：

- 新增接口
- 调整现有 `realm` 相关接口入参与出参
- 调整前端页面字段与交互

本次不做：

- 登录页配置管理
- 审计日志
- 主体管理、账号管理、客户端管理的扩展接口
- 与身份域页面无关的策略中心重构

## 2. 设计原则

- 以身份域 `realm` 作为聚合根，不再把当前页面拆成多个后端聚合拼装。
- 认证方式必须绑定数据库中的认证方式，身份域与认证方式通过 `id` 关联。
- 认证与安全配置采用“身份域直接保存”模式，不强制依赖独立策略模板引用。
- 只新增一张关系表解决多认证方式关联，不引入额外子表或抽象层。
- 兼容当前已有字段，优先保证现有页面最小改造即可联调。

## 3. 数据模型调整

### 3.1 `realm` 表保留并扩展

现有 `realm` 表已包含以下字段，可继续复用：

- 基础信息：`id`、`name`、`code`、`realm_type_id`、`status`、`description`
- SSO：`sso_enabled`、`sso_session_timeout`、`sso_idle_timeout`、`sso_single_logout`、`existing_session_handler`、`no_client_id_handler`

现有 `realm` 表中以下字段不再作为本次页面主模型依赖：

- `auth_policy_id`
- `password_policy`
- `mfa_policy`
- `unique_policy`

这些旧字段先保留兼容，不作为本次前端主读写入口。后续如彻底切换完成，可再评估下线。

### 3.2 `realm` 表新增字段

为满足前端页面直接保存需求，`realm` 新增以下字段：

#### 认证配置

- `default_auth_method_id`：默认认证方式 ID
- `mfa_auth_method_id`：MFA 认证方式 ID，可为空
- `captcha_mode`：图形验证码模式，建议值：
  - `none`
  - `always`
  - `threshold`
- `captcha_threshold`：失败多少次后触发验证码，仅 `threshold` 模式使用

#### 密码安全策略

- `password_min_length`
- `password_max_length`
- `password_complexity`
- `password_expire_days`

#### Token 安全策略

- `access_token_timeout`
- `refresh_token_timeout`
- `token_rotation_enabled`
- `token_blacklist_enabled`

#### 会话安全策略

- `session_idle_timeout`
- `session_multi_device`
- `session_max_devices`

#### 账号安全策略

- `login_fail_max_count`
- `login_fail_window_minutes`
- `login_fail_lock_minutes`
- `login_fail_auto_unlock`

### 3.3 新增关系表 `realm_auth_method_rel`

新增表：`realm_auth_method_rel`

用途：

- 保存身份域支持的认证方式列表
- 用认证方式 `id` 绑定 `auth_method` 表

建议字段：

- `id`
- `realm_id`
- `auth_method_id`
- `create_time`
- `update_time`

约束建议：

- `realm_id + auth_method_id` 唯一，避免重复绑定
- 为 `realm_id`、`auth_method_id` 建索引

不额外保存排序。前端展示顺序直接沿用 `auth_method.sort_no`。

## 4. 业务规则

### 4.1 基础信息

- `name` 必填
- `code` 必填，保持唯一
- `realmTypeId` 必填，前端继续使用 `typeCategoryId` 入参兼容
- `status` 必填

### 4.2 认证方式

- `authMethodIds` 至少选择一个
- `defaultAuthMethodId` 必须属于 `authMethodIds`
- `mfaAuthMethodId` 为空表示不启用 MFA
- `mfaAuthMethodId` 不为空时，必须是可用认证方式中的一个有效 ID

### 4.3 图形验证码

- `captchaMode = none` 时，`captchaThreshold` 可为空
- `captchaMode = always` 时，`captchaThreshold` 忽略
- `captchaMode = threshold` 时，`captchaThreshold` 必须大于 0

### 4.4 SSO

- `ssoEnabled = false` 时，SSO 细项仍允许保存默认值，不强制清空
- `ssoSingleLogout`、`existingSessionHandler`、`noClientIdHandler` 继续校验枚举合法性

### 4.5 安全策略

- 密码、Token、会话、账号安全字段都直接保存在身份域
- 本次不再要求页面通过独立策略模板回填

## 5. 接口设计

## 5.1 列表接口

接口：

- `POST /admin/realm/page`

请求：

- 兼容现有分页结构
- 查询条件保留：
  - `name`
  - `code`
  - `status`
  - `typeCategoryId`

响应字段：

- `id`
- `name`
- `code`
- `realmTypeId`
- `realmTypeName`
- `status`
- `description`
- `ssoEnabled`
- `defaultAuthMethodId`
- `defaultAuthMethodName`
- `authMethodList`
  - `id`
  - `code`
  - `name`
  - `description`
- `accountCount`
- `ssoClientCount`
- `createTime`
- `updateTime`

说明：

- 列表页不返回全部安全配置，只返回展示所需摘要字段
- `authMethodList` 从 `realm_auth_method_rel + auth_method` 聚合得到

## 5.2 详情接口

新增接口：

- `GET /admin/realm/{id}`

响应字段：

- 列表接口全部字段
- `ssoSessionTimeout`
- `ssoIdleTimeout`
- `ssoSingleLogout`
- `existingSessionHandler`
- `noClientIdHandler`
- `mfaAuthMethodId`
- `captchaMode`
- `captchaThreshold`
- `passwordMinLength`
- `passwordMaxLength`
- `passwordComplexity`
- `passwordExpireDays`
- `accessTokenTimeout`
- `refreshTokenTimeout`
- `tokenRotationEnabled`
- `tokenBlacklistEnabled`
- `sessionIdleTimeout`
- `sessionMultiDevice`
- `sessionMaxDevices`
- `loginFailMaxCount`
- `loginFailWindowMinutes`
- `loginFailLockMinutes`
- `loginFailAutoUnlock`
- `authMethodIds`

说明：

- 详情接口作为编辑页回填接口
- 不返回与当前页面无关的审计、登录页、主体统计

## 5.3 创建接口

接口：

- `POST /admin/realm`

请求字段：

- 基础信息
  - `name`
  - `code`
  - `typeCategoryId`
  - `status`
  - `description`
- SSO
  - `ssoEnabled`
  - `ssoSessionTimeout`
  - `ssoIdleTimeout`
  - `ssoSingleLogout`
  - `existingSessionHandler`
  - `noClientIdHandler`
- 认证
  - `authMethodIds`
  - `defaultAuthMethodId`
  - `mfaAuthMethodId`
  - `captchaMode`
  - `captchaThreshold`
- 密码安全
  - `passwordMinLength`
  - `passwordMaxLength`
  - `passwordComplexity`
  - `passwordExpireDays`
- Token 安全
  - `accessTokenTimeout`
  - `refreshTokenTimeout`
  - `tokenRotationEnabled`
  - `tokenBlacklistEnabled`
- 会话安全
  - `sessionIdleTimeout`
  - `sessionMultiDevice`
  - `sessionMaxDevices`
- 账号安全
  - `loginFailMaxCount`
  - `loginFailWindowMinutes`
  - `loginFailLockMinutes`
  - `loginFailAutoUnlock`

处理逻辑：

1. 校验身份域编码唯一
2. 校验身份域类型存在
3. 校验认证方式 ID 全部存在且可用
4. 校验默认认证方式属于支持认证方式集合
5. 校验 MFA 认证方式合法
6. 校验验证码配置、SSO 配置和安全字段范围
7. 保存 `realm`
8. 保存 `realm_auth_method_rel`

## 5.4 编辑接口

接口：

- `PUT /admin/realm/{id}`

请求字段：

- 与创建接口一致

处理逻辑：

1. 校验身份域存在
2. 若编码允许修改，则校验唯一；若不允许修改，前端直接禁用输入框
3. 按创建接口相同规则校验
4. 更新 `realm`
5. 删除当前身份域旧认证方式关联
6. 重建 `realm_auth_method_rel`

## 5.5 状态切换接口

接口保留：

- `PUT /admin/realm/status/{id}`

本次不扩展语义，只保留启用/禁用切换。

## 5.6 删除接口

接口保留：

- `DELETE /admin/realm/{id}`

本次保持已有校验：

- 有账号引用时不可删除
- 有客户端引用时不可删除

不新增主体引用校验，避免把未完成的主体模块一起拖入本次范围。

## 6. 前端页面调整

### 6.1 列表页

- 删除或停止使用基于 `code` 推断认证方式、策略状态的假数据逻辑
- 直接展示后端返回的：
  - `authMethodList`
  - `defaultAuthMethodName`
  - `accountCount`
  - `ssoClientCount`
- 查询项只保留后端实际支持字段

### 6.2 创建页

- 不再提交旧的策略引用式字段作为主逻辑
- 直接提交身份域聚合字段
- 认证方式下拉与选项从认证方式接口加载

### 6.3 编辑页

- 页面进入时调用详情接口回填
- 默认认证方式、MFA 认证方式必须从当前认证方式库中选择
- 编码是否允许编辑，由页面按当前业务决定；如果存在已关联数据，可前端锁定

### 6.4 详情页

- 停止使用本地推断填充的策略摘要
- 全部使用详情接口回显

## 7. 实现拆分

建议按以下顺序实施：

1. SQL：扩展 `realm` 表并新增 `realm_auth_method_rel`
2. 后端模型：`model`、`dto`、`mapper`、`service`、`controller`
3. 后端校验：认证方式绑定校验、默认认证方式归属校验、验证码与 SSO 枚举校验
4. 前端 API 类型调整
5. 前端 4 个页面联调
6. 测试补齐

## 8. 测试要求

后端至少补以下测试：

- 创建身份域成功
- 创建时默认认证方式不属于 `authMethodIds` 失败
- 创建时认证方式 ID 无效失败
- 创建时 `captchaMode = threshold` 且阈值非法失败
- 编辑时认证方式关联重建成功
- 详情接口正确返回认证方式列表与安全字段

前端联调至少验证：

- 列表真实展示认证方式和统计值
- 创建后可在详情页正确回显
- 编辑保存后再次进入仍能完整回显

## 9. 风险与边界

- 当前仓库已存在对 `authPolicyId/passwordPolicy/mfaPolicy/uniquePolicy` 的旧模型引用，本次只做兼容保留，不同步清理全部历史逻辑。
- 如果编辑页后续要求“编码在部分条件下可编辑、部分条件下不可编辑”，需要单独补充业务规则，本次设计不强行定义。
- 如果后续需要“主体数、客户端数、登录页配置、策略完整度”等扩展展示，应作为下一轮需求处理，不并入本次实现。

## 10. 推荐实现结论

采用以下方案：

- `realm` 主表直存认证与安全配置
- 新增 `realm_auth_method_rel` 保存身份域支持的认证方式
- 新增详情接口 `GET /admin/realm/{id}`
- 保留现有删除、启停接口语义

这是当前需求下最小、最稳、最容易联调的实现路径。
