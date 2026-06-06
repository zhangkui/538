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
      <el-select v-model="filterType" placeholder="题型筛选" clearable style="width: 140px">
        <el-option label="编程题" value="PROGRAMMING" />
        <el-option label="单选题" value="SINGLE_CHOICE" />
        <el-option label="多选题" value="MULTIPLE_CHOICE" />
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
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getQuestionTypeTag(row.questionType)" size="small">
              {{ getQuestionTypeText(row.questionType) }}
            </el-tag>
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
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="题型" prop="questionType">
              <el-radio-group v-model="form.questionType">
                <el-radio value="PROGRAMMING">编程题</el-radio>
                <el-radio value="SINGLE_CHOICE">单选题</el-radio>
                <el-radio value="MULTIPLE_CHOICE">多选题</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
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
        
        <template v-if="form.questionType === 'PROGRAMMING'">
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
        </template>
        
        <template v-else>
          <el-form-item label="选项">
            <div class="options-list">
              <div
                v-for="(opt, idx) in optionList"
                :key="idx"
                class="option-item"
              >
                <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                <el-input
                  v-model="optionList[idx]"
                  :placeholder="`请输入选项 ${String.fromCharCode(65 + idx)} 的内容`"
                  @input="updateOptions"
                />
                <el-checkbox
                  v-if="form.questionType === 'SINGLE_CHOICE'"
                  v-model="singleCorrect"
                  :label="String.fromCharCode(65 + idx)"
                  @change="onSingleCorrectChange(String.fromCharCode(65 + idx))"
                >正确</el-checkbox>
                <el-checkbox
                  v-else
                  v-model="multiCorrect"
                  :label="String.fromCharCode(65 + idx)"
                  @change="updateCorrectAnswer"
                >正确</el-checkbox>
                <el-button
                  v-if="optionList.length > 2"
                  type="danger"
                  link
                  @click="removeOption(idx)"
                >删除</el-button>
              </div>
            </div>
            <el-button type="primary" plain size="small" @click="addOption">
              <el-icon><Plus /></el-icon>
              添加选项
            </el-button>
          </el-form-item>
          
          <el-form-item label="正确答案" prop="correctAnswer">
            <el-input
              v-model="form.correctAnswer"
              placeholder="单选如：A，多选如：A,C,D"
            />
            <div class="form-tip">
              <template v-if="form.questionType === 'SINGLE_CHOICE'">
                单选题只能有 1 个正确答案
              </template>
              <template v-else>
                多选题必须有 2 个及以上正确答案，用英文逗号分隔
              </template>
            </div>
          </el-form-item>
          
          <el-form-item label="答案解析">
            <el-input
              v-model="form.answerExplanation"
              type="textarea"
              :rows="4"
              placeholder="请输入答案解析/讲解（学生提交后可见）"
            />
          </el-form-item>
        </template>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建题目' }}
        </el-button>
      </template>
    </el-dialog>
    
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
              <li>列顺序：标题、描述、题型、选项、正确答案、答案解析、要求、示例输入、示例输出、参考答案、分值、难度</li>
              <li>题型可选：PROGRAMMING/SINGLE_CHOICE/MULTIPLE_CHOICE</li>
              <li>选项格式：JSON数组，如["A.选项1","B.选项2"]</li>
              <li>正确答案：单选如A，多选如A,C,D</li>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { getQuestions, createQuestion, updateQuestion, deleteQuestion, importQuestions } from '@/api/teacher'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const questions = ref([])
const searchKeyword = ref('')
const filterDifficulty = ref('')
const filterType = ref('')
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const selectedFile = ref(null)
const formRef = ref(null)

const optionList = ref([])
const singleCorrect = ref('')
const multiCorrect = ref([])

const form = reactive({
  title: '',
  description: '',
  requirements: '',
  sampleInput: '',
  sampleOutput: '',
  referenceAnswer: '',
  score: 10,
  difficulty: 'MEDIUM',
  questionType: 'PROGRAMMING',
  options: '',
  correctAnswer: '',
  answerExplanation: ''
})

const rules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  correctAnswer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }]
}

const filteredQuestions = computed(() => {
  return questions.value.filter(q => {
    const matchKeyword = !searchKeyword.value ||
      q.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchDifficulty = !filterDifficulty.value || q.difficulty === filterDifficulty.value
    const matchType = !filterType.value || q.questionType === filterType.value
    return matchKeyword && matchDifficulty && matchType
  })
})

watch(() => form.questionType, () => {
  form.correctAnswer = ''
  singleCorrect.value = ''
  multiCorrect.value = []
  if (form.questionType !== 'PROGRAMMING' && optionList.value.length === 0) {
    optionList.value = ['', '', '', '']
  }
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
  form.questionType = 'PROGRAMMING'
  form.options = ''
  form.correctAnswer = ''
  form.answerExplanation = ''
  optionList.value = []
  singleCorrect.value = ''
  multiCorrect.value = []
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
    difficulty: question.difficulty,
    questionType: question.questionType || 'PROGRAMMING',
    options: question.options || '',
    correctAnswer: question.correctAnswer || '',
    answerExplanation: question.answerExplanation || ''
  })
  
  if (question.options) {
    try {
      optionList.value = JSON.parse(question.options).map(o => {
        const m = o.match(/^[A-Z]\.\s*(.*)$/)
        return m ? m[1] : o
      })
    } catch (e) {
      optionList.value = ['', '', '', '']
    }
  } else {
    optionList.value = ['', '', '', '']
  }
  
  if (question.correctAnswer) {
    if (form.questionType === 'SINGLE_CHOICE') {
      singleCorrect.value = question.correctAnswer
    } else {
      multiCorrect.value = question.correctAnswer.split(',').map(s => s.trim())
    }
  }
  
  dialogVisible.value = true
}

async function handleSubmit() {
  let valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (form.questionType !== 'PROGRAMMING') {
    const validOpts = optionList.value.filter(o => o && o.trim())
    if (validOpts.length < 2) {
      ElMessage.error('选择题至少需要2个选项')
      return
    }
    
    if (form.questionType === 'SINGLE_CHOICE') {
      if (!form.correctAnswer || form.correctAnswer.split(',').length !== 1) {
        ElMessage.error('单选题只能有1个正确答案')
        return
      }
    } else if (form.questionType === 'MULTIPLE_CHOICE') {
      if (!form.correctAnswer || form.correctAnswer.split(',').length < 2) {
        ElMessage.error('多选题必须有2个及以上正确答案')
        return
      }
    }
  }
  
  submitting.value = true
  try {
    const submitData = { ...form }
    if (form.questionType !== 'PROGRAMMING') {
      const opts = optionList.value
        .filter(o => o && o.trim())
        .map((o, i) => `${String.fromCharCode(65 + i)}. ${o.trim()}`)
      submitData.options = JSON.stringify(opts)
    }
    
    const res = isEdit.value
      ? await updateQuestion(editId.value, submitData)
      : await createQuestion(submitData)
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadQuestions()
    } else {
      ElMessage.error(res.message || '操作失败')
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

function addOption() {
  if (optionList.value.length < 8) {
    optionList.value.push('')
  } else {
    ElMessage.warning('最多支持8个选项')
  }
}

function removeOption(idx) {
  optionList.value.splice(idx, 1)
  updateOptions()
}

function updateOptions() {
  const opts = optionList.value
    .filter(o => o && o.trim())
    .map((o, i) => `${String.fromCharCode(65 + i)}. ${o.trim()}`)
  form.options = JSON.stringify(opts)
}

function onSingleCorrectChange(val) {
  singleCorrect.value = val
  form.correctAnswer = val
}

function updateCorrectAnswer() {
  form.correctAnswer = multiCorrect.value.join(',')
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
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } finally {
    importing.value = false
  }
}

function downloadTemplate() {
  const templateData = [
    ['标题', '描述', '题型', '选项', '正确答案', '答案解析', '要求', '示例输入', '示例输出', '参考答案', '分值', '难度'],
    ['实现单例模式', '请编写一个Java类实现单例模式', 'PROGRAMMING', '', '', '', '1. 确保类只有一个实例', '无', '同一实例', 'public class Singleton...', 15, 'MEDIUM'],
    ['Java中哪个关键字用于定义常量？', '选择正确的关键字', 'SINGLE_CHOICE', '["A. static","B. final","C. const","D. abstract"]', 'B', 'final用于定义常量', '', '', '', '', 5, 'EASY'],
    ['以下哪些是Java的基本数据类型？', '选择所有基本类型', 'MULTIPLE_CHOICE', '["A. int","B. String","C. boolean","D. Integer","E. double"]', 'A,C,E', '基本类型有8种', '', '', '', '', 5, 'EASY']
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

function getQuestionTypeText(type) {
  const map = { 'PROGRAMMING': '编程题', 'SINGLE_CHOICE': '单选题', 'MULTIPLE_CHOICE': '多选题' }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = { 'PROGRAMMING': '', 'SINGLE_CHOICE': 'warning', 'MULTIPLE_CHOICE': 'success' }
  return map[type] || ''
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

.options-list {
  width: 100%;
  margin-bottom: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  
  .option-label {
    font-weight: 600;
    color: #303133;
    width: 24px;
    flex-shrink: 0;
  }
  
  .el-input {
    flex: 1;
  }
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
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
