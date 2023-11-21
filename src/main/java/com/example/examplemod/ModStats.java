package com.example.examplemod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModStats {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (CustomStats stat : CustomStats.values()) {
                ResourceLocation it = stat.getRegistryName();
                Registry.register(BuiltInRegistries.CUSTOM_STAT, it.getPath(), it);
                Stats.CUSTOM.get(it, StatFormatter.DEFAULT);
            }
        });
    }

    public enum CustomStats {
        UNBREAKING("unbreaking"),
        MENDING("mending"),
        PROTECTION("protection"),
        FIRE_PROTECTION("fire_protection"),
        BLAST_PROTECTION("blast_protection"),
        PROJECTILE_PROTECTION("projectile_protection"),
        THORNS("thorns"),
        RESPIRATION("respiration"),
        AQUA_AFFINITY("aqua_affinity"),
        FEATHER_FALLING("feather_falling"),
        DEPTH_STRIDER("depth_strider"),
        FROST_WALKER("frost_walker"),
        SOUL_SPEED("soul_speed"),
        // SWIFT_SNEAK("swift_sneak"),
        SHARPNESS("sharpness"),
        // SMITE("smite"),
        // BANE_OF_ARTHROPODS("bane_of_arthropods"),
        KNOCKBACK("knockback"),
        FIRE_ASPECT("fire_aspect"),
        // LOOTING("looting"),
        SWEEPING_EDGE("sweeping_edge"),
        EFFICIENCY("efficiency"),
        SILK_TOUCH("silk_touch"),
        FORTUNE("fortune"),
        CHANNELING("channeling"),
        LOYALTY("loyalty"),
        // RIPTIDE("riptide"),
        // IMPALING("impaling"),
        POWER("power"),
        PUNCH("punch"),
        MULTISHOT("multishot"),
        PIERCING("piercing"),
        // QUICK_CHARGE("quick_charge"),
        FLAME("flame");
        // INFINITY("infinity"),
        // LUCK_OF_THE_SEA("luck_of_the_sea"),
        // LURE("lure");

        private final ResourceLocation registryName;

        CustomStats(String id) {
            this.registryName = new ResourceLocation(ExampleMod.MODID, id);
        }

        public void addToPlayer(Player player, int amount) {
            player.awardStat(this.registryName, amount);
        }

        public ResourceLocation getRegistryName() {
            return registryName;
        }
    }
}
