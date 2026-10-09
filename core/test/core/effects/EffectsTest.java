// path: core/test/core/effects/EffectsTest.java
package core.test.core.effects;

import core.animation.AnimationEvent;
import core.effects.EnchantParticles;
import core.effects.ItemGlow;
import core.effects.RuneEffect;

public class EffectsTest {

    public static void runAllTests() {
        testRuneEffect();
        testItemGlow();
        testEnchantParticles();
        System.out.println("  ✓ EffectsTest passed");
    }

    private static void testRuneEffect() {
        RuneEffect runes = new RuneEffect(8, 1.5, 2.0);
        assertEquals("RuneEffect", runes.getName());

        runes.update(0.1, 0.5, 1.0);
        assertEquals(8, runes.getActiveRunes().size());

        RuneEffect.RuneInstance first = runes.getActiveRunes().get(0);
        assertTrue(first.alpha() > 0.0);
        assertTrue(first.scale() > 0.0);

        runes.reset();
        assertEquals(0, runes.getActiveRunes().size());
    }

    private static void testItemGlow() {
        ItemGlow glow = new ItemGlow(ItemGlow.RgbColor.CLASSIC_PURPLE, 1.0);
        assertEquals("ItemGlow", glow.getName());

        glow.update(0.5, 0.5, 1.0);
        assertEquals(0.5, glow.getUvOffset(), 1e-6);
        assertTrue(glow.getCurrentIntensity() > 0.0);
    }

    private static void testEnchantParticles() {
        EnchantParticles particles = new EnchantParticles(EnchantParticles.EmitterMode.VORTEX_SWIRL, 50);
        assertEquals("EnchantParticles", particles.getName());

        particles.update(0.2, 0.5, 1.0);
        assertTrue(particles.getParticleCount() > 0);

        int initial = particles.getParticleCount();
        particles.triggerBurst(10);
        assertTrue(particles.getParticleCount() > initial);

        particles.reset();
        assertEquals(0, particles.getParticleCount());
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed: condition was false");
        }
    }

    private static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Expected %.4f but got %.4f", expected, actual));
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(String.format("Expected %s but got %s", expected, actual));
        }
    }
}
