package com.ledouxm.progressiveenchantments.init;

import com.ledouxm.progressiveenchantments.ProgressiveEnchantments;
import com.ledouxm.progressiveenchantments.block.EnchantingBenchMenu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
                        ProgressiveEnchantments.MODID);

        public static final RegistryObject<MenuType<EnchantingBenchMenu>> ENCHANTING_BENCH_MENU = MENUS.register(
                        "enchanting_bench_menu",
                        () -> IForgeMenuType.create(EnchantingBenchMenu::new));

}
