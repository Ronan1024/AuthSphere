package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.RealmTypePageRequest;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.model.RealmType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【type_category(类型项)】的数据库操作Mapper
 * @createDate 2026-05-28 06:04:57
 * @Entity com.authsphere.server.realm.model.RealmType
 */
@Mapper
public interface RealmTypeMapper extends BaseMapper<RealmType> {

    Page<RealmTypePageResponse> page(@Param("page") IPage<RealmTypePageResponse> page,
                                        @Param("request") RealmTypePageRequest request);

    List<RealmTypePageResponse> listAll();

}



