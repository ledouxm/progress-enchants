package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantingBenchEntity extends BlockEntity implements MenuProvider {
    private static final Component TITLE = Component
            .translatable("container." + ExampleMod.MODID + ".enchanting_bench_menu");

    public EnchantingBenchEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_BENCH_ENTITY.get(), pos, state);
    }

    public void enchantItem(ItemStack item, PossibleEnchantment enchantment) {
        item.enchant(enchantment.enchantment, enchantment.level);
    }

    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new EnchantingBenchMenu(p_39954_, p_39955_, this);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

}
