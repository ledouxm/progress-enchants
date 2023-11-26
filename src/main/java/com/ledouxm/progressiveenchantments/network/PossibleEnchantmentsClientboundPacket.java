package com.ledouxm.progressiveenchantments.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ledouxm.progressiveenchantments.block.PossibleEnchantment;
import com.ledouxm.progressiveenchantments.client.ClientModHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PossibleEnchantmentsClientboundPacket {
    public List<PossibleEnchantment> possibleEnchantments;

    public PossibleEnchantmentsClientboundPacket(List<PossibleEnchantment> possibleEnchantments) {
        this.possibleEnchantments = possibleEnchantments;
    }

    public PossibleEnchantmentsClientboundPacket(FriendlyByteBuf buffer) {
        this.possibleEnchantments = this.readPossibleEnchantmentList(buffer);
    }

    public void write(FriendlyByteBuf buffer) {
        this.writePossibleEnchantmentList(buffer, this.possibleEnchantments);
    }

    public List<PossibleEnchantment> readPossibleEnchantmentList(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<PossibleEnchantment> list = new ArrayList<PossibleEnchantment>(size);
        for (int i = 0; i < size; i++) {
            list.add(new PossibleEnchantment(buffer));
        }
        return list;
    }

    public void writePossibleEnchantmentList(FriendlyByteBuf buffer, List<PossibleEnchantment> list) {
        buffer.writeInt(list.size());
        for (PossibleEnchantment possibleEnchantment : list) {
            possibleEnchantment.write(buffer);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientModHandler.onPossibleEnchantments(this);
            });
        });

        context.get().setPacketHandled(true);

        return true;
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        EnchantmentProgressChannel.registerMessage(PossibleEnchantmentsClientboundPacket.class,
                PossibleEnchantmentsClientboundPacket::write, PossibleEnchantmentsClientboundPacket::new,
                PossibleEnchantmentsClientboundPacket::handle);
    }
}