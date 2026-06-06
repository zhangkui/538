<template>
  <div class="students-container">
    <div class="page-header">
      <h1 class="page-title">学生管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="downloadTemplate">
          <el-icon><Download /></el-icon>
          下载模板
        </el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索学号/姓名..."
        prefix-icon="Search"
        clearable
        style="width: 280px"
      />
      <el-select v-model="filterClass" placeholder="按班级筛选" clearable style="width: 180px">
        <el-option v-for="cls in classList" :key="cls" :label="cls" :value="cls" />
      </el-select>
    </div>
    
    <div v-loading="loading" class="students-content">
      <el-table
        :data="filteredStudents"
        stripe
        style="width: 100%"
        empty-text="暂无学生数据"
      >
        <el-table-column prop="username" label="学号" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="className" label="班级" min-width="150">
          <template #default="{ row }">
            {{ row.className || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="needChangePwd" label="密码状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.needChangePwd ? 'warning' : 'success'" size="small">
              {{ row.needChangePwd ? '待修改' : '已修改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确定要删除该学生吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 批量导入弹窗 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入学生"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="import-tips">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>请使用Excel文件（.xlsx格式）</li>
              <li>文件格式：学号、姓名、班级（第一行为标题）</li>
              <li>学生初始密码为：123456</li>
              <li>学生首次登录需要修改密码</li>
            </ul>
          </template>
        </el-alert>
      </div>
      
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xlsx/.xls 格式的Excel文件
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStudents, importStudents, deleteStudent } from '@/api/teacher'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const loading = ref(false)
const importing = ref(false)
const students = ref([])
const searchKeyword = ref('')
const filterClass = ref('')
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const selectedFile = ref(null)

const classList = computed(() => {
  const classes = new Set()
  students.value.forEach(s => {
    if (s.className) classes.add(s.className)
  })
  return Array.from(classes)
})

const filteredStudents = computed(() => {
  return students.value.filter(s => {
    const matchKeyword = !searchKeyword.value ||
      s.username?.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      s.realName?.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchClass = !filterClass.value || s.className === filterClass.value
    return matchKeyword && matchClass
  })
})

onMounted(() => {
  loadStudents()
})

async function loadStudents() {
  loading.value = true
  try {
    const res = await getStudents()
    if (res.code === 200) {
      students.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

function showImportDialog() {
  selectedFile.value = null
  importDialogVisible.value = true
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件')
}

async function handleImport() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  importing.value = true
  try {
    const res = await importStudents(selectedFile.value)
    if (res.code === 200) {
      ElMessage.success(res.message || '导入成功')
      importDialogVisible.value = false
      await loadStudents()
    }
  } finally {
    importing.value = false
  }
}

async function handleDelete(id) {
  try {
    const res = await deleteStudent(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadStudents()
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

function downloadTemplate() {
  const templateData = [
    ['学号', '姓名', '班级'],
    ['2024001', '张三', '计算机2401班'],
    ['2024002', '李四', '计算机2401班'],
    ['2024003', '王五', '计算机2402班']
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '学生模板')
  
  // 设置列宽
  ws['!cols'] = [{ wch: 15 }, { wch: 12 }, { wch: 20 }]
  
  XLSX.writeFile(wb, '学生导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.students-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.students-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.import-tips {
  margin-bottom: 20px;
  
  .tips-list {
    margin: 8px 0 0;
    padding-left: 20px;
    
    li {
      line-height: 1.8;
    }
  }
}

.upload-area {
  width: 100%;
  
  :deep(.el-upload) {
    width: 100%;
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-bar {
    flex-direction: column;
    
    .el-input, .el-select {
      width: 100% !important;
    }
  }
}
</style>
