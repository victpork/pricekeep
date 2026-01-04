<script setup lang="ts" generic="TData, TValue">
import {
  FlexRender,
  getCoreRowModel,
  useVueTable,
} from '@tanstack/vue-table'
import { type PaginationState } from '@tanstack/vue-table'
import { ref, computed } from 'vue'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import PaginationControl from './PaginationControl.vue';
import { useGetApiProductAll } from '@/apiClient';
import { defaultColumns } from './column';

const props = defineProps<{ keyword?: string }>()
const pagination = ref<PaginationState>({
  pageIndex: 0,
  pageSize: 25,
})
const params = computed(() => {
  return {
    page: pagination.value.pageIndex + 1,
    pageSize: pagination.value.pageSize,
    keyword: props.keyword
  }
})
const { data: searchRsp, refetch } = useGetApiProductAll(params)

defineExpose({ refresh: refetch })

const table = useVueTable({
  get data() { return searchRsp.value?.data.productList || [] },
  get columns() { return defaultColumns },
  get pageCount() { return searchRsp.value?.data.totalPages },
  getCoreRowModel: getCoreRowModel(),
  manualPagination: true,
  rowCount: searchRsp.value?.data.totalRecords,
  state: {
    pagination: pagination.value,
  },
  onPaginationChange: (updater) => {
    if (typeof updater === 'function') {
      setPagination(
        updater({
          pageIndex: pagination.value.pageIndex,
          pageSize: pagination.value.pageSize,
        }),
      )
    } else {
      setPagination(updater)
    }
  },
})

function setPagination({
  pageIndex,
  pageSize,
}: PaginationState): PaginationState {
  pagination.value.pageIndex = pageIndex
  pagination.value.pageSize = pageSize

  return { pageIndex, pageSize }
}

</script>

<template>
  <div class="border rounded-md">
    <Table>
      <TableHeader>
        <TableRow v-for="headerGroup in table.getHeaderGroups()" :key="headerGroup.id">
          <TableHead v-for="header in headerGroup.headers" :key="header.id">
            <FlexRender v-if="!header.isPlaceholder" :render="header.column.columnDef.header"
              :props="header.getContext()" />
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <template v-if="table.getRowModel().rows?.length">
          <TableRow v-for="row in table.getRowModel().rows" :key="row.id"
            :data-state="row.getIsSelected() ? 'selected' : undefined">
            <TableCell v-for="cell in row.getVisibleCells()" :key="cell.id">
              <FlexRender :render="cell.column.columnDef.cell" :props="cell.getContext()" />
            </TableCell>
          </TableRow>
        </template>
        <template v-else>
          <TableRow>
            <TableCell :colspan="defaultColumns.length" class="h-24 text-center">
              No results.
            </TableCell>
          </TableRow>
        </template>
      </TableBody>
    </Table>
  </div>
  <PaginationControl :table="table" />
</template>
