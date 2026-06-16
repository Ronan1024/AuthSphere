-- 客户端登录接入方式、客户端级 OSS 配置引用、登录入口模板扩展字段。

alter table app_client
    add column login_mode varchar(32) not null default 'IAM_HOSTED' comment '登录接入方式：IAM_HOSTED/EXTERNAL_REDIRECT/API_ONLY/SERVICE' after default_entry_url,
    add column external_login_url varchar(512) null comment '客户自有登录页地址' after login_mode,
    add column login_callback_url varchar(512) null comment '登录回调地址' after external_login_url,
    add column oss_config_id bigint null comment '客户端绑定的OSS外部配置ID' after auth_policy_id,
    add key idx_client_login_mode (login_mode),
    add key idx_client_oss_config (oss_config_id);

alter table login_page
    add column logo_object_key varchar(512) null comment 'Logo OSS对象标识' after background_url,
    add column background_object_key varchar(512) null comment '背景图 OSS对象标识' after logo_object_key,
    add column layout_mode varchar(32) not null default 'CENTER_CARD' comment '页面布局模式' after background_object_key,
    add column theme_config_json text null comment '主题配置JSON' after layout_mode,
    add column component_config_json text null comment '组件展示配置JSON' after theme_config_json,
    add column micro_frontend_url varchar(512) null comment '微前端远程入口地址' after component_config_json;

update app_client
set login_mode = 'SERVICE',
    login_page_id = null
where client_type in (5, 6);
