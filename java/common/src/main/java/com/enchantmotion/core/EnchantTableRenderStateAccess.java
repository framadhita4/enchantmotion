package com.enchantmentanimation.core;

import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * Accessor interface for EnchantTableRenderState holding rendering properties.
 * Shared across all mod loaders via the core package.
 */
public interface EnchantTableRenderStateAccess {
    ItemStackRenderState enchantmotion$getItemState();
    boolean enchantmotion$hasItem();
    void enchantmotion$setHasItem(boolean hasItem);

    int enchantmotion$getLapisCount();
    void enchantmotion$setLapisCount(int count);

    float enchantmotion$getEnchantProgress();
    void enchantmotion$setEnchantProgress(float progress);

    boolean enchantmotion$isEnchanting();
    void enchantmotion$setIsEnchanting(boolean enchanting);

    int enchantmotion$getEnchantTier();
    void enchantmotion$setEnchantTier(int tier);
}
