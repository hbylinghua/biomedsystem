<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  queryForm: {
    keyword: ''
  }
})

const logForm = reactive({
  id: null,
  moduleName: '',
  operType: '',
  operDesc: '',
  operUser: '',
  operTime: ''
})

const rules = {
  moduleName: [{ required: true, message: '请输入模块名称', trigger: 'blur' }],
  operType: [{ required: true, message: '请输入操作类型', trigger: 'blur' }],
  operDesc: [{ required: true, message: '请输入操作描述', trigger: 'blur' }]
}

const isSuccess = (res) => {
  return !!res && (res.code === 200 || res.code === '200')
}

const resetLogForm = () => {
  logForm.id = null
  logForm.moduleName = ''
  logForm.operType = ''
  logForm.operDesc = ''
  logForm.operUser = ''
  logForm.operTime = ''
}

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/biomedOperLog/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        keyword: data.queryForm.keyword
      }
    })

    if (isSuccess(res)) {
      const pageData = res.data || {}
      data.tableData = pageData.list || []
      data.total = pageData.total || 0
    } else {
      data.tableData = []
      data.total = 0
      ElMessage.error(res.msg || '日志查询失败')
    }
  } catch (err) {
    console.error('日志查询失败：', err)
    data.tableData = []
    data.total = 0
    ElMessage.error('日志查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  data.pageNum = 1
  load()
}

const resetSearch = () => {
  data.queryForm.keyword = ''
  data.pageNum = 1
  load()
}

const handleAdd = () => {
  resetLogForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetLogForm()
  Object.assign(logForm, row)
  dialogVisible.value = true
}

const submitLog = () => {
  if (!formRef.value) return

  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const api = logForm.id ? '/biomedOperLog/update' : '/biomedOperLog/add'
      const res = await request.post(api, logForm)

      if (isSuccess(res)) {
        ElMessage.success(logForm.id ? '日志修改成功' : '日志新增成功')
        dialogVisible.value = false
        load()
      } else {
        ElMessage.error(res.msg || (logForm.id ? '修改失败' : '新增失败'))
      }
    } catch (err) {
      console.error('日志提交失败：', err)
      ElMessage.error(logForm.id ? '修改失败' : '新增失败')
    }
  })
}

const del = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条日志吗？', '提示', {
      type: 'warning'
    })

    const res = await request.delete(`/biomedOperLog/delete/${id}`)

    if (isSuccess(res)) {
      ElMessage.success('日志删除成功')

      if (data.tableData.length === 1 && data.pageNum > 1) {
        data.pageNum -= 1
      }

      load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      console.error('日志删除失败：', err)
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (val) => {
  data.pageSize = val
  data.pageNum = 1
  load()
}

const handleCurrentChange = (val) => {
  data.pageNum = val
  load()
}

onMounted(() => {
  load()
})
</script>