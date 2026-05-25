<template>
  <div class="statistics-page">
    <h2 style="margin-bottom: 16px;">成绩统计与可视化</h2>

    <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
      <StatisticsFilterBar
        :papers="availablePapers"
        :loading="loading"
        @query="handleQuery"
      />
      <a-button v-if="stats" @click="handleExportExcel" size="small" type="primary">
        导出统计Excel
      </a-button>
    </div>

    <a-spin :spinning="loading" style="margin-top: 16px;">
      <template v-if="stats">
        <a-tabs v-model:activeKey="activeTab" type="card">
          <a-tab-pane key="overview" tab="整体概览">
            <StatisticsOverview :stats="stats" />
          </a-tab-pane>
          <a-tab-pane key="dimension" tab="维度分析">
            <StatisticsDimensionCharts :stats="stats" />
          </a-tab-pane>
          <a-tab-pane key="comparison" tab="个人对比">
            <StatisticsComparison :paper-id="selectedPaperId" />
          </a-tab-pane>
          <a-tab-pane key="wrong" tab="高频错题">
            <StatisticsWrongQuestions
              :wrong-questions="stats.highFreqWrongQuestions || []"
              :paper-id="selectedPaperId"
            />
          </a-tab-pane>
          <a-tab-pane key="trend" tab="成绩趋势">
            <StatisticsTrend />
          </a-tab-pane>
        </a-tabs>
      </template>
      <a-empty v-else-if="!loading" description="请选择一份试卷查看成绩统计" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getPaperStatistics, getAvailablePapers, exportStatisticsExcel } from '@/api/tongji'
import StatisticsFilterBar from '@/components/statistics/StatisticsFilterBar.vue'
import StatisticsOverview from '@/components/statistics/StatisticsOverview.vue'
import StatisticsDimensionCharts from '@/components/statistics/StatisticsDimensionCharts.vue'
import StatisticsComparison from '@/components/statistics/StatisticsComparison.vue'
import StatisticsWrongQuestions from '@/components/statistics/StatisticsWrongQuestions.vue'
import StatisticsTrend from '@/components/statistics/StatisticsTrend.vue'

const availablePapers = ref<API.PaperStatisticsVO[]>([])
const stats = ref<API.PaperStatisticsVO | null>(null)
const loading = ref(false)
const selectedPaperId = ref<number | undefined>(undefined)
const activeTab = ref('overview')

onMounted(async () => {
  try {
    const res = await getAvailablePapers()
    if (res.data.code === 0) {
      availablePapers.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载试卷列表失败', e)
  }
})

const handleQuery = async (paperId: number) => {
  selectedPaperId.value = paperId
  loading.value = true
  try {
    const res = await getPaperStatistics({ paperId })
    if (res.data.code === 0 && res.data.data) {
      stats.value = res.data.data
      activeTab.value = 'overview'
    } else {
      message.error(res.data.message || '加载统计失败')
      stats.value = null
    }
  } catch (e) {
    message.error('加载统计失败')
    stats.value = null
  } finally {
    loading.value = false
  }
}

const handleExportExcel = async () => {
  if (!selectedPaperId.value) return
  try {
    const res = await exportStatisticsExcel({ paperId: selectedPaperId.value })
    const blob = res.data instanceof Blob ? res.data : new Blob([res.data as any])
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = '成绩统计-' + new Date().toISOString().slice(0, 10) + '.xlsx'
    a.click()
    message.success('导出成功')
  } catch { message.error('导出失败') }
}
</script>

<style scoped>
.statistics-page {
  padding: 0;
}
</style>
