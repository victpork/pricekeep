<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue';
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar/';
import Navbar from '@/components/Navbar.vue';
import { RouterView } from 'vue-router';

const defaultOpen = true

</script>

<template>
  <SidebarProvider :default-open="defaultOpen">
    <AppSidebar />
    <SidebarInset>
      <header class="flex h-16 shrink-0 items-center gap-2 border-b px-4 text-center justify-center ease-linear">
        <Navbar />
      </header>
      <div>
        <router-view v-slot="{ Component }">
          <Transition name="fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>