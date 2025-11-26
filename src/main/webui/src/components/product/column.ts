import { h } from 'vue'
import { createColumnHelper} from '@tanstack/vue-table'
import { type QuoteDTO, type StoreInfo } from '@/model'
import { Badge } from '@/components/ui/badge'

const fetchStoreInfo = (id: number): StoreInfo => {
  return { id: id, name: "Temp", address: "tmp address" }
}

const colHelper = createColumnHelper<QuoteDTO>()

export const defaultColumns = [
  colHelper.accessor(row => row.productInfo?.name, {
    id: 'name',
  }),
  colHelper.accessor(row => row.price, {
    id: 'price',
    cell: props => {
      const amount = props.getValue()
      const formatted = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'NZD',
      }).format(amount)

      return h('div', { class: 'text-right font-medium' }, formatted)
    }
  }),
  colHelper.accessor(row => (
    ("name" in row.storeInfo) ? row.storeInfo.name :
      fetchStoreInfo(row.id ?? 0).name
  ), {
    id: 'store'
  }),
  colHelper.accessor(row => row.productInfo?.tags, {
    id: 'tags',
    cell: props => {
      const renderedTags = props.getValue()?.sort().map(tag => h(Badge, { variant: 'outline' }, () => tag))
      return h('div', { class: 'flex space-x-2' }, renderedTags)
    }
  }),
]