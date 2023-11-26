package com.ledouxm.progressiveenchantments;

import java.util.List;
import java.util.Optional;

import com.ledouxm.progressiveenchantments.ItemUtils.CaughtTypes;
import com.ledouxm.progressiveenchantments.init.ModBlockEntities;
import com.ledouxm.progressiveenchantments.init.ModBlocks;
import com.ledouxm.progressiveenchantments.init.ModCreativeTabs;
import com.ledouxm.progressiveenchantments.init.ModItems;
import com.ledouxm.progressiveenchantments.init.ModMenus;
import com.ledouxm.progressiveenchantments.init.ModStats;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ProgressiveEnchantments.MODID)
@Mod.EventBusSubscriber(modid = ProgressiveEnchantments.MODID, bus = Bus.MOD)
public class ProgressiveEnchantments {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "progressiveenchantments";

    public ProgressiveEnchantments() {
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.register(ModStats.class);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        Player player = event.getEntity();
        if (player == null)
            return;

        if (ItemUtils.isTool(event.getOriginal())) {
            ModStats.CustomStats.UNBREAKING.addToPlayer(player, 1);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null)
            return;

        ModStats.CustomStats.EFFICIENCY.addToPlayer(player, 1);

        if (event.getState().is(net.minecraftforge.common.Tags.Blocks.ORES)) {
            ModStats.CustomStats.FORTUNE.addToPlayer(player, 1);
        }

        if (player.isUnderWater()) {
            ModStats.CustomStats.AQUA_AFFINITY.addToPlayer(player, 1);
        }

    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Player attacker = event.getEntity();
        if (attacker.isHolding(itemStack -> itemStack.getItem() instanceof SwordItem)) {
            ModStats.CustomStats.SWEEPING_EDGE.addToPlayer(attacker, 1);
        }
    }

    @SubscribeEvent
    public void onLoadPlayer(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            EnchantmentProgressManager.get(player.getServer()).getAllPlayerProgress(serverPlayer);
            EnchantmentProgressManager.get(player.getServer()).sendDataToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            ModStats.CustomStats.PROTECTION.addToPlayer(player, 1);
        }
    }

    public void onDamageGiven(Player player, LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.PLAYER_ATTACK)
                && player.isHolding(itemStack -> itemStack.getItem() instanceof SwordItem)) {
            ModStats.CustomStats.SHARPNESS.addToPlayer(player, 1);
        } else if (source.is(DamageTypes.ARROW)) {
            Arrow arrow = (Arrow) source.getDirectEntity();
            if (arrow.shotFromCrossbow()) {
                ModStats.CustomStats.QUICK_CHARGE.addToPlayer(player, 1);
            } else {
                ModStats.CustomStats.POWER.addToPlayer(player, 1);
                if (arrow.isOnFire()) {
                    ModStats.CustomStats.FLAME.addToPlayer(player, 1);
                }
            }
        } else if (source.is(DamageTypes.TRIDENT)) {
            ModStats.CustomStats.RIPTIDE.addToPlayer(player, 1);
        }

        // check if the player has taken damage from victim in the last 3 seconds
        if (player.getLastHurtByMobTimestamp() + 3000 < player.tickCount) {
            return;
        }

        if (victim.getLastHurtMob() == player) {
            ModStats.CustomStats.THORNS.addToPlayer(player, 1);
        }
    }

    public void onDamageTaken(Player player, LivingDamageEvent event) {
        DamageSource source = event.getSource();

        boolean isStillAlive = player.getHealth() > event.getAmount();
        if (!isStillAlive)
            return;

        if (source.is(DamageTypes.IN_FIRE)) {
            ModStats.CustomStats.FIRE_PROTECTION.addToPlayer(player, 1);
        } else if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            ModStats.CustomStats.BLAST_PROTECTION.addToPlayer(player, 1);
        } else if (source.is(DamageTypes.MOB_PROJECTILE) || source.is(DamageTypes.ARROW)) {
            ModStats.CustomStats.PROJECTILE_PROTECTION.addToPlayer(player, 1);
        } else if (source.is(DamageTypes.FALL)) {
            ModStats.CustomStats.FEATHER_FALLING.addToPlayer(player, 1);
        }
    }

    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();

        int luck = 0;
        int speed = 0;

        for (ItemStack drop : event.getDrops()) {
            CaughtTypes caughtType = ItemUtils.getItemCaughtType(drop);

            if (caughtType == CaughtTypes.TREASURE) {
                luck++;
            }

            speed++;
        }

        if (luck > 0) {
            ModStats.CustomStats.LUCK_OF_THE_SEA.addToPlayer(player, luck);
        }

        if (speed > 0) {
            ModStats.CustomStats.LURE.addToPlayer(player, speed);
        }
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        // check if the damage is from a player
        if (event.getSource().getEntity() instanceof Player sourcePlayer) {
            onDamageGiven(sourcePlayer, event);
            return;
        }

        // neither the source nor the target is a player
        if (!(event.getEntity() instanceof Player)) {
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
                    ModStats.CustomStats.KNOCKBACK.addToPlayer(attacker, 1);
                } else if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
                    ModStats.CustomStats.FIRE_ASPECT.addToPlayer(attacker, 1);
                } else if (source.is(DamageTypes.TRIDENT)) {
                    ModStats.CustomStats.LOYALTY.addToPlayer(attacker, 1);
                } else if (source.is(DamageTypes.ARROW)) {
                    Arrow arrow = (Arrow) source.getDirectEntity();
                    if (arrow.shotFromCrossbow()) {
                        ModStats.CustomStats.PIERCING.addToPlayer(attacker, 1);
                    } else {
                        ModStats.CustomStats.PUNCH.addToPlayer(attacker, 1);
                    }

                }

                MobType mobType = entity.getMobType();
                if (mobType == MobType.ARTHROPOD) {
                    ModStats.CustomStats.BANE_OF_ARTHROPODS.addToPlayer(attacker, 1);
                } else if (mobType == MobType.UNDEAD) {
                    ModStats.CustomStats.SMITE.addToPlayer(attacker, 1);

                    if (entity instanceof Drowned) {
                        ModStats.CustomStats.IMPALING.addToPlayer(attacker, 1);
                    }
                }
                ModStats.CustomStats.LOOTING.addToPlayer(attacker, 1);
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
            if (player.isUnderWater()) {
                ModStats.CustomStats.RESPIRATION.addToPlayer(player, 1);

                if (player.isSwimming() && player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    ModStats.CustomStats.DEPTH_STRIDER.addToPlayer(player, 1);
                }
            } else if (player.isSprinting()) {
                BlockPos pos = player.blockPosition();
                BlockState blockBelow = player.level().getBlockState(pos.below());

                if (player.getFeetBlockState().is(Blocks.SOUL_SAND)) {
                    ModStats.CustomStats.SOUL_SPEED.addToPlayer(player, 1);
                } else if (blockBelow.is(Blocks.ICE)) {
                    ModStats.CustomStats.FROST_WALKER.addToPlayer(player, 1);
                }
            } else if (player.isCrouching()) {
                ModStats.CustomStats.SWIFT_SNEAK.addToPlayer(player, 1);
            }
        }

    }

}
