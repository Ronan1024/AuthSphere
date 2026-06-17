package com.authsphere.server.subject.api;

import com.authsphere.server.subject.dto.SubjectTypeInfoResponse;
import com.authsphere.server.subject.dto.SubjectTypePageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRequest;
import com.authsphere.server.subject.dto.SubjectTypeResponse;
import com.authsphere.server.subject.service.SubjectTypeService;
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
 * 主体类型管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subject/type")
public class SubjectTypeController {

    private final SubjectTypeService subjectTypeService;

    /**
     * 主体类型分页查询。
     */
    @PostMapping("/page")
    public Page<SubjectTypeResponse> page(@Validated @RequestBody SubjectTypePageRequest request) {
        return subjectTypeService.page(request);
    }

    /**
     * 主体类型列表。
     */
    @GetMapping("/list")
    public List<SubjectTypeResponse> list() {
        return subjectTypeService.listAll();
    }

    /**
     * 主体类型详情。
     */
    @GetMapping("/{id}")
    public SubjectTypeInfoResponse detail(@PathVariable Long id) {
        return subjectTypeService.detail(id);
    }

    /**
     * 新增主体类型。
     */
    @PostMapping
    public Boolean create(@Validated @RequestBody SubjectTypeRequest request) {
        return subjectTypeService.create(request);
    }

    /**
     * 修改主体类型。
     */
    @PutMapping("/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody SubjectTypeRequest request) {
        return subjectTypeService.edit(id, request);
    }

    /**
     * 修改主体类型状态。
     */
    @PutMapping("/status/{id}")
    public Boolean editStatus(@PathVariable Long id) {
        return subjectTypeService.editStatus(id);
    }

    /**
     * 删除主体类型，系统内置类型不允许删除。
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return subjectTypeService.delete(id);
    }
}
