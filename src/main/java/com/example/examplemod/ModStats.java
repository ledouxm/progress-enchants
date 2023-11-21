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
        AQUA_AFFINITY("aqua_affinity"), UNBREAKING("unbreaking"),
        EFFICIENCY("efficiency"), FORTUNE("fortune"),
        SWEEPING_EDGE("sweeping_edge"), PROTECTION("protection"),
        MULTISHOT("multishot"), THORNS("thorns"),
        FLAME("flame"), KNOCKBACK("knockback"), FIRE_ASPECT("fire_aspect"),
        LOYALTY("loyalty"), POWER("power"),
        RESPIRATION("respiration"), DEPTH_STRIDER("depth_strider"),
        SOUL_SPEED("soul_speed"),
        FROST_WALKER("frost_walker");

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
