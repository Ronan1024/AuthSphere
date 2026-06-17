package com.authsphere.server.subject.service;

import com.authsphere.server.subject.dto.SubjectTypeInfoResponse;
import com.authsphere.server.subject.dto.SubjectTypePageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRequest;
import com.authsphere.server.subject.dto.SubjectTypeResponse;
import com.authsphere.server.subject.model.SubjectType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【subject_type(主体类型表)】的数据库操作Service
 * @createDate 2026-06-01 13:36:39
 */
public interface SubjectTypeService extends IService<SubjectType> {

    /**
     * 主体类型分页
     */
    Page<SubjectTypeResponse> page(SubjectTypePageRequest request);

    /**
     * 获取所有的主体类型
     */
    List<SubjectTypeResponse> listAll();

    /**
     * 获取主体类型详情。
     */
    SubjectTypeInfoResponse detail(Long id);

    /**
     * 创建主体类型
     *
     */
    Boolean create(SubjectTypeRequest request);

    /**
     * 修改主体类型
     *
     * @param id 主体类型ID
     */
    Boolean edit(Long id, SubjectTypeRequest request);

    /**
     * 修改主体类型状态
     */
    Boolean editStatus(Long id);

    /**
     * 删除主体类型
     */
    Boolean delete(Long id);

}
