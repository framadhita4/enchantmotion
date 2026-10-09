// path: core/test/TestRunner.java
package core.test;

import core.test.core.animation.EnchantAnimationTest;
import core.test.core.animation.EnchantBrightnessDynamicsTest;
import core.test.core.animation.KeyframeTest;
import core.test.core.animation.TimelineTest;
import core.test.core.effects.EffectsTest;

/**
 * Standalone test suite runner for EnchantMotion Core module.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       Running EnchantMotion Core Test Suite      ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        try {
            KeyframeTest.runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ KeyframeTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            TimelineTest.runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ TimelineTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            EnchantAnimationTest.runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ EnchantAnimationTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            EffectsTest.runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ EffectsTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            EnchantBrightnessDynamicsTest.runAllTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("  ✗ EnchantBrightnessDynamicsTest failed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("==================================================");
        System.out.printf("Test Results: %d passed, %d failed%n", passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
