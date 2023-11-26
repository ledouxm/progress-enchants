package com.ledouxm.progressiveenchantments.client;

import com.ledouxm.progressiveenchantments.EnchantmentProgressManager;
import com.ledouxm.progressiveenchantments.ProgressiveEnchantments;
import com.ledouxm.progressiveenchantments.block.EnchantingBenchMenu;
import com.ledouxm.progressiveenchantments.block.EnchantingBenchRenderer;
import com.ledouxm.progressiveenchantments.block.EnchantingBenchScreen;
import com.ledouxm.progressiveenchantments.init.ModBlockEntities;
import com.ledouxm.progressiveenchantments.init.ModMenus;
import com.ledouxm.progressiveenchantments.item.AdventureBookScreen;
import com.ledouxm.progressiveenchantments.network.EnchantmentManagerClientboundPacket;
import com.ledouxm.progressiveenchantments.network.PossibleEnchantmentsClientboundPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ProgressiveEnchantments.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    public static void onEnchantmentManagerPacket(EnchantmentManagerClientboundPacket packet) {

        EnchantmentProgressManager.get(null).setPlayerData(packet.player, packet.playerData);
        EnchantmentProgressManager.get(null).setBonusClaimed(packet.player, packet.bonusClaimed);
        EnchantmentProgressManager.get(null).setProgress(packet.player, packet.progress);

        // if (localPlayer.containerMenu instanceof EnchantingBenchMenu) {
        // ((EnchantingBenchMenu)
        // localPlayer.containerMenu).updatePossibleEnchantments(localPlayer);
        // }
    }

    public static void onPossibleEnchantments(PossibleEnchantmentsClientboundPacket packet) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        packet.possibleEnchantments.stream().forEach(possibleEnchantment -> possibleEnchantment.setPlayer(localPlayer));

        if (localPlayer.containerMenu instanceof EnchantingBenchMenu) {
            ((EnchantingBenchMenu) localPlayer.containerMenu).setPossibleEnchantments(localPlayer,
                    packet.possibleEnchantments);
        }
    }

}
