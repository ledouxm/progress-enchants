package com.example.examplemod;

import net.minecraft.world.entity.Entity;

public class EntityWithTimestamp {
    public Entity entity;
    public long timestamp;

    public EntityWithTimestamp(Entity entity) {
        this.entity = entity;
        this.timestamp = System.currentTimeMillis();
    }
}