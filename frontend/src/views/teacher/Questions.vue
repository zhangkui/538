<template>
  <div class="questions-container">
    <div class="page-header">
      <h1 class="page-title">题目管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建题目
        </el-button>
        <el-button @click="showImportDialog">
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
        placeholder="搜索题目..."
        prefix-icon="Search"
        clearable
        style="width: 280px"
      />
      <el-select v-model="filterDifficulty" placeholder="难度筛选" clearable style="width: 120px">
        <el-option label="简单" value="EASY" />
        <el-option label="中等" value="MEDIUM" />
        <el-option label="困难" value="HARD" />
      </el-select>
    </div>
    
    <div v-loading="loading" class="questions-content">
      <el-table
        :data="filteredQuestions"
        stripe
        style="width: 100%"
        empty-text="暂无题目数据"
      >
        <el-table-column prop="title" label="题目标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="showEditDialog(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100" align="center">
          <template #default="{ row }">
            <span :class="['difficulty-tag', row.difficulty?.toLowerCase()]">
              {{ getDifficultyText(row.difficulty) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="creatorName" label="创建者" width="100" align="center">
          <template #default="{ row }">
            {{ row.creatorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该题目吗？"
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
    
    <!-- 新建/编辑题目弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '新建题目'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="题目标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目标题" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="题目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题目描述"
          />
        </el-form-item>
        
        <el-form-item label="要求说明">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="3"
            placeholder="请输入具体要求（可选）"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="示例输入">
              <el-input
                v-model="form.sampleInput"
                type="textarea"
                :rows="3"
                placeholder="示例输入（可选）"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="示例输出">
              <el-input
                v-model="form.sampleOutput"
                type="textarea"
                :rows="3"
                placeholder="示例输出（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="参考答案">
          <el-input
            v-model="form.referenceAnswer"
            type="textarea"
            :rows="6"
            placeholder="请输入参考答案代码（可选，仅老师可见）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建题目' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 批量导入弹窗 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入题目"
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
              <li>列顺序：标题、描述、要求、示例输入、示例输出、参考答案、分值、难度</li>
              <li>难度可选：EASY/MEDIUM/HARD</li>
            </ul>
          </template>
        </el-alert>
      </div>
      
      <el-upload
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
import { ref, reactive, computed, onMounted } from 'vue'
import { getQuestions, createQuestion, updateQuestion, deleteQuestion, importQuestions } from '@/api/teacher'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const questions = ref([])
const searchKeyword = ref('')
const filterDifficulty = ref('')
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const selectedFile = ref(null)
const formRef = ref(null)

const form = reactive({
  title: '',
  description: '',
  requirements: '',
  sampleInput: '',
  sampleOutput: '',
  referenceAnswer: '',
  score: 10,
  difficulty: 'MEDIUM'
})

const rules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const filteredQuestions = computed(() => {
  return questions.value.filter(q => {
    const matchKeyword = !searchKeyword.value ||
      q.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchDifficulty = !filterDifficulty.value || q.difficulty === filterDifficulty.value
    return matchKeyword && matchDifficulty
  })
})

onMounted(() => {
  loadQuestions()
})

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getQuestions()
    if (res.code === 200) {
      questions.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.requirements = ''
  form.sampleInput = ''
  form.sampleOutput = ''
  form.referenceAnswer = ''
  form.score = 10
  form.difficulty = 'MEDIUM'
}

function showCreateDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function showEditDialog(question) {
  isEdit.value = true
  editId.value = question.id
  Object.assign(form, {
    title: question.title,
    description: question.description,
    requirements: question.requirements || '',
    sampleInput: question.sampleInput || '',
    sampleOutput: question.sampleOutput || '',
    referenceAnswer: question.referenceAnswer || '',
    score: question.score,
    difficulty: question.difficulty
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const res = isEdit.value
      ? await updateQuestion(editId.value, form)
      : await createQuestion(form)
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadQuestions()
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    const res = await deleteQuestion(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadQuestions()
    }
  } catch (error) {
    console.error('删除失败', error)
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
    const res = await importQuestions(selectedFile.value)
    if (res.code === 200) {
      ElMessage.success(res.message || '导入成功')
      importDialogVisible.value = false
      await loadQuestions()
    }
  } finally {
    importing.value = false
  }
}

function downloadTemplate() {
  const templateData = [
    ['标题', '描述', '要求', '示例输入', '示例输出', '参考答案', '分值', '难度'],
    ['实现单例模式', '请编写一个Java类实现单例模式', '1. 确保类只有一个实例\n2. 提供全局访问点', '无', '每次调用返回同一实例', 'public class Singleton {...}', 15, 'MEDIUM']
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '题目模板')
  
  XLSX.writeFile(wb, '题目导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

function getDifficultyText(difficulty) {
  const map = { 'EASY': '简单', 'MEDIUM': '中等', 'HARD': '困难' }
  return map[difficulty] || '未知'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.questions-container {
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
}

.questions-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.difficulty-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.easy { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
  &.medium { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
  &.hard { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
}

.import-tips {
  margin-bottom: 20px;
  
  .tips-list {
    margin: 8px 0 0;
    padding-left: 20px;
    li { line-height: 1.8; }
  }
}

.upload-area {
  width: 100%;
  :deep(.el-upload), :deep(.el-upload-dragger) { width: 100%; }
}
</style>
