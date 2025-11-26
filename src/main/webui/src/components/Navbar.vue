<script setup lang="ts">
import { computed, ref } from 'vue'
import { Bell } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import AutoComplete from './ui/AutoComplete.vue'
import {
  useGetApiProductSuggest,
  useGetApiProductAlerts,
  type GetApiProductAlertsQueryResult,
} from '@/apiClient';
import { refDebounced } from '@vueuse/core'
import { type Prettify } from '@/lib/prettify'

const searchText = ref("")
const debounceQuery = refDebounced(searchText, 300)
const searchEnabled = computed(() => debounceQuery.value !== null && debounceQuery.value.length >= 3)
const queryParam = computed(() => ({ q: debounceQuery.value }))
const { data } = useGetApiProductSuggest(queryParam, { query: { enabled: searchEnabled } })

const t = computed<string[]>(() => data.value?.data ?? [])

const { data: alerts } = useGetApiProductAlerts<Prettify<GetApiProductAlertsQueryResult>>()

</script>

<template>
  <div class="flex items-center w-full gap-4">
    <div class="flex-1">
      <AutoComplete v-model="searchText" :suggest="t" />
    </div>

    <div class="flex items-center">
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <Button class="border-0 p-1.5 size-12" variant="secondary">
            <Bell class="size-8" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <template v-for="(alert, index) in alerts?.data">
            <DropdownMenuItem>
              <span>{{ alert.productInfo?.name }}</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator v-if="index === (alerts?.data.length ?? 1) - 1"/>
          </template>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  </div>
</template>

<style scoped>
/* No Tailwind imports here — Tailwind should be included globally. */
</style>