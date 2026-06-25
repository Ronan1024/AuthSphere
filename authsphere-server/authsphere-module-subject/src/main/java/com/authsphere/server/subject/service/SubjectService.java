package com.authsphere.server.subject.service;

import com.authsphere.server.subject.dto.SubjectDetailResponse;
import com.authsphere.server.subject.dto.SubjectPageRequest;
import com.authsphere.server.subject.dto.SubjectPageResponse;
import com.authsphere.server.subject.dto.SubjectRequest;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.model.Subject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【subject(主体表)】的数据库操作Service
* @createDate 2026-06-01 14:19:02
*/
public interface SubjectService extends IService<Subject> {

    /**
     * 主体分页查询。
     */
    Page<SubjectPageResponse> page(SubjectPageRequest request);

    /**
     * 启用主体列表。
     */
    List<SubjectResponse> listAll();

    /**
     * 主体详情。
     */
    SubjectDetailResponse detail(Long id);

    /**
     * 新增主体。
     */
    Boolean create(SubjectRequest request);

    /**
     * 修改主体。
     */
    Boolean edit(Long id, SubjectRequest request);

    /**
     * 修改主体状态。
     */
    Boolean editStatus(Long id);

    /**
     * 删除主体。
     */
    Boolean delete(Long id);

    /**
     * 查询指定父主体的直接子主体列表。
     */
    List<SubjectResponse> listChildren(Long parentId);
}
