package com.example.examplemod.block;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.example.examplemod.ExampleMod;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantingBenchScreen extends AbstractContainerScreen<EnchantingBenchMenu> {

    private static final int PANEL_SRC_X = 88;
    private static final int PANEL_SRC_Y = 166;
    private static final int PANEL_X = 5;
    private static final int PANEL_Y = 18;
    private static final int PANEL_WIDTH = 88;
    private static final int PANEL_HEIGHT = 139;

    private static final int ITEM_HEIGHT = 12;

    private static final int SLIDER_SRC_X = 0;
    private static final int SLIDER_SRC_Y = 199;
    private static final int SLIDER_X = 94;
    private static final int SLIDER_Y = 18;

    private static final int SLIDER_WIDTH = 6;
    private static final int SLIDER_HEIGHT = 27;

    private static final ResourceLocation TEXTURE = new ResourceLocation(ExampleMod.MODID,
            "textures/gui/enchanting_bench_menu.png");

    public EnchantingBenchScreen(EnchantingBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
        super.init();

    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0, this.imageWidth,
        // this.imageHeight, 512, 256);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 367,
                226);

    }

    // 88 166
    // 95 177
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderPanel(graphics);
        this.renderSlider(graphics);
    }

    public void renderPanel(@NotNull GuiGraphics graphics) {
        Map<Enchantment, Integer> enchantments = this.menu.getPossibleEnchantments();
        if (enchantments == null) {
            return;
        }
        int cpt = 0;
        for (Enchantment enchantment : enchantments.keySet()) {
            int y = ITEM_HEIGHT * cpt;
            graphics.blit(TEXTURE, leftPos + PANEL_X, topPos + PANEL_Y + y, 0, PANEL_SRC_X, PANEL_SRC_Y, PANEL_WIDTH,
                    ITEM_HEIGHT,
                    367,
                    226);

            int progress = enchantments.get(enchantment);
            graphics.drawString(font, enchantment.getFullname(Math.max(progress, 1)), leftPos + PANEL_X + 2,
                    topPos + PANEL_Y + y, 4210752);
            cpt++;
        }
        // graphics.blit(TEXTURE, leftPos + PANEL_X, topPos + PANEL_Y, 1, PANEL_SRC_X,
        // PANEL_SRC_Y, PANEL_WIDTH,
        // ITEM_HEIGHT, 367,
        // 226);
    }

    public void renderSlider(@NotNull GuiGraphics graphics) {
        graphics.blit(TEXTURE, leftPos + SLIDER_X, topPos + SLIDER_Y, 2, SLIDER_SRC_X, SLIDER_SRC_Y, SLIDER_WIDTH,
                SLIDER_HEIGHT, 367, 226);
    }

}
