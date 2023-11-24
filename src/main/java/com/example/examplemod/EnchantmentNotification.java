package com.example.examplemod;

import java.util.List;

import java.util.ArrayList;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Bus.FORGE)
public class EnchantmentNotification {

    public static List<Component> queue = new ArrayList<Component>();
    public static int animationTick = 0;

    public static Component currentString;
    public static int x;
    public static int y;

    @SubscribeEvent
    public static void onRenderOverLayer(RenderGuiEvent.Post event) {
        if (currentString == null)
            return;
        GuiGraphics graphics = event.getGuiGraphics();
        Window window = event.getWindow();

        int x = window.getGuiScaledWidth() / 2;

        graphics.drawCenteredString(Minecraft.getInstance().font, currentString, x, y, 0xFFFFFF);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == ClientTickEvent.Phase.END) {
            if (currentString == null)
                return;
            animationTick++;
            updateAnimation();
        }
    }

    public static void addNotification(Component component) {
        queue.add(component);
        if (currentString == null) {
            popQueue();
        }
    }

    public static void popQueue() {
        if (queue.size() > 0) {
            currentString = queue.get(0);
            queue.remove(0);
        } else
            currentString = null;
    }

    public static void updateAnimation() {
        if (animationTick < 10) {
            y = animationTick;
        } else if (animationTick < 40) {
        } else if (animationTick < 50) {
            y = 50 - animationTick;
        } else {
            animationTick = 0;
            popQueue();
        }
    }

}
