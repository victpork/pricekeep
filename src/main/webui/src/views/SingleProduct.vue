<script setup lang="ts">
import { watch, ref, computed } from 'vue'
import { useGetApiProductProductId, useGetApiProductProductIdQuoteHist } from '@/apiClient'
import type { GetApiProductProductIdQuoteHistParams } from '@/model'
import { useRouter } from 'vue-router'
import type { ChartConfig } from '@/components/ui/chart'
import { VisAxis, VisLine, VisXYContainer } from '@unovis/vue'
import {
  ChartContainer,
  ChartCrosshair,
  ChartTooltip,
  ChartTooltipContent,
  componentToString,
} from '@/components/ui/chart'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Button } from '@/components/ui/button';
import { ButtonGroup } from '@/components/ui/button-group'
import { Dialog, DialogTrigger } from '@/components/ui/dialog'
import { Undo2, PlusCircleIcon } from 'lucide-vue-next'
import { formatCurrency } from '@/util/currencyFormatter'
import { average } from '@/util/average'
import { maxDate } from '@/util/maxDate'
import Checkbox from '@/components/ui/checkbox/Checkbox.vue'
import Label from '@/components/ui/label/Label.vue'
import QuoteForm from './QuoteForm.vue'
import { capitalise } from '@/util/capitalise'
import PriceMatrix from '@/components/pricematrix/PriceMatrix.vue'
const router = useRouter()
const props = defineProps<{
  id: number
}>()

const { data, isError } = useGetApiProductProductId(props.id)
const quoteQueryParam = ref<GetApiProductProductIdQuoteHistParams>({ discount: true, l: 'YEAR' })
const { data: quoteData, refetch: refetchQuoteData } = useGetApiProductProductIdQuoteHist(props.id, quoteQueryParam)
type ChartNode = { date: Date, price: number, store: string }
const chartData = ref<ChartNode[]>([])
const useUpdatedDataOnly = ref(false)
const avg = ref(0)
const datasetDate = ref(new Date())

watch(isError, (isErr) => {
  if (isErr) {
    console.log("Product not found")
    router.push({ name: 'NotFound' })
  }
})

watch(quoteQueryParam, () => {
  refetchQuoteData()
})
const product = computed(() => data.value?.data ?? { name: "test", desc: "", imgUrl: "", latestQuotes: [], unit: 'EA', id: 0 })
const lowestPrice = computed(() => {
  const latest = maxDate(product.value.latestQuotes?.map(q => new Date(q.quoteDate)) ?? [])
  return Math.min(...product.value.latestQuotes?.filter(q => useUpdatedDataOnly.value ? new Date(q.quoteDate) == latest : true).map(q => q.price) ?? [0])
})

watch(quoteData,
  (d) => {
    console.log("Quote data fetched")
    datasetDate.value = maxDate(d?.data?.map(q => new Date(q.quoteDate)) ?? [])
    chartData.value = d?.data?.map(q => ({ date: new Date(q.quoteDate), price: q.price, store: 'name' in q.storeInfo ? q.storeInfo.name : '' })) ?? []
    avg.value = average(chartData.value.map(q => q.price))
  }
)

const chartConfig = {
  price: {
    label: "price",
    color: "var(--chart-1)",
  },
} satisfies ChartConfig

</script>

<template>
  <div class="flex flex-1 flex-col gap-4 p-12">
    <div class="h-full px-4 py-6 lg:px-8 justify-between flex">
      <div>
        <Button variant="outline" @click="router.back()">
          <Undo2 class="mr-2 h-4 w-4" />
          Back
        </Button>
      </div>
      <div>
        <ButtonGroup>
          <Dialog>
            <DialogTrigger as-child>
              <Button variant="outline">
                <PlusCircleIcon class="mr-2 h-4 w-4" />
                Quote
              </Button>
            </DialogTrigger>
            <QuoteForm :product-id="id" />
          </Dialog>
          <Dialog>
            <DialogTrigger as-child>
              <Button variant="outline">
                <PlusCircleIcon class="mr-2 h-4 w-4" />
                Alert
              </Button>
            </DialogTrigger>
            <QuoteForm :product-id="id" />
          </Dialog>
        </ButtonGroup>
      </div>
    </div>
    <div class="grid auto-rows-min gap-4 md:grid-cols-12">
      <div class="col-span-8 bg-background text-foreground rounded-md">
        <div class="p-4">
          <h2 class="text-2xl font-semibold">{{ capitalise(product.name) }}</h2>
          <p>{{ product.desc }}</p>
        </div>
        <div>
          <picture>
            <img :src="product?.imgUrl" :alt="product?.name" class="max-w-lg" />
          </picture>
        </div>
        <div class="p-4 gap-2 flex flex-col">
          <h4 class="text-lg font-semibold">Compare prices</h4>
          <PriceMatrix :quote="product.latestQuotes ?? []" />
        </div>
      </div>
      <div class="col-span-4 bg-background text-foreground rounded-md">
        <div class="p-4 flex flex-col gap-4">
          <Card>
            <CardHeader>
              <CardTitle>Price Info</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="flex flex-1 flex-row justify-center gap-1 text-left">
                <div v-if="product.latestQuotes?.[0]" class="flex flex-1 flex-col justify-center gap-1 text-left px-6">
                  <span class="text-muted-foreground text-xs">
                    Lowest Price {{ useUpdatedDataOnly ? 'Now' : 'All Time' }}
                  </span>
                  <span class="text-lg leading-none font-bold sm:text-3xl">
                    {{ formatCurrency(lowestPrice) }}
                  </span>
                  <span class="flex flex-row items-center gap-1">
                    <span v-if="product.latestQuotes?.[0]?.storeInfo?.storeGroupLogoPath">
                      <img :src="product.latestQuotes?.[0]?.storeInfo?.storeGroupLogoPath" alt="" class="w-4 h-4" />
                    </span>
                    <span class="text-muted-foreground text-xs">
                      {{ product.latestQuotes?.[0]?.storeInfo?.name }}
                    </span>
                  </span>
                </div>
                <div v-if="product.latestQuotes?.[0]"
                  class="flex flex-1 flex-col justify-center gap-1 text-left border-l px-6 even:border-l">
                  <span class="text-muted-foreground text-xs">
                    Average Price
                  </span>
                  <span class="text-lg leading-none font-bold sm:text-3xl">
                    {{ formatCurrency(avg) }}
                  </span>
                  <span class="text-muted-foreground text-xs"> for past {{ quoteQueryParam.l?.toLowerCase() }}</span>
                </div>
              </div>
            </CardContent>
            <CardFooter class="flex items-center gap-1">
              <Checkbox v-model="useUpdatedDataOnly" />
              <Label>Use latest data only</Label>
            </CardFooter>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Price History</CardTitle>
              <CardDescription>Past {{ quoteQueryParam.l?.toLowerCase() }}</CardDescription>
            </CardHeader>
            <CardContent>
              <ChartContainer :config="chartConfig" class="min-h-[400px] w-full">
                <VisXYContainer :data="chartData" :yDomain="[0, avg * 2]" :yRange="[100, 0]" :scaleByDomain="true">
                  <VisAxis type="x" :x="(d: ChartNode) => d.date" :tick-line="false" :domain-line="false"
                    :grid-line="false" :tick-format="(d: number) => {
                      const date = new Date(d)
                      return date.toLocaleDateString('en-GB', {
                        month: 'short',
                        day: 'numeric',
                      })
                    }" :tick-values="chartData.map(d => d.date)" />
                  <VisAxis type="y" :tick-format="(d: number) => {
                    return formatCurrency(d)
                  }" :tick-line="false" :domain-line="false" :grid-line="true" />
                  <VisLine :x="(d: ChartNode) => d.date" :y="(d: ChartNode) => d.price" :color="chartConfig.price.color"
                    :interpolateMissingData="true" />
                  <ChartTooltip />
                  <ChartCrosshair :template="componentToString(chartConfig, ChartTooltipContent, {
                    nameKey: 'store',
                    labelKey: 'store',

                  })" :color="chartConfig.price.color" />
                </VisXYContainer>
              </ChartContainer>
            </CardContent>
            <CardFooter class="flex-col items-start gap-2 text-sm p-4">
              <div class="flex gap-2 leading-none font-medium">
              </div>
              <div class="text-muted-foreground leading-none">
                <Select v-model="quoteQueryParam.l">
                  <SelectTrigger class="w-[120px]">
                    <SelectValue placeholder="Date Range" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectLabel>Date Range</SelectLabel>
                      <SelectItem value="WEEK">
                        Past Week
                      </SelectItem>
                      <SelectItem value="MONTH">
                        Past Month
                      </SelectItem>
                      <SelectItem value="QUARTER">
                        Past Quarter
                      </SelectItem>
                      <SelectItem value="YEAR">
                        Past year
                      </SelectItem>
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </div>
            </CardFooter>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>