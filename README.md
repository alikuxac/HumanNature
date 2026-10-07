# Human Nature

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?style=flat-square)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange.svg?style=flat-square)](https://neoforged.net/)
[![Forge](https://img.shields.io/badge/Loader-Forge-blue.svg?style=flat-square)](https://files.minecraftforge.net/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/Models-CC%20BY--NC--ND%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-nd/4.0/)

**Human Nature** is a survival-realism Minecraft mod for **Forge** and **NeoForge** (1.21.1+). It enhances gameplay depth by introducing realistic human physiology, environmental survival mechanics, and injury systems.

---

## Core Features & Modules

### 1. Injury & Bone Recovery System
- **Two-Tier Leg Fractures**:
  - **Tier 1 (Single Leg Broken)**: $-45\%$ movement speed, limping.
  - **Tier 2 (Both Legs Broken)**: $-85\%$ movement speed, disabled jumping, forced crawling pose (`Pose.SWIMMING`).
- **Near-Death Shock**: Fall damage resulting in $\le 1.0$ HP ($0.5$ heart) triggers Darkness, forced item drop, and heartbeat sound effects.
- **Active Resting & Healing**:
  - **Standing Still**: Standard recovery speed ($1\times$).
  - **Crouching / Sleeping**: Accelerated recovery speed ($2\times$).
  - **Moving (Unsplinted)**: Recovery timer pauses.
- **Multi-Tier Splints & Right-Click Treatment**: Holding right-click for 2 seconds (40 ticks) performs treatment with `UseAnim.BRUSH`, wool particles, and leather sound effects across 5 tiers (Wooden, Reinforced, Golden, Diamond, Netherite).
- **Nutrition & Healing Acceleration**: Drinking milk, eating nutrient-rich stews/soups, golden apples, or health potions instantly reduces remaining fracture recovery time.
- **Client HUD Overlay**: Real-time display showing remaining recovery time and current status (`Resting 2x`, `Standing Still`, `Paused`).

### 2. Environmental Temperature System
- Dynamic body temperature mechanics influenced by biomes, weather, time of day, block heat sources, and equipment.

### 3. Weight & Inventory Encumbrance
- Realistic weight simulation based on inventory load, affecting stamina consumption and movement speed.

### 4. Physiology & Survival Metrics
- Core human body survival stats and physiological requirements.

---

## Documentation
For detailed module specifications, refer to:
- [Injury System Documentation](docs/INJURY_SYSTEM.md)

---

## Build & Installation

### Requirements
- **JDK**: 21+
- **Gradle**: 8.x+ (via Gradle Wrapper `gradlew`)
- **Supported Loaders**: NeoForge & Forge (Minecraft 1.21.1)

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
