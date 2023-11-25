package com.ledouxm.progressiveenchantments.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EnchantingBenchRenderer implements BlockEntityRenderer<EnchantingBenchEntity> {

    public ItemStack item;

    public EnchantingBenchRenderer(BlockEntityRendererProvider.Context context) {
        item = new ItemStack(Items.IRON_SWORD, 1);
    }

    @Override
    public void render(EnchantingBenchEntity entity, float partialTicks_, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        Level level = entity.getLevel();
        if (level == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 1f, 0.5f);

        poseStack.mulPose(Axis.YN.rotationDegrees(45f));
        poseStack.mulPose(Axis.XN.rotationDegrees(180f));
        poseStack.mulPose(Axis.ZN.rotationDegrees(45f));

        poseStack.scale(0.5f, 0.5f, 0.5f);
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED,
                combinedLightIn, combinedOverlayIn, poseStack, buffer, level, 0);
        poseStack.popPose();
    }

}
