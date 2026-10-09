// path: core/effects/RuneEffect.java
package core.effects;

import core.animation.AnimationEvent;
import core.animation.EnchantAnimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simulates mystical Standard Galactic Alphabet (SGA) runes orbiting around the enchanted item.
 */
public class RuneEffect implements EnchantmentEffect {

    public static record RuneInstance(
            int index,
            int glyphId,
            double x,
            double y,
            double z,
            double rotationYaw,
            double rotationPitch,
            double alpha,
            double scale
    ) {}

    private boolean enabled = true;
    private int runeCount = 12;
    private double baseRadius = 1.2;
    private double angularVelocity = 1.5; // radians per second
    private double bobbingSpeed = 2.0;
    private double bobbingAmplitude = 0.15;
    private double currentAngle = 0.0;
    private double totalTime = 0.0;

    private final List<RuneInstance> activeRunes = new ArrayList<>();

    public RuneEffect() {
        this(12, 1.2, 1.5);
    }

    public RuneEffect(int runeCount, double baseRadius, double angularVelocity) {
        this.runeCount = Math.max(1, runeCount);
        this.baseRadius = baseRadius;
        this.angularVelocity = angularVelocity;
    }

    @Override
    public String getName() {
        return "RuneEffect";
    }

    @Override
    public void initialize(EnchantAnimation animation) {
        reset();
    }

    @Override
    public void update(double deltaSeconds, double progress, double intensity) {
        if (!enabled) {
            activeRunes.clear();
            return;
        }

        totalTime += deltaSeconds;
        currentAngle += angularVelocity * deltaSeconds * (0.5 + intensity * 0.5);

        activeRunes.clear();
        double angleStep = (2.0 * Math.PI) / runeCount;
        double currentRadius = baseRadius * (0.8 + 0.4 * intensity);

        for (int i = 0; i < runeCount; i++) {
            double theta = currentAngle + i * angleStep;
            double x = currentRadius * Math.cos(theta);
            double z = currentRadius * Math.sin(theta);
            double y = Math.sin(totalTime * bobbingSpeed + i) * bobbingAmplitude + (intensity * 0.2);

            // Inward-facing orientation
            double yaw = Math.toDegrees(-theta + Math.PI / 2.0);
            double pitch = Math.sin(totalTime + i) * 10.0;
            double alpha = Math.min(1.0, 0.4 + 0.6 * intensity);
            double scale = 0.8 + 0.4 * Math.sin(totalTime * 3.0 + i);

            activeRunes.add(new RuneInstance(
                    i,
                    (i * 3 + (int) (totalTime * 2)) % 26, // SGA glyph index 0-25
                    x, y, z,
                    yaw, pitch,
                    alpha, scale
            ));
        }
    }

    @Override
    public void onAnimationEvent(AnimationEvent event) {
        if (event.getType() == AnimationEvent.EventType.KEYFRAME_REACHED) {
            // Burst rune spin on milestone keyframes
            this.currentAngle += Math.PI / 4.0;
        }
    }

    @Override
    public void reset() {
        this.currentAngle = 0.0;
        this.totalTime = 0.0;
        this.activeRunes.clear();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RuneInstance> getActiveRunes() {
        return Collections.unmodifiableList(activeRunes);
    }

    public int getRuneCount() {
        return runeCount;
    }

    public void setRuneCount(int runeCount) {
        this.runeCount = Math.max(1, runeCount);
    }

    public double getBaseRadius() {
        return baseRadius;
    }

    public void setBaseRadius(double baseRadius) {
        this.baseRadius = baseRadius;
    }
}
