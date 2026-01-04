import { h } from 'vue'
import { type ProductInfo } from '@/model'
import { Badge } from '@/components/ui/badge'
import type { ColumnDef } from '@tanstack/vue-table'
import { capitalise } from '@/util/capitalise'

//const colHelper = createColumnHelper<ProductInfo>()

export const defaultColumns: ColumnDef<ProductInfo>[] = [
  {
    accessorKey: 'id',
    header: () => h('div', { class: 'text-left' }, 'ID'),
    cell: ({ row }) => {
      return h('div', { class: 'text-left' }, row.getValue('id'))
    },
  },
  {
    accessorKey: 'name',
    header: () => h('div', { class: 'text-left' }, 'Name'),
    cell: ({ row }) => {
      return h('a', { class: 'text-left font-medium min-w-[100px]', href: `/products/${row.original.id}` }, capitalise(row.getValue('name')))
    },
  },
  {
    accessorKey: 'description',
    header: () => h('div', { class: 'text-left' }, 'Description'),
    cell: ({ row }) => {
      return h('div', { class: 'text-left font-medium min-w-[200px]' }, row.original.desc ?? '')
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
      const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'NZD',
      })

      const formatted = `${formatter.format(min)} - ${formatter.format(max)}`

      return h('div', { class: 'text-right font-medium' }, formatted)
    },
  },
  {
    accessorKey: 'tags',
    header: () => h('div', { class: 'text-right' }, 'Tags'),
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