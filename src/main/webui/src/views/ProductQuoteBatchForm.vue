<script setup lang="ts">
import { ref, h, computed, watch } from 'vue'
import { useForm } from '@tanstack/vue-form'
import { toast } from 'vue-sonner'
import { z } from 'zod'
import { CheckIcon, ChevronDown } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel,
    FieldSet,
    FieldSeparator,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import {
    InputGroup,
    InputGroupAddon,
    InputGroupText,
    InputGroupTextarea,
} from '@/components/ui/input-group'
import { useGetApiAdminStoreSearch, usePostApiAdminBatchNewProductQuoteImport } from '@/apiClient'
import { DialogHeader, DialogTitle, DialogFooter, DialogClose, DialogDescription } from '@/components/ui/dialog'
import cronstrue from 'cronstrue'
import cronparse from 'cron-parser'
import { TagsInput, TagsInputItem, TagsInputItemDelete, TagsInputItemText, TagsInputInput } from '@/components/ui/tags-input'
import { Checkbox } from '@/components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Popover, PopoverContent, PopoverTrigger, PopoverAnchor } from '@/components/ui/popover'
import { ListboxContent, ListboxFilter, ListboxItem, ListboxItemIndicator, ListboxRoot, useFilter, type AcceptableInputValue } from 'reka-ui'

const { mutateAsync: postNewBatch } = usePostApiAdminBatchNewProductQuoteImport()

const cronExpr = ref('0 30 10 * * ?')
const useAdvCronExpr = ref(false)

const { data: stores } = useGetApiAdminStoreSearch()

const formSchema = z.object({
    name: z
        .string()
        .min(3, 'Batch name must be at least 3 characters.')
        .max(100, 'Batch name must be at most 100 characters.')
        .default(''),
    desc: z
        .string()
        .max(255, 'Description must be at most 255 characters.')
        .default(''),
    keyword: z.string().min(3, 'Keyword must be at least 3 characters'),
})

const defaultValues: z.input<typeof formSchema> = {
    name: '',
    desc: '',
    keyword: '',
}

const cronFreq = computed(() => {
    if (!cronExpr.value) {
        return ''
    }
    try {
        cronparse.parse(cronExpr.value)
    } catch (e) {
        return ''
    }
    return cronstrue.toString(cronExpr.value, { verbose: true })
})

function isInvalid(field: any) {
    return field.state.meta.isTouched && !field.state.meta.isValid
}

const emit = defineEmits(['success'])

const isCronValid = computed<string | undefined>(() => {
    if (!cronExpr.value) {
        return undefined
    }
    try {
        cronparse.parse(cronExpr.value)
        return undefined
    } catch (e) {
        return "Invalid cron expression"
    }
})

const searchTerm = ref('')
const storeListRef = ref<Record<string, number>[]>([])
const open = ref(false)
const { contains } = useFilter({ sensitivity: 'base' })
const filteredStores = computed(() =>
    searchTerm.value === ''
        ? stores.value?.data.map((s) => ({ label: s.name ?? '', value: s.id ?? 0 }))
        : stores.value?.data.map((s) => ({ label: s.name ?? '', value: s.id ?? 0 })).filter(option => contains(option.label, searchTerm.value)),
)

watch(searchTerm, (f) => {
    if (f) {
        open.value = true
    }
})
const freq = ref<'daily' | 'weekly' | 'monthly'>('daily')
const time = ref('10:30')
watch([freq, time], ([f, t]) => {
    const [hour, minute] = t.split(':')
    if (f === 'daily') {
        cronExpr.value = `0 ${minute} ${hour} * * ?`
    } else if (f === 'weekly') {
        cronExpr.value = `0 ${minute} ${hour} */7 * ?`
    } else if (f === 'monthly') {
        cronExpr.value = `0 ${minute} ${hour} 1 * ?`
    }
})

const form = useForm({
    defaultValues,
    validators: {
        onSubmit: formSchema,
    },
    onSubmit: async ({ value }) => {
        const body = {
            name: value.name ?? '',
            description: value.desc?.trim() || undefined,
            cronTrigger: cronExpr.value,
            storeList: storeListRef.value.flatMap((s) => Object.values(s)),
            keyword: value.keyword,
        }
        console.log(body)
        await postNewBatch({
            data: body
        }).then((p) => {
            console.log('Posted', p.status)
            if (p.status == 202) {
                toast.success('Batch created', {
                    description: h('span', { class: 'text-sm' }, [
                        'Batch created successfully'
                    ])
                })
                form.reset()
                emit('success')
            } else {
                toast.error('Failed to create batch', {
                    description: p.data?.msg,
                })
            }
        })
    },
})

const isRecord = (x: AcceptableInputValue): x is Record<string, number> => {
    return Object.keys(x).length > 0;
};

const displayTagVal = (v: AcceptableInputValue): string => {
    if (isRecord(v)) {
        return Object.keys(v)[0] ?? ''
    }
    return v.toString()
}
</script>

<template>
    <DialogHeader>
        <DialogTitle>Create ProductQuote Import</DialogTitle>
        <DialogDescription>
            Import market data
        </DialogDescription>
    </DialogHeader>
    <form id="batchForm" @submit.prevent="form.handleSubmit" class="flex flex-col gap-6">
        <div class="flex flex-col w-full max-w-sm items-center space-x-2">
            <FieldGroup>
                <form.Field name="name" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Name</FieldLabel>
                        <Input :id="field.name" :name="field.name" :model-value="field.state.value"
                            @input="field.handleChange($event.target.value)" />
                        <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                    </Field>
                </form.Field>
                <form.Field name="desc" #default="{ field }">
                    <Field>
                        <FieldLabel :for="field.name">Description</FieldLabel>
                        <InputGroup>
                            <InputGroupTextarea :id="field.name" :name="field.name" :model-value="field.state.value"
                                placeholder="Job description (e.g. Import fresh meat)" :rows="6"
                                class="min-h-24 resize-none" :aria-invalid="isInvalid(field)" @blur="field.handleBlur"
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
                <FieldSeparator />
                <FieldSet class="flex flex-row gap-4">
                    <Field>
                        <FieldLabel for="frequency">Frequency</FieldLabel>
                        <Select id="frequency" :disabled="useAdvCronExpr" v-model="freq">
                            <SelectTrigger>
                                <SelectValue placeholder="Frequency" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="daily">Everyday</SelectItem>
                                <SelectItem value="weekly">Every week</SelectItem>
                                <SelectItem value="monthly">Every month</SelectItem>
                            </SelectContent>
                        </Select>
                    </Field>
                    <Field>
                        <FieldLabel for="time-picker">Run at</FieldLabel>
                        <Input id="time-picker" type="time" default-value="10:30"
                            class="bg-background appearance-none [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-calendar-picker-indicator]:appearance-none"
                            :disabled="useAdvCronExpr" v-model="time" />
                    </Field>
                </FieldSet>
                <div class="flex items-start gap-3 rounded-lg border p-3">
                    <Checkbox id="useAdvCronExpr" v-model="useAdvCronExpr" />
                    <div class="grid gap-1.5 font-normal">
                        <p class="text-sm leading-none font-medium">
                            Custom Cron Expression
                        </p>
                        <p class="text-muted-foreground text-sm">
                            Input custom cron expression
                        </p>
                        <p>
                            <Field v-if="useAdvCronExpr">
                                <Input id="cronExpr" v-model="cronExpr" />
                                <FieldError v-if="isCronValid" :errors="[isCronValid]" />
                                <FieldDescription>{{ cronFreq }}</FieldDescription>
                            </Field>
                        </p>
                    </div>
                </div>
                <FieldSeparator />
                <FieldGroup>
                    <p>Parameters</p>
                    <Field>
                        <FieldLabel for="storeList">Store List</FieldLabel>
                        <Popover v-model:open="open">
                            <ListboxRoot v-model="storeListRef" highlight-on-hover multiple>
                                <PopoverAnchor class="inline-flex w-[300px]">
                                    <TagsInput v-slot="{ modelValue: tags }" v-model="storeListRef" class="w-full"
                                        :display-value="displayTagVal">
                                        <TagsInputItem v-for="item in tags"
                                            :key="item as Record<string, number>['value']" :value="item">
                                            <TagsInputItemText />
                                            <TagsInputItemDelete />
                                        </TagsInputItem>
                                        <ListboxFilter v-model="searchTerm" as-child>
                                            <TagsInputInput placeholder="Stores to import from..."
                                                @keydown.enter.prevent @keydown.down="open = true" />
                                        </ListboxFilter>
                                        <PopoverTrigger as-child>
                                            <Button size="icon-sm" variant="ghost"
                                                class="order-last self-start ml-auto">
                                                <ChevronDown class="size-3.5" />
                                            </Button>
                                        </PopoverTrigger>
                                    </TagsInput>
                                </PopoverAnchor>
                                <PopoverContent class="p-1" @open-auto-focus.prevent>
                                    <ListboxContent
                                        class="max-h-[300px] scroll-py-1 overflow-x-hidden overflow-y-auto empty:after:content-['No_options'] empty:p-1 empty:after:block"
                                        tabindex="0">
                                        <!-- <CommandEmpty>No results found.</CommandEmpty> -->
                                        <ListboxItem v-for="item in filteredStores" :key="item.value"
                                            class="data-highlighted:bg-accent data-highlighted:text-accent-foreground [&_svg:not([class*=\'text-\'])]:text-muted-foreground relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*=\'size-\'])]:size-4"
                                            :value="{ [item.label]: item.value } as Record<string, number>" @select="() => {
                                                searchTerm = ''
                                            }">
                                            <span>{{ item.label }}</span>
                                            <ListboxItemIndicator
                                                class="ml-auto inline-flex items-center justify-center">
                                                <CheckIcon />
                                            </ListboxItemIndicator>
                                        </ListboxItem>
                                    </ListboxContent>
                                </PopoverContent>
                            </ListboxRoot>
                        </Popover>
                    </Field>
                    <form.Field name="keyword" #default="{ field }">
                        <Field>
                            <FieldLabel :for="field.name">Keyword</FieldLabel>
                            <Input :id="field.name" :name="field.name" :model-value="field.state.value"
                                @input="field.handleChange($event.target.value)" />
                            <FieldError v-if="isInvalid(field)" :errors="field.state.meta.errors" />
                        </Field>
                    </form.Field>
                </FieldGroup>
            </FieldGroup>

        </div>
    </form>

    <DialogFooter>
        <DialogClose as-child>
            <Button variant="outline">Cancel</Button>
        </DialogClose>
        <Button type="submit" form="batchForm">Create</Button>
    </DialogFooter>
</template>
