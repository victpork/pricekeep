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
import { usePostApiProductProductIdQuote } from '@/apiClient'
import { Unit } from '@/model/unit'
import { DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose, DialogDescription } from '@/components/ui/dialog'
import { TagsInput, TagsInputItem, TagsInputItemDelete, TagsInputItemText, TagsInputInput } from '@/components/ui/tags-input'
import { Type } from '@/model'

const props = defineProps<{
    productId: number
}>()

const emit = defineEmits<{
    success: []
}>()

const { mutateAsync: postQuote } = usePostApiProductProductIdQuote()

const formSchema = z.object({
    price: z.number().min(0),
    quoteDate: z.date(),
    storeInfo: z.object({
        storeId: z.number(),
    }),
    discountType: z.enum([Type.PERCENTAGE, Type.FIXED_AMOUNT, Type.BUNDLE, Type.OTHER]).optional(),
    salePrice: z.number().min(0).optional(),
    multibuyQuantity: z.number().min(0).optional(),
})

const defaultValues: z.input<typeof formSchema> = {
    price: 1.00,
    quoteDate: new Date(),
    storeInfo: {
        storeId: 0,
    }
}

const form = useForm({

})
/*
postQuote({
    productId: props.productId,
    data: {
        price: 1,
        quoteDate: new Date().toISOString().slice(0, 10),
        storeInfo: {
            id: 1,
        },

    }
}).then((p) => {
    console.log('Posted', p.status)
    if (p.status == 201) {
        toast.success('Quote created', {
            description: h('span', { class: 'text-sm' }, [
                'Quote ',
                h('RouterLink', { to: `/quote/${p.data.id}`, class: 'font-medium underline' }, p.data.productInfo?.name),
                ' created successfully'
            ])
        })
        form.reset()
        emit('success')
    } else {
        toast.error('Failed to create quote', {
            description: p.data,
        })
    }
})
*/
</script>
<template>
    <DialogContent class="min-w-[800px]">
        <DialogHeader>
            <DialogTitle>Add Product</DialogTitle>
            <DialogDescription>
                Add a new product to your collection.
            </DialogDescription>
        </DialogHeader>
        <DialogFooter>
            <DialogClose as-child>
                <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button type="submit" form="productForm">Save changes</Button>
        </DialogFooter>
    </DialogContent>
</template>