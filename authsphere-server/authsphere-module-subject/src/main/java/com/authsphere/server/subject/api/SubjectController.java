package com.authsphere.server.subject.api;

import com.authsphere.server.subject.dto.SubjectDetailResponse;
import com.authsphere.server.subject.dto.SubjectPageRequest;
import com.authsphere.server.subject.dto.SubjectPageResponse;
import com.authsphere.server.subject.dto.SubjectRequest;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.service.SubjectService;
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
 * 主体管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subject")
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * 主体分页查询。
     */
    @PostMapping("/page")
    public Page<SubjectPageResponse> page(@Validated @RequestBody SubjectPageRequest request) {
        return subjectService.page(request);
    }

    /**
     * 主体列表。
     */
    @GetMapping("/list")
    public List<SubjectResponse> list() {
        return subjectService.listAll();
    }

    /**
     * 主体详情。
     */
    @GetMapping("/{id}")
    public SubjectDetailResponse detail(@PathVariable Long id) {
        return subjectService.detail(id);
    }

    /**
     * 新增主体。
     */
    @PostMapping
    public Boolean create(@Validated @RequestBody SubjectRequest request) {
        return subjectService.create(request);
    }

    /**
     * 修改主体。
     */
    @PutMapping("/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody SubjectRequest request) {
        return subjectService.edit(id, request);
    }

    /**
     * 修改主体状态。
     */
    @PutMapping("/status/{id}")
    public Boolean editStatus(@PathVariable Long id) {
        return subjectService.editStatus(id);
    }

    /**
     * 删除主体，系统内置主体不允许删除。
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return subjectService.delete(id);
    }

    /**
     * 查询指定父主体的直接子主体列表。
     */
    @GetMapping("/{id}/children")
    public List<SubjectResponse> children(@PathVariable Long id) {
        return subjectService.listChildren(id);
    }
}
