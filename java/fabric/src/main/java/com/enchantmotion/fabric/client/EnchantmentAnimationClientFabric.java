// path: java/fabric/src/main/java/com/enchantmentanimation/fabric/client/EnchantmentAnimationClientFabric.java
package com.enchantmentanimation.fabric.client;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import core.animation.EnchantAnimation;

/**
 * Fabric client-side entrypoint for rendering hooks and tick listeners.
 */
public class EnchantmentAnimationClientFabric {

    private EnchantAnimation currentAnimation;

    public void onInitializeClient() {
        EnchantmentAnimationCore.init();
        EnchantmentAnimationCore.LOGGER.info("EnchantMotion Fabric client initialized.");
        this.currentAnimation = EnchantAnimation.createItemGlintPreset();
        this.currentAnimation.start();
    }

    public void onClientTick(double deltaSeconds) {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.update(deltaSeconds);
        }
    }

    public EnchantAnimation getCurrentAnimation() {
        return currentAnimation;
    }
}
