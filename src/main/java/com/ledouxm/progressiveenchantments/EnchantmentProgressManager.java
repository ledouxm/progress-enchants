package com.ledouxm.progressiveenchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.ledouxm.progressiveenchantments.block.PossibleEnchantment;
import com.ledouxm.progressiveenchantments.network.AtomicScoreClientboundPacket;
import com.ledouxm.progressiveenchantments.network.EnchantmentManagerClientboundPacket;
import com.ledouxm.progressiveenchantments.network.EnchantmentNotificationClientboundPacket;
import com.ledouxm.progressiveenchantments.network.EnchantmentProgressChannel;
import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;

public class EnchantmentProgressManager extends SavedData {
    public static final EnchantmentProgressManager clientCopy = new EnchantmentProgressManager();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<UUID, Map<Enchantment, Integer>> data = new HashMap<>();
    private final Map<UUID, Map<Enchantment, Integer>> bonusClaimed = new HashMap<>();
    private final Map<UUID, List<EnchantmentProgressSnapshot>> snapshots = new HashMap<>();

    public Map<Enchantment, Integer> getPlayerData(Player player) {
        Map<Enchantment, Integer> playerData = data.get(player.getUUID());

        if (playerData == null) {
            playerData = new HashMap<>();
            data.put(player.getUUID(), playerData);
            this.setDirty();
        }

        return playerData;
    }

    public void setAtomicScore(UUID player, Enchantment enchantment, int score) {
        List<EnchantmentProgressSnapshot> scores = snapshots.get(player);

        if (scores == null) {
            scores = new ArrayList<>();
            snapshots.put(player, scores);
        }

        EnchantmentProgressSnapshot existing = scores.stream().filter(s -> s.enchantment.equals(enchantment))
                .findFirst().orElse(null);

        int index = scores.indexOf(existing);

        if (existing != null) {
            scores.remove(existing);
        }

        scores.add(index, new EnchantmentProgressSnapshot(enchantment, score));

    }

    public Map<Enchantment, Integer> getBonusClaimed(Player player) {
        return bonusClaimed.get(player.getUUID());
    }

    public List<EnchantmentProgressSnapshot> getProgress(Player player) {
        return snapshots.get(player.getUUID());
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

    public void sendAtomicDataToPlayer(ServerPlayer player, Enchantment enchantment, int score) {
        AtomicScoreClientboundPacket packet = new AtomicScoreClientboundPacket(player.getUUID(), enchantment, score);
        EnchantmentProgressChannel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void sendDataToPlayer(ServerPlayer player) {
        Map<Enchantment, Integer> playerData = getPlayerData(player);

        this.getAllPlayerProgress(player);

        EnchantmentManagerClientboundPacket packet = new EnchantmentManagerClientboundPacket(player.getUUID(),
                playerData, bonusClaimed.get(player.getUUID()), snapshots.get(player.getUUID()));

        EnchantmentProgressChannel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void sendNotificationToPlayer(ServerPlayer player, Enchantment enchantment, int level) {
        EnchantmentNotificationClientboundPacket packet = new EnchantmentNotificationClientboundPacket(enchantment,
                level);
        EnchantmentProgressChannel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public List<EnchantmentProgressSnapshot> getAllPlayerProgress(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<EnchantmentProgressSnapshot> progress = new ArrayList<>();

            ServerStatsCounter stats = serverPlayer.getStats();
            for (Map.Entry<Enchantment, Function<ServerStatsCounter, Integer>> entry : EnchantmentUtils.enchantmentRequirements
                    .entrySet()) {
                progress.add(new EnchantmentProgressSnapshot(entry.getKey(), entry.getValue().apply(stats)));
            }

            this.snapshots.put(player.getUUID(), progress);

            return progress;
        } else {
            return this.snapshots.get(player.getUUID());
        }
    }

    public void checkPlayerProgress(Player player, Enchantment enchantment) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int amount = EnchantmentUtils.enchantmentRequirements.get(enchantment)
                .apply(serverPlayer.getStats());

        sendAtomicDataToPlayer(serverPlayer, enchantment, amount);

        UUID uuid = player.getUUID();
        Map<Enchantment, Integer> playerData = data.get(uuid);

        if (playerData == null) {
            playerData = new HashMap<>();
            data.put(uuid, playerData);
            this.setDirty();
        }

        if (EnchantmentProgressSteps.isBonus(enchantment)) {
            checkPlayerBonusProgress(enchantment, playerData, serverPlayer);
            return;
        }

        Integer level = playerData.get(enchantment);
        int newLevel = EnchantmentProgressSteps.getLevel(enchantment, amount);

        // sendDataToPlayer(serverPlayer);

        if (newLevel > 0 && (level == null || newLevel > level)) {
            unlockEnchantment(enchantment, newLevel, playerData, serverPlayer);
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

        this.unlockEnchantment(enchantment, level, playerData, (ServerPlayer) player);
    }

    public void unlockEnchantment(Enchantment enchantment, int level, Map<Enchantment, Integer> playerData,
            ServerPlayer player) {
        LOGGER.info("Unlocking enchantment " + enchantment.getFullname(1) + " with level " + level);
        playerData.put(enchantment, level);

        sendNotificationToPlayer(player, enchantment, level);

        if (!EnchantmentProgressSteps.isBonus(enchantment)) {
            Enchantment bonusEnchantment = this.getBonusEnchantmentsRequiring(enchantment);
            if (bonusEnchantment != null) {
                this.checkPlayerBonusProgress(bonusEnchantment, playerData, player);
            }
        }

        this.setDirty();
    }

    public Enchantment getBonusEnchantmentsRequiring(Enchantment enchantment) {
        for (Map.Entry<Enchantment, List<Enchantment>> bonusEnchantment : EnchantmentUtils.bonusEnchantmentsRequirements
                .entrySet()) {
            if (bonusEnchantment.getValue().contains(enchantment)) {
                return bonusEnchantment.getKey();
            }
        }

        return null;
    }

    public void checkPlayerBonusProgress(Enchantment enchantment, Map<Enchantment, Integer> playerData,
            ServerPlayer player) {
        if (!hasBonusEnchantment(player, enchantment)) {
            return;
        }

        unlockEnchantment(enchantment, 1, playerData, player);
    }

    public boolean hasBonusEnchantment(Player player, Enchantment enchantment) {
        List<Enchantment> requirements = EnchantmentUtils.bonusEnchantmentsRequirements.get(enchantment);

        if (requirements == null) {
            return false;
        }

        for (Enchantment requirement : requirements) {
            if (!isMaxLevel(requirement, getPlayerData(player))) {
                return false;
            }
        }

        return true;
    }

    public List<PossibleEnchantment> filterEnchantmentsListForPlayer(List<Enchantment> enchantments, Player player) {
        Map<Enchantment, Integer> playerData = getPlayerData(player);

        List<PossibleEnchantment> filteredEnchantments = new ArrayList<>();

        for (Enchantment enchantment : enchantments) {
            filteredEnchantments.add(new PossibleEnchantment(enchantment,
                    playerData == null ? 0 : playerData.getOrDefault(enchantment, 0), player)); // (enchantment,
                                                                                                // playerData.get(enchantment));
        }

        Collections.sort(filteredEnchantments, (a, b) -> {
            if (a.status != b.status) {
                return a.status.ordinal() - b.status.ordinal();
            } else {
                return a.enchantment.getFullname(1).toString().compareTo(b.enchantment.getFullname(1).toString());
            }
        });

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

    public boolean isMaxLevel(Enchantment enchantment, Player player) {
        return isMaxLevel(enchantment, getPlayerData(player));
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
        FREE("free", 0x00FF00), UNLOCKED("unlocked", 0xFFFFFF), LOCKED("locked", 0x00FFFF);

        public String name;
        public int color;

        private Status(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }
}
