package com.authsphere.server.app.mapper;

import com.authsphere.server.app.model.AppClientMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author longjiangran
* @description 针对表【app_menu(应用菜单资源表)】的数据库操作Mapper
* @createDate 2026-06-03 11:07:48
* @Entity com.authsphere.server.app.model.AppMenu
*/
@Mapper
public interface AppMenuMapper extends BaseMapper<AppClientMenu> {

}




