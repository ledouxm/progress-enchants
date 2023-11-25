package com.ledouxm.progressiveenchantments.network;

import java.util.function.Function;
import java.util.function.Supplier;

import com.ledouxm.progressiveenchantments.ProgressiveEnchantments;

import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class EnchantmentProgressChannel {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ProgressiveEnchantments.MODID, "enchantment_progress"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

    private static int id = 0;

    public static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        CHANNEL.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
    }

}
