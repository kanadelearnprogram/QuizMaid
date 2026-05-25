<template>
  <div class="paper-manager">
    <a-card>
      <template #title>
        <div class="card-title">试卷管理</div>
      </template>

      <!-- 筛选查询区 -->
      <div class="filter-section">
        <a-space>
          <a-input
            v-model:value="filters.paperName"
            placeholder="试卷名称"
            style="width: 200px"
          />
          <a-select
            v-model:value="filters.subject"
            placeholder="所属科目"
            style="width: 150px"
            allow-clear
          >
            <a-select-option v-for="subject in subjectOptions" :key="subject" :value="subject">
              {{ subject }}
            </a-select-option>
          </a-select>
          <a-select
            v-model:value="filters.status"
            placeholder="试卷状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="2">已归档</a-select-option>
            <a-select-option :value="3">已停用</a-select-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
      </div>

      <!-- 操作按钮区 -->
      <div class="table-operations">
        <a-button type="primary" @click="handleAdd">
          新增试卷
        </a-button>
        <a-button type="primary" @click="handleManualAssembly">
          手动组卷
        </a-button>
        <a-button @click="strategyVisible = true">
          策略管理
        </a-button>
        <a-button type="primary" @click="handleAIPaperAssembly">
          AI智能组卷
        </a-button>
        <a-button
          v-if="loginUserStore.loginUser?.role === 'user'"
          @click="loadSharedToMe"
        >
          分享给我的
        </a-button>
        <a-button
          danger
          @click="handleBatchDelete"
          :disabled="selectedRowKeys.length === 0"
        >
          批量删除
        </a-button>
        <a-button
          @click="exportVisible = true"
          :disabled="selectedRowKeys.length === 0"
        >
          批量导出
        </a-button>
      </div>

      <!-- 试卷列表表格区 -->
      <a-table
        :columns="columns"
        :data-source="paperList"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: handleSelectionChange
        }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status !== 1"
                type="link" size="small" @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-button
                type="link" size="small" @click="handleCopy(record)"
              >
                复制
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                删除
              </a-button>
              <a-button type="link" size="small" @click="handleManageQuestions(record)">
                管理试题
              </a-button>
              <a-button type="link" size="small" @click="exportPid = record.id; exportPname = record.paperName; exportVisible = true">
                导出
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link" size="small"
                style="color: #52c41a;"
                @click="handleStartExam(record)"
              >
                考试
              </a-button>
              <a-button type="link" size="small" @click="handleShareToUser(record)">分享</a-button>
              <a-select
                v-model:value="record.status"
                size="small"
                style="width:90px"
                @change="(v: number) => handleStatusChange(record, v)"
                @click.stop
              >
                <a-select-option :value="0">草稿</a-select-option>
                <a-select-option :value="1">已发布</a-select-option>
                <a-select-option :value="2">已归档</a-select-option>
                <a-select-option :value="3">已停用</a-select-option>
              </a-select>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 试卷新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      :confirmLoading="modalLoading"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="试卷名称" name="paperName" :rules="[{ required: true, message: '请输入试卷名称' }]">
          <a-input v-model:value="formState.paperName" />
        </a-form-item>
        <a-form-item label="所属科目" name="subject" :rules="[{ required: true, message: '请选择所属科目' }]">
          <a-select v-model:value="formState.subject">
            <a-select-option v-for="subject in subjectOptions" :key="subject" :value="subject">
              {{ subject }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="总分" name="totalScore" :rules="[{ required: true, message: '请输入总分' }]">
          <a-input-number v-model:value="formState.totalScore" :min="0" :precision="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="考试时长(分钟)" name="duration">
          <a-input-number v-model:value="formState.duration" :min="1" :max="300" :precision="0" style="width: 100%" placeholder="如90分钟" />
        </a-form-item>
        <a-form-item label="状态" name="status" :rules="[{ required: true, message: '请选择状态' }]">
          <a-select v-model:value="formState.status">
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="2">已归档</a-select-option>
            <a-select-option :value="3">已停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 试卷试题管理弹窗 -->
    <a-modal
      v-model:open="questionManageVisible"
      :title="questionManageTitle"
      width="80%"
      @ok="handleQuestionManageOk"
      @cancel="handleQuestionManageCancel"
      :footer="null"
    >
      <div class="question-manage-content">
        <div class="question-manage-operations">
          <a-button type="primary" @click="handleAddQuestions">
            添加试题
          </a-button>
        </div>

        <a-table
          :columns="questionColumns"
          :data-source="currentPaperQuestions"
          :loading="questionLoading"
          :pagination="false"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'sort'">
              <a-input-number
                v-model:value="record.sort"
                :min="1"
                :precision="0"
                @change="handleSortChange(record)"
              />
            </template>
            <template v-if="column.key === 'type'">
              <a-tag :color="getQuestionTypeColor(record.type)">
                {{ getQuestionTypeText(record.type) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'questionScore'">
              <a-input-number
                v-model:value="record.questionScore"
                :min="0"
                :precision="0"
                @change="handleScoreChange(record)"
              />
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" danger @click="handleRemoveQuestion(record)">
                移除
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>

    <!-- 选择试题弹窗 -->
    <a-modal
      v-model:open="selectQuestionVisible"
      title="选择试题"
      width="80%"
      @ok="handleSelectQuestionOk"
      @cancel="handleSelectQuestionCancel"
      :confirmLoading="selectQuestionLoading"
    >
      <div class="select-question-content">
        <div class="select-question-filters">
          <a-space>
            <a-input
              v-model:value="questionFilters.content"
              placeholder="题目内容"
              style="width: 200px"
            />
            <a-select
              v-model:value="questionFilters.subject"
              placeholder="科目"
              style="width: 120px"
              allow-clear
            >
              <a-select-option v-for="subject in subjectOptions" :key="subject" :value="subject">
                {{ subject }}
              </a-select-option>
            </a-select>
            <a-select
              v-model:value="questionFilters.type"
              placeholder="题目类型"
              style="width: 120px"
              allow-clear
            >
              <a-select-option :value="1">单选题</a-select-option>
              <a-select-option :value="2">多选题</a-select-option>
              <a-select-option :value="3">判断题</a-select-option>
              <a-select-option :value="4">填空题</a-select-option>
              <a-select-option :value="5">简答题</a-select-option>
            </a-select>
            <a-button type="primary" @click="handleSearchQuestions">搜索</a-button>
          </a-space>
        </div>

        <a-table
          :columns="selectQuestionColumns"
          :data-source="availableQuestions"
          :loading="availableQuestionsLoading"
          :pagination="availableQuestionsPagination"
          :row-selection="{
            selectedRowKeys: selectedQuestionKeys,
            onChange: handleQuestionSelectionChange
          }"
          @change="handleSelectQuestionTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'type'">
              <a-tag :color="getQuestionTypeColor(record.type)">
                {{ getQuestionTypeText(record.type) }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>

    <!-- AI智能组卷弹窗（两阶段 + 历史记录） -->
    <a-modal
      v-model:open="aiAssemblyVisible"
      title="AI智能组卷"
      width="65%"
      :footer="null"
      @cancel="handleAIClose"
    >
      <a-tabs v-model:activeKey="(aiStage as string)" @change="(k: string) => { if (k === 'history') loadAIHistory() }">
        <!-- 阶段1：输入需求 -->
        <a-tab-pane key="input" tab="组卷需求">
          <a-form :model="aiFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="试卷名称" required>
              <a-input v-model:value="aiFormState.paperName" placeholder="请输入试卷名称" />
            </a-form-item>
            <a-form-item label="所属科目">
              <a-select v-model:value="aiFormState.subject" placeholder="选择科目（可选）" allow-clear>
                <a-select-option v-for="s in subjectOptions" :key="s" :value="s">{{ s }}</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="难度">
              <a-select v-model:value="aiFormState.difficulty" placeholder="选择难度（可选）" allow-clear>
                <a-select-option :value="1">简单</a-select-option>
                <a-select-option :value="2">中等</a-select-option>
                <a-select-option :value="3">困难</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="状态" required>
              <a-select v-model:value="aiFormState.status">
                <a-select-option :value="0">草稿</a-select-option>
                <a-select-option :value="1">已发布</a-select-option>
              </a-select>
            </a-form-item>
            <a-divider>个性化配置</a-divider>
            <a-form-item label="融入学习画像">
              <a-switch v-model:checked="aiV2UsePersonalization" />
              <span style="margin-left:8px;color:#888;font-size:12px">将您的答题统计和薄弱知识点预填为AI提示词</span>
            </a-form-item>
            <a-form-item v-if="aiV2UsePersonalization" label="聚焦薄弱点">
              <a-switch v-model:checked="aiV2IncludeWeak" />
              <span style="margin-left:8px;color:#888;font-size:12px">优先选择薄弱知识点相关题目</span>
            </a-form-item>

            <!-- 学习画像摘要 -->
            <a-alert v-if="aiV2UsePersonalization && aiV2Profile" type="info" style="margin-bottom:12px">
              <template #message>
                总答题 {{ aiV2Profile.answerNum }} 题，正确率 {{ aiV2Profile.accuracy?.toFixed(1) || 'N/A' }}%
                <span v-if="aiV2Profile.weakPoints?.length">
                  | 薄弱知识点：
                  <a-tag v-for="wp in aiV2Profile.weakPoints.slice(0,3)" :key="wp.knowledgePoint" color="orange" size="small">
                    {{ wp.knowledgePoint }}({{ wp.accuracy }}%)
                  </a-tag>
                </span>
              </template>
            </a-alert>

            <a-form-item label="组卷需求">
              <a-textarea v-model:value="aiFormState.userRequirement"
                placeholder="自然语言描述组卷需求，如：侧重二次函数、难度适中、题型均衡"
                :rows="3" />
            </a-form-item>
          </a-form>
          <div style="text-align:center;margin-top:12px">
            <a-button type="primary" size="large" @click="handleAIGenerate" :loading="aiV2Loading">
              生成组卷方案
            </a-button>
          </div>
          <div v-if="aiV2Error" style="text-align:center;margin-top:8px;color:red">{{ aiV2Error }}</div>
        </a-tab-pane>

        <!-- 阶段2：确认方案 -->
        <a-tab-pane key="review" tab="确认方案" v-if="aiV2Strategy">
          <a-descriptions bordered size="small" :column="2">
            <a-descriptions-item label="选中题目数">{{ aiV2Strategy.totalQuestions }} 题</a-descriptions-item>
            <a-descriptions-item label="实际总分">{{ aiV2Strategy.actualTotalScore }} 分</a-descriptions-item>
            <a-descriptions-item label="平均难度">{{ aiV2Strategy.difficultyAvg || 'AI未指定' }}</a-descriptions-item>
            <a-descriptions-item label="算法类型">AI (DeepSeek)</a-descriptions-item>
          </a-descriptions>

          <a-divider>选中题目预览</a-divider>
          <a-table
            :columns="[
              { title: '#', width: 50, customRender: ({ index }: any) => index + 1 },
              { title: '题目ID', dataIndex: 'questionId', width: 80 },
            ]"
            :data-source="aiV2Strategy.questionIds?.map((id, i) => ({ key: i, questionId: id })) || []"
            :pagination="false" size="small" style="margin-bottom:12px"
          />

          <a-alert v-if="aiV2RetryCount > 0" :message="'AI调用重试了' + aiV2RetryCount + '次后成功'" type="warning" show-icon style="margin-bottom:12px" />

          <div style="text-align:center;margin-top:12px">
            <a-space size="large">
              <a-button type="primary" size="large" @click="handleAIConfirm" :loading="aiV2Loading">确认组卷</a-button>
              <a-button size="large" @click="handleAIRegenerate" :loading="aiV2Loading">重新生成</a-button>
              <a-button size="large" @click="handleAISwitchManual">切换手动组卷</a-button>
            </a-space>
          </div>
        </a-tab-pane>

        <!-- 历史记录 -->
        <a-tab-pane key="history" tab="历史记录">
          <a-table
            :columns="[
              { title: '时间', dataIndex: 'createTime', width: 160 },
              { title: '用户需求', dataIndex: 'chatContent', ellipsis: true },
              { title: '状态', dataIndex: 'status', width: 80 },
              { title: '操作', key: 'action', width: 80 },
            ]"
            :data-source="aiV2History"
            :pagination="false" size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '成功' : '失败' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" size="small" @click="handleAIReuse(record)">复用</a-button>
              </template>
            </template>
          </a-table>
          <div v-if="aiV2History.length === 0" style="text-align:center;padding:24px;color:#999">暂无历史记录</div>
        </a-tab-pane>
      </a-tabs>
    </a-modal>

    <!-- 导出弹窗 -->
    <PaperExportDialog
      v-if="exportVisible"
      :paper-id="exportPid"
      :paper-name="exportPname"
      :paper-ids="selectedRowKeys"
      @close="exportVisible = false"
    />

    <!-- 策略管理弹窗 -->
    <a-modal v-model:open="strategyVisible" title="组卷策略管理" width="70%" :footer="null">
      <div style="margin-bottom:12px">
        <a-button type="primary" @click="handleStrategyDetail(null)">新建策略</a-button>
      </div>
      <a-table :columns="strategyColumns" :data-source="strategyList" :loading="strategyLoading"
        :pagination="strategyPagination" @change="handleStrategyTableChange" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'isDefault'">
            <a-tag v-if="record.isDefault===1" color="gold">默认</a-tag>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'weightSum'">
            <span :style="{color: record.weightSum===100?'#52c41a':'#ff4d4f'}">{{ record.weightSum || 0 }}%</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleStrategyDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="handleStrategyEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleStrategyCopy(record.id)">复制</a-button>
              <a-button v-if="record.isDefault!==1" type="link" size="small" @click="handleStrategySetDefault(record.id)">设默认</a-button>
              <a-button type="link" size="small" danger @click="handleStrategyDelete(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 策略详情/编辑弹窗 -->
    <a-modal v-model:open="strategyDetailVisible" :title="strategyDetailTitle" width="60%" @ok="handleStrategySave" :confirmLoading="strategyDetailLoading">
      <a-form :model="strategyForm" :label-col="{span:6}" :wrapper-col="{span:14}">
        <a-form-item label="策略名称"><a-input v-model:value="strategyForm.strategyName" /></a-form-item>
        <a-form-item label="目标总分"><a-input-number v-model:value="strategyForm.totalScore" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="平均难度"><a-select v-model:value="strategyForm.difficultyAvg">
          <a-select-option :value="1">1-简单</a-select-option>
          <a-select-option :value="2">2-较易</a-select-option>
          <a-select-option :value="3">3-中等</a-select-option>
          <a-select-option :value="4">4-较难</a-select-option>
          <a-select-option :value="5">5-困难</a-select-option>
        </a-select></a-form-item>
        <a-form-item label="答题时长(分)"><a-input-number v-model:value="strategyForm.duration" :min="0" style="width:100%" /></a-form-item>
        <a-divider>6维权重配置（总和必须为100%）</a-divider>
        <a-form-item v-for="w in strategyWeights" :key="w.key" :label="w.label">
          <a-slider v-model:value="w.value" :min="0" :max="100" @change="updateWeightSum" />
          <span style="margin-left:8px;font-weight:bold" :style="{color: weightSum===100?'#52c41a':'#ff4d4f'}">{{ w.value }}%</span>
        </a-form-item>
        <a-form-item label="权重总和">
          <a-progress :percent="weightSum" :status="weightSum===100?'success':'exception'" style="width:200px" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分享弹窗 -->
    <a-modal v-model:open="shareVisible" title="试卷分享" @ok="handleShareConfirm" :confirmLoading="shareLoading">
      <a-form :label-col="{span:8}" :wrapper-col="{span:12}">
        <a-form-item label="试卷">{{ sharePaperName }}</a-form-item>
        <a-form-item label="分享方式">
          <a-radio-group v-model:value="shareMode">
            <a-radio value="user">指定用户</a-radio>
            <a-radio value="group">指定班级/组</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="shareMode==='user'" label="目标用户ID">
          <a-input-number v-model:value="shareTargetId" :min="1" style="width:100%" placeholder="输入用户ID" />
        </a-form-item>
        <a-form-item v-else label="目标组ID">
          <a-input-number v-model:value="shareTargetId" :min="1" style="width:100%" placeholder="输入班级/组ID" />
        </a-form-item>
      </a-form>
      <a-divider>已分享记录</a-divider>
      <a-table :columns="shareRecordColumns" :data-source="shareRecords" :loading="shareRecordLoading"
        row-key="id" size="small" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key==='action'">
            <a-button type="link" size="small" danger @click="handleRevokeShare(record.id)">撤销</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 分享给我的试卷弹窗 -->
    <a-modal v-model:open="sharedToMeVisible" title="分享给我的试卷" width="60%" :footer="null">
      <a-table :columns="sharedToMeColumns" :data-source="sharedToMeList" :loading="sharedToMeLoading"
        row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key==='action'">
            <a-button v-if="record.status===1" type="link" size="small" style="color:#52c41a"
              @click="handleStartExam(record)">考试</a-button>
          </template>
        </template>
      </a-table>
      <a-empty v-if="!sharedToMeLoading && sharedToMeList.length===0" description="暂无分享给您的试卷" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  addExamPaper,
  deleteExamPaper,
  copyExamPaper,
  listExamPaperByPage,
  updateExamPaper,
  updateStatus1,
  getExamPaperById,
  aiAssemblePaperV2,
  confirmAIAssembly,
  getAIProfile,
  getAIChatHistory,
  listPaperStrategyByPage,
  getPaperStrategyById,
  addPaperStrategy,
  updatePaperStrategy,
  deletePaperStrategy,
  copyPaperStrategy,
  setDefaultStrategy,
  sharePaperToUser,
  sharePaperToGroup,
  getPaperShares,
  revokeShare,
  getSharedToMe
} from '@/api/shijuanguanli'
import {
  addQuestionToPaper,
  removeQuestionFromPaper,
  updatePaperQuestion
} from '@/api/shijuanshitiguanlianguanli'
import { listQuestionByPage, listAllQuestionByPage } from '@/api/shitiguanli'
import { useLoginUserStore } from '@/stores/loginUser'
import PaperExportDialog from './PaperExportDialog.vue'
import type { FormInstance } from 'ant-design-vue'

interface PaperRecord {
  id?: number
  paperName?: string
  subject?: string
  totalScore?: number
  creatorId?: number
  status?: number
  createTime?: string
  updateTime?: string
  questions?: PaperQuestionVO[]
}

interface PaperQuestionVO {
  id?: number
  questionId?: number
  questionContent?: string
  questionScore?: number
  sort?: number
  type?: number
}

interface QuestionRecord {
  id?: number
  type?: number
  subject?: string
  content?: string
  difficulty?: number
  knowledgePoints?: string
  tags?: string
  answer?: string
  analysis?: string
  creatorId?: number
  status?: number
  createTime?: string
  updateTime?: string
}

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '试卷名称', dataIndex: 'paperName', key: 'paperName' },
  { title: '所属科目', dataIndex: 'subject', key: 'subject' },
  { title: '总分', dataIndex: 'totalScore', key: 'totalScore' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 }
]

const questionColumns = [
  { title: '序号', key: 'sort', width: 100 },
  { title: '题目类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '题目内容', dataIndex: 'questionContent', key: 'questionContent' },
  { title: '分值', key: 'questionScore', width: 100 },
  { title: '操作', key: 'action', width: 80 }
]

const selectQuestionColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '题目类型', dataIndex: 'type', key: 'type' },
  { title: '科目', dataIndex: 'subject', key: 'subject' },
  { title: '题目内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '难度', dataIndex: 'difficulty', key: 'difficulty' }
]

const loading = ref(false)
const paperList = ref<PaperRecord[]>([])
const selectedRowKeys = ref<number[]>([])
const router = useRouter()
const exportVisible = ref(false)
const exportPid = ref<number>()
const exportPname = ref('')
const loginUserStore = useLoginUserStore()

const subjectOptions = ref<string[]>(['数学', '语文', '英语', '物理', '化学', '生物', '历史', '地理', '政治'])

const filters = reactive({
  paperName: '',
  subject: undefined as string | undefined,
  status: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const modalVisible = ref(false)
const modalTitle = ref('')
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const formState = reactive({
  id: undefined as number | undefined,
  paperName: '',
  subject: '',
  totalScore: 100,
  duration: 90 as number | undefined,
  status: 0
})

const questionManageVisible = ref(false)
const questionManageTitle = ref('')
const questionLoading = ref(false)
const currentPaperQuestions = ref<PaperQuestionVO[]>([])
const currentPaperId = ref<number>()

const selectQuestionVisible = ref(false)
const selectQuestionLoading = ref(false)
const availableQuestions = ref<QuestionRecord[]>([])
const availableQuestionsLoading = ref(false)
const selectedQuestionKeys = ref<number[]>([])

const questionFilters = reactive({
  content: '',
  subject: undefined as string | undefined,
  type: undefined as number | undefined
})

const availableQuestionsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// AI组卷相关状态
const aiAssemblyVisible = ref(false)

const aiFormState = reactive({
  paperName: '',
  subject: undefined as string | undefined,
  chapter: undefined as string | undefined,
  difficulty: undefined as number | undefined,
  totalScore: undefined as number | undefined,
  status: 0,
  userRequirement: ''
})

// ===== AI v2 增强状态 =====
const aiStage = ref<'input' | 'review' | 'history'>('input')
const aiV2Loading = ref(false)
const aiV2RetryCount = ref(0)
const aiV2Error = ref('')
const aiV2Strategy = ref<API.AIAssemblyStrategyVO | null>(null)
const aiV2Profile = ref<API.AIProfileVO | null>(null)
const aiV2History = ref<API.AIChatVO[]>([])
const aiV2UsePersonalization = ref(true)
const aiV2IncludeWeak = ref(false)

async function loadAIProfile() {
  try {
    const res = await getAIProfile()
    if (res.data.code === 0 && res.data.data) {
      aiV2Profile.value = res.data.data
    }
  } catch { /* ignore */ }
}

async function loadAIHistory() {
  try {
    const res = await getAIChatHistory({ limit: 20 })
    if (res.data.code === 0 && res.data.data) {
      aiV2History.value = res.data.data
    }
  } catch { /* ignore */ }
}

async function handleAIGenerate() {
  if (!aiFormState.paperName) { message.warning('请输入试卷名称'); return }
  aiV2Loading.value = true
  aiV2RetryCount.value = 0
  aiV2Error.value = ''
  try {
    const res = await aiAssemblePaperV2({
      paperName: aiFormState.paperName,
      subject: aiFormState.subject,
      chapter: aiFormState.chapter,
      difficulty: aiFormState.difficulty,
      totalScore: aiFormState.totalScore,
      status: aiFormState.status,
      userRequirement: aiFormState.userRequirement,
      usePersonalization: aiV2UsePersonalization.value,
      includeWeakAreas: aiV2IncludeWeak.value,
    })
    if (res.data.code === 0 && res.data.data) {
      aiV2Strategy.value = res.data.data
      aiStage.value = 'review'
    } else {
      aiV2Error.value = res.data.message || 'AI组卷失败，建议切换手动组卷'
    }
  } catch {
    aiV2Error.value = 'AI组卷请求失败，建议切换手动组卷'
  } finally {
    aiV2Loading.value = false
  }
}

async function handleAIConfirm() {
  if (!aiV2Strategy.value || !aiV2Strategy.value.questionIds?.length) {
    message.warning('没有题目可供保存'); return
  }
  aiV2Loading.value = true
  try {
    const res = await confirmAIAssembly({
      paperName: aiFormState.paperName,
      subject: aiFormState.subject,
      status: aiFormState.status,
      totalScore: aiFormState.totalScore,
      strategy: aiV2Strategy.value,
    })
    if (res.data.code === 0) {
      message.success('AI组卷成功！试卷已创建')
      handleAIClose()
      loadPaperList()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch {
    message.error('保存请求失败')
  } finally {
    aiV2Loading.value = false
  }
}

function handleAIRegenerate() {
  aiV2Strategy.value = null
  aiStage.value = 'input'
  handleAIGenerate()
}

function handleAISwitchManual() {
  aiAssemblyVisible.value = false
  resetAIForm()
  router.push('/paper/assembly')
}

function handleAIReuse(chat: API.AIChatVO) {
  aiFormState.userRequirement = chat.chatContent || ''
  aiStage.value = 'input'
  // 尝试从历史恢复策略
  if (chat.aiResponse) {
    try {
      const strategy = JSON.parse(chat.aiResponse)
      aiV2Strategy.value = {
        questionIds: strategy.questionIds || [],
        totalQuestions: strategy.questionIds?.length || 0,
        actualTotalScore: (strategy.questionIds?.length || 0) * 10,
      }
      aiStage.value = 'review'
    } catch { /* ignore */ }
  }
}

function handleAIClose() {
  aiAssemblyVisible.value = false
  aiStage.value = 'input'
  aiV2Strategy.value = null
  aiV2Error.value = ''
  aiV2RetryCount.value = 0
}

function handleAIOpen() {
  aiAssemblyVisible.value = true
  aiStage.value = 'input'
  resetAIForm()
  loadAIProfile()
  loadAIHistory()
}

const getCurrentUser = () => {
  return loginUserStore.loginUser
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  loadPaperList()
})

const loadPaperList = async () => {
  loading.value = true
  try {
    const query: Record<string, unknown> = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    if (filters.paperName) {
      query.paperName = filters.paperName
    }
    if (filters.subject) {
      query.subject = filters.subject
    }
    if (filters.status !== undefined) {
      query.status = filters.status
    }

    const currentUser = getCurrentUser()
    if (currentUser?.role !== 'admin') {
      query.creatorId = currentUser?.id
    }

    const res = await listExamPaperByPage(query)
    if (res.data.code === 0) {
      paperList.value = res.data.data?.records || []
      pagination.total = res.data.data?.totalRow || 0
    } else {
      message.error('加载试卷列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error(error)
    message.error('加载试卷列表请求失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadPaperList()
}

const handleSelectionChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const handleSearch = () => {
  pagination.current = 1
  loadPaperList()
}

const handleReset = () => {
  filters.paperName = ''
  filters.subject = undefined
  filters.status = undefined
  pagination.current = 1
  loadPaperList()
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增试卷'
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record: PaperRecord) => {
  if (record.status === 1) {
    message.warning('已发布试卷不可直接编辑，请使用复制功能修改')
    return
  }
  isEdit.value = true
  modalTitle.value = '编辑试卷'
  formState.id = record.id
  formState.paperName = record.paperName || ''
  formState.subject = record.subject || ''
  formState.totalScore = record.totalScore || 100
  formState.duration = record.duration
  formState.status = record.status || 0
  modalVisible.value = true
}

const handleCopy = async (record: PaperRecord) => {
  if (!record.id) return
  try {
    const res = await copyExamPaper(record.id)
    if (res.data.code === 0) {
      message.success('复制成功，已创建试卷副本（状态：草稿）')
      loadPaperList()
    } else {
      message.error('复制失败：' + res.data.message)
    }
  } catch (error) {
    console.error(error)
    message.error('复制请求失败')
  }
}

const handleStartExam = (record: PaperRecord) => {
  router.push({ path: '/exam', query: { paperId: record.id } })
}

const handleDelete = (record: PaperRecord) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除试卷「${record.paperName}」吗？`,
    onOk: async () => {
      if (!record.id) return
      try {
        const res = await deleteExamPaper({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          loadPaperList()
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        console.error(error)
        message.error('删除请求失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个试卷吗？`,
    onOk: async () => {
      try {
        for (const id of selectedRowKeys.value) {
          const res = await deleteExamPaper({ id })
          if (res.data.code !== 0) {
            message.error(`删除ID为${id}的试卷失败：${res.data.message}`)
            return
          }
        }
        message.success('批量删除成功')
        selectedRowKeys.value = []
        loadPaperList()
      } catch (error) {
        console.error(error)
        message.error('批量删除请求失败')
      }
    }
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validate()
    modalLoading.value = true

    if (isEdit.value) {
      const res = await updateExamPaper({
        id: formState.id,
        paperName: formState.paperName,
        subject: formState.subject,
        totalScore: formState.totalScore,
        duration: formState.duration
      })
      if (res.data.code === 0) {
        message.success('更新成功')
        modalVisible.value = false
        loadPaperList()
      } else {
        message.error('更新失败：' + res.data.message)
      }
    } else {
      const res = await addExamPaper({
        paperName: formState.paperName,
        subject: formState.subject,
        totalScore: formState.totalScore,
        duration: formState.duration,
        status: formState.status
      })
      if (res.data.code === 0) {
        message.success('添加成功')
        modalVisible.value = false
        loadPaperList()
      } else {
        message.error('添加失败：' + res.data.message)
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    modalLoading.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
}

const resetForm = () => {
  formState.id = undefined
  formState.paperName = ''
  formState.subject = ''
  formState.totalScore = 100
  formState.duration = 90
  formState.status = 0
}

const handleManageQuestions = async (record: PaperRecord) => {
  if (!record.id) return

  currentPaperId.value = record.id
  questionManageTitle.value = `管理试题 - ${record.paperName}`
  questionManageVisible.value = true

  await loadPaperQuestions(record.id)
}

const loadPaperQuestions = async (paperId: number) => {
  questionLoading.value = true
  try {
    const res = await getExamPaperById({ id: paperId })
    if (res.data.code === 0) {
      currentPaperQuestions.value = res.data.data?.questions || []
    } else {
      message.error('加载试卷试题失败：' + res.data.message)
    }
  } catch {
    message.error('加载试卷试题请求失败')
  } finally {
    questionLoading.value = false
  }
}

const handleQuestionManageOk = () => {
  questionManageVisible.value = false
  loadPaperList()
}

const handleQuestionManageCancel = () => {
  questionManageVisible.value = false
  currentPaperQuestions.value = []
  currentPaperId.value = undefined
}

const handleAddQuestions = () => {
  selectQuestionVisible.value = true
  selectedQuestionKeys.value = []
  loadAvailableQuestions()
}

const loadAvailableQuestions = async () => {
  availableQuestionsLoading.value = true
  try {
    const query: Record<string, unknown> = {
      pageNum: availableQuestionsPagination.current,
      pageSize: availableQuestionsPagination.pageSize
    }

    if (questionFilters.content) {
      query.content = questionFilters.content
    }
    if (questionFilters.subject) {
      query.subject = questionFilters.subject
    }
    if (questionFilters.type !== undefined) {
      query.type = questionFilters.type
    }

    const currentUser = getCurrentUser()
    if (currentUser?.role === 'admin') {
      const res = await listAllQuestionByPage(query)
      if (res.data.code === 0) {
        availableQuestions.value = res.data.data?.records || []
        availableQuestionsPagination.total = res.data.data?.totalRow || 0
      } else {
        message.error('加载试题列表失败：' + res.data.message)
      }
    } else {
      query.creatorId = currentUser?.id
      const res = await listQuestionByPage(query)
      if (res.data.code === 0) {
        availableQuestions.value = res.data.data?.records || []
        availableQuestionsPagination.total = res.data.data?.totalRow || 0
      } else {
        message.error('加载试题列表失败：' + res.data.message)
      }
    }
  } catch {
    message.error('加载试题列表请求失败')
  } finally {
    availableQuestionsLoading.value = false
  }
}

const handleSearchQuestions = () => {
  availableQuestionsPagination.current = 1
  loadAvailableQuestions()
}

const handleSelectQuestionTableChange = (pag: { current: number; pageSize: number }) => {
  availableQuestionsPagination.current = pag.current
  availableQuestionsPagination.pageSize = pag.pageSize
  loadAvailableQuestions()
}

const handleQuestionSelectionChange = (keys: number[]) => {
  selectedQuestionKeys.value = keys
}

const handleSelectQuestionOk = async () => {
  if (selectedQuestionKeys.value.length === 0) {
    message.warning('请选择要添加的试题')
    return
  }

  if (!currentPaperId.value) return

  selectQuestionLoading.value = true
  try {
    const currentSort = currentPaperQuestions.value.length + 1
    for (const questionId of selectedQuestionKeys.value) {
      const res = await addQuestionToPaper({
        paperId: currentPaperId.value,
        questionId: questionId,
        questionScore: 10,
        sort: currentSort
      })

      if (res.data.code !== 0) {
        message.error(`添加试题失败：${res.data.message}`)
        return
      }
    }

    message.success('添加试题成功')
    selectQuestionVisible.value = false
    await loadPaperQuestions(currentPaperId.value)
  } catch {
    message.error('添加试题请求失败')
  } finally {
    selectQuestionLoading.value = false
  }
}

const handleSelectQuestionCancel = () => {
  selectQuestionVisible.value = false
  selectedQuestionKeys.value = []
  availableQuestions.value = []
}

const handleRemoveQuestion = (record: PaperQuestionVO) => {
  Modal.confirm({
    title: '确认移除',
    content: '确定要移除这道试题吗？',
    onOk: async () => {
      if (!record.id || !currentPaperId.value) return

      try {
        const res = await removeQuestionFromPaper({ id: record.id })
        if (res.data.code === 0) {
          message.success('移除成功')
          await loadPaperQuestions(currentPaperId.value)
        } else {
          message.error('移除失败：' + res.data.message)
        }
      } catch {
        message.error('移除请求失败')
      }
    }
  })
}

const handleSortChange = async (record: PaperQuestionVO) => {
  if (!record.id) return

  try {
    const res = await updatePaperQuestion({
      id: record.id,
      sort: record.sort
    })
    if (res.data.code !== 0) {
      message.error('更新排序失败：' + res.data.message)
      await loadPaperQuestions(currentPaperId.value!)
    }
  } catch {
    message.error('更新排序请求失败')
    await loadPaperQuestions(currentPaperId.value!)
  }
}

const handleScoreChange = async (record: PaperQuestionVO) => {
  if (!record.id) return

  try {
    const res = await updatePaperQuestion({
      id: record.id,
      questionScore: record.questionScore
    })
    if (res.data.code !== 0) {
      message.error('更新分值失败：' + res.data.message)
      await loadPaperQuestions(currentPaperId.value!)
    }
  } catch {
    message.error('更新分值请求失败')
    await loadPaperQuestions(currentPaperId.value!)
  }
}

const getStatusColor = (status?: number) => {
  const colorMap: Record<number, string> = {
    0: 'default',
    1: 'success',
    2: 'warning',
    3: 'error'
  }
  return colorMap[status || 0] || 'default'
}

const getStatusText = (status?: number) => {
  const textMap: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '已归档',
    3: '已停用'
  }
  return textMap[status || 0] || '未知'
}

const getQuestionTypeColor = (type?: number) => {
  const colorMap: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple',
    5: 'red'
  }
  return colorMap[type || 0] || 'default'
}

const getQuestionTypeText = (type?: number) => {
  const textMap: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '填空题',
    5: '简答题'
  }
  return textMap[type || 0] || '未知'
}

// 手动组卷
const handleManualAssembly = () => {
  router.push('/paper/assembly')
}

// ===== 策略管理 =====
const strategyVisible = ref(false)
const strategyLoading = ref(false)
const strategyList = ref<any[]>([])
const strategyDetailVisible = ref(false)
const strategyDetailTitle = ref('')
const strategyDetailLoading = ref(false)
const strategyEditingId = ref<number | undefined>()
const strategyForm = reactive({
  strategyName: '', totalScore: 100, difficultyAvg: 3, duration: 90
})
const strategyWeights = reactive([
  { key: 'difficulty', label: '难度权重', value: 30 },
  { key: 'accuracy', label: '正确率权重', value: 15 },
  { key: 'discrimination', label: '区分度权重', value: 20 },
  { key: 'calcLevel', label: '计算量权重', value: 10 },
  { key: 'examFrequency', label: '考频权重', value: 10 },
  { key: 'knowledgeCount', label: '考点关联数权重', value: 15 },
])
const weightSum = ref(100)
const updateWeightSum = () => {
  weightSum.value = strategyWeights.reduce((s, w) => s + w.value, 0)
}
const strategyPagination = reactive({ current: 1, pageSize: 10, total: 0 })
const strategyColumns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '名称', dataIndex: 'strategyName' },
  { title: '总分', dataIndex: 'totalScore', width: 80 },
  { title: '平均难度', dataIndex: 'difficultyAvg', width: 80 },
  { title: '权重和', key: 'weightSum', width: 80 },
  { title: '默认', key: 'isDefault', width: 60 },
  { title: '操作', key: 'action', width: 280 },
]

async function loadStrategyList() {
  strategyLoading.value = true
  try {
    const res = await listPaperStrategyByPage({ pageNum: strategyPagination.current, pageSize: strategyPagination.pageSize })
    if (res.data.code === 0 && res.data.data) {
      strategyList.value = res.data.data.records || []
      strategyPagination.total = res.data.data.totalRow || 0
    }
  } catch { /* ignore */ }
  strategyLoading.value = false
}

function handleStrategyTableChange(pag: { current: number; pageSize: number }) {
  strategyPagination.current = pag.current; strategyPagination.pageSize = pag.pageSize; loadStrategyList()
}

function handleStrategyDetail(record: any) {
  if (!record) { handleStrategyEdit(null); return }
  strategyDetailTitle.value = '策略详情 - ' + record.strategyName
  strategyEditingId.value = record.id
  strategyForm.strategyName = record.strategyName || ''
  strategyForm.totalScore = record.totalScore || 100
  strategyForm.difficultyAvg = record.difficultyAvg || 3
  strategyForm.duration = record.duration || 90
  if (record.weights?.length) {
    record.weights.forEach((w: any) => {
      const found = strategyWeights.find(sw => sw.key === w.weightType)
      if (found) found.value = w.weightValue || 0
    })
  }
  updateWeightSum()
  strategyDetailVisible.value = true
}

function handleStrategyEdit(record: any) {
  strategyDetailTitle.value = record ? '编辑策略 - ' + record.strategyName : '新建策略'
  strategyEditingId.value = record?.id
  if (record) { handleStrategyDetail(record) }
  else {
    strategyForm.strategyName = ''; strategyForm.totalScore = 100
    strategyForm.difficultyAvg = 3; strategyForm.duration = 90
    strategyWeights.forEach(w => w.value = w.key === 'difficulty' ? 30 : w.key === 'discrimination' ? 20 : w.key === 'accuracy' ? 15 : w.key === 'knowledgeCount' ? 15 : 10)
    updateWeightSum()
    strategyDetailVisible.value = true
  }
}

async function handleStrategySave() {
  if (!strategyForm.strategyName) { message.warning('请输入策略名称'); return }
  if (weightSum.value !== 100) { message.warning('6个指标权重之和必须等于100%，当前为' + weightSum.value + '%'); return }
  strategyDetailLoading.value = true
  try {
    const body: any = { ...strategyForm, weights: strategyWeights.map(w => ({ weightType: w.key, weightValue: w.value })) }
    let res
    if (strategyEditingId.value) {
      body.id = strategyEditingId.value
      res = await updatePaperStrategy(body)
    } else {
      res = await addPaperStrategy(body)
    }
    if (res.data.code === 0) { message.success(strategyEditingId.value ? '已更新' : '已创建'); strategyDetailVisible.value = false; loadStrategyList() }
    else { message.error(res.data.message) }
  } catch { message.error('请求失败') }
  strategyDetailLoading.value = false
}

async function handleStrategyCopy(id: number) {
  try {
    const res = await copyPaperStrategy(id)
    if (res.data.code === 0) { message.success('已复制'); loadStrategyList() }
    else { message.error(res.data.message) }
  } catch { message.error('请求失败') }
}

async function handleStrategySetDefault(id: number) {
  try {
    const res = await setDefaultStrategy(id)
    if (res.data.code === 0) { message.success('已设为默认'); loadStrategyList() }
    else { message.error(res.data.message) }
  } catch { message.error('请求失败') }
}

async function handleStrategyDelete(id: number) {
  Modal.confirm({
    title: '确认删除', content: '确定要删除此策略吗？',
    onOk: async () => {
      const res = await deletePaperStrategy(id)
      if (res.data.code === 0) { message.success('已删除'); loadStrategyList() }
      else { message.error(res.data.message) }
    }
  })
}

// ===== 试卷分享 =====
const shareVisible = ref(false)
const shareLoading = ref(false)
const sharePaperId = ref<number>()
const sharePaperName = ref('')
const shareMode = ref<'user'|'group'>('user')
const shareTargetId = ref<number>()
const shareRecords = ref<any[]>([])
const shareRecordLoading = ref(false)
const shareRecordColumns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '目标用户ID', dataIndex: 'targetUserId' },
  { title: '目标组ID', dataIndex: 'targetGroupId' },
  { title: '时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action', width: 80 },
]

async function handleStatusChange(record: PaperRecord, newStatus: number) {
  if (!record.id) return
  try {
    const res = await updateStatus1({ id: record.id, status: newStatus })
    if (res.data.code === 0) { message.success('状态已更新') }
    else { message.error(res.data.message || '状态更新失败'); loadPaperList() }
  } catch { message.error('状态更新请求失败'); loadPaperList() }
}

function handleShareToUser(record: PaperRecord) {
  sharePaperId.value = record.id
  sharePaperName.value = record.paperName || ''
  shareMode.value = 'user'
  shareTargetId.value = undefined
  shareVisible.value = true
  loadShareRecords()
}

async function loadShareRecords() {
  if (!sharePaperId.value) return
  shareRecordLoading.value = true
  try {
    const res = await getPaperShares(sharePaperId.value)
    if (res.data.code === 0) shareRecords.value = res.data.data || []
  } catch { /* ignore */ }
  shareRecordLoading.value = false
}

async function handleShareConfirm() {
  if (!shareTargetId.value) { message.warning('请输入目标ID'); return }
  shareLoading.value = true
  try {
    const fn = shareMode.value === 'user' ? sharePaperToUser : sharePaperToGroup
    const res = await fn(sharePaperId.value!, shareTargetId.value)
    if (res.data.code === 0) { message.success('分享成功'); loadShareRecords() }
    else { message.error(res.data.message) }
  } catch { message.error('请求失败') }
  shareLoading.value = false
}

async function handleRevokeShare(shareId: number) {
  try {
    const res = await revokeShare(shareId)
    if (res.data.code === 0) { message.success('已撤销'); loadShareRecords() }
    else { message.error(res.data.message) }
  } catch { message.error('请求失败') }
}

// ===== 分享给我的 =====
const sharedToMeVisible = ref(false)
const sharedToMeLoading = ref(false)
const sharedToMeList = ref<any[]>([])
const sharedToMeColumns = [
  { title: '试卷名称', dataIndex: 'paperName' },
  { title: '分享者ID', dataIndex: 'ownerId' },
  { title: '分享时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action', width: 80 },
]

async function loadSharedToMe() {
  sharedToMeVisible.value = true
  sharedToMeLoading.value = true
  try {
    const res = await getSharedToMe()
    if (res.data.code === 0 && res.data.data) {
      // Enrich with paper info (we need to look up each paper)
      const enriched: any[] = []
      for (const share of res.data.data) {
        try {
          const pRes = await getExamPaperById({ id: share.paperId! })
          if (pRes.data.code === 0 && pRes.data.data) {
            enriched.push({ ...share, ...pRes.data.data })
          }
        } catch { /* skip */ }
      }
      sharedToMeList.value = enriched
    }
  } catch { /* ignore */ }
  sharedToMeLoading.value = false
}

// AI组卷相关方法
const handleAIPaperAssembly = () => { handleAIOpen() }

const resetAIForm = () => {
  aiFormState.paperName = ''
  aiFormState.subject = undefined
  aiFormState.chapter = undefined
  aiFormState.difficulty = undefined
  aiFormState.totalScore = undefined
  aiFormState.status = 0
  aiFormState.userRequirement = ''
}

// Load strategy list when strategyVisible changes
watch(strategyVisible, (v) => { if (v) loadStrategyList() })
</script>

<style scoped>
.paper-manager {
  padding: 0;
}

.card-title {
  font-size: 18px;
  font-weight: 500;
}

.filter-section {
  margin-bottom: 16px;
}

.table-operations {
  margin-bottom: 16px;
}

.question-manage-content {
  min-height: 400px;
}

.question-manage-operations {
  margin-bottom: 16px;
}

.select-question-content {
  min-height: 500px;
}

.select-question-filters {
  margin-bottom: 16px;
}
</style>
