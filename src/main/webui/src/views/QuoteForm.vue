<script setup lang="ts">
import { ref, h, computed, watch } from 'vue'
import { useForm } from '@tanstack/vue-form'
import { toast } from 'vue-sonner'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import {
    InputGroup,
    InputGroupAddon,
    InputGroupText,
    InputGroupTextarea,
} from '@/components/ui/input-group'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
    SelectGroup,
    SelectLabel,
} from '@/components/ui/select'
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command'
import Switch from '@/components/ui/switch/Switch.vue'
import Label from '@/components/ui/label/Label.vue'
import { usePostApiProductProductIdQuote, useGetApiAdminStoreSearch } from '@/apiClient'
import type { StoreInfo, } from '@/model'
import { today, getLocalTimeZone, type DateValue } from '@internationalized/date'
import { DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose, DialogDescription } from '@/components/ui/dialog'
import { Type } from '@/model'
import { cn } from '@/lib/utils'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { CalendarIcon, ChevronsUpDownIcon } from 'lucide-vue-next'
import { capitalise, skewToCapText } from '@/util/capitalise'
import { refDebounced } from '@vueuse/core'
const props = defineProps<{
    productId: number
    productName: string
}>()

const emit = defineEmits<{
    success: []
}>()

const { mutateAsync: postQuote } = usePostApiProductProductIdQuote()

const formSchema = z.object({
    price: z.number().min(0),
    quoteDate: z.custom<DateValue>(),
    storeId: z.number(),
    discountType: z.enum([Type.PERCENTAGE, Type.FIXED_AMOUNT, Type.BUNDLE, Type.OTHER]).optional(),
    salePrice: z.number().min(0).optional(),
    multibuyQuantity: z.number().min(0).optional(),
})

const defaultValues: z.input<typeof formSchema> = {
    price: 1.00,
    quoteDate: today(getLocalTimeZone()),
    storeId: 0,
    discountType: undefined,
    salePrice: undefined,
    multibuyQuantity: undefined,
}

const form = useForm({
    defaultValues,
    validators: {
        onSubmit: formSchema,
    },
    onSubmit: async ({ value }) => {
        console.log(value)
        postQuote({
            productId: props.productId,
            data: {
                price: value.price,
                quoteDate: value.quoteDate.toDate(getLocalTimeZone()).toISOString().slice(0, 10),
                storeInfo: {
                    id: value.storeId,
                },
                discountType: value.discountType,
                discountPrice: value.salePrice,
                multibuyQuantity: value.multibuyQuantity,
            }
        }).then((p) => {
            console.log('Posted', p.status)
            if (p.status == 201) {
                toast.success('New Price Posted', {
                    description: h('span', { class: 'text-sm' }, [
                        'Quote ',
                        h('p', { class: 'font-medium underline' }, props.productName),
                        ' price posted successfully'
                    ])
                })
                form.reset()
                emit('success')
            } else {
                toast.error('Failed to create quote', {
                    description: p.data,
                })
            }
        }
        )
        emit('success')
    },
})

function isInvalid(field: any) {
    return field.state.meta.isTouched && !field.state.meta.isValid
}
const defaultPlaceholder = today(getLocalTimeZone())
const datePopoverClose = ref(false)
const storePopoverClose = ref(false)

const handleDateSelect = (newDate: DateValue | undefined) => {
    if (newDate) {
        datePopoverClose.value = false;
    }
};
const storeSearchKey = ref("")
const debounceStoreSearchKey = refDebounced(storeSearchKey, 300)
const storeSearchQuery = computed(() => ({ q: debounceStoreSearchKey.value }))
const storeQueryEnabled = computed(() => debounceStoreSearchKey.value.length >= 3)
const { data: storeResult } = useGetApiAdminStoreSearch(storeSearchQuery, {
    query: {
        enabled: storeQueryEnabled
    }
})

const stores = computed<StoreInfo[]>(() => storeResult.value?.data ?? [])

const selectedStore = computed(() =>
    stores.value.find(s => s.id === selectedStoreId.value)
)

const selectedStoreId = ref(0)
const discountFormEnabled = ref(false)
</script>
<template>
    <DialogContent class="min-w-[800px]">
        <DialogHeader>
            <DialogTitle>Quote</DialogTitle>
            <DialogDescription class="text-sm">
                Quote price for {{ capitalise(props.productName) }}
            </DialogDescription>
        </DialogHeader>
        <form id="quoteForm" @submit.prevent="form.handleSubmit" class="flex flex-col gap-6">
            <div class="flex flex-row gap-6">
                <FieldGroup>
                    <form.Field name="quoteDate" v-slot="{ field }">
                        <Field :data-invalid="isInvalid(field)">
                            <Popover v-model:open="datePopoverClose">
                                <PopoverTrigger as-child>
                                    <Button variant="outline" :class="cn(
                                        'w-[280px] justify-start text-left font-normal',
                                        !form.state.values.quoteDate && 'text-muted-foreground',
                                    )">
                                        <CalendarIcon class="mr-2 h-4 w-4" />
                                        {{ form.state.values.quoteDate ? form.state.values.quoteDate.toString() :
                                            "Quote Date" }}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent class="w-auto p-0">
                                    <Calendar v-model="form.state.values.quoteDate" :initial-focus="true"
                                        :default-placeholder="defaultPlaceholder" layout="month-and-year"
                                        :max-value="today(getLocalTimeZone())" @update:model-value="handleDateSelect" />
                                </PopoverContent>
                            </Popover>
                            <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                        </Field>
                    </form.Field>
                </FieldGroup>
                <form.Field name="storeId" v-slot="{ field }">
                    <Field :data-invalid="isInvalid(field)">
                        <Popover v-model:open="storePopoverClose">
                            <PopoverTrigger as-child>
                                <Button variant="outline" role="combobox" :aria-expanded="storePopoverClose"
                                    class="w-[200px] justify-between">
                                    {{ selectedStore?.name || "Store to quote from..." }}
                                    <ChevronsUpDownIcon class="opacity-50" />
                                </Button>
                            </PopoverTrigger>
                            <PopoverContent class="w-[200px] p-0">
                                <Command>
                                    <CommandInput class="h-9" placeholder="Search store..."
                                        @input="storeSearchKey = $event.target.value" />
                                    <CommandList>
                                        <CommandEmpty>No store found.</CommandEmpty>
                                        <CommandGroup>
                                            <CommandItem v-for="store in stores" :key="store.id" :value="store.id ?? 0"
                                                @select="() => {
                                                    selectedStoreId = selectedStoreId === store.id ? 0 : store.id ?? 0
                                                    storePopoverClose = false
                                                }">
                                                {{ store.name }}
                                            </CommandItem>
                                        </CommandGroup>
                                    </CommandList>
                                </Command>
                            </PopoverContent>
                        </Popover>
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                    </Field>
                </form.Field>
            </div>
            <div>
                <form.Field name="price" v-slot="{ field }">
                    <Field :data-invalid="isInvalid(field)" class="w-[200px]">
                        <Input v-model="field.state.value" type="number" placeholder="Price" min="0" step="0.01"
                            class="w-[200px]" />
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                        <FieldDescription>Original price without discount</FieldDescription>
                    </Field>
                </form.Field>
            </div>
            <div>
                <div class="flex items-center space-x-2 py-2">
                    <Switch id="airplane-mode" v-model="discountFormEnabled" />
                    <Label for="airplane-mode">Discount</Label>
                </div>
                <Transition name="fade" enter-from-class="opacity-0"
                    enter-active-class="transition duration-500 ease-out" leave-to-class="opacity-0"
                    leave-active-class="transition duration-500 ease-in">
                    <FieldGroup v-if="discountFormEnabled">
                        <form.Field name="discountType" v-slot="{ field }">
                            <Field :data-invalid="isInvalid(field)">
                                <Select :name="field.name" :value="field.state.value"
                                    @update:model-value="field.handleChange">
                                    <SelectTrigger class="w-[180px]">
                                        <SelectValue placeholder="Discount Type" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectGroup>
                                            <SelectLabel class="px-2">Discount Type</SelectLabel>
                                            <SelectItem v-for="discountType in Type" :key="discountType"
                                                :value="discountType">
                                                {{ skewToCapText(discountType) }}
                                            </SelectItem>
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                                <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                                <FieldDescription>Original price for the product without discount/promotion
                                </FieldDescription>
                            </Field>
                        </form.Field>
                        <form.Field name="salePrice" v-slot="{ field }">
                            <Field :data-invalid="isInvalid(field)">
                                <Input v-model="field.state.value" type="number" placeholder="Sale Price" />
                                <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                                <FieldDescription>Sale price for the product with discount/promotion</FieldDescription>
                            </Field>
                        </form.Field>
                        <Transition name="fade" enter-from-class="opacity-0"
                            enter-active-class="transition duration-500 ease-out" leave-to-class="opacity-0"
                            leave-active-class="transition duration-500 ease-in">
                            <div v-if="form.state.values.discountType === Type.BUNDLE">
                                <form.Field name="multibuyQuantity" v-slot="{ field }">
                                    <Field :data-invalid="isInvalid(field)">
                                        <Input v-model="field.state.value" type="number"
                                            placeholder="Multibuy Quantity" />
                                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                                        <FieldDescription>Multibuy quantity for the product with discount/promotion
                                        </FieldDescription>
                                    </Field>
                                </form.Field>
                            </div>
                        </Transition>
                    </FieldGroup>
                </Transition>
            </div>
        </form>
        <DialogFooter>
            <DialogClose as-child>
                <Button variant="outline" @click="form.reset()">Cancel</Button>
            </DialogClose>
            <Button type="submit" form="productForm">Quote</Button>
        </DialogFooter>
    </DialogContent>
</template>