<script setup lang="ts">
import { Input } from '@/components/ui/input'
import { Search } from 'lucide-vue-next'
import { ref, watch, computed } from 'vue'
import { cn } from '@/lib/utils';
import { PopoverAnchor, Popover, PopoverContent } from '@/components/ui/popover/';

const props = defineProps<{
  suggest: string[]
  class?: string
}>()

const model = defineModel<string>()

const suggestions = computed({ get: () => (props.suggest), set: (v) => { v = v; v = [] } })
const selectedIndex = ref(-1)
const isLoading = ref(false)
const isFocused = ref(false)

const openDropdown = computed(() => (isFocused.value && suggestions.value.length > 0))

function handleInputChange(event: Event) {
  const newValue = (event.target as HTMLInputElement).value
  model.value = newValue
  selectedIndex.value = -1
}

function handleKeyDown(event: KeyboardEvent) {
  if (event.key === 'ArrowDown') {
    event.preventDefault()
    selectedIndex.value = Math.min(selectedIndex.value + 1, suggestions.value.length - 1)
  } else if (event.key === 'ArrowUp') {
    event.preventDefault()
    selectedIndex.value = Math.max(0, selectedIndex.value + 1)
  } else if (event.key === 'Enter' && selectedIndex.value >= 0) {
    console.log(`enter key with ${suggestions.value[selectedIndex.value]}`)
    model.value = (suggestions.value[selectedIndex.value])
    suggestions.value = []
    selectedIndex.value = -1
  } else if (event.key === 'Escape') {
    suggestions.value = []
    selectedIndex.value = -1
  }
}

function handleSuggestionClick(suggestion: string) {
  console.log(`mouse click with ${suggestion}`)
  model.value = suggestion
  suggestions.value = []
  selectedIndex.value = -1
}

function handleFocus() {
  isFocused.value = true
}

function handleBlur() {
  // Delay hiding suggestions to allow for click events on suggestions
  setTimeout(() => {
    isFocused.value = false
    suggestions.value = []
    selectedIndex.value = -1
  }, 300)
}

watch(suggestions, (w) => {
  console.log("Array changed: " + w)
})
</script>

<template>
  <div class="w-full max-w-xs mx-auto">
    <Popover :open="openDropdown">
      <PopoverAnchor asChild>
        <div class="relative w-full max-w-sm items-center">
          <Input id="search" type="text" placeholder="Search..." class="pl-10" v-model="model"
            @input="handleInputChange" @keydown="handleKeyDown" @Focus="handleFocus" @Blur="handleBlur"
            aria-label="Search input" aria-autocomplete="list" aria-controls="suggestions-list"
            :aria-expanded="suggestions.length > 0" />
          <span class="absolute start-0 inset-y-0 flex items-center justify-center px-2">
            <Search class="size-6 text-muted-foreground" />
          </span>
        </div>
      </PopoverAnchor>
      <PopoverContent asChild>
        <div aria-live="polite" v-if="isLoading && isFocused">
          Loading...
        </div>
        <!--  -->
        <ul v-if="suggest.length > 0 && !isLoading && isFocused" id="suggestions-list" role="listbox"
          :class="cn('grid grid-cols-1 gap-1 px-1 py-1 rounded-sm', props.class)">
          <li v-for="(suggestion, index) in suggest" :key="suggestion"
            class="px-3 py-1 cursor-pointer hover:bg-muted rounded-sm"
            :class="index === selectedIndex ? 'bg-muted' : ''" @click="handleSuggestionClick(suggestion)" role="option"
            :aria-selected="index === selectedIndex">
            {{ suggestion }}
          </li>
        </ul>
      </PopoverContent>
    </Popover>
  </div>
</template>