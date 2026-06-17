package com.authsphere.server.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 当前账号权限码响应。
 */
@Data
@AllArgsConstructor
public class CurrentPermissionResponse {

    /**
     * 当前账号在指定主体和客户端下最终可用的权限编码集合。
     */
    private List<String> permissions;
}
