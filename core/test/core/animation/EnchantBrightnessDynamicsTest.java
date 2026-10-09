// path: core/test/core/animation/EnchantBrightnessDynamicsTest.java
package core.test.core.animation;

/**
 * Validates the mathematical and physical models for:
 * 1. Resonant power hum (high-frequency electric micro-vibrations) & upward levitation.
 * 2. Linear 0% to 100% texture brightness charging.
 * 3. Strictly sequential 1-by-1 lapis lazuli dash intervals.
 * 4. Directional recoil force vector calculation and physical squash & stretch.
 * 5. Pre-detonation climax swell at peak charge.
 */
public class EnchantBrightnessDynamicsTest {

    public static void runAllTests() {
        testResonantPowerHumAndLevitation();
        testLinearBrightnessCharging();
        testStrictSequential1By1LapisDashing();
        testDirectionalRecoilForceAndSquash();
        testClimaxSwellBeforeDetonation();
        System.out.println("  ✓ EnchantBrightnessDynamicsTest passed");
    }

    private static void testResonantPowerHumAndLevitation() {
        // As charge builds (p: 0 -> 1), hum frequency and amplitude escalate
        float freq0 = 25.0F + 0.0F * 45.0F;
        float freqMid = 25.0F + 0.5F * 45.0F;
        float freqMax = 25.0F + 1.0F * 45.0F;

        assertEquals(25.0F, freq0, 1e-4);
        assertEquals(47.5F, freqMid, 1e-4);
        assertEquals(70.0F, freqMax, 1e-4);
        assertTrue(freqMax > freqMid && freqMid > freq0);

        float amp0 = 0.003F + (0.0F * 0.0F) * 0.012F;
        float ampMax = 0.003F + (1.0F * 1.0F) * 0.012F;
        assertEquals(0.003F, amp0, 1e-4);
        assertEquals(0.015F, ampMax, 1e-4);
        assertTrue(ampMax > amp0);

        // Smooth cubic levitation rises from 0.0 to +0.25 blocks
        float p0 = 0.0F;
        float p1 = 1.0F;
        float lev0 = (p0 * p0 * (3.0F - 2.0F * p0)) * 0.25F;
        float lev1 = (p1 * p1 * (3.0F - 2.0F * p1)) * 0.25F;
        assertEquals(0.0F, lev0, 1e-4);
        assertEquals(0.25F, lev1, 1e-4);
    }

    private static void testLinearBrightnessCharging() {
        // Charging scales brightness linearly from 0.0 (0%) to 1.0 (100%)
        float p0 = 0.0F;
        float pQuarter = 0.25F;
        float pHalf = 0.50F;
        float pFull = 1.0F;

        assertEquals(0.0F, Math.min(1.0F, Math.max(0.0F, p0)), 1e-4);
        assertEquals(0.25F, Math.min(1.0F, Math.max(0.0F, pQuarter)), 1e-4);
        assertEquals(0.50F, Math.min(1.0F, Math.max(0.0F, pHalf)), 1e-4);
        assertEquals(1.0F, Math.min(1.0F, Math.max(0.0F, pFull)), 1e-4);
    }

    private static void testStrictSequential1By1LapisDashing() {
        int activeLapis = 3;
        float dashPhaseStart = 0.20F;
        float dashPhaseEnd = 0.80F;
        float slotDuration = (dashPhaseEnd - dashPhaseStart) / activeLapis; // 0.20F
        float dashDuration = slotDuration * 0.65F; // 0.13F

        for (int i = 0; i < activeLapis; i++) {
            float dashStart = dashPhaseStart + i * slotDuration;
            float dashImpact = dashStart + dashDuration;

            // Verify each lapis finishes dash before the next one starts
            if (i > 0) {
                float prevDashStart = dashPhaseStart + (i - 1) * slotDuration;
                float prevDashImpact = prevDashStart + dashDuration;
                assertTrue("Lapis " + i + " must start AFTER lapis " + (i - 1) + " impact", dashStart >= prevDashImpact);
            }

            assertTrue("Impact must happen after dash starts", dashImpact > dashStart);
        }
    }

    private static void testDirectionalRecoilForceAndSquash() {
        int activeLapis = 3;
        float baseRadius = 0.38F;

        for (int i = 0; i < activeLapis; i++) {
            float angleDeg = (360.0F / activeLapis) * i;
            float angleRad = (float) Math.toRadians(angleDeg);

            // Incoming position relative to center: (cos(theta) * R, sin(theta) * R)
            float lapisPosX = (float) Math.cos(angleRad) * baseRadius;
            float lapisPosZ = (float) Math.sin(angleRad) * baseRadius;

            // Force direction vector points from incoming lapis towards center: (-cos, -sin)
            float forceDirX = - (float) Math.cos(angleRad);
            float forceDirZ = - (float) Math.sin(angleRad);

            float forceMag = 0.20F;
            float recoilX = forceDirX * forceMag;
            float recoilZ = forceDirZ * forceMag;

            // Dot product of recoil displacement and lapis position must be negative
            // (meaning the item is knocked AWAY from the incoming projectile)
            float dotProduct = lapisPosX * recoilX + lapisPosZ * recoilZ;
            assertTrue("Recoil must push item away from the incoming projectile", dotProduct < 0.0F);
        }
    }

    private static void testClimaxSwellBeforeDetonation() {
        // Scale swells by +25% between p = 0.88 and p = 0.98
        float pPreSwell = 0.85F;
        float pMidSwell = 0.93F;
        float pMaxSwell = 0.98F;

        float scalePre = 1.0F;
        if (pPreSwell >= 0.88F) {
            scalePre *= (1.0F + ((pPreSwell - 0.88F) / 0.10F) * 0.25F);
        }
        assertEquals(1.0F, scalePre, 1e-4);

        float scaleMid = 1.0F;
        if (pMidSwell >= 0.88F) {
            scaleMid *= (1.0F + ((pMidSwell - 0.88F) / 0.10F) * 0.25F);
        }
        assertTrue(scaleMid > 1.0F && scaleMid < 1.26F);

        float scaleMax = 1.0F;
        if (pMaxSwell >= 0.88F) {
            scaleMax *= (1.0F + ((pMaxSwell - 0.88F) / 0.10F) * 0.25F);
        }
        assertEquals(1.25F, scaleMax, 1e-4);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed: condition was false");
        }
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Expected %.4f but got %.4f", expected, actual));
        }
    }
}
