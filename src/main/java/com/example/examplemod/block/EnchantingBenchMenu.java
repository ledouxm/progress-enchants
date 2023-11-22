package com.example.examplemod.block;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.EnchantmentProgressSteps;
import com.example.examplemod.EnchantmentProgressManager.Status;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModMenus;
import com.mojang.logging.LogUtils;

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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnchantingBenchMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ContainerLevelAccess levelAccess;
    public static final int ENCHANTING_SLOT = 36;
    private final Container enchantSlot = new SimpleContainer(1) {
        public void setChanged() {
            super.setChanged();
            EnchantingBenchMenu.this.onItemChange(this.getItem(0), EnchantingBenchMenu.this.player);
        }
    };

    private List<PossibleEnchantment> possibleEnchantments = new ArrayList<>();
    private EnchantingBenchEntity entity;
    private Player player;

    public EnchantingBenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf additionalData) {
        this(containerId, playerInventory,
                playerInventory.player.level().getBlockEntity(additionalData.readBlockPos()));
        LOGGER.info("EnchantingBenchMenu constructor with buffer");
    }

    public EnchantingBenchMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenus.ENCHANTING_BENCH_MENU.get(), containerId);
        LOGGER.info("EnchantingBenchMenu constructor with blockEntity");
        if (!(blockEntity instanceof EnchantingBenchEntity entity)) {
            throw new IllegalStateException("Block entity is not an enchanting bench");
        }

        this.entity = entity;
        this.player = playerInventory.player;

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
        // System.out.println("updatePossibleEnchantments with item " + item);
        if (item == null || item.isEmpty()) {
            possibleEnchantments = null;
            return;
        }
        List<Enchantment> possibleEnchantmentsForItem = EnchantmentProgressSteps.getPossibleEnchantmentsForItem(item);
        // System.out.println("possibleEnchantmentsForItem: " +
        // possibleEnchantmentsForItem);
        this.possibleEnchantments = EnchantmentProgressManager.get(null)
                .filterEnchantmentsListForPlayer(possibleEnchantmentsForItem, player);
        // System.out.println("possibleEnchantments: " + possibleEnchantments);

    }

    @Override
    public boolean clickMenuButton(Player __, int index) {
        PossibleEnchantment enchantment = possibleEnchantments.get(index);
        boolean isFree = enchantment.status == Status.FREE;
        boolean isLocked = enchantment.status == Status.LOCKED;

        if (!isFree && (isLocked || player.experienceLevel < enchantment.cost)) {
            return false;
        }

        ItemStack item = this.enchantSlot.getItem(0);
        if (item.isEmpty())
            return false;

        if (!item.canApplyAtEnchantingTable(enchantment.enchantment)) {
            return false;
        }

        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(item).entrySet()) {
            Enchantment currentEnchantment = entry.getKey();
            if (!currentEnchantment.isCompatibleWith(enchantment.enchantment)) {
                return false;
            }
        }

        this.levelAccess.execute((level, blockPos) -> {
            LOGGER.info("Où suis je ?");
            entity.enchantItem(item, enchantment);

            if (!isFree) {
                LOGGER.info("Item is not free, removing " + enchantment.cost + " levels");
                player.experienceLevel -= enchantment.cost;
            } else {
                EnchantmentProgressManager.get(player.getServer()).claimBonus(player, enchantment.enchantment,
                        enchantment.level);
            }

            this.enchantSlot.setChanged();
            this.player.level().playSound(this.player, this.entity.getBlockPos(), SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS);

        });
        return true;
    }

    public void onItemChange(ItemStack item, Player player) {
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

        // System.out.println("rawStack: " + rawStack + " : " + index);

        if (index == ENCHANTING_SLOT) {
            // System.out.println("ENCHANTING_SLOT");
            if (!this.moveItemStackTo(rawStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
            onItemChange(rawStack, player);

        } else if (EnchantmentProgressSteps.canEnchant(rawStack)) {
            // System.out.println("canEnchant");
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
