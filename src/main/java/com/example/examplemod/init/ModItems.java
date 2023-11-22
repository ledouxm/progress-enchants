package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        ExampleMod.MODID);

        public static final RegistryObject<Item> ENCHANTING_BENCH = registerItem("enchanting_bench",
                        new Item.Properties());

        private static <T extends Item> RegistryObject<Item> registerItem(String name,
                        Item.Properties properties) {
                RegistryObject<Item> item = ModItems.ITEMS.register(name,
                                () -> new BlockItem(ModBlocks.ENCHANTING_BENCH.get(), properties));
                ModCreativeTabs.addToTab(item);
                return item;
        }

}
