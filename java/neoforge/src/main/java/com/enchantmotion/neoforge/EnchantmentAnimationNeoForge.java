package com.enchantmentanimation.neoforge;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge mod entrypoint implementation delegating to the shared core package.
 */
@Mod(EnchantmentAnimationCore.MOD_ID)
public class EnchantmentAnimationNeoForge {

    public EnchantmentAnimationNeoForge() {
        EnchantmentAnimationCore.init();
        EnchantmentAnimationCore.LOGGER.info("EnchantMotion NeoForge initialized successfully.");
    }
}
