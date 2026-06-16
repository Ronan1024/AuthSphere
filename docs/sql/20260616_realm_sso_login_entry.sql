-- 身份域不再绑定登录页，登录入口由客户端配置承接。
-- 执行前请确认当前环境的 realm 表仍包含 login_url / login_page_id 字段。

alter table realm
    drop column login_url,
    drop column login_page_id,
    add column sso_session_timeout int null comment 'SSO会话有效期，单位小时' after sso_enabled,
    add column sso_idle_timeout int null comment 'SSO空闲超时，单位分钟' after sso_session_timeout,
    add column sso_single_logout varchar(32) null comment '单点退出策略' after sso_idle_timeout,
    add column existing_session_handler varchar(32) null comment '已存在会话处理方式' after sso_single_logout,
    add column no_client_id_handler varchar(32) null comment '无client_id时的处理方式' after existing_session_handler;
