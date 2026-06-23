package com.authsphere.server.realm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 身份域类型详情响应。
 *
 * <p>除基础信息外，额外返回详情页“引用分析”模块所需的数据，
 * 由后端统一计算引用统计，避免前端重复拼装。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmTypeInfoResponse extends RealmTypePageResponse {
    /**
     * 已启用引用数量
     */
    private Integer enabledCount;

    /**
     * 已禁用引用数量
     */
    private Integer disabledCount;

    /**
     * 关联身份域列表
     */
    private List<RealmListResponse> realmList;
}
