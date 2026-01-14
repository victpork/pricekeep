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
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/components/ui/avatar'
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
          <span class="relative inline-flex">
            <Button class="border-0 p-1.5 size-12" @mouseenter="" @mouseleave="" variant="secondary" size="icon">
              <Bell class="size-8" />
            </Button>
            <span v-if="alerts?.data" class="absolute top-0 right-0 -mt-1 -mr-1 flex size-4">
              <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-red-500 opacity-75"></span>
              <span class="relative inline-flex size-4 rounded-full bg-red-500"></span>
            </span>
          </span>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start">
          <template v-for="(alert, index) in alerts?.data">
            <DropdownMenuItem>
              <Avatar>
                <AvatarImage :src="alert.imgUrl ?? ''" :alt="alert.name" />
                <AvatarFallback>CN</AvatarFallback>
              </Avatar>
              <a :href="`/products/${alert.id}`">{{ alert.name }}</a>
            </DropdownMenuItem>
          </template>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  </div>
</template>

<style scoped>
/* No Tailwind imports here — Tailwind should be included globally. */
</style>