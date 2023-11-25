package com.ledouxm.progressiveenchantments;

import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentProgressSnapshot {
    public final Enchantment enchantment;
    public final int score;

    public int currentLevel;
    public int nextLevelScore;
    public int currentLevelScore;

    public EnchantmentProgressSnapshot(Enchantment enchantment, int score) {
        this.enchantment = enchantment;
        this.score = score;

        this.init();
    }

    public void init() {
        this.currentLevel = EnchantmentProgressSteps.getLevel(enchantment, score);
        this.nextLevelScore = EnchantmentProgressSteps.getNextLevelScore(enchantment, currentLevel);
        this.currentLevelScore = EnchantmentProgressSteps.getCurrentLevelScore(enchantment, currentLevel);
    }

    public void setScore(int score) {
        this.init();
    }

    public boolean isMaxLevel() {
        return this.nextLevelScore == -1;
    }

    @Override
    public String toString() {
        return String.format("EnchantmentProgressSnapshot(%s, %d, %d, %d)", enchantment, score, currentLevel,
                nextLevelScore);
    }

    public double getPercentage() {
        if (this.nextLevelScore == -1) {
            return 1.0F;
        }

        return (double) (this.score - this.currentLevelScore) / (double) (this.nextLevelScore - this.currentLevelScore);
    }
}
