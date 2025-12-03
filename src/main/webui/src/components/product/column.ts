import { h } from 'vue'
import { createColumnHelper } from '@tanstack/vue-table'
import { type ProductInfo, type StoreInfo } from '@/model'
import { Badge } from '@/components/ui/badge'

const fetchStoreInfo = (id: number): StoreInfo => {
  return { id: id, name: "Temp", address: "tmp address" }
}

const colHelper = createColumnHelper<ProductInfo>()

export const defaultColumns = [
  colHelper.accessor(row => row.name, {
    id: 'name',
  }),
  colHelper.accessor(row => row.latestQuotes, {
    id: 'price',
    cell: props => {
      const quotes = props.getValue()
      if (!quotes || quotes.length === 0) {
        return h('div', { class: 'text-right font-medium' }, '-')
      }

      const prices = quotes.map(q => q.price)
      const min = Math.min(...prices)
      const max = Math.max(...prices)

      const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'NZD',
      })

      const formatted = `${formatter.format(min)} - ${formatter.format(max)}`

      return h('div', { class: 'text-right font-medium' }, formatted)
    }
  }),
  /*
  colHelper.accessor(row => (
    ("name" in row.storeInfo) ? row.storeInfo.name :
      fetchStoreInfo(row.id ?? 0).name
  ), {
    id: 'store'
  }),
  */
  colHelper.accessor(row => row.tags, {
    id: 'tags',
    cell: props => {
      const renderedTags = props.getValue()?.sort().map(tag => h(Badge, { variant: 'outline' }, () => tag))
      return h('div', { class: 'flex space-x-2' }, renderedTags)
    }
  }),
]