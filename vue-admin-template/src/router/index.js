import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    // component: () => import('@/views/ea/login/index'),
    component: () => import('@/views/login'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/info',
    hidden: true,
    children: [{
      path: 'info',
      name: 'Info',
      component: () => import('@/views/ea/admin/info/index'),
      meta: { title: '个人信息', icon: 'user' }
    }]
  },

  {
    path: '/admin/userManage',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'UserManage',
        component: () => import('@/views/ea/admin/userManage/index'),
        meta: { title: '用户管理', icon: 'user' }
      }
    ]
  },

  {
    path: '/admin/unfreezeAudit',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'UnfreezeAudit',
        component: () => import('@/views/ea/admin/unfreezeAudit/index'),
        meta: { title: '解冻审核', icon: 'user' }
      }
    ]
  },

  {
    path: '/admin/fileManage',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'FileManage',
        component: () => import('@/views/ea/admin/fileManage/index'),
        meta: { title: '文件管理', icon: 'user' }
      }
    ]
  },

  {
    path: '/admin/operatingRecord',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'OperatingRecord',
        component: () => import('@/views/ea/admin/operatingRecord/index'),
        meta: { title: '操作记录', icon: 'user' }
      }
    ]
  },

  {
    path: '/admin/algorithmManage',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'AlgorithmManage',
        component: () => import('@/views/ea/admin/algorithmManage/index'),
        meta: { title: '算法管理', icon: 'user' }
      }
    ]
  },

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
