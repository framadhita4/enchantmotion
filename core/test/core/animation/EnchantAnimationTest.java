// path: core/test/core/animation/EnchantAnimationTest.java
package core.test.core.animation;

import core.animation.EnchantAnimation;
import core.animation.Keyframe;
import core.animation.Timeline;
import core.effects.ItemGlow;
import core.effects.RuneEffect;

public class EnchantAnimationTest {

    public static void runAllTests() {
        testOrchestratorChannels();
        testPresets();
        System.out.println("  ✓ EnchantAnimationTest passed");
    }

    private static void testOrchestratorChannels() {
        EnchantAnimation anim = EnchantAnimation.of("custom_anim");
        Timeline scale = Timeline.of("scale").addKeyframe(0.0, 1.0).addKeyframe(1.0, 2.0);
        Timeline alpha = Timeline.of("alpha").addKeyframe(0.0, 0.0).addKeyframe(2.0, 1.0);

        anim.addChannel(scale);
        anim.addChannel(alpha);

        assertEquals(2.0, anim.getMaxDuration(), 1e-6);

        anim.start();
        assertTrue(anim.isRunning());

        anim.update(0.5);
        assertEquals(1.5, anim.getChannelValue("scale", 0.0), 1e-6);
        assertEquals(0.25, anim.getChannelValue("alpha", 0.0), 1e-6);

        anim.update(1.5); // Reaches 2.0 (end)
        assertTrue(!anim.isRunning());
    }

    private static void testPresets() {
        EnchantAnimation tablePreset = EnchantAnimation.createEnchantTablePreset();
        assertEquals("enchanting_table_burst", tablePreset.getId());
        assertTrue(tablePreset.getEffect(RuneEffect.class) != null);
        assertTrue(tablePreset.getEffect(ItemGlow.class) != null);

        EnchantAnimation glintPreset = EnchantAnimation.createItemGlintPreset();
        assertEquals("ambient_item_glint", glintPreset.getId());
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
