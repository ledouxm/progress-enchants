package com.example.examplemod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class EnchantmentProgressManager extends SavedData {
    private static final EnchantmentProgressManager clientCopy = new EnchantmentProgressManager();

    private final Map<UUID, Map<Enchantment, Integer>> data = new HashMap<>();

    public static EnchantmentProgressManager create() {
        return new EnchantmentProgressManager();
    }

    public static EnchantmentProgressManager read(CompoundTag tag) {
        EnchantmentProgressManager data = create();

        return data;
    }

    public Map<Enchantment, Integer> getPlayerData(Player player) {
        return data.get(player.getUUID());
    }

    public void checkPlayerProgress(Player player, Enchantment enchantment, int amount) {
        UUID uuid = player.getUUID();
        Map<Enchantment, Integer> playerData = data.get(uuid);

        if (playerData == null) {
            playerData = new HashMap<>();
            data.put(uuid, playerData);
        }

        if (EnchantmentProgressSteps.isBonus(enchantment)) {
            checkPlayerBonusProgress(player, enchantment, playerData);
        }

        Integer progress = playerData.get(enchantment);
        int newProgress = EnchantmentProgressSteps.getProgress(enchantment, amount);

        if (newProgress > 0 && (progress == null || newProgress > progress)) {
            unlockEnchantment(enchantment, newProgress, playerData);
        }
    }

    private void unlockEnchantment(Enchantment enchantment, int progress, Map<Enchantment, Integer> playerData) {
        playerData.put(enchantment, progress);
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

    public Map<Enchantment, Integer> filterEnchantmentsListForPlayer(List<Enchantment> enchantments, Player player,
            boolean showAll) {
        Map<Enchantment, Integer> playerData = getPlayerData(player);
        System.out.println("playerData: " + playerData);

        Map<Enchantment, Integer> filteredEnchantments = new HashMap<>();

        for (Enchantment enchantment : enchantments) {
            if (playerData != null && playerData.containsKey(enchantment)) {
                filteredEnchantments.put(enchantment, playerData.get(enchantment));
            } else if (showAll) {
                filteredEnchantments.put(enchantment, 0);
            }
        }

        return filteredEnchantments;
    }

    public Integer getProgress(Enchantment enchantment, Map<Enchantment, Integer> playerData) {
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
        Integer progress = getProgress(enchantment, playerData);
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

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag tagList = new ListTag();
        for (Map.Entry<UUID, Map<Enchantment, Integer>> entry : data.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());

            ListTag enchantmentsTag = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantmentEntry : entry.getValue().entrySet()) {
                CompoundTag enchantmentTag = new CompoundTag();
                enchantmentTag.putString("enchantment", enchantmentEntry.getClass().getName());
                enchantmentTag.putInt("progress", enchantmentEntry.getValue());
                enchantmentsTag.add(enchantmentTag);
            }

            playerTag.put("enchantments", enchantmentsTag);
            tagList.add(playerTag);
        }

        tag.put("enchantment_progress", tagList);
        return tag;
    }

    public static EnchantmentProgressManager get(@Nullable MinecraftServer server) {
        if (server != null) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);

            return Objects.requireNonNull(overworld).getDataStorage().computeIfAbsent(EnchantmentProgressManager::read,
                    EnchantmentProgressManager::create,
                    "enchantment_progress");
        }

        return clientCopy;
    }
}
