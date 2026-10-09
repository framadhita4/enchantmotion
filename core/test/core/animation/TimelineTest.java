// path: core/test/core/animation/TimelineTest.java
package core.test.core.animation;

import core.animation.AnimationEvent;
import core.animation.Keyframe;
import core.animation.Timeline;

import java.util.concurrent.atomic.AtomicInteger;

public class TimelineTest {

    public static void runAllTests() {
        testSortingAndInterpolation();
        testPlaybackControls();
        testLooping();
        testEventNotifications();
        System.out.println("  ✓ TimelineTest passed");
    }

    private static void testSortingAndInterpolation() {
        Timeline tl = Timeline.of("scale");
        // Add out of order
        tl.addKeyframe(2.0, 20.0);
        tl.addKeyframe(0.0, 0.0);
        tl.addKeyframe(1.0, 10.0);

        assertEquals(2.0, tl.getDuration(), 1e-6);
        assertEquals(0.0, tl.getValueAt(0.0), 1e-6);
        assertEquals(5.0, tl.getValueAt(0.5), 1e-6);
        assertEquals(15.0, tl.getValueAt(1.5), 1e-6);
        assertEquals(20.0, tl.getValueAt(2.0), 1e-6);
        assertEquals(20.0, tl.getValueAt(5.0), 1e-6); // Clamped to last
    }

    private static void testPlaybackControls() {
        Timeline tl = Timeline.of("rot");
        tl.addKeyframe(0.0, 0.0);
        tl.addKeyframe(1.0, 360.0);

        assertEquals(Timeline.PlaybackState.STOPPED, tl.getState());
        tl.play();
        assertEquals(Timeline.PlaybackState.PLAYING, tl.getState());

        tl.update(0.5);
        assertEquals(180.0, tl.getCurrentValue(), 1e-6);
        assertEquals(0.5, tl.getProgress(), 1e-6);

        tl.pause();
        assertEquals(Timeline.PlaybackState.PAUSED, tl.getState());
        tl.update(0.2); // Should not advance while paused
        assertEquals(180.0, tl.getCurrentValue(), 1e-6);

        tl.play();
        tl.update(0.5); // Reaches 1.0 (end)
        assertEquals(Timeline.PlaybackState.COMPLETED, tl.getState());
        assertEquals(360.0, tl.getCurrentValue(), 1e-6);
    }

    private static void testLooping() {
        Timeline tl = Timeline.of("pulse");
        tl.setLoop(true);
        tl.addKeyframe(0.0, 1.0);
        tl.addKeyframe(1.0, 2.0);

        tl.play();
        tl.update(1.25); // Should wrap around to 0.25
        assertEquals(1.25, tl.getCurrentValue(), 1e-6);
        assertEquals(Timeline.PlaybackState.PLAYING, tl.getState());
    }

    private static void testEventNotifications() {
        Timeline tl = Timeline.of("events");
        tl.addKeyframe(0.0, 0.0);
        tl.addKeyframe(1.0, 10.0);

        AtomicInteger eventsReceived = new AtomicInteger(0);
        tl.addListener(event -> {
            if (event.getType() == AnimationEvent.EventType.START ||
                event.getType() == AnimationEvent.EventType.COMPLETED ||
                event.getType() == AnimationEvent.EventType.UPDATE) {
                eventsReceived.incrementAndGet();
            }
        });

        tl.play();
        tl.update(1.0);
        if (eventsReceived.get() < 2) {
            throw new AssertionError("Expected at least 2 events (start, completed)");
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
