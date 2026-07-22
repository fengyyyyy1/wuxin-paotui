<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Edit, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { getConfigs, updateConfig } from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import type { SystemConfig } from '@/types/admin'

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const activeGroup = ref('ERRAND')
const rows = ref<SystemConfig[]>([])
const editing = ref<SystemConfig | null>(null)
const form = ref({ configValue: '', status: 1 })

const groups = [
  { value: 'ERRAND', label: '跑腿配置' },
  { value: 'PLATFORM', label: '平台配置' },
  { value: 'USER', label: '用户配置' },
  { value: 'HOME', label: '首页配置' },
  { value: 'SYSTEM', label: '系统配置' },
]

const activeLabel = computed(
  () => groups.find((item) => item.value === activeGroup.value)?.label || '系统配置',
)

async function load(): Promise<void> {
  loading.value = true
  try {
    rows.value = await getConfigs(activeGroup.value)
  } finally {
    loading.value = false
  }
}

function openEditor(value: unknown): void {
  const row = value as SystemConfig
  editing.value = row
  form.value = { configValue: row.configValue, status: row.status }
}

async function save(): Promise<void> {
  if (!editing.value || saving.value) return
  saving.value = true
  try {
    await updateConfig(editing.value.id, form.value.configValue, form.value.status)
    ElMessage.success('配置已保存并立即生效')
    editing.value = null
    await load()
  } finally {
    saving.value = false
  }
}

function valueTypeText(type: string): string {
  return ({ DECIMAL: '小数', INTEGER: '整数', BOOLEAN: '布尔值', TEXT: '长文本', STRING: '文本' } as Record<string, string>)[type] || type
}

onMounted(() => void load())
</script>

<template>
  <div class="page-shell">
    <PageHeader title="系统配置" subtitle="配置保存到数据库并由各端动态读取，无需重新发版">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </template>
    </PageHeader>

    <section class="content-panel config-panel">
      <el-tabs v-model="activeGroup" @tab-change="load">
        <el-tab-pane v-for="group in groups" :key="group.value" :label="group.label" :name="group.value" />
      </el-tabs>

      <el-alert
        v-if="activeGroup === 'SYSTEM'"
        title="密钥类配置仅显示掩码。生产环境仍应优先使用密钥管理服务或环境变量。"
        type="warning"
        show-icon
        :closable="false"
      />

      <el-table v-loading="loading" :data="rows" border class="config-table" row-key="id">
        <el-table-column prop="configName" label="配置项" min-width="160" fixed="left" />
        <el-table-column prop="configKey" label="配置键" min-width="240" show-overflow-tooltip />
        <el-table-column label="当前值" min-width="210">
          <template #default="{ row }">
            <span :class="{ 'masked-value': row.sensitive === 1 }">{{ row.configValue || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ valueTypeText(row.valueType) }}</template>
        </el-table-column>
        <el-table-column prop="configDescription" label="说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="auth.hasPermission('config:manage')"
              link
              type="primary"
              :icon="Edit"
              @click="openEditor(row)"
            >编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog :model-value="Boolean(editing)" :title="`编辑${activeLabel}`" width="560px" destroy-on-close @update:model-value="(value: boolean) => { if (!value) editing = null }">
      <el-form v-if="editing" label-position="top" @submit.prevent="save">
        <el-form-item label="配置项">
          <el-input :model-value="editing.configName" disabled />
        </el-form-item>
        <el-form-item label="配置键">
          <el-input :model-value="editing.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input
            v-if="editing.valueType === 'TEXT'"
            v-model="form.configValue"
            type="textarea"
            :rows="6"
            maxlength="20000"
            show-word-limit
          />
          <el-select v-else-if="editing.valueType === 'BOOLEAN'" v-model="form.configValue">
            <el-option label="是" value="true" />
            <el-option label="否" value="false" />
          </el-select>
          <el-input v-else v-model="form.configValue" maxlength="20000" />
          <div class="field-hint">{{ editing.configDescription || '保存后立即影响读取该配置的业务端。' }}</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button :value="1">启用</el-radio-button>
            <el-radio-button :value="0">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="editing = null">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存并生效</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.config-panel { padding-top: 8px; }
.config-table { margin-top: 16px; }
.masked-value { color: #9a6700; font-family: monospace; letter-spacing: 2px; }
.field-hint { margin-top: 8px; color: #697386; font-size: 12px; line-height: 1.6; }
</style>
