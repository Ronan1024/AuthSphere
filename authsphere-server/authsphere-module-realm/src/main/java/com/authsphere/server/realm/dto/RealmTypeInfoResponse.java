package com.authsphere.server.realm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 类型分类响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmTypeInfoResponse extends RealmTypePageResponse {
    /**
     * 已禁用引用数量
     */
    private Integer disabledCount;

    /**
     * 身份域列表
     */
    private List<RealmListResponse> realmList;
}
