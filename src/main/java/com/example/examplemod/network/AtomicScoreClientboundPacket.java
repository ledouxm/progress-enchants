package com.example.examplemod.network;

import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.example.examplemod.EnchantmentProgressManager;
import com.example.examplemod.EnchantmentUtils;
import com.mojang.logging.LogUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AtomicScoreClientboundPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Enchantment enchantment;
    public int score;
    public UUID player;

    public AtomicScoreClientboundPacket(UUID player, Enchantment enchantment, int score) {
        LOGGER.info("AtomicScoreClientboundPacket: " + player + ", " + enchantment + ", " + score);
        this.player = player;
        this.enchantment = enchantment;
        this.score = score;
    }

    public AtomicScoreClientboundPacket(FriendlyByteBuf buffer) {
        LOGGER.info("AtomicScoreClientboundPacket: " + buffer);
        this.player = buffer.readUUID();
        this.enchantment = this.readEnchantment(buffer);
        this.score = buffer.readInt();
    }

    public void writeEnchantment(FriendlyByteBuf buffer, Enchantment enchantment) {
        buffer.writeUtf(EnchantmentUtils.getEnchantmentId(enchantment));
    }

    public Enchantment readEnchantment(FriendlyByteBuf buffer) {
        return EnchantmentUtils.getEnchantmentById(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.player);
        this.writeEnchantment(buffer, this.enchantment);
        buffer.writeInt(this.score);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LOGGER.info("AtomicScoreClientboundPacket.handle: " + this.player + ", " + this.enchantment + ", "
                        + this.score);
                EnchantmentProgressManager.get(null).setAtomicScore(this.player, this.enchantment, this.score);
            });
        });

        context.get().setPacketHandled(true);

        return true;
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        EnchantmentProgressChannel.registerMessage(AtomicScoreClientboundPacket.class,
                AtomicScoreClientboundPacket::write, AtomicScoreClientboundPacket::new,
                AtomicScoreClientboundPacket::handle);
    }
}
