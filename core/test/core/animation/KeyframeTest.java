// path: core/test/core/animation/KeyframeTest.java
package core.test.core.animation;

import core.animation.Keyframe;

public class KeyframeTest {

    public static void runAllTests() {
        testConstruction();
        testLinearInterpolation();
        testEasingCurves();
        testBoundaryConditions();
        System.out.println("  ✓ KeyframeTest passed");
    }

    private static void testConstruction() {
        Keyframe kf = Keyframe.of(1.5, 42.0);
        assertEquals(1.5, kf.getTime(), 1e-6);
        assertEquals(42.0, kf.getValue(), 1e-6);
        assertEquals(Keyframe.Easing.LINEAR, kf.getEasing());

        try {
            new Keyframe(-0.1, 1.0, Keyframe.Easing.LINEAR);
            throw new AssertionError("Should reject negative time");
        } catch (IllegalArgumentException expected) {}
    }

    private static void testLinearInterpolation() {
        Keyframe k1 = Keyframe.of(0.0, 10.0);
        Keyframe k2 = Keyframe.of(2.0, 30.0);

        assertEquals(10.0, k1.interpolateTo(k2, 0.0), 1e-6);
        assertEquals(20.0, k1.interpolateTo(k2, 1.0), 1e-6);
        assertEquals(30.0, k1.interpolateTo(k2, 2.0), 1e-6);
    }

    private static void testEasingCurves() {
        Keyframe k1 = Keyframe.of(0.0, 0.0, Keyframe.Easing.EASE_IN_QUAD);
        Keyframe k2 = Keyframe.of(1.0, 100.0);

        // At midpoint 0.5, t^2 = 0.25 -> value = 25.0
        assertEquals(25.0, k1.interpolateTo(k2, 0.5), 1e-6);

        Keyframe step1 = Keyframe.of(0.0, 0.0, Keyframe.Easing.STEP);
        assertEquals(0.0, step1.interpolateTo(k2, 0.5), 1e-6);
        assertEquals(100.0, step1.interpolateTo(k2, 1.0), 1e-6);
    }

    private static void testBoundaryConditions() {
        Keyframe k1 = Keyframe.of(1.0, 10.0);
        Keyframe k2 = Keyframe.of(3.0, 50.0);

        assertEquals(10.0, k1.interpolateTo(k2, -5.0), 1e-6);
        assertEquals(50.0, k1.interpolateTo(k2, 10.0), 1e-6);
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
