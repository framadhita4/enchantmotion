// path: java/fabric/src/main/java/com/enchantmentanimation/fabric/EnchantmentAnimationFabric.java
package com.enchantmentanimation.fabric;

import com.enchantmentanimation.core.EnchantmentAnimationCore;

/**
 * Fabric mod entrypoint implementation delegating to the shared core package.
 */
public class EnchantmentAnimationFabric {

    public void onInitialize() {
        EnchantmentAnimationCore.init();
        EnchantmentAnimationCore.LOGGER.info("EnchantMotion Fabric initialized successfully.");
    }
}
