<template>
  <div class="role-page">
    <div class="title_style">
      <span>角色管理</span>
    </div>

    <el-card class="role-card">
      <!-- ============================== -->
      <!-- 工具栏 -->
      <!-- ============================== -->

      <div class="toolbar">
        <!-- 创建 -->
        <el-button type="primary" plain @click="openCreateDialog"> 创建 </el-button>

        <!-- 批量删除 -->
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

      <!-- ============================== -->
      <!-- 角色列表 -->
      <!-- ============================== -->

      <el-table
        :data="roleList.data"
        stripe
        style="width: 100%; height: calc(100vh - 328px)"
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
            <!-- 修改角色权限 -->
            <el-button
              style="margin-left: 0"
              size="small"
              text
              title="修改角色权限"
              @click="loadMenuData(row.id)"
            >
              <MyIcon type="icon-edit" />
            </el-button>

            <!-- 单个删除 -->
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

      <!-- ============================== -->
      <!-- 分页 -->
      <!-- ============================== -->

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

    <!-- ============================== -->
    <!-- 修改角色权限 -->
    <!-- ============================== -->

    <el-dialog v-model="updateRolePerDialog" title="角色权限修改" width="30%" destroy-on-close>
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

    <!-- ============================== -->
    <!-- 创建角色 -->
    <!-- ============================== -->

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
          <el-input
            v-model="roleDate.roleCode"
            autocomplete="off"
            style="width: 80%; max-width: 250px"
          />
        </el-form-item>

        <el-form-item label="角色名称：" prop="roleName">
          <el-input
            v-model="roleDate.roleName"
            autocomplete="off"
            style="width: 80%; max-width: 250px"
          />
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

/**
 * ==============================
 * 类型定义
 * ==============================
 */

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

/**
 * ==============================
 * Icon
 * ==============================
 */

const { MyIcon } = icon()

/**
 * ==============================
 * 创建角色
 * ==============================
 */

const roleCreateDialog = ref(false)

const createRoleFormRef = ref<FormInstance>()

const createRoleLoading = ref(false)

const roleDate = reactive({
  roleCode: '',
  roleName: '',
})

/**
 * 角色编码校验
 */
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

/**
 * 创建角色校验规则
 */
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

/**
 * 打开创建角色
 */
const openCreateDialog = () => {
  roleDate.roleCode = ''
  roleDate.roleName = ''

  roleCreateDialog.value = true
}

/**
 * 创建角色
 */
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

/**
 * 重置创建表单
 */
const resetCreateRoleForm = () => {
  createRoleFormRef.value?.resetFields()

  roleDate.roleCode = ''
  roleDate.roleName = ''
}

/**
 * ==============================
 * 角色列表
 * ==============================
 */

const ids = ref<(number | string)[]>([])

const page = ref(1)

const size = ref(20)

const total = ref(0)

const roleList = reactive<{
  data: Role[]
}>({
  data: [],
})

/**
 * 选中角色
 */
const selected = (rows: Role[]) => {
  ids.value = rows.map((item) => item.id)

  if (ids.value.length === 0) {
    deleteBtnPopoverByIds.value = false
  }
}

/**
 * 查询角色列表
 */
const getRoleList = async (currentPage: number, pageSize: number) => {
  const res: any = await roleListApi(currentPage, pageSize)

  if (res.code === 200) {
    const result: RoleListResult = res.result

    roleList.data = result.list || []

    total.value = result.total || 0
  }
}

/**
 * 页码改变
 */
const pageChange = (currentPage: number) => {
  page.value = currentPage

  getRoleList(page.value, size.value)
}

/**
 * 每页数量改变
 */
const sizeChange = (pageSize: number) => {
  size.value = pageSize

  page.value = 1

  getRoleList(page.value, size.value)
}

/**
 * ==============================
 * 删除角色
 * ==============================
 */

const deleteBtnPopoverByIds = ref(false)

/**
 * 每个角色独立的 Popover
 */
const deletePopoverVisible = reactive<Record<number | string, boolean>>({})

/**
 * 打开批量删除确认框
 */
const openBatchDeletePopover = () => {
  if (ids.value.length === 0) {
    deleteBtnPopoverByIds.value = false

    return
  }

  deleteBtnPopoverByIds.value = true
}

/**
 * 删除角色
 */
const deleteRoleFun = async (id?: number | string) => {
  try {
    /**
     * ==========================
     * 批量删除
     * ==========================
     */

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

    /**
     * ==========================
     * 单个删除
     * ==========================
     */

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

/**
 * ==============================
 * 角色权限
 * ==============================
 */

const updateRolePerDialog = ref(false)

const updateRolePerLoading = ref(false)

/**
 * 权限树
 */
const roleTreeRef = ref<any>()

/**
 * 所有菜单
 */
const menuList = reactive<{
  data: Menu[]
}>({
  data: [],
})

/**
 * 当前角色 ID
 */
const roleId = ref<number | string>()

/**
 * 当前角色实际拥有的
 * 叶子菜单权限
 */
const roleMenu = ref<(number | string)[]>([])

/**
 * 当前提交权限
 */
const rolePer = ref<(number | string)[]>([])

/**
 * Tree 配置
 */
const roleAuthProps: RoleAuthProps = {
  children: 'children',
  label: 'menuName',
}

/**
 * ==============================
 * 获取所有菜单
 * ==============================
 */

const getMenuApi = async () => {
  const res: any = await allMenuApi(2)

  if (res.code === 200) {
    menuList.data = Array.isArray(res.result) ? res.result : []
  }
}

/**
 * ==============================
 * 根据菜单树获取所有叶子节点 ID
 * ==============================
 *
 * 这里是这次修复的核心。
 *
 * 有 children 的节点：
 *     属于父菜单 / 目录
 *
 * 没有 children 的节点：
 *     才是真正可以直接勾选的菜单。
 *
 * 我们不能把父菜单 ID 直接
 * 传给 setCheckedKeys。
 */

const getLeafMenuIds = (menus: Menu[]): (number | string)[] => {
  const ids: (number | string)[] = []

  const loop = (list: Menu[]) => {
    list.forEach((item) => {
      if (Array.isArray(item.children) && item.children.length > 0) {
        /**
         * 父菜单
         *
         * 不加入 checkedKeys。
         *
         * 继续处理子菜单。
         */
        loop(item.children)
      } else {
        /**
         * 叶子菜单
         */
        ids.push(item.id)
      }
    })
  }

  loop(menus)

  return ids
}

/**
 * ==============================
 * 获取角色已有权限
 * ==============================
 */

const selectRolePerListFun = async (id: number | string) => {
  const res: any = await selectRolePerListApi(id)

  if (res.code === 200) {
    const permissionIds = Array.isArray(res.result?.perIds) ? res.result.perIds : []

    /**
     * 这里只先保存接口返回值。
     *
     * 真正设置 Tree 时，
     * 还会根据菜单树过滤一次。
     */
    roleMenu.value = [...permissionIds]

    rolePer.value = [...permissionIds]
  }
}

/**
 * ==============================
 * 过滤角色权限
 * ==============================
 *
 * 只保留当前菜单树中真实存在的
 * 叶子菜单 ID。
 *
 * 这样即使接口中还存在：
 *
 * 父菜单 ID
 *
 * 也不会因为 setCheckedKeys()
 * 导致整个父菜单下面的子菜单
 * 全部被选中。
 */

const filterRoleMenuIds = (permissionIds: (number | string)[]) => {
  /**
   * 当前菜单树所有叶子节点
   */
  const leafIds = getLeafMenuIds(menuList.data)

  /**
   * Set 方便快速判断
   */
  const leafIdSet = new Set(leafIds)

  /**
   * 只保留叶子节点
   */
  return permissionIds.filter((id) => leafIdSet.has(id))
}

/**
 * ==============================
 * 打开角色权限
 * ==============================
 */

const loadMenuData = async (id: number | string) => {
  roleId.value = id

  updateRolePerDialog.value = true

  /**
   * 清理旧数据
   */
  menuList.data = []

  roleMenu.value = []

  rolePer.value = []

  try {
    /**
     * 同时加载：
     *
     * 1. 菜单树
     * 2. 角色权限
     */
    await Promise.all([getMenuApi(), selectRolePerListFun(id)])

    /**
     * 等待 Tree 渲染
     */
    await nextTick()

    /**
     * ==========================
     * 关键处理
     * ==========================
     *
     * 接口返回：
     *
     * [父菜单ID, 子菜单ID]
     *
     * 只保留：
     *
     * [子菜单ID]
     *
     * 父菜单不要直接勾选。
     *
     * Element Plus 会自动根据
     * 子菜单计算父菜单状态。
     */
    const checkedLeafIds = filterRoleMenuIds(roleMenu.value)

    /**
     * 更新当前真正的 Tree 勾选数据
     */
    roleMenu.value = [...checkedLeafIds]

    /**
     * 设置 Tree 勾选状态
     */
    roleTreeRef.value?.setCheckedKeys(checkedLeafIds, false)

    /**
     * 同步一次当前状态。
     *
     * 这样 rolePer 中也保持
     * 当前 Tree 的真实状态。
     */
    const checkedKeys = roleTreeRef.value?.getCheckedKeys() || []

    const halfCheckedKeys = roleTreeRef.value?.getHalfCheckedKeys() || []

    rolePer.value = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))
  } catch (error) {
    console.error('加载角色权限失败：', error)
  }
}

/**
 * ==============================
 * 权限树变化
 * ==============================
 *
 * 不需要手动计算父菜单半选。
 *
 * Element Plus 默认父子联动：
 *
 * 子菜单全部选中
 * → 父菜单全选
 *
 * 子菜单部分选中
 * → 父菜单半选
 *
 * 子菜单全部取消
 * → 父菜单取消
 */

const handleCheckChange = (
  data: Menu,
  checked: {
    checkedKeys: (number | string)[]

    halfCheckedKeys: (number | string)[]
  },
) => {
  const checkedKeys = checked.checkedKeys || []

  const halfCheckedKeys = checked.halfCheckedKeys || []

  /**
   * 保存：
   *
   * 完整选中节点
   * +
   * 半选父节点
   */
  rolePer.value = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))
}

/**
 * ==============================
 * 提交角色权限
 * ==============================
 */

const updateRolePerFun = async () => {
  if (roleId.value === undefined || roleId.value === null) {
    return
  }

  /**
   * 获取当前完整选中节点
   */
  const checkedKeys = roleTreeRef.value?.getCheckedKeys() || []

  /**
   * 获取当前半选节点
   */
  const halfCheckedKeys = roleTreeRef.value?.getHalfCheckedKeys() || []

  /**
   * 最终提交：
   *
   * 完整选中
   * +
   * 半选父目录
   */
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

/**
 * ==============================
 * 初始化
 * ==============================
 */

onMounted(() => {
  getRoleList(page.value, size.value)
})
</script>

<style scoped>
/* =========================================================
   页面
   ========================================================= */

.role-page {
  width: 100%;
}

/* =========================================================
   标题
   ========================================================= */

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

/* =========================================================
   Card
   ========================================================= */

.role-card {
  width: 95%;

  margin: 18px 2%;
}

/* =========================================================
   工具栏
   ========================================================= */

.toolbar {
  display: flex;
  align-items: center;

  gap: 10px;

  margin-bottom: 12px;
}

/* =========================================================
   Popover 底部
   ========================================================= */

.popover-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;

  gap: 8px;
}

/* =========================================================
   分页
   ========================================================= */

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;

  margin: 20px 0 50px;
}

/* =========================================================
   权限树
   ========================================================= */

.permission-tree-wrapper {
  height: 300px;

  overflow-y: auto;
  overflow-x: hidden;

  padding: 4px;
}

/* =========================================================
   Dialog Footer
   ========================================================= */

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;

  gap: 10px;
}
</style>
