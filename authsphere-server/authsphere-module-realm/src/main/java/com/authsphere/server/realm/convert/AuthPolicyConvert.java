package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.AuthPolicyInfoResponse;
import com.authsphere.server.realm.dto.AuthPolicyRequest;
import com.authsphere.server.realm.model.AuthPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 认证策略对象转换。
 */
@Mapper
public interface AuthPolicyConvert {

    /**
     * MapStruct生成的认证策略转换器实例。
     */
    AuthPolicyConvert INSTANCE = Mappers.getMapper(AuthPolicyConvert.class);

    /**
     * 将保存请求转换为认证策略实体。
     *
     * @param request 已完成领域校验和集合规范化的保存请求
     * @return 待持久化的认证策略实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "systemBuiltin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "authMethods", expression = "java(join(request.getAuthMethods()))")
    @Mapping(target = "mfaTriggers", expression = "java(join(request.getMfaTriggers()))")
    @Mapping(target = "mfaMethods", expression = "java(join(request.getMfaMethods()))")
    AuthPolicy model(AuthPolicyRequest request);

    /**
     * 将保存请求覆盖到已有认证策略实体。
     *
     * @param request 已完成领域校验和集合规范化的保存请求
     * @param policy 待更新的认证策略实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "systemBuiltin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "authMethods", expression = "java(join(request.getAuthMethods()))")
    @Mapping(target = "mfaTriggers", expression = "java(join(request.getMfaTriggers()))")
    @Mapping(target = "mfaMethods", expression = "java(join(request.getMfaMethods()))")
    void copyToModel(AuthPolicyRequest request, @MappingTarget AuthPolicy policy);

    /**
     * 将认证策略实体转换为详情响应，关联信息由领域服务补充。
     *
     * @param policy 认证策略实体
     * @return 尚未补充引用信息和适用身份域名称的详情响应
     */
    @Mapping(target = "applicableRealmName", ignore = true)
    @Mapping(target = "authMethods", expression = "java(split(policy.getAuthMethods()))")
    @Mapping(target = "authMethodsText", source = "authMethods")
    @Mapping(target = "mfaTriggers", expression = "java(split(policy.getMfaTriggers()))")
    @Mapping(target = "mfaMethods", expression = "java(split(policy.getMfaMethods()))")
    @Mapping(target = "referenceCount", ignore = true)
    @Mapping(target = "realmReferenceCount", ignore = true)
    @Mapping(target = "clientReferenceCount", ignore = true)
    @Mapping(target = "references", ignore = true)
    AuthPolicyInfoResponse infoResponse(AuthPolicy policy);

    /**
     * 将字符串集合转换为逗号分隔文本。
     *
     * @param values 待持久化的字符串集合
     * @return 逗号分隔文本；集合为null时返回null
     */
    default String join(List<String> values) {
        return values == null ? null : String.join(",", values);
    }

    /**
     * 将逗号分隔文本转换为字符串集合。
     *
     * @param value 数据库存储的逗号分隔文本
     * @return 字符串集合；文本为空时返回空集合
     */
    default List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }
}
