package com.example.examplemod.block;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.EnchantmentProgressManager.Status;
import com.example.examplemod.EnchantmentProgressSnapshot;
import com.example.examplemod.EnchantmentProgressSteps;

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
        this.player = player;
        this.snapshot = EnchantmentProgressManager.get(null).getEnchantmentProgressSnapshotForPlayer(player,
                enchantment, level);
        this.status = EnchantmentProgressManager.get(null).getEnchantStatusForPlayer(player, enchantment,
                level);
        if (this.status == Status.UNLOCKED) {
            this.cost = EnchantmentProgressSteps.getCost(enchantment, level);
        } else {
            this.cost = 0;
        }
        this.canBuy = (this.status == Status.FREE)
                || (this.status == Status.UNLOCKED && this.player.experienceLevel >= this.cost);
    }

    @Override
    public String toString() {
        return String.format("PossibleEnchantment(%s, %d, %s, %s, %d)", enchantment, level, player, status, cost);
    }
}
