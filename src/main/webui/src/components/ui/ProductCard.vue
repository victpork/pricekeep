<script setup lang="ts">
import { Badge } from "@/components/ui/badge"
import { ImageIcon } from "lucide-vue-next"
import { type QuoteDTO } from '@/model'

defineProps<{
    prodQuote: QuoteDTO
}>()


</script>

<template>
    <div class="max-w-sm bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700 min-w-40">
        <div class="relative">
            <a href="#">
                <img v-if="prodQuote.productInfo?.imgUrl" class="h-full w-full object-cover"
                    :src="prodQuote.productInfo?.imgUrl" :alt="prodQuote.productInfo.name" />

                <div v-else
                    class="max-w-sm h-48 bg-gray-200 flex items-center justify-center border border-gray-300 rounded-t-lg">
                    <ImageIcon :size=36 color="grey" />
                </div>
            </a>
            <Badge v-if="prodQuote.discountType" class="absolute top-2 right-2" variant="secondary">Sale</Badge>
        </div>
        <div class="p-5">
            <span class="flex items-center justify-between">
                <a href="#">
                    <h5 class="mb-2 text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                        {{ prodQuote.productInfo?.name }}</h5>
                </a>
                <h4 class="mb-2 text-2xl">${{ prodQuote.price }}</h4>
            </span>
            <p class="mb-3 font-normal text-gray-700 dark:text-gray-400">{{ prodQuote.storeInfo }}</p>
        </div>
    </div>
</template>