import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { public: true } },
  {
    path: '/', component: () => import('../views/layout/MainLayout.vue'),
    children: [
      { path: '', redirect: '/student/home' },
      // Student routes
      { path: 'student/home', component: () => import('../views/student/StudentHome.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/career', component: () => import('../views/student/CareerSearch.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/assessment', component: () => import('../views/student/AssessmentList.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/assessment/:typeId', component: () => import('../views/student/AssessmentTake.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/result/:resultId', component: () => import('../views/student/AssessmentResult.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/chat', component: () => import('../views/student/AiChat.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/training', component: () => import('../views/student/TrainingList.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/training/:id', component: () => import('../views/student/TrainingSubmit.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/resume', component: () => import('../views/student/ResumeOptimizer.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/career-tools', component: () => import('../views/student/CareerTools.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/interview', component: () => import('../views/student/MockInterview.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/exam', component: () => import('../views/student/WrittenExam.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/calendar', component: () => import('../views/student/CareerCalendar.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/salary', component: () => import('../views/student/SalaryVisual.vue'), meta: { role: 'ROLE_STUDENT' } },
      { path: 'student/learning-path', component: () => import('../views/student/LearningPath.vue'), meta: { role: 'ROLE_STUDENT' } },
      // Teacher routes
      { path: 'teacher/dashboard', component: () => import('../views/teacher/TeacherDashboard.vue'), meta: { role: 'ROLE_TEACHER' } },
      { path: 'teacher/students', component: () => import('../views/teacher/StudentManage.vue'), meta: { role: 'ROLE_TEACHER' } },
      { path: 'teacher/tasks', component: () => import('../views/teacher/TaskList.vue'), meta: { role: 'ROLE_TEACHER' } },
      { path: 'teacher/reports', component: () => import('../views/teacher/ReportManage.vue'), meta: { role: 'ROLE_TEACHER' } },
      { path: 'teacher/grades', component: () => import('../views/teacher/GradeOverview.vue'), meta: { role: 'ROLE_TEACHER' } },
      { path: 'teacher/stats', component: () => import('../views/teacher/TaskStats.vue'), meta: { role: 'ROLE_TEACHER' } },
      { path: 'profile', component: () => import('../views/Profile.vue') },
      // Admin routes
      { path: 'admin/dashboard', component: () => import('../views/admin/AdminDashboard.vue'), meta: { role: 'ROLE_ADMIN' } },
      { path: 'admin/users', component: () => import('../views/admin/UserManage.vue'), meta: { role: 'ROLE_ADMIN' } },
      { path: 'admin/careers', component: () => import('../views/admin/CareerManage.vue'), meta: { role: 'ROLE_ADMIN' } },
      { path: 'admin/config', component: () => import('../views/admin/SystemConfig.vue'), meta: { role: 'ROLE_ADMIN' } },
      { path: 'admin/training', component: () => import('../views/admin/TrainingManage.vue'), meta: { role: 'ROLE_ADMIN' } },
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('../views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.meta.public) {
    if (token && to.name === 'Login') { next('/'); return }
    next(); return
  }
  if (!token) { next('/login'); return }
  next()
})

export default router
