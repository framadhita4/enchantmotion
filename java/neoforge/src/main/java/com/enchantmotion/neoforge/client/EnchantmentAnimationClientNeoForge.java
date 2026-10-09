// path: java/neoforge/src/main/java/com/enchantmentanimation/neoforge/client/EnchantmentAnimationClientNeoForge.java
package com.enchantmentanimation.neoforge.client;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import core.animation.EnchantAnimation;

/**
 * NeoForge client event handlers and rendering setup.
 */
public class EnchantmentAnimationClientNeoForge {

    private final EnchantAnimation currentAnimation;

    public EnchantmentAnimationClientNeoForge() {
        EnchantmentAnimationCore.init();
        EnchantmentAnimationCore.LOGGER.info("EnchantMotion NeoForge client initialized.");
        this.currentAnimation = EnchantAnimation.createEnchantTablePreset();
        this.currentAnimation.start();
    }

    public void onRenderTick(double deltaSeconds) {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.update(deltaSeconds);
        }
    }

    public EnchantAnimation getCurrentAnimation() {
        return currentAnimation;
    }
}
