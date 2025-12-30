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
import { TrendingUp } from 'lucide-vue-next'
import { formatCurrency } from '@/util/currencyFormatter'
const router = useRouter()
const props = defineProps<{
  id: number
}>()

const { data, isError } = useGetApiProductProductId(props.id)
const quoteQueryParam = ref<GetApiProductProductIdQuoteHistParams>({ discount: true, l: 'YEAR' })
const { data: quoteData, refetch: refetchQuoteData } = useGetApiProductProductIdQuoteHist(props.id, quoteQueryParam)
type ChartNode = { date: Date, price: number, store: string }
const chartData = ref<ChartNode[]>([])

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

watch(quoteData,
  (d) => {
    console.log("Quote data fetched")
    chartData.value = d?.data?.map(q => ({ date: new Date(q.quoteDate), price: q.price, store: 'name' in q.storeInfo ? q.storeInfo.name : '' })) ?? []
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
    <div class="grid auto-rows-min gap-4 md:grid-cols-12">
      <div class="col-span-8 bg-background text-foreground rounded-md">
        <div class="p-4">
          <h2 class="text-2xl font-semibold">{{ product.name }}</h2>
          <p>{{ product.desc }}</p>
        </div>
        <div>
          <picture>
            <img :src="product?.imgUrl" alt="" />
          </picture>
        </div>
      </div>
      <div class="col-span-4 bg-background text-foreground rounded-md">
        <div class="p-4 flex flex-col gap-4">
          <Card>
            <CardHeader>
              <CardTitle>Current Status</CardTitle>
            </CardHeader>
            <CardContent>
              <div v-if="product.latestQuotes?.[0]">
                <p>Best Price: {{ formatCurrency(product.latestQuotes?.[0]?.price) }}</p>
                <p>At: {{ product.latestQuotes?.[0]?.storeInfo?.name }}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Price History</CardTitle>
              <CardDescription>Past {{ quoteQueryParam.l?.toLowerCase() }}</CardDescription>
            </CardHeader>
            <CardContent>
              <ChartContainer :config="chartConfig" class="min-h-[400px] w-full">
                <VisXYContainer :data="chartData" :yDomain="[0, undefined]">
                  <VisAxis type="x" :x="(d: ChartNode) => d.date" :tick-line="false" :domain-line="false"
                    :grid-line="false" :tick-format="(d: number) => {
                      const date = new Date(d)
                      return date.toLocaleDateString('en-GB', {
                        month: 'short',
                        day: 'numeric',
                      })
                    }" :tick-values="chartData.map(d => d.date)" />
                  <VisAxis type="y" :tick-format="(d: number) => ''" :tick-line="false" :domain-line="false"
                    :grid-line="true" />
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
                Trending up by 5.2% this month
                <TrendingUp className="h-4 w-4" />
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