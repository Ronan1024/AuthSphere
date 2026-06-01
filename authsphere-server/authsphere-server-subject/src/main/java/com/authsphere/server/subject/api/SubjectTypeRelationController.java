package com.authsphere.server.subject.api;

import com.authsphere.server.subject.dto.SubjectTypeRelationPageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationResponse;
import com.authsphere.server.subject.service.SubjectTypeRelationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 主体类型关系规则接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subject/type/relation")
public class SubjectTypeRelationController {

    private final SubjectTypeRelationService subjectTypeRelationService;

    /**
     * 主体类型关系规则分页查询。
     */
    @PostMapping("/page")
    public Page<SubjectTypeRelationResponse> page(@Validated @RequestBody SubjectTypeRelationPageRequest request) {
        return subjectTypeRelationService.page(request);
    }

    /**
     * 主体类型关系规则列表。
     */
    @GetMapping("/list")
    public List<SubjectTypeRelationResponse> list() {
        return subjectTypeRelationService.listAll();
    }

    /**
     * 新增主体类型关系规则。
     */
    @PostMapping
    public Boolean create(@Validated @RequestBody SubjectTypeRelationRequest request) {
        return subjectTypeRelationService.create(request);
    }

    /**
     * 修改主体类型关系规则。
     */
    @PutMapping("/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody SubjectTypeRelationRequest request) {
        return subjectTypeRelationService.edit(id, request);
    }

    /**
     * 修改主体类型关系规则状态。
     */
    @PutMapping("/status/{id}")
    public Boolean editStatus(@PathVariable Long id) {
        return subjectTypeRelationService.editStatus(id);
    }

    /**
     * 删除主体类型关系规则。
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return subjectTypeRelationService.delete(id);
    }
}
