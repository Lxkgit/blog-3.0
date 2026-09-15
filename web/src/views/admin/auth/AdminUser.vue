<template>
  <div class="user-page">
    <div class="title_style">
      <span>用户管理</span>
    </div>

    <el-card class="user-card">
      <el-table :data="userList.data" stripe style="width: 100%; height: calc(100vh - 268px)">
        <el-table-column type="selection" width="55" />

        <el-table-column prop="username" label="用户名" min-width="150" />

        <el-table-column prop="headImg" label="头像" width="90">
          <template #default="{ row }">
            <el-avatar :src="row.headImg" />
          </template>
        </el-table-column>

        <el-table-column prop="nickname" label="昵称" min-width="150" />

        <el-table-column prop="email" label="邮箱地址" min-width="180" />

        <el-table-column prop="sysRole" label="角色" min-width="240">
          <template #default="{ row }">
            <div class="role-tags">
              <el-tag v-for="(role, index) in row.roleList" :key="index">
                {{ role.roleName }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '1'" type="success">
              {{ userStatus(row.status) }}
            </el-tag>

            <el-tag v-else type="warning">
              {{ userStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建日期" width="162" />

        <el-table-column prop="updateTime" label="最近登录" min-width="162" />

        <el-table-column fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button
              style="margin-left: 0"
              size="small"
              text
              title="修改信息"
              @click.prevent="loadUserData(row)"
            >
              <MyIcon type="icon-edit" />
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:page-size="size"
          v-model:current-page="page"
          background
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="pageChange"
          @size-change="sizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="updateUserDialog"
      title="用户信息"
      width="30%"
      style="max-width: 500px"
      destroy-on-close
    >
      <el-form :model="userDate.data">
        <el-form-item label="用户名：" label-width="100">
          <el-input
            v-model="userDate.data.username"
            disabled
            autocomplete="off"
            class="form-input"
          />
        </el-form-item>

        <el-form-item label="头像：" label-width="100">
          <ImgUpload
            :key="userDate.data.id"
            @upload="userHeadUpload"
            :imgList="
              userDate.data.headImg === null ||
              userDate.data.headImg === undefined ||
              userDate.data.headImg === ''
                ? ['']
                : [userDate.data.headImg]
            "
            :num="1"
            fileTypeCode="1"
            filePathCode="3"
            :cropper="1"
            :autoCropWidth="100"
            :autoCropHeight="100"
          />
        </el-form-item>

        <el-form-item label="昵称：" label-width="100">
          <el-input v-model="userDate.data.nickname" autocomplete="off" class="form-input" />
        </el-form-item>

        <el-form-item label="角色：" label-width="100">
          <el-select
            v-model="userDate.data.roleIds"
            multiple
            collapse-tags
            placeholder="选择角色"
            class="form-input"
          >
            <el-option
              v-for="role in roleList.data"
              :key="role.id"
              :label="role.label"
              :value="role.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="邮箱：" label-width="100">
          <el-input v-model="userDate.data.email" autocomplete="off" class="form-input" />
        </el-form-item>

        <el-form-item label="账号状态：" label-width="100">
          <el-select v-model="userDate.data.status" class="form-input">
            <el-option label="禁用" :value="0" />
            <el-option label="正常" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="最近登录：" label-width="100">
          <el-input
            v-model="userDate.data.updateTime"
            disabled
            autocomplete="off"
            class="form-input"
          />
        </el-form-item>

        <el-form-item label="创建日期：" label-width="100">
          <el-input
            v-model="userDate.data.createTime"
            disabled
            autocomplete="off"
            class="form-input"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="updateUserDialog = false"> 取消 </el-button>

          <el-button type="primary" @click="updateUserFun"> 提交 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import mixin from '@/mixins/user'
import { ref, reactive, onMounted } from 'vue'
import { userListApi, roleListApi, updateUserPerApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import icon from '@/utils/icon'
import ImgUpload from '@/components/common/ImgUpload.vue'

const {
  page,
  size,
  total,
  userList,
  roleList,
  updateUserDialog,
  userDate,
  pageChange,
  sizeChange,
  getUserList,
  getRoleList,
  loadUserData,
  userHeadUpload,
  updateUserFun,
} = userFn()

const { MyIcon } = icon()
const { userStatus } = mixin()

onMounted(() => {
  pageChange(1)
})

function userFn(): any {
  const page = ref<number>(1)
  const size = ref<number>(20)
  const total = ref<number>(0)

  const userList: any = reactive({
    data: [],
  })

  const roleList: any = reactive({
    data: [],
  })

  const updateUserDialog = ref(false)

  const userDate: any = reactive({
    data: {},
  })

  const pageChange = (currentPage: number) => {
    page.value = currentPage
    getUserList(currentPage)
  }

  const sizeChange = (pageSize: number) => {
    size.value = pageSize
    page.value = 1
    getUserList(1)
  }

  const getUserList = (currentPage: number) => {
    userListApi(currentPage, size.value).then((res: any) => {
      if (res.code === 200) {
        userList.data = res.result.list
        total.value = res.result.total
      }
    })
  }

  const getRoleList = () => {
    roleListApi(1, 200).then((res: any) => {
      if (res.code === 200) {
        roleList.data = res.result.list

        for (let i = 0; i < roleList.data.length; i++) {
          roleList.data[i].label = roleList.data[i].roleName
          roleList.data[i].value = roleList.data[i].id
        }
      }
    })
  }

  const loadUserData = (data: any) => {
    userDate.data = JSON.parse(JSON.stringify(data))
    updateUserDialog.value = true
    getRoleList()
  }

  const userHeadUpload = (upload: any) => {
    if (upload !== null && upload !== undefined) {
      userDate.data.headImg = upload.fileUrl
    }
  }

  const updateUserFun = () => {
    updateUserDialog.value = false

    updateUserPerApi({
      id: userDate.data.id,
      nickname: userDate.data.nickname,
      roleIds: userDate.data.roleIds,
      headImg: userDate.data.headImg,
      email: userDate.data.email,
      status: userDate.data.status,
    }).then((res: any) => {
      if (res.code === 200) {
        ElMessage({
          message: '用户数据修改成功',
          type: 'success',
        })

        pageChange(1)
      }
    })
  }

  return {
    page,
    size,
    total,
    userList,
    roleList,
    updateUserDialog,
    userDate,
    pageChange,
    sizeChange,
    getUserList,
    getRoleList,
    loadUserData,
    userHeadUpload,
    updateUserFun,
  }
}
</script>

<style scoped>
.user-page {
  width: 100%;
}

.title_style {
  display: flex;
  justify-content: flex-start;
  align-items: baseline;
  max-height: 31px;
  color: #445160;
  font-size: 24px;
  font-weight: 600;
  text-align: left;
}

.user-card {
  width: 98%;
  margin: 10px 0 0 0;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 12px;
}

.form-input {
  width: 80%;
  max-width: 250px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}
</style>
