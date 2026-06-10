package com.authsphere.server.subject.api;

import com.authsphere.server.subject.dto.SubjectMemberCreateAccountRequest;
import com.authsphere.server.subject.dto.SubjectMemberPageRequest;
import com.authsphere.server.subject.dto.SubjectMemberRequest;
import com.authsphere.server.subject.dto.SubjectMemberResponse;
import com.authsphere.server.subject.service.SubjectMemberService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主体成员管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subject-member")
public class SubjectMemberController {

    private final SubjectMemberService subjectMemberService;

    @PostMapping("/page")
    public Page<SubjectMemberResponse> page(@Validated @RequestBody SubjectMemberPageRequest request) {
        return subjectMemberService.page(request);
    }

    @PostMapping("/subject/{subjectId}/existing-account")
    public Boolean addExisting(@PathVariable Long subjectId, @Validated @RequestBody SubjectMemberRequest request) {
        return subjectMemberService.addExisting(subjectId, request);
    }

    @PostMapping("/subject/{subjectId}/new-account")
    public Boolean createAccountAndAdd(@PathVariable Long subjectId,
                                       @Validated @RequestBody SubjectMemberCreateAccountRequest request) {
        return subjectMemberService.createAccountAndAdd(subjectId, request);
    }

    @PutMapping("/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody SubjectMemberRequest request) {
        return subjectMemberService.edit(id, request);
    }

    @PutMapping("/{id}/enable")
    public Boolean enable(@PathVariable Long id) {
        return subjectMemberService.enable(id);
    }

    @PutMapping("/{id}/disable")
    public Boolean disable(@PathVariable Long id) {
        return subjectMemberService.disable(id);
    }

    @DeleteMapping("/{id}")
    public Boolean remove(@PathVariable Long id) {
        return subjectMemberService.remove(id);
    }
}
