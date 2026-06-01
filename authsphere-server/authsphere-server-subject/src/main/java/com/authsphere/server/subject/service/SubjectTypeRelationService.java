package com.authsphere.server.subject.service;

import com.authsphere.server.subject.dto.SubjectTypeRelationPageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationResponse;
import com.authsphere.server.subject.model.SubjectTypeRelation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【subject_type_relation(主体类型关系规则表)】的数据库操作Service
 * @createDate 2026-06-01 14:15:22
 */
public interface SubjectTypeRelationService extends IService<SubjectTypeRelation> {

    /**
     * 主体类型关系规则分页列表
     */
    Page<SubjectTypeRelationResponse> page(SubjectTypeRelationPageRequest request);

    /**
     * 获取所有主体类型关系规则列表
     *
     */
    List<SubjectTypeRelationResponse> listAll();

    /**
     * 创建主体类型关系规则
     */
    Boolean create(SubjectTypeRelationRequest request);

    /**
     * 编辑主体类型关系规则
     *
     * @param id 主体类型id
     */
    Boolean edit(Long id, SubjectTypeRelationRequest request);

    /**
     * 修改主体类型关系规则
     */
    Boolean editStatus(Long id);

    /**
     * 删除主体类型关系规则
     */
    Boolean delete(Long id);

}
