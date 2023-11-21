package com.example.examplemod;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class CustomEventHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        System.out.println("Block broken!");
        System.out.println(event);
    }

    @SubscribeEvent
    public void pickupItem(EntityItemPickupEvent event) {
        System.out.println("Item picked up!");
    }

}
