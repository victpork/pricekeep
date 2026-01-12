import { h } from 'vue'
import { type ProductInfo } from '@/model'
import { Badge } from '@/components/ui/badge'
import type { ColumnDef } from '@tanstack/vue-table'
import { capitalise } from '@/util/capitalise'

//const colHelper = createColumnHelper<ProductInfo>()

export const defaultColumns: ColumnDef<ProductInfo>[] = [
  {
    accessorKey: 'name',
    header: () => h('div', { class: 'text-left' }, 'Name'),
    size: 150,
    cell: ({ row }) => {
      return h('a', { class: 'text-left font-medium max-w-[150px] overflow-hidden', href: `/products/${row.original.id}` }, capitalise(row.getValue('name')))
    },
  },
  {
    accessorKey: 'description',
    header: () => h('div', { class: 'text-left' }, 'Description'),
    cell: ({ row }) => {
      return h('div', { class: 'text-left font-medium min-w-[150px]' }, row.original.desc ?? '')
    },
  },
  {
    accessorKey: 'latestQuotes',
    header: () => h('div', { class: 'text-right' }, 'Price'),
    cell: ({ row }) => {
      const quotes = row.original.latestQuotes
      if (!quotes || quotes.length === 0) {
        return h('div', { class: 'text-center italic font-medium' }, 'no price data')
      }
      const prices = quotes.map(q => q.price)
      const min = Math.min(...prices)
      const max = Math.max(...prices)
      const formatter = new Intl.NumberFormat('en-NZ', {
        style: 'currency',
        currency: 'NZD',
      })
      if (min === max) {
        return h('div', { class: 'text-right font-medium' }, formatter.format(min))
      }
      const formatted = `${formatter.format(min)} - ${formatter.format(max)}`

      return h('div', { class: 'text-right font-medium' }, formatted)
    },
  },
  {
    accessorKey: 'unitPrice',
    header: () => h('div', { class: 'text-right font-medium' }, 'Unit Price'),
    cell: ({ row }) => {
      const quotes = row.original.latestQuotes
      if (!quotes || quotes.length === 0) {
        return h('div', { class: 'text-center italic font-medium' }, 'no price data')
      }
      const unitPrices = quotes.map(q => q.unitPrice)
      const unit = quotes?.[0]?.unit?.toLowerCase() ?? ''
      const min = Math.min(...unitPrices)
      const max = Math.max(...unitPrices)
      const formatter = new Intl.NumberFormat('en-NZ', {
        style: 'currency',
        currency: 'NZD',
      })
      if (min === max) {
        return h('div', { class: 'text-right font-medium' }, formatter.format(min) + " /" + unit)
      }
      const formatted = `${formatter.format(min)} - ${formatter.format(max)}`

      return h('div', { class: 'text-right font-medium' }, formatted + " /" + unit)
    },
  },
  {
    accessorKey: 'tags',
    header: () => h('div', { class: 'text-left w-[100px]' }, 'Tags'),
    minSize: 100,
    cell: ({ row }) => {
      const renderedTags = row.original.tags?.map(tag => h(Badge, { variant: 'outline' }, () => tag)) ?? h('div', { class: 'text-center italic font-medium' }, '')
      return h('div', { class: 'flex space-x-2' }, renderedTags)
    },
  }
]

/*
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

  colHelper.accessor(row => (
    ("name" in row.storeInfo) ? row.storeInfo.name :
      fetchStoreInfo(row.id ?? 0).name
  ), {
    id: 'store'
  }),
  
  colHelper.accessor(row => row.tags, {
    id: 'tags',
    cell: props => {
      const renderedTags = props.getValue()?.map(tag => h(Badge, { variant: 'outline' }, () => tag))
      return h('div', { class: 'flex space-x-2' }, renderedTags)
    }
  }),
]
*/