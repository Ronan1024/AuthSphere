package com.authsphere.server.api.model.dto.subject;

import lombok.Data;

/**
 * 账号关联主体数量
 *
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Data
public class AccountSubjectCountResponse {

    /**
     * 账号id
     */
    private Long accountId;

    /**
     * 关联主体数量
     */
    private Integer count;


}
