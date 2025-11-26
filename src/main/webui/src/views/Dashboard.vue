<script setup lang="ts">
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { ScrollArea, ScrollBar } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { PlusCircleIcon, LayoutGridIcon, SheetIcon } from 'lucide-vue-next';
import { ButtonGroup } from '@/components/ui/button-group';
import {
  useGetApiCommonLatestDeals,
} from '@/apiClient';
import ProductColleciton from './ProductColleciton.vue';
import { computed } from 'vue';
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
  FieldLegend,
  FieldSeparator,
  FieldSet,
} from '@/components/ui/field'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
const { data: latestDeals } = useGetApiCommonLatestDeals()
const flatDeals = computed(() => (latestDeals.value?.data.results ?? []))
</script>
<template>
  <div class="col-span-3 lg:col-span-4 lg:border-l">
    <div class="h-full px-4 py-6 lg:px-8">
      <Tabs default-value="music" class="h-full space-y-6">
        <div class="space-between flex items-center">
          <TabsList>
            <TabsTrigger value="at-a-glance" class="relative px-5">
              <LayoutGridIcon />
            </TabsTrigger>
            <TabsTrigger value="products" class="px-5">
              <SheetIcon />
            </TabsTrigger>
          </TabsList>
          <div class="ml-auto mr-4">
            <Dialog>
              <form>
                <ButtonGroup class="hidden sm:flex">
                  <DialogTrigger as-child>
                    <Button variant="outline">
                      <PlusCircleIcon class="mr-2 h-4 w-4" />
                      Add product
                    </Button>
                  </DialogTrigger>
                  <DialogContent class="sm:max-w-[425px]">
                    <DialogHeader>
                      <DialogTitle>Add New Product</DialogTitle>
                      <DialogDescription>
                        Register a new product. Click save when you're done.
                      </DialogDescription>
                    </DialogHeader>
                    <FieldGroup>
                      <FieldSet>
                        <FieldLegend>Payment Method</FieldLegend>
                        <FieldDescription>
                          All transactions are secure and encrypted
                        </FieldDescription>
                        <FieldGroup>
                          <Field>
                            <FieldLabel for="checkout-7j9-card-name-43j">
                              Name on Card
                            </FieldLabel>
                            <Input id="checkout-7j9-card-name-43j" placeholder="Evil Rabbit" required />
                          </Field>
                          <Field>
                            <FieldLabel for="checkout-7j9-card-number-uw1">
                              Card Number
                            </FieldLabel>
                            <Input id="checkout-7j9-card-number-uw1" placeholder="1234 5678 9012 3456" required />
                            <FieldDescription>
                              Enter your 16-digit card number
                            </FieldDescription>
                          </Field>
                          <div class="grid grid-cols-3 gap-4">
                            <Field>
                              <FieldLabel for="checkout-exp-month-ts6">
                                Month
                              </FieldLabel>
                              <Select default-value="">
                                <SelectTrigger id="checkout-exp-month-ts6">
                                  <SelectValue placeholder="MM" />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="01">
                                    01
                                  </SelectItem>
                                  <SelectItem value="02">
                                    02
                                  </SelectItem>
                                  <SelectItem value="03">
                                    03
                                  </SelectItem>
                                  <SelectItem value="04">
                                    04
                                  </SelectItem>
                                  <SelectItem value="05">
                                    05
                                  </SelectItem>
                                  <SelectItem value="06">
                                    06
                                  </SelectItem>
                                  <SelectItem value="07">
                                    07
                                  </SelectItem>
                                  <SelectItem value="08">
                                    08
                                  </SelectItem>
                                  <SelectItem value="09">
                                    09
                                  </SelectItem>
                                  <SelectItem value="10">
                                    10
                                  </SelectItem>
                                  <SelectItem value="11">
                                    11
                                  </SelectItem>
                                  <SelectItem value="12">
                                    12
                                  </SelectItem>
                                </SelectContent>
                              </Select>
                            </Field>
                            <Field>
                              <FieldLabel for="checkout-7j9-exp-year-f59">
                                Year
                              </FieldLabel>
                              <Select default-value="">
                                <SelectTrigger id="checkout-7j9-exp-year-f59">
                                  <SelectValue placeholder="YYYY" />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="2024">
                                    2024
                                  </SelectItem>
                                  <SelectItem value="2025">
                                    2025
                                  </SelectItem>
                                  <SelectItem value="2026">
                                    2026
                                  </SelectItem>
                                  <SelectItem value="2027">
                                    2027
                                  </SelectItem>
                                  <SelectItem value="2028">
                                    2028
                                  </SelectItem>
                                  <SelectItem value="2029">
                                    2029
                                  </SelectItem>
                                </SelectContent>
                              </Select>
                            </Field>
                            <Field>
                              <FieldLabel for="checkout-7j9-cvv">
                                CVV
                              </FieldLabel>
                              <Input id="checkout-7j9-cvv" placeholder="123" required />
                            </Field>
                          </div>
                        </FieldGroup>
                      </FieldSet>
                      <FieldSeparator />
                      <FieldSet>
                        <FieldLegend>Billing Address</FieldLegend>
                        <FieldDescription>
                          The billing address associated with your payment method
                        </FieldDescription>
                        <FieldGroup>
                          <Field orientation="horizontal">
                            <Checkbox id="checkout-7j9-same-as-shipping-wgm" :default-value="true" />
                            <FieldLabel for="checkout-7j9-same-as-shipping-wgm" class="font-normal">
                              Same as shipping address
                            </FieldLabel>
                          </Field>
                        </FieldGroup>
                      </FieldSet>
                      <FieldSet>
                        <FieldGroup>
                          <Field>
                            <FieldLabel for="checkout-7j9-optional-comments">
                              Comments
                            </FieldLabel>
                            <Textarea id="checkout-7j9-optional-comments" placeholder="Add any additional comments"
                              class="resize-none" />
                          </Field>
                        </FieldGroup>
                      </FieldSet>
                      <Field orientation="horizontal">
                        <Button type="submit">
                          Submit
                        </Button>
                        <Button variant="outline" type="button">
                          Cancel
                        </Button>
                      </Field>
                    </FieldGroup>
                    <DialogFooter>
                      <DialogClose as-child>
                        <Button variant="outline">
                          Cancel
                        </Button>
                      </DialogClose>
                      <Button type="submit">
                        Save changes
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                  <Button variant="outline">
                    <PlusCircleIcon class="mr-2 h-4 w-4" />
                    Quote
                  </Button>
                </ButtonGroup>
              </form>
            </Dialog>
          </div>
        </div>
        <TabsContent value="at-a-glance" class="border-none p-0 outline-none">
          <div class="flex items-center justify-between">
            <div class="space-y-1">
              <h2 class="text-2xl font-semibold tracking-tight">
                Daily Deals
              </h2>
              <p class="text-sm text-muted-foreground">
                Top picks for you. Updated daily.
              </p>
            </div>
          </div>
          <Separator class="my-4" />
          <div class="relative">
            <ScrollArea>
              <div class="flex space-x-4 pb-4">
                <ProductColleciton v-model="flatDeals" />
              </div>
              <ScrollBar orientation="horizontal" />
            </ScrollArea>
          </div>
          <div class="mt-6 space-y-1">
            <h2 class="text-2xl font-semibold tracking-tight">
              Sales ongoing
            </h2>
            <p class="text-sm text-muted-foreground">
              Recently discovered sales
            </p>
          </div>
          <Separator class="my-4" />
          <div class="relative">
            <ScrollArea>
              <div class="flex space-x-4 pb-4">

              </div>
              <ScrollBar orientation="horizontal" />
            </ScrollArea>
          </div>
        </TabsContent>
        <TabsContent value="products" class="h-full flex-col border-none p-0 data-[state=active]:flex">
          <div class="flex items-center justify-between">
            <div class="space-y-1">
              <h2 class="text-2xl font-semibold tracking-tight">
                New Episodes
              </h2>
              <p class="text-sm text-muted-foreground">
                Your favorite podcasts. Updated daily.
              </p>
              <!-- ProductTable / -->
            </div>
          </div>
          <Separator class="my-4" />

        </TabsContent>
      </Tabs>
    </div>
  </div>
</template>