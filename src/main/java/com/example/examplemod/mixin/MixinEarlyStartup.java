package com.example.examplemod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.examplemod.init.ModStats;

import net.minecraft.server.Bootstrap;

@Mixin(Bootstrap.class)
public abstract class MixinEarlyStartup {

    @Inject(at = @At("TAIL"), method = "bootStrap")
    private static void initRegistries(CallbackInfo ci) {
        // ModStats.init();
    }

}