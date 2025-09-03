<template>
  <div>
    <div class="title_style">
      <span>定时任务</span>
    </div>
    <el-card style="margin: 18px 2%; width: 95%">
      <el-table stripe style="width: 100%; height: calc(100vh - 296px)" @selection-change="">
        <el-table-column type="selection" width="55"> </el-table-column>
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="createBy" label="创建用户" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column prop="updateBy" label="修改用户" />
        <el-table-column prop="updateTime" label="修改时间" />
        <el-table-column fixed="right" label="操作" width="100">
          <template #default="scope">
            <el-button style="margin-left: 0" @click="" size="small" text title="修改角色信息">
              <MyIcon type="icon-edit" />
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin: 20px 0 50px 0">
        <el-pagination
          background
          v-model:page-size="page"
          :page-sizes="[10, 20, 50, 100]"
          style="float: right"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="1"
          @size-change="size"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

let { page, size, total } = taskFn()

/**
 * 页面初始化
 */
onMounted(() => {})

/**
 * 角色删改查接口方法合集
 */
function taskFn(): any {
  // 勾选角色id（删除使用）
  let ids = reactive([])

  // 分页数据 （查询页面）
  let page = ref<number>(1)
  let size = ref<number>(20)
  let total = ref<number>(0)

  return {
    page,
    size,
    total,
  }
}
</script>

<style scoped>
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
</style>
