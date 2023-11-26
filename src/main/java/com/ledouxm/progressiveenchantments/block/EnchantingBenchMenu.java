package com.ledouxm.progressiveenchantments.block;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;

import com.ledouxm.progressiveenchantments.EnchantmentProgressManager;
import com.ledouxm.progressiveenchantments.EnchantmentProgressSteps;
import com.ledouxm.progressiveenchantments.EnchantmentProgressManager.Status;
import com.ledouxm.progressiveenchantments.init.ModBlocks;
import com.ledouxm.progressiveenchantments.init.ModMenus;
import com.ledouxm.progressiveenchantments.network.EnchantItemActionServerboundPacket;
import com.ledouxm.progressiveenchantments.network.EnchantmentProgressChannel;
import com.mojang.logging.LogUtils;

import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnchantingBenchMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ContainerLevelAccess levelAccess;
    public static final int ENCHANTING_SLOT = 36;
    public final Container enchantSlot = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantingBenchMenu.this.onItemChange(this.getItem(0), EnchantingBenchMenu.this.player);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack item) {
            return EnchantmentProgressSteps.canEnchant(item);
        };

        @Override
        public int getMaxStackSize() {
            return 1;
        };

    };

    private List<PossibleEnchantment> possibleEnchantments = new ArrayList<>();
    private EnchantingBenchEntity entity;
    private Player player;

    public EnchantingBenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf additionalData) {
        this(containerId, playerInventory,
                playerInventory.player.level().getBlockEntity(additionalData.readBlockPos()));
    }

    public EnchantingBenchMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenus.ENCHANTING_BENCH_MENU.get(), containerId);
        this.player = playerInventory.player;
        if (!(blockEntity instanceof EnchantingBenchEntity entity)) {
            throw new IllegalStateException("Block entity is not an enchanting bench");
        }

        this.entity = entity;

        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        this.createPlayerHotbarSlots(playerInventory);
        this.createPlayerInventorySlots(playerInventory);
        this.createEnchantingSlot(entity);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.levelAccess.execute((level, blockPos) -> {
            this.clearContainer(player, this.enchantSlot);
        });
    }

    private void createEnchantingSlot(EnchantingBenchEntity entity) {
        this.addSlot(new Slot(enchantSlot, 0, 239, 37));
    }

    private void createPlayerInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 167 + column * 18, 84 + row * 18));
            }
        }
    }

    private void createPlayerHotbarSlots(Inventory inventory) {
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(inventory, column, 167 + column * 18, 142));
        }
    }

    private void updatePossibleEnchantments(ItemStack item, Player player) {
        if (item == null || item.isEmpty()) {
            possibleEnchantments = null;
            return;
        }
        List<Enchantment> possibleEnchantmentsForItem = EnchantmentProgressSteps.getPossibleEnchantmentsForItem(item);
        this.possibleEnchantments = EnchantmentProgressManager.get(player.getServer())
                .filterEnchantmentsListForPlayer(possibleEnchantmentsForItem, player);
        LOGGER.info("Updating enchantments " + possibleEnchantments.get(0).enchantment + " "
                + possibleEnchantments.get(1).enchantment);

    }

    @Override
    public boolean clickMenuButton(Player __, int index) {
        LOGGER.info("Clicked menu button: " + index);
        PossibleEnchantment enchantment = possibleEnchantments.get(index);

        System.out.println("click1 " + player);
        return this.clickMenuButton(player, enchantment);
    }

    public void sendEnchantItemPacket(PossibleEnchantment enchantment) {
        EnchantmentProgressChannel.CHANNEL.sendToServer(new EnchantItemActionServerboundPacket(enchantSlot.getItem(0),
                enchantment));
    }

    public boolean clickMenuButton(Player player, PossibleEnchantment enchantment) {
        this.player = player;
        boolean isFree = enchantment.status == Status.FREE;
        boolean isLocked = enchantment.status == Status.LOCKED;
        LOGGER.info("Possible enchantment: " + enchantment.enchantment + " " + enchantment.level + " "
                + enchantment.status);

        if (!isFree && (isLocked || player.experienceLevel < enchantment.cost)) {
            return false;
        }

        ItemStack item = this.enchantSlot.getItem(0);
        LOGGER.info("Item: " + item);
        if (item.isEmpty())
            return false;

        boolean isBookAndEnchantable = item.getItem() == Items.BOOK && enchantment.enchantment.isAllowedOnBooks();
        LOGGER.info("Item is not empty");
        if (!(isBookAndEnchantable || enchantment.enchantment.canEnchant(item))) {
            return false;
        }
        LOGGER.info("Item can be enchanted");
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(item).entrySet()) {
            Enchantment currentEnchantment = entry.getKey();
            if (!currentEnchantment.isCompatibleWith(enchantment.enchantment)) {
                return false;
            }
        }

        // this.enchantItem(item, enchantment.enchantment, enchantment.level);

        if (player.level().isClientSide()) {
            this.sendEnchantItemPacket(enchantment);
        }

        this.performEnchantment(enchantment.enchantment, enchantment.level, enchantment.status, enchantment.cost);

        return true;
    }

    public void performEnchantment(Enchantment enchantment, int level, Status status, int cost) {
        boolean isFree = status == Status.FREE;

        LOGGER.info("Enchanting item");
        ItemStack item = this.enchantSlot.getItem(0);
        if (item.is(Items.BOOK)) {
            item = new ItemStack(Items.ENCHANTED_BOOK);
            this.enchantSlot.setItem(0, item);
        }
        item.enchant(enchantment, level);
        // EnchantmentHelper.setEnchantments(Map.of(enchantment, level),
        // enchantSlot.getItem(0));

        if (!isFree) {
            player.experienceLevel -= cost;
            if (player.experienceLevel < 0) {
                player.experienceLevel = 0;
                player.experienceProgress = 0.0F;
                player.totalExperience = 0;
            }
        } else {
            EnchantmentProgressManager.get(player.getServer()).claimBonus(player,
                    enchantment,
                    level);
        }
    }

    public void onItemChange(ItemStack item, Player player) {
        LOGGER.info(player == null ? "Player is null" : player.toString());
        updatePossibleEnchantments(item, player);
    }

    public List<PossibleEnchantment> getPossibleEnchantments() {
        return possibleEnchantments;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        Slot quickMovedSlot = getSlot(index);
        ItemStack quickMovedStack = ItemStack.EMPTY;

        if (quickMovedSlot == null || !quickMovedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack rawStack = quickMovedSlot.getItem();
        quickMovedStack = rawStack.copy();

        if (index == ENCHANTING_SLOT) {
            if (!this.moveItemStackTo(rawStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
            onItemChange(rawStack, player);

        } else if (EnchantmentProgressSteps.canEnchant(rawStack)) {

            if (!this.moveItemStackTo(rawStack, ENCHANTING_SLOT, ENCHANTING_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
            onItemChange(quickMovedStack, player);
        }

        if (rawStack.isEmpty()) {
            quickMovedSlot.set(ItemStack.EMPTY);
        } else {
            quickMovedSlot.setChanged();
        }

        if (rawStack.getCount() == quickMovedStack.getCount()) {
            return ItemStack.EMPTY;
        }

        quickMovedSlot.onTake(player, rawStack);

        return quickMovedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, ModBlocks.ENCHANTING_BENCH.get());
    }

}
