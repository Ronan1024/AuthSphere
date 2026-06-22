create table realm
(
    id               bigint       not null primary key,
    code             varchar(255) null comment '身份域编码， 唯一',
    name             varchar(255) null comment '身份域名称',
    realm_type_id    bigint       null comment '身份域类型',
    auth_policy_id   bigint       null comment '默认认证策略ID',
    register_enabled bit(1)       null comment '是否允许注册',
    sso_enabled      bit(1)       null comment '是否开启SSO',
    sso_session_timeout int       null comment 'SSO会话有效期，单位小时',
    sso_idle_timeout int          null comment 'SSO空闲超时，单位分钟',
    sso_single_logout varchar(32) null comment '单点退出策略',
    existing_session_handler varchar(32) null comment '已存在会话处理方式',
    no_client_id_handler varchar(32) null comment '无client_id时的处理方式',
    default_auth_method_id bigint null comment '默认认证方式ID',
    mfa_auth_method_id bigint null comment 'MFA认证方式ID',
    captcha_mode varchar(32) null comment '图形验证码模式',
    captcha_threshold int null comment '图形验证码阈值',
    status           tinyint(1)   null comment '状态',
    password_policy  bigint       null comment '密码策略',
    mfa_policy       bigint       null comment 'MFA 策略',
    unique_policy    bigint       null comment '账号唯一性规则',
    password_min_length int       null comment '密码最小长度',
    password_max_length int       null comment '密码最大长度',
    password_complexity varchar(32) null comment '密码复杂度',
    password_expire_days int      null comment '密码过期天数',
    access_token_timeout int      null comment 'Access Token有效期(分钟)',
    refresh_token_timeout int     null comment 'Refresh Token有效期(天)',
    token_rotation_enabled bit(1) null comment '是否开启Refresh Token轮换',
    token_blacklist_enabled bit(1) null comment '是否开启Token黑名单',
    session_idle_timeout int      null comment '会话空闲超时(分钟)',
    session_multi_device varchar(32) null comment '多端会话策略',
    session_max_devices int       null comment '最大登录设备数',
    login_fail_max_count int      null comment '登录失败最大次数',
    login_fail_window_minutes int null comment '登录失败统计窗口(分钟)',
    login_fail_lock_minutes int   null comment '登录失败锁定时长(分钟)',
    login_fail_auto_unlock bit(1) null comment '是否自动解锁',
    description      varchar(500) null comment '描述',
    create_time      datetime     null,
    update_time      datetime     null
)
    comment '身份域信息' engine = InnoDB;

create table realm_auth_method_rel
(
    id             bigint   not null primary key,
    realm_id       bigint   not null comment '身份域ID',
    auth_method_id bigint   not null comment '认证方式ID',
    create_time    datetime null,
    update_time    datetime null,
    constraint uk_realm_auth_method unique (realm_id, auth_method_id),
    key idx_realm_auth_method_realm (realm_id),
    key idx_realm_auth_method_method (auth_method_id)
)
    comment '身份域与认证方式关联表' engine = InnoDB;

create table login_page
(
    id                       bigint       not null primary key,
    code                     varchar(64)  not null comment '登录页编码',
    name                     varchar(128) not null comment '登录页名称',
    applicable_realm_type_id bigint       null comment '适用身份域类型ID',
    page_title               varchar(128) not null comment '页面标题',
    page_subtitle            varchar(255) null comment '页面副标题',
    logo_url                 varchar(512) null comment 'Logo地址',
    background_url           varchar(512) null comment '背景图地址',
    logo_object_key          varchar(512) null comment 'Logo OSS对象标识',
    background_object_key    varchar(512) null comment '背景图 OSS对象标识',
    layout_mode              varchar(32)  not null default 'CENTER_CARD' comment '页面布局模式',
    theme_config_json        text         null comment '主题配置JSON',
    component_config_json    text         null comment '组件展示配置JSON',
    micro_frontend_url       varchar(512) null comment '微前端远程入口地址',
    auth_methods             varchar(255) not null comment '登录方式，逗号分隔',
    default_auth_method      varchar(64)  not null comment '默认登录方式',
    show_forgot_password     bit(1)      not null default b'1' comment '是否显示忘记密码',
    forgot_password_url      varchar(512) null comment '忘记密码跳转地址',
    show_register            bit(1)       not null default b'0' comment '是否显示注册入口',
    register_url             varchar(512) null comment '注册入口跳转地址',
    show_third_party_login   bit(1)       not null default b'0' comment '是否显示第三方登录',
    success_redirect_url     varchar(512) null comment '登录成功跳转地址',
    failure_prompt_mode      varchar(32)  not null default 'page' comment '登录失败提示方式',
    default_page             bit(1)       not null default b'0' comment '是否默认登录页',
    system_builtin           bit(1)       not null default b'0' comment '是否系统内置',
    status                   tinyint(1)   not null comment '状态',
    description              varchar(512) null comment '备注',
    deleted                  bit(1)       not null default b'0' comment '逻辑删除',
    create_time              datetime     null,
    update_time              datetime     null,
    constraint uk_login_page_code unique (code),
    key idx_login_page_realm_type (applicable_realm_type_id),
    key idx_login_page_status (status)
)
    comment '登录页配置' engine = InnoDB;

create table auth_policy
(
    id                          bigint       not null primary key,
    code                        varchar(64)  not null comment '认证策略编码',
    name                        varchar(128) not null comment '认证策略名称',
    applicable_realm_id         bigint       null comment '适用身份域ID',
    auth_methods                varchar(255) not null comment '支持的登录方式，逗号分隔',
    default_auth_method         varchar(64)  not null comment '默认登录方式',
    captcha_enabled             bit(1)      not null default b'0' comment '是否启用图形验证码',
    captcha_failure_threshold   int          null comment '失败几次后启用验证码',
    captcha_ttl_seconds         int          null comment '验证码有效期，单位秒',
    captcha_error_limit         int          null comment '验证码错误次数限制',
    max_failure_count           int          not null comment '最大连续失败次数',
    failure_window_minutes      int          not null comment '失败统计周期，单位分钟',
    lock_minutes                int          not null comment '锁定时长，单位分钟',
    notify_user                 bit(1)      not null default b'0' comment '锁定后是否通知用户',
    risk_log_enabled            bit(1)      not null default b'1' comment '是否记录风险日志',
    mfa_enabled                 bit(1)      not null default false comment '是否启用MFA',
    mfa_triggers                varchar(255) null comment 'MFA触发条件，逗号分隔',
    mfa_methods                 varchar(255) null comment 'MFA方式，逗号分隔',
    remember_device_enabled     bit(1)      not null default b'0' comment '是否允许记住设备',
    remember_device_days        int          null comment '记住设备有效期，单位天',
    ip_restriction_enabled      bit(1)      not null default b'0' comment '是否限制登录IP',
    device_check_enabled        bit(1)      not null default b'0' comment '是否校验设备',
    remote_login_check_enabled  bit(1)      not null default b'0' comment '是否校验异地登录',
    abnormal_time_check_enabled bit(1)      not null default b'0' comment '是否校验异常时间登录',
    system_builtin              bit(1)      not null default b'0' comment '是否系统内置',
    status                      tinyint(1)   not null comment '状态',
    description                 varchar(512) null comment '备注',
    deleted                     bit(1)      not null default b'0' comment '逻辑删除',
    create_time                 datetime     null,
    update_time                 datetime     null,
    constraint uk_auth_policy_code unique (code),
    key idx_auth_policy_realm (applicable_realm_id),
    key idx_auth_policy_status (status)
)
    comment '认证策略' engine = InnoDB;

create table auth_method
(
    id               bigint       not null primary key,
    code             varchar(64)  not null comment '认证方式唯一编码',
    name             varchar(128) not null comment '认证方式名称',
    positions        varchar(255) not null comment '可用位置，逗号分隔',
    applicable_range varchar(128) null comment '适用范围说明',
    params_json      json         null comment '认证方式扩展参数，敏感字段接口脱敏',
    sort_no          int          not null default 0 comment '排序值，越小越靠前',
    system_builtin   bit(1)       not null default b'0' comment '是否系统内置',
    status           tinyint(1)   not null comment '状态：1启用，2待配置，3禁用',
    description      varchar(512) null comment '备注',
    deleted          bit(1)       not null default b'0' comment '逻辑删除',
    create_time      datetime     null,
    update_time      datetime     null,
    constraint uk_auth_method_code unique (code),
    key idx_auth_method_status_sort (status, sort_no)
)
    comment '认证方式配置' engine = InnoDB;

insert ignore into auth_method
(id, code, name, positions, applicable_range, params_json, sort_no, system_builtin, status, description, deleted, create_time, update_time)
values
    (1001001, 'password_login', '账号密码', '主登录',
     '全部',
     json_object('passwordPolicy', 'default', 'maxRetries', 5, 'lockDuration', 30),
     10, b'1', 1, '系统内置账号密码认证方式，字段结构由系统锁定。', b'0', now(), now()),
    (1001002, 'totp_login', 'TOTP 动态口令', 'MFA 二次认证,敏感操作',
     '全部',
     json_object('totpDigits', 6, 'totpPeriod', 30, 'totpWindow', 1),
     20, b'1', 1, '系统内置 TOTP 动态口令认证方式，字段结构由系统锁定。', b'0', now(), now()),
    (1001003, 'extended_login', '扩展登录', '主登录,账号绑定校验',
     '全部',
     json_object('formSchema', json_array()),
     30, b'1', 2, '系统内置扩展登录模板，允许为具体接入场景配置自定义字段。', b'0', now(), now()),
    (1001004, 'client_credentials', 'Client Credentials', '接口认证',
     '服务端接口认证',
     json_object('tokenTtl', 365, 'signAlg', 'HS256', 'allowScope', 'iam:token,api:read'),
     40, b'1', 1, '系统内置服务端接口凭证认证方式，字段结构由系统锁定。', b'0', now(), now());

create table password_policy
(
    id                   bigint       not null primary key,
    code                 varchar(100) not null comment '策略编码',
    name                 varchar(100) not null comment '策略名称',
    min_length           int          not null comment '最小密码长度',
    max_length           int          not null comment '最大密码长度',
    require_uppercase    boolean      not null comment '是否要求大写字母',
    require_lowercase    boolean      not null comment '是否要求小写字母',
    require_digit        boolean      not null comment '是否要求数字',
    require_special_char boolean      not null comment '是否要求特殊字符',
    disallow_username    boolean      not null comment '是否禁止密码包含用户名',
    history_count        int          not null comment '历史密码不可重复次数',
    expire_days          int          not null comment '密码有效期天数，0表示不限制',
    retry_limit          int          not null comment '连续失败锁定阈值，0表示不启用',
    lock_minutes         int          not null comment '锁定分钟数，0表示不自动解锁',
    status               tinyint(1)   not null comment '状态',
    description          varchar(500) null comment '描述',
    create_time          datetime     null,
    update_time          datetime     null,
    constraint uk_password_policy_code unique (code)
)
    comment '密码策略模板' engine = InnoDB;

create table mfa_policy
(
    id                      bigint       not null primary key,
    code                    varchar(100) not null comment '策略编码',
    name                    varchar(100) not null comment '策略名称',
    required                boolean      not null comment '是否强制 MFA',
    sms_enabled             boolean      not null comment '是否启用短信 MFA',
    email_enabled           boolean      not null comment '是否启用邮箱 MFA',
    totp_enabled            boolean      not null comment '是否启用 TOTP MFA',
    webauthn_enabled        boolean      not null comment '是否启用 WebAuthn MFA',
    remember_device_enabled boolean      not null comment '是否记住设备',
    remember_device_days    int          not null comment '记住设备天数',
    risk_based_enabled      boolean      not null comment '是否启用风险触发 MFA',
    status                  tinyint(1)   not null comment '状态',
    description             varchar(500) null comment '描述',
    create_time             datetime     null,
    update_time             datetime     null,
    constraint uk_mfa_policy_code unique (code)
)
    comment 'MFA 策略模板' engine = InnoDB;

create table unique_policy
(
    id                 bigint       not null primary key,
    code               varchar(100) not null comment '规则编码',
    name               varchar(100) not null comment '规则名称',
    username_unique    boolean      not null comment '用户名是否唯一',
    email_unique       boolean      not null comment '邮箱是否唯一',
    mobile_unique      boolean      not null comment '手机号是否唯一',
    case_sensitive     boolean      not null comment '是否大小写敏感',
    allow_email_reuse  boolean      not null comment '是否允许邮箱复用',
    allow_mobile_reuse boolean      not null comment '是否允许手机号复用',
    status             tinyint(1)   not null comment '状态',
    description        varchar(500) null comment '描述',
    create_time        datetime     null,
    update_time        datetime     null,
    constraint uk_unique_policy_code unique (code)
)
    comment '账号唯一性规则模板' engine = InnoDB;

create table type_category
(
    id             bigint       not null primary key,
    code           varchar(100) null comment '分类类型编号',
    name           varchar(100) null comment '分类名称',
    description    varchar(500) null comment '描述',
    system_builtin boolean      null comment '是否系统内置',
    editable       boolean      null comment '是否允许编辑',
    status         tinyint(1)   null comment '状态',
    create_time    datetime     null,
    update_time    datetime     null
)
    comment '类型项' engine = InnoDB;



CREATE TABLE subject_type
(
    id               BIGINT PRIMARY KEY COMMENT '主键ID',
    code             VARCHAR(64)  NOT NULL UNIQUE COMMENT '主体类型编码',
    name             VARCHAR(128) NOT NULL COMMENT '主体类型名称',
    category         VARCHAR(64)           DEFAULT NULL COMMENT '主体分类',
    can_have_members bit(1)       NOT NULL DEFAULT b'1' COMMENT '是否允许拥有成员',
    can_open_app     bit(1)       NOT NULL DEFAULT b'1' COMMENT '是否允许开通应用',
    can_assign_role  bit(1)       NOT NULL DEFAULT b'1' COMMENT '是否允许分配角色',
    can_be_root      bit          NOT NULL DEFAULT b'0' COMMENT '是否可以作为数据隔离根主体',
    built_in         bit          NOT NULL DEFAULT b'0' COMMENT '是否系统内置',
    status           tinyint(1)   NOT NULL COMMENT '状态：ENABLED/DISABLED',
    description      VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    create_time      DATETIME     NOT NULL COMMENT '创建时间',
    update_time      DATETIME     NOT NULL COMMENT '更新时间'
) COMMENT ='主体类型表' engine = InnoDB;

CREATE TABLE subject_type_relation
(
    id             BIGINT PRIMARY KEY COMMENT '主键ID',
    parent_type_id bigint     NOT NULL COMMENT '上级主体类型id',
    child_type_id  bigint     NOT NULL COMMENT '下级主体类型id',
    relation_type  tinyint(1) NOT NULL COMMENT '关系类型：1.MANAGE 2.OWN 3.BELONG 4.SERVICE 5.BIND',
    allow_create   bit(1)     NOT NULL DEFAULT b'0' COMMENT '是否允许创建',
    allow_manage   bit(1)     NOT NULL DEFAULT b'0' COMMENT '是否允许管理',
    status         tinyint(1) NOT NULL COMMENT '状态：ENABLED/DISABLED',
    description    VARCHAR(512)        DEFAULT NULL COMMENT '描述',
    create_time    DATETIME   NOT NULL COMMENT '创建时间',
    update_time    DATETIME   NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_subject_type_relation (
                                         parent_type_id,
                                         child_type_id,
                                         relation_type
        )
) COMMENT ='主体类型关系规则表' engine = InnoDB;


CREATE TABLE subject
(
    id                BIGINT PRIMARY KEY COMMENT '主键ID',
    subject_type_id   bigint       NOT NULL COMMENT '主体类型编码',
    realm_id          BIGINT       NOT NULL COMMENT '默认身份域ID',
    code              VARCHAR(64)  NOT NULL COMMENT '主体编码',
    name              VARCHAR(128) NOT NULL COMMENT '主体名称',
    status            tinyint(1)   NOT NULL COMMENT '状态：ENABLED/DISABLED',
    root_subject_id   BIGINT                DEFAULT NULL COMMENT '数据隔离根主体ID',
    parent_subject_id BIGINT                DEFAULT NULL COMMENT '快捷上级主体ID',
    is_root           TINYINT      NOT NULL DEFAULT 0 COMMENT '是否数据隔离根主体',
    built_in          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否系统内置主体',
    description       VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    create_time       DATETIME     NOT NULL COMMENT '创建时间',
    update_time       DATETIME     NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_subject_code (code),
    KEY idx_subject_type (subject_type_id),
    KEY idx_subject_realm (realm_id),
    KEY idx_subject_root (root_subject_id),
    KEY idx_subject_parent (parent_subject_id)
) COMMENT ='主体表';



create table account
(
    id              bigint       not null primary key,
    realm_id        bigint       null comment '身份域id',
    username        varchar(255) null comment '用户名',
    password        varchar(255) null comment '密码',
    email           varchar(255) null comment '邮箱',
    mobile          varchar(15)  null comment '手机号',
    status          tinyint(1)   null comment '账号状态： 1.正常 2.锁定 3.禁用',
    nickname        VARCHAR(128) DEFAULT NULL COMMENT '昵称',
    avatar          VARCHAR(512) DEFAULT NULL COMMENT '头像',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',

    create_time     datetime     null,
    update_time     datetime     null,
    constraint fk_account_realm foreign key (realm_id) references realm (id)
)
    comment '账号表' engine = InnoDB;

CREATE TABLE account_credential
(
    id               BIGINT PRIMARY KEY COMMENT '凭证ID',

    account_id       BIGINT       NOT NULL COMMENT '账号ID',
    realm_id         BIGINT       NOT NULL COMMENT '身份域ID，冗余便于查询',
    credential_type  tinyint(1)   NOT NULL COMMENT '凭证类型：1.PASSWORD 2.TOTP 3.PASSKEY',
    credential_value VARCHAR(512) NOT NULL COMMENT '凭证值，密码为hash',
    password_salt    VARCHAR(128)          DEFAULT NULL COMMENT '密码盐，可选',
    password_algo    VARCHAR(64)           DEFAULT NULL COMMENT '密码算法，如 BCrypt',
    force_change     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否强制修改',
    expire_at        DATETIME              DEFAULT NULL COMMENT '凭证过期时间',
    status           tinyint(1)   NOT NULL COMMENT '状态 ENABLED/DISABLED',

    create_time      DATETIME     NOT NULL,
    update_time      DATETIME     NOT NULL,

    UNIQUE KEY uk_account_credential (account_id, credential_type),
    KEY idx_credential_account (account_id),
    KEY idx_credential_realm (realm_id)
) COMMENT ='账号凭证表';


# TODO 待处理
CREATE TABLE iam_realm_identity_provider
(
    id                BIGINT PRIMARY KEY COMMENT '身份源ID',

    realm_id          BIGINT       NOT NULL COMMENT '所属身份域',

    provider_type     VARCHAR(32)  NOT NULL COMMENT '身份源类型：WECHAT_MINI/ALIPAY_MINI/DOUYIN_MINI/OIDC/SAML/LDAP/WECHAT_WORK/DINGTALK',
    provider_code     VARCHAR(64)  NOT NULL COMMENT '身份源编码',
    provider_name     VARCHAR(128) NOT NULL COMMENT '身份源名称',

    app_id            VARCHAR(128) DEFAULT NULL COMMENT '第三方应用ID，如小程序 appid',
    app_secret        VARCHAR(512) DEFAULT NULL COMMENT '第三方应用密钥，加密存储',

    client_id         VARCHAR(256) DEFAULT NULL COMMENT 'OAuth/OIDC Client ID',
    client_secret     VARCHAR(512) DEFAULT NULL COMMENT 'OAuth/OIDC Client Secret，加密存储',

    authorization_uri VARCHAR(512) DEFAULT NULL,
    token_uri         VARCHAR(512) DEFAULT NULL,
    user_info_uri     VARCHAR(512) DEFAULT NULL,
    jwks_uri          VARCHAR(512) DEFAULT NULL,

    config_json       TEXT         DEFAULT NULL COMMENT '扩展配置JSON',

    status            VARCHAR(32)  NOT NULL COMMENT 'ENABLED/DISABLED',

    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,

    UNIQUE KEY uk_realm_provider_code (realm_id, provider_code),
    KEY idx_provider_realm (realm_id),
    KEY idx_provider_type (provider_type)
) COMMENT ='身份域外部身份源配置表';


CREATE TABLE iam_account_external_identity
(
    id                BIGINT PRIMARY KEY COMMENT '绑定ID',
    realm_id          BIGINT       NOT NULL COMMENT '身份域ID',
    account_id        BIGINT       NOT NULL COMMENT '本地账号ID',
    provider_id       BIGINT       NOT NULL COMMENT '身份源配置ID',
    provider_type     VARCHAR(32)  NOT NULL COMMENT '身份源类型',
    provider_code     VARCHAR(64)  NOT NULL COMMENT '身份源编码',
    external_user_id  VARCHAR(256) NOT NULL COMMENT '第三方用户唯一ID，如 openid/user_id/sub',
    external_union_id VARCHAR(256) DEFAULT NULL COMMENT '第三方统一ID，如微信 unionid',
    external_app_id   VARCHAR(128) DEFAULT NULL COMMENT '第三方应用ID，如小程序 appid',
    external_username VARCHAR(256) DEFAULT NULL COMMENT '第三方用户名',
    external_nickname VARCHAR(256) DEFAULT NULL COMMENT '第三方昵称',
    external_avatar   VARCHAR(512) DEFAULT NULL COMMENT '第三方头像',
    external_mobile   VARCHAR(64)  DEFAULT NULL COMMENT '第三方手机号',
    external_email    VARCHAR(256) DEFAULT NULL COMMENT '第三方邮箱',
    raw_profile       TEXT         DEFAULT NULL COMMENT '第三方原始用户资料JSON',

    status            tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',

    bind_at           DATETIME     NOT NULL COMMENT '绑定时间',
    last_login_time   DATETIME     DEFAULT NULL COMMENT '最近一次通过该身份登录时间',
    create_time       DATETIME     NOT NULL,
    update_time       DATETIME     NOT NULL,

    UNIQUE KEY uk_provider_external_user (
                                          provider_id,
                                          external_user_id
        ),

    KEY idx_external_account (account_id),
    KEY idx_external_realm (realm_id),
    KEY idx_external_union_id (external_union_id),
    KEY idx_external_app_id (external_app_id)
) COMMENT ='账号第三方身份绑定表';


CREATE TABLE iam_login_log
(
    id               BIGINT PRIMARY KEY COMMENT '登录日志ID',

    realm_id         BIGINT      NOT NULL COMMENT '身份域ID',
    account_id       BIGINT       DEFAULT NULL COMMENT '账号ID，登录失败时可能为空',

    login_entry_code VARCHAR(64)  DEFAULT NULL COMMENT '登录入口',
    provider_type    VARCHAR(32)  DEFAULT NULL COMMENT '登录方式来源',
    provider_id      BIGINT       DEFAULT NULL COMMENT '第三方身份源ID',

    login_type       VARCHAR(32) NOT NULL COMMENT 'PASSWORD/SMS/EMAIL/WECHAT_MINI/ALIPAY_MINI/DOUYIN_MINI/OIDC',
    login_identifier VARCHAR(256) DEFAULT NULL COMMENT '登录标识，手机号/邮箱/用户名/openid等',

    success          TINYINT     NOT NULL COMMENT '是否成功',
    fail_reason      VARCHAR(256) DEFAULT NULL COMMENT '失败原因',

    ip               VARCHAR(64)  DEFAULT NULL COMMENT '登录IP',
    user_agent       VARCHAR(512) DEFAULT NULL COMMENT 'UA',
    device_id        VARCHAR(128) DEFAULT NULL COMMENT '设备ID',

    create_time      DATETIME    NOT NULL,

    KEY idx_login_account (account_id),
    KEY idx_login_realm (realm_id),
    KEY idx_login_created (create_time)
) COMMENT ='登录日志表';


CREATE TABLE subject_member
(
    id                     BIGINT PRIMARY KEY COMMENT '成员ID',

    subject_id             BIGINT     NOT NULL COMMENT '主体ID',
    account_id             BIGINT     NOT NULL COMMENT '账号ID',

    member_type            tinyint(1) NOT NULL COMMENT '1.OWNER 2.ADMIN 3.MEMBER 4.STAFF',
    display_name           VARCHAR(128) DEFAULT NULL COMMENT '主体内显示名称',
    remark                 VARCHAR(512) DEFAULT NULL COMMENT '备注',
    status                 tinyint(1) NOT NULL COMMENT '1.ENABLED 2.DISABLED',

    joined_at              DATETIME     DEFAULT NULL COMMENT '加入时间',
    joined_by_account_id   BIGINT       DEFAULT NULL COMMENT '添加人账号ID',

    disabled_at            DATETIME     DEFAULT NULL COMMENT '禁用时间',
    disabled_by_account_id BIGINT       DEFAULT NULL COMMENT '禁用人账号ID',

    removed_at             DATETIME     DEFAULT NULL COMMENT '移除时间',
    removed_by_account_id  BIGINT       DEFAULT NULL COMMENT '移除人账号ID',

    create_time            DATETIME   NOT NULL,
    update_time            DATETIME   NOT NULL,

    UNIQUE KEY uk_subject_account (subject_id, account_id),

    KEY idx_member_subject (subject_id),
    KEY idx_member_account (account_id),
    KEY idx_member_type (member_type),
    KEY idx_member_status (status)
) COMMENT ='主体成员表';

CREATE TABLE app
(
    id          BIGINT PRIMARY KEY COMMENT '应用ID',
    app_code    VARCHAR(64)  NOT NULL COMMENT '应用编码',
    app_name    VARCHAR(128) NOT NULL COMMENT '应用名称',
    app_type    VARCHAR(32)  NOT NULL COMMENT '应用类型：IAM/MALL/PAYMENT/WAREHOUSE/LOGISTICS/CUSTOM',
    entry_url   VARCHAR(512)          DEFAULT NULL COMMENT '应用入口',
    icon        VARCHAR(128)          DEFAULT NULL COMMENT '应用图标',
    status      tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    built_in    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
    description VARCHAR(512)          DEFAULT NULL,
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    UNIQUE KEY uk_app_code (app_code),
    KEY idx_app_type (app_type),
    KEY idx_app_status (status)
) COMMENT ='应用定义表';

CREATE TABLE app_client_menu
(
    id             BIGINT PRIMARY KEY COMMENT '菜单ID',
    app_code       VARCHAR(64)  NOT NULL COMMENT '所属应用编码',
    client_code    VARCHAR(128) NOT NULL COMMENT '应用端编码',
    parent_id      BIGINT                DEFAULT NULL COMMENT '父级菜单ID',
    menu_code      VARCHAR(128) NOT NULL COMMENT '菜单编码',
    menu_name      VARCHAR(128) NOT NULL COMMENT '菜单名称',
    route_path     VARCHAR(256)          DEFAULT NULL COMMENT '前端路由',
    component_path VARCHAR(256)          DEFAULT NULL COMMENT '组件路径',
    icon           VARCHAR(128)          DEFAULT NULL COMMENT '图标',
    sort_no        INT                   DEFAULT 0 COMMENT '排序',
    visible        TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见',
    status         tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    built_in       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
    create_time    DATETIME     NOT NULL,
    update_time    DATETIME     NOT NULL,
    UNIQUE KEY uk_app_client_menu_code (app_code, client_code, menu_code),
    KEY idx_menu_client (app_code, client_code),
    KEY idx_menu_parent (parent_id)
) COMMENT ='应用端菜单资源表';


CREATE TABLE app_client_permission
(
    id              BIGINT PRIMARY KEY COMMENT '权限ID',
    app_code        VARCHAR(64)  NOT NULL COMMENT '所属应用编码',
    client_code     VARCHAR(128) NOT NULL COMMENT '应用端编码',
    menu_id         BIGINT                DEFAULT NULL COMMENT '所属菜单ID',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    permission_type tinyint(1)   NOT NULL COMMENT '1.BUTTON 2.API 3.DATA',
    api_path        VARCHAR(256)          DEFAULT NULL COMMENT 'API接口路径',
    method          VARCHAR(16)           DEFAULT NULL COMMENT 'HTTP请求方法',
    description     VARCHAR(512)          DEFAULT NULL,
    status          tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    built_in        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
    create_time     DATETIME     NOT NULL,
    update_time     DATETIME     NOT NULL,
    UNIQUE KEY uk_app_client_permission_code (app_code, client_code, permission_code),
    KEY idx_permission_client (app_code, client_code),
    KEY idx_permission_menu (menu_id)
) COMMENT ='应用端权限资源表';


CREATE TABLE app_instance
(
    id               BIGINT PRIMARY KEY COMMENT '应用实例ID',
    app_id           BIGINT       NOT NULL COMMENT '应用ID',
    app_code         VARCHAR(64)  NOT NULL COMMENT '应用编码',
    owner_subject_id BIGINT       NOT NULL COMMENT '应用实例归属主体',
    root_subject_id  BIGINT       NOT NULL COMMENT '数据隔离根主体',
    instance_code    VARCHAR(128) NOT NULL COMMENT '实例编码',
    instance_name    VARCHAR(128) NOT NULL COMMENT '实例名称',
    open_mode        VARCHAR(32)  NOT NULL DEFAULT 'PLATFORM' COMMENT 'PLATFORM/SELF',
    status           tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    config_json      TEXT                  DEFAULT NULL COMMENT '实例配置JSON',
    create_time      DATETIME     NOT NULL,
    update_time      DATETIME     NOT NULL,
    UNIQUE KEY uk_subject_app (owner_subject_id, app_code),
    UNIQUE KEY uk_instance_code (instance_code),
    KEY idx_instance_app (app_id),
    KEY idx_instance_app_code (app_code),
    KEY idx_instance_owner_subject (owner_subject_id),
    KEY idx_instance_root_subject (root_subject_id)
) COMMENT ='应用实例表';


CREATE TABLE app_instance_menu
(
    id              BIGINT PRIMARY KEY COMMENT '主键ID',
    app_instance_id BIGINT     NOT NULL COMMENT '应用实例ID',
    menu_id         BIGINT     NOT NULL COMMENT '菜单ID',
    status          tinyint(1) NOT NULL COMMENT 'ENABLED/DISABLED',
    create_time     DATETIME   NOT NULL,
    update_time     DATETIME   NOT NULL,
    UNIQUE KEY uk_instance_menu (app_instance_id, menu_id),
    KEY idx_instance_menu_instance (app_instance_id),
    KEY idx_instance_menu_menu (menu_id)
) COMMENT ='应用实例启用菜单表';


CREATE TABLE app_instance_permission
(
    id              BIGINT PRIMARY KEY COMMENT '主键ID',
    app_instance_id BIGINT     NOT NULL COMMENT '应用实例ID',
    permission_id   BIGINT     NOT NULL COMMENT '权限ID',
    status          tinyint(1) NOT NULL COMMENT 'ENABLED/DISABLED',
    create_time     DATETIME   NOT NULL,
    update_time     DATETIME   NOT NULL,
    UNIQUE KEY uk_instance_permission (app_instance_id, permission_id),
    KEY idx_instance_permission_instance (app_instance_id),
    KEY idx_instance_permission_permission (permission_id)
) COMMENT ='应用实例启用权限表';



CREATE TABLE app_client
(
    id                BIGINT PRIMARY KEY COMMENT '应用端ID',
    app_id            BIGINT       NOT NULL COMMENT '应用ID',
    app_code          VARCHAR(64)  NOT NULL COMMENT '应用编码',
    client_code       VARCHAR(128) NOT NULL COMMENT '应用端编码',
    client_name       VARCHAR(128) NOT NULL COMMENT '应用端名称',
    client_type       tinyint      NOT NULL COMMENT '1.ADMIN_WEB 2.MERCHANT_WEB 3.MINI_PROGRAM 4.H5 5.OPEN_API 6.SERVICE',
    client_secret     VARCHAR(512)          DEFAULT NULL COMMENT '客户端密钥，加密存储',
    default_realm_id  BIGINT                DEFAULT NULL COMMENT '默认身份域ID',
    default_entry_url VARCHAR(512)          DEFAULT NULL COMMENT '默认入口地址',
    login_mode        VARCHAR(32)  NOT NULL DEFAULT 'IAM_HOSTED' COMMENT '登录接入方式：IAM_HOSTED/EXTERNAL_REDIRECT/API_ONLY/SERVICE',
    external_login_url VARCHAR(512)         DEFAULT NULL COMMENT '客户自有登录页地址',
    login_callback_url VARCHAR(512)         DEFAULT NULL COMMENT '登录回调地址',
    login_page_id     BIGINT                DEFAULT NULL COMMENT '覆盖身份域默认登录页ID',
    auth_policy_id    BIGINT                DEFAULT NULL COMMENT '覆盖身份域默认认证策略ID',
    oss_config_id     BIGINT                DEFAULT NULL COMMENT '客户端绑定的OSS外部配置ID',
    status            tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    built_in          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
    description       VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    create_time       DATETIME     NOT NULL,
    update_time       DATETIME     NOT NULL,
    UNIQUE KEY uk_app_client_code (app_code, client_code),
    KEY idx_client_app (app_id),
    KEY idx_client_type (client_type),
    KEY idx_client_realm (default_realm_id),
    KEY idx_client_login_page (login_page_id),
    KEY idx_client_auth_policy (auth_policy_id),
    KEY idx_client_login_mode (login_mode),
    KEY idx_client_oss_config (oss_config_id)
) COMMENT ='应用端定义表';

CREATE TABLE app_client_instance
(
    id                   BIGINT PRIMARY KEY COMMENT '应用端实例ID',
    app_instance_id      BIGINT       NOT NULL COMMENT '应用实例ID',
    app_id               BIGINT       NOT NULL COMMENT '应用ID',
    app_code             VARCHAR(64)  NOT NULL COMMENT '应用编码',
    app_client_id        BIGINT       NOT NULL COMMENT '应用端定义ID',
    client_code          VARCHAR(128) NOT NULL COMMENT '应用端编码',
    owner_subject_id     BIGINT       NOT NULL COMMENT '归属主体ID',
    root_subject_id      BIGINT       NOT NULL COMMENT '数据隔离根主体ID',
    client_instance_code VARCHAR(128) NOT NULL COMMENT '应用端实例编码',
    client_instance_name VARCHAR(128) NOT NULL COMMENT '应用端实例名称',
    realm_id             BIGINT       DEFAULT NULL COMMENT '身份域ID',
    entry_url            VARCHAR(512) DEFAULT NULL COMMENT '访问入口',
    status               tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    create_time          DATETIME     NOT NULL,
    update_time          DATETIME     NOT NULL,
    UNIQUE KEY uk_instance_client (app_instance_id, client_code),
    UNIQUE KEY uk_client_instance_code (client_instance_code),
    KEY idx_client_instance_app_instance (app_instance_id),
    KEY idx_client_instance_client (app_client_id),
    KEY idx_client_instance_realm (realm_id)
) COMMENT ='应用端实例表';

CREATE TABLE iam_role
(
    id                 BIGINT PRIMARY KEY COMMENT '角色ID',
    app_code           VARCHAR(64)  NOT NULL COMMENT '应用编码',
    app_instance_id    BIGINT       NOT NULL COMMENT '应用实例ID',
    client_code        VARCHAR(128) NOT NULL COMMENT '应用端编码',
    client_instance_id BIGINT       NOT NULL COMMENT '应用端实例ID',
    root_subject_id    BIGINT       NOT NULL COMMENT '数据隔离根主体ID',
    owner_subject_id   BIGINT       NOT NULL COMMENT '归属主体ID',
    role_code          VARCHAR(128) NOT NULL COMMENT '角色编码',
    role_name          VARCHAR(128) NOT NULL COMMENT '角色名称',
    role_type          VARCHAR(32)  NOT NULL COMMENT 'SYSTEM/CUSTOM',
    status             tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    built_in           TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
    description        VARCHAR(512)          DEFAULT NULL COMMENT '描述',
    create_time        DATETIME     NOT NULL,
    update_time        DATETIME     NOT NULL,
    UNIQUE KEY uk_role_client_instance_code (client_instance_id, role_code),
    KEY idx_role_client_instance (client_instance_id),
    KEY idx_role_app_instance (app_instance_id)
) COMMENT ='角色表';

CREATE TABLE iam_role_menu
(
    id                 BIGINT PRIMARY KEY COMMENT '主键ID',
    role_id            BIGINT   NOT NULL COMMENT '角色ID',
    menu_id            BIGINT   NOT NULL COMMENT '应用端菜单ID',
    client_instance_id BIGINT   NOT NULL COMMENT '应用端实例ID',
    create_time        DATETIME NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_menu_role (role_id),
    KEY idx_role_menu_menu (menu_id),
    KEY idx_role_menu_client_instance (client_instance_id)
) COMMENT ='角色菜单表';

CREATE TABLE iam_role_permission
(
    id                 BIGINT PRIMARY KEY COMMENT '主键ID',
    role_id            BIGINT   NOT NULL COMMENT '角色ID',
    permission_id      BIGINT   NOT NULL COMMENT '应用端权限ID',
    client_instance_id BIGINT   NOT NULL COMMENT '应用端实例ID',
    create_time        DATETIME NOT NULL,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_permission_role (role_id),
    KEY idx_role_permission_permission (permission_id),
    KEY idx_role_permission_client_instance (client_instance_id)
) COMMENT ='角色权限表';

CREATE TABLE iam_member_role
(
    id                    BIGINT PRIMARY KEY COMMENT '主键ID',
    member_id             BIGINT     NOT NULL COMMENT '主体成员ID',
    role_id               BIGINT     NOT NULL COMMENT '角色ID',
    app_instance_id       BIGINT     NOT NULL COMMENT '应用实例ID',
    client_instance_id    BIGINT     NOT NULL COMMENT '应用端实例ID',
    granted_by_account_id BIGINT DEFAULT NULL COMMENT '授权人账号ID',
    granted_at            DATETIME   NOT NULL COMMENT '授权时间',
    status                tinyint(1) NOT NULL COMMENT 'ENABLED/DISABLED',
    create_time           DATETIME   NOT NULL,
    update_time           DATETIME   NOT NULL,
    UNIQUE KEY uk_member_role_client_instance (member_id, role_id, client_instance_id),
    KEY idx_member_role_member (member_id),
    KEY idx_member_role_role (role_id),
    KEY idx_member_role_app_instance (app_instance_id),
    KEY idx_member_role_client_instance (client_instance_id)
) COMMENT ='成员角色表';

CREATE TABLE app_client_external_config
(
    id            BIGINT PRIMARY KEY COMMENT '主键ID',
    app_client_id BIGINT       NOT NULL COMMENT '应用客户端ID',
    provider_type VARCHAR(32)  NOT NULL COMMENT 'WECHAT_MINI/ALIPAY_MINI/DOUYIN_MINI/OIDC/WECHAT_WORK',
    provider_code VARCHAR(64)  NOT NULL COMMENT '配置编码',
    provider_name VARCHAR(128) NOT NULL COMMENT '配置名称',
    app_id        VARCHAR(128)  DEFAULT NULL COMMENT '第三方应用ID',
    app_secret    VARCHAR(1024) DEFAULT NULL COMMENT '第三方密钥，加密存储',
    public_key    TEXT          DEFAULT NULL COMMENT '第三方公钥',
    private_key   TEXT          DEFAULT NULL COMMENT '应用私钥，加密存储',
    callback_url  VARCHAR(512)  DEFAULT NULL COMMENT '回调地址',
    config_json   TEXT          DEFAULT NULL COMMENT '扩展配置JSON',
    status        tinyint(1)   NOT NULL COMMENT 'ENABLED/DISABLED',
    create_time   DATETIME     NOT NULL,
    update_time   DATETIME     NOT NULL,
    UNIQUE KEY uk_client_provider (app_client_id, provider_type, provider_code),
    KEY idx_external_client (app_client_id)
) COMMENT ='应用客户端外部平台配置表';


CREATE TABLE role
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    realm_id    BIGINT       NOT NULL COMMENT '身份域ID',
    subject_id  BIGINT       NULL COMMENT '主体ID',
    client_id   BIGINT       NOT NULL COMMENT '客户端ID',

    role_name   VARCHAR(128) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(128) NOT NULL COMMENT '角色编码',

    data_scope  VARCHAR(32)  NOT NULL DEFAULT 'current_subject' COMMENT '数据范围',

    status      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：enabled/disabled',
    remark      VARCHAR(512) NULL COMMENT '备注',

    created_by  BIGINT       NULL COMMENT '创建人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by  BIGINT       NULL COMMENT '更新人',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    UNIQUE KEY uk_role_code (realm_id, subject_id, client_id, role_code),
    KEY idx_realm_id (realm_id),
    KEY idx_subject_id (subject_id),
    KEY idx_client_id (client_id),
    KEY idx_status (status)
) COMMENT ='角色表';


CREATE TABLE role_resource
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    role_id     BIGINT   NOT NULL COMMENT '角色ID',
    resource_id BIGINT   NOT NULL COMMENT '资源ID',
    client_id   BIGINT   NOT NULL COMMENT '客户端ID',

    created_by  BIGINT   NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_role_resource (role_id, resource_id),
    KEY idx_role_id (role_id),
    KEY idx_resource_id (resource_id),
    KEY idx_client_id (client_id)
) COMMENT ='角色资源关系表';

CREATE TABLE account_role
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    account_id BIGINT      NOT NULL COMMENT '账号ID',
    subject_id BIGINT      NULL COMMENT '主体ID',
    client_id  BIGINT      NOT NULL COMMENT '客户端ID',
    role_id    BIGINT      NOT NULL COMMENT '角色ID',

    status     VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',

    created_by BIGINT      NULL COMMENT '创建人',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_account_role (account_id, subject_id, client_id, role_id),
    KEY idx_account_id (account_id),
    KEY idx_subject_id (subject_id),
    KEY idx_client_id (client_id),
    KEY idx_role_id (role_id)
) COMMENT ='账号角色关系表';
