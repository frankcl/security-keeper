import { reactive } from 'vue'

export const formRules = reactive({
  name: [
    { required: true, message: '请输入角色名称', trigger: 'change' }
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ]
})