package com.ledouxm.progressiveenchantments;

import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;

public class CustomStatsUtils {
    static int getNbUndeadKilled(ServerStatsCounter stats) {

        int drownedKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.DROWNED));
        int zombieKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOMBIE));
        int huskKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.HUSK));
        int strayKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.STRAY));
        int zombieVillagerKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOMBIE_VILLAGER));
        int zombiePiglinKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOMBIFIED_PIGLIN));
        int skeletonKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.SKELETON));
        int witherSkeletonKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.WITHER_SKELETON));
        int skeletonHorseKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.SKELETON_HORSE));
        int phantomKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.PHANTOM));
        int zoglinKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOGLIN));

        return drownedKilled + zombieKilled + huskKilled + strayKilled + zombieVillagerKilled + zombiePiglinKilled
                + skeletonKilled + witherSkeletonKilled + skeletonHorseKilled + phantomKilled + zoglinKilled;
    }

    static int getNbArthropodKilled(ServerStatsCounter stats) {
        int spiderKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.SPIDER));
        int caveSpiderKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.CAVE_SPIDER));
        int silverfishKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.SILVERFISH));
        int endermiteKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ENDERMITE));
        int beeKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.BEE));
        return spiderKilled + caveSpiderKilled + silverfishKilled + endermiteKilled + beeKilled;
    }

    static int getNbMonsterKilled(ServerStatsCounter stats) {
        int arthropodKilled = getNbArthropodKilled(stats);
        int undeadKilled = getNbUndeadKilled(stats);
        int endermanKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ENDERMAN));
        int enderDragonKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ENDER_DRAGON));
        int witherKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.WITHER));
        int blazeKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.BLAZE));
        int ghastKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.GHAST));
        int magmaCubeKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.MAGMA_CUBE));
        int slimeKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.SLIME));
        int vexKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.VEX));
        int vindicatorKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.VINDICATOR));
        int evokerKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.EVOKER));
        int pillagerKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.PILLAGER));
        int ravagerKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.RAVAGER));
        int witchKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.WITCH));
        int guardianKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.GUARDIAN));
        int elderGuardianKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.ELDER_GUARDIAN));
        int shulkerKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.SHULKER));
        int hoglinKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.HOGLIN));
        int piglinBruteKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.PIGLIN_BRUTE));
        int enderCrystalKilled = stats.getValue(Stats.ENTITY_KILLED.get(EntityType.END_CRYSTAL));

        return arthropodKilled + undeadKilled + endermanKilled + enderDragonKilled + witherKilled + blazeKilled
                + ghastKilled + magmaCubeKilled + slimeKilled + vexKilled + vindicatorKilled + evokerKilled
                + pillagerKilled + ravagerKilled + witchKilled + guardianKilled + elderGuardianKilled + shulkerKilled
                + hoglinKilled + piglinBruteKilled + enderCrystalKilled;
    }
}
