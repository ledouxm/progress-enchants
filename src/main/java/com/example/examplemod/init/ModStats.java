package com.example.examplemod.init;

import java.util.List;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.ExampleMod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeConfig.Server;
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
        UNBREAKING("unbreaking", Enchantments.UNBREAKING),
        // MENDING("mending"),
        PROTECTION("protection", Enchantments.ALL_DAMAGE_PROTECTION),
        FIRE_PROTECTION("fire_protection", Enchantments.FIRE_PROTECTION),
        BLAST_PROTECTION("blast_protection", Enchantments.BLAST_PROTECTION),
        PROJECTILE_PROTECTION("projectile_protection", Enchantments.PROJECTILE_PROTECTION),
        THORNS("thorns", Enchantments.THORNS),
        RESPIRATION("respiration", Enchantments.RESPIRATION),
        AQUA_AFFINITY("aqua_affinity", Enchantments.AQUA_AFFINITY),
        FEATHER_FALLING("feather_falling", Enchantments.FALL_PROTECTION),
        DEPTH_STRIDER("depth_strider", Enchantments.DEPTH_STRIDER),
        FROST_WALKER("frost_walker", Enchantments.FROST_WALKER),
        SOUL_SPEED("soul_speed", Enchantments.SOUL_SPEED),
        SWIFT_SNEAK("swift_sneak", Enchantments.SWIFT_SNEAK),
        SHARPNESS("sharpness", Enchantments.SHARPNESS),
        SMITE("smite", Enchantments.SMITE),
        BANE_OF_ARTHROPODS("bane_of_arthropods", Enchantments.BANE_OF_ARTHROPODS),
        KNOCKBACK("knockback", Enchantments.KNOCKBACK),
        FIRE_ASPECT("fire_aspect", Enchantments.FIRE_ASPECT),
        LOOTING("looting", Enchantments.MOB_LOOTING),
        SWEEPING_EDGE("sweeping_edge", Enchantments.SWEEPING_EDGE),
        EFFICIENCY("efficiency", Enchantments.BLOCK_EFFICIENCY),
        // SILK_TOUCH("silk_touch"),
        FORTUNE("fortune", Enchantments.BLOCK_FORTUNE),
        // CHANNELING("channeling"),
        LOYALTY("loyalty", Enchantments.LOYALTY),
        RIPTIDE("riptide", Enchantments.RIPTIDE),
        IMPALING("impaling", Enchantments.IMPALING),
        POWER("power", Enchantments.POWER_ARROWS),
        PUNCH("punch", Enchantments.PUNCH_ARROWS),
        // MULTISHOT("multishot"),
        PIERCING("piercing", Enchantments.PIERCING),
        QUICK_CHARGE("quick_charge", Enchantments.QUICK_CHARGE),
        FLAME("flame", Enchantments.FLAMING_ARROWS),
        // INFINITY("infinity"),
        LUCK_OF_THE_SEA("luck_of_the_sea", Enchantments.FISHING_LUCK),
        LURE("lure", Enchantments.FISHING_SPEED);

        private final ResourceLocation registryName;
        private final Enchantment enchantment;

        CustomStats(String id, Enchantment enchantment) {
            this.registryName = new ResourceLocation(ExampleMod.MODID, id);
            this.enchantment = enchantment;
        }

        public void addToPlayer(Player player, int score) {
            player.awardStat(this.registryName, score);

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            EnchantmentProgressManager.get(player.getServer()).checkPlayerProgress(player, this.enchantment);
        }

        public int getAmount(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return 0;
            }
            return serverPlayer.getStats().getValue(Stats.CUSTOM.get(this.registryName));
        }

        public int getAmount(ServerStatsCounter stats) {
            return stats.getValue(Stats.CUSTOM.get(this.registryName));
        }

        public ResourceLocation getRegistryName() {
            return registryName;
        }
    }
}
