package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.EnchantingBenchMenu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
                        ExampleMod.MODID);

        public static final RegistryObject<MenuType<EnchantingBenchMenu>> ENCHANTING_BENCH_MENU = MENUS.register(
                        "enchanting_bench_menu",
                        () -> IForgeMenuType.create(
                                        (containerId, playerInventory, additionalData) -> new EnchantingBenchMenu(
                                                        containerId,
                                                        playerInventory, additionalData)));

}
