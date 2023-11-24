package com.example.examplemod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.example.examplemod.init.ModStats.CustomStats;

import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class EnchantmentUtils {

    public static Map<Enchantment, List<Enchantment>> bonusEnchantmentsRequirements = new HashMap<>();
    static {
        bonusEnchantmentsRequirements.put(Enchantments.MENDING, List.of(Enchantments.ALL_DAMAGE_PROTECTION,
                Enchantments.SHARPNESS, Enchantments.BLOCK_EFFICIENCY, Enchantments.POWER_ARROWS,
                Enchantments.FISHING_LUCK));
        bonusEnchantmentsRequirements.put(Enchantments.SILK_TOUCH,
                List.of(Enchantments.BLOCK_FORTUNE, Enchantments.BLOCK_EFFICIENCY));
        bonusEnchantmentsRequirements.put(Enchantments.CHANNELING,
                List.of(Enchantments.LOYALTY, Enchantments.RIPTIDE));
        bonusEnchantmentsRequirements.put(Enchantments.MULTISHOT,
                List.of(Enchantments.PIERCING, Enchantments.QUICK_CHARGE));
        bonusEnchantmentsRequirements.put(Enchantments.INFINITY_ARROWS,
                List.of(Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.FLAMING_ARROWS));
    }

    public static List<Enchantment> getRequirementsForBonusEnchantment(Enchantment enchantment) {
        return bonusEnchantmentsRequirements.get(enchantment);
    }

    public static Map<Enchantment, Function<Map<Enchantment, Integer>, Boolean>> bonusEnchantmentsConditions = new HashMap<>();
    static {
        for (Enchantment enchantment : bonusEnchantmentsRequirements.keySet()) {
            bonusEnchantmentsConditions.put(enchantment,
                    enchantments -> containsAllKeys(enchantments, bonusEnchantmentsRequirements.get(enchantment)));
        }
    }

    public static boolean containsAllKeys(Map<Enchantment, Integer> enchantments, List<Enchantment> keys) {
        for (Enchantment key : keys) {
            if (!enchantments.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public static Map<Enchantment, Function<ServerStatsCounter, Integer>> enchantmentRequirements = new HashMap<>();
    static {
        enchantmentRequirements.put(Enchantments.UNBREAKING, stats -> CustomStats.UNBREAKING.getAmount(stats));
        enchantmentRequirements.put(Enchantments.ALL_DAMAGE_PROTECTION,
                stats -> CustomStats.PROTECTION.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FIRE_PROTECTION,
                stats -> CustomStats.FIRE_PROTECTION.getAmount(stats));
        enchantmentRequirements.put(Enchantments.BLAST_PROTECTION,
                stats -> CustomStats.BLAST_PROTECTION.getAmount(stats));
        enchantmentRequirements.put(Enchantments.PROJECTILE_PROTECTION,
                stats -> CustomStats.PROJECTILE_PROTECTION.getAmount(stats));
        enchantmentRequirements.put(Enchantments.THORNS, stats -> CustomStats.THORNS.getAmount(stats));
        enchantmentRequirements.put(Enchantments.RESPIRATION, stats -> CustomStats.RESPIRATION.getAmount(stats));
        enchantmentRequirements.put(Enchantments.AQUA_AFFINITY, stats -> CustomStats.AQUA_AFFINITY.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FALL_PROTECTION,
                stats -> CustomStats.FEATHER_FALLING.getAmount(stats));
        enchantmentRequirements.put(Enchantments.DEPTH_STRIDER, stats -> CustomStats.DEPTH_STRIDER.getAmount(stats));
        enchantmentRequirements.put(Enchantments.SOUL_SPEED, stats -> CustomStats.SOUL_SPEED.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FROST_WALKER, stats -> CustomStats.FROST_WALKER.getAmount(stats));

        enchantmentRequirements.put(Enchantments.SHARPNESS, stats -> CustomStats.SHARPNESS.getAmount(stats));
        enchantmentRequirements.put(Enchantments.KNOCKBACK, stats -> CustomStats.KNOCKBACK.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FIRE_ASPECT, stats -> CustomStats.FIRE_ASPECT.getAmount(stats));
        enchantmentRequirements.put(Enchantments.SWEEPING_EDGE, stats -> CustomStats.SWEEPING_EDGE.getAmount(stats));
        enchantmentRequirements.put(Enchantments.BLOCK_EFFICIENCY, stats -> CustomStats.EFFICIENCY.getAmount(stats));
        enchantmentRequirements.put(Enchantments.BLOCK_FORTUNE, stats -> CustomStats.FORTUNE.getAmount(stats));
        enchantmentRequirements.put(Enchantments.LOYALTY, stats -> CustomStats.LOYALTY.getAmount(stats));
        enchantmentRequirements.put(Enchantments.PUNCH_ARROWS, stats -> CustomStats.PUNCH.getAmount(stats));
        enchantmentRequirements.put(Enchantments.PIERCING, stats -> CustomStats.PIERCING.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FLAMING_ARROWS, stats -> CustomStats.FLAME.getAmount(stats));
        enchantmentRequirements.put(Enchantments.SWIFT_SNEAK, stats -> CustomStats.SWIFT_SNEAK.getAmount(stats));
        enchantmentRequirements.put(Enchantments.RIPTIDE, stats -> stats.getValue(Stats.ITEM_USED.get(Items.TRIDENT)));
        enchantmentRequirements.put(Enchantments.QUICK_CHARGE,
                stats -> stats.getValue(Stats.ITEM_USED.get(Items.CROSSBOW)));
        enchantmentRequirements.put(Enchantments.POWER_ARROWS, stats -> stats.getValue(Stats.ITEM_USED.get(Items.BOW)));
        enchantmentRequirements.put(Enchantments.IMPALING,
                stats -> CustomStats.IMPALING.getAmount(stats));
        enchantmentRequirements.put(Enchantments.SMITE, stats -> CustomStats.SMITE.getAmount(stats));
        enchantmentRequirements.put(Enchantments.BANE_OF_ARTHROPODS,
                stats -> CustomStats.BANE_OF_ARTHROPODS.getAmount(stats));
        enchantmentRequirements.put(Enchantments.MOB_LOOTING, stats -> CustomStats.LOOTING.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FISHING_LUCK,
                stats -> CustomStats.LUCK_OF_THE_SEA.getAmount(stats));
        enchantmentRequirements.put(Enchantments.FISHING_SPEED,
                stats -> CustomStats.LURE.getAmount(stats));
    }

    private static Map<String, Enchantment> enchantments = new HashMap<>();
    static {
        enchantments.put("ALL_DAMAGE_PROTECTION", Enchantments.ALL_DAMAGE_PROTECTION);
        enchantments.put("FIRE_PROTECTION", Enchantments.FIRE_PROTECTION);
        enchantments.put("FALL_PROTECTION", Enchantments.FALL_PROTECTION);
        enchantments.put("BLAST_PROTECTION", Enchantments.BLAST_PROTECTION);
        enchantments.put("PROJECTILE_PROTECTION", Enchantments.PROJECTILE_PROTECTION);
        enchantments.put("RESPIRATION", Enchantments.RESPIRATION);
        enchantments.put("AQUA_AFFINITY", Enchantments.AQUA_AFFINITY);
        enchantments.put("THORNS", Enchantments.THORNS);
        enchantments.put("DEPTH_STRIDER", Enchantments.DEPTH_STRIDER);
        enchantments.put("FROST_WALKER", Enchantments.FROST_WALKER);
        enchantments.put("BINDING_CURSE", Enchantments.BINDING_CURSE);
        enchantments.put("SOUL_SPEED", Enchantments.SOUL_SPEED);
        enchantments.put("SWIFT_SNEAK", Enchantments.SWIFT_SNEAK);
        enchantments.put("SHARPNESS", Enchantments.SHARPNESS);
        enchantments.put("SMITE", Enchantments.SMITE);
        enchantments.put("BANE_OF_ARTHROPODS", Enchantments.BANE_OF_ARTHROPODS);
        enchantments.put("KNOCKBACK", Enchantments.KNOCKBACK);
        enchantments.put("FIRE_ASPECT", Enchantments.FIRE_ASPECT);
        enchantments.put("MOB_LOOTING", Enchantments.MOB_LOOTING);
        enchantments.put("SWEEPING_EDGE", Enchantments.SWEEPING_EDGE);
        enchantments.put("BLOCK_EFFICIENCY", Enchantments.BLOCK_EFFICIENCY);
        enchantments.put("SILK_TOUCH", Enchantments.SILK_TOUCH);
        enchantments.put("UNBREAKING", Enchantments.UNBREAKING);
        enchantments.put("BLOCK_FORTUNE", Enchantments.BLOCK_FORTUNE);
        enchantments.put("POWER_ARROWS", Enchantments.POWER_ARROWS);
        enchantments.put("PUNCH_ARROWS", Enchantments.PUNCH_ARROWS);
        enchantments.put("FLAMING_ARROWS", Enchantments.FLAMING_ARROWS);
        enchantments.put("INFINITY_ARROWS", Enchantments.INFINITY_ARROWS);
        enchantments.put("FISHING_LUCK", Enchantments.FISHING_LUCK);
        enchantments.put("FISHING_SPEED", Enchantments.FISHING_SPEED);
        enchantments.put("LOYALTY", Enchantments.LOYALTY);
        enchantments.put("IMPALING", Enchantments.IMPALING);
        enchantments.put("RIPTIDE", Enchantments.RIPTIDE);
        enchantments.put("CHANNELING", Enchantments.CHANNELING);
        enchantments.put("MULTISHOT", Enchantments.MULTISHOT);
        enchantments.put("QUICK_CHARGE", Enchantments.QUICK_CHARGE);
        enchantments.put("PIERCING", Enchantments.PIERCING);
        enchantments.put("MENDING", Enchantments.MENDING);
    }

    public static Enchantment getEnchantmentById(String id) {
        return enchantments.get(id);
    }

    public static String getEnchantmentId(Enchantment enchantment) {
        for (Map.Entry<String, Enchantment> entry : enchantments.entrySet()) {
            if (entry.getValue().equals(enchantment)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
