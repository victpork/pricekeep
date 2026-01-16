<script setup lang="ts">
import { computed, ref } from 'vue'
import { Bell } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Badge } from '@/components/ui/badge'
import AutoComplete from './ui/AutoComplete.vue'
import {
  useGetApiProductSuggest,
  useGetApiProductAlerts,
  type GetApiProductAlertsQueryResult,
} from '@/apiClient';
import {
  Item,
  ItemContent,
  ItemDescription,
  ItemMedia,
  ItemTitle,
} from '@/components/ui/item'
import { refDebounced } from '@vueuse/core'
import { type Prettify } from '@/lib/prettify'
import Image from '@/components/ui/image/Image.vue'
import { formatCurrency } from '@/util/currencyFormatter'
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
            <span v-if="alerts?.data.length" class="absolute top-0 right-0 -mt-1 -mr-1 flex size-4">
              <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-red-500 opacity-75"></span>
              <span class="relative inline-flex size-4 rounded-full bg-red-500"></span>
            </span>
          </span>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start" v-if="alerts?.data.length">
          <template v-for="(alert, index) in alerts?.data" :key="index">
            <DropdownMenuItem>
              <Item as-child>
                <RouterLink :to="`/products/${alert.productId}`">
                  <ItemMedia>
                    <Image :src="alert.productImgPath" :alt="alert.productName" width="64" height="64"
                      class="size-16 rounded-md" />
                  </ItemMedia>
                  <ItemContent>
                    <ItemTitle>{{ alert.productName }}</ItemTitle>
                    <ItemDescription>{{ formatCurrency(alert.discountPrice ?? alert.price) }} @ {{ alert.storeInfo?.name
                    }}<Badge v-if="alert.discountPrice" variant="destructive">Discounted</Badge>
                    </ItemDescription>
                  </ItemContent>
                </RouterLink>
              </Item>
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