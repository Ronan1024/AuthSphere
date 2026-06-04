package com.authsphere.server.app.dto;

import com.authsphere.server.app.model.App;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInfoResponse extends App {

    /**
     * 应用实例数量
     */
    private Integer appInstanceSize;




    /**
     * 客户端数量
     */
    private Integer clientSize;

}
