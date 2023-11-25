package com.ledouxm.progressiveenchantments.network;

import java.util.function.Supplier;

import com.ledouxm.progressiveenchantments.EnchantmentNotification;
import com.ledouxm.progressiveenchantments.EnchantmentProgressSteps;
import com.ledouxm.progressiveenchantments.EnchantmentUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantmentNotificationClientboundPacket {
    private Enchantment enchantment;
    private int level;

    public EnchantmentNotificationClientboundPacket(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public EnchantmentNotificationClientboundPacket(FriendlyByteBuf buffer) {
        this.enchantment = this.readEnchantment(buffer);
        this.level = buffer.readInt();
    }

    public void write(FriendlyByteBuf buffer) {
        this.writeEnchantment(buffer, this.enchantment);
        buffer.writeInt(this.level);
    }

    public Enchantment readEnchantment(FriendlyByteBuf buffer) {
        return EnchantmentUtils.getEnchantmentById(buffer.readUtf());
    }

    public void writeEnchantment(FriendlyByteBuf buffer, Enchantment enchantment) {
        buffer.writeUtf(EnchantmentUtils.getEnchantmentId(enchantment));
    }

    public Component format() {
        MutableComponent enchantmentComponent = (MutableComponent) this.enchantment.getFullname(this.level);

        return Component.literal("Unlocked ")
                .append(enchantmentComponent.withStyle(getEnchantmentColor(this.enchantment)))
                .append(Component.literal(" !"));
    }

    public ChatFormatting getEnchantmentColor(Enchantment enchantment) {
        if (EnchantmentProgressSteps.isBonus(enchantment))
            return ChatFormatting.GOLD;
        return ChatFormatting.WHITE;
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                EnchantmentNotification.addNotification(this.format());
            });
        });

        context.get().setPacketHandled(true);

        return true;
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        EnchantmentProgressChannel.registerMessage(EnchantmentNotificationClientboundPacket.class,
                EnchantmentNotificationClientboundPacket::write, EnchantmentNotificationClientboundPacket::new,
                EnchantmentNotificationClientboundPacket::handle);
    }

}
