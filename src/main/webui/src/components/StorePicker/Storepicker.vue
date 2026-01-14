<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { Popover, PopoverTrigger, PopoverContent } from '@/components/ui/popover';
import Button from '@/components/ui/button/Button.vue';
import { Command, CommandInput, CommandList, CommandEmpty, CommandGroup, CommandItem } from '@/components/ui/command';
import { useGetApiAdminStoreSearch } from '@/apiClient';
import type { StoreInfo } from '@/model';
import { ChevronsUpDownIcon } from 'lucide-vue-next';

const model = defineModel<number>()
const isPopoverClosed = ref(false)
const storeSearchQuery = ref<{ q?: string }>({ q: "" })
const searchEnabled = ref(false)
const { data } = useGetApiAdminStoreSearch(storeSearchQuery, { query: { enabled: searchEnabled } })
const storeList = computed<StoreInfo[]>(() => data.value?.data ?? [])
const selectedStore = ref<StoreInfo>()
watch(selectedStore, () => {
    model.value = selectedStore.value?.id
})
watch(storeSearchQuery, (newQuery) => {
    if (newQuery.q?.length ?? 0 > 2) {
        searchEnabled.value = true
    }
},
    { deep: true }
)
</script>
<template>
    <Popover v-model:open="isPopoverClosed">
        <PopoverTrigger as-child>
            <Button variant="outline" role="combobox" :aria-expanded="isPopoverClosed" class="justify-between">
                {{ selectedStore?.name || "Store to quote from..." }}
                <ChevronsUpDownIcon class="opacity-50" />
            </Button>
        </PopoverTrigger>
        <PopoverContent class="p-0">
            <Command>
                <CommandInput class="h-9" placeholder="Search store..."
                    @input="storeSearchQuery.q = $event.target.value" />
                <CommandList>
                    <CommandEmpty>No store found.</CommandEmpty>
                    <CommandGroup>
                        <CommandItem v-for="store in storeList" :key="store.id" :value="store.id ?? 0" @select="() => {
                            selectedStore = (selectedStore?.id === store.id) ? undefined : store
                            console.log(model, store.id, model === store.id)
                            isPopoverClosed = false
                        }">
                            {{ store.name }}
                        </CommandItem>
                    </CommandGroup>
                </CommandList>
            </Command>
        </PopoverContent>
    </Popover>
</template>