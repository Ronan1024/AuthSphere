package com.authsphere.server.subject.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.convert.SubjectTypeConvert;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.dto.SubjectTypeInfoResponse;
import com.authsphere.server.subject.dto.SubjectTypePageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRequest;
import com.authsphere.server.subject.dto.SubjectTypeResponse;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectTypeMapper;
import com.authsphere.server.subject.model.SubjectType;
import com.authsphere.server.subject.service.SubjectTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【subject_type(主体类型表)】的数据库操作Service实现
 * @createDate 2026-06-01 13:36:39
 */
@Service
@RequiredArgsConstructor
public class SubjectTypeServiceImpl extends ServiceImpl<SubjectTypeMapper, SubjectType>
        implements SubjectTypeService {

    private final SubjectTypeMapper subjectTypeMapper;
    private final SubjectTypeDomain subjectTypeDomain;


    /**
     * 分页查询主体类型
     */
    @Override
    public Page<SubjectTypeResponse> page(SubjectTypePageRequest request) {
        Page<SubjectTypeResponse> page = new Page<>(request.getPage(), request.getSize());
        return subjectTypeMapper.page(page, request);
    }

    /**
     * 查询所有主体类型
     *
     */
    @Override
    public List<SubjectTypeResponse> listAll() {
        return subjectTypeMapper.listAll();
    }

    /**
     * 主体分类详情
     */
    @Override
    public SubjectTypeInfoResponse detail(Long id) {
        SubjectType subjectType = subjectTypeDomain.findById(id);
        return SubjectTypeConvert.INSTANCE.toSubjectTypeInfoResponse(subjectType);
    }

    /**
     * 创建主体类型
     *
     * @param request 主体类型请求参数
     * @return 是否创建成功
     */
    @Override
    public Boolean create(SubjectTypeRequest request) {
        subjectTypeDomain.checkCodeExists(null, request.getCode());
        SubjectType subjectType = SubjectTypeConvert.INSTANCE.model(request);
        if (request.getStatus() != null) {
            subjectType.setStatus(request.getStatus());
        } else {
            subjectType.setStatus(StatusEnum.NORMAL.getCode());
        }
        return subjectTypeMapper.insert(subjectType) > 0;
    }

    @Override
    public Boolean edit(Long id, SubjectTypeRequest request) {
        SubjectType subjectType = subjectTypeDomain.findById(id);
        subjectTypeDomain.checkCodeExists(id, request.getCode());
        SubjectTypeConvert.INSTANCE.copyToModel(request, subjectType);
        if (request.getStatus() != null) {
            subjectType.setStatus(request.getStatus());
        }
        subjectTypeMapper.updateById(subjectType);
        return Boolean.TRUE;
    }

    @Override
    public Boolean editStatus(Long id) {
        SubjectType subjectType = subjectTypeDomain.findById(id);
        subjectType.setStatus(StatusEnum.reverseStatus(subjectType.getStatus()));
        subjectTypeMapper.updateById(subjectType);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(Long id) {
        SubjectType subjectType = subjectTypeDomain.findById(id);
        if (Boolean.TRUE.equals(subjectType.getBuiltIn())) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_BUILT_IN_DELETE_DENIED);
        }
        subjectTypeMapper.deleteById(id);
        return Boolean.TRUE;
    }

    /**
     * 获取主体类型分类列表
     */
    @Override
    public List<String> categoryList() {
        List<SubjectType> list = this.lambdaQuery().select(SubjectType::getCategory).list();
        return list.stream()
                .map(SubjectType::getCategory)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .toList();
    }


}
