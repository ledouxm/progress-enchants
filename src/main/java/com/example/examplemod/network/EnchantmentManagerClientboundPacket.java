package com.example.examplemod.network;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.UUID;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.EnchantmentProgressSnapshot;
import com.example.examplemod.EnchantmentUtils;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantmentManagerClientboundPacket {
    public final UUID player;
    public final Map<Enchantment, Integer> playerData;
    public final Map<Enchantment, Integer> bonusClaimed;
    public final List<EnchantmentProgressSnapshot> progress;

    public EnchantmentManagerClientboundPacket(UUID player, Map<Enchantment, Integer> playerData,
            Map<Enchantment, Integer> bonusClaimed, List<EnchantmentProgressSnapshot> progress) {
        this.player = player;
        this.playerData = playerData;
        this.bonusClaimed = bonusClaimed;
        this.progress = progress;

    }

    public EnchantmentManagerClientboundPacket(FriendlyByteBuf buffer) {
        System.out.println("Reading data from server");
        this.player = this.readUUID(buffer);
        System.out.println("Player: " + player);
        this.playerData = this.readEnchantmentIntegerMap(buffer);
        System.out.println("Player data: " + playerData);
        this.bonusClaimed = this.readEnchantmentIntegerMap(buffer);
        System.out.println("Bonus claimed: " + bonusClaimed);
        this.progress = this.readEnchantmentProgressSnapshotList(buffer);
        System.out.println("Progress: " + progress);
    }

    public void writeEnchantmentIntegerMap(FriendlyByteBuf buffer, Map<Enchantment, Integer> map) {
        if (map == null) {
            buffer.writeInt(0);
            return;
        }

        buffer.writeInt(map.size());
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            buffer.writeUtf(EnchantmentUtils.getEnchantmentId(entry.getKey()));
            buffer.writeInt(entry.getValue());
        }
    }

    public void writeEnchantmentProgressSnapshotList(FriendlyByteBuf buffer, List<EnchantmentProgressSnapshot> list) {
        if (list == null) {
            buffer.writeInt(0);
            return;
        }

        buffer.writeInt(list.size());
        for (EnchantmentProgressSnapshot snapshot : list) {
            buffer.writeUtf(EnchantmentUtils.getEnchantmentId(snapshot.enchantment));
            buffer.writeInt(snapshot.score);
        }
    }

    public Map<Enchantment, Integer> readEnchantmentIntegerMap(FriendlyByteBuf buffer) {
        Map<Enchantment, Integer> map = new HashMap<>();
        int size = buffer.readInt();

        for (int i = 0; i < size; i++) {
            String enchantmentId = buffer.readUtf();
            Enchantment enchantment = EnchantmentUtils.getEnchantmentById(enchantmentId);
            int progress = buffer.readInt();

            map.put(enchantment, progress);
        }

        return map;
    }

    public List<EnchantmentProgressSnapshot> readEnchantmentProgressSnapshotList(FriendlyByteBuf buffer) {
        List<EnchantmentProgressSnapshot> list = new ArrayList<>();
        int size = buffer.readInt();

        for (int i = 0; i < size; i++) {
            String enchantmentId = buffer.readUtf();
            Enchantment enchantment = EnchantmentUtils.getEnchantmentById(enchantmentId);
            int progress = buffer.readInt();

            list.add(new EnchantmentProgressSnapshot(enchantment, progress));
        }

        return list;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(player);
        this.writeEnchantmentIntegerMap(buffer, playerData);
        this.writeEnchantmentIntegerMap(buffer, bonusClaimed);
        this.writeEnchantmentProgressSnapshotList(buffer, progress);
    }

    public UUID readUUID(FriendlyByteBuf buffer) {
        return buffer.readUUID();
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                System.out.println("Received data from server: " + playerData);
                EnchantmentProgressManager.clientCopy.setPlayerData(player, playerData);
                EnchantmentProgressManager.clientCopy.setBonusClaimed(player, bonusClaimed);
                EnchantmentProgressManager.clientCopy.setProgress(player, progress);
            });
        });

        context.get().setPacketHandled(true);

        return true;
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        EnchantmentProgressChannel.registerMessage(EnchantmentManagerClientboundPacket.class,
                EnchantmentManagerClientboundPacket::write, EnchantmentManagerClientboundPacket::new,
                EnchantmentManagerClientboundPacket::handle);
    }
}
