package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.RealmListResponse;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【realm(身份域信息)】的数据库操作Mapper
 * @createDate 2026-05-26 14:40:36
 * @Entity com.authsphere.server.realm.model.Realm
 */
@Mapper
public interface RealmMapper extends BaseMapper<Realm> {

    Page<RealmPageResponse> page(@Param("page") IPage<RealmPageResponse> page, @Param("realmPageRequest") RealmPageRequest realmPageRequest);

    /**
     * 查询全部启用身份域，用于下拉选择。
     */
    List<RealmListResponse> listAll();

    /**
     * 统计引用该身份域的账号数量。
     */
    int countAccountReferences(@Param("realmId") Long realmId);

    /**
     * 统计引用该身份域的客户端（应用）数量。
     */
    int countClientReferences(@Param("realmId") Long realmId);
}





