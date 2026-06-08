package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.LoginPageInfoResponse;
import com.authsphere.server.realm.dto.LoginPageRequest;
import com.authsphere.server.realm.dto.LoginPagePreviewResponse;
import com.authsphere.server.realm.dto.LoginPageResponse;
import com.authsphere.server.realm.model.LoginPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 登录页对象转换。
 */
@Mapper
public interface LoginPageConvert {

    /**
     * MapStruct转换器实例。
     */
    LoginPageConvert INSTANCE = Mappers.getMapper(LoginPageConvert.class);

    /**
     * 将保存请求转换为登录页实体。
     *
     * @param request 登录页保存参数
     * @return 登录页实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "systemBuiltin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "authMethods", expression = "java(joinAuthMethods(request.getAuthMethods()))")
    LoginPage model(LoginPageRequest request);

    /**
     * 将保存请求覆盖到已有登录页实体，保留系统维护字段。
     *
     * @param request 登录页保存参数
     * @param loginPage 已有登录页实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "systemBuiltin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "authMethods", expression = "java(joinAuthMethods(request.getAuthMethods()))")
    void copyToModel(LoginPageRequest request, @MappingTarget LoginPage loginPage);

    /**
     * 将登录页实体转换为列表响应。
     *
     * @param loginPage 登录页实体
     * @return 登录页列表响应
     */
    @Mapping(target = "applicableRealmTypeName", ignore = true)
    @Mapping(target = "authMethod", ignore = true)
//    @Mapping(target = "authMethodsText", source = "authMethods")
    @Mapping(target = "referenceCount", ignore = true)
    LoginPageResponse response(LoginPage loginPage);

    /**
     * 将登录页实体转换为详情响应，关联信息由领域服务补充。
     *
     * @param loginPage 登录页实体
     * @return 登录页详情响应
     */
    @Mapping(target = "applicableRealmTypeName", ignore = true)
    @Mapping(target = "authMethod", ignore = true)
//    @Mapping(target = "authMethodsText", source = "authMethods")
    @Mapping(target = "realmReferenceCount", ignore = true)
    @Mapping(target = "clientReferenceCount", ignore = true)
    @Mapping(target = "referenceCount", ignore = true)
    LoginPageInfoResponse infoResponse(LoginPage loginPage);

    /**
     * 将登录页实体转换为预览响应。
     *
     * @param loginPage 登录页实体
     * @return 登录页预览响应
     */
    @Mapping(target = "authMethods", expression = "java(splitAuthMethods(loginPage.getAuthMethods()))")
    LoginPagePreviewResponse previewResponse(LoginPage loginPage);

    /**
     * 将认证方式编码集合转换为数据库存储文本。
     *
     * @param authMethods 认证方式编码集合
     * @return 逗号分隔的认证方式编码
     */
    default String joinAuthMethods(List<String> authMethods) {
        return authMethods == null ? null : String.join(",", authMethods);
    }

    /**
     * 将数据库存储文本转换为认证方式编码集合。
     *
     * @param authMethods 逗号分隔的认证方式编码
     * @return 认证方式编码集合
     */
    default List<String> splitAuthMethods(String authMethods) {
        if (authMethods == null || authMethods.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(authMethods.split(","));
    }

    @Mapping(target = "referenceCount", ignore = true)
    @Mapping(target = "realmReferenceCount", ignore = true)
    @Mapping(target = "clientReferenceCount", ignore = true)
    @Mapping(target = "authMethod", ignore = true)
    @Mapping(target = "applicableRealmTypeName", ignore = true)
    LoginPageInfoResponse toLoginPageInfoResponse(LoginPage loginPage);
}
