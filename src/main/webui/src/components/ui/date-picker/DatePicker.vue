<script setup lang="ts">
import { ref } from 'vue'
import { CalendarIcon } from 'lucide-vue-next'
import { type DateValue } from '@internationalized/date'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { cn } from '@/lib/utils'

interface Props {
    modelValue?: DateValue
    placeholder?: string
    disabled?: boolean
    maxValue?: DateValue
    minValue?: DateValue
}

withDefaults(defineProps<Props>(), {
    placeholder: 'Pick a date',
    disabled: false
})

const emit = defineEmits<{
    'update:modelValue': [value: DateValue | undefined]
}>()

const isOpen = ref(false)

const handleSelect = (date: DateValue | undefined) => {
    emit('update:modelValue', date)
    if (date) {
        isOpen.value = false
    }
}
</script>

<template>
    <Popover v-model:open="isOpen">
        <PopoverTrigger as-child>
            <Button variant="outline" :disabled="disabled" :class="cn(
                'w-full justify-start text-left font-normal',
                !modelValue && 'text-muted-foreground'
            )">
                <CalendarIcon class="mr-2 h-4 w-4" />
                {{ modelValue ? modelValue.toString() : placeholder }}
            </Button>
        </PopoverTrigger>
        <PopoverContent class="w-auto p-0">
            <Calendar :model-value="modelValue" :max-value="maxValue" :min-value="minValue"
                @update:model-value="handleSelect" initial-focus />
        </PopoverContent>
    </Popover>
</template>
