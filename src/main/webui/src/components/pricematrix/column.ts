import type { SimpleQuoteDTO } from '@/model'
import { h } from 'vue'
import { type ColumnDef } from '@tanstack/vue-table'
import { ArrowUpDown } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

export const columns: ColumnDef<SimpleQuoteDTO>[] = [

    {
        accessorKey: 'quoteStore',
        header: () => h('div', { class: 'text-left' }, 'Store'),
        cell: ({ row }) => {
            const store = row.original.storeInfo

            const name = (store && 'name' in store) ? store.name : ''
            const logo = (store && 'storeGroupLogoPath' in store) ? store.storeGroupLogoPath : undefined

            return h('div', { class: 'flex items-center gap-2 text-left font-medium' }, [
                logo ? h('img', { src: logo, class: 'w-4 h-4', alt: name }) : null,
                h('span', name)
            ])

        },
    },
    {
        accessorKey: 'price',
        header: ({ column }) => {
            return h('div',
                { class: 'flex flex-row-reverse' },
                h(Button, {
                    variant: 'ghost',
                    class: 'flex flex-row-reverse p-4',
                    onClick: () => column.toggleSorting(column.getIsSorted() === 'asc'),
                }, () => [h('span', 'Price'),
                h(ArrowUpDown, { class: 'ml-2 h-4 w-4' }),
                ]))
        },

        cell: ({ row }) => {
            const amount = row.original.price
            const formatted = new Intl.NumberFormat('en-NZ', {
                style: 'currency',
                currency: 'NZD',
            }).format(amount)
            return h('div', { class: 'text-right font-medium pr-3' }, formatted)
        },
    },
    {
        accessorKey: 'unitPrice',
        header: ({ column }) => {
            return h('div',
                { class: 'flex flex-row-reverse' },
                h(Button, {
                    variant: 'ghost',
                    class: 'flex flex-row-reverse p-4',
                    onClick: () => column.toggleSorting(column.getIsSorted() === 'asc'),
                }, () => [h('span', 'Unit Price'),
                h(ArrowUpDown, { class: 'ml-2 h-4 w-4' }),
                ]))
        },

        cell: ({ row }) => {
            const amount = row.original.unitPrice
            const formatted = new Intl.NumberFormat('en-NZ', {
                style: 'currency',
                currency: 'NZD',
            }).format(amount)
            return h('div', { class: 'text-right font-medium pr-3' }, formatted + " /" + row.original.unit?.toLowerCase())
        },
    },
    {
        accessorKey: 'quoteDate',
        header: () => h('div', { class: 'text-right' }, 'Last Updated'),
        cell: ({ row }) => {
            const quoteDate = row.original.quoteDate
            const formatted = new Intl.DateTimeFormat('en-NZ', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
            }).format(new Date(quoteDate))
            return h('div', { class: 'text-right font-medium' }, formatted)
        },
    },
]