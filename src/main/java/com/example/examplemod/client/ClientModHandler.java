package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.EnchantingBenchRenderer;
import com.example.examplemod.block.EnchantingBenchScreen;
import com.example.examplemod.init.ModBlockEntities;
import com.example.examplemod.init.ModMenus;
import com.example.examplemod.item.AdventureBookScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModHandler {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ENCHANTING_BENCH_MENU.get(), EnchantingBenchScreen::new);
            BlockEntityRenderers.register(ModBlockEntities.ENCHANTING_BENCH_ENTITY.get(), EnchantingBenchRenderer::new);
        });
    }

    public static void openAdventureItemScreen(Player player) {
        Minecraft.getInstance().setScreen(new AdventureBookScreen(player));
    }

}
