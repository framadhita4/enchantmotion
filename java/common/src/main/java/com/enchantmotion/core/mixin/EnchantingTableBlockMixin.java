package com.enchantmentanimation.core.mixin;

import com.enchantmentanimation.core.EnchantTableItemHolder;
import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends BaseEntityBlock {

    protected EnchantingTableBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void enchantmotion$preventOpenWhileEnchanting(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (EnchantmentAnimationCore.preventOpenWhileEnchanting(level, pos)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends BlockEntity> void enchantmotion$enableServerTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type,
            CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (!level.isClientSide()) {
            cir.setReturnValue(createTickerHelper(type, BlockEntityTypes.ENCHANTING_TABLE, (lvl, pos, blockState, be) -> {
                if (be instanceof EnchantTableItemHolder holder) {
                    EnchantmentAnimationCore.tickAnimation(holder, lvl, pos);
                }
            }));
        }
    }
}
