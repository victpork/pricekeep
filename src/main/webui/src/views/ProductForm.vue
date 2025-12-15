<script setup lang="ts">
import { ref, h } from 'vue'
import { useForm } from '@tanstack/vue-form'
import { toast } from 'vue-sonner'
import { z } from 'zod'

import { Button } from '@/components/ui/button'
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel,
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
} from '@/components/ui/select'
import { usePostApiProductNew } from '@/apiClient'
import { Unit } from '@/model/unit'
import { DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose, DialogDescription } from '@/components/ui/dialog'
import { TagsInput, TagsInputItem, TagsInputItemDelete, TagsInputItemText, TagsInputInput } from '@/components/ui/tags-input'

const { mutateAsync: postNewProduct } = usePostApiProductNew()

const formSchema = z.object({
    name: z
        .string()
        .min(3, 'Product name must be at least 3 characters.')
        .max(100, 'Product name must be at most 100 characters.')
        .default(''),
    desc: z
        .string()
        .max(255, 'Description must be at most 255 characters.')
        .default(''),
    gtin: z.union([
        z.string().length(0),
        z.string()
            .regex(/^[0-9]{8}$|^[0-9]{12,14}$/, 'Must be valid barcode')
    ])
        .optional()
        .transform(e => e === "" ? undefined : e),
    unit: z.enum(Unit),
    quantityPerItem: z
        .number()
        .min(1)
        .optional(),
    itemPerBundle: z
        .number()
        .min(1),
    tags: z.array(z.string()),
})

const defaultValues: z.input<typeof formSchema> = {
    name: '',
    desc: '',
    gtin: undefined,
    unit: Unit.EA,
    quantityPerItem: undefined,
    itemPerBundle: 1,
    tags: [],
}

const tags = ref<string[]>([])
const form = useForm({
    defaultValues,
    validators: {
        onSubmit: formSchema,
    },
    onSubmit: async ({ value }) => {
        console.log(value)
        await postNewProduct({
            data: {
                name: value.name ?? '',
                desc: value.desc?.trim() || undefined,
                gtin: value.gtin,
                unit: value.unit,
                quantityPerItem: value.quantityPerItem,
                itemPerBundle: value.itemPerBundle,
                tags: tags.value.length == 0 ? undefined : tags.value,
            }
        }).then((p) => {
            console.log('Posted', p.status)
            if (p.status == 201) {
                toast.success('Product created', {
                    description: h('span', { class: 'text-sm' }, [
                        'Product ',
                        h('RouterLink', { to: `/product/${p.data.id}`, class: 'font-medium underline' }, p.data.name),
                        ' created successfully'
                    ])
                })
                form.reset()
                emit('success')
            } else {
                toast.error('Failed to create product', {
                    description: p.data,
                })
            }
        })
    },
})

function isInvalid(field: any) {
    return field.state.meta.isTouched && !field.state.meta.isValid
}

const unitSelect = Object.values(Unit).map(v => ({
    label: v.toLowerCase(),
    value: v
}));

const emit = defineEmits(['success'])

</script>

<template>
    <DialogContent class="min-w-[800px]">
        <DialogHeader>
            <DialogTitle>Add Product</DialogTitle>
            <DialogDescription>
                Add a new product to your collection.
            </DialogDescription>
        </DialogHeader>
        <form id="productForm" @submit.prevent="form.handleSubmit" class="flex flex-col gap-6">
            <FieldGroup>
                <form.Field name="name" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Name</FieldLabel>
                        <Input :id="field.name" :name="field.name" :model-value="field.state.value"
                            @input="field.handleChange($event.target.value)" />
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                    </Field>
                </form.Field>
            </FieldGroup>
            <FieldGroup>
                <form.Field name="desc" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Description</FieldLabel>
                        <InputGroup>
                            <InputGroupTextarea :id="field.name" :name="field.name" :model-value="field.state.value"
                                placeholder="Product description goes here..." :rows="6" class="min-h-24 resize-none"
                                :aria-invalid="isInvalid(field)" @blur="field.handleBlur"
                                @input="field.handleChange($event.target.value)" />
                            <InputGroupAddon align="block-end">
                                <InputGroupText class="tabular-nums">
                                    {{ field.state.value?.length || 0 }}/100 characters
                                </InputGroupText>
                            </InputGroupAddon>
                        </InputGroup>
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                    </Field>
                </form.Field>
            </FieldGroup>
            <FieldGroup>
                <form.Field name="gtin" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Barcode</FieldLabel>
                        <Input :id="field.name" :name="field.name" :model-value="field.state.value"
                            placeholder="1234567890123" type="number"
                            @input="field.handleChange($event.target.value)" />
                        <FieldDescription>Barcode/GTIN/GS1 of the product</FieldDescription>
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                    </Field>
                </form.Field>
            </FieldGroup>
            <div class="grid grid-cols-12 gap-4">
                <div class="col-span-8">
                    <FieldGroup>
                        <form.Field name="quantityPerItem" #default="{ field }">
                            <Field>
                                <FieldLabel :for="field.name">Quantity per item</FieldLabel>
                                <Input :id="field.name" :name="field.name" :model-value="field.state.value"
                                    type="number" @input="field.handleChange($event.target.valueAsNumber)"
                                    step=".001" />
                                <FieldDescription>Quantity per item of the product</FieldDescription>
                                <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                            </Field>
                        </form.Field>
                    </FieldGroup>
                </div>
                <div class="col-span-4">
                    <FieldGroup>
                        <form.Field name="unit" #default="{ field }">
                            <Field>
                                <FieldLabel for="new-product-select-unit">Unit</FieldLabel>
                                <Select :name="field.name" :model-value="field.state.value"
                                    @update:model-value="(v) => field.handleChange(v as any)">
                                    <SelectTrigger id="new-product-select-unit" :aria-invalid="isInvalid(field)"
                                        class="min-w-[120px]">
                                        <SelectValue placeholder="Select" />
                                    </SelectTrigger>
                                    <SelectContent position="item-aligned">
                                        <SelectItem v-for="unit in unitSelect" :key="unit.value" :value="unit.value">
                                            {{ unit.label }}
                                        </SelectItem>
                                    </SelectContent>
                                </Select>
                                <FieldDescription>
                                    Unit of measurement
                                </FieldDescription>
                                <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                            </Field>
                        </form.Field>
                    </FieldGroup>
                </div>
            </div>
            <FieldGroup>
                <form.Field name="itemPerBundle" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Item per bundle</FieldLabel>
                        <Input :id="field.name" :name="field.name" :model-value="field.state.value" type="number"
                            @input="field.handleChange($event.target.valueAsNumber)" />
                        <FieldDescription>Item per bundle of the product</FieldDescription>
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                    </Field>
                </form.Field>
            </FieldGroup>
            <FieldGroup>
                <form.Field name="tags" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Tags</FieldLabel>
                        <TagsInput class="w-[300px]" v-model="tags">
                            <TagsInputItem v-for="item in tags" :key="item" :value="item"
                                @input="field.handleChange($event.target.value)">
                                <TagsInputItemText />
                                <TagsInputItemDelete />
                            </TagsInputItem>
                            <TagsInputInput placeholder="Tags" />
                        </TagsInput>
                        <FieldError />
                    </Field>
                </form.Field>
            </FieldGroup>
        </form>
        <DialogFooter>
            <DialogClose as-child>
                <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button type="submit" form="productForm">Save changes</Button>
        </DialogFooter>
    </DialogContent>
</template>
