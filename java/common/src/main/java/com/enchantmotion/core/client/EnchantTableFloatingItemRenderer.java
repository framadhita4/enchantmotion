// path: java/common/src/main/java/com/enchantmentanimation/core/client/EnchantTableFloatingItemRenderer.java
package com.enchantmentanimation.core.client;

import com.enchantmentanimation.core.EnchantTableItemHolder;
import com.enchantmentanimation.core.EnchantTableRenderStateAccess;
import com.enchantmentanimation.core.EnchantmentAnimationCore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Dedicated renderer for the floating item and orbiting lapis lazuli above the Enchanting Table.
 * Shared across all mod loaders via the core package.
 * Features:
 * - Resonant power hum (high-frequency electric energy vibration)
 * - Upward levitation and climax swell
 * - Punchy directional lapis recoil with physical squash & stretch
 * - Linear 0% to 100% texture brightness charging
 * - Steady ambient orientation (no distracting spin overriding force physics)
 */
public final class EnchantTableFloatingItemRenderer {

    private EnchantTableFloatingItemRenderer() {}

    public static void render(
            EnchantTableRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState) {
        if (!(state instanceof EnchantTableRenderStateAccess access) || !access.enchantmotion$hasItem()) {
            return;
        }

        float open = Mth.clamp(state.open, 0.0F, 1.0F);
        // If the book is closed, the item is completely tucked inside the book
        if (open <= 0.001F || access.enchantmotion$getItemState().isEmpty()) {
            return;
        }

        float time = state.time;
        // Smooth hermite easing for natural opening/closing motion
        float ease = open * open * (3.0F - 2.0F * open);

        // Damped vertical bobbing amplitude as it enters/exits book
        float bob = (float) Math.sin(time * 0.08F) * 0.05F * ease;

        // Ascend from the book pages (0.82F) up to the floating height (1.38F + bob)
        float startY = EnchantmentAnimationCore.ITEM_BASE_START_Y;
        float targetY = EnchantmentAnimationCore.ITEM_BASE_TARGET_Y + bob;
        float baseFloatingY = startY + (targetY - startY) * ease;
        float itemRenderY = baseFloatingY;

        // Scale grows smoothly from 0 to full size 0.5F
        float currentScale = EnchantmentAnimationCore.ITEM_SCALE * ease;

        // Gentle swirl effect during transition
        float transitionSpin = (1.0F - ease) * 120.0F;
        float currentRot = (time * 3.0F) + transitionSpin;

        // Check if an enchantment animation is actively playing
        boolean isEnchanting = access.enchantmotion$isEnchanting();
        float enchantProgress = access.enchantmotion$getEnchantProgress();
        int enchantTier = Math.max(1, access.enchantmotion$getEnchantTier());

        // Determine active level of the enchantment table based on bookshelves (cross-platform)
        Level level = Minecraft.getInstance().level;
        int tableLevel = 1;
        if (level != null && state.blockPos != null) {
            tableLevel = EnchantmentAnimationCore.calculateBookshelfLevel(level, state.blockPos);
        }

        // Determine placed lapis count
        int placedLapis = access.enchantmotion$getLapisCount();
        if (placedLapis <= 0 && level != null && state.blockPos != null) {
            if (level.getBlockEntity(state.blockPos) instanceof EnchantTableItemHolder holder) {
                placedLapis = holder.enchantmotion$getLapis().getCount();
            }
        }

        int lapisCount = Math.min(placedLapis, tableLevel);
        // When enchanting, use the exact tier of the enchantment (1 to 3 lapis)
        int activeLapis = isEnchanting ? Math.min(3, Math.max(1, enchantTier)) : lapisCount;

        // Dashing and Recoil timing parameters:
        // Lapis dashes sequentially 1 by 1 across the window [0.20, 0.80]
        float dashPhaseStart = 0.20F;
        float dashPhaseEnd = 0.80F;
        float totalDashDuration = dashPhaseEnd - dashPhaseStart;
        float slotDuration = activeLapis > 0 ? (totalDashDuration / activeLapis) : totalDashDuration;
        float dashDuration = slotDuration * 0.65F;

        // Directional force recoil displacement, tilt, squash & stretch
        float recoilX = 0.0F;
        float recoilZ = 0.0F;
        float recoilY = 0.0F;
        float recoilTiltX = 0.0F;
        float recoilTiltZ = 0.0F;
        float impactFlashBonus = 0.0F;
        float totalSquash = 1.0F;

        // Resonant Power Hum (high-frequency electric micro-vibrations)
        float humX = 0.0F;
        float humY = 0.0F;
        float humZ = 0.0F;

        if (isEnchanting && enchantProgress >= 0.0F && enchantProgress < 1.0F) {
            float p = enchantProgress;

            // 1. Resonant Power Hum: micro-vibrations accelerate and intensify as charge builds
            float humFreq = 25.0F + p * 45.0F;
            float humAmp = 0.003F + (p * p) * 0.012F;
            humX = (float) Math.sin(time * humFreq) * humAmp;
            humZ = (float) Math.cos(time * (humFreq * 1.15F)) * humAmp;
            humY = (float) Math.sin(time * (humFreq * 0.85F)) * (humAmp * 0.6F);

            // 2. Sequential Lapis Impact Recoil, Directional Tilt & Physical Squash
            if (activeLapis > 0) {
                for (int i = 0; i < activeLapis; i++) {
                    float dashStart = dashPhaseStart + i * slotDuration;
                    float dashImpact = dashStart + dashDuration;

                    if (p >= dashImpact) {
                        float dt = p - dashImpact;
                        float recoilDuration = 0.16F;
                        if (dt < recoilDuration) {
                            float u = dt / recoilDuration; // 0.0 -> 1.0
                            float decay = (float) Math.exp(-u * 4.5F); // Damped spring decay
                            float spring = (float) Math.sin(u * Math.PI * 2.5F);

                            // Angle from which this lapis launched
                            float timeAtDash = (time - (p - dashStart) * 72.0F);
                            float launchAngleDeg = (timeAtDash * 2.2F) + (360.0F / activeLapis) * i;
                            float launchAngleRad = launchAngleDeg * Mth.DEG_TO_RAD;

                            // Force direction vector points from incoming lapis towards center: (-cos, -sin)
                            float forceMag = 0.35F * decay * spring;
                            recoilX += -Mth.cos(launchAngleRad) * forceMag;
                            recoilZ += -Mth.sin(launchAngleRad) * forceMag;
                            recoilY += -0.10F * decay * (float) Math.sin(u * Math.PI * 3.0F);

                            // Physical squash & stretch upon impact
                            float squashVal = 0.22F * decay * (float) Math.sin(u * Math.PI * 2.0F);
                            totalSquash *= (1.0F + squashVal);

                            // Angular tilt: the impact jars the item back away from the incoming vector
                            float tiltForce = 24.0F * decay * spring;
                            recoilTiltX += -Mth.sin(launchAngleRad) * tiltForce;
                            recoilTiltZ += Mth.cos(launchAngleRad) * tiltForce;

                            // Flash spike upon impact
                            impactFlashBonus = Math.max(impactFlashBonus, 0.45F * (float) Math.exp(-u * 6.0F));
                        }
                    }
                }
            }
        }

        int currentLight = state.lightCoords;
        int currentOverlay = OverlayTexture.NO_OVERLAY;

        if (isEnchanting && enchantProgress >= 0.0F && enchantProgress < 1.0F) {
            float p = enchantProgress;

            // 1. Progressive Power Illumination: light coordinates blend up to full bright (15, 15)
            int baseBlock = LightCoordsUtil.block(state.lightCoords);
            int baseSky = LightCoordsUtil.sky(state.lightCoords);
            int lightBlock = (int) (baseBlock + (15 - baseBlock) * p);
            int lightSky = (int) (baseSky + (15 - baseSky) * p);
            currentLight = LightCoordsUtil.pack(lightBlock, lightSky);

            // 2. Texture brightness overlay scales linearly from 0.0F (0%) to 1.0F (100%)
            float totalBrightness = Mth.clamp(p + impactFlashBonus, 0.0F, 1.0F);
            if (totalBrightness > 0.01F) {
                currentOverlay = OverlayTexture.pack(totalBrightness, false);
            }

            // 3. Smooth upward levitation as energy infuses into the item (+0.25 blocks)
            float levitation = (p * p * (3.0F - 2.0F * p)) * 0.25F;
            itemRenderY = baseFloatingY + levitation + recoilY + humY;

            // 4. Physical scale: kinetic impact squash + pre-detonation climax swell at p >= 0.88
            currentScale *= totalSquash;
            if (p >= 0.88F) {
                float swell = (p - 0.88F) / 0.10F;
                currentScale *= (1.0F + swell * 0.25F);
            }
        }

        // 1. Render the main tool with resonant power hum, levitation, and directional recoil
        poseStack.pushPose();
        poseStack.translate(0.5F + recoilX + humX, itemRenderY, 0.5F + recoilZ + humZ);

        // Directional tilt around world axes from lapis impact
        if (recoilTiltX != 0.0F || recoilTiltZ != 0.0F) {
            rotate(poseStack, Axis.XP.rotationDegrees(recoilTiltX));
            rotate(poseStack, Axis.ZP.rotationDegrees(recoilTiltZ));
        }

        // Standard gentle ambient rotation (no fast spin overriding force physics)
        rotate(poseStack, Axis.YP.rotationDegrees(currentRot));
        poseStack.scale(currentScale, currentScale, currentScale);

        access.enchantmotion$getItemState().submit(
                poseStack,
                submitNodeCollector,
                currentLight,
                currentOverlay,
                0
        );
        poseStack.popPose();

        // 2. Render orbiting or sequentially dashing lapis lazuli
        if (activeLapis <= 0) {
            return;
        }

        ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
        if (resolver == null) {
            return;
        }

        ItemStackRenderState lapisState = new ItemStackRenderState();
        resolver.updateForTopItem(
                lapisState,
                new ItemStack(Items.LAPIS_LAZULI),
                ItemDisplayContext.FIXED,
                level,
                null,
                0
        );

        if (lapisState.isEmpty()) {
            return;
        }

        float orbitRadius = 0.38F * ease;
        float lapisScale = 0.28F * ease;

        for (int i = 0; i < activeLapis; i++) {
            float curRadius = orbitRadius;
            float curLapisY = baseFloatingY;
            float curLapisScale = lapisScale;
            int lapisLight = currentLight;
            int lapisOverlay = OverlayTexture.NO_OVERLAY;
            float angleDeg;

            // Celestial vertical bobbing wave for this orbiting lapis
            float lapisWave = (float) Math.sin(time * 0.1F + i * (Mth.TWO_PI / activeLapis)) * 0.04F * ease;

            if (isEnchanting && enchantProgress >= 0.0F && enchantProgress < 1.0F) {
                float p = enchantProgress;
                float dashStart = dashPhaseStart + i * slotDuration;
                float dashImpact = dashStart + dashDuration;

                // If lapis has already impacted the item, it is absorbed into the item!
                if (p >= dashImpact) {
                    continue;
                }

                // If actively dashing inward 1 by 1:
                if (p >= dashStart) {
                    float timeAtDash = time - (p - dashStart) * 72.0F;
                    angleDeg = (timeAtDash * 2.2F) + (360.0F / activeLapis) * i;

                    float u = (p - dashStart) / dashDuration; // 0.0 -> 1.0
                    float dashEase = u * u * u; // Aggressive inward acceleration
                    curRadius = orbitRadius * (1.0F - dashEase);
                    curLapisScale = lapisScale * (1.0F + 0.35F * (float) Math.sin(u * Math.PI));
                    lapisOverlay = OverlayTexture.pack(Math.min(1.0F, 0.4F + u * 0.6F), false);

                    // Interpolate height towards the floating item's center
                    float startLapisY = baseFloatingY + lapisWave;
                    curLapisY = Mth.lerp(u, startLapisY, itemRenderY);
                } else {
                    // Orbiting peacefully before its turn to dash (completely independent of item recoil)
                    angleDeg = (time * 2.2F) + (360.0F / activeLapis) * i;
                    curLapisY = baseFloatingY + lapisWave;
                    lapisOverlay = OverlayTexture.pack(p * 0.5F, false);
                }
            } else {
                angleDeg = (time * 2.2F) + (360.0F / activeLapis) * i;
                curLapisY = baseFloatingY + lapisWave;
            }

            float angleRad = angleDeg * Mth.DEG_TO_RAD;
            float lapisX = 0.5F + Mth.cos(angleRad) * curRadius;
            float lapisZ = 0.5F + Mth.sin(angleRad) * curRadius;

            poseStack.pushPose();
            poseStack.translate(lapisX, curLapisY, lapisZ);

            // Dynamic rotation and celestial tilt
            rotate(poseStack, Axis.YP.rotationDegrees(time * 3.5F + i * 45.0F));
            rotate(poseStack, Axis.XP.rotationDegrees(15.0F));
            rotate(poseStack, Axis.ZP.rotationDegrees((float) Math.sin(time * 0.08F + i) * 10.0F));

            poseStack.scale(curLapisScale, curLapisScale, curLapisScale);

            lapisState.submit(
                    poseStack,
                    submitNodeCollector,
                    lapisLight,
                    lapisOverlay,
                    0
            );
            poseStack.popPose();
        }
    }

    /**
     * Cross-version quaternion rotation helper.
     * In Minecraft 26.2 and earlier, PoseStack had mulPose(Quaternionfc).
     * In Minecraft 26.3+, PoseStack removed mulPose(Quaternionfc) in favor of rotate(Quaternionfc).
     * Invoking poseStack.last().rotate(quaternion) is supported across both 26.2 and 26.3+ without NoSuchMethodError.
     */
    private static void rotate(PoseStack poseStack, org.joml.Quaternionfc quaternion) {
        poseStack.last().rotate(quaternion);
    }
}
