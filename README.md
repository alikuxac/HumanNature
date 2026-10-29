# Human Nature

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?style=flat-square)](https://minecraft.net/)
[![NeoForge 21.1.77+](https://img.shields.io/badge/NeoForge-21.1.77+-orange.svg?style=flat-square)](https://neoforged.net/)
[![Forge 52.1.20+](https://img.shields.io/badge/Forge-52.1.20+-blue.svg?style=flat-square)](https://files.minecraftforge.net/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/Models-CC%20BY--NC--ND%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-nd/4.0/)

**Human Nature** is a survival-realism mod for Minecraft 1.21.1 supporting both **NeoForge (21.1.77+)** and **Minecraft Forge (52.1.20+)**. It adds localized skeletal injuries, environmental temperature, inventory encumbrance, and metabolic recovery.

> [!CAUTION]
> **Mechanics First**: Core gameplay systems, mechanics, and balance take priority over visual polish. Textures, icons, and models remain minimal or placeholders while features are actively being built.

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
Scales stamina use and movement speed by carried inventory weight.

### 5. Physiology
Tracks sleep deprivation and fatigue buildup over time, triggering brief involuntary darkness blinks during motion when reaching severe exhaustion.

---

## Documentation
For detailed module specifications, refer to:
- [Injury System Documentation](docs/INJURY_SYSTEM.md)
- [Physiology System Documentation](docs/PHYSIOLOGY_SYSTEM.md)

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
Output JAR files will be generated in `neoforge/build/libs/` and `forge/build/libs/`.

---

## License
- **Code & Project**
  - [![License: LGPL v3](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
  - Licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**. See the [LICENSE](LICENSE) file for details.
- **3D Item Models**
  - [![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
  - Custom 3D item models are licensed under **Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)**.
