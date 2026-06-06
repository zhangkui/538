<template>
  <div class="exam-paper-container">
    <div class="page-header">
      <h1 class="page-title">试卷管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建试卷
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索试卷标题..."
        prefix-icon="Search"
        clearable
        style="width: 280px"
      />
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 140px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
    </div>

    <div v-loading="loading" class="content-card">
      <el-table
        :data="filteredPapers"
        stripe
        style="width: 100%"
        empty-text="暂无试卷数据"
      >
        <el-table-column prop="title" label="试卷标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewSubmissions(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="80" align="center" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="publishStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.publishStatus)" size="small">
              {{ getStatusText(row.publishStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考试时间" width="320" align="center">
          <template #default="{ row }">
            <span v-if="row.startTime || row.endTime">
              {{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="success" link @click="handleCopy(row.id)">复制</el-button>
            <el-button
              v-if="row.publishStatus === 'DRAFT'"
              type="warning"
              link
              @click="handlePublish(row.id)"
            >发布</el-button>
            <el-button
              v-if="row.publishStatus === 'PUBLISHED'"
              type="info"
              link
              @click="handleClose(row.id)"
            >关闭</el-button>
            <el-button type="primary" link @click="viewSubmissions(row)">答卷</el-button>
            <el-popconfirm title="确定要删除该试卷吗？" @confirm="handleDelete(row.id)">
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
      :title="isEdit ? '编辑试卷' : '新建试卷'"
      width="1100px"
      :close-on-click-modal="false"
      class="exam-paper-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="试卷标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入试卷标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number v-model="form.duration" :min="10" :max="300" style="width: 100%" />
              <span style="margin-left: 8px; color: #909399; font-size: 12px">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="试卷描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入试卷描述/说明（可选）"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="作答次数">
              <el-input-number v-model="form.maxAttempts" :min="0" :max="10" style="width: 100%" />
              <span style="margin-left: 8px; color: #909399; font-size: 12px">0表示不限</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="答案可见时机">
              <el-select v-model="form.answerVisible" style="width: 100%">
                <el-option label="始终可见" value="ALWAYS" />
                <el-option label="提交后可见" value="AFTER_SUBMIT" />
                <el-option label="批改后可见" value="AFTER_GRADED" />
                <el-option label="不可见" value="NEVER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="发布班级">
          <el-input
            v-model="form.targetClasses"
            placeholder="留空表示所有班级可见，多个班级用英文逗号分隔，如：计科1班,计科2班"
          />
        </el-form-item>

        <el-divider />

        <el-form-item label="题目列表">
          <div class="question-list-wrapper">
            <div class="question-list-header">
              <span style="font-weight: 600">已选题目（共 {{ form.questions.length }} 题，{{ totalScore }} 分）</span>
              <el-button type="primary" size="small" @click="showQuestionSelector">
                <el-icon><Plus /></el-icon>
                从题库选择
              </el-button>
            </div>

            <el-table
              v-if="form.questions.length > 0"
              :data="form.questions"
              border
              size="small"
              style="width: 100%; margin-top: 12px"
              @sort-change="onQuestionSort"
            >
              <el-table-column label="序号" width="60" align="center">
                <template #default="{ $index }">{{ $index + 1 }}</template>
              </el-table-column>
              <el-table-column label="题型" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="getQuestionTypeTag(row.questionType)" size="small">
                    {{ getQuestionTypeText(row.questionType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="题目标题" min-width="200" />
              <el-table-column label="分值" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.score"
                    :min="1"
                    :max="100"
                    size="small"
                    @change="calculateTotal"
                  />
                </template>
              </el-table-column>
              <el-table-column label="排序" width="140" align="center">
                <template #default="{ $index }">
                  <el-button
                    size="small"
                    :disabled="$index === 0"
                    @click="moveQuestion($index, -1)"
                  >
                    <el-icon><Top /></el-icon>
                  </el-button>
                  <el-button
                    size="small"
                    :disabled="$index === form.questions.length - 1"
                    @click="moveQuestion($index, 1)"
                  >
                    <el-icon><Bottom /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeQuestion($index)">
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty v-else description="还没有添加题目，点击上方按钮从题库选择" />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建试卷' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="questionSelectorVisible"
      title="从题库选择题目"
      width="900px"
    >
      <div class="question-selector">
        <div class="selector-filter">
          <el-input
            v-model="questionSearch"
            placeholder="搜索题目..."
            prefix-icon="Search"
            clearable
            style="width: 240px"
          />
          <el-select v-model="questionTypeFilter" placeholder="题型" clearable style="width: 120px">
            <el-option label="编程题" value="PROGRAMMING" />
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
          </el-select>
        </div>

        <el-table
          v-loading="questionLoading"
          :data="filteredQuestions"
          border
          size="small"
          max-height="400"
          @selection-change="onQuestionSelectionChange"
          row-key="id"
        >
          <el-table-column type="selection" width="50" :selectable="isQuestionSelectable" />
          <el-table-column label="题型" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getQuestionTypeTag(row.questionType)" size="small">
                {{ getQuestionTypeText(row.questionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="题目标题" min-width="200" />
          <el-table-column prop="score" label="原分值" width="80" align="center" />
          <el-table-column label="难度" width="80" align="center">
            <template #default="{ row }">
              {{ getDifficultyText(row.difficulty) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="questionSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddQuestions">
          添加选中（{{ selectedQuestions.length }}）
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="submissionsVisible"
      :title="`查看答卷 - ${currentPaper?.title || ''}`"
      width="1200px"
    >
      <el-table
        v-loading="submissionsLoading"
        :data="paperSubmissions"
        stripe
        size="small"
        empty-text="暂无答卷"
      >
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="attemptNumber" label="第几次" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getSubmissionStatusTag(row.status)" size="small">
              {{ getSubmissionStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'GRADED'" class="score-text">
              {{ row.totalScore }} / {{ row.paperTotalScore }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" align="center">
          <template #default="{ row }">{{ formatDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewSubmissionDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  getExamPapers,
  getExamPaperDetail,
  createExamPaper,
  updateExamPaper,
  copyExamPaper,
  publishExamPaper,
  closeExamPaper,
  deleteExamPaper,
  getPaperSubmissions,
  getSubmissionDetail
} from '@/api/teacherExam'
import { getQuestions } from '@/api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Top, Bottom } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const papers = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  title: '',
  description: '',
  duration: 120,
  startTime: null,
  endTime: null,
  maxAttempts: 1,
  answerVisible: 'AFTER_GRADED',
  targetClasses: '',
  questions: []
})

const rules = {
  title: [{ required: true, message: '请输入试卷标题', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }]
}

const questionSelectorVisible = ref(false)
const questionLoading = ref(false)
const questionSearch = ref('')
const questionTypeFilter = ref('')
const questionBank = ref([])
const selectedQuestions = ref([])

const submissionsVisible = ref(false)
const submissionsLoading = ref(false)
const currentPaper = ref(null)
const paperSubmissions = ref([])

const filteredPapers = computed(() => {
  return papers.value.filter(p => {
    const matchKeyword = !searchKeyword.value ||
      p.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchStatus = !filterStatus.value || p.publishStatus === filterStatus.value
    return matchKeyword && matchStatus
  })
})

const totalScore = computed(() => {
  return form.questions.reduce((sum, q) => sum + (q.score || 0), 0)
})

const filteredQuestions = computed(() => {
  return questionBank.value.filter(q => {
    const matchKeyword = !questionSearch.value ||
      q.title.toLowerCase().includes(questionSearch.value.toLowerCase())
    const matchType = !questionTypeFilter.value || q.questionType === questionTypeFilter.value
    return matchKeyword && matchType
  })
})

onMounted(() => {
  loadPapers()
  loadQuestionBank()
})

async function loadPapers() {
  loading.value = true
  try {
    const res = await getExamPapers()
    if (res.code === 200) {
      papers.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

async function loadQuestionBank() {
  questionLoading.value = true
  try {
    const res = await getQuestions()
    if (res.code === 200) {
      questionBank.value = res.data || []
    }
  } finally {
    questionLoading.value = false
  }
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.duration = 120
  form.startTime = null
  form.endTime = null
  form.maxAttempts = 1
  form.answerVisible = 'AFTER_GRADED'
  form.targetClasses = ''
  form.questions = []
}

function showCreateDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

async function showEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  const res = await getExamPaperDetail(row.id)
  if (res.code === 200 && res.data) {
    const p = res.data
    form.title = p.title
    form.description = p.description || ''
    form.duration = p.duration || 120
    form.startTime = p.startTime || null
    form.endTime = p.endTime || null
    form.maxAttempts = p.maxAttempts || 1
    form.answerVisible = p.answerVisible || 'AFTER_GRADED'
    form.targetClasses = p.targetClasses || ''
    form.questions = (p.questions || []).map(q => ({
      id: q.id,
      questionId: q.questionId,
      questionOrder: q.questionOrder,
      score: q.score,
      title: q.title,
      description: q.description,
      requirements: q.requirements,
      sampleInput: q.sampleInput,
      sampleOutput: q.sampleOutput,
      referenceAnswer: q.referenceAnswer,
      difficulty: q.difficulty,
      questionType: q.questionType,
      options: q.options,
      correctAnswer: q.correctAnswer,
      answerExplanation: q.answerExplanation
    }))
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  let valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.questions.length === 0) {
    ElMessage.warning('请至少添加一道题目')
    return
  }

  submitting.value = true
  try {
    const submitData = {
      ...form,
      questions: form.questions.map((q, idx) => ({
        ...q,
        questionOrder: idx + 1
      }))
    }

    const res = isEdit.value
      ? await updateExamPaper(editId.value, submitData)
      : await createExamPaper(submitData)

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadPapers()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

async function handleCopy(id) {
  try {
    const res = await copyExamPaper(id)
    if (res.code === 200) {
      ElMessage.success('复制成功')
      await loadPapers()
    } else {
      ElMessage.error(res.message || '复制失败')
    }
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

async function handlePublish(id) {
  try {
    await ElMessageBox.confirm('确定要发布该试卷吗？发布后学生即可看到。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await publishExamPaper(id)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      await loadPapers()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (e) {}
}

async function handleClose(id) {
  try {
    await ElMessageBox.confirm('确定要关闭该试卷吗？关闭后学生将无法再作答。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await closeExamPaper(id)
    if (res.code === 200) {
      ElMessage.success('关闭成功')
      await loadPapers()
    }
  } catch (e) {}
}

async function handleDelete(id) {
  try {
    const res = await deleteExamPaper(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadPapers()
    }
  } catch (e) {}
}

function showQuestionSelector() {
  selectedQuestions.value = []
  questionSelectorVisible.value = true
}

function isQuestionSelectable(row) {
  return !form.questions.find(q => q.questionId === row.id)
}

function onQuestionSelectionChange(selection) {
  selectedQuestions.value = selection
}

function confirmAddQuestions() {
  for (const q of selectedQuestions.value) {
    if (!form.questions.find(fq => fq.questionId === q.id)) {
      form.questions.push({
        questionId: q.id,
        score: q.score || 10,
        title: q.title,
        description: q.description,
        requirements: q.requirements,
        sampleInput: q.sampleInput,
        sampleOutput: q.sampleOutput,
        referenceAnswer: q.referenceAnswer,
        difficulty: q.difficulty,
        questionType: q.questionType,
        options: q.options,
        correctAnswer: q.correctAnswer,
        answerExplanation: q.answerExplanation
      })
    }
  }
  calculateTotal()
  questionSelectorVisible.value = false
  ElMessage.success(`已添加 ${selectedQuestions.value.length} 道题目`)
}

function removeQuestion(index) {
  form.questions.splice(index, 1)
  calculateTotal()
}

function moveQuestion(index, direction) {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= form.questions.length) return
  const temp = form.questions[index]
  form.questions[index] = form.questions[newIndex]
  form.questions[newIndex] = temp
}

function onQuestionSort() {}

function calculateTotal() {}

async function viewSubmissions(row) {
  currentPaper.value = row
  submissionsLoading.value = true
  paperSubmissions.value = []
  try {
    const res = await getPaperSubmissions(row.id)
    if (res.code === 200) {
      paperSubmissions.value = res.data || []
    }
  } finally {
    submissionsLoading.value = false
  }
  submissionsVisible.value = true
}

function viewSubmissionDetail(row) {
  if (row.status === 'PENDING_GRADING' || row.status === 'GRADED') {
    router.push(`/teacher/exam-grading/${row.id}`)
  } else {
    router.push(`/teacher/exam-submissions/${row.id}`)
  }
}

function getStatusText(status) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭' }
  return map[status] || '未知'
}

function getStatusTag(status) {
  const map = { DRAFT: 'info', PUBLISHED: 'success', CLOSED: 'danger' }
  return map[status] || ''
}

function getSubmissionStatusText(status) {
  const map = { DRAFT: '草稿', SUBMITTED: '已提交', PENDING_GRADING: '待批改', GRADED: '已批改' }
  return map[status] || '未知'
}

function getSubmissionStatusTag(status) {
  const map = { DRAFT: 'info', SUBMITTED: 'primary', PENDING_GRADING: 'warning', GRADED: 'success' }
  return map[status] || ''
}

function getQuestionTypeText(type) {
  const map = { PROGRAMMING: '编程题', SINGLE_CHOICE: '单选题', MULTIPLE_CHOICE: '多选题' }
  return map[type] || '编程题'
}

function getQuestionTypeTag(type) {
  const map = { PROGRAMMING: '', SINGLE_CHOICE: 'warning', MULTIPLE_CHOICE: 'success' }
  return map[type] || ''
}

function getDifficultyText(d) {
  const map = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }
  return map[d] || '未知'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.exam-paper-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
  .header-actions { display: flex; gap: 12px; }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-list-wrapper {
  width: 100%;
  .question-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
  }
}

.selector-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.score-text {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

:deep(.exam-paper-dialog) {
  .el-dialog__body { max-height: 70vh; overflow-y: auto; }
}
</style>
