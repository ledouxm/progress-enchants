package com.ledouxm.progressiveenchantments.block;

import com.ledouxm.progressiveenchantments.EnchantmentProgressManager;
import com.ledouxm.progressiveenchantments.EnchantmentProgressManager.Status;
import com.ledouxm.progressiveenchantments.EnchantmentProgressSnapshot;
import com.ledouxm.progressiveenchantments.EnchantmentProgressSteps;
import com.ledouxm.progressiveenchantments.EnchantmentUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;

public class PossibleEnchantment {
    public Enchantment enchantment;
    public int level;
    public Player player;
    public Status status;
    public int cost;
    public boolean canBuy;
    public EnchantmentProgressSnapshot snapshot;

    public PossibleEnchantment(Enchantment enchantment, int level, Player player) {
        this.enchantment = enchantment;
        this.level = level;
        if (player == null)
            return;

        this.setPlayer(player);
    }

    public void setPlayer(Player player) {
        this.player = player;

        this.snapshot = EnchantmentProgressManager.get(player.getServer()).getEnchantmentProgressSnapshotForPlayer(
                player,
                enchantment, level);
        this.status = EnchantmentProgressManager.get(player.getServer()).getEnchantStatusForPlayer(player, enchantment,
                level);
        if (this.status == Status.UNLOCKED) {
            this.cost = EnchantmentProgressSteps.getCost(enchantment, level);
        } else {
            this.cost = 0;
        }
        this.canBuy = (this.status == Status.FREE)
                || (this.status == Status.UNLOCKED && this.player.experienceLevel >= this.cost);
    }

    public PossibleEnchantment(FriendlyByteBuf buffer, Player player) {
        this(EnchantmentUtils.getEnchantmentById(buffer.readUtf()), buffer.readInt(), player);
    }

    public PossibleEnchantment(FriendlyByteBuf buffer) {
        this(EnchantmentUtils.getEnchantmentById(buffer.readUtf()), buffer.readInt(), null);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(EnchantmentUtils.getEnchantmentId(this.enchantment));
        buffer.writeInt(this.level);
    }

    @Override
    public String toString() {
        return String.format("PossibleEnchantment(%s, %d, %s, %s, %d)", enchantment, level, player, status, cost);
    }
}
