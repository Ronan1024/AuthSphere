<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { AccountRecord } from '@/api/account'

const props = defineProps<{
  account: AccountRecord
}>()

const roleList = ref([
  {
    id: 1,
    subjectName: '租户A',
    appInstanceName: '租户A商城实例',
    clientName: '商城平台端',
    roles: ['商城管理员', '商城运营'],
    roleType: '自定义',
    status: '启用',
    authTime: '2026-05-01'
  },
  {
    id: 2,
    subjectName: '租户A',
    appInstanceName: '租户A支付实例',
    clientName: '支付平台端',
    roles: ['支付管理员'],
    roleType: '自定义',
    status: '启用',
    authTime: '2026-05-03'
  },
  {
    id: 3,
    subjectName: '支付服务商A',
    appInstanceName: '服务商支付实例',
    clientName: '支付商户端',
    roles: [] as string[],
    roleType: '-',
    status: '无',
    authTime: '-'
  }
])

const handleAssign = () => {
  ElMessage.success('正在为您调起客户端角色分配向导...')
}

const handleAdjust = (row: any) => {
  ElMessage.info(`调整角色: ${row.appInstanceName}`)
}

const handleRemove = (row: any) => {
  ElMessageBox.confirm(
    `确定要移除该账号在 ${row.subjectName} / ${row.appInstanceName} 下分配的客户端角色吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    roleList.value = roleList.value.filter(item => item.id !== row.id)
    ElMessage.success('移除成功')
  }).catch(() => {})
}
</script>

<template>
  <div class="account-client-role-container">
    <!-- 顶部说明及按钮栏 -->
    <div class="role-header-bar mb-4">
      <div class="header-left">
        <span class="main-title">账号详情 / 客户端角色</span>
        <span class="subtitle-tip">角色必须在主体 + 应用实例 + 客户端上下文下分配</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleAssign">分配客户端角色</el-button>
    </div>

    <!-- 表格数据区 -->
    <el-card shadow="never" class="role-table-card">
      <el-table :data="roleList" style="width: 100%" class="custom-role-table">
        <el-table-column prop="subjectName" label="主体" min-width="120" />
        <el-table-column prop="appInstanceName" label="应用实例" min-width="150" />
        <el-table-column prop="clientName" label="客户端" min-width="130" />
        
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <div class="roles-tags-flex" v-if="row.roles && row.roles.length > 0">
              <el-tag 
                v-for="role in row.roles" 
                :key="role" 
                size="default" 
                class="role-badge-tag"
              >
                {{ role }}
              </el-tag>
            </div>
            <span v-else class="status-pill orange-light">待分配</span>
          </template>
        </el-table-column>

        <el-table-column prop="roleType" label="角色类型" width="100" />
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="status-pill green-pill" v-if="row.status === '启用'">启用</span>
            <span class="status-pill gray-pill" v-else>无</span>
          </template>
        </el-table-column>

        <el-table-column prop="authTime" label="授权时间" width="140" />

        <el-table-column label="操作" width="160" fixed="right" align="right">
          <template #default="{ row }">
            <div class="row-actions-flex">
              <template v-if="row.roles && row.roles.length > 0">
                <span class="action-btn-link" @click="handleAdjust(row)">调整角色</span>
                <span class="action-btn-link danger" @click="handleRemove(row)">移除</span>
              </template>
              <template v-else>
                <span class="action-btn-link" @click="handleAssign">分配角色</span>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.account-client-role-container {
  padding-top: 8px;
  --primary-color: #0369A1;
  --primary-hover: #0284c7;
  --text-main: #0C4A6E;
  --text-muted: #475569;
  --border-light: rgba(226, 232, 240, 0.8);
  --success-color: #16A34A;
  --success-bg: #dcfce7;
  --font-family-display: 'Lexend', system-ui, -apple-system, sans-serif;
  --font-family-body: 'Source Sans 3', system-ui, -apple-system, sans-serif;
}

.role-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 16px;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.main-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.subtitle-tip {
  font-size: 13px;
  color: var(--text-muted);
}

.role-table-card {
  border-radius: 12px;
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.roles-tags-flex {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-badge-tag {
  background: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  font-weight: 600;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

.status-pill.green-pill {
  color: var(--success-color);
  background: var(--success-bg);
}

.status-pill.gray-pill {
  color: #64748b;
  background: #f1f5f9;
}

.status-pill.orange-light {
  color: #c2410c;
  background: #ffedd5;
  border: 1px solid #fed7aa;
}

.row-actions-flex {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.action-btn-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-color);
  cursor: pointer;
  transition: color 0.15s ease;
}

.action-btn-link:hover {
  color: var(--primary-hover);
}

.action-btn-link.danger {
  color: #dc2626;
}

.action-btn-link.danger:hover {
  color: #b91c1c;
}

.mb-4 {
  margin-bottom: 16px;
}

:deep(.el-card__body) {
  padding: 0;
}
</style>
