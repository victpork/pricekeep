<script setup lang="ts">
import ProductCard from '@/components/ui/ProductCard.vue'
import {ref} from 'vue'
import { type QuoteDTO } from '@/model' 
const prodQuote = ref([{
    id: 1,
    name: 'Test',
    price: 33.99,
    storeName: 'PNS Westgate',
    date: '3/11/2025'
}])

const model = defineModel<QuoteDTO[]>()
</script>
<template>
    <ProductCard v-for="pq in model" :prod-quote="pq" :key="pq.id" />
</template>