// path: java/forge/src/main/java/com/enchantmentanimation/forge/client/EnchantmentAnimationClientForge.java
package com.enchantmentanimation.forge.client;

import com.enchantmentanimation.core.EnchantmentAnimationCore;
import core.animation.EnchantAnimation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Forge client event handlers and rendering setup.
 */
@Mod.EventBusSubscriber(modid = EnchantmentAnimationCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EnchantmentAnimationClientForge {

    private static EnchantAnimation currentAnimation;

    public EnchantmentAnimationClientForge() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EnchantmentAnimationCore.init();
        EnchantmentAnimationCore.LOGGER.info("EnchantMotion Forge client initialized.");
        currentAnimation = EnchantAnimation.createEnchantTablePreset();
        currentAnimation.start();
    }

    public static void onRenderTick(double deltaSeconds) {
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.update(deltaSeconds);
        }
    }

    public static EnchantAnimation getCurrentAnimation() {
        return currentAnimation;
    }
}
