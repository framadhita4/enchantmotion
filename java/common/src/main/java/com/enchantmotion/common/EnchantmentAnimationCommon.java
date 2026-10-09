// path: java/common/src/main/java/com/enchantmentanimation/common/EnchantmentAnimationCommon.java
package com.enchantmentanimation.common;

import core.animation.EnchantAnimation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Platform-independent common logic and lifecycle registration for Minecraft Java Edition.
 */
public final class EnchantmentAnimationCommon {

    public static final String MOD_ID = "enchantmotion";
    public static final String MOD_NAME = "EnchantMotion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static boolean initialized = false;

    private EnchantmentAnimationCommon() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        LOGGER.info("Initializing {} common runtime (MOD_ID: {})", MOD_NAME, MOD_ID);
        
        // Warm up animation presets
        EnchantAnimation preset = EnchantAnimation.createEnchantTablePreset();
        LOGGER.debug("Loaded core enchantment animation preset: {}", preset.getId());
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
