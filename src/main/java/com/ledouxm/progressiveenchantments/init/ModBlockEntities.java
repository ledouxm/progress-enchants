package com.ledouxm.progressiveenchantments.init;

import com.ledouxm.progressiveenchantments.ProgressiveEnchantments;
import com.ledouxm.progressiveenchantments.block.EnchantingBenchEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.BLOCK_ENTITY_TYPES, ProgressiveEnchantments.MODID);

        public static final RegistryObject<BlockEntityType<EnchantingBenchEntity>> ENCHANTING_BENCH_ENTITY = BLOCK_ENTITIES
                        .register("enchantment_bench_entity",
                                        () -> BlockEntityType.Builder
                                                        .of(EnchantingBenchEntity::new,
                                                                        ModBlocks.ENCHANTING_BENCH.get())
                                                        .build(null));

}
