<script setup lang="ts">
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { ScrollArea, ScrollBar } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { PlusCircleIcon, LayoutGridIcon, SheetIcon } from 'lucide-vue-next';
import {
  useGetApiCommonLatestDeals,
} from '@/apiClient';
import ProductColleciton from './ProductColleciton.vue';
import { computed, ref, useTemplateRef } from 'vue';
import type { ComponentExposed } from 'vue-component-type-helpers'
import {
  Dialog,
  DialogTrigger,
} from '@/components/ui/dialog'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group';
import { SearchIcon } from 'lucide-vue-next';
import ProductForm from '@/views/ProductForm.vue'
import ProductTable from '@/components/product/ProductTable.vue';
import { refDebounced } from '@vueuse/core'
const { data: latestDeals } = useGetApiCommonLatestDeals()
const flatDeals = computed(() => (latestDeals.value?.data.results ?? []))
const isProductDialogOpen = ref(false)
const productTable = useTemplateRef<ComponentExposed<typeof ProductTable>>('productTable')
const searchKey = ref<string>()

const debouncedSearchKey = refDebounced(searchKey, 500)
</script>
<template>
  <div class="col-span-3 lg:col-span-4 lg:border-x-l">
    <div class="h-full px-4 py-6 lg:px-8">
      <Tabs default-value="at-a-glance" class="h-full space-y-6">
        <div class="space-between flex items-center">
          <TabsList>
            <TabsTrigger value="at-a-glance" class="relative px-5">
              <LayoutGridIcon />
            </TabsTrigger>
            <TabsTrigger value="products" class="px-5">
              <SheetIcon />
            </TabsTrigger>
          </TabsList>
          <div class="ml-auto mr-4">
            <Dialog v-model:open="isProductDialogOpen">
              <DialogTrigger as-child>
                <Button variant="outline">
                  <PlusCircleIcon class="mr-2 h-4 w-4" />
                  Add Product
                </Button>
              </DialogTrigger>
              <ProductForm @success="() => { isProductDialogOpen = false; productTable?.refresh() }" />
            </Dialog>
          </div>
        </div>
        <TabsContent value="at-a-glance" class="border-none p-0 outline-none">
          <div class="flex items-center justify-between">
            <div class="space-y-1">
              <h2 class="text-2xl font-semibold tracking-tight">
                Daily Deals
              </h2>
              <p class="text-sm text-muted-foreground">
                Top picks for you. Updated daily.
              </p>
            </div>
          </div>
          <Separator class="my-4" />
          <div class="relative">
            <ScrollArea>
              <div class="flex space-x-4 pb-4">
                <ProductColleciton v-model="flatDeals" />
              </div>
              <ScrollBar orientation="horizontal" />
            </ScrollArea>
          </div>
          <div class="mt-6 space-y-1">
            <h2 class="text-2xl font-semibold tracking-tight">
              Sales ongoing
            </h2>
            <p class="text-sm text-muted-foreground">
              Recently discovered sales
            </p>
          </div>
          <Separator class="my-4" />
          <div class="relative">
            <ScrollArea>
              <div class="flex space-x-4 pb-4">

              </div>
              <ScrollBar orientation="horizontal" />
            </ScrollArea>
          </div>
        </TabsContent>
        <TabsContent value="products" class="h-full flex-col border-none p-0 data-[state=active]:flex">
          <div class="flex items-center justify-between w-full">
            <div class="space-y-1 w-full flex flex-col gap-2">
              <h2 class="text-2xl font-semibold tracking-tight">
                Product List
              </h2>
              <div class="flex flex-row">
                <InputGroup class="basis-xs">
                  <InputGroupInput placeholder="Search..." v-model="searchKey" />
                  <InputGroupAddon>
                    <SearchIcon />
                  </InputGroupAddon>
                </InputGroup>
              </div>
              <ProductTable ref="productTable" :keyword="debouncedSearchKey" />
            </div>
          </div>
          <Separator class="my-4" />
        </TabsContent>
      </Tabs>
    </div>
  </div>
</template>