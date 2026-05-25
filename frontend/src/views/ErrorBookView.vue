<template>
  <div class="error-book-page">
    <h2>错题本</h2>

    <a-card size="small" style="margin-bottom: 16px;">
      <a-row :gutter="12" align="middle">
        <a-col :span="3">
          <a-select v-model:value="filterErrorType" placeholder="错误类型" allow-clear @change="loadErrors">
            <a-select-option :value="1">概念错误</a-select-option>
            <a-select-option :value="2">计算错误</a-select-option>
            <a-select-option :value="3">思路错误</a-select-option>
            <a-select-option :value="4">审题错误</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="3">
          <a-input v-model:value="filterKnowledgePoint" placeholder="知识点" allow-clear @change="loadErrors" />
        </a-col>
        <a-col :span="3">
          <a-select v-model:value="filterReviewStatus" placeholder="复习状态" allow-clear @change="loadErrors">
            <a-select-option :value="0">未复习</a-select-option>
            <a-select-option :value="1">复习中</a-select-option>
            <a-select-option :value="2">已掌握</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="3">
          <a-select v-model:value="sortBy" @change="loadErrors">
            <a-select-option value="lastErrorTime">最近错误</a-select-option>
            <a-select-option value="firstErrorTime">首次错误</a-select-option>
            <a-select-option value="errorCount">错误次数</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-space>
            <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
              <a-radio-button value="list">列表</a-radio-button>
              <a-radio-button value="charts">图表</a-radio-button>
              <a-radio-button value="recommend">推荐</a-radio-button>
              <a-radio-button value="groups">分组</a-radio-button>
            </a-radio-group>
            <a-dropdown>
              <a-button size="small">导出 <DownOutlined /></a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="preview" @click="handlePreview">在线预览</a-menu-item>
                  <a-menu-item key="csv" @click="handleExportExcel">导出CSV</a-menu-item>
                  <a-menu-item key="word" @click="handleExportWord">导出Word</a-menu-item>
                  <a-menu-item key="pdf" @click="handleExportPdf">导出PDF</a-menu-item>
                  <a-menu-item key="batch" @click="handleBatchExport">批量导出ZIP</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-button size="small" type="primary" @click="showAssemblyModal = true">强化组卷</a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <template v-if="viewMode === 'list'">
      <div style="margin-bottom:8px;">
        <a-space>
          <a-button size="small" danger :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</a-button>
          <a-button size="small" :disabled="selectedIds.length===0" @click="handleBatchReview(1)">批量标复习中</a-button>
          <a-button size="small" :disabled="selectedIds.length===0" @click="handleBatchReview(2)">批量标已掌握</a-button>
        </a-space>
      </div>
      <a-table :columns="columns" :data-source="errorList" :loading="loading"
        :pagination="pagination" @change="handleTableChange" row-key="id" size="small"
        :row-selection="{ selectedRowKeys: selectedIds, onChange: (keys: any) => selectedIds = keys }"
        :expanded-row-keys="expandedRowKeys" @expand="handleExpandRow">
        <template #expandedRowRender="{ record }">
          <div v-if="expandedQuestions[record.questionId]" class="question-detail">
            <div class="q-content"><strong>题干：</strong><span v-html="renderQContent(expandedQuestions[record.questionId].content || '')"></span></div>
            <div v-if="expandedOptions(record.questionId).length" class="q-options">
              <strong>选项：</strong>
              <div v-for="opt in expandedOptions(record.questionId)" :key="opt.key" class="q-opt-item">
                {{ opt.key }}. {{ opt.text }}
              </div>
            </div>
            <div class="q-answer"><strong>正确答案：</strong>{{ expandedQuestions[record.questionId].answer }}</div>
            <div v-if="expandedQuestions[record.questionId].analysis" class="q-analysis"><strong>解析：</strong>{{ expandedQuestions[record.questionId].analysis }}</div>
          </div>
          <a-spin v-else size="small" />
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'errorType'">
            <a-tag :color="getErrorTypeColor(record.errorType)">{{ getErrorTypeName(record.errorType) }}</a-tag>
          </template>
          <template v-if="column.key === 'reviewStatus'">
            <a-select v-model:value="record.reviewStatus" size="small" style="width: 90px;"
              @change="(v: number) => handleReviewStatus(record.id, v)">
              <a-select-option :value="0">未复习</a-select-option>
              <a-select-option :value="1">复习中</a-select-option>
              <a-select-option :value="2">已掌握</a-select-option>
            </a-select>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-select v-model:value="record.errorType" size="small" style="width: 70px;"
                @change="(v: number) => handleErrorType(record.id, v)">
                <a-select-option :value="1">概念</a-select-option>
                <a-select-option :value="2">计算</a-select-option>
                <a-select-option :value="3">思路</a-select-option>
                <a-select-option :value="4">审题</a-select-option>
              </a-select>
              <a-button size="small" @click="handleOpenNotes(record.id)">备注</a-button>
              <a-button size="small" @click="handleShareError(record.id)">分享</a-button>
              <a-button size="small" @click="handleArchive(record.id)">归档</a-button>
              <a-button size="small" danger @click="handleDelete(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </template>

    <template v-if="viewMode === 'charts'">
      <a-spin :spinning="statsLoading">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-card size="small"><a-statistic title="错题总数" :value="errorStats?.totalErrors || 0" /></a-card>
          </a-col>
          <a-col :span="16">
            <a-card size="small" title="错误类型分布">
              <div ref="typePieRef" style="height: 250px;"></div>
            </a-card>
          </a-col>
        </a-row>
        <a-row :gutter="16" style="margin-top: 16px;">
          <a-col :span="12">
            <a-card size="small" title="薄弱知识点雷达图">
              <div ref="radarRef" style="height: 350px;"></div>
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card size="small" title="高频错误知识点">
              <div ref="barRef" style="height: 350px;"></div>
            </a-card>
          </a-col>
        </a-row>
      </a-spin>
    </template>

    <!-- 推荐模式 -->
    <template v-if="viewMode === 'recommend'">
      <a-card size="small" style="margin-bottom: 12px;">
        <a-row :gutter="12" align="middle">
          <a-col :span="4"><span>推荐数量：</span><a-input-number v-model:value="recCount" :min="5" :max="30" size="small" /></a-col>
          <a-col :span="4">
            <a-select v-model:value="recTendency" size="small">
              <a-select-option value="basic">基础</a-select-option>
              <a-select-option value="balanced">均衡</a-select-option>
              <a-select-option value="advanced">进阶</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4"><a-checkbox v-model:checked="recIncludeAnalysis">含解析</a-checkbox></a-col>
          <a-col :span="4"><a-button type="primary" size="small" @click="loadRecommend" :loading="recLoading">获取推荐</a-button></a-col>
        </a-row>
      </a-card>
      <a-table :columns="recColumns" :data-source="recList" :loading="recLoading" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag color="blue">{{ getTypeName(record.type) }}</a-tag>
          </template>
          <template v-if="column.key === 'difficulty'">
            <a-tag :color="record.difficulty===1?'green':record.difficulty===3?'red':'orange'">{{ getDiffName(record.difficulty) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button size="small" type="primary" @click="handleRecFeedback(record.id, true)">已掌握</a-button>
            <a-button size="small" @click="handleRecFeedback(record.id, false)" style="margin-left:4px;">仍困难</a-button>
          </template>
        </template>
      </a-table>
      <a-empty v-if="!recLoading && recList.length===0" description="暂无推荐题目，请先积累错题后重试" />
    </template>

    <!-- 强化组卷弹窗 -->
    <a-modal v-model:open="showAssemblyModal" title="错题专项强化组卷" @ok="handleAssembly" :ok-text="'开始组卷'">
      <a-form layout="vertical">
        <a-form-item label="试卷名称"><a-input v-model:value="asmPaperName" placeholder="错题强化卷" /></a-form-item>
        <a-form-item label="目标题数"><a-input-number v-model:value="asmQuestionCount" :min="5" :max="50" style="width:100%;" /></a-form-item>
        <a-form-item label="目标难度"><a-select v-model:value="asmDifficulty">
          <a-select-option :value="1">简单</a-select-option>
          <a-select-option :value="2">中等</a-select-option>
          <a-select-option :value="3">困难</a-select-option>
        </a-select></a-form-item>
        <a-form-item label="答题时长(分钟)"><a-input-number v-model:value="asmDuration" :min="15" :max="180" style="width:100%;" /></a-form-item>
      </a-form>
    </a-modal>

    <!-- 分组模式 -->
    <template v-if="viewMode === 'groups'">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-card size="small" title="错题分组">
            <template #extra>
              <a-button size="small" type="primary" @click="showCreateGroup = true">新建</a-button>
            </template>
            <div v-for="g in groups" :key="g.id" style="padding:8px;margin-bottom:4px;border:1px solid #f0f0f0;border-radius:4px;cursor:pointer"
              :style="{background: selectedGroupId===g.id?'#e6f7ff':''}" @click="selectGroup(g)">
              <div style="font-weight:500">{{ g.groupName }}</div>
              <div style="font-size:11px;color:#999">{{ g.description || '无描述' }}</div>
            </div>
            <a-empty v-if="groups.length===0" description="暂无分组" style="padding:24px" />
          </a-card>
        </a-col>
        <a-col :span="16">
          <a-card size="small" :title="selectedGroupId ? '分组内错题' : '请选择分组'">
            <template v-if="selectedGroupId" #extra>
              <a-space>
                <a-button size="small" danger @click="handleDeleteGroup(selectedGroupId!)">删除分组</a-button>
              </a-space>
            </template>
            <a-table v-if="selectedGroupId" :columns="groupItemColumns" :data-source="groupItems" :loading="groupItemsLoading"
              row-key="id" size="small" :pagination="false">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key==='action'">
                  <a-button type="link" size="small" danger @click="handleRemoveGroupItem(record.id)">移除</a-button>
                </template>
              </template>
            </a-table>
            <a-empty v-if="!selectedGroupId" description="点击左侧分组查看内容" style="padding:24px" />
          </a-card>
        </a-col>
      </a-row>
    </template>

    <!-- 创建分组弹窗 -->
    <a-modal v-model:open="showCreateGroup" title="创建错题分组" @ok="handleCreateGroup">
      <a-form :label-col="{span:6}">
        <a-form-item label="分组名称"><a-input v-model:value="newGroupName" /></a-form-item>
        <a-form-item label="描述"><a-textarea v-model:value="newGroupDesc" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <!-- 错题备注弹窗 -->
    <a-modal v-model:open="noteVisible" title="错题备注" width="500px" :footer="null">
      <a-list :data-source="notesList" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>{{ item.noteType===1 ? '文字备注' : '图片备注' }}</template>
              <template #description>
                <template v-if="item.noteType===1">{{ item.content }}</template>
                <img v-else :src="item.imageUrl" style="max-width:200px;max-height:200px" />
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
      <a-divider>添加备注</a-divider>
      <a-space direction="vertical" style="width:100%">
        <a-textarea v-model:value="newNoteContent" placeholder="输入文字备注（解题思路、易错提醒等）" :rows="2" />
        <a-space>
          <a-button type="primary" @click="handleAddNote(1)">添加文字备注</a-button>
          <a-input v-model:value="newNoteImageUrl" placeholder="图片URL" style="width:250px" />
          <a-button @click="handleAddNote(2)">添加图片</a-button>
        </a-space>
      </a-space>
    </a-modal>

    <!-- 预览弹窗 -->
    <a-modal v-model:open="previewVisible" title="错题集预览" width="700px" :footer="null">
      <div v-html="previewHtml" style="max-height:500px;overflow-y:auto;"></div>
    </a-modal>

    <!-- 导出日志弹窗 -->
    <a-modal v-model:open="exportLogVisible" title="导出记录" width="600px" :footer="null">
      <a-table :columns="exportLogColumns" :data-source="exportLogs" :loading="exportLogLoading"
        row-key="id" size="small" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key==='status'">
            <a-tag :color="record.exportStatus===2?'green':record.exportStatus===3?'red':'orange'">
              {{ ['待处理','处理中','已完成','失败'][record.exportStatus||0] }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import { listErrors, updateReviewStatus, updateErrorType, toggleArchive, deleteError, getErrorStats, getWeakKnowledgePoints, recommendQuestions, recommendFeedback, batchDeleteErrors, batchUpdateReviewStatus, exportErrorBookExcel, exportErrorBookWord, exportErrorBookPdf, batchExportErrorBook, previewErrorBook, reinforceAssemble, createGroup, listGroups, deleteGroup, getGroupItems, removeGroupItem, batchGroup, addNote, listNotes, shareErrorBookToTeacher, checkErrorBookShared, getErrorBookExportLogs } from '@/api/gerentongji'
import { getQuestionById } from '@/api/shitiguanli'
import { DownOutlined } from '@ant-design/icons-vue'

const loading = ref(false)
const statsLoading = ref(false)
const viewMode = ref('list')
const errorList = ref<any[]>([])
const errorStats = ref<any>(null)
const weakPoints = ref<API.PersonalDimensionVO[]>([])
const filterErrorType = ref<number | undefined>(undefined)
const filterKnowledgePoint = ref('')
const filterReviewStatus = ref<number | undefined>(undefined)
const sortBy = ref('lastErrorTime')
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showTotal: (t: number) => `共 ${t} 条` })
const selectedIds = ref<number[]>([])
const typePieRef = ref<HTMLElement | null>(null)
const radarRef = ref<HTMLElement | null>(null)
const barRef = ref<HTMLElement | null>(null)
let chartInstances: echarts.ECharts[] = []

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '题号', dataIndex: 'questionId', width: 70 },
  { title: '错误类型', key: 'errorType', width: 100 },
  { title: '错误次数', dataIndex: 'errorCount', width: 80 },
  { title: '最近错误', dataIndex: 'lastErrorTime', width: 160 },
  { title: '复习状态', key: 'reviewStatus', width: 110 },
  { title: '操作', key: 'action', width: 220 },
]

const getErrorTypeName = (t: number) => ({ 1:'概念错误', 2:'计算错误', 3:'思路错误', 4:'审题错误' }[t] || '未知')
const getErrorTypeColor = (t: number) => ({ 1:'orange', 2:'red', 3:'purple', 4:'blue' }[t] || 'default')

// 展开行：加载题目详情
const expandedRowKeys = ref<number[]>([])
const expandedQuestions = ref<Record<number, any>>({})
const handleExpandRow = async (expanded: boolean, record: any) => {
  if (!record?.questionId) return
  if (expanded) {
    expandedRowKeys.value = [record.id]
    const qid = record.questionId
    if (!expandedQuestions.value[qid]) {
      try {
        const res = await getQuestionById({ id: qid })
        if (res.data.code === 0 && res.data.data) {
          expandedQuestions.value[qid] = res.data.data
        }
      } catch { /* ignore */ }
    }
  } else {
    expandedRowKeys.value = []
  }
}
const expandedOptions = (qid: number) => {
  const q = expandedQuestions.value[qid]
  if (!q?.options) return []
  try {
    const arr = JSON.parse(q.options)
    return arr.map((o: any) => ({ key: String(o.key || o.label || o), text: String(o.value || o.text || o) }))
  } catch { return [] }
}
const renderQContent = (c: string) => c?.replace(/\n/g, '<br/>') || ''

const loadErrors = async () => {
  loading.value = true
  try {
    const res = await listErrors({
      pageNum: pagination.current, pageSize: pagination.pageSize,
      errorType: filterErrorType.value,
      knowledgePoint: filterKnowledgePoint.value || undefined,
      reviewStatus: filterReviewStatus.value,
      sortBy: sortBy.value,
    })
    if (res.data.code === 0 && res.data.data) {
      errorList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    }
  } catch (e) { message.error('加载失败') }
  loading.value = false
}

const loadStats = async () => {
  statsLoading.value = true
  try {
    const [sRes, kpRes] = await Promise.all([getErrorStats(), getWeakKnowledgePoints()])
    if (sRes.data.code === 0) errorStats.value = sRes.data.data
    weakPoints.value = kpRes.data.code === 0 ? (kpRes.data.data || []) : []
    nextTick(() => renderStatsCharts())
  } catch (e) { /* ignore */ }
  statsLoading.value = false
}

const renderStatsCharts = () => {
  chartInstances.forEach(c => c.dispose()); chartInstances = []
  if (typePieRef.value && errorStats.value?.byErrorType) {
    const c = echarts.init(typePieRef.value); chartInstances.push(c)
    c.setOption({
      tooltip:{}, toolbox:{feature:{saveAsImage:{title:'保存'}}},
      series:[{type:'pie',radius:'70%',
        data:errorStats.value.byErrorType.map((d:any)=>({name:d.dimensionKey,value:d.totalCount}))}]
    })
  }
  if (radarRef.value && weakPoints.value.length) {
    const c = echarts.init(radarRef.value); chartInstances.push(c)
    const top = weakPoints.value.slice(0,8)
    const maxVal = Math.max(...top.map(t=>t.totalCount||0))
    c.setOption({
      tooltip:{}, toolbox:{feature:{saveAsImage:{title:'保存'}}},
      radar:{ indicator: top.map(k=>({name:k.dimensionKey,max:maxVal*1.3})) },
      series:[{type:'radar',data:[{value:top.map(k=>k.totalCount||0),name:'错误次数',
        areaStyle:{color:'rgba(255,77,79,0.3)'}}]}]
    })
  }
  if (barRef.value && weakPoints.value.length) {
    const c = echarts.init(barRef.value); chartInstances.push(c)
    const top = weakPoints.value.slice(0,15)
    c.setOption({
      tooltip:{}, toolbox:{feature:{saveAsImage:{title:'保存'}}},
      grid:{left:'3%',right:'8%',containLabel:true},
      xAxis:{type:'value'}, yAxis:{type:'category',data:top.map(k=>k.dimensionKey).reverse()},
      series:[{type:'bar',data:top.map(k=>k.totalCount||0).reverse(),itemStyle:{color:'#ff4d4f'}}]
    })
  }
}

const handleTableChange = (pag:any) => { pagination.current=pag.current; pagination.pageSize=pag.pageSize; loadErrors() }
const handleReviewStatus = async (id:number,s:number) => { try{await updateReviewStatus(id,s)}catch{message.error('失败')} }
const handleErrorType = async (id:number,t:number) => { try{await updateErrorType(id,t)}catch{message.error('失败')} }
const handleArchive = async (id:number) => { try{await toggleArchive(id);message.success('已归档');loadErrors()}catch{message.error('失败')} }
const handleDelete = async (id:number) => { try{await deleteError(id);message.success('已删除');loadErrors()}catch{message.error('失败')} }

watch(viewMode, (v) => { if (v==='charts') loadStats(); if (v==='groups') loadGroups() })
// ===== 推荐 =====
const recCount = ref(15)
const recTendency = ref('balanced')
const recIncludeAnalysis = ref(true)
const recLoading = ref(false)
const recList = ref<any[]>([])
const recColumns = [
  { title: 'ID', dataIndex: 'id', width: 60 }, { title: '题型', key: 'type', width: 80 },
  { title: '难度', key: 'difficulty', width: 70 }, { title: '题干', dataIndex: 'content', ellipsis: true },
  { title: '知识点', dataIndex: 'knowledgePoints', width: 150, ellipsis: true },
  { title: '操作', key: 'action', width: 160 }
]
const getTypeName = (t: number) => ({ 1:'单选题', 2:'多选题', 3:'填空题', 4:'简答题' }[t] || '?')
const getDiffName = (d: number) => ({ 1:'简单', 2:'中等', 3:'困难' }[d] || '?')

const loadRecommend = async () => {
  recLoading.value = true
  try {
    const res = await recommendQuestions({ count: recCount.value, difficultyTendency: recTendency.value, includeAnalysis: recIncludeAnalysis.value })
    if (res.data.code === 0) recList.value = res.data.data || []
  } catch { recList.value = [] }
  recLoading.value = false
}
const handleRecFeedback = async (qid: number, correct: boolean) => {
  try { await recommendFeedback(qid, correct); message.success(correct ? '已记录' : '已记录') } catch {}
}

// ===== 批量操作 =====
const handleBatchDelete = async () => {
  try { await batchDeleteErrors(selectedIds.value); message.success('已删除'); selectedIds.value = []; loadErrors() } catch { message.error('失败') }
}
const handleBatchReview = async (status: number) => {
  try { await batchUpdateReviewStatus(selectedIds.value, status); message.success('已更新'); loadErrors() } catch { message.error('失败') }
}

// ===== 导出/预览 =====
const previewVisible = ref(false); const previewHtml = ref('')
const handlePreview = async () => {
  try { const res = await previewErrorBook(); if (res.data.code===0) { previewHtml.value=res.data.data||''; previewVisible.value=true } } catch {}
}
const handleExportExcel = async () => {
  try { const res=await exportErrorBookExcel(); const b=new Blob([res.data as any]); const a=document.createElement('a'); a.href=URL.createObjectURL(b); a.download='错题集.csv'; a.click() } catch { message.error('导出失败') }
}

// ===== 强化组卷 =====
const showAssemblyModal = ref(false); const asmPaperName = ref('错题强化卷')
const asmQuestionCount = ref(15); const asmDifficulty = ref<number|undefined>(2); const asmDuration = ref(45)
const handleAssembly = async () => {
  try {
    const res = await reinforceAssemble({ paperName: asmPaperName.value, questionCount: asmQuestionCount.value, difficultyAvg: asmDifficulty.value, duration: asmDuration.value })
    if (res.data.code === 0) { message.success('组卷成功！试卷ID: '+res.data.data?.id); showAssemblyModal.value=false }
  } catch { message.error('组卷失败') }
}

// ===== 分组管理 =====
const groups = ref<any[]>([]); const selectedGroupId = ref<number|undefined>()
const showCreateGroup = ref(false); const newGroupName = ref(''); const newGroupDesc = ref('')
const groupItems = ref<any[]>([]); const groupItemsLoading = ref(false)
const groupItemColumns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '错题ID', dataIndex: 'errorBookId', width: 80 },
  { title: '时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action', width: 80 },
]

const loadGroups = async () => {
  try {
    const res = await listGroups()
    if (res.data.code===0) groups.value = res.data.data || []
  } catch {}
}
const selectGroup = async (g:any) => {
  selectedGroupId.value = g.id; groupItemsLoading.value = true
  try {
    const res = await getGroupItems(g.id)
    if (res.data.code===0) groupItems.value = res.data.data || []
  } catch {}
  groupItemsLoading.value = false
}
const handleCreateGroup = async () => {
  if (!newGroupName.value) { message.warning('请输入分组名称'); return }
  try { await createGroup(newGroupName.value, newGroupDesc.value); message.success('已创建'); showCreateGroup.value=false; loadGroups(); newGroupName.value=''; newGroupDesc.value='' } catch { message.error('失败') }
}
const handleDeleteGroup = async (id:number) => {
  try { await deleteGroup(id); message.success('已删除'); selectedGroupId.value=undefined; loadGroups() } catch { message.error('失败') }
}
const handleRemoveGroupItem = async (itemId:number) => {
  try { await removeGroupItem(itemId); message.success('已移除'); if (selectedGroupId.value) selectGroup({id:selectedGroupId.value}) } catch { message.error('失败') }
}
// Wire batch to groups
const handleBatchGroup = async (groupId:number) => {
  if (selectedIds.value.length===0) { message.warning('请先选择错题'); return }
  try { await batchGroup(selectedIds.value, groupId); message.success('已分组'); selectedIds.value=[]; if (selectedGroupId.value===groupId) selectGroup({id:groupId}) } catch { message.error('失败') }
}

// ===== 备注 =====
const noteVisible = ref(false); const noteErrorBookId = ref<number|undefined>()
const notesList = ref<any[]>([]); const newNoteContent = ref(''); const newNoteImageUrl = ref('')
const handleOpenNotes = async (errorBookId:number) => {
  noteErrorBookId.value = errorBookId; noteVisible.value = true
  try {
    const res = await listNotes(errorBookId)
    if (res.data.code===0) notesList.value = res.data.data || []
  } catch {}
}
const handleAddNote = async (noteType:number) => {
  if (!noteErrorBookId.value) return
  const content = noteType===1 ? newNoteContent.value : newNoteImageUrl.value
  if (!content) { message.warning('请输入内容'); return }
  try {
    await addNote(noteErrorBookId.value, content, noteType, noteType===2?newNoteImageUrl.value:undefined)
    message.success('已添加'); newNoteContent.value=''; newNoteImageUrl.value=''
    handleOpenNotes(noteErrorBookId.value)
  } catch { message.error('失败') }
}

// ===== 导出增强 =====
const handleExportWord = async () => {
  try { const res = await exportErrorBookWord(); downloadBlob(res.data, '错题集.docx') } catch { message.error('导出失败') }
}
const handleExportPdf = async () => {
  try { const res = await exportErrorBookPdf(); downloadBlob(res.data, '错题集.pdf') } catch { message.error('导出失败') }
}
const handleBatchExport = async () => {
  try { const res = await batchExportErrorBook(); downloadBlob(res.data, '错题集-批量.zip') } catch { message.error('导出失败') }
}
const downloadBlob = (data:any, name:string) => {
  const b = data instanceof Blob ? data : new Blob([data as any])
  const a = document.createElement('a'); a.href=URL.createObjectURL(b); a.download=name; a.click()
}

// ===== 错题分享 =====
const handleShareError = async (errorBookId:number) => {
  try {
    const res = await shareErrorBookToTeacher(errorBookId)
    if (res.data.code===0) { message.success('已分享给教师'); return }
    message.error(res.data.message || '分享失败')
  } catch { message.error('分享失败') }
}

// ===== 导出日志 =====
const exportLogVisible = ref(false); const exportLogs = ref<any[]>([]); const exportLogLoading = ref(false)
const exportLogColumns = [
  { title: '文件名', dataIndex: 'fileName', ellipsis: true },
  { title: '类型', dataIndex: 'exportType', width: 80 },
  { title: '状态', key: 'status', width: 80 },
  { title: '时间', dataIndex: 'createTime', width: 160 },
]
const loadExportLogs = async () => {
  exportLogVisible.value = true; exportLogLoading.value = true
  try {
    const res = await getErrorBookExportLogs()
    if (res.data.code===0) exportLogs.value = res.data.data || []
  } catch {}
  exportLogLoading.value = false
}

onMounted(() => { loadErrors() })
onUnmounted(() => { chartInstances.forEach(c=>c.dispose()) })
</script>

<style scoped>
.error-book-page { padding: 0; }
.question-detail { padding: 8px 16px; background: #fafafa; border-radius: 4px; }
.question-detail .q-content { margin-bottom: 8px; line-height: 1.6; }
.question-detail .q-options { margin-bottom: 8px; }
.question-detail .q-opt-item { padding: 2px 0; }
.question-detail .q-answer { color: #52c41a; font-weight: 500; }
.question-detail .q-analysis { color: #faad14; }
</style>
