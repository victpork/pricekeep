import { createMemoryHistory, createRouter } from 'vue-router'
import SingleProduct from '@/views/SingleProduct.vue'
import Dashboard from '@/views/Dashboard.vue'
import layout from './layout/layout.vue'
import ProductColleciton from '@/views/ProductColleciton.vue'

const routes = [
  {
    path: '/',
    component: layout,
    children: [
      {
        path: 'dashboard',
        component: Dashboard
      }
    ]
  },
  { path: '/products/:id(\\d+)', component: SingleProduct },
  { path: '/products/tags/:tag', component: ProductColleciton },

]

const router = createRouter({
  history: createMemoryHistory(),
  routes,
})

export default router