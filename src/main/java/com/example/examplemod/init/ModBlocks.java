package com.example.examplemod.init;

import java.util.function.Supplier;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.EnchantingBenchBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
                        ExampleMod.MODID);

        public static final RegistryObject<Block> ENCHANTING_BENCH = registerBlock("enchanting_bench",
                        () -> new EnchantingBenchBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1)));

        public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
                RegistryObject<T> toReturn = BLOCKS.register(name, block);
                // registerBlockItem(name, toReturn);
                return toReturn;
        }
}