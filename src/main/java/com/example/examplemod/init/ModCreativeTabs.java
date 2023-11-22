package com.example.examplemod.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.example.examplemod.ExampleMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            ExampleMod.MODID);

    public static final List<Supplier<? extends ItemLike>> EXAMPLE_TAB_ITEMS = new ArrayList<>();

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = TABS.register("enchanting_progress_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ExampleMod.MODID + ".enchanting_progress_tab"))
                    .icon(ModItems.ENCHANTING_BENCH.get()::getDefaultInstance)
                    .displayItems((displayParams, output) -> EXAMPLE_TAB_ITEMS
                            .forEach(itemLike -> output.accept(itemLike.get())))
                    .withSearchBar()
                    .build());

    public static <T extends Item> RegistryObject<T> addToTab(RegistryObject<T> itemLike) {
        EXAMPLE_TAB_ITEMS.add(itemLike);
        return itemLike;
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.getEntries().putAfter(Items.ACACIA_LOG.getDefaultInstance(),
                    ModItems.ENCHANTING_BENCH.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
