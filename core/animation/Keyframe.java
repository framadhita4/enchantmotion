// path: core/animation/Keyframe.java
package core.animation;

import java.util.Objects;

/**
 * Represents a single point in time along an animation timeline with an associated value
 * and easing function for interpolating towards the next keyframe.
 */
public class Keyframe implements Comparable<Keyframe> {

    /**
     * Standard easing functions for natural motion curves.
     */
    public enum Easing {
        LINEAR {
            @Override
            public double apply(double t) {
                return t;
            }
        },
        EASE_IN_QUAD {
            @Override
            public double apply(double t) {
                return t * t;
            }
        },
        EASE_OUT_QUAD {
            @Override
            public double apply(double t) {
                return t * (2.0 - t);
            }
        },
        EASE_IN_OUT_QUAD {
            @Override
            public double apply(double t) {
                return t < 0.5 ? 2.0 * t * t : -1.0 + (4.0 - 2.0 * t) * t;
            }
        },
        EASE_IN_CUBIC {
            @Override
            public double apply(double t) {
                return t * t * t;
            }
        },
        EASE_OUT_CUBIC {
            @Override
            public double apply(double t) {
                double f = t - 1.0;
                return f * f * f + 1.0;
            }
        },
        EASE_IN_OUT_CUBIC {
            @Override
            public double apply(double t) {
                return t < 0.5 ? 4.0 * t * t * t : (t - 1.0) * (2.0 * t - 2.0) * (2.0 * t - 2.0) + 1.0;
            }
        },
        EASE_OUT_BOUNCE {
            @Override
            public double apply(double t) {
                final double n1 = 7.5625;
                final double d1 = 2.75;
                if (t < 1.0 / d1) {
                    return n1 * t * t;
                } else if (t < 2.0 / d1) {
                    double f = t - (1.5 / d1);
                    return n1 * f * f + 0.75;
                } else if (t < 2.5 / d1) {
                    double f = t - (2.25 / d1);
                    return n1 * f * f + 0.9375;
                } else {
                    double f = t - (2.625 / d1);
                    return n1 * f * f + 0.984375;
                }
            }
        },
        STEP {
            @Override
            public double apply(double t) {
                return t >= 1.0 ? 1.0 : 0.0;
            }
        };

        public abstract double apply(double t);
    }

    private final double time;
    private final double value;
    private final Easing easing;

    public Keyframe(double time, double value, Easing easing) {
        if (time < 0.0) {
            throw new IllegalArgumentException("Keyframe time cannot be negative: " + time);
        }
        if (!Double.isFinite(time) || !Double.isFinite(value)) {
            throw new IllegalArgumentException("Keyframe time and value must be finite numbers");
        }
        this.time = time;
        this.value = value;
        this.easing = Objects.requireNonNull(easing, "Easing function cannot be null");
    }

    public static Keyframe of(double time, double value) {
        return new Keyframe(time, value, Easing.LINEAR);
    }

    public static Keyframe of(double time, double value, Easing easing) {
        return new Keyframe(time, value, easing);
    }

    public double getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

    public Easing getEasing() {
        return easing;
    }

    /**
     * Interpolates between this keyframe and the subsequent keyframe at the target timestamp.
     *
     * @param next the next keyframe
     * @param targetTime the query timestamp in seconds
     * @return the interpolated value
     */
    public double interpolateTo(Keyframe next, double targetTime) {
        Objects.requireNonNull(next, "Next keyframe cannot be null");
        if (targetTime <= this.time) {
            return this.value;
        }
        if (targetTime >= next.time) {
            return next.value;
        }

        double duration = next.time - this.time;
        if (duration <= 1e-9) {
            return next.value;
        }

        double linearProgress = (targetTime - this.time) / duration;
        double easedProgress = this.easing.apply(linearProgress);
        return this.value + easedProgress * (next.value - this.value);
    }

    @Override
    public int compareTo(Keyframe o) {
        return Double.compare(this.time, o.time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keyframe keyframe = (Keyframe) o;
        return Double.compare(keyframe.time, time) == 0 &&
               Double.compare(keyframe.value, value) == 0 &&
               easing == keyframe.easing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, value, easing);
    }

    @Override
    public String toString() {
        return String.format("Keyframe{time=%.3f, value=%.3f, easing=%s}", time, value, easing);
    }
}
