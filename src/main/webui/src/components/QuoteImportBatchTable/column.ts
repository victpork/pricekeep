import { h } from 'vue'
import { type JobInfo } from '@/model'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import type { ColumnDef } from '@tanstack/vue-table'
import cronstrue from 'cronstrue';
import { Spinner } from '@/components/ui/spinner'

export const defaultColumns: ColumnDef<JobInfo>[] = [
  {
    id: 'select',
    header: ({ table }) => h(Checkbox, {
      'checked': table.getIsAllPageRowsSelected() || (table.getIsSomePageRowsSelected() && 'indeterminate'),
      'onUpdate:modelValue': value => table.toggleAllPageRowsSelected(!!value),
      'ariaLabel': 'Select all',
    }),
    cell: ({ row }) => {
      return h(Checkbox, {
        'modelValue': row.getIsSelected(),
        'onUpdate:modelValue': value => row.toggleSelected(!!value),
        'ariaLabel': 'Select row',
      })
    },
  },
  {
    accessorKey: 'name',
    header: () => h('div', { class: 'text-left' }, 'Name'),
    size: 150,
    cell: ({ row }) => {
      return h('div', { class: 'text-left font-medium max-w-[150px] overflow-hidden' }, row.getValue('name'))
    },
  },
  {
    accessorKey: 'description',
    header: () => h('div', { class: 'text-left' }, 'Description'),
    cell: ({ row }) => {
      return h('div', { class: 'text-left font-medium min-w-[150px]' }, row.original.description ?? '')
    },
  },
  {
    accessorKey: 'type',
    header: () => h('div', { class: 'text-left' }, 'Batch Type'),
    cell: ({ row }) => {
      return h('div', { class: 'text-left font-medium min-w-[150px]' }, row.original.parameters?.['keyword'] ? 'Product/Quote' : 'Branch')
    },
  },
  {
    accessorKey: 'frequency',
    header: () => h('div', { class: 'text-left' }, 'Run Frequency'),
    cell: ({ row }) => {
      return h('div', { class: 'text-left font-medium' }, row.original.frequency ? cronstrue.toString(row.original.frequency, { verbose: true }) : 'N/A')
    },
  },
  {
    accessorKey: 'status',
    header: () => h('div', { class: 'text-left' }, 'Status'),
    cell: ({ row }) => {
      if (!row.original.enabled) {
        return h(Badge, { class: 'text-left font-medium bg-gray-400', variant: 'destructive' }, () => 'Disabled')
      }
      switch (row.original.lastResult) {
        case 'COMPLETED':
          return h(Badge, { class: 'text-left font-medium bg-green-500', variant: 'default' }, () => 'Success')
        case 'ERROR':
          return h(Badge, { class: 'text-left font-medium', variant: 'destructive' }, () => 'Error')
        case 'RUNNING':
          return h(Badge, { class: 'text-left font-medium bg-orange-500', variant: 'default' }, () => [h(Spinner), 'Running'])
        case 'PAUSED':
          return h(Badge, { class: 'text-left font-medium bg-gray-500', variant: 'default' }, () => 'Disabled')
        default:
          return h(Badge, { class: 'text-left font-medium bg-sky-500', variant: 'default' }, () => 'Pending')
      }
    },
  },
  {
    accessorKey: 'lastRunTime',
    header: () => h('div', { class: 'text-left' }, 'Last Run'),
    cell: ({ row }) => {
      const time = row.original.lastRunTime ?? ''
      if (time === '') {
        return h('div', { class: 'text-left font-medium' }, 'N/A')
      } else {
        const t = new Intl.DateTimeFormat('en-NZ', {
          dateStyle: 'short',
          timeStyle: 'short',
        }).format(new Date(time))
        return h('div', { class: 'text-left font-medium' }, t)
      }
    },
  },
  {
    accessorKey: 'nextRunTime',
    header: () => h('div', { class: 'text-left' }, 'Next Run'),
    cell: ({ row }) => {
      const time = row.original.nextExecTime ?? ''
      if (time === '') {
        return h('div', { class: 'text-left font-medium' }, 'N/A')
      } else {
        const t = new Intl.DateTimeFormat('en-NZ', {
          dateStyle: 'short',
          timeStyle: 'short',
        }).format(new Date(time))
        return h('div', { class: 'text-left font-medium' }, t)
      }
    },
  },
  {
    accessorKey: 'Parameters',
    header: () => h('div', { class: 'text-left' }, 'Parameters'),
    cell: ({ row }) => {
      const params = Object.entries(row.original.parameters ?? {})
        .map(([key, value]) => h(Badge, { class: 'text-left font-medium', variant: 'outline' }, () => `${key}: ${value}`))

      return h('div', { class: 'flex flex-row gap-2' }, params)
    },
  }
]