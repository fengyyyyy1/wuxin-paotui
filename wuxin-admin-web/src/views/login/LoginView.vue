<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

interface LoginForm {
  username: string
  password: string
}

const formRef = ref<FormInstance>()
const form = reactive<LoginForm>({
  username: '',
  password: '',
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  ElMessage.info('登录接口将在下一阶段联调')
}
</script>

<template>
  <main class="login-page">
    <section class="login-brand">
      <div class="login-brand__mark">五鑫</div>
      <h1>五鑫跑腿</h1>
      <p>总控管理后台</p>
      <div class="login-brand__line"></div>
      <span>商家审核与平台运营工作台</span>
    </section>

    <section class="login-panel" aria-labelledby="login-title">
      <div class="login-panel__inner">
        <div class="login-panel__heading">
          <span class="login-panel__eyebrow">ADMIN CONSOLE</span>
          <h2 id="login-title">管理员登录</h2>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="submit"
        >
          <el-form-item label="管理员账号" prop="username">
            <el-input
              v-model="form.username"
              :prefix-icon="User"
              autocomplete="username"
              placeholder="请输入管理员账号"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              :prefix-icon="Lock"
              type="password"
              autocomplete="current-password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-button native-type="submit" type="primary" class="login-button">登录</el-button>
        </el-form>

        <p class="login-panel__footer">仅限已授权的平台管理员访问</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(320px, 0.9fr) minmax(420px, 1.1fr);
  background: #ffffff;
}

.login-brand {
  display: flex;
  padding: 64px;
  color: #f7f9fc;
  flex-direction: column;
  justify-content: center;
  background: #172033;
}

.login-brand__mark {
  display: grid;
  width: 56px;
  height: 56px;
  margin-bottom: 32px;
  color: #172033;
  font-size: 16px;
  font-weight: 750;
  place-items: center;
  background: #f7c948;
  border-radius: 6px;
}

.login-brand h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.15;
  letter-spacing: 0;
}

.login-brand p {
  margin: 12px 0 0;
  color: #c7d0dc;
  font-size: 20px;
}

.login-brand__line {
  width: 48px;
  height: 3px;
  margin: 36px 0 20px;
  background: #f7c948;
}

.login-brand span {
  color: #9da9ba;
  font-size: 14px;
}

.login-panel {
  display: grid;
  padding: 48px;
  place-items: center;
  background: #f7f8fa;
}

.login-panel__inner {
  width: min(100%, 420px);
  padding: 40px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
  box-shadow: 0 16px 48px rgb(23 32 51 / 8%);
}

.login-panel__heading {
  margin-bottom: 30px;
}

.login-panel__eyebrow {
  color: #9a7412;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.login-panel h2 {
  margin: 8px 0 0;
  color: #172033;
  font-size: 28px;
  letter-spacing: 0;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.login-panel__footer {
  margin: 24px 0 0;
  color: #8a94a4;
  font-size: 12px;
  text-align: center;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-brand {
    min-height: 240px;
    padding: 36px 28px;
  }

  .login-brand__mark {
    margin-bottom: 18px;
  }

  .login-brand h1 {
    font-size: 32px;
  }

  .login-brand__line,
  .login-brand span {
    display: none;
  }

  .login-panel {
    padding: 28px 18px;
  }

  .login-panel__inner {
    padding: 30px 24px;
  }
}
</style>
