<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import {
  getAdminUsers,
  getPermissions,
  getRoles,
  updateAdminUserRoles,
  updateRolePermissions,
} from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import type { AdminUserItem, PermissionItem, RoleItem } from '@/types/admin'

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const roles = ref<RoleItem[]>([])
const permissions = ref<PermissionItem[]>([])
const users = ref<AdminUserItem[]>([])
const selectedUser = ref<AdminUserItem | null>(null)
const selectedRole = ref<RoleItem | null>(null)
const selectedRoleIds = ref<number[]>([])
const selectedPermissionIds = ref<number[]>([])

const permissionGroups = computed(() => {
  const result = new Map<string, PermissionItem[]>()
  for (const permission of permissions.value) {
    const items = result.get(permission.moduleCode) || []
    items.push(permission)
    result.set(permission.moduleCode, items)
  }
  return [...result.entries()]
})

async function load(): Promise<void> {
  loading.value = true
  try {
    const [roleRows, permissionRows, userRows] = await Promise.all([
      getRoles(),
      getPermissions(),
      getAdminUsers(),
    ])
    roles.value = roleRows
    permissions.value = permissionRows
    users.value = userRows
  } finally {
    loading.value = false
  }
}

function editUser(value: unknown): void {
  const row = value as AdminUserItem
  selectedUser.value = row
  selectedRoleIds.value = [...row.roleIds]
}

function editRole(value: unknown): void {
  const row = value as RoleItem
  selectedRole.value = row
  selectedPermissionIds.value = [...row.permissionIds]
}

async function saveUserRoles(): Promise<void> {
  if (!selectedUser.value || selectedRoleIds.value.length === 0) return
  saving.value = true
  try {
    await updateAdminUserRoles(selectedUser.value.userId, selectedRoleIds.value)
    ElMessage.success('管理员角色已更新')
    selectedUser.value = null
    await load()
  } finally {
    saving.value = false
  }
}

async function saveRolePermissions(): Promise<void> {
  if (!selectedRole.value || selectedPermissionIds.value.length === 0) return
  saving.value = true
  try {
    await updateRolePermissions(selectedRole.value.roleId, selectedPermissionIds.value)
    ElMessage.success('角色权限已更新')
    selectedRole.value = null
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(() => void load())
</script>

<template>
  <div class="page-shell">
    <PageHeader title="权限管理" subtitle="基于角色与权限点控制后台菜单和敏感操作">
      <template #actions><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></template>
    </PageHeader>

    <el-tabs class="content-panel security-tabs">
      <el-tab-pane label="管理员账号">
        <el-table v-loading="loading" :data="users" border row-key="userId">
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="username" label="账号" min-width="150" />
          <el-table-column prop="nickname" label="姓名" min-width="120" />
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column label="角色" min-width="240">
            <template #default="{ row }">
              <div class="tag-list"><el-tag v-for="name in row.roleNames" :key="name">{{ name }}</el-tag></div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最近登录" min-width="170" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }"><el-button v-if="auth.hasPermission('rbac:manage')" link type="primary" @click="editUser(row)">分配角色</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="角色权限">
        <el-table v-loading="loading" :data="roles" border row-key="roleId">
          <el-table-column prop="roleName" label="角色" min-width="150" />
          <el-table-column prop="roleCode" label="角色编码" min-width="180" />
          <el-table-column prop="roleDescription" label="职责说明" min-width="260" />
          <el-table-column label="权限数" width="100"><template #default="{ row }">{{ row.permissionIds.length }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="110" fixed="right"><template #default="{ row }"><el-button v-if="auth.hasPermission('rbac:manage')" link type="primary" @click="editRole(row)">配置权限</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="权限字典">
        <el-table v-loading="loading" :data="permissions" border row-key="permissionId">
          <el-table-column prop="permissionName" label="权限名称" min-width="180" />
          <el-table-column prop="permissionCode" label="权限编码" min-width="200" />
          <el-table-column prop="moduleCode" label="模块" width="130" />
          <el-table-column prop="permissionType" label="类型" width="100" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :model-value="Boolean(selectedUser)" title="分配管理员角色" width="520px" @update:model-value="(value: boolean) => { if (!value) selectedUser = null }">
      <p v-if="selectedUser" class="dialog-summary">账号：{{ selectedUser.username }}。至少保留一个后台角色。</p>
      <el-checkbox-group v-model="selectedRoleIds" class="choice-list">
        <el-checkbox v-for="role in roles" :key="role.roleId" :value="role.roleId" :disabled="role.status !== 1">
          <strong>{{ role.roleName }}</strong><span>{{ role.roleDescription || role.roleCode }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer><el-button :disabled="saving" @click="selectedUser = null">取消</el-button><el-button type="primary" :loading="saving" :disabled="selectedRoleIds.length === 0" @click="saveUserRoles">保存角色</el-button></template>
    </el-dialog>

    <el-dialog :model-value="Boolean(selectedRole)" title="配置角色权限" width="720px" @update:model-value="(value: boolean) => { if (!value) selectedRole = null }">
      <p v-if="selectedRole" class="dialog-summary">角色：{{ selectedRole.roleName }}。菜单权限控制访问，操作权限控制写操作。</p>
      <div class="permission-groups">
        <section v-for="[moduleCode, items] in permissionGroups" :key="moduleCode" class="permission-group">
          <strong>{{ moduleCode }}</strong>
          <el-checkbox-group v-model="selectedPermissionIds">
            <el-checkbox v-for="permission in items" :key="permission.permissionId" :value="permission.permissionId" :disabled="permission.status !== 1">{{ permission.permissionName }}</el-checkbox>
          </el-checkbox-group>
        </section>
      </div>
      <template #footer><el-button :disabled="saving" @click="selectedRole = null">取消</el-button><el-button type="primary" :loading="saving" :disabled="selectedPermissionIds.length === 0" @click="saveRolePermissions">保存权限</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.security-tabs { padding: 8px 20px 20px; }
.tag-list { display: flex; flex-wrap: wrap; gap: 6px; }
.dialog-summary { margin: 0 0 18px; color: #697386; line-height: 1.6; }
.choice-list { display: grid; gap: 10px; }
.choice-list :deep(.el-checkbox) { height: auto; margin: 0; padding: 12px; align-items: flex-start; border: 1px solid #e1e6ed; border-radius: 6px; }
.choice-list span { display: block; margin-top: 4px; color: #697386; font-size: 12px; }
.permission-groups { display: grid; max-height: 520px; overflow-y: auto; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.permission-group { padding: 14px; border: 1px solid #e1e6ed; border-radius: 6px; }
.permission-group > strong { display: block; margin-bottom: 10px; text-transform: uppercase; }
.permission-group :deep(.el-checkbox-group) { display: grid; gap: 8px; }
@media (max-width: 680px) { .permission-groups { grid-template-columns: 1fr; } }
</style>
