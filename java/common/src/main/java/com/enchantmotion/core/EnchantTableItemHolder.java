package com.enchantmentanimation.core;

import net.minecraft.world.item.ItemStack;

/**
 * Interface injected onto EnchantingTableBlockEntity to store and retrieve the enchanting item and lapis.
 * Shared across all mod loaders via the core package.
 */
public interface EnchantTableItemHolder {
    ItemStack enchantmotion$getItem();
    void enchantmotion$setItem(ItemStack stack);

    ItemStack enchantmotion$getLapis();
    void enchantmotion$setLapis(ItemStack stack);

    void enchantmotion$triggerEnchant(int tier);
    float enchantmotion$getEnchantProgress(float partialTick);
    boolean enchantmotion$isEnchanting();
    int enchantmotion$getEnchantTier();

    void enchantmotion$finishEnchantAndDrop(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos);
    void enchantmotion$resetEnchant();

    boolean enchantmotion$hasClientExploded();
    void enchantmotion$setClientExploded(boolean exploded);

    int enchantmotion$getLapisImpactSoundMask();
    void enchantmotion$setLapisImpactSoundMask(int mask);

    long enchantmotion$getEnchantStartTimeMs();
}
