<p align="center">
  <img src="assets/icon.png" width="160" height="160" alt="EnchantMotion Icon" style="border-radius: 24px;" />
</p>

<h1 align="center">⚡ EnchantMotion</h1>

<p align="center">
  <strong>A cinematic visual overhaul and animation engine for Minecraft enchanting.</strong>
</p>

<p align="center">
  <a href="https://minecraft.net/"><img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg" alt="Minecraft 1.21.1" /></a>
  <a href="https://neoforged.net/"><img src="https://img.shields.io/badge/NeoForge-21.1.65+-blue.svg" alt="NeoForge" /></a>
  <a href="https://fabricmc.net/"><img src="https://img.shields.io/badge/Fabric-0.16.5+-orange.svg" alt="Fabric" /></a>
  <a href="https://minecraft.net/"><img src="https://img.shields.io/badge/Bedrock-1.21+-red.svg" alt="Bedrock Add-on" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-LGPL--3.0-blue.svg" alt="LGPL-3.0 License" /></a>
</p>

---

## 📖 Overview

**EnchantMotion** transforms Minecraft's static enchanting table into a dynamic, physically responsive arcane workstation. With real-time 3D item levitation, orbiting Lapis Lazuli rings, kinetic impact recoil, high-frequency energy vibrations, progressive brightness overlays, and particle vortex bursts, enchanting weapons and armor feels truly magical.

Built with a clean decoupled architecture, EnchantMotion natively supports **NeoForge (Java)**, **Fabric (Java)**, and **Bedrock Edition Add-ons**.

---

## ✨ Features & Visual Physics

- **🗡️ Dynamic Levitating Item:** Tools and weapons smoothly ascend from the ancient book pages upon interaction using Hermite easing, gently bobbing in mid-air.
- **💎 3D Orbiting Lapis Lazuli:** Placed Lapis Lazuli circles the floating item in synchronized 3D celestial orbits. The number of active orbiting gems dynamically scales with bookshelf power (Tiers 1, 2, and 3).
- **⚡ Resonant Power Hum:** High-frequency electric micro-vibrations surge through the weapon, intensifying in amplitude and speed as the enchantment charges.
- **💥 Sequential Kinetic Dash & Recoil:** Lapis gems dash inward one by one to infuse the weapon. Upon impact, the item suffers directional knockback, tilt, and elastic squash-and-stretch physics.
- **🌟 Progressive Texture Illumination & Climax Swell:** The item seamlessly charges from standard lighting to 100% luminous celestial white overlay with flash spikes, concluding in an expansive climax swell before the enchantment binds.
- **🌌 Standard Galactic Alphabet (SGA) Runes:** Mystical orbiting glyph rings and swirling particle vortexes complete the visual experience.
- **🛡️ 100% Client-Side Safe:** Purely visual and atmospheric. Safe to use when connecting to vanilla servers, realms, or modpacks without requiring server installation.

---

## 📁 Project Structure

```text
EnchantMotion/
│
├── assets/
│   └── icon.png                      # 1024x1024 Master mod logo & emblem
│
├── core/
│   ├── animation/
│   │   ├── EnchantAnimation.java     # Master orchestrator combining timelines and effects
│   │   ├── Timeline.java             # Multi-keyframe interpolation and playback manager
│   │   ├── Keyframe.java             # Keyframe points with linear, quad, cubic, and bounce easing
│   │   └── AnimationEvent.java       # Lifecycle and trigger event dispatching
│   │
│   ├── effects/
│   │   ├── EnchantmentEffect.java    # Core visual effect interface
│   │   ├── RuneEffect.java           # Standard Galactic Alphabet (SGA) orbital rings
│   │   ├── ItemGlow.java             # UV sweep glint, pulse luminosity, and color auras
│   │   └── EnchantParticles.java     # Vortex swirl and completion burst particle simulation
│   │
│   └── test/                         # Unit test suite verifying interpolation and physics
│
├── java/
│   ├── common/                       # Minecraft Java shared abstraction layer
│   │   └── src/main/java/com/enchantmentanimation/common/
│   ├── fabric/                       # Fabric Mod Loader implementation
│   │   └── src/main/java/com/enchantmentanimation/fabric/
│   └── neoforge/                     # NeoForge Mod Loader implementation
│       └── src/main/java/com/enchantmentanimation/neoforge/
│
└── bedrock/
    ├── resource_pack/                # Bedrock client textures, materials, and particle emitters
    │   ├── manifest.json
    │   ├── pack_icon.png
    │   ├── particles/
    │   │   ├── enchant_runes.particle.json
    │   │   └── enchant_glow.particle.json
    │   └── render_controllers/
    │       └── item_glint.render_controllers.json
    │
    └── behavior_pack/                # Bedrock entity triggers and animation controllers
        ├── manifest.json
        ├── pack_icon.png
        └── animation_controllers/
            └── enchant_trigger.animation_controllers.json
```

---

## 🏛️ Architectural Highlights

1. **Decoupled Pure-Java Core (`core/`)**:
   - Zero Minecraft/loader dependencies.
   - 100% deterministic timeline calculations and physics simulation.
   - Comprehensive unit test suite included.
2. **Multi-Loader Java Support (`java/`)**:
   - **`common/`**: Shared logic, mod constants, and configuration abstractions.
   - **`fabric/`**: Fabric client/server entrypoints and mixin hooks.
   - **`neoforge/`**: NeoForge mod registration, event listeners, and custom block entity rendering.
3. **Bedrock Add-on Support (`bedrock/`)**:
   - Standard Bedrock `manifest.json` files with valid UUID linkages.
   - Custom Molang parametric equations for orbiting rune particles.
   - Custom animation controllers for item-in-hand trigger states.

---

## 🛠️ Verification & Compilation

### Run the Core Test Suite

```bash
# Compile and run unit tests with Java 21
javac --release 21 -d /tmp/ea_build $(find core/animation core/effects core/test -name "*.java")
java -cp /tmp/ea_build core.test.TestRunner
```

### Compile Java Modules (Common, Fabric, NeoForge)

```bash
javac --release 21 \
  -cp "$(find ~/.m2 ~/.gradle -name '*slf4j-api*.jar' | head -n 1):/tmp/ea_build" \
  -d /tmp/ea_mods $(find java -name "*.java")
```

---

## 🎮 Installation

### Java Edition (NeoForge & Fabric)

1. Download the corresponding `.jar` for your mod loader from the releases page (or CurseForge / Modrinth).
2. Place the jar into your Minecraft `.minecraft/mods` folder.
3. (For Fabric) Ensure [Fabric API](https://modrinth.com/mod/fabric-api) is also present in your `mods` folder.

### Bedrock Edition

1. Compress `bedrock/resource_pack` contents into `enchantmotion_rp.mcpack`.
2. Compress `bedrock/behavior_pack` contents into `enchantmotion_bp.mcpack`.
3. Open each `.mcpack` file to automatically import into Minecraft Bedrock Edition, then activate both under your world settings.

---

## 📜 License

This project is open-source and licensed under the [GNU Lesser General Public License v3.0 (LGPL-3.0)](LICENSE). Modpack authors are welcome to include EnchantMotion in their packs!
