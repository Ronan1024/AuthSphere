package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author longjiangran
 * @description 针对表【realm(身份域信息)】的数据库操作Mapper
 * @createDate 2026-05-26 14:40:36
 * @Entity com.authsphere.server.realm.model.Realm
 */
@Mapper
public interface RealmMapper extends BaseMapper<Realm> {

    Page<RealmPageResponse> page(@Param("page") IPage<RealmPageResponse> page, @Param("realmPageRequest") RealmPageRequest realmPageRequest);
}




