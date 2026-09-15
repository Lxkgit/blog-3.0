<template>
  <div class="role-page">
    <div class="title_style">
      <span>角色管理</span>
    </div>

    <el-card class="role-card">
      <div class="toolbar">
        <el-button type="primary" plain @click="openCreateDialog"> 创建 </el-button>

        <el-popover
          v-model:visible="deleteBtnPopoverByIds"
          placement="top"
          :width="160"
          trigger="click"
          :disabled="ids.length === 0"
        >
          <p>删除所选角色？</p>

          <div class="popover-footer">
            <el-button size="small" text @click="deleteBtnPopoverByIds = false"> 取消 </el-button>

            <el-button size="small" type="primary" @click="deleteRoleFun(0)"> 删除 </el-button>
          </div>

          <template #reference>
            <el-button type="danger" plain :disabled="ids.length === 0"> 删除 </el-button>
          </template>
        </el-popover>
      </div>

      <el-table
        :data="roleList.data"
        stripe
        style="width: 100%; height: calc(100vh - 312px)"
        @selection-change="selected"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="roleCode" label="角色编码" min-width="120" />

        <el-table-column prop="roleName" label="角色名称" min-width="120" />

        <el-table-column prop="createBy" label="创建用户" min-width="100" />

        <el-table-column prop="createTime" label="创建时间" min-width="160" />

        <el-table-column prop="updateBy" label="修改用户" min-width="100" />

        <el-table-column prop="updateTime" label="修改时间" min-width="160" />

        <el-table-column fixed="right" label="操作" width="110">
          <template #default="{ row }">
            <el-button
              style="margin-left: 0"
              size="small"
              text
              title="修改角色权限"
              @click="loadMenuData(row.id)"
            >
              <MyIcon type="icon-edit" />
            </el-button>

            <el-popover
              v-model:visible="deletePopoverVisible[row.id]"
              placement="top"
              :width="180"
              trigger="click"
            >
              <p>删除角色「{{ row.roleName }}」？</p>

              <div class="popover-footer">
                <el-button size="small" text @click="deletePopoverVisible[row.id] = false">
                  取消
                </el-button>

                <el-button size="small" type="primary" @click="deleteRoleFun(row.id)">
                  删除
                </el-button>
              </div>

              <template #reference>
                <el-button style="margin-left: 0" size="small" text title="删除角色">
                  <MyIcon type="icon-delete" />
                </el-button>
              </template>
            </el-popover>
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
      v-model="updateRolePerDialog"
      title="角色权限修改"
      width="30%"
      style="max-width: 500px"
      destroy-on-close
    >
      <div class="permission-tree-wrapper">
        <el-tree
          ref="roleTreeRef"
          :data="menuList.data"
          node-key="id"
          show-checkbox
          :default-expand-all="false"
          :props="roleAuthProps"
          @check="handleCheckChange"
        >
          <template #default="{ data }">
            <span>{{ data.menuName }}</span>
          </template>
        </el-tree>

        <el-empty v-if="menuList.data.length === 0" description="暂无菜单权限" />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="updateRolePerDialog = false"> 取消 </el-button>

          <el-button type="primary" :loading="updateRolePerLoading" @click="updateRolePerFun">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="roleCreateDialog"
      title="创建新角色"
      width="30%"
      style="max-width: 500px"
      @closed="resetCreateRoleForm"
    >
      <el-form
        ref="createRoleFormRef"
        :model="roleDate"
        :rules="createRoleRules"
        label-width="100px"
      >
        <el-form-item label="角色编码：" prop="roleCode">
          <el-input v-model="roleDate.roleCode" autocomplete="off" class="form-input" />
        </el-form-item>

        <el-form-item label="角色名称：" prop="roleName">
          <el-input v-model="roleDate.roleName" autocomplete="off" class="form-input" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleCreateDialog = false"> 取消 </el-button>

          <el-button type="primary" :loading="createRoleLoading" @click="createRoleFun">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { allMenuApi, selectRolePerListApi } from '@/api/auth'
import { roleListApi, createRoleApi, deleteRoleApi, updateRolePerApi } from '@/api/user'
import icon from '@/utils/icon'

interface Role {
  id: number | string
  roleCode: string
  roleName: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

interface Menu {
  id: number | string
  menuName: string
  children?: Menu[]
  [key: string]: any
}

interface RoleAuthProps {
  children: string
  label: string
}

interface RoleListResult {
  list: Role[]
  total: number
}

const { MyIcon } = icon()

const roleCreateDialog = ref(false)
const createRoleFormRef = ref<FormInstance>()
const createRoleLoading = ref(false)

const roleDate = reactive({
  roleCode: '',
  roleName: '',
})

const checkRoleCode = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入角色编码(只允许大小写字母与数字)'))
    return
  }

  const pattern = /^[a-zA-Z0-9]+$/

  if (!pattern.test(value)) {
    callback(new Error('请输入正确的角色编码(只允许大小写字母与数字)'))
    return
  }

  callback()
}

const createRoleRules: FormRules = {
  roleCode: [
    {
      required: true,
      validator: checkRoleCode,
      trigger: 'blur',
    },
  ],

  roleName: [
    {
      required: true,
      message: '请输入角色名称',
      trigger: 'blur',
    },
  ],
}

const openCreateDialog = () => {
  roleDate.roleCode = ''
  roleDate.roleName = ''
  roleCreateDialog.value = true
}

const createRoleFun = async () => {
  if (!createRoleFormRef.value) {
    return
  }

  try {
    await createRoleFormRef.value.validate()

    createRoleLoading.value = true

    const res: any = await createRoleApi(roleDate)

    if (res.code === 200) {
      ElMessage.success('角色创建成功')

      roleCreateDialog.value = false

      await getRoleList(page.value, size.value)
    }
  } finally {
    createRoleLoading.value = false
  }
}

const resetCreateRoleForm = () => {
  createRoleFormRef.value?.resetFields()

  roleDate.roleCode = ''
  roleDate.roleName = ''
}

const ids = ref<(number | string)[]>([])

const page = ref(1)
const size = ref(20)
const total = ref(0)

const roleList = reactive<{
  data: Role[]
}>({
  data: [],
})

const selected = (rows: Role[]) => {
  ids.value = rows.map((item) => item.id)

  if (ids.value.length === 0) {
    deleteBtnPopoverByIds.value = false
  }
}

const getRoleList = async (currentPage: number, pageSize: number) => {
  const res: any = await roleListApi(currentPage, pageSize)

  if (res.code === 200) {
    const result: RoleListResult = res.result

    roleList.data = result.list || []
    total.value = result.total || 0
  }
}

const pageChange = (currentPage: number) => {
  page.value = currentPage

  getRoleList(page.value, size.value)
}

const sizeChange = (pageSize: number) => {
  size.value = pageSize
  page.value = 1

  getRoleList(page.value, size.value)
}

const deleteBtnPopoverByIds = ref(false)

const deletePopoverVisible = reactive<Record<number | string, boolean>>({})

const deleteRoleFun = async (id?: number | string) => {
  try {
    if (id === 0) {
      if (ids.value.length === 0) {
        deleteBtnPopoverByIds.value = false
        return
      }

      const res: any = await deleteRoleApi(ids.value.join())

      if (res.code === 200) {
        deleteBtnPopoverByIds.value = false
        ids.value = []

        ElMessage.success('角色删除成功')

        await getRoleList(page.value, size.value)
      }

      return
    }

    if (id === undefined || id === null) {
      return
    }

    const res: any = await deleteRoleApi(id)

    if (res.code === 200) {
      deletePopoverVisible[id] = false

      ElMessage.success('角色删除成功')

      await getRoleList(page.value, size.value)
    }
  } catch (error) {
    console.error('删除角色失败：', error)
  }
}

const updateRolePerDialog = ref(false)
const updateRolePerLoading = ref(false)

const roleTreeRef = ref<any>()

const menuList = reactive<{
  data: Menu[]
}>({
  data: [],
})

const roleId = ref<number | string>()

const roleMenu = ref<(number | string)[]>([])
const rolePer = ref<(number | string)[]>([])

const roleAuthProps: RoleAuthProps = {
  children: 'children',
  label: 'menuName',
}

const getMenuApi = async () => {
  const res: any = await allMenuApi(2)

  if (res.code === 200) {
    menuList.data = Array.isArray(res.result) ? res.result : []
  }
}

const getLeafMenuIds = (menus: Menu[]): (number | string)[] => {
  const ids: (number | string)[] = []

  const loop = (list: Menu[]) => {
    list.forEach((item) => {
      if (Array.isArray(item.children) && item.children.length > 0) {
        loop(item.children)
      } else {
        ids.push(item.id)
      }
    })
  }

  loop(menus)

  return ids
}

const selectRolePerListFun = async (id: number | string) => {
  const res: any = await selectRolePerListApi(id)

  if (res.code === 200) {
    const permissionIds = Array.isArray(res.result?.perIds) ? res.result.perIds : []

    roleMenu.value = [...permissionIds]
    rolePer.value = [...permissionIds]
  }
}

const filterRoleMenuIds = (permissionIds: (number | string)[]) => {
  const leafIds = getLeafMenuIds(menuList.data)

  const leafIdSet = new Set(leafIds)

  return permissionIds.filter((id) => leafIdSet.has(id))
}

const loadMenuData = async (id: number | string) => {
  roleId.value = id

  updateRolePerDialog.value = true

  menuList.data = []
  roleMenu.value = []
  rolePer.value = []

  try {
    await Promise.all([getMenuApi(), selectRolePerListFun(id)])

    await nextTick()

    const checkedLeafIds = filterRoleMenuIds(roleMenu.value)

    roleMenu.value = [...checkedLeafIds]

    roleTreeRef.value?.setCheckedKeys(checkedLeafIds, false)

    const checkedKeys = roleTreeRef.value?.getCheckedKeys() || []

    const halfCheckedKeys = roleTreeRef.value?.getHalfCheckedKeys() || []

    rolePer.value = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))
  } catch (error) {
    console.error('加载角色权限失败：', error)
  }
}

const handleCheckChange = (
  data: Menu,
  checked: {
    checkedKeys: (number | string)[]
    halfCheckedKeys: (number | string)[]
  },
) => {
  const checkedKeys = checked.checkedKeys || []

  const halfCheckedKeys = checked.halfCheckedKeys || []

  rolePer.value = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))
}

const updateRolePerFun = async () => {
  if (roleId.value === undefined || roleId.value === null) {
    return
  }

  const checkedKeys = roleTreeRef.value?.getCheckedKeys() || []

  const halfCheckedKeys = roleTreeRef.value?.getHalfCheckedKeys() || []

  const menuIds = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))

  rolePer.value = menuIds

  try {
    updateRolePerLoading.value = true

    const res: any = await updateRolePerApi({
      id: roleId.value,
      menuIds,
    })

    if (res.code === 200) {
      ElMessage.success('角色权限修改成功')

      updateRolePerDialog.value = false
    }
  } catch (error) {
    console.error('角色权限修改失败：', error)
  } finally {
    updateRolePerLoading.value = false

    getRoleList(page.value, size.value)
  }
}

onMounted(() => {
  getRoleList(page.value, size.value)
})
</script>

<style scoped>
.role-page {
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

.role-card {
  width: 98%;
  margin: 10px 0 0 0;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.popover-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 12px;
}

.permission-tree-wrapper {
  height: 300px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px;
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
