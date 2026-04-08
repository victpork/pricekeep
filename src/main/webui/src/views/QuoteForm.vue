<script setup lang="ts">
import { ref, h, watch } from 'vue'
import { useForm } from '@tanstack/vue-form'
import { toast } from 'vue-sonner'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
    SelectGroup,
    SelectLabel,
} from '@/components/ui/select'
import {
    InputGroup,
    InputGroupAddon,
    InputGroupText,
    InputGroupInput,
} from '@/components/ui/input-group'
import Switch from '@/components/ui/switch/Switch.vue'
import Label from '@/components/ui/label/Label.vue'
import { usePostApiProductProductIdQuote } from '@/apiClient'
import { today, getLocalTimeZone, type DateValue } from '@internationalized/date'
import { DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose, DialogDescription } from '@/components/ui/dialog'
import { Type } from '@/model'
import { DatePicker } from '@/components/ui/date-picker'
import { capitalise, skewToCapText } from '@/util/capitalise'
import Storepicker from '@/components/StorePicker/Storepicker.vue'
const props = defineProps<{
    productId: number
    productName: string
    price: number
}>()

const emit = defineEmits<{
    success: []
}>()

const { mutateAsync: postQuote } = usePostApiProductProductIdQuote()

const formSchema = z.object({
    price: z.coerce.number().min(0.01),
    quoteDate: z.custom<DateValue>(),
    storeId: z.number(),
    discountType: z.enum([Type.PERCENTAGE, Type.FIXED_AMOUNT, Type.BUNDLE, Type.NEAR_EXPIRY, Type.OTHER]).optional(),
    salePrice: z.coerce.number().min(0).optional(),
    multibuyQuantity: z.coerce.number().min(0).optional(),
})

const defaultValues: z.input<typeof formSchema> = {
    price: props.price,
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
        postQuote({
            productId: props.productId,
            data: {
                price: Number(value.price),
                quoteDate: value.quoteDate.toString(),
                storeInfo: {
                    id: value.storeId,
                },
                discountType: value.discountType,
                discountPrice: value.salePrice ? Number(value.salePrice) : undefined,
                multibuyQuantity: value.multibuyQuantity ? Number(value.multibuyQuantity) : undefined,
            }
        }).then((p) => {
            console.log('Posted', p.status)
            if (p.status == 202) {
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

watch(
    () => form.state.values,
    (newValue, oldValue) => {
        console.log(newValue, oldValue)
    },
    { deep: true }
)

const discountFormEnabled = ref(false)
</script>
<template>
    <DialogContent class="min-w-auto">
        <DialogHeader>
            <DialogTitle>Quote</DialogTitle>
            <DialogDescription>
                Quote price for {{ capitalise(props.productName) }}
            </DialogDescription>
        </DialogHeader>
        <form id="quoteForm" @submit.prevent="form.handleSubmit" class="flex flex-col">
            <div class="flex flex-col w-full max-w-sm items-center space-x-2 px-1 gap-6">
                <FieldGroup>
                    <form.Field name="quoteDate" v-slot="{ field }">
                        <Field :data-invalid="isInvalid(field)">
                            <FieldLabel :for="field.name">Date for Quote</FieldLabel>
                            <DatePicker :model-value="field.state.value"
                                @update:model-value="(v) => field.handleChange(v as DateValue)" placeholder="Quote Date"
                                :max-value="today(getLocalTimeZone())" />
                            <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                        </Field>
                    </form.Field>
                    <form.Field name="storeId" v-slot="{ field }">
                        <Field :data-invalid="isInvalid(field)">
                            <FieldLabel :for="field.name">Store</FieldLabel>
                            <Storepicker :model-value="field.state.value"
                                @update:model-value="(v) => field.handleChange(v as number)" />
                            <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                        </Field>
                    </form.Field>
                    <form.Field name="price" v-slot="{ field }">
                        <Field :data-invalid="isInvalid(field)" class="w-[200px]">
                            <FieldLabel :for="field.name">Original Price</FieldLabel>
                            <InputGroup>
                                <InputGroupAddon>
                                    <InputGroupText>$</InputGroupText>
                                </InputGroupAddon>
                                <InputGroupInput type="number" step="0.01" placeholder="0.00" id="price" :name="field.name"
                                    :model-value="field.state.value" @blur="field.handleBlur"
                                    @input="field.handleChange($event.target.value)" />
                            </InputGroup>
                            <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                        </Field>
                    </form.Field>
                </FieldGroup>
                <FieldGroup>
                    <div class="flex items-center space-x-2">
                        <Switch id="airplane-mode" v-model="discountFormEnabled" />
                        <Label for="airplane-mode">Discount</Label>
                    </div>
                    <Transition name="fade" enter-from-class="opacity-0"
                        enter-active-class="transition duration-500 ease-out" leave-to-class="opacity-0"
                        leave-active-class="transition duration-500 ease-in">
                        <FieldGroup v-if="discountFormEnabled">
                            <form.Field name="discountType" v-slot="{ field }">
                                <Field :data-invalid="isInvalid(field)">
                                    <FieldLabel :for="field.name">Discount Type</FieldLabel>
                                    <Select :name="field.name" :model-value="field.state.value"
                                        @update:model-value="(e) => field.handleChange(e as Type)">
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
                                </Field>
                            </form.Field>
                            <form.Field name="salePrice" v-slot="{ field }">
                                <Field :data-invalid="isInvalid(field)">
                                    <FieldLabel :for="field.name">Price after Discount</FieldLabel>
                                    <InputGroup>
                                        <InputGroupAddon>
                                            <InputGroupText>$</InputGroupText>
                                        </InputGroupAddon>
                                        <InputGroupInput type="number" step="0.01" placeholder="0.00" id="sale-price" :name="field.name"
                                            :model-value="field.state.value" @blur="field.handleBlur"
                                            @input="field.handleChange($event.target.value)" />
                                    </InputGroup>
                                    <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
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
                                        </Field>
                                    </form.Field>
                                </div>
                            </Transition>
                        </FieldGroup>
                    </Transition>
                </FieldGroup>
            </div>

        </form>
        <DialogFooter>
            <DialogClose as-child>
                <Button variant="outline" @click="form.reset()">Cancel</Button>
            </DialogClose>
            <Button type="submit" form="quoteForm">Quote</Button>
        </DialogFooter>
    </DialogContent>
</template>