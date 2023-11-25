package com.ledouxm.progressiveenchantments.init;

import java.util.function.Supplier;

import com.ledouxm.progressiveenchantments.ProgressiveEnchantments;
import com.ledouxm.progressiveenchantments.item.AdventureBookItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        ProgressiveEnchantments.MODID);

        public static final RegistryObject<Item> ENCHANTING_BENCH = registerItem("enchanting_bench",
                        () -> new BlockItem(ModBlocks.ENCHANTING_BENCH.get(), new Item.Properties()));

        public static final RegistryObject<Item> ADVENTURE_BOOK = registerItem("adventure_book",
                        () -> new AdventureBookItem(new Item.Properties()));

        private static <T extends Item> RegistryObject<Item> registerItem(String name,
                        Supplier<? extends Item> supplier) {
                RegistryObject<Item> item = ModItems.ITEMS.register(name, supplier);
                ModCreativeTabs.addToTab(item);
                return item;
        }

}
