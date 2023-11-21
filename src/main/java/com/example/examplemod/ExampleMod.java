package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Bus.MOD)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "examplemod";

    private static final HashMap<Player, List<EntityWithTimestamp>> lastDamageTaken = new HashMap<>();
    private static final HashMap<Player, EntityWithTimestamp> lastDamageGiven = new HashMap<>();

    public ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(CustomEventHandler.class);

        FMLJavaModLoadingContext.get().getModEventBus().register(ModStats.class);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null)
            return;

        System.out.println("Unbreaking");
        System.out.println("Efficiency");

        if (event.getState().is(net.minecraftforge.common.Tags.Blocks.ORES)) {
            System.out.println("Fortune");
        }

        if (player.isInWater()) {
            System.out.println("Aqua affinity");
        }

    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Player attacker = event.getEntity();
        if (attacker.isHolding(itemStack -> itemStack.getItem() instanceof SwordItem)) {
            attacker.sendSystemMessage(Component.literal("Sweeping Edge : " + event.getDamageModifier()));
        }
    }

    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Player player) {
            System.out.println("Protection");
        }
    }

    public void onDamageGiven(Player player, LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.PLAYER_ATTACK)
                && player.isHolding(itemStack -> itemStack.getItem() instanceof SwordItem)) {
            player.sendSystemMessage(Component.literal("Sharpness"));
        } else if (source.is(DamageTypes.ARROW)) {
            Arrow arrow = (Arrow) source.getDirectEntity();
            if (arrow.isOnFire()) {
                player.sendSystemMessage(Component.literal("Flame"));
                ModStats.CustomStats.FLAME.addToPlayer(player, 1);
                // player.awardStat(ModStats.CustomStats.FLAME);
            }
        }

        // check if the player has dealt damage to victim in the last 3 seconds
        EntityWithTimestamp entityGiven = lastDamageGiven.get(player);
        long now = System.currentTimeMillis();
        if (entityGiven != null && entityGiven.entity == victim && now - entityGiven.timestamp < 3000) {
            player.sendSystemMessage(Component.literal("Multishot"));
            removeGivenDamage(player);
        }

        // check if the player has taken damage from victim in the last 3 seconds
        List<EntityWithTimestamp> entities = lastDamageTaken.get(player);
        if (entities != null) {
            for (EntityWithTimestamp entity : entities) {
                if (entity.entity == victim) {
                    player.sendSystemMessage(Component.literal("Thorns"));
                    removeDamage(player, victim);
                }
            }
        }

    }

    public void onDamageTaken(Player player, LivingDamageEvent event) {

        DamageSource source = event.getSource();

        System.out.println(source.toString());

        Entity sourceEntity = source.getEntity();
        if (sourceEntity != null && sourceEntity instanceof LivingEntity livingSourceEntity) {
            boolean isAllied = sourceEntity.isAlliedTo(event.getEntity());
            if (!isAllied) {
                cleanup(player);
                addDamage(player, livingSourceEntity);
            }
        }
        // Fire protection
        System.out.println(source.is(DamageTypes.IN_FIRE));
        // Blast protection
        System.out.println(source.is(DamageTypes.EXPLOSION));
        System.out.println(source.is(DamageTypes.PLAYER_EXPLOSION));
        // Projectile protection
        System.out.println(source.is(DamageTypes.MOB_PROJECTILE));

        // Feather falling
        if (event.getAmount() < player.getHealth()) {
            System.out.println(source.is(DamageTypes.FALL));
        }

        player.sendSystemMessage(Component.literal("Damage!"));
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        System.out.println(event.getItemStack());
    }

    // TridentItem aModel;
    // @SubscribeEvent
    // public void onArrowLoose(ArrowLooseEvent event) {
    // System.out.println(event.getCharge());
    // }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        // check if the damage is from a player
        if (event.getSource().getEntity() instanceof Player sourcePlayer) {
            onDamageGiven(sourcePlayer, event);
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            // neither the source nor the target is a player
            return;
        }
        onDamageTaken((Player) event.getEntity(), event);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();

        Entity sourceEntity = Optional.ofNullable(source.getEntity()).orElse(entity.getLastAttacker());

        if ((sourceEntity instanceof Player attacker)) {

            if (!attacker.isAlliedTo(entity)) {

                if (source.is(DamageTypes.FALL)) {
                    attacker.sendSystemMessage(Component.literal("Knockback"));
                } else if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
                    // attacker.awardStat(ModStats.FIRE_ASPECT);
                    ModStats.CustomStats.FIRE_ASPECT.addToPlayer(attacker, 1);
                    attacker.sendSystemMessage(Component.literal("Fire Aspect"));
                } else if (source.is(DamageTypes.TRIDENT)) {
                    attacker.sendSystemMessage(Component.literal("Loyalty"));
                } else if (source.is(DamageTypes.ARROW)) {
                    attacker.sendSystemMessage(Component.literal("Power"));
                }
            }

        }
    }

    private int serverTickCount = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        serverTickCount++;
        if (serverTickCount < 20)
            return;
        serverTickCount = 0;

        List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();

        for (ServerPlayer player : players) {
            ServerStatsCounter counter = player.getStats();
            // Swift sneak
            int distanceCrouched = counter.getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM));
            // Riptide
            int nbTridentsThrown = counter.getValue(Stats.ITEM_USED.get(Items.TRIDENT));
            // Quick charge
            int nbCrossbowUsed = counter.getValue(Stats.ITEM_USED.get(Items.CROSSBOW));
            // Infinity
            int nbBowUsed = counter.getValue(Stats.ITEM_USED.get(Items.BOW));

            int nbZombieKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOMBIE));
            int nbHusksKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.HUSK));
            int nbStraysKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.STRAY));
            // Impaling
            int nbDrownedKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.DROWNED));
            int nbZombieVillagerKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOMBIE_VILLAGER));
            int nbZombiePiglinKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOMBIFIED_PIGLIN));
            int nbSkeletonKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.SKELETON));
            int nbSkeletonHorseKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.SKELETON_HORSE));
            int nbPhantomKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.PHANTOM));
            int nbZoglinKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.ZOGLIN));
            // Smite
            int nbUndeadKilled = nbZombieKilled + nbHusksKilled + nbStraysKilled + nbDrownedKilled
                    + nbZombieVillagerKilled + nbZombiePiglinKilled + nbSkeletonKilled + nbSkeletonHorseKilled
                    + nbPhantomKilled + nbZoglinKilled;

            int nbSpiderKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.SPIDER));
            int nbCaveSpiderKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.CAVE_SPIDER));
            int nbSilverfishKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.SILVERFISH));
            int nbEndermiteKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.ENDERMITE));
            // Bane of the arthropods
            int nbArthropodKilled = nbSpiderKilled + nbCaveSpiderKilled + nbSilverfishKilled + nbEndermiteKilled;

            int nbGuardianKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.GUARDIAN));
            int nbElderGuardianKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.ELDER_GUARDIAN));

            int nbPillagerKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.PILLAGER));
            int nbVexKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.VEX));
            int nbVindicatorKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.VINDICATOR));
            int nbWitchKilled = counter.getValue(Stats.ENTITY_KILLED.get(EntityType.WITCH));
            int nbIllagerKilled = nbPillagerKilled + nbVexKilled + nbVindicatorKilled + nbWitchKilled;

            // for sharpness & looting
            int totalMobKilled = nbUndeadKilled + nbArthropodKilled + nbGuardianKilled + nbElderGuardianKilled
                    + nbIllagerKilled;

            // for luck of the sea
            int nbFished = counter.getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT));

            // for lure
            int nbFishingRodUser = counter.getValue(Stats.ITEM_USED.get(Items.FISHING_ROD));

            if (player.isInWater()) {
                player.sendSystemMessage(Component.literal("Respiration"));

                if (player.isSwimming() && player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    player.sendSystemMessage(Component.literal("Depth Strider"));
                }
            } else if (player.isSprinting()) {
                if (player.getFeetBlockState().is(Blocks.SOUL_SAND)) {
                    player.sendSystemMessage(Component.literal("Soul Speed"));
                } else if (player.getFeetBlockState().is(Blocks.ICE)) {
                    player.sendSystemMessage(Component.literal("Frost Walker"));
                }
            }
        }

    }

    public void cleanup(Player player) {
        if (!lastDamageTaken.containsKey(player)) {
            lastDamageTaken.put(player, new ArrayList<EntityWithTimestamp>());
            return;
        }

        // remove all entries older than 3 seconds
        long now = System.currentTimeMillis();
        List<EntityWithTimestamp> entities = lastDamageTaken.get(player);
        entities.removeIf((entity) -> now - entity.timestamp > 3000);
    }

    public void removeGivenDamage(Player player) {
        lastDamageGiven.remove(player);
    }

    public void addGivenDamage(Player player, LivingEntity entity) {
        lastDamageGiven.put(player, new EntityWithTimestamp(entity));
    }

    public void removeDamage(Player player, LivingEntity entity) {
        List<EntityWithTimestamp> entities = lastDamageTaken.get(player);
        entities.removeIf((e) -> e.entity == entity);
    }

    public void addDamage(Player player, LivingEntity entity) {
        List<EntityWithTimestamp> entities = lastDamageTaken.get(player);
        entities.removeIf((e) -> e.entity == entity);
        entities.add(new EntityWithTimestamp(entity));

    }
}