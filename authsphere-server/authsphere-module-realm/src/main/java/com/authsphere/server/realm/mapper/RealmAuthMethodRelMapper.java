package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.model.RealmAuthMethodRel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 身份域与认证方式关联 Mapper。
 *
 * <p>这里不加复杂自定义 SQL，当前只依赖 MyBatis-Plus 的基础 CRUD，
 * 由 Service 层完成批量替换、聚合和业务校验。</p>
 */
@Mapper
public interface RealmAuthMethodRelMapper extends BaseMapper<RealmAuthMethodRel> {
}
