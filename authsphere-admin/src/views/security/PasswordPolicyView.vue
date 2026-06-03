<script setup lang="ts">
import {
  Edit,
  Plus,
  Refresh,
  Search,
  SwitchButton,
  View,
} from '@element-plus/icons-vue'
import {
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from 'element-plus'
import { computed, reactive, ref } from 'vue'

import {
  passwordPolicyApi,
  type PageResult,
  type PasswordPolicyPayload,
  type PasswordPolicyRecord,
} from '@/api/passwordPolicy'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const loading = ref(false)
const usingMock = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const formRef = ref<FormInstance>()
const detail = ref<PasswordPolicyRecord>()
const saveLoading = ref(false)
const formError = ref('')

const query = reactive({
  page: 1,
  size: 10,
  code: '',
  name: '',
  status: undefined as number | undefined,
})

const tableData = ref<PasswordPolicyRecord[]>([])
const total = ref(0)

const mockStore = ref<PasswordPolicyRecord[]>([])

const getErrorMessage = (error: unknown, fallback = '操作失败') => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === 'string' && message) {
      return message
    }
  }
  return fallback
}

const showFormError = (message: string) => {
  formError.value = message
  showErrorMessage(message)
}

const defaultForm = (): PasswordPolicyPayload => ({
  code: '',
  name: '',
  minLength: 8,
  maxLength: 32,
  requireUppercase: true,
  requireLowercase: true,
  requireDigit: true,
  requireSpecialChar: false,
  disallowUsername: true,
  historyCount: 0,
  expireDays: 0,
  retryLimit: 5,
  lockMinutes: 30,
  description: '',
})

const form = reactive<PasswordPolicyPayload>(defaultForm())

const rules: FormRules<PasswordPolicyPayload> = {
  code: [
    { required: true, message: '请输入策略编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线和下划线',
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  minLength: [{ required: true, message: '请输入最小长度', trigger: 'change' }],
  maxLength: [{ required: true, message: '请输入最大长度', trigger: 'change' }],
}

const complexityText = (row: PasswordPolicyRecord) => {
  const rules = [
    row.requireUppercase,
    row.requireLowercase,
    row.requireDigit,
    row.requireSpecialChar,
  ].filter(Boolean).length
  if (row.minLength >= 12 && rules >= 4) {
    return '高'
  }
  if (row.minLength >= 8 && rules >= 3) {
    return '中'
  }
  return '低'
}

const expireEnabled = computed({
  get: () => form.expireDays > 0,
  set: (enabled: boolean) => {
    form.expireDays = enabled ? 90 : 0
  },
})

const lockEnabled = computed({
  get: () => form.retryLimit > 0,
  set: (enabled: boolean) => {
    form.retryLimit = enabled ? 5 : 0
    form.lockMinutes = enabled ? 30 : 0
  },
})

const normalizePage = (result: PageResult<PasswordPolicyRecord>) => {
  tableData.value = result.records ?? []
  total.value = Number(result.total ?? 0)
}

const loadMockPage = () => {
  const filtered = mockStore.value.filter((item) => {
    const matchCode = !query.code || item.code.includes(query.code)
    const matchName = !query.name || item.name.includes(query.name)
    const matchStatus = !query.status || item.status === query.status
    return matchCode && matchName && matchStatus
  })
  const start = (query.page - 1) * query.size
  tableData.value = filtered.slice(start, start + query.size)
  total.value = filtered.length
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await passwordPolicyApi.page({
      page: query.page,
      size: query.size,
      code: query.code || undefined,
      name: query.name || undefined,
      status: query.status,
    })
    usingMock.value = false
    normalizePage(result)
  } catch (error) {
    usingMock.value = true
    loadMockPage()
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.page = 1
  query.code = ''
  query.name = ''
  query.status = undefined
  fetchData()
}

const assignForm = (value: PasswordPolicyPayload) => {
  Object.assign(form, value)
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = undefined
  formError.value = ''
  assignForm(defaultForm())
  dialogVisible.value = true
}

const openEdit = (row: PasswordPolicyRecord) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  formError.value = ''
  assignForm({
    code: row.code,
    name: row.name,
    minLength: row.minLength,
    maxLength: row.maxLength,
    requireUppercase: row.requireUppercase,
    requireLowercase: row.requireLowercase,
    requireDigit: row.requireDigit,
    requireSpecialChar: row.requireSpecialChar,
    disallowUsername: row.disallowUsername,
    historyCount: 0,
    expireDays: row.expireDays,
    retryLimit: row.retryLimit,
    lockMinutes: row.lockMinutes,
    description: row.description,
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  formError.value = ''
  if (!formRef.value) {
    showFormError('表单尚未初始化，请重新打开后再保存')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    formError.value = '请先完善必填项和格式校验'
    showErrorMessage(formError.value)
    return
  }
  if (form.minLength > form.maxLength) {
    showFormError('最小密码长度不能大于最大密码长度')
    return
  }

  const payload: PasswordPolicyPayload = {
    ...form,
    historyCount: 0,
    expireDays: expireEnabled.value ? form.expireDays : 0,
    retryLimit: lockEnabled.value ? form.retryLimit : 0,
    lockMinutes: lockEnabled.value ? form.lockMinutes : 0,
  }

  saveLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await passwordPolicyApi.create(payload)
    } else if (editingId.value) {
      await passwordPolicyApi.update(editingId.value, payload)
    }
    showSuccessMessage(dialogMode.value === 'create' ? '密码策略已创建' : '密码策略已更新')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    showFormError(getErrorMessage(error, '保存密码策略失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: PasswordPolicyRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响绑定该模板的身份域密码校验与登录失败锁定策略。确认${action}？`,
    `${action}密码策略`,
    {
      type: row.status === STATUS_NORMAL ? 'warning' : 'info',
      appendTo: 'body',
      modalClass: 'app-confirm-overlay',
      customClass: 'app-confirm-box',
      confirmButtonText: action,
      cancelButtonText: '取消',
    },
  )

  try {
    if (usingMock.value) {
      row.status = row.status === STATUS_NORMAL ? STATUS_DISABLED : STATUS_NORMAL
    } else {
      await passwordPolicyApi.toggleStatus(row.id)
    }
    showSuccessMessage(`密码策略已${action}`)
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, `${action}密码策略失败`))
  }
}

const openDetail = async (row: PasswordPolicyRecord) => {
  try {
    detail.value = usingMock.value ? row : await passwordPolicyApi.detail(row.id)
    detailVisible.value = true
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取密码策略详情失败'))
  }
}

fetchData()
</script>

<template>
  <section class="password-policy-page">
    <div class="page-heading">
      <div>
        <h1>密码策略</h1>
        <p>定义账号登录密码的安全基线。策略创建后可绑定到身份域，用于统一校验新密码、密码过期和登录失败锁定。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增策略</el-button>
    </div>

    <el-card shadow="never" class="section-card">
      <div class="section-intro">
        <div>
          <strong>策略模板列表</strong>
          <span>列表仅维护可复用的密码策略模板；是否生效取决于身份域或登录配置中的绑定关系。</span>
        </div>
      </div>
      <div class="table-toolbar">
        <div class="filter-row">
          <el-input
            v-model="query.name"
            class="filter-input"
            clearable
            placeholder="策略名称"
            :prefix-icon="Search"
            @keyup.enter="fetchData"
          />
          <el-input
            v-model="query.code"
            class="filter-input"
            clearable
            placeholder="策略编码"
            @keyup.enter="fetchData"
          />
          <el-select v-model="query.status" clearable placeholder="全部状态" class="status-filter">
            <el-option label="启用" :value="STATUS_NORMAL" />
            <el-option label="禁用" :value="STATUS_DISABLED" />
          </el-select>
        </div>
        <div class="toolbar-actions">
          <el-button @click="resetQuery">重置</el-button>
          <el-button :icon="Refresh" @click="fetchData">查询</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" row-key="id" empty-text="暂无密码策略，请点击右上角新增策略">
        <el-table-column prop="name" label="策略名称" min-width="160" />
        <el-table-column prop="code" label="策略编码" min-width="170" />
        <el-table-column label="复杂度" width="100">
          <template #default="{ row }">
            <el-tag :type="complexityText(row) === '高' ? 'danger' : 'primary'">
              {{ complexityText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="长度" width="120">
          <template #default="{ row }">{{ row.minLength }}-{{ row.maxLength }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="120">
          <template #default="{ row }">
            {{ row.expireDays === 0 ? '不限制' : `${row.expireDays} 天` }}
          </template>
        </el-table-column>
        <el-table-column label="失败锁定" width="150">
          <template #default="{ row }">
            {{ row.retryLimit === 0 ? '不启用' : `${row.retryLimit} 次 / ${row.lockMinutes} 分钟` }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === STATUS_NORMAL ? 'success' : 'info'">
              {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row)">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增密码策略' : '编辑密码策略'"
      width="720px"
      class="policy-dialog"
      destroy-on-close
    >
      <el-form
        id="password-policy-form"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submitForm"
      >
        <div class="form-section">
          <div class="form-section-title">基础信息</div>
          <p class="form-section-desc">策略编码用于接口、绑定关系和审计记录，创建后不可修改。</p>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="策略名称" prop="name">
                <el-input v-model="form.name" placeholder="默认密码策略" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="策略编码" prop="code">
                <el-input v-model="form.code" :disabled="dialogMode === 'edit'" placeholder="default-password" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">密码复杂度</div>
          <p class="form-section-desc">复杂度规则会在用户修改密码、重置密码或首次设置密码时执行校验。</p>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="最小长度" prop="minLength">
                <el-input-number v-model="form.minLength" :min="6" :max="128" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大长度" prop="maxLength">
                <el-input-number v-model="form.maxLength" :min="6" :max="256" controls-position="right" />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="rule-switch-list">
            <label>
              <span>要求大写字母</span>
              <el-switch v-model="form.requireUppercase" />
            </label>
            <label>
              <span>要求小写字母</span>
              <el-switch v-model="form.requireLowercase" />
            </label>
            <label>
              <span>要求数字</span>
              <el-switch v-model="form.requireDigit" />
            </label>
            <label>
              <span>要求特殊字符</span>
              <el-switch v-model="form.requireSpecialChar" />
            </label>
            <label>
              <span>禁止包含用户名</span>
              <el-switch v-model="form.disallowUsername" />
            </label>
          </div>
        </div>

        <div class="form-section">
          <div class="form-section-title">过期与锁定</div>
          <div class="policy-option-card">
            <div class="policy-option-header">
              <div>
                <strong>密码过期</strong>
                <span>启用后，用户需要按周期更新密码。</span>
              </div>
              <el-switch v-model="expireEnabled" />
            </div>
            <el-form-item label="有效期天数" class="policy-option-field">
              <el-input-number
                v-model="form.expireDays"
                :disabled="!expireEnabled"
                :min="0"
                :max="3650"
                controls-position="right"
              />
            </el-form-item>
          </div>

          <div class="policy-option-card">
            <div class="policy-option-header">
              <div>
                <strong>失败锁定</strong>
                <span>启用后，连续登录失败会临时锁定账号。</span>
              </div>
              <el-switch v-model="lockEnabled" />
            </div>
            <div v-if="lockEnabled" class="lock-setting-row">
              <el-form-item label="连续失败次数">
                <el-input-number
                  v-model="form.retryLimit"
                  :min="1"
                  :max="20"
                  controls-position="right"
                />
              </el-form-item>
              <el-form-item label="锁定时长（分钟）">
                <el-input-number
                  v-model="form.lockMinutes"
                  :min="1"
                  :max="10080"
                  controls-position="right"
                />
              </el-form-item>
            </div>
            <div v-else class="disabled-hint">关闭后不执行失败次数累计和自动锁定。</div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button native-type="button" @click="dialogVisible = false">取消</el-button>
        <el-button
          form="password-policy-form"
          native-type="submit"
          type="primary"
          :loading="saveLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="密码策略详情" size="480px">
      <div v-if="detail" class="detail-drawer">
        <div class="detail-summary">
          <div>
            <h2>{{ detail.name }}</h2>
            <p>{{ detail.code }}</p>
          </div>
          <el-tag :type="detail.status === STATUS_NORMAL ? 'success' : 'info'">
            {{ detail.status === STATUS_NORMAL ? '启用' : '禁用' }}
          </el-tag>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">复杂度规则</div>
          <p class="detail-section-desc">用户新密码必须同时满足以下已启用规则。</p>
          <div class="rule-tags">
            <el-tag>{{ detail.minLength }}-{{ detail.maxLength }} 位</el-tag>
            <el-tag v-if="detail.requireUppercase">大写字母</el-tag>
            <el-tag v-if="detail.requireLowercase">小写字母</el-tag>
            <el-tag v-if="detail.requireDigit">数字</el-tag>
            <el-tag v-if="detail.requireSpecialChar">特殊字符</el-tag>
            <el-tag v-if="detail.disallowUsername">禁止用户名</el-tag>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">过期与锁定</div>
          <p class="detail-section-desc">这些规则影响密码轮换周期以及连续登录失败后的账号锁定行为。</p>
          <div class="detail-grid">
            <div>
              <span>有效期</span>
              <strong>{{ detail.expireDays === 0 ? '不限制' : `${detail.expireDays} 天` }}</strong>
            </div>
            <div>
              <span>失败锁定</span>
              <strong>{{ detail.retryLimit === 0 ? '不启用' : `${detail.retryLimit} 次` }}</strong>
            </div>
            <div>
              <span>锁定时长</span>
              <strong>{{ detail.lockMinutes === 0 ? '不自动解锁' : `${detail.lockMinutes} 分钟` }}</strong>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">说明</div>
          <p class="detail-description">{{ detail.description || '暂无说明' }}</p>
        </div>
      </div>
    </el-drawer>
  </section>
</template>

<style scoped>
.password-policy-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-heading,
.table-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
}

.page-heading {
  padding: 24px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.page-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-input {
  width: 220px;
}

.status-filter {
  width: 140px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.section-intro {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f9fafb;
}

.section-intro div {
  display: grid;
  gap: 6px;
}

.section-intro strong {
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.section-intro span,
.form-section-desc,
.detail-section-desc {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.el-tag + .el-tag {
  margin-left: 6px;
}

.form-section {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafbfc;
}

.form-section + .form-section {
  margin-top: 14px;
}

.form-section-title {
  margin-bottom: 6px;
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.form-section-desc {
  margin: 0 0 14px;
}

.policy-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
}

.policy-dialog :deep(.el-input-number) {
  width: 100%;
}

.rule-switch-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.rule-switch-list label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
  color: #374151;
  font-size: 14px;
}

.policy-option-card {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.policy-option-card + .policy-option-card {
  margin-top: 12px;
}

.policy-option-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.policy-option-header div {
  display: grid;
  gap: 4px;
}

.policy-option-header strong {
  color: #111827;
  font-size: 14px;
  font-weight: 600;
}

.policy-option-header span,
.disabled-hint {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.policy-option-field {
  margin-top: 14px;
  margin-bottom: 0;
  max-width: 260px;
}

.lock-setting-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.lock-setting-row :deep(.el-form-item) {
  margin-bottom: 0;
}

.disabled-hint {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f9fafb;
}

.detail-drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 18px;
  border-radius: 8px;
  background: #f7f8fa;
}

.detail-summary h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.detail-summary p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.detail-section {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.detail-section-title {
  margin-bottom: 6px;
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.detail-section-desc {
  margin: 0 0 12px;
}

.rule-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.rule-tags .el-tag {
  margin-left: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-grid div {
  display: grid;
  gap: 6px;
  padding: 12px;
  border-radius: 6px;
  background: #f9fafb;
}

.detail-grid span {
  color: #6b7280;
  font-size: 12px;
}

.detail-grid strong {
  color: #111827;
  font-size: 14px;
}

.detail-description {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .page-heading,
  .table-toolbar,
  .filter-row {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-input,
  .status-filter {
    width: 100%;
  }

  .rule-switch-list,
  .detail-grid,
  .lock-setting-row {
    grid-template-columns: 1fr;
  }
}
</style>
