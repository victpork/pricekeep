<script setup lang="ts">
import { ref } from "vue"
import { Badge } from "@/components/ui/badge"
import { ImageIcon } from "lucide-vue-next"
import { type SimpleQuoteDTO } from '@/model'
import { RouterLink } from "vue-router"
import { AspectRatio } from "@/components/ui/aspect-ratio"
defineProps<{
    prodQuote: SimpleQuoteDTO
}>()
import { capitalise } from "@/util"
const hasImageError = ref(false)
</script>

<template>
    <div
        class="w-70 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700 transition-shadow shadow-lg hover:shadow-[0_10px_15px_-3px_rgba(6,182,212,0.5)]">
        <div class="relative">
            <RouterLink :to="{ name: 'product', params: { id: prodQuote.productId } }">
                <AspectRatio :ratio="16 / 9" class="bg-muted rounded-t-lg content-center">
                    <picture>
                        <img v-if="!hasImageError && prodQuote.productImgPath" :src="prodQuote.productImgPath"
                            :alt="prodQuote.productName" class="rounded-t-lg h-full w-full object-cover"
                            @error="hasImageError = true" />
                        <div v-else class="bg-muted flex items-center justify-center rounded-t-lg">
                            <div class="flex flex-col items-center gap-2 text-muted-foreground">
                                <ImageIcon :size="48" />
                                <span>No image available</span>
                            </div>
                        </div>
                    </picture>
                </AspectRatio>
            </RouterLink>
            <Badge v-if="prodQuote.discountType" class="absolute top-2 right-2 bg-red-500 text-white"
                variant="secondary">Sale
            </Badge>
        </div>
        <div class="px-3 flex flex-col">
            <span class="flex items-start justify-between h-35">
                <RouterLink :to="{ name: 'product', params: { id: prodQuote.productId } }">
                    <h5 class="mt-6 mb-2 text-l font-semibold tracking-tight text-heading">
                        {{ capitalise(prodQuote.productName ?? "") }}</h5>
                </RouterLink>
            </span>
            <p class="mb-4 text-body">{{ prodQuote.storeInfo?.name }}</p>
            <div class="flex items-center gap-2">
                <h4 class="mb-2 text-xl mt-6">${{ prodQuote.discountPrice ?? prodQuote.price }}</h4>
                <h6 class="text-sm mt-6">${{ prodQuote.unitPrice }}/{{ prodQuote.unit?.toLowerCase() }}</h6>
            </div>

        </div>
    </div>
</template>