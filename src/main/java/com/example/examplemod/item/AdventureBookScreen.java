package com.example.examplemod.item;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.EnchantmentProgressSnapshot;
import com.example.examplemod.EnchantmentProgressSteps;
import com.example.examplemod.EnchantmentUtils;
import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdventureBookScreen extends Screen {
    /**
     *
     */

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation TEXTURE = new ResourceLocation(ExampleMod.MODID,
            "textures/gui/adventure_book_screen.png");

    public static final int GLOBAL_OFFSET_Y = -10;

    public static final int BOOK_SRC_X = 20;
    public static final int BOOK_SRC_Y = 1;

    public static final int BOOK_WIDTH = 146;
    public static final int BOOK_HEIGHT = 180;

    public static final int WRITABLE_OFFSET_X = 18;
    public static final int WRITABLE_OFFSET_Y = 16;

    public static final int WRITABLE_WIDTH = 110;
    public static final int WRITABLE_HEIGHT = 140;

    public static final int BACK_BUTTON_OFFSET_X = 20;
    public static final int FORWARD_BUTTON_OFFSET_X = 90;
    public static final int BUTTONS_OFFSET_Y = 154;

    private static final int PROGRESS_SRC_X = 166;

    private static final int PROGRESS_COMPLETE_SRC_Y = 0;
    private static final int PROGRESS_EMPTY_SRC_Y = 5;
    private static final int PROGRESS_XP_SRC_Y = 10;
    private static final int PROGRESS_WIDTH = 88;
    private static final int PROGRESS_HEIGHT = 5;

    private static final int PROGRESS_OFFSET_X = 11;
    private static final int PROGRESS_OFFSET_Y = 18;

    private static final int NUMBERS_OFFSET_Y = 18;
    private static final int PROGRESS_NUMBERS_MARGIN = 2;
    private static final int RIGHT_NUMBER_OFFSET_X = 99 + PROGRESS_NUMBERS_MARGIN;

    private static final int ITEM_HEIGHT = 27;

    private static final int DESCRIPTION_OFFSET_Y = 10;

    private static final int DESCRIPTION_OFFSET_X = 18;

    public int leftPos;
    public int topPos;
    public Player player;
    public List<EnchantmentProgressSnapshot> progress;

    private PageButton forwardButton;
    private PageButton backButton;

    public int currentPage = 0;
    public int maxPages = 0;

    private int nbItemsPerPage;

    private Map<Enchantment, Boolean> bonusEnchantments;

    private int size;

    private Map<Enchantment, Integer> playerData;

    public AdventureBookScreen(Player player) {
        super(Component.translatable("gui.examplemod.adventure_book.title"));
        this.player = player;

        tryToLoadProgress();
    }

    @Override
    protected void init() {
        this.getLeftAndTopPos();
        this.forwardButton = this
                .addRenderableWidget(
                        new PageButton(leftPos + FORWARD_BUTTON_OFFSET_X, topPos + BUTTONS_OFFSET_Y, true,
                                (__) -> this.pageForward(), true));
        this.backButton = this
                .addRenderableWidget(new PageButton(leftPos + BACK_BUTTON_OFFSET_X, topPos + BUTTONS_OFFSET_Y, false,
                        (__) -> this.pageBack(), true));

    }

    private void updatePagination() {
        if (this.progress == null) {
            return;
        }

        this.nbItemsPerPage = (int) Math.floor(WRITABLE_HEIGHT / ITEM_HEIGHT);
        this.size = progress.size() + bonusEnchantments.size();
        this.maxPages = (int) Math.ceil(size / (double) nbItemsPerPage);

        if (this.currentPage >= this.maxPages) {
            this.currentPage = this.maxPages - 1;
        }

        this.updateButtonVisibility();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }

        this.updateButtonVisibility();
    }

    public void pageForward() {
        if (this.currentPage < this.maxPages - 1) {
            ++this.currentPage;
        }

        this.updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        if (this.forwardButton == null || this.backButton == null)
            return;

        this.forwardButton.visible = this.currentPage < this.maxPages - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    public void getLeftAndTopPos() {
        this.leftPos = (this.width - BOOK_WIDTH) / 2;
        this.topPos = (this.height - BOOK_HEIGHT) / 2 + GLOBAL_OFFSET_Y;
    }

    protected void renderBg(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, BOOK_SRC_X, BOOK_SRC_Y, BOOK_WIDTH, BOOK_HEIGHT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (scroll > 0) {
            this.pageBack();
        } else if (scroll < 0) {
            this.pageForward();
        }

        return true;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.getLeftAndTopPos();

        this.renderBg(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderItems(graphics);
        // LOGGER.info("SALUT" +
        // EnchantmentProgressManager.get(null).getPlayerData(player));

    }

    public void tryToLoadProgress() {
        if (this.progress != null)
            return;
        this.progress = EnchantmentProgressManager.get(null).getProgress(player);
        this.bonusEnchantments = EnchantmentProgressSteps.getBonusEnchantmentsAvailability(player);
        this.playerData = EnchantmentProgressManager.get(null).getPlayerData(player);
        updatePagination();
    }

    public void renderItems(@NotNull GuiGraphics graphics) {
        if (progress == null) {
            tryToLoadProgress();
            return;
        }

        Collections.sort(progress, (a, b) -> {
            if (a.isMaxLevel() == b.isMaxLevel()) {
                return a.enchantment.getFullname(0).toString().compareTo(b.enchantment.getFullname(0).toString());
            }

            return a.isMaxLevel() ? 1 : -1;
        });

        int start = currentPage * nbItemsPerPage;
        int end = Math.min(start + nbItemsPerPage, size);
        for (int i = start; i < end; i++) {
            int y = topPos + WRITABLE_OFFSET_Y + (i - start) * ITEM_HEIGHT;

            if (i < progress.size()) {
                EnchantmentProgressSnapshot snapshot = progress.get(i);
                this.renderItemName(graphics, y, snapshot.enchantment);
                this.renderItemDescription(graphics, y, snapshot.enchantment);
                this.renderProgress(graphics, y, snapshot);
                this.renderProgressNumbers(graphics, y, snapshot);
            } else {
                Enchantment enchantment = (Enchantment) bonusEnchantments.keySet().toArray()[i - progress.size()];
                this.renderItemName(graphics, y, enchantment);
                this.renderBonusItemDescription(graphics, y, enchantment);
            }

        }
    }

    public void renderItemName(@NotNull GuiGraphics graphics, int y, Enchantment enchantment) {
        int currentLevel = playerData.getOrDefault(enchantment, 0);
        MutableComponent text = currentLevel == 0 ? Component.translatable(enchantment.getDescriptionId())
                : (MutableComponent) enchantment.getFullname(currentLevel);
        drawString(graphics, font,
                text.withStyle(ChatFormatting.BLACK),
                leftPos + WRITABLE_OFFSET_X, y, 8.0D, 0x000000,
                false);

    }

    public void renderItemDescription(@NotNull GuiGraphics graphics, int y, Enchantment enchantment) {
        String descriptionId = enchantment.getDescriptionId();

        MutableComponent text = Component.translatable(descriptionId.replace("minecraft", "examplemod") +
                ".desc");

        final List<FormattedCharSequence> lines = font.split(text, WRITABLE_WIDTH * font.lineHeight / 6);
        int baseY = y + DESCRIPTION_OFFSET_Y;
        for (FormattedCharSequence line : lines) {
            drawString(graphics, font,
                    line,
                    leftPos + DESCRIPTION_OFFSET_X, baseY, 6.0D, 0x000000,
                    false);
            baseY += 6;
        }

        // drawString(graphics, font,
        // text.withStyle(ChatFormatting.BLACK),
        // leftPos + DESCRIPTION_OFFSET_X, y + DESCRIPTION_OFFSET_Y, 6.0D, 0x000000,
        // false);
    }

    public void renderBonusItemDescription(@NotNull GuiGraphics graphics, int y, Enchantment enchantment) {
        List<Enchantment> requirements = EnchantmentUtils.getRequirementsForBonusEnchantment(enchantment);

        MutableComponent text = Component.empty();
        Component delimiter = Component.literal(", ");

        for (int i = 0; i < requirements.size(); i++) {
            Enchantment requiredEnchantment = requirements.get(i);
            boolean isMaxLevel = EnchantmentProgressManager.get(null).isMaxLevel(enchantment, player);
            text.append(Component.translatable(requiredEnchantment.getDescriptionId())
                    .withStyle(isMaxLevel ? ChatFormatting.GREEN : ChatFormatting.BLACK));

            if (i < requirements.size() - 1) {
                text.append(delimiter.copy());
            }
        }

        final List<FormattedCharSequence> lines = font.split(text, WRITABLE_WIDTH * font.lineHeight / 6);
        int baseY = y + DESCRIPTION_OFFSET_Y;

        for (FormattedCharSequence line : lines) {
            drawString(graphics, font, line, leftPos + DESCRIPTION_OFFSET_X, baseY, 6.0D, 0x000000,
                    false);
            baseY += 6;
        }
    }

    public void renderProgressNumbers(@NotNull GuiGraphics graphics, int y, EnchantmentProgressSnapshot snapshot) {
        if (snapshot.isMaxLevel()) {
            return;
        }

        int leftNumber = snapshot.score;
        int rightNumber = snapshot.nextLevelScore;

        int leftNumberWidth = (int) (font.width(leftNumber + "") * 5.0D / font.lineHeight);
        int leftNumberOffsetX = PROGRESS_OFFSET_X - leftNumberWidth - PROGRESS_NUMBERS_MARGIN;

        drawString(graphics, font, Component.literal(leftNumber + "").withStyle(ChatFormatting.BLACK),
                leftPos + WRITABLE_OFFSET_X + leftNumberOffsetX, y + NUMBERS_OFFSET_Y, 5.0D, 0x000000, false);

        drawString(graphics, font, Component.literal(rightNumber + "").withStyle(ChatFormatting.BLACK),
                leftPos + WRITABLE_OFFSET_X + RIGHT_NUMBER_OFFSET_X, y + NUMBERS_OFFSET_Y, 5.0D, 0x000000, false);
    }

    public void renderProgress(@NotNull GuiGraphics graphics, int y, EnchantmentProgressSnapshot snapshot) {
        if (snapshot.isMaxLevel()) {
            renderCompleteProgress(graphics, y, snapshot);
            return;
        }

        renderProgressXp(graphics, y, snapshot);
    }

    public void renderProgressXp(@NotNull GuiGraphics graphics, int y, EnchantmentProgressSnapshot snapshot) {
        double percentage = snapshot.getPercentage();
        int progressWidth = (int) (percentage * PROGRESS_WIDTH);

        graphics.blit(TEXTURE, leftPos + WRITABLE_OFFSET_X + PROGRESS_OFFSET_X, y + PROGRESS_OFFSET_Y,
                PROGRESS_SRC_X, PROGRESS_EMPTY_SRC_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT);

        graphics.blit(TEXTURE, leftPos + WRITABLE_OFFSET_X + PROGRESS_OFFSET_X, y + PROGRESS_OFFSET_Y,
                PROGRESS_SRC_X, PROGRESS_XP_SRC_Y, progressWidth, PROGRESS_HEIGHT);
    }

    public void renderCompleteProgress(@NotNull GuiGraphics graphics, int y, EnchantmentProgressSnapshot snapshot) {
        graphics.blit(TEXTURE, leftPos + WRITABLE_OFFSET_X + PROGRESS_OFFSET_X, y + PROGRESS_OFFSET_Y,
                PROGRESS_SRC_X, PROGRESS_COMPLETE_SRC_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT);
    }

    public static void drawString(@NotNull GuiGraphics graphics, Font font, Component text, int x, int y,
            double fontSize,
            int color,
            boolean shadow) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        double scale = fontSize / font.lineHeight;
        poseStack.scale((float) scale, (float) scale, (float) scale);

        graphics.drawString(font, text, 0, 0, color, shadow);

        poseStack.popPose();
    }

    public static void drawString(@NotNull GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y,
            double fontSize,
            int color,
            boolean shadow) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        double scale = fontSize / font.lineHeight;
        poseStack.scale((float) scale, (float) scale, (float) scale);

        graphics.drawString(font, text, 0, 0, color, shadow);

        poseStack.popPose();
    }

    protected void closeScreen() {
        this.minecraft.setScreen((Screen) null);
    }
}
