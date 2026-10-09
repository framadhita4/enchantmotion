package com.enchantmentanimation.core.mixin;

import com.enchantmentanimation.core.EnchantTableItemHolder;
import com.enchantmentanimation.core.EnchantTableRenderStateAccess;
import com.enchantmentanimation.core.client.EnchantTableFloatingItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantTableRenderer.class)
public abstract class EnchantTableRendererMixin implements BlockEntityRenderer<EnchantingTableBlockEntity, EnchantTableRenderState> {

    @Unique
    private ItemModelResolver enchantmotion$itemModelResolver;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void enchantmotion$initResolver(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        this.enchantmotion$itemModelResolver = context.itemModelResolver();
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/EnchantingTableBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/EnchantTableRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"), remap = false)
    private void enchantmotion$extractItem(
            EnchantingTableBlockEntity be,
            EnchantTableRenderState state,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfo ci) {
        if (be instanceof EnchantTableItemHolder holder && state instanceof EnchantTableRenderStateAccess access) {
            ItemStack stack = holder.enchantmotion$getItem();
            ItemStack lapis = holder.enchantmotion$getLapis();
            access.enchantmotion$setLapisCount(lapis == null ? 0 : lapis.getCount());
            access.enchantmotion$setIsEnchanting(holder.enchantmotion$isEnchanting());
            access.enchantmotion$setEnchantProgress(holder.enchantmotion$getEnchantProgress(partialTick));
            access.enchantmotion$setEnchantTier(holder.enchantmotion$getEnchantTier());

            if (!stack.isEmpty()) {
                access.enchantmotion$setHasItem(true);
                if (this.enchantmotion$itemModelResolver != null) {
                    this.enchantmotion$itemModelResolver.updateForTopItem(
                            access.enchantmotion$getItemState(),
                            stack,
                            ItemDisplayContext.FIXED,
                            be.getLevel(),
                            null,
                            0
                    );
                }
            } else {
                access.enchantmotion$setHasItem(false);
                access.enchantmotion$getItemState().clear();
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EnchantTableRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"), remap = false)
    private void enchantmotion$renderFloatingItem(
            EnchantTableRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo ci) {
        EnchantTableFloatingItemRenderer.render(state, poseStack, submitNodeCollector, cameraRenderState);
    }

    // NeoForge-only extension method from IBlockEntityRendererExtension; require = 0 allows Fabric to ignore it safely.
    @Inject(method = "getRenderBoundingBox(Lnet/minecraft/world/level/block/entity/EnchantingTableBlockEntity;)Lnet/minecraft/world/phys/AABB;", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void enchantmotion$expandAABB(EnchantingTableBlockEntity be, CallbackInfoReturnable<AABB> cir) {
        BlockPos pos = be.getBlockPos();
        cir.setReturnValue(new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2.2, pos.getZ() + 1));
    }
}
