package com.example.examplemod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.example.examplemod.init.ModStats.CustomStats;

import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
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

    public static Map<Enchantment, Function<ServerStatsCounter, Integer>> customStats = new HashMap<>();
    static {
        customStats.put(Enchantments.UNBREAKING, stats -> CustomStats.UNBREAKING.getAmount(stats));
        customStats.put(Enchantments.ALL_DAMAGE_PROTECTION, stats -> CustomStats.PROTECTION.getAmount(stats));
        customStats.put(Enchantments.FIRE_PROTECTION, stats -> CustomStats.FIRE_PROTECTION.getAmount(stats));
        customStats.put(Enchantments.BLAST_PROTECTION, stats -> CustomStats.BLAST_PROTECTION.getAmount(stats));
        customStats.put(Enchantments.PROJECTILE_PROTECTION,
                stats -> CustomStats.PROJECTILE_PROTECTION.getAmount(stats));
        customStats.put(Enchantments.THORNS, stats -> CustomStats.THORNS.getAmount(stats));
        customStats.put(Enchantments.RESPIRATION, stats -> CustomStats.RESPIRATION.getAmount(stats));
        customStats.put(Enchantments.AQUA_AFFINITY, stats -> CustomStats.AQUA_AFFINITY.getAmount(stats));
        customStats.put(Enchantments.FALL_PROTECTION, stats -> CustomStats.FEATHER_FALLING.getAmount(stats));
        customStats.put(Enchantments.DEPTH_STRIDER, stats -> CustomStats.DEPTH_STRIDER.getAmount(stats));
        customStats.put(Enchantments.SOUL_SPEED, stats -> CustomStats.SOUL_SPEED.getAmount(stats));
        customStats.put(Enchantments.SHARPNESS, stats -> CustomStats.SHARPNESS.getAmount(stats));
        customStats.put(Enchantments.KNOCKBACK, stats -> CustomStats.KNOCKBACK.getAmount(stats));
        customStats.put(Enchantments.FIRE_ASPECT, stats -> CustomStats.FIRE_ASPECT.getAmount(stats));
        customStats.put(Enchantments.SWEEPING_EDGE, stats -> CustomStats.SWEEPING_EDGE.getAmount(stats));
        customStats.put(Enchantments.BLOCK_EFFICIENCY, stats -> CustomStats.EFFICIENCY.getAmount(stats));
        customStats.put(Enchantments.BLOCK_FORTUNE, stats -> CustomStats.FORTUNE.getAmount(stats));
        customStats.put(Enchantments.LOYALTY, stats -> CustomStats.LOYALTY.getAmount(stats));
        customStats.put(Enchantments.PUNCH_ARROWS, stats -> CustomStats.PUNCH.getAmount(stats));
        customStats.put(Enchantments.PIERCING, stats -> CustomStats.PIERCING.getAmount(stats));
        customStats.put(Enchantments.FLAMING_ARROWS, stats -> CustomStats.FLAME.getAmount(stats));
        customStats.put(Enchantments.SWIFT_SNEAK, stats -> stats.getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM)));
        customStats.put(Enchantments.RIPTIDE, stats -> stats.getValue(Stats.ITEM_USED.get(Items.TRIDENT)));
        customStats.put(Enchantments.QUICK_CHARGE, stats -> stats.getValue(Stats.ITEM_USED.get(Items.CROSSBOW)));
        customStats.put(Enchantments.POWER_ARROWS, stats -> stats.getValue(Stats.ITEM_USED.get(Items.BOW)));
        customStats.put(Enchantments.IMPALING, stats -> stats.getValue(Stats.ENTITY_KILLED.get(EntityType.DROWNED)));
        customStats.put(Enchantments.SMITE, stats -> CustomStatsUtils.getNbUndeadKilled(stats));
        customStats.put(Enchantments.BANE_OF_ARTHROPODS, stats -> CustomStatsUtils.getNbArthropodKilled(stats));
        customStats.put(Enchantments.MOB_LOOTING, stats -> CustomStatsUtils.getNbMonsterKilled(stats));
        customStats.put(Enchantments.SHARPNESS, stats -> {
            int cptSwordHits = stats.getValue(Stats.ITEM_USED.get(Items.IRON_SWORD));
            cptSwordHits += stats.getValue(Stats.ITEM_USED.get(Items.DIAMOND_SWORD));
            cptSwordHits += stats.getValue(Stats.ITEM_USED.get(Items.GOLDEN_SWORD));
            cptSwordHits += stats.getValue(Stats.ITEM_USED.get(Items.STONE_SWORD));
            cptSwordHits += stats.getValue(Stats.ITEM_USED.get(Items.WOODEN_SWORD));
            cptSwordHits += stats.getValue(Stats.ITEM_USED.get(Items.NETHERITE_SWORD));

            return cptSwordHits;
        });
        customStats.put(Enchantments.FISHING_LUCK, stats -> stats.getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT)));
        customStats.put(Enchantments.FISHING_SPEED, stats -> stats.getValue(Stats.ITEM_USED.get(Items.FISHING_ROD)));
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
