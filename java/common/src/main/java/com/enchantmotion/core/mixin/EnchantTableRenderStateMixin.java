package com.enchantmentanimation.core.mixin;

import com.enchantmentanimation.core.EnchantTableRenderStateAccess;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EnchantTableRenderState.class)
public abstract class EnchantTableRenderStateMixin extends BlockEntityRenderState implements EnchantTableRenderStateAccess {

    @Unique
    private final ItemStackRenderState enchantmotion$itemState = new ItemStackRenderState();

    @Unique
    private boolean enchantmotion$hasItem = false;

    @Unique
    private int enchantmotion$lapisCount = 0;

    @Unique
    private float enchantmotion$enchantProgress = 1.0F;

    @Unique
    private boolean enchantmotion$isEnchanting = false;

    @Unique
    private int enchantmotion$enchantTier = 1;

    @Override
    public ItemStackRenderState enchantmotion$getItemState() {
        return this.enchantmotion$itemState;
    }

    @Override
    public boolean enchantmotion$hasItem() {
        return this.enchantmotion$hasItem;
    }

    @Override
    public void enchantmotion$setHasItem(boolean hasItem) {
        this.enchantmotion$hasItem = hasItem;
    }

    @Override
    public int enchantmotion$getLapisCount() {
        return this.enchantmotion$lapisCount;
    }

    @Override
    public void enchantmotion$setLapisCount(int count) {
        this.enchantmotion$lapisCount = count;
    }

    @Override
    public float enchantmotion$getEnchantProgress() {
        return this.enchantmotion$enchantProgress;
    }

    @Override
    public void enchantmotion$setEnchantProgress(float progress) {
        this.enchantmotion$enchantProgress = progress;
    }

    @Override
    public boolean enchantmotion$isEnchanting() {
        return this.enchantmotion$isEnchanting;
    }

    @Override
    public void enchantmotion$setIsEnchanting(boolean enchanting) {
        this.enchantmotion$isEnchanting = enchanting;
    }

    @Override
    public int enchantmotion$getEnchantTier() {
        return this.enchantmotion$enchantTier;
    }

    @Override
    public void enchantmotion$setEnchantTier(int tier) {
        this.enchantmotion$enchantTier = tier;
    }
}
