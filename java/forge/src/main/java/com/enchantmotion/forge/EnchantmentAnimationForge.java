package com.enchantmentanimation.forge;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge mod entrypoint implementation delegating to the shared core package.
 */
@Mod(EnchantmentAnimationCore.MOD_ID)
public class EnchantmentAnimationForge {

    public EnchantmentAnimationForge() {
        EnchantmentAnimationCore.init();
        EnchantmentAnimationCore.LOGGER.info("EnchantMotion Forge initialized successfully.");
    }
}
