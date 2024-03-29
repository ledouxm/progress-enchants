package com.ledouxm.progressiveenchantments.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.ledouxm.progressiveenchantments.EnchantmentProgressSnapshot;
import com.ledouxm.progressiveenchantments.EnchantmentUtils;
import com.ledouxm.progressiveenchantments.client.ClientModHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

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
        this.player = this.readUUID(buffer);
        this.playerData = this.readEnchantmentIntegerMap(buffer);
        this.bonusClaimed = this.readEnchantmentIntegerMap(buffer);
        this.progress = this.readEnchantmentProgressSnapshotList(buffer);
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
                ClientModHandler.onEnchantmentManagerPacket(this);
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
