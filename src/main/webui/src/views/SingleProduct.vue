<script setup lang="ts">
import { watch, ref, computed, toRef } from 'vue'
import { useGetApiProductProductIdGetAlert, useGetApiProductProductId, useGetApiProductProductIdQuoteHist, usePostApiProductProductIdEditAlert } from '@/apiClient'
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
import { Undo2, PlusCircleIcon, ImageIcon, BellPlusIcon, BellDotIcon } from 'lucide-vue-next'
import { formatCurrency } from '@/util/currencyFormatter'
import { average, maxDate, capitalise } from '@/util'
import Separator from '@/components/ui/separator/Separator.vue'
import QuoteForm from './QuoteForm.vue'
import PriceMatrix from '@/components/pricematrix/PriceMatrix.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Label } from '@/components/ui/label'
import { Field, FieldDescription } from '@/components/ui/field'
import { InputGroup, InputGroupAddon, InputGroupInput, InputGroupText } from '@/components/ui/input-group'
import { Switch } from '@/components/ui/switch'
const router = useRouter()
const props = defineProps<{
  id: number
}>()

const pid = toRef(() => props.id)

const { data, isError, refetch: refetchProductData } = useGetApiProductProductId(pid)
const quoteQueryParam = ref<GetApiProductProductIdQuoteHistParams>({ discount: true, l: 'YEAR' })
const { data: quoteData, refetch: refetchQuoteData } = useGetApiProductProductIdQuoteHist(pid, quoteQueryParam)
const { data: alertData } = useGetApiProductProductIdGetAlert(pid)
const { mutate: setAlert } = usePostApiProductProductIdEditAlert()
type ChartNode = { date: Date, price: number, store: string }
const chartData = ref<ChartNode[]>([])
const avg = ref(0)
const datasetDate = ref(new Date())
const hasImageError = ref(false)

watch(isError, (isErr) => {
  if (isErr) {
    console.log("Product not found")
    router.push({ name: 'NotFound' })
  }
})
const alertForm = ref({
  enabled: alertData.value !== undefined,
  targetPrice: alertData.value?.data.priceLevel ?? 0,
})

watch(alertData, (newData) => {
  if (newData !== undefined) {
    alertForm.value.enabled = newData.data.priceLevel !== undefined
    alertForm.value.targetPrice = newData.data.priceLevel ?? 0
  } else {
    alertForm.value.enabled = false
    alertForm.value.targetPrice = 0
  }
},
  { once: true })

const alertPopoverOpen = ref(false)

watch(quoteQueryParam, () => {
  refetchQuoteData()
})
const isQuoteDialogOpen = ref(false)

const product = computed(() => data.value?.data ?? { name: "test", desc: "", imgUrl: "", latestQuotes: [], unit: 'EA', id: 0 })
const lowestQuote = computed(() => {
  if (!product.value.latestQuotes?.length) {
    return { price: 0, discountPrice: 0, storeInfo: { name: "", storeGroupLogoPath: "" }, quoteDate: new Date() };
  }
  return product.value.latestQuotes?.reduce((min, obj) => (obj.discountPrice ?? obj.price) < (min.discountPrice ?? min.price) ? obj : min) ??
    { price: 0, discountPrice: 0, storeInfo: { name: "", storeGroupLogoPath: "" }, quoteDate: new Date() };
})

watch(quoteData,
  (d) => {
    datasetDate.value = maxDate(d?.data?.map(q => new Date(q.quoteDate)) ?? [])
    chartData.value = d?.data?.map(q => ({ date: new Date(q.quoteDate), price: q.discountPrice ?? q.price, store: 'name' in q.storeInfo ? q.storeInfo.name : '' })) ?? []
    avg.value = average(chartData.value.map(q => q.price))
  }
)
const chartConfig = {
  price: {
    label: "price",
    color: "var(--chart-1)",
  },
} satisfies ChartConfig

dayjs.extend(relativeTime)

const updateAlert = async () => {
  if (alertForm.value.enabled) {
    setAlert({
      productId: props.id,
      data: { action: 'SET', targetPrice: alertForm.value.targetPrice }
    })
  } else {
    setAlert({
      productId: props.id,
      data: { action: 'REMOVE' }
    })
  }
  alertPopoverOpen.value = false
}
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
          <Dialog v-model:open="isQuoteDialogOpen">
            <DialogTrigger as-child>
              <Button variant="outline">
                <PlusCircleIcon class="mr-2 h-4 w-4" />
                Quote
              </Button>
            </DialogTrigger>
            <QuoteForm :product-id="id" :product-name="product.name"
              @success="() => { isQuoteDialogOpen = false; refetchQuoteData(); refetchProductData() }" />
          </Dialog>
          <Popover v-model:open="alertPopoverOpen">
            <PopoverTrigger as-child>
              <Button variant="outline">
                <BellDotIcon v-if="alertForm.enabled" class="mr-2 h-4 w-4" color="#FF0000" />
                <BellPlusIcon v-else class="mr-2 h-4 w-4" />
                Alert
              </Button>
            </PopoverTrigger>
            <PopoverContent align="end">
              <div class="flex flex-col gap-2 gap-y-4">
                <div class="flex gap-2">
                  <Switch v-model="alertForm.enabled" /> <Label>Enable Alert</Label>
                </div>
                <div>
                  <Field v-if="alertForm.enabled">
                    <InputGroup class="max-w-[200px]">
                      <InputGroupAddon>
                        <InputGroupText>$</InputGroupText>
                      </InputGroupAddon>
                      <InputGroupInput placeholder="0.00" type="number" min="0" step="0.01"
                        v-model="alertForm.targetPrice" />
                    </InputGroup>
                    <FieldDescription>
                      You will be notified when price drops to this level
                    </FieldDescription>
                  </Field>
                </div>
                <div class="self-end">
                  <Button @click="updateAlert">Apply</Button>
                </div>
              </div>
            </PopoverContent>
          </Popover>
        </ButtonGroup>
      </div>
    </div>
    <div class="grid auto-rows-min gap-4 md:grid-cols-12">
      <div class="col-span-8 bg-background text-foreground rounded-md">
        <div class="p-4">
          <h2 class="text-2xl font-semibold">{{ capitalise(product.name) }}</h2>
          <p>{{ product.desc }}</p>
        </div>
        <div class="p-4">
          <picture>
            <img v-if="!hasImageError && product?.imgUrl" :src="product?.imgUrl" :alt="product?.name"
              class="max-w-lg rounded-md min-w-[500px]" @error="hasImageError = true" />
            <div v-else class="max-w-lg h-64 bg-muted flex items-center justify-center rounded-md border border-dashed">
              <div class="flex flex-col items-center gap-2 text-muted-foreground">
                <ImageIcon :size="48" />
                <span>No image available</span>
              </div>
            </div>
          </picture>
        </div>
        <div class="p-4">
          <Separator />
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
                    Lowest Price
                  </span>
                  <span class="text-lg leading-none font-bold sm:text-3xl">
                    {{ formatCurrency(lowestQuote.discountPrice ?? lowestQuote.price) }}
                  </span>
                  <span class="flex flex-row items-center gap-1">
                    <span v-if="lowestQuote.storeInfo?.storeGroupLogoPath">
                      <img :src="lowestQuote.storeInfo?.storeGroupLogoPath" alt="" class="w-4 h-4" />
                    </span>
                    <span class="text-muted-foreground text-xs">
                      {{ lowestQuote.storeInfo?.name }}
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
              <span class="text-muted-foreground text-sm">
                Updated {{
                  dayjs(lowestQuote.quoteDate).fromNow() }}</span>
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
                    }" :tick-values="chartData.map(d => d.date)" :tickTextHideOverlapping="true" />
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