import { createWebHistory, createRouter, type RouteLocationNormalized } from 'vue-router'
import SingleProduct from '@/views/SingleProduct.vue'
import Dashboard from '@/views/Dashboard.vue'
import layout from './layout/layout.vue'
import ProductColleciton from '@/views/ProductColleciton.vue'
import ManageDataSource from '@/views/ManageDataSource.vue'
import NotFound from '@/views/NotFound.vue'
const routes = [
  {
    path: '/',
    component: layout,
    children: [
      { path: '', redirect: { name: 'dashboard' } },
      { path: 'dashboard', name: 'dashboard', component: Dashboard },
      { path: 'batches', name: 'batches', component: ManageDataSource },
      {
        path: 'products/:id(\\d+)',
        component: SingleProduct,
        props: (route: RouteLocationNormalized) => ({ id: Number(route.params.id) })
      },
      { path: 'products/tags/:tag', component: ProductColleciton },
      { path: ':pathMatch(.*)*', name: 'NotFound', component: NotFound },
    ]
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router