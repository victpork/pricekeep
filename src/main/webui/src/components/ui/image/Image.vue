<script lang="ts" setup>
import { ref } from 'vue'
import { ImageIcon } from "lucide-vue-next"
defineOptions({
    inheritAttrs: false
})
defineProps<{
    src?: string
    alt?: string
    width?: string
    height?: string
}>()
const hasImageError = ref(false)
</script>

<template>
    <picture>
        <img v-if="!hasImageError && src" :src="src" :alt="alt" :width="width" :height="height" v-bind="$attrs"
            @error="hasImageError = true" />
        <div v-else
            class="max-w-lg max-h-lg bg-muted flex items-center justify-center rounded-md border border-dashed aspect-square">
            <div class="flex flex-col items-center gap-2 text-muted-foreground ">
                <ImageIcon :size="48" />
                <span>No image available</span>
            </div>
        </div>
    </picture>
</template>
