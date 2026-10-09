// path: core/effects/EnchantmentEffect.java
package core.effects;

import core.animation.AnimationEvent;
import core.animation.EnchantAnimation;

/**
 * Base interface representing a modular visual effect (runes, glow, particles)
 * coordinated by an {@link EnchantAnimation}.
 */
public interface EnchantmentEffect {

    /**
     * Unique identifier for this effect type.
     */
    String getName();

    /**
     * Called when the effect is attached to an animation orchestrator.
     */
    void initialize(EnchantAnimation animation);

    /**
     * Updates the effect state based on elapsed time and current animation progress.
     *
     * @param deltaSeconds time step in seconds
     * @param progress global animation progress (0.0 to 1.0)
     * @param intensity dynamic intensity multiplier
     */
    void update(double deltaSeconds, double progress, double intensity);

    /**
     * Handles lifecycle or milestone animation events.
     */
    void onAnimationEvent(AnimationEvent event);

    /**
     * Resets the effect simulation state.
     */
    void reset();

    /**
     * Checks if this effect is actively rendering.
     */
    boolean isEnabled();

    /**
     * Toggles effect activation.
     */
    void setEnabled(boolean enabled);
}
