<script setup lang="ts">
import ProductCard from '@/components/ui/ProductCard.vue'
import { type SimpleQuoteDTO } from '@/model'


const model = defineModel<SimpleQuoteDTO[]>()
</script>
<template>
    <ProductCard v-for="pq in model" :prod-quote="pq" :key="pq.productId" />
</template>