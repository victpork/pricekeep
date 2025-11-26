<script setup lang="ts">
import {
  Gauge,
  Settings, 
  Store, 
  Bell, 
  Package2, 
  TableConfig, 
  PanelLeftOpenIcon,
  PanelLeftCloseIcon,
} from "lucide-vue-next"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarHeader,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  useSidebar
} from '@/components/ui/sidebar'
import { RouterLink } from "vue-router";
import { ref } from "vue";

const {toggleSidebar, open, openMobile} = useSidebar()
const toggleIcon = ref(PanelLeftCloseIcon)
const toggleBtnClass = ref("")
openMobile.value = true
const toggle = () => {
  toggleSidebar();
  toggleIcon.value = open.value ? PanelLeftCloseIcon: PanelLeftOpenIcon
  toggleBtnClass.value = open.value ? "bg-cyan-500" : ""
}

const linkStruct = [
  {
    name: "Pages",
    items: [
      {
        title: "Dashboard",
        url: "/dashboard",
        icon: Gauge,
      },
    ],
  },
  {
    name: "Config",
    items: [
      {
        title: "Stores",
        url: "#",
        icon: Store,
      },
      {
        title: "Products",
        url: "#",
        icon: Package2,
      },
      {
        title: "Data Sources",
        url: "#",
        icon: TableConfig,
      },
      {
        title: "Alerts",
        url: "#",
        icon: Bell,
      },
      {
        title: "Settings",
        url: "#",
        icon: Settings,
      },
    ]
  }
]
</script>

<template>
  <Sidebar collapsible="icon">
    <SidebarHeader>
      <button @click="toggle">
        <div class="px-1"><component :is="toggleIcon"/></div>
      </button>
    </SidebarHeader>
    <SidebarContent>
      <SidebarGroup v-for="group in linkStruct">
        <SidebarGroupLabel class="text-base flex justify-center px-0"><span>{{ group.name }}</span></SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="item in group.items" :key="item.title">
              <SidebarMenuButton asChild size="xl" :isActive="item.url === $route.fullPath">
                <RouterLink :to="item.url" class="flex flex-col items-center gap-1 h-auto py-2">
                  <component :is="item.icon"/>
                  <span class="text-xs">{{ item.title }}</span>
                </RouterLink>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
    <SidebarFooter />
  </Sidebar>
</template>

<style scoped>

</style>