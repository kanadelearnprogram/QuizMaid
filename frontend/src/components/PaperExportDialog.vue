<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { exportWord, exportPdf, previewPaper, listExportFiles, deleteExportFile, batchExportPapers, downloadExportFile } from '@/api/shijuanguanli'
import ExportTemplateManager from './ExportTemplateManager.vue'

const props = defineProps<{ paperId?: number; paperName?: string; paperIds?: number[] }>()
const emit = defineEmits(['close'])

const showAnswer = ref(true)
const showAnalysis = ref(false)
const exportType = ref<'Word'|'PDF'>('Word')
const previewHtml = ref('')
const previewLoading = ref(false)
const files = ref<API.ExportFileVO[]>([])
const loading = ref(false)
const selectedTemplateId = ref<number | undefined>()
const showTemplates = ref(false)
const editable = ref(false)

watch(() => props.paperId, async (id) => {
  if (id) {
    previewLoading.value = true
    try {
      const res = await previewPaper({ paperId: id, showAnswer: showAnswer.value, showAnalysis: showAnalysis.value })
      if (res.data.code === 0 && res.data.data) previewHtml.value = res.data.data
    } catch { /* ignore */ } finally { previewLoading.value = false }
  }
}, { immediate: true })

async function refreshPreview() {
  if (!props.paperId) return
  previewLoading.value = true
  try {
    const res = await previewPaper({ paperId: props.paperId, showAnswer: showAnswer.value, showAnalysis: showAnalysis.value })
    if (res.data.code === 0 && res.data.data) previewHtml.value = res.data.data
  } catch { /* ignore */ } finally { previewLoading.value = false }
}

async function loadFiles() {
  try {
    const res = await listExportFiles()
    if (res.data.code === 0 && res.data.data) files.value = res.data.data
  } catch { /* ignore */ }
}

loadFiles()

function downloadBlob(data: any, fileName: string) {
  const blob = data instanceof Blob ? data : new Blob([data])
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = fileName; a.click()
  URL.revokeObjectURL(url)
}

async function handleExport() {
  loading.value = true
  try {
    const config: any = { paperId: props.paperId, showAnswer: showAnswer.value, showAnalysis: showAnalysis.value }
    if (selectedTemplateId.value) config.templateId = selectedTemplateId.value
    if (exportType.value === 'PDF') config.editable = editable.value
    const fn = exportType.value === 'Word' ? exportWord : exportPdf
    const res = await fn(config)
    const ext = exportType.value === 'Word' ? 'docx' : 'pdf'
    downloadBlob(res.data, (props.paperName || '试卷') + '.' + ext)
    message.success('导出成功')
    loadFiles()
  } catch { message.error('导出失败') } finally { loading.value = false }
}

async function handleBatchExport() {
  if (!props.paperIds?.length) { message.warning('请先选择试卷'); return }
  loading.value = true
  try {
    const config: any = { paperIds: props.paperIds, showAnswer: showAnswer.value, showAnalysis: showAnalysis.value }
    if (selectedTemplateId.value) config.templateId = selectedTemplateId.value
    const res = await batchExportPapers(config)
    downloadBlob(res.data, '批量导出-' + new Date().toISOString().slice(0,10) + '.zip')
    message.success(`已导出 ${props.paperIds.length} 份试卷`)
    loadFiles()
  } catch { message.error('批量导出失败') } finally { loading.value = false }
}

async function handleDownloadFile(fileName: string) {
  try {
    const res = await downloadExportFile(fileName)
    downloadBlob(res.data, fileName)
  } catch { message.error('下载失败') }
}

function onTemplateSelect(template: any) {
  selectedTemplateId.value = template.id
  if (template.config) {
    try {
      const cfg = JSON.parse(template.config)
      if (cfg.showAnswer !== undefined) showAnswer.value = cfg.showAnswer
      if (cfg.showAnalysis !== undefined) showAnalysis.value = cfg.showAnalysis
    } catch { /* ignore */ }
  }
}

async function handleDelete(index: number) {
  const res = await deleteExportFile(index)
  if (res.data.code === 0) { message.success('已删除'); loadFiles() }
}
</script>

<template>
  <a-modal :open="true" title="导出试卷" width="75%" @cancel="emit('close')" :footer="null">
    <a-row :gutter="16">
      <!-- 左侧配置 -->
      <a-col :span="8">
        <a-card title="导出配置" size="small">
          <a-form layout="vertical">
            <a-form-item label="导出格式">
              <a-radio-group v-model:value="exportType">
                <a-radio-button value="Word">Word (.docx)</a-radio-button>
                <a-radio-button value="PDF">PDF</a-radio-button>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="导出答案">
              <a-switch v-model:checked="showAnswer" @change="refreshPreview" />
            </a-form-item>
            <a-form-item label="导出解析">
              <a-switch v-model:checked="showAnalysis" @change="refreshPreview" />
            </a-form-item>
            <a-form-item v-if="exportType==='PDF'" label="可编辑PDF">
              <a-switch v-model:checked="editable" />
            </a-form-item>
            <a-form-item label="模板">
              <a-space>
                <a-select v-model:value="selectedTemplateId" placeholder="使用模板(可选)" allow-clear style="width:160px" size="small">
                  <a-select-option v-for="tid in [undefined]" :key="'none'" :value="undefined">不使用</a-select-option>
                </a-select>
                <a-button size="small" @click="showTemplates = !showTemplates">{{ showTemplates ? '收起' : '管理模板' }}</a-button>
              </a-space>
            </a-form-item>
            <a-space direction="vertical" style="width:100%">
              <a-button type="primary" block @click="handleExport" :loading="loading">
                导出{{ exportType }}
              </a-button>
              <a-button v-if="paperIds?.length" block @click="handleBatchExport" :loading="loading">
                批量导出ZIP ({{ paperIds.length }}份)
              </a-button>
            </a-space>
          </a-form>
        </a-card>

        <a-card title="导出记录" size="small" style="margin-top:12px">
          <div v-for="(f, i) in files" :key="i" style="display:flex;justify-content:space-between;align-items:center;padding:4px 0;border-bottom:1px solid #f0f0f0">
            <span style="font-size:12px;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ f.fileName }}</span>
            <a-space>
              <a-button type="link" size="small" @click="handleDownloadFile(f.fileName || '')">下载</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(i)">删除</a-button>
            </a-space>
          </div>
          <div v-if="!files.length" style="color:#999;font-size:12px;text-align:center;padding:12px">暂无导出记录</div>
        </a-card>

        <!-- 模板管理 -->
        <div v-if="showTemplates" style="margin-top:12px">
          <ExportTemplateManager :selected-id="selectedTemplateId" @select="onTemplateSelect" />
        </div>
      </a-col>

      <!-- 右侧预览 -->
      <a-col :span="16">
        <a-card title="预览" size="small">
          <a-spin :spinning="previewLoading">
            <div v-if="previewHtml" class="preview-frame" v-html="previewHtml" />
            <div v-else style="color:#999;text-align:center;padding:48px">加载预览中...</div>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </a-modal>
</template>

<style scoped>
.preview-frame {
  border: 1px solid #e8e8e8; padding: 16px; min-height: 400px;
  max-height: 500px; overflow-y: auto; background: #fff;
}
</style>
