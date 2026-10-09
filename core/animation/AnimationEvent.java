// path: core/animation/AnimationEvent.java
package core.animation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Event published during animation playback lifecycle milestones or keyframe markers.
 */
public class AnimationEvent {

    public enum EventType {
        START,
        UPDATE,
        KEYFRAME_REACHED,
        EFFECT_TRIGGER,
        LOOP_RESTARTED,
        COMPLETED
    }

    private final EventType type;
    private final double timestamp;
    private final double value;
    private final String channelName;
    private final Map<String, Object> metadata;

    public AnimationEvent(EventType type, double timestamp, double value, String channelName, Map<String, Object> metadata) {
        this.type = Objects.requireNonNull(type, "EventType cannot be null");
        this.timestamp = timestamp;
        this.value = value;
        this.channelName = channelName != null ? channelName : "default";
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public static AnimationEvent started(double timestamp) {
        return new AnimationEvent(EventType.START, timestamp, 0.0, "root", null);
    }

    public static AnimationEvent update(double timestamp, double value, String channelName) {
        return new AnimationEvent(EventType.UPDATE, timestamp, value, channelName, null);
    }

    public static AnimationEvent keyframeReached(double timestamp, double value, String channelName) {
        return new AnimationEvent(EventType.KEYFRAME_REACHED, timestamp, value, channelName, null);
    }

    public static AnimationEvent effectTrigger(double timestamp, String effectName, Map<String, Object> data) {
        return new AnimationEvent(EventType.EFFECT_TRIGGER, timestamp, 1.0, effectName, data);
    }

    public static AnimationEvent loopRestarted(double timestamp, int loopIndex) {
        Map<String, Object> data = new HashMap<>();
        data.put("loopIndex", loopIndex);
        return new AnimationEvent(EventType.LOOP_RESTARTED, timestamp, 0.0, "root", data);
    }

    public static AnimationEvent completed(double timestamp) {
        return new AnimationEvent(EventType.COMPLETED, timestamp, 1.0, "root", null);
    }

    public EventType getType() {
        return type;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public String getChannelName() {
        return channelName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return String.format("AnimationEvent{type=%s, time=%.3f, value=%.3f, channel='%s'}",
                type, timestamp, value, channelName);
    }
}
