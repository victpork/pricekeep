import { createWebHistory, createRouter, type RouteLocationNormalized } from 'vue-router'
import layout from './layout/layout.vue'

const dashboard = () => import('@/views/Dashboard.vue')
const manageDataSource = () => import('@/views/ManageDataSource.vue')
const singleProduct = () => import('@/views/SingleProduct.vue')
const productColleciton = () => import('@/views/ProductColleciton.vue')
const notFound = () => import('@/views/NotFound.vue')
const routes = [
  {
    path: '/',
    component: layout,
    children: [
      { path: '', redirect: { name: 'dashboard' } },
      { path: 'dashboard', name: 'dashboard', component: dashboard },
      { path: 'batches', name: 'batches', component: manageDataSource },
      {
        path: 'products/:id(\\d+)',
        name: 'product',
        component: singleProduct,
        props: (route: RouteLocationNormalized) => ({ id: Number(route.params.id) })
      },
      { path: 'products/tags/:tag', name: 'tag', component: productColleciton },
      { path: ':pathMatch(.*)*', name: 'NotFound', component: notFound },
    ]
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router