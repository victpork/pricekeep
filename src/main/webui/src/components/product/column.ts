import { h } from 'vue'
import { createColumnHelper } from '@tanstack/vue-table'
import { type ProductInfo } from '@/model'
import { Badge } from '@/components/ui/badge'


const colHelper = createColumnHelper<ProductInfo>()

export const defaultColumns = [
  colHelper.accessor(row => row.id, {
    id: 'id',
    cell: props => {
      return h('div', { class: 'text-left' }, props.getValue())
    }
  }),
  colHelper.accessor(row => row.name, {
    id: 'name',
    minSize: 200,
    cell: props => {
      return h('a', { class: 'text-left font-medium min-w-[200px]', href: `/products/${props.row.original.id}` }, props.getValue())
    }
  }),
  colHelper.accessor((row) => row.desc, {
    id: 'description',
    cell: (props) => {
      return h('div', { class: 'text-left font-medium min-w-[200px]' }, props.getValue())
    }
  }),
  colHelper.accessor(row => row.latestQuotes, {
    id: 'price',
    cell: props => {
      const quotes = props.getValue()
      if (!quotes || quotes.length === 0) {
        return h('div', { class: 'text-center italic font-medium' }, 'no price data')
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
      const renderedTags = props.getValue()?.map(tag => h(Badge, { variant: 'outline' }, () => tag))
      return h('div', { class: 'flex space-x-2' }, renderedTags)
    }
  }),
]