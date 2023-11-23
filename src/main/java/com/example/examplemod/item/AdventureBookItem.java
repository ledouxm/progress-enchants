package com.example.examplemod.item;

import com.example.examplemod.EnchantmentProgressManager;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AdventureBookItem extends Item {

    public AdventureBookItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        System.out.println("AdventureBookItem.use: " + item);

        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new AdventureBookScreen(player));
            return InteractionResultHolder.success(item);
        }

        EnchantmentProgressManager.get(player.getServer()).sendDataToPlayer((ServerPlayer) player);

        return InteractionResultHolder.success(item);
    }
}
