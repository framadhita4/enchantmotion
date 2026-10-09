// path: core/effects/EnchantParticles.java
package core.effects;

import core.animation.AnimationEvent;
import core.animation.EnchantAnimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages physics-based enchanted particle simulations (vortex spirals, bursts, and glint specks).
 */
public class EnchantParticles implements EnchantmentEffect {

    public enum EmitterMode {
        VORTEX_SWIRL,
        RADIAL_BURST,
        AMBIENT_FLOAT
    }

    public static class Particle {
        public double x, y, z;
        public double vx, vy, vz;
        public double age;
        public final double maxLifetime;
        public double scale;
        public double alpha;

        public Particle(double x, double y, double z, double vx, double vy, double vz, double maxLifetime, double scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.age = 0.0;
            this.maxLifetime = maxLifetime;
            this.scale = scale;
            this.alpha = 1.0;
        }

        public boolean isAlive() {
            return age < maxLifetime;
        }

        public void step(double deltaSeconds, double drag) {
            age += deltaSeconds;
            x += vx * deltaSeconds;
            y += vy * deltaSeconds;
            z += vz * deltaSeconds;

            vx *= Math.pow(drag, deltaSeconds * 20.0);
            vy *= Math.pow(drag, deltaSeconds * 20.0);
            vz *= Math.pow(drag, deltaSeconds * 20.0);

            // Fade out towards end of life
            double lifeProgress = age / maxLifetime;
            alpha = Math.max(0.0, 1.0 - lifeProgress);
        }
    }

    private boolean enabled = true;
    private EmitterMode mode = EmitterMode.VORTEX_SWIRL;
    private int maxParticles = 64;
    private double spawnInterval = 0.05; // seconds between spawns
    private double spawnTimer = 0.0;
    private double drag = 0.92;
    private final Random random = new Random(42);

    private final List<Particle> particles = new ArrayList<>();

    public EnchantParticles() {}

    public EnchantParticles(EmitterMode mode, int maxParticles) {
        this.mode = mode;
        this.maxParticles = maxParticles;
    }

    @Override
    public String getName() {
        return "EnchantParticles";
    }

    @Override
    public void initialize(EnchantAnimation animation) {
        reset();
    }

    @Override
    public void update(double deltaSeconds, double progress, double intensity) {
        if (!enabled) {
            particles.clear();
            return;
        }

        // 1. Advance existing particles
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.step(deltaSeconds, drag);
            if (!p.isAlive()) {
                iterator.remove();
            }
        }

        // 2. Spawn new particles based on intensity
        spawnTimer += deltaSeconds;
        double adjustedInterval = spawnInterval / Math.max(0.1, intensity);
        while (spawnTimer >= adjustedInterval && particles.size() < maxParticles) {
            spawnTimer -= adjustedInterval;
            spawnParticle(intensity);
        }
    }

    private void spawnParticle(double intensity) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = 0.8 + random.nextDouble() * 0.4;
        double px = Math.cos(angle) * radius;
        double pz = Math.sin(angle) * radius;
        double py = (random.nextDouble() - 0.5) * 0.2;

        double vx, vy, vz;
        if (mode == EmitterMode.VORTEX_SWIRL) {
            // Tangential velocity + upward lift towards center
            double speed = 1.2 * intensity;
            vx = -Math.sin(angle) * speed - px * 0.5;
            vz = Math.cos(angle) * speed - pz * 0.5;
            vy = 0.6 + random.nextDouble() * 0.4;
        } else if (mode == EmitterMode.RADIAL_BURST) {
            double speed = 2.0 + random.nextDouble() * 1.5;
            vx = Math.cos(angle) * speed;
            vz = Math.sin(angle) * speed;
            vy = (random.nextDouble() - 0.2) * 1.0;
        } else {
            vx = (random.nextDouble() - 0.5) * 0.2;
            vz = (random.nextDouble() - 0.5) * 0.2;
            vy = 0.1 + random.nextDouble() * 0.2;
        }

        double lifetime = 0.8 + random.nextDouble() * 0.6;
        double scale = 0.08 + random.nextDouble() * 0.06;
        particles.add(new Particle(px, py, pz, vx, vy, vz, lifetime, scale));
    }

    public void triggerBurst(int count) {
        for (int i = 0; i < count && particles.size() < maxParticles * 2; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi = (random.nextDouble() - 0.5) * Math.PI;
            double speed = 1.5 + random.nextDouble() * 2.0;

            double vx = speed * Math.cos(phi) * Math.cos(theta);
            double vy = speed * Math.sin(phi) + 0.5;
            double vz = speed * Math.cos(phi) * Math.sin(theta);

            particles.add(new Particle(0, 0.2, 0, vx, vy, vz, 1.2, 0.12));
        }
    }

    @Override
    public void onAnimationEvent(AnimationEvent event) {
        if (event.getType() == AnimationEvent.EventType.COMPLETED) {
            triggerBurst(30);
        }
    }

    @Override
    public void reset() {
        particles.clear();
        spawnTimer = 0.0;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    public int getParticleCount() {
        return particles.size();
    }
}
