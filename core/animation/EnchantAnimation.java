// path: core/animation/EnchantAnimation.java
package core.animation;

import core.effects.EnchantParticles;
import core.effects.EnchantmentEffect;
import core.effects.ItemGlow;
import core.effects.RuneEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Master animation orchestrator coordinating multi-channel timelines and procedural visual effects.
 */
public class EnchantAnimation {

    private final String id;
    private final Map<String, Timeline> channels = new LinkedHashMap<>();
    private final List<EnchantmentEffect> effects = new ArrayList<>();
    private final List<Consumer<AnimationEvent>> listeners = new ArrayList<>();

    private boolean running = false;
    private double currentProgress = 0.0;

    public EnchantAnimation(String id) {
        this.id = Objects.requireNonNull(id, "Animation ID cannot be null");
    }

    public static EnchantAnimation of(String id) {
        return new EnchantAnimation(id);
    }

    /**
     * Creates a pre-configured enchanting table burst animation preset.
     */
    public static EnchantAnimation createEnchantTablePreset() {
        EnchantAnimation anim = new EnchantAnimation("enchanting_table_burst");

        // Scale channel: quick squash then pop
        Timeline scaleTimeline = Timeline.of("scale")
                .addKeyframe(0.0, 1.0)
                .addKeyframe(0.3, 0.85, Keyframe.Easing.EASE_IN_QUAD)
                .addKeyframe(0.8, 1.35, Keyframe.Easing.EASE_OUT_BOUNCE)
                .addKeyframe(1.5, 1.0, Keyframe.Easing.EASE_IN_OUT_CUBIC);

        // Rotation channel: full 360 degree spin
        Timeline rotTimeline = Timeline.of("rotation")
                .addKeyframe(0.0, 0.0)
                .addKeyframe(1.5, 360.0, Keyframe.Easing.EASE_IN_OUT_CUBIC);

        // Intensity channel: crescendo towards completion
        Timeline intensityTimeline = Timeline.of("intensity")
                .addKeyframe(0.0, 0.2)
                .addKeyframe(0.9, 1.0, Keyframe.Easing.EASE_IN_QUAD)
                .addKeyframe(1.5, 0.4, Keyframe.Easing.EASE_OUT_QUAD);

        anim.addChannel(scaleTimeline);
        anim.addChannel(rotTimeline);
        anim.addChannel(intensityTimeline);

        anim.addEffect(new RuneEffect(16, 1.4, 2.5));
        anim.addEffect(new ItemGlow(ItemGlow.RgbColor.CLASSIC_PURPLE, 1.2));
        anim.addEffect(new EnchantParticles(EnchantParticles.EmitterMode.VORTEX_SWIRL, 80));

        return anim;
    }

    /**
     * Creates a continuous ambient glint animation preset for held or equipped items.
     */
    public static EnchantAnimation createItemGlintPreset() {
        EnchantAnimation anim = new EnchantAnimation("ambient_item_glint");

        Timeline glowTimeline = Timeline.of("intensity")
                .setLoop(true)
                .addKeyframe(0.0, 0.3)
                .addKeyframe(1.0, 0.9, Keyframe.Easing.EASE_IN_OUT_QUAD)
                .addKeyframe(2.0, 0.3, Keyframe.Easing.EASE_IN_OUT_QUAD);

        anim.addChannel(glowTimeline);
        anim.addEffect(new ItemGlow(ItemGlow.RgbColor.CLASSIC_PURPLE, 0.8));
        return anim;
    }

    public String getId() {
        return id;
    }

    public EnchantAnimation addChannel(Timeline timeline) {
        Objects.requireNonNull(timeline, "Timeline cannot be null");
        channels.put(timeline.getChannelName(), timeline);
        timeline.addListener(this::dispatchSubEvent);
        return this;
    }

    public Timeline getChannel(String name) {
        return channels.get(name);
    }

    public double getChannelValue(String name, double defaultValue) {
        Timeline tl = channels.get(name);
        return tl != null ? tl.getCurrentValue() : defaultValue;
    }

    public EnchantAnimation addEffect(EnchantmentEffect effect) {
        Objects.requireNonNull(effect, "Effect cannot be null");
        effects.add(effect);
        effect.initialize(this);
        return this;
    }

    public List<EnchantmentEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    @SuppressWarnings("unchecked")
    public <T extends EnchantmentEffect> T getEffect(Class<T> effectClass) {
        for (EnchantmentEffect effect : effects) {
            if (effectClass.isInstance(effect)) {
                return (T) effect;
            }
        }
        return null;
    }

    public void start() {
        this.running = true;
        channels.values().forEach(Timeline::play);
        dispatchEvent(AnimationEvent.started(0.0));
    }

    public void pause() {
        this.running = false;
        channels.values().forEach(Timeline::pause);
    }

    public void resume() {
        this.running = true;
        channels.values().forEach(Timeline::play);
    }

    public void stop() {
        this.running = false;
        channels.values().forEach(Timeline::stop);
        effects.forEach(EnchantmentEffect::reset);
    }

    public void reset() {
        stop();
        channels.values().forEach(Timeline::reset);
        this.currentProgress = 0.0;
    }

    /**
     * Updates all animation channels and procedural effects by elapsed time delta.
     */
    public void update(double deltaSeconds) {
        if (!running || deltaSeconds <= 0.0) {
            return;
        }

        boolean allCompleted = true;
        double maxProgress = 0.0;

        for (Timeline timeline : channels.values()) {
            timeline.update(deltaSeconds);
            if (timeline.isPlaying() || timeline.isLooping()) {
                allCompleted = false;
            }
            maxProgress = Math.max(maxProgress, timeline.getProgress());
        }

        this.currentProgress = maxProgress;
        double intensity = getChannelValue("intensity", 0.5);

        for (EnchantmentEffect effect : effects) {
            effect.update(deltaSeconds, currentProgress, intensity);
        }

        if (allCompleted && !channels.isEmpty()) {
            this.running = false;
            dispatchEvent(AnimationEvent.completed(getMaxDuration()));
        }
    }

    public double getMaxDuration() {
        return channels.values().stream()
                .mapToDouble(Timeline::getDuration)
                .max()
                .orElse(0.0);
    }

    public double getProgress() {
        return currentProgress;
    }

    public boolean isRunning() {
        return running;
    }

    public void addListener(Consumer<AnimationEvent> listener) {
        listeners.add(Objects.requireNonNull(listener, "Listener cannot be null"));
    }

    private void dispatchEvent(AnimationEvent event) {
        for (Consumer<AnimationEvent> listener : listeners) {
            listener.accept(event);
        }
        for (EnchantmentEffect effect : effects) {
            effect.onAnimationEvent(event);
        }
    }

    private void dispatchSubEvent(AnimationEvent event) {
        dispatchEvent(event);
    }
}
