package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlockEntities;

import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class EnchantingBenchEntity extends BlockEntity implements MenuProvider {
    private static final Component TITLE = Component
            .translatable("container." + ExampleMod.MODID + ".enchanting_bench_menu");

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            EnchantingBenchEntity.this.setChanged();
        }

    };
    private final LazyOptional<ItemStackHandler> optionnal = LazyOptional.of(() -> items);

    public EnchantingBenchEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_BENCH_ENTITY.get(), pos, state);
    }

    public ItemStack getItemInStack() {
        return items.getStackInSlot(0);
    }

    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new EnchantingBenchMenu(p_39954_, p_39955_, this, null);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    public LazyOptional<ItemStackHandler> getOptionnal() {
        return optionnal;
    }
}
