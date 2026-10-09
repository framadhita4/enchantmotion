// path: java/common/src/main/java/com/enchantmentanimation/common/config/AnimationConfig.java
package com.enchantmentanimation.common.config;

/**
 * Common configuration options for enchantment animation and visual effects.
 */
public class AnimationConfig {

    private boolean enableRunes = true;
    private boolean enableItemGlow = true;
    private boolean enableParticles = true;
    private double particleDensity = 1.0;
    private double animationSpeed = 1.0;

    public boolean isEnableRunes() {
        return enableRunes;
    }

    public void setEnableRunes(boolean enableRunes) {
        this.enableRunes = enableRunes;
    }

    public boolean isEnableItemGlow() {
        return enableItemGlow;
    }

    public void setEnableItemGlow(boolean enableItemGlow) {
        this.enableItemGlow = enableItemGlow;
    }

    public boolean isEnableParticles() {
        return enableParticles;
    }

    public void setEnableParticles(boolean enableParticles) {
        this.enableParticles = enableParticles;
    }

    public double getParticleDensity() {
        return particleDensity;
    }

    public void setParticleDensity(double particleDensity) {
        this.particleDensity = Math.max(0.0, Math.min(2.0, particleDensity));
    }

    public double getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(double animationSpeed) {
        this.animationSpeed = Math.max(0.1, Math.min(5.0, animationSpeed));
    }
}
