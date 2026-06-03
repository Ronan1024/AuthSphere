<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'

const visible = ref(false)
const selectedPlatform = ref('')

const platforms = [
  { id: 'wechat', name: '微信', color: '#07c160', letter: 'W' },
  { id: 'alipay', name: '支付宝', color: '#1677ff', letter: 'A' },
  { id: 'wecom', name: '企业微信', color: '#2b85e4', letter: '企' },
  { id: 'dingtalk', name: '钉钉', color: '#008ce5', letter: '钉' },
  { id: 'google', name: 'Google', color: '#ea4335', letter: 'G' }
]

const open = () => {
  selectedPlatform.value = ''
  visible.value = true
}

const selectPlatform = (id: string) => {
  selectedPlatform.value = id
}

const submit = () => {
  if (!selectedPlatform.value) {
    ElMessage.warning('请选择要绑定的平台')
    return
  }
  ElMessage.success('已发送绑定请求，请在新窗口完成授权')
  visible.value = false
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="绑定第三方账号" width="520px" destroy-on-close>
    <el-alert
      title="绑定后，用户可以使用第三方账号快捷登录。"
      type="info"
      show-icon
      :closable="false"
      class="mb-6"
    />

    <div class="form-section">
      <div class="section-label">选择平台</div>
      <div class="platform-grid">
        <div 
          v-for="p in platforms" 
          :key="p.id" 
          class="platform-item" 
          :class="{ active: selectedPlatform === p.id }"
          @click="selectPlatform(p.id)"
        >
          <div class="platform-icon" :style="{ color: p.color, borderColor: p.color }">{{ p.letter }}</div>
          <span>{{ p.name }}</span>
        </div>
        <div class="platform-item">
          <div class="platform-icon more-icon"><el-icon><MoreFilled /></el-icon></div>
          <span>更多</span>
        </div>
      </div>
    </div>

    <div class="instruction-section">
      <div class="section-label">绑定说明</div>
      <ol class="instruction-list">
        <li>点击选择要绑定的第三方平台；</li>
        <li>完成第三方平台的授权登录；</li>
        <li>授权成功后将自动完成绑定。</li>
      </ol>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :disabled="!selectedPlatform">开始绑定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.mb-6 { margin-bottom: 24px; }

.section-label {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
  margin-bottom: 12px;
}

.platform-grid {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.platform-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.platform-item:hover {
  background-color: #f3f4f6;
}

.platform-item.active {
  background-color: #eff6ff;
}

.platform-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 1px solid;
  font-size: 20px;
  font-weight: bold;
  background-color: white;
}

.more-icon {
  color: #9ca3af;
  border-color: #e5e7eb;
}

.platform-item span {
  font-size: 12px;
  color: #4b5563;
}

.instruction-list {
  padding-left: 20px;
  color: #6b7280;
  font-size: 13px;
  line-height: 2;
  margin: 0;
}
</style>
