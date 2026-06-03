package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.MemberRoleAssignRequest;
import com.authsphere.server.app.model.MemberRole;
import com.authsphere.server.app.service.MemberRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成员角色授权接口。
 */
@RestController
@RequiredArgsConstructor
public class MemberRoleController {

    private final MemberRoleService memberRoleService;

    /**
     * 查询成员在指定应用端实例下的角色授权。
     */
    @GetMapping("/api/app-client-instances/{clientInstanceId}/members/{memberId}/roles")
    public List<MemberRole> listMemberRoles(@PathVariable Long clientInstanceId,
                                            @PathVariable Long memberId) {
        return memberRoleService.listMemberRoles(clientInstanceId, memberId);
    }

    /**
     * 覆盖保存成员在指定应用端实例下的角色授权。
     */
    @PutMapping("/api/app-client-instances/{clientInstanceId}/members/{memberId}/roles")
    public Boolean assignMemberRoles(@PathVariable Long clientInstanceId,
                                     @PathVariable Long memberId,
                                     @Validated @RequestBody MemberRoleAssignRequest request) {
        return memberRoleService.assignMemberRoles(clientInstanceId, memberId, request);
    }

    /**
     * 查询成员拥有的全部应用端实例角色授权。
     */
    @GetMapping("/api/subject-members/{memberId}/client-instance-roles")
    public List<MemberRole> listByMember(@PathVariable Long memberId) {
        return memberRoleService.listByMember(memberId);
    }
}
