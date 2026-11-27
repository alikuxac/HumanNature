# Human Nature

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?style=flat-square)](https://minecraft.net/)
[![NeoForge 21.1.77+](https://img.shields.io/badge/NeoForge-21.1.77+-orange.svg?style=flat-square)](https://neoforged.net/)
[![Forge 52.1.20+](https://img.shields.io/badge/Forge-52.1.20+-blue.svg?style=flat-square)](https://files.minecraftforge.net/)
[![JEI 19.21.0.247+ (optional)](https://img.shields.io/badge/JEI-19.21.0.247%2B-9c6b3f.svg?style=flat-square)](docs/JEI.md)
[![JitPack](https://img.shields.io/badge/JitPack-Available-brightgreen.svg?style=flat-square)](https://jitpack.io/#alikuxac/HumanNature)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/Models-CC%20BY--NC--ND%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
[![License: CC0 1.0](https://img.shields.io/badge/Audio-CC0%201.0-green.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/)

**Human Nature** adds injuries, body temperature, carried weight, fatigue, and nutrient reserves to Minecraft **1.21.1**. It supports **NeoForge 21.1.77+** and **Minecraft Forge 52.1.20+**.

## Gameplay

- **Elytra flight risks**: Rapid consecutive firework boosts trigger a brief G-force blackout, while high-speed wall and landing impacts can cause fractures.
- **Falling-block head trauma**: Anvils and other falling blocks can injure your head. Blast Protection on helmets reduces concussion risk.
- **Hard-block mining strain**: Mining very hard blocks can injure your main arm, affecting mining speed, attacks, and bow handling.
- **Deep-cave claustrophobia**: Prolonged exposure to pitch-black conditions below Y=0 can trigger panic and bow or crossbow aim tremors. Light, campfires, music, and nearby pets offer ways to calm down.
- **Recursive container weight**: Shulker Box and Bundle contents count toward your carried load, including nested containers.
- **Potions and enchantments for hauling**: Burden Bearer increases carrying capacity. Load Bearing adds capacity on armor and reduces contents weight on containers.

> [!CAUTION]
> Some textures, icons, and models are placeholders. Development currently focuses on mechanics and balance.

---

## Systems

### 1. 6-Limb Injury & Recovery
- **Head**:
  - Tier 1: periodic tinnitus and disorientation.
  - Tier 2: darkness flashes.
  - Mitigated by Blast Protection helmets.
- **Torso**:
  - Tier 1: increases exhaustion while moving.
  - Tier 2: caps maximum health at 14 HP (7 hearts).
  - Diamond and Netherite chestplates negate Tier 1 hits from blunt attacks.
- **Main Arm**:
  - $-50\%$ attack speed and block breaking speed.
  - $15\%$ chance to drop held item on attack or block break.
  - Limits bow and crossbow draw to partial charge.
  - Triggered by high-speed Elytra crashes or prolonged mining of hard blocks.
- **Off Arm**:
  - Disables offhand slot, offhand item use, and shield blocking.
  - Triggered by heavy blocked hits ($\ge 10$ damage), shield disable attacks, or Elytra crashes.
- **Legs**:
  - Tier 1: $-45\%$ speed, sprinting disabled.
  - Tier 2: $-85\%$ speed, jumping disabled.
  - Safe fall distance scales with Feather Falling boots and higher-tier leggings.
- **Healing Rates**:
  - Standing still: $1\times$ speed.
  - Crouching or sleeping: $2\times$ speed.
  - Moving without splint: pauses recovery.
  - Waking with $\ge 16$ hunger downgrades the worst injury by 1 tier and consumes 4 hunger points.
- **Death**:
  - `keepInventory = false`: resets all injuries on respawn.
  - `keepInventory = true`: respawns with 30 seconds of Weakness I, Hunger I, and Tier 1 leg trauma to prevent suicide healing.
- **Splints**: 5 tiers (Wooden, Reinforced, Golden, Diamond, Netherite). Apply by channeling for 2 seconds (40 ticks) or via diagnostic screen.
- **Dietary Recovery**: Milk, stews, golden apples, and potions cut remaining heal time on consumption.

### 2. Diagnostics UI
- **Diagnostic Screen**: Open with `H` key or the inventory tab button. Shows limb health states (green, yellow, red), lists active penalties, and lets players click a limb to apply a splint from inventory (2-second cooldown).
- **HUD Overlay**: Shows injured limbs and remaining recovery time.

### 3. Temperature
Simulates body temperature based on biome, weather, time of day, nearby heat sources, and armor.

### 4. Weight & Encumbrance
Calculates load across inventory, armor, and container contents against player capacity. Applies tiered penalties to movement speed, water sinking, and fall damage, with capacity-expanding potions and enchantments.

### 5. Physiology
Tracks sleep deprivation and fatigue buildup over time, triggering brief involuntary darkness blinks during motion when reaching severe exhaustion.

---

## JEI Support

With **JEI 19.21.0.247+** installed on the client, open a food's **Uses** view to see its **Nutrition** category: calcium, carbohydrates, and vitamins gained per consumption. Values come from the server's datapacks and update on login and `/reload`.

JEI is optional on **Forge and NeoForge** for Minecraft **1.21.1**. Human Nature works without it. EMI and REI have no native integration and have not been verified with this category.

See [JEI Support](docs/JEI.md) for installation, display limits, and development setup.

---

## Feedback & Support

OP players can run `/humannature feedback` to display the project's Discord, GitHub, and feedback email contacts.

Contact: [Discord](https://discord.gg/tP7uhhQdpP) | [GitHub](https://github.com/alikuxac/HumanNature) | [Email](mailto:hello@alikuxac.xyz)

---

## Documentation
For detailed module specifications, refer to:
- [Extensibility API & Event Pipeline](docs/API.md)
- [JEI Support & Nutrition Display](docs/JEI.md)
- [Advancements & Gameplay Milestones](docs/ADVANCEMENTS.md)
- [Injury System Documentation](docs/INJURY_SYSTEM.md)
- [Physiology System Documentation](docs/PHYSIOLOGY_SYSTEM.md)
- [Temperature System Documentation](docs/TEMPERATURE_SYSTEM.md)
- [Weight & Encumbrance System Documentation](docs/WEIGHT_SYSTEM.md)

---

## Addon Developer API

Human Nature provides a standalone API JAR published to JitPack for third-party mod and addon developers.

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly "com.github.alikuxac.HumanNature:humannature-api:${humannature_version}"
}
```

See [API Documentation](docs/API.md) for accessor interfaces, event bus registration, and KubeJS examples.

---

## Build & Installation

### Requirements
- **JDK**: 21+
- **Gradle**: 8.x+ / 9.x (via Gradle Wrapper `gradlew`)
- **Supported Loaders & Versions**:
  - **NeoForge**: `21.1.77+` (Minecraft `1.21.1`)
  - **Minecraft Forge**: `52.1.20+` (Minecraft `1.21.1`)

### Building from Source
```bash
./gradlew clean build
```
Output JAR files are generated in:
- `neoforge/build/libs/` (`humannature-*+mc1.21.1-neoforge.jar`)
- `forge/build/libs/` (`humannature-*+mc1.21.1-forge.jar`)

Local builds use the API classes directly. The standalone API JAR is built and published by JitPack.

---

## Development

I develop Human Nature with AI assistance, including generated code. I review the code, make the design decisions, and test the mod in-game.

---

## License
- **Code & Project**
  - [![License: LGPL v3](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
  - Licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**. See the [LICENSE](LICENSE) file for details.
- **3D Item Models**
  - [![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
  - Custom 3D item models are licensed under **Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)**.
- **Audio Assets**
  - [![License: CC0 1.0](https://img.shields.io/badge/License-CC0%201.0-green.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/)
  - All sounds are an original, procedurally generated work created for this project and released under **CC0 1.0 Universal** (public domain dedication). No third-party audio samples are used.
