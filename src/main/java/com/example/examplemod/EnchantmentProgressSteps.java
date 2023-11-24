package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class EnchantmentProgressSteps {

    public static final List<Enchantment> bonusEnchantments = new ArrayList<Enchantment>();
    static {
        {
            bonusEnchantments.add(Enchantments.MENDING);
            // bonusEnchantments.add(Enchantments.FROST_WALKER);
            bonusEnchantments.add(Enchantments.SILK_TOUCH);
            bonusEnchantments.add(Enchantments.CHANNELING);
            bonusEnchantments.add(Enchantments.INFINITY_ARROWS);
            bonusEnchantments.add(Enchantments.MULTISHOT);
        }
    };

    public static Map<Enchantment, int[]> steps = new HashMap<>();
    static {
        steps.put(Enchantments.UNBREAKING, new int[] { 1, 3, 6 });
        steps.put(Enchantments.ALL_DAMAGE_PROTECTION, new int[] { 25, 75, 125, 250 });
        steps.put(Enchantments.FIRE_PROTECTION, new int[] { 10, 60, 180, 300 });
        steps.put(Enchantments.BLAST_PROTECTION, new int[] { 3, 10, 25, 50 });
        steps.put(Enchantments.PROJECTILE_PROTECTION, new int[] { 10, 25, 60, 150 });
        steps.put(Enchantments.THORNS, new int[] { 5, 25, 75 });
        steps.put(Enchantments.RESPIRATION, new int[] { 20, 1, 3 });
        steps.put(Enchantments.AQUA_AFFINITY, new int[] { 32 });
        steps.put(Enchantments.FALL_PROTECTION, new int[] { 10, 25, 50, 100 });
        steps.put(Enchantments.DEPTH_STRIDER, new int[] { 15, 45, 90 });
        steps.put(Enchantments.SOUL_SPEED, new int[] { 10, 50, 250 });
        steps.put(Enchantments.SWIFT_SNEAK, new int[] { 50, 250, 500 });
        steps.put(Enchantments.SHARPNESS, new int[] { 300, 1000, 3000, 8000, 20000 });
        steps.put(Enchantments.SMITE, new int[] { 10, 25, 50, 80, 120 });
        steps.put(Enchantments.BANE_OF_ARTHROPODS, new int[] { 7, 15, 30, 50, 90 });
        steps.put(Enchantments.KNOCKBACK, new int[] { 3, 15 });
        steps.put(Enchantments.FIRE_ASPECT, new int[] { 5, 50 });
        steps.put(Enchantments.MOB_LOOTING, new int[] { 40, 100, 300 });
        steps.put(Enchantments.SWEEPING_EDGE, new int[] { 20, 50, 150 });
        steps.put(Enchantments.BLOCK_EFFICIENCY, new int[] { 100, 500, 1000, 3000, 6000 });
        steps.put(Enchantments.BLOCK_FORTUNE, new int[] { 150, 400, 800 });
        steps.put(Enchantments.LOYALTY, new int[] { 5, 20, 60 });
        steps.put(Enchantments.RIPTIDE, new int[] { 10, 50, 120 });
        steps.put(Enchantments.IMPALING, new int[] { 8, 20, 50, 100, 200 });
        steps.put(Enchantments.POWER_ARROWS, new int[] { 5, 20, 50, 80, 120 });
        steps.put(Enchantments.PUNCH_ARROWS, new int[] { 100, 250 });
        steps.put(Enchantments.PIERCING, new int[] { 5, 20, 50, 80, 120 });
        steps.put(Enchantments.QUICK_CHARGE, new int[] { 5, 20, 50 });
        steps.put(Enchantments.FLAMING_ARROWS, new int[] { 32 });
        steps.put(Enchantments.FISHING_LUCK, new int[] { 1, 3, 6 });
        steps.put(Enchantments.FISHING_SPEED, new int[] { 5, 15, 30 });
    }

    public static int getCost(Enchantment enchantment, int level) {
        int[] steps = EnchantmentProgressSteps.steps.get(enchantment);

        if (steps == null) {
            return 20;
        }

        switch (steps.length) {
            case 1:
                return 20;
            case 2:
                return level * 10;
            case 3:
                return level * 6;
            case 4:
                return level * 5;
            case 5:
                return level * 4;
        }

        return -1;
    }

    public static boolean isBonus(Enchantment enchantment) {
        return bonusEnchantments.contains(enchantment);
    }

    public static int getLevel(Enchantment enchantment, int score) {
        int[] steps = EnchantmentProgressSteps.steps.get(enchantment);

        if (steps == null) {
            return 0;
        }

        int progress = 0;
        for (int i = 0; i < steps.length; i++) {
            if (score >= steps[i]) {
                progress = i + 1;
            }
        }

        return progress;
    }

    public static int getNextLevelScore(Enchantment enchantment, int currentLevel) {
        int[] steps = EnchantmentProgressSteps.steps.get(enchantment);

        if (steps == null) {
            return 0;
        }

        if (currentLevel >= steps.length || currentLevel < 0) {
            return -1;
        }

        return steps[currentLevel];
    }

    public static int getCurrentLevelScore(Enchantment enchantment, int currentLevel) {
        int[] steps = EnchantmentProgressSteps.steps.get(enchantment);

        if (steps == null) {
            return 0;
        }

        if (currentLevel == 0) {
            return 0;
        }

        return steps[currentLevel - 1];
    }

    public static boolean canEnchant(ItemStack itemStack) {
        return getPossibleEnchantmentsForItem(itemStack).size() > 0;
    }

    public static List<Enchantment> getPossibleEnchantmentsForItem(ItemStack itemStack) {
        List<Enchantment> possibleEnchantments = new ArrayList<Enchantment>();

        Item item = itemStack.getItem();
        boolean isBook = item == Items.BOOK || item == Items.ENCHANTED_BOOK;

        for (Enchantment enchantment : EnchantmentProgressSteps.steps.keySet()) {
            if ((isBook && enchantment.isAllowedOnBooks()) || enchantment.canEnchant(itemStack)) {
                possibleEnchantments.add(enchantment);
            }
        }

        for (Enchantment enchantment : EnchantmentProgressSteps.bonusEnchantments) {
            if ((isBook && enchantment.isAllowedOnBooks()) || enchantment.canEnchant(itemStack)) {
                possibleEnchantments.add(enchantment);
            }
        }

        return possibleEnchantments;
    }

    public static Map<Enchantment, Boolean> getBonusEnchantmentsAvailability(Player player) {
        Map<Enchantment, Integer> playerData = EnchantmentProgressManager.get(player.getServer()).getPlayerData(player);
        Map<Enchantment, Boolean> bonusEnchantmentsAvailability = new HashMap<Enchantment, Boolean>();

        for (Enchantment enchantment : EnchantmentProgressSteps.bonusEnchantments) {
            bonusEnchantmentsAvailability.put(enchantment,
                    EnchantmentUtils.bonusEnchantmentsConditions.get(enchantment).apply(playerData));
        }

        return bonusEnchantmentsAvailability;
    }

}
