<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { useGetApiAdminBatchAll, usePostApiAdminBatchBatchIdRun, usePostApiAdminBatchDisable, usePostApiAdminBatchEnable } from '@/apiClient';
import { Button } from '@/components/ui/button';
import { ButtonGroup } from '@/components/ui/button-group';
import { Play, Trash2, Power, PowerOff, FilePlusCorner, Store, PackagePlus } from 'lucide-vue-next';
import GenericDataTable from '@/components/GenericDataTable.vue';
import { defaultColumns } from '@/components/QuoteImportBatchTable/column.ts';
import { toast } from 'vue-sonner';
import type { JobInfo } from '@/model';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Dialog, DialogTrigger } from '@/components/ui/dialog';
import ProductQuoteBatchForm from './ProductQuoteBatchForm.vue';
import DialogContent from '@/components/ui/dialog/DialogContent.vue';
const { data, refetch: refetchBatches } = useGetApiAdminBatchAll()
const { mutateAsync: runBatch } = usePostApiAdminBatchBatchIdRun()


const batches = computed(() => data.value?.data ?? [])
const table = ref<{ selectedRows: JobInfo[] }>()

watch(() => table.value?.selectedRows, (newSelection) => {
    console.log("Current Selection:", newSelection?.map(row => row.id))
}, { deep: true })

const handleRunBatch = () => {
    console.log(table.value?.selectedRows)
    table.value?.selectedRows.forEach((row) => {
        row.id ? runBatch({ batchId: row.id }).then(() => toast.success(`Batch ${row.name} run queued`)) : null
    })
    refetchBatches()
}

const { mutateAsync: enableBatch } = usePostApiAdminBatchEnable()
const { mutateAsync: disableBatch } = usePostApiAdminBatchDisable()

const handleEnableBatch = () => {
    if (table.value?.selectedRows !== undefined) {
        console.log(table.value?.selectedRows)
        enableBatch({ data: table.value?.selectedRows.filter(row => !row.enabled).flatMap(row => row.id) })
        refetchBatches()
    }
}

const handleDisableBatch = () => {
    if (table.value?.selectedRows !== undefined) {
        console.log(table.value?.selectedRows)
        disableBatch({ data: table.value?.selectedRows.filter(row => row.enabled).flatMap(row => row.id) })
        refetchBatches()
    }
}
const dialogForm = ref<("quote" | "store" | "")>("")
const setDialog = (form: "quote" | "store" | "") => {
    dialogForm.value = form
}
const isDialogOpen = ref(false)
</script>

<template>
    <div class="col-span-3 lg:col-span-4 lg:border-x-l">
        <div class="h-full px-4 py-6 lg:px-8 flex flex-col gap-6">
            <div class="flex items-center justify-between">
                <div class="space-y-1">
                    <h2 class="text-2xl font-semibold tracking-tight">
                        Data Sources
                    </h2>
                    <p class="text-sm text-muted-foreground">
                        Manage your data sources
                    </p>
                </div>
            </div>
            <div class="flex flex-row gap-2">
                <Button variant="outline" size="sm" @click="handleRunBatch">
                    <Play /> Run
                </Button>
                <ButtonGroup>

                    <Button variant="outline" size="sm" @click="handleEnableBatch">
                        <Power /> Enable
                    </Button>
                    <Button variant="outline" size="sm" @click="handleDisableBatch">
                        <PowerOff /> Disable
                    </Button>

                </ButtonGroup>

                <ButtonGroup>
                    <Dialog>
                        <DropdownMenu>
                            <DropdownMenuTrigger as-child>
                                <Button variant="outline" size="sm">
                                    <FilePlusCorner /> Create
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent>

                                <DialogTrigger as-child @click="setDialog('quote')">
                                    <DropdownMenuItem>
                                        <Package-Plus /> Quote Import Batch
                                    </DropdownMenuItem>
                                </DialogTrigger>
                                <DialogTrigger as-child @click="setDialog('store')">
                                    <DropdownMenuItem>
                                        <Store /> Store Import Batch
                                    </DropdownMenuItem>
                                </DialogTrigger>
                            </DropdownMenuContent>
                            <DialogContent class="min-w-auto" v-model:open="isDialogOpen">
                                <ProductQuoteBatchForm v-if="dialogForm == 'quote'"
                                    @success="() => { isDialogOpen = false; refetchBatches() }" />
                                <StoreImportBatchForm v-if="dialogForm == 'store'" />
                            </DialogContent>
                        </DropdownMenu>
                    </Dialog>
                    <Button variant="outline" size="sm">
                        <Trash2 /> Remove
                    </Button>
                </ButtonGroup>
            </div>
            <div class="flex flex-row gap-2">
                <GenericDataTable :data="batches" :columns="defaultColumns" :page-control="{ showSelectionCount: true }"
                    ref="table" />
            </div>
        </div>
    </div>
</template>
