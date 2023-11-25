package com.ledouxm.progressiveenchantments;

import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentWithLevel {
    public Enchantment enchantment;
    public int level;

    public EnchantmentWithLevel(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }
}
