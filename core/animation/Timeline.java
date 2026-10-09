// path: core/animation/Timeline.java
package core.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages an ordered sequence of Keyframes for a specific animated property channel.
 */
public class Timeline {

    public enum PlaybackState {
        STOPPED,
        PLAYING,
        PAUSED,
        COMPLETED
    }

    private final String channelName;
    private final List<Keyframe> keyframes = new ArrayList<>();
    private final List<Consumer<AnimationEvent>> listeners = new ArrayList<>();

    private double currentTime = 0.0;
    private double speed = 1.0;
    private boolean loop = false;
    private int loopCount = 0;
    private PlaybackState state = PlaybackState.STOPPED;

    public Timeline(String channelName) {
        this.channelName = Objects.requireNonNull(channelName, "Channel name cannot be null");
    }

    public static Timeline of(String channelName) {
        return new Timeline(channelName);
    }

    public String getChannelName() {
        return channelName;
    }

    public Timeline addKeyframe(double time, double value) {
        return addKeyframe(Keyframe.of(time, value));
    }

    public Timeline addKeyframe(double time, double value, Keyframe.Easing easing) {
        return addKeyframe(Keyframe.of(time, value, easing));
    }

    public Timeline addKeyframe(Keyframe keyframe) {
        Objects.requireNonNull(keyframe, "Keyframe cannot be null");
        keyframes.add(keyframe);
        Collections.sort(keyframes);
        return this;
    }

    public List<Keyframe> getKeyframes() {
        return Collections.unmodifiableList(keyframes);
    }

    public Timeline setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public boolean isLooping() {
        return loop;
    }

    public Timeline setSpeed(double speed) {
        if (speed <= 0.0) {
            throw new IllegalArgumentException("Timeline playback speed must be positive: " + speed);
        }
        this.speed = speed;
        return this;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDuration() {
        if (keyframes.isEmpty()) {
            return 0.0;
        }
        return keyframes.get(keyframes.size() - 1).getTime();
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public PlaybackState getState() {
        return state;
    }

    public boolean isPlaying() {
        return state == PlaybackState.PLAYING;
    }

    public void play() {
        if (state == PlaybackState.STOPPED || state == PlaybackState.COMPLETED) {
            currentTime = 0.0;
            notifyListeners(AnimationEvent.started(currentTime));
        }
        this.state = PlaybackState.PLAYING;
    }

    public void pause() {
        if (state == PlaybackState.PLAYING) {
            this.state = PlaybackState.PAUSED;
        }
    }

    public void stop() {
        this.state = PlaybackState.STOPPED;
        this.currentTime = 0.0;
    }

    public void reset() {
        this.currentTime = 0.0;
        this.loopCount = 0;
        this.state = PlaybackState.STOPPED;
    }

    public void seek(double time) {
        this.currentTime = Math.max(0.0, time);
        double duration = getDuration();
        if (currentTime >= duration && duration > 0.0) {
            if (loop) {
                currentTime = currentTime % duration;
            } else {
                currentTime = duration;
                state = PlaybackState.COMPLETED;
            }
        }
    }

    /**
     * Advances playback by delta seconds.
     *
     * @param deltaSeconds elapsed time step in seconds
     */
    public void update(double deltaSeconds) {
        if (state != PlaybackState.PLAYING || deltaSeconds <= 0.0) {
            return;
        }

        currentTime += deltaSeconds * speed;
        double duration = getDuration();

        if (duration <= 0.0) {
            state = PlaybackState.COMPLETED;
            notifyListeners(AnimationEvent.completed(currentTime));
            return;
        }

        if (currentTime >= duration) {
            if (loop) {
                loopCount++;
                currentTime = currentTime % duration;
                notifyListeners(AnimationEvent.loopRestarted(currentTime, loopCount));
            } else {
                currentTime = duration;
                state = PlaybackState.COMPLETED;
                notifyListeners(AnimationEvent.completed(currentTime));
            }
        }

        notifyListeners(AnimationEvent.update(currentTime, getCurrentValue(), channelName));
    }

    /**
     * Evaluates the interpolated value at the current playback time.
     */
    public double getCurrentValue() {
        return getValueAt(currentTime);
    }

    /**
     * Evaluates the interpolated value at a specific timestamp.
     */
    public double getValueAt(double queryTime) {
        if (keyframes.isEmpty()) {
            return 0.0;
        }
        if (keyframes.size() == 1 || queryTime <= keyframes.get(0).getTime()) {
            return keyframes.get(0).getValue();
        }

        Keyframe last = keyframes.get(keyframes.size() - 1);
        if (queryTime >= last.getTime()) {
            return last.getValue();
        }

        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe current = keyframes.get(i);
            Keyframe next = keyframes.get(i + 1);

            if (queryTime >= current.getTime() && queryTime <= next.getTime()) {
                return current.interpolateTo(next, queryTime);
            }
        }

        return last.getValue();
    }

    public double getProgress() {
        double duration = getDuration();
        if (duration <= 0.0) {
            return 1.0;
        }
        return Math.min(1.0, Math.max(0.0, currentTime / duration));
    }

    public void addListener(Consumer<AnimationEvent> listener) {
        listeners.add(Objects.requireNonNull(listener, "Listener cannot be null"));
    }

    public void removeListener(Consumer<AnimationEvent> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(AnimationEvent event) {
        for (Consumer<AnimationEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
