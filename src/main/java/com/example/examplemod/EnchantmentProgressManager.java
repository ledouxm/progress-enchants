package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.example.examplemod.block.PossibleEnchantment;
import com.example.examplemod.network.EnchantmentProgressChannel;
import com.example.examplemod.network.EnchantmentProgressClientboundPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;

public class EnchantmentProgressManager extends SavedData {
    public static final EnchantmentProgressManager clientCopy = new EnchantmentProgressManager();

    private final Map<UUID, Map<Enchantment, Integer>> data = new HashMap<>();
    private final Map<UUID, Map<Enchantment, Integer>> bonusClaimed = new HashMap<>();
    private final Map<UUID, List<EnchantmentProgressSnapshot>> snapshots = new HashMap<>();

    public Map<Enchantment, Integer> getPlayerData(Player player) {
        return data.get(player.getUUID());
    }

    public void setPlayerData(UUID player, Map<Enchantment, Integer> playerData) {
        data.put(player, playerData);
        this.setDirty();
    }

    public void setBonusClaimed(UUID player, Map<Enchantment, Integer> bonusClaimed) {
        this.bonusClaimed.put(player, bonusClaimed);
        this.setDirty();
    }

    public void setProgress(UUID player, List<EnchantmentProgressSnapshot> snapshots) {
        this.snapshots.put(player, snapshots);
        this.setDirty();
    }

    public EnchantmentProgressSnapshot getEnchantmentProgressSnapshotForPlayer(Player player, Enchantment enchantment,
            int level) {
        List<EnchantmentProgressSnapshot> snapshots = this.snapshots.get(player.getUUID());

        if (snapshots == null) {
            return null;
        }

        for (EnchantmentProgressSnapshot snapshot : snapshots) {
            if (snapshot.enchantment.equals(enchantment)) {
                return snapshot;
            }
        }

        return null;
    }

    public void sendDataToPlayer(ServerPlayer player) {
        Map<Enchantment, Integer> playerData = getPlayerData(player);

        if (playerData == null) {
            return;
        }

        this.getAllPlayerProgress(player);

        EnchantmentProgressClientboundPacket packet = new EnchantmentProgressClientboundPacket(player.getUUID(),
                playerData, bonusClaimed.get(player.getUUID()), snapshots.get(player.getUUID()));

        System.out.println("Sending data to player " + player.getDisplayName().getString() + ": " + playerData);
        EnchantmentProgressChannel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public List<EnchantmentProgressSnapshot> getAllPlayerProgress(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<EnchantmentProgressSnapshot> progress = new ArrayList<>();

            ServerStatsCounter stats = serverPlayer.getStats();
            for (Map.Entry<Enchantment, Function<ServerStatsCounter, Integer>> entry : EnchantmentUtils.customStats
                    .entrySet()) {
                progress.add(new EnchantmentProgressSnapshot(entry.getKey(), entry.getValue().apply(stats)));
            }

            this.snapshots.put(player.getUUID(), progress);

            return progress;
        } else {
            return this.snapshots.get(player.getUUID());
        }
    }

    public void checkPlayerProgress(Player player, Enchantment enchantment, int amount) {
        UUID uuid = player.getUUID();
        System.out.println("Checking player progress for " + player.getDisplayName().getString() + " for "
                + enchantment.getFullname(1) + " with amount " + amount);
        Map<Enchantment, Integer> playerData = data.get(uuid);

        System.out.println("playerData: " + playerData);
        if (playerData == null) {
            playerData = new HashMap<>();
            data.put(uuid, playerData);
            this.setDirty();
        }

        if (EnchantmentProgressSteps.isBonus(enchantment)) {
            checkPlayerBonusProgress(player, enchantment, playerData);
            return;
        }

        Integer level = playerData.get(enchantment);
        int newLevel = EnchantmentProgressSteps.getLevel(enchantment, amount);

        System.out.println("progress: " + level);
        System.out.println("newProgress: " + newLevel);

        if (newLevel > 0 && (level == null || newLevel > level)) {
            unlockEnchantment(enchantment, newLevel, playerData);
        }
    }

    public Status getEnchantStatusForPlayer(Player player, Enchantment enchantment, int level) {
        UUID uuid = player.getUUID();
        Map<Enchantment, Integer> playerData = this.data.get(uuid);
        if (playerData == null) {
            return Status.LOCKED;
        }
        int currentLevel = playerData.getOrDefault(enchantment, 0);
        if (currentLevel == 0 || currentLevel < level) {
            return Status.LOCKED;
        }

        Map<Enchantment, Integer> playerBonusClaimed = this.bonusClaimed.get(uuid);

        if (playerBonusClaimed == null) {
            playerBonusClaimed = new HashMap<>();
            this.bonusClaimed.put(uuid, playerBonusClaimed);
            return Status.FREE;
        }

        int alreadyClaimedLevel = playerBonusClaimed.getOrDefault(enchantment, 0);
        if (level <= alreadyClaimedLevel) {
            return Status.UNLOCKED;
        }

        return Status.FREE;
    }

    public boolean isFree(Player player, Enchantment enchantment, int level) {
        return getEnchantStatusForPlayer(player, enchantment, level) == Status.FREE;
    }

    public void claimBonus(Player player, Enchantment enchantment, int level) {
        if (!isFree(player, enchantment, level))
            return;

        UUID uuid = player.getUUID();
        Map<Enchantment, Integer> playerBonusClaimed = this.bonusClaimed.get(uuid);

        if (playerBonusClaimed == null) {
            playerBonusClaimed = new HashMap<>();
            this.bonusClaimed.put(uuid, playerBonusClaimed);
        }

        playerBonusClaimed.put(enchantment, level);

        this.setDirty();
    }

    public void unlockEnchantment(Enchantment enchantment, int level, Player player) {
        Map<Enchantment, Integer> playerData = data.get(player.getUUID());
        if (playerData == null) {
            playerData = new HashMap<>();
            data.put(player.getUUID(), playerData);
        }

        this.unlockEnchantment(enchantment, level, playerData);
    }

    public void unlockEnchantment(Enchantment enchantment, int level, Map<Enchantment, Integer> playerData) {
        System.out.println("Unlocking enchantment " + enchantment.getFullname(1) + " with level " + level);
        playerData.put(enchantment, level);
        this.setDirty();
    }

    public void checkPlayerBonusProgress(Player player, Enchantment enchantment, Map<Enchantment, Integer> playerData) {
        if (enchantment.equals(Enchantments.MENDING)) {
            if (isMaxLevelInEveryEnchant(playerData, Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.SHARPNESS,
                    Enchantments.BLOCK_EFFICIENCY, Enchantments.POWER_ARROWS, Enchantments.FISHING_LUCK,
                    Enchantments.FISHING_SPEED)) {
                unlockEnchantment(enchantment, 1, playerData);
            }

        } else if (enchantment.equals(Enchantments.SILK_TOUCH)) {
            if (isMaxLevelInEveryEnchant(playerData, Enchantments.BLOCK_FORTUNE, Enchantments.BLOCK_EFFICIENCY)) {
                unlockEnchantment(enchantment, 1, playerData);
            }
        } else if (enchantment.equals(Enchantments.CHANNELING)) {
            if (isMaxLevelInEveryEnchant(playerData, Enchantments.LOYALTY, Enchantments.RIPTIDE)) {
                unlockEnchantment(enchantment, 1, playerData);
            }
        } else if (enchantment.equals(Enchantments.INFINITY_ARROWS)) {
            if (isMaxLevelInEveryEnchant(playerData, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS,
                    Enchantments.FLAMING_ARROWS)) {
                unlockEnchantment(enchantment, 1, playerData);
            }
        } else if (enchantment.equals(Enchantments.MULTISHOT)) {
            if (isMaxLevelInEveryEnchant(playerData, Enchantments.PIERCING, Enchantments.QUICK_CHARGE)) {
                unlockEnchantment(enchantment, 1, playerData);
            }
        }
    }

    public List<PossibleEnchantment> filterEnchantmentsListForPlayer(List<Enchantment> enchantments, Player player) {
        Map<Enchantment, Integer> playerData = getPlayerData(player);
        System.out.println("playerData: " + playerData);

        List<PossibleEnchantment> filteredEnchantments = new ArrayList<>();

        for (Enchantment enchantment : enchantments) {
            filteredEnchantments.add(new PossibleEnchantment(enchantment,
                    playerData == null ? 0 : playerData.getOrDefault(enchantment, 0), player)); // (enchantment,
                                                                                                // playerData.get(enchantment));
        }

        return filteredEnchantments;
    }

    public Integer getLevel(Enchantment enchantment, Map<Enchantment, Integer> playerData) {
        Integer progress = playerData.get(enchantment);

        if (progress == null) {
            return 0;
        }

        int[] steps = EnchantmentProgressSteps.steps.get(enchantment);

        if (steps == null) {
            return 0;
        }

        for (int i = 0; i < steps.length; i++) {
            if (progress < steps[i]) {
                return i;
            }
        }

        return steps.length;
    }

    public boolean isMaxLevel(Enchantment enchantment, Map<Enchantment, Integer> playerData) {
        Integer progress = getLevel(enchantment, playerData);
        int[] steps = EnchantmentProgressSteps.steps.get(enchantment);

        if (steps == null) {
            return false;
        }

        return progress >= steps[steps.length - 1];
    }

    public boolean isMaxLevelInEveryEnchant(Map<Enchantment, Integer> playerData, Enchantment... enchantments) {
        for (Enchantment enchantment : enchantments) {
            if (!isMaxLevel(enchantment, playerData)) {
                return false;
            }
        }

        return true;
    }

    public ListTag getMapNBT(Map<UUID, Map<Enchantment, Integer>> data) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Map<Enchantment, Integer>> entry : data.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("player", entry.getKey());
            ListTag enchantments = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantmentEntry : entry.getValue().entrySet()) {
                CompoundTag enchantmentData = new CompoundTag();
                enchantmentData.putString("enchantment", EnchantmentUtils.getEnchantmentId(enchantmentEntry.getKey()));
                enchantmentData.putInt("progress", enchantmentEntry.getValue());
                enchantments.add(enchantmentData);
            }
            tag.put("enchantments", enchantments);
            list.add(tag);
        }

        return list;
    }

    public Map<UUID, Map<Enchantment, Integer>> getMapFromNBT(ListTag list) {
        Map<UUID, Map<Enchantment, Integer>> data = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            UUID player = tag.getUUID("player");
            ListTag enchantments = tag.getList("enchantments", 10);
            Map<Enchantment, Integer> playerData = new HashMap<>();
            for (int j = 0; j < enchantments.size(); j++) {
                CompoundTag enchantmentData = enchantments.getCompound(j);
                Enchantment enchantment = EnchantmentUtils
                        .getEnchantmentById(enchantmentData.getString("enchantment"));
                int progress = enchantmentData.getInt("progress");
                playerData.put(enchantment, progress);
            }
            data.put(player, playerData);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("data", getMapNBT(data));
        tag.put("bonusClaimed", getMapNBT(bonusClaimed));

        return tag;
    }

    public static EnchantmentProgressManager read(CompoundTag tag) {
        EnchantmentProgressManager manager = new EnchantmentProgressManager();

        manager.data.putAll(manager.getMapFromNBT(tag.getList("data", 10)));
        manager.bonusClaimed.putAll(manager.getMapFromNBT(tag.getList("bonusClaimed", 10)));

        return manager;
    }

    public static EnchantmentProgressManager get(@Nullable MinecraftServer server) {
        if (server != null) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);

            return Objects.requireNonNull(overworld).getDataStorage().computeIfAbsent(EnchantmentProgressManager::read,
                    EnchantmentProgressManager::new,
                    "enchantment_progress");
        }

        return clientCopy;
    }

    public enum Status {
        FREE, UNLOCKED, LOCKED
    }
}
