package com.authsphere.server.subject.service.impl;

import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.api.realm.RealmApi;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.convert.SubjectConvert;
import com.authsphere.server.subject.domain.SubjectDomain;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.dto.SubjectPageRequest;
import com.authsphere.server.subject.dto.SubjectPageResponse;
import com.authsphere.server.subject.dto.SubjectRequest;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectMapper;
import com.authsphere.server.subject.model.Subject;
import com.authsphere.server.subject.service.SubjectService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author longjiangran
 * @description 针对表【subject(主体表)】的数据库操作Service实现
 * @createDate 2026-06-01 14:19:02
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final RealmApi realmApi;
    private final SubjectDomain subjectDomain;
    private final SubjectTypeDomain subjectTypeDomain;

    @Override
    public Page<SubjectPageResponse> page(SubjectPageRequest request) {
        IPage<SubjectPageResponse> page = new Page<>(request.getPage(), request.getSize());
        Page<SubjectPageResponse> result = subjectMapper.page(page, request);
        List<SubjectPageResponse> records = result.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> realmIdList = records.stream().map(SubjectPageResponse::getRealmId).toList();
            List<RealmInfoResponse> realmInfoList = realmApi.list(realmIdList);
            Map<Long, RealmInfoResponse> realmInfoMap = realmInfoList.stream().collect(Collectors.toMap(RealmInfoResponse::getId, e -> e));

            List<Long> parentIdList = records.stream().map(SubjectPageResponse::getParentId).distinct().toList();
            Map<Long, Subject> parentSubjectMap = new HashMap<>();
            if (CollectionUtils.isEmpty(parentIdList)) {
                List<Subject> parentSubjects = subjectDomain.findByIds(parentIdList);
                parentSubjectMap.putAll(parentSubjects.stream().collect(Collectors.toMap(Subject::getId, e -> e)));

            }
            records.forEach(record -> {
                RealmInfoResponse realmInfo = realmInfoMap.get(record.getRealmId());
                record.setRealmName(realmInfo.getName());
                record.setRealmCode(realmInfo.getCode());
                Subject subject = parentSubjectMap.getOrDefault(record.getParentId(), new Subject());
                record.setParentName(subject.getName());
                // TODO 主体关联数据
                record.setMemberCount(0);
                record.setClientCount(0);
                record.setClientCount(0);
            });


        }


        return result;
    }

    @Override
    public List<SubjectResponse> listAll() {
        return subjectMapper.listAll();
    }

    @Override
    public SubjectResponse detail(Long id) {
        subjectDomain.findById(id);
        return subjectMapper.detail(id);
    }

    @Override
    public Boolean create(SubjectRequest request) {
        validateSubject(null, request);
        subjectDomain.checkCodeExists(null, request.getCode());
        Subject subject = SubjectConvert.INSTANCE.model(request);
        subject.setStatus(StatusEnum.NORMAL.getCode());
        subjectMapper.insert(subject);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, SubjectRequest request) {
        Subject subject = subjectDomain.findById(id);
        validateSubject(id, request);
        subjectDomain.checkCodeExists(id, request.getCode());
        SubjectConvert.INSTANCE.copyToModel(request, subject);
        subjectMapper.updateById(subject);
        return Boolean.TRUE;
    }

    @Override
    public Boolean editStatus(Long id) {
        Subject subject = subjectDomain.findById(id);
        subject.setStatus(StatusEnum.reverseStatus(subject.getStatus()));
        subjectMapper.updateById(subject);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(Long id) {
        Subject subject = subjectDomain.findById(id);
        // 内置主体不允许删除，避免误删平台基础数据。
        if (Boolean.TRUE.equals(subject.getBuiltIn())) {
            throw new BizException(SubjectErrorCode.SUBJECT_BUILT_IN_DELETE_DENIED);
        }
        subjectMapper.deleteById(id);
        return Boolean.TRUE;
    }

    @Override
    public List<SubjectResponse> listChildren(Long parentId) {
        subjectDomain.findById(parentId);
        return subjectMapper.listChildren(parentId);
    }

    private void validateSubject(Long currentId, SubjectRequest request) {
        // 创建和编辑共用同一套引用校验，避免两处规则漂移。
        subjectTypeDomain.findById(request.getSubjectTypeId());
        checkRealmExists(request.getRealmId());
        if (request.getParentSubjectId() != null) {
            if (Objects.equals(currentId, request.getParentSubjectId())) {
                throw new BizException(SubjectErrorCode.SUBJECT_PARENT_SELF_DENIED);
            }
            subjectDomain.findById(request.getParentSubjectId());
        }
        if (request.getRootSubjectId() != null) {
            if (Objects.equals(currentId, request.getRootSubjectId())) {
                throw new BizException(SubjectErrorCode.SUBJECT_ROOT_SELF_DENIED);
            }
            subjectDomain.findById(request.getRootSubjectId());
        }
    }

    private void checkRealmExists(Long realmId) {
        Long count = subjectMapper.countRealmById(realmId);
        if (count == null || count == 0) {
            throw new BizException(SubjectErrorCode.SUBJECT_REALM_DATA_ERROR);
        }
    }
}
