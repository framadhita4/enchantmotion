// path: core/effects/ItemGlow.java
package core.effects;

import core.animation.AnimationEvent;
import core.animation.EnchantAnimation;

/**
 * Controls dynamic glint shimmer, specular sweep offsets, and pulse aura illumination.
 */
public class ItemGlow implements EnchantmentEffect {

    public static record RgbColor(float red, float green, float blue, float alpha) {
        public static final RgbColor CLASSIC_PURPLE = new RgbColor(0.608f, 0.349f, 0.714f, 1.0f);
        public static final RgbColor CELESTIAL_CYAN = new RgbColor(0.102f, 0.737f, 0.612f, 1.0f);
        public static final RgbColor GOLDEN_AURA = new RgbColor(0.945f, 0.769f, 0.059f, 1.0f);
        public static final RgbColor NETHER_CRIMSON = new RgbColor(0.906f, 0.298f, 0.235f, 1.0f);

        public static RgbColor fromHex(int hex, float alpha) {
            float r = ((hex >> 16) & 0xFF) / 255.0f;
            float g = ((hex >> 8) & 0xFF) / 255.0f;
            float b = (hex & 0xFF) / 255.0f;
            return new RgbColor(r, g, b, alpha);
        }
    }

    private boolean enabled = true;
    private RgbColor primaryColor = RgbColor.CLASSIC_PURPLE;
    private double glintSpeed = 0.75; // UV translation rate per second
    private double pulseFrequency = 2.5; // Sine pulses per second
    private double minIntensity = 0.2;
    private double maxIntensity = 1.0;

    private double uvOffset = 0.0;
    private double currentIntensity = 0.5;
    private double totalTime = 0.0;

    public ItemGlow() {}

    public ItemGlow(RgbColor primaryColor, double glintSpeed) {
        this.primaryColor = primaryColor != null ? primaryColor : RgbColor.CLASSIC_PURPLE;
        this.glintSpeed = glintSpeed;
    }

    @Override
    public String getName() {
        return "ItemGlow";
    }

    @Override
    public void initialize(EnchantAnimation animation) {
        reset();
    }

    @Override
    public void update(double deltaSeconds, double progress, double intensityMultiplier) {
        if (!enabled) {
            currentIntensity = 0.0;
            return;
        }

        totalTime += deltaSeconds;
        uvOffset = (uvOffset + deltaSeconds * glintSpeed) % 1.0;

        double sineFactor = 0.5 + 0.5 * Math.sin(totalTime * pulseFrequency * Math.PI * 2.0);
        double base = minIntensity + sineFactor * (maxIntensity - minIntensity);
        this.currentIntensity = Math.min(1.0, Math.max(0.0, base * intensityMultiplier));
    }

    @Override
    public void onAnimationEvent(AnimationEvent event) {
        if (event.getType() == AnimationEvent.EventType.LOOP_RESTARTED) {
            this.uvOffset = 0.0;
        }
    }

    @Override
    public void reset() {
        this.uvOffset = 0.0;
        this.currentIntensity = minIntensity;
        this.totalTime = 0.0;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getUvOffset() {
        return uvOffset;
    }

    public double getCurrentIntensity() {
        return currentIntensity;
    }

    public RgbColor getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(RgbColor primaryColor) {
        this.primaryColor = primaryColor != null ? primaryColor : RgbColor.CLASSIC_PURPLE;
    }

    public double getGlintSpeed() {
        return glintSpeed;
    }

    public void setGlintSpeed(double glintSpeed) {
        this.glintSpeed = glintSpeed;
    }
}
