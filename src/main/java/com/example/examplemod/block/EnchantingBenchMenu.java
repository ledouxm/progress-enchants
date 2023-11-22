package com.example.examplemod.block;

import java.util.List;
import java.util.Map;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.EnchantmentProgressSteps;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModMenus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class EnchantingBenchMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess levelAccess;
    public static final int ENCHANTING_SLOT = 36;
    private Map<Enchantment, Integer> possibleEnchantments;
    private EnchantingBenchEntity entity;

    private boolean showAll = false;

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll, Player player) {
        this.showAll = showAll;
        updatePossibleEnchantments(entity.getItemInStack(), player);
    }

    public EnchantingBenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf additionalData) {
        this(containerId, playerInventory,
                playerInventory.player.level().getBlockEntity(additionalData.readBlockPos()),
                new SimpleContainerData(2));
    }

    public EnchantingBenchMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity,
            ContainerData data) {
        super(ModMenus.ENCHANTING_BENCH_MENU.get(), containerId);
        if (!(blockEntity instanceof EnchantingBenchEntity entity)) {
            throw new IllegalStateException("Block entity is not an enchanting bench");
        }

        this.entity = entity;

        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        this.createPlayerHotbarSlots(playerInventory);
        this.createPlayerInventorySlots(playerInventory);
        this.createEnchantingSlot(entity);
    }

    private void createEnchantingSlot(EnchantingBenchEntity entity) {
        entity.getOptionnal().ifPresent(items -> {
            this.addSlot(new SlotItemHandler(items, 0, 180, 37));
        });
    }

    private void createPlayerInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 108 + column * 18, 84 + row * 18));
            }
        }
    }

    private void createPlayerHotbarSlots(Inventory inventory) {
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(inventory, column, 108 + column * 18, 142));
        }
    }

    private void updatePossibleEnchantments(ItemStack item, Player player) {
        System.out.println("updatePossibleEnchantments with item " + item);
        if (item == null || item.isEmpty()) {
            possibleEnchantments = null;
            return;
        }
        List<Enchantment> possibleEnchantmentsForItem = EnchantmentProgressSteps.getPossibleEnchantmentsForItem(item);
        System.out.println("possibleEnchantmentsForItem: " + possibleEnchantmentsForItem);
        this.possibleEnchantments = EnchantmentProgressManager.get(null)
                .filterEnchantmentsListForPlayer(possibleEnchantmentsForItem, player, true);
        System.out.println("possibleEnchantments: " + possibleEnchantments);

    }

    private void onItemChange(ItemStack item, Player player) {
        updatePossibleEnchantments(item, player);
    }

    public Map<Enchantment, Integer> getPossibleEnchantments() {
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

        System.out.println("rawStack: " + rawStack + " : " + index);

        if (index == ENCHANTING_SLOT) {
            System.out.println("ENCHANTING_SLOT");
            if (!this.moveItemStackTo(rawStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
            onItemChange(quickMovedStack, player);

        } else if (EnchantmentProgressSteps.canEnchant(rawStack)) {
            System.out.println("canEnchant");
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
