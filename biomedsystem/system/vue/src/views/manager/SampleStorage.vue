<template>
  <div class="storage-page">
    <div class="page-title">样本存储信息管理</div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.position"
          style="width: 260px; margin-right: 10px"
          placeholder="请输入存储位置查询"
          clearable
          @keyup.enter="load"
      />
      <el-select v-model="data.status" placeholder="存储状态" clearable style="width: 160px; margin-right: 10px">
        <el-option label="在库" value="在库" />
        <el-option label="使用" value="使用" />
        <el-option label="过期" value="过期" />
        <el-option label="销毁" value="销毁" />
        <el-option label="正常" value="正常" />
        <el-option label="待核查" value="待核查" />
        <el-option label="已出库" value="已出库" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增存储记录</el-button>
        <el-alert
            title="也可以在新增/编辑样本时直接填写存储位置；如果该位置不存在，系统会自动生成存储记录并关联样本。"
            type="info"
            :closable="false"
            show-icon
            style="margin-top: 10px"
        />
      </div>

      <el-table :data="data.tableData" stripe v-loading="loading" border style="width: 100%">
        <el-table-column label="存储ID" prop="id" width="90" />
        <el-table-column label="关联样本" prop="sampleName" min-width="160" />
        <el-table-column label="入库数量" prop="number" width="100" />
        <el-table-column label="存储位置" prop="storageLocation" min-width="160" />
        <el-table-column label="入库/到期日期" prop="storageDate" width="130" />
        <el-table-column label="保管人" prop="custodian" width="110" />
        <el-table-column label="存储状态" prop="storageStatus" width="110">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.storageStatus)" size="small">
              {{ scope.row.storageStatus || '在库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注/温度" prop="comment" min-width="180" />
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @size-change="load"
          @current-change="load"
          style="text-align: right; padding: 10px 0"
      />
    </div>

    <el-dialog
        title="样本存储信息"
        width="50%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form :model="data.form" label-width="110px" style="padding-right: 20px" :rules="formRules" ref="formRef">
        <el-form-item label="关联样本" prop="sampleId">
          <el-select v-model="data.form.sampleId" placeholder="可选择关联样本" clearable style="width: 100%" @change="getSampleInfo">
            <el-option
                v-for="item in data.sampleList"
                :key="item.id"
                :label="getSampleLabel(item)"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="number">
          <el-input-number :min="1" v-model="data.form.number" placeholder="请输入入库数量" style="width: 100%" />
        </el-form-item>
        <el-form-item label="存储位置" prop="storageLocation">
          <el-input v-model="data.form.storageLocation" placeholder="请输入存储位置（如：冷库A-01柜）" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="日期" prop="storageDate">
          <el-date-picker v-model="data.form.storageDate" placeholder="请选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="保管人" prop="custodian">
          <el-input v-model="data.form.custodian" placeholder="请输入保管人姓名" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="存储状态" prop="storageStatus">
          <el-select v-model="data.form.storageStatus" placeholder="请选择存储状态" style="width: 100%">
            <el-option label="在库" value="在库" />
            <el-option label="使用" value="使用" />
            <el-option label="过期" value="过期" />
            <el-option label="销毁" value="销毁" />
            <el-option label="正常" value="正常" />
            <el-option label="待核查" value="待核查" />
            <el-option label="已出库" value="已出库" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注/温度" prop="comment">
          <el-input v-model="data.form.comment" placeholder="请输入备注或温度，如：-80℃、样本保存要求" type="textarea" rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { reactive, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const formRef = ref(null)

const formRules = {
  storageLocation: [{ required: true, message: '请输入存储位置', trigger: 'blur' }],
  storageStatus: [{ required: true, message: '请选择存储状态', trigger: 'change' }]
}

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: { storageStatus: '在库', number: 1 },
  tableData: [],
  sampleList: [],
  position: '',
  status: ''
})

const safeParseTemp = (temp) => {
  if (!temp) return {}
  if (typeof temp !== 'string') return {}
  const text = temp.trim()
  if (!text.startsWith('{')) {
    return { comment: text }
  }
  try {
    return JSON.parse(text)
  } catch {
    return { comment: text }
  }
}

const mapFrontToBack = (formData) => {
  return {
    id: formData.id || null,
    position: formData.storageLocation,
    status: formData.storageStatus || '在库',
    expireTime: formData.storageDate ? `${formData.storageDate}T00:00:00` : null,
    temp: JSON.stringify({
      sampleId: formData.sampleId || null,
      number: formData.number || 1,
      custodian: formData.custodian || '',
      comment: formData.comment || ''
    })
  }
}

const mapBackToFront = (backData) => {
  const extra = safeParseTemp(backData.temp)
  const frontData = {
    id: backData.id,
    storageLocation: backData.position,
    storageStatus: backData.status || '在库',
    storageDate: backData.expireTime ? dayjs(backData.expireTime).format('YYYY-MM-DD') : '',
    sampleId: extra.sampleId || null,
    number: extra.number || 1,
    custodian: extra.custodian || '',
    comment: extra.comment || ''
  }
  if (frontData.sampleId) {
    const sample = data.sampleList.find(item => item.id == frontData.sampleId)
    frontData.sampleName = sample ? getSampleLabel(sample) : `样本#${frontData.sampleId}`
  } else {
    frontData.sampleName = '未指定'
  }
  return frontData
}

const getSampleLabel = (item) => {
  if (!item) return ''
  return `${item.sampleName || item.source || '未命名样本'}（${item.sampleNo || item.id}）`
}

const getStatusTag = (status) => {
  if (status === '在库' || status === '正常') return 'success'
  if (status === '待核查' || status === '使用') return 'warning'
  if (status === '过期' || status === '销毁') return 'danger'
  return 'info'
}

const getSampleList = async () => {
  try {
    const res = await request.get('/biomedSample/selectAll')
    data.sampleList = res.data || []
  } catch {
    data.sampleList = []
    ElMessage.error('获取样本列表失败')
  }
}

const getSampleInfo = () => {
  const sample = data.sampleList.find(item => item.id === data.form.sampleId)
  if (sample && !data.form.storageLocation) {
    data.form.storageLocation = sample.queue || ''
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/biomedStorage/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        position: data.position,
        status: data.status
      }
    })
    const pageData = res.data || {}
    data.tableData = (pageData.list || []).map(item => mapBackToFront(item))
    data.total = pageData.total || 0
  } catch {
    data.tableData = []
    data.total = 0
  } finally {
    loading.value = false
  }
}

const handleAdd = async () => {
  data.form = { storageStatus: '在库', number: 1 }
  data.formVisible = true
  await getSampleList()
}

const handleEdit = async (row) => {
  await getSampleList()
  data.form = { ...row }
  data.formVisible = true
}

const add = async () => {
  const submitData = mapFrontToBack(data.form)
  const res = await request.post('/biomedStorage/add', submitData)
  if (res.code === '200') {
    await load()
    data.formVisible = false
    ElMessage.success('新增成功')
  }
}

const update = async () => {
  const submitData = mapFrontToBack(data.form)
  const res = await request.put('/biomedStorage/update', submitData)
  if (res.code === '200') {
    await load()
    data.formVisible = false
    ElMessage.success('编辑成功')
  }
}

const save = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填项')
      return
    }
    try {
      data.form.id ? await update() : await add()
    } catch {
      ElMessage.error('保存失败')
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除？如果已有样本关联该存储记录，建议先解除关联。', '提示', { type: 'warning' })
      .then(async () => {
        await request.delete('/biomedStorage/delete/' + id)
        await load()
        ElMessage.success('删除成功')
      })
      .catch(() => {})
}

const reset = () => {
  data.position = ''
  data.status = ''
  data.pageNum = 1
  load()
}

const init = async () => {
  await getSampleList()
  await load()
}

init()
</script>

<style scoped>
.storage-page { padding: 15px; background-color: #f5f7fa; min-height: calc(100vh - 20px); }
.page-title { font-size: 18px; font-weight: 600; margin-bottom: 15px; color: #1989fa; }
.card { background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05); margin-bottom: 10px; }
.el-tag { font-size: 12px; }
.dialog-footer { text-align: right; }
</style>
