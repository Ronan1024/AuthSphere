package com.authsphere.server.app.mapper;

import com.authsphere.server.app.model.AppClient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author longjiangran
* @description 针对表【app_client(应用客户端表)】的数据库操作Mapper
* @createDate 2026-06-03 11:07:48
* @Entity com.authsphere.server.app.model.AppClient
*/
@Mapper
public interface AppClientMapper extends BaseMapper<AppClient> {

}




