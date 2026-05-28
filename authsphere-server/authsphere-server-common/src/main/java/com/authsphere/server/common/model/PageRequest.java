package com.authsphere.server.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Data
public class PageRequest {

    @Min(value = 1)
    private Integer page;

    @Max(value = 100)
    private Integer size;
}
