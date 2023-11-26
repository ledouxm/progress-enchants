package com.ledouxm.progressiveenchantments.network;

import java.util.function.Supplier;

import org.slf4j.Logger;

import com.ledouxm.progressiveenchantments.EnchantmentUtils;
import com.ledouxm.progressiveenchantments.EnchantmentProgressManager.Status;
import com.ledouxm.progressiveenchantments.block.EnchantingBenchMenu;
import com.ledouxm.progressiveenchantments.block.PossibleEnchantment;
import com.mojang.logging.LogUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantItemActionServerboundPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ItemStack item;
    public Enchantment enchantment;
    public int level;
    public Status status;

    private int cost;

    public EnchantItemActionServerboundPacket(ItemStack item, PossibleEnchantment enchantment) {
        this.item = item;
        this.enchantment = enchantment.enchantment;
        this.level = enchantment.level;
        this.status = enchantment.status;
        this.cost = enchantment.cost;
    }

    public EnchantItemActionServerboundPacket(FriendlyByteBuf buffer) {
        this.item = buffer.readItem();
        this.enchantment = EnchantmentUtils.getEnchantmentById(buffer.readUtf());
        this.level = buffer.readInt();
        this.status = Status.getByName(buffer.readUtf());
        this.cost = buffer.readInt();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeItem(this.item);
        buffer.writeUtf(EnchantmentUtils.getEnchantmentId(enchantment));
        buffer.writeInt(level);
        buffer.writeUtf(status.name());
        buffer.writeInt(cost);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LOGGER.info("ENQUEUED WORK");
            ServerPlayer player = context.get().getSender();
            LOGGER.info(this.enchantment.toString());

            EnchantingBenchMenu menu = (EnchantingBenchMenu) player.containerMenu;
            menu.performEnchantment(enchantment, level, status, cost);
            menu.broadcastChanges();
        });

        context.get().setPacketHandled(true);

        return true;
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        EnchantmentProgressChannel.registerMessage(EnchantItemActionServerboundPacket.class,
                EnchantItemActionServerboundPacket::write, EnchantItemActionServerboundPacket::new,
                EnchantItemActionServerboundPacket::handle);
    }
}
