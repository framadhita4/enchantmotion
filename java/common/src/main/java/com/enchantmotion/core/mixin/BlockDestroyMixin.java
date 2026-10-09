package com.enchantmentanimation.core.mixin;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockDestroyMixin {

    @Inject(method = "destroy", at = @At("HEAD"), remap = false)
    private void enchantmotion$onDestroy(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof EnchantingTableBlock) {
            EnchantmentAnimationCore.handleBlockDestroyed(level, pos);
        }
    }
}
