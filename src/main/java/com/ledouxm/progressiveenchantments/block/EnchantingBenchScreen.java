package com.ledouxm.progressiveenchantments.block;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.ledouxm.progressiveenchantments.ProgressiveEnchantments;
import com.ledouxm.progressiveenchantments.EnchantmentProgressManager.Status;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EnchantingBenchScreen extends AbstractContainerScreen<EnchantingBenchMenu> {

    private static final int ENCHANTMENT_SRC_X = 88;
    private static final int UNLOCKED_ENCHANTMENT_SRC_Y = 166;
    private static final int LOCKED_ENCHANTMENT_SRC_Y = 186;
    private static final int FREE_ENCHANTMENT_SRC_Y = 206;

    private static final int PANEL_X = 4;
    private static final int PANEL_Y = 18;
    private static final int PANEL_WIDTH = 149;
    private static final int PANEL_HEIGHT = 140;

    private static final int ITEM_HEIGHT = 20;

    private static final int XP_ORB_SRC_X = 80;
    private static final int XP_ORB_SRC_Y = 166;

    private static final int XP_ORB_OFFSET_X = 136;
    private static final int XP_ORB_OFFSET_Y = 6;

    private static final int XP_ORB_WIDTH = 8;
    private static final int XP_ORB_HEIGHT = 8;

    private static final int COST_OFFSET_X = 122;
    private static final int COST_OFFSET_Y = 6;

    private static final int PROGRESS_SRC_X = 0;
    private static final int PROGRESS_COMPLETE_SRC_Y = 181;
    private static final int PROGRESS_EMPTY_SRC_Y = 186;
    private static final int PROGRESS_XP_SRC_Y = 191;

    private static final int PROGRESS_WIDTH = 88;
    private static final int PROGRESS_HEIGHT = 5;

    private static final int PROGRESS_OFFSET_X = 31;
    private static final int PROGRESS_OFFSET_Y = 13;

    private static final int SLIDER_ACTIVE_SRC_X = 0;
    private static final int SLIDER_INACTIVE_SRC_X = 6;
    private static final int SLIDER_SRC_Y = 199;
    private static final int SLIDER_X = 154;
    private static final int SLIDER_Y = 18;
    private static final int SLIDER_MAX_Y = 131;

    private static final int SLIDER_WIDTH = 6;
    private static final int SLIDER_HEIGHT = 27;

    private boolean canBeScrolled = false;
    private int maxScrollOffset = 0;
    private int scrollOffset = 0;
    private int nbDisplayable;
    private int startEnchantIndex;
    private int endEnchantIndex;
    private int sliderY;
    private boolean isDragging;

    private List<PossibleEnchantment> possibleEnchantments;

    private static final ResourceLocation TEXTURE = new ResourceLocation(ProgressiveEnchantments.MODID,
            "textures/gui/enchanting_bench_menu.png");

    public EnchantingBenchScreen(EnchantingBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 335;
        this.inventoryLabelX = 166;
        super.init();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0, this.imageWidth,
        // this.imageHeight, 512, 256);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 367,
                226);

    }

    public void updateScroll(int nbToDisplay) {
        this.nbDisplayable = PANEL_HEIGHT / ITEM_HEIGHT;
        this.canBeScrolled = nbToDisplay > nbDisplayable;

        if (!this.canBeScrolled) {
            this.maxScrollOffset = 0;
            this.scrollOffset = 0;
            return;
        }
        this.maxScrollOffset = (nbToDisplay - nbDisplayable) * ITEM_HEIGHT;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (!this.canBeScrolled) {
            return true;
        }

        if (scroll > 0) {
            this.scrollOffset = Math.max(0, this.scrollOffset - ITEM_HEIGHT);
        } else {
            this.scrollOffset = Math.min(this.maxScrollOffset, this.scrollOffset + ITEM_HEIGHT);
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickType) {
        if (mouseX >= leftPos + SLIDER_X && mouseX <= leftPos + SLIDER_X + SLIDER_WIDTH && mouseY >= sliderY
                && mouseY <= sliderY + SLIDER_HEIGHT) {
            this.isDragging = true;
            return super.mouseClicked(mouseX, mouseY, clickType);
        }

        // check if clicked on an enchantment
        if (mouseX < leftPos + PANEL_X || mouseX > leftPos + PANEL_X + PANEL_WIDTH || mouseY < topPos + PANEL_Y
                || mouseY > topPos + PANEL_Y + PANEL_HEIGHT) {
            return super.mouseClicked(mouseX, mouseY, clickType);
        }

        int enchantmentIndex = (int) Math.floor((mouseY - topPos - PANEL_Y + scrollOffset) / ITEM_HEIGHT);

        if (enchantmentIndex < startEnchantIndex || enchantmentIndex >= endEnchantIndex) {
            return super.mouseClicked(mouseX, mouseY, clickType);
        }

        if (this.menu.clickMenuButton(null, enchantmentIndex)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, enchantmentIndex);
            return true;

        }

        return super.mouseClicked(mouseY, clickType, enchantmentIndex);
    }

    @Override
    public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
        if (this.isDragging) {
            this.isDragging = false;
            return true;
        }

        return super.mouseReleased(p_97812_, p_97813_, p_97814_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickType, double deltaX, double deltaY) {
        if (isDragging) {
            int newScrollOffset = (int) ((mouseY - topPos - SLIDER_Y - (SLIDER_HEIGHT / 2))
                    / (float) (SLIDER_MAX_Y - SLIDER_Y)
                    * maxScrollOffset);
            int rounded = Math.round(newScrollOffset / (float) ITEM_HEIGHT) * ITEM_HEIGHT;
            this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, rounded));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, clickType, deltaX, deltaY);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderPanel(graphics);
        this.renderSlider(graphics);
    }

    public void renderPanel(@NotNull GuiGraphics graphics) {
        this.possibleEnchantments = this.menu.getPossibleEnchantments();
        this.updateScroll(possibleEnchantments == null ? 0 : possibleEnchantments.size());
        if (possibleEnchantments == null) {
            return;
        }

        this.startEnchantIndex = this.scrollOffset / ITEM_HEIGHT;
        this.endEnchantIndex = Math.min(possibleEnchantments.size(), startEnchantIndex + nbDisplayable);

        for (int i = startEnchantIndex; i < endEnchantIndex; i++) {
            renderItem(graphics, i * ITEM_HEIGHT, possibleEnchantments.get(i));
        }
    }

    public void renderItem(@NotNull GuiGraphics graphics, int y, PossibleEnchantment enchantment) {
        // render item background
        int srcY;
        switch (enchantment.status) {
            case UNLOCKED:
                srcY = UNLOCKED_ENCHANTMENT_SRC_Y;
                break;
            case FREE:
                srcY = FREE_ENCHANTMENT_SRC_Y;
                break;
            default:
                srcY = LOCKED_ENCHANTMENT_SRC_Y;
                break;
        }

        int startY = topPos + PANEL_Y + y - scrollOffset;
        graphics.blit(TEXTURE, leftPos + PANEL_X, startY, 0, ENCHANTMENT_SRC_X, srcY,
                PANEL_WIDTH, ITEM_HEIGHT, 367, 226);

        int labelColor = enchantment.canBuy ? 0xFFFFFF : 0x808080;

        graphics.drawString(font, enchantment.enchantment.getFullname(Math.max(enchantment.level, 1)).getString(),
                leftPos + PANEL_X + 2,
                startY + 3, labelColor, false);

        this.renderCost(graphics, startY, enchantment);
    }

    public void renderCost(@NotNull GuiGraphics graphics, int y, PossibleEnchantment enchantment) {
        if (enchantment.status == Status.LOCKED) {
            return;
        }

        graphics.blit(TEXTURE, leftPos + PANEL_X + XP_ORB_OFFSET_X, y + XP_ORB_OFFSET_Y, 0,
                XP_ORB_SRC_X, XP_ORB_SRC_Y, XP_ORB_WIDTH, XP_ORB_HEIGHT, 367, 226);

        int x = leftPos + PANEL_X + COST_OFFSET_X + (enchantment.cost < 10 ? 7 : 0);
        graphics.drawString(font, String.valueOf(enchantment.cost), x,
                y + COST_OFFSET_Y, enchantment.canBuy ? enchantment.status.color : 0xba370f, false);
    }

    public void renderProgress(@NotNull GuiGraphics graphics, int y, PossibleEnchantment enchantment) {
        if (enchantment.snapshot.isMaxLevel()) {
            renderCompleteProgress(graphics, y, enchantment);
            return;
        }

        renderProgressXp(graphics, y, enchantment);
    }

    public void renderProgressXp(@NotNull GuiGraphics graphics, int y, PossibleEnchantment enchantment) {
        double percentage = enchantment.snapshot.getPercentage();
        int progressWidth = (int) (percentage * PROGRESS_WIDTH);

        graphics.blit(TEXTURE, leftPos + PANEL_X + PROGRESS_OFFSET_X, y + PROGRESS_OFFSET_Y, 0,
                PROGRESS_SRC_X, PROGRESS_EMPTY_SRC_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, 367, 226);

        graphics.blit(TEXTURE, leftPos + PANEL_X + PROGRESS_OFFSET_X, y + PROGRESS_OFFSET_Y, 0,
                PROGRESS_SRC_X, PROGRESS_XP_SRC_Y, progressWidth, PROGRESS_HEIGHT, 367, 226);
    }

    public void renderCompleteProgress(@NotNull GuiGraphics graphics, int y, PossibleEnchantment enchantment) {
        graphics.blit(TEXTURE, leftPos + PANEL_X + PROGRESS_OFFSET_X, y + PROGRESS_OFFSET_Y, 0,
                PROGRESS_SRC_X, PROGRESS_COMPLETE_SRC_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, 367, 226);
    }

    public void renderSlider(@NotNull GuiGraphics graphics) {
        int sliderSrcX = canBeScrolled ? SLIDER_ACTIVE_SRC_X : SLIDER_INACTIVE_SRC_X;
        this.sliderY = topPos + SLIDER_Y
                + (int) ((float) scrollOffset / (float) maxScrollOffset * (SLIDER_MAX_Y - SLIDER_Y));

        graphics.blit(TEXTURE, leftPos + SLIDER_X, sliderY, 2, sliderSrcX, SLIDER_SRC_Y, SLIDER_WIDTH,
                SLIDER_HEIGHT, 367, 226);
    }

}
