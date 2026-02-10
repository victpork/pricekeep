<script setup lang="ts">
import ProductCard from '@/components/ui/ProductCard.vue'
import { type QuoteDTO } from '@/model'


const model = defineModel<QuoteDTO[]>()
</script>
<template>
    <ProductCard v-for="pq in model" :prod-quote="pq" :key="pq.productInfo?.id" />
</template>