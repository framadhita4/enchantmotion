package com.enchantmentanimation.neoforge.client;

/**
 * @deprecated Use {@link com.enchantmentanimation.core.client.EnchantTableFloatingItemRenderer} instead.
 */
@Deprecated
public final class EnchantTableFloatingItemRenderer {
    private EnchantTableFloatingItemRenderer() {}

    public static void render(
            net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState state,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector,
            net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState) {
        com.enchantmentanimation.core.client.EnchantTableFloatingItemRenderer.render(state, poseStack, submitNodeCollector, cameraRenderState);
    }
}
