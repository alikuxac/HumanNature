# Human Nature

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?style=flat-square)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange.svg?style=flat-square)](https://neoforged.net/)
[![Forge](https://img.shields.io/badge/Loader-Forge-blue.svg?style=flat-square)](https://files.minecraftforge.net/)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.html)
[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/Models-CC%20BY--NC--ND%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-nd/4.0/)

**Human Nature** is a survival-realism Minecraft mod for **Forge** and **NeoForge** (1.21.1+). It enhances gameplay depth by introducing realistic human physiology, environmental survival mechanics, and injury systems.

---

## Core Features & Modules

### 1. 6-Limb Skeletal Trauma & Recovery System
- **Comprehensive Anatomical Tracking**:
  - **Head**: Tier 1 causes periodic tinnitus/disorientation; Tier 2 triggers flashing darkness. Mitigated by Blast Protection helmets.
  - **Torso**: Tier 1 increases metabolic food exhaustion; Tier 2 caps maximum health to 7 Hearts (14 HP). Mitigated by Diamond/Netherite chestplates.
  - **Arms**: $-30\%$ Attack Speed when fractured.
  - **Legs**: Tier 1 reduces speed by $-45\%$ and locks sprinting; Tier 2 causes severe $-85\%$ slow, disabled jumping, and forced crawling pose (`Pose.SWIMMING`).
- **Mitigation & Thresholds**: Feather Falling boots and higher-tier leggings increase safe fall distance.
- **Active Resting & Metabolic Healing**:
  - **Standing Still**: Standard recovery speed ($1\times$).
  - **Crouching / Sleeping**: Accelerated recovery speed ($2\times$).
  - **Moving (Unsplinted)**: Recovery timer pauses.
  - **Sleep Trauma Downgrade**: Waking with full hunger ($\ge 16$ points) downgrades the worst injury by 1 tier at the cost of 4 hunger points.
- **Fair Death & Anti-Suicide Rules**:
  - `keepInventory = false`: Full recovery and negative status cleansing on death.
  - `keepInventory = true`: Applies a 30-second penalty (Weakness, Hunger, Tier 1 leg sprain) to prevent suicide exploits.
- **Multi-Tier Splints**: 5 tiers (Wooden, Reinforced, Golden, Diamond, Netherite) usable via right-click channeling (2 seconds) or GUI first-aid.
- **Nutritional Acceleration**: Milk, stews, golden apples, and potions provide instant percentage recovery cuts.

### 2. Diagnostic GUI & HUD Overlays
- **Dedicated Diagnostic Screen**:
  - Accessible via **`H`** hotkey or an **inventory tab button**.
  - Interactive 6-limb color-coded silhouette (Healthy 🟢, Minor 🟡, Severe 🔴).
  - Quick-treat injured limbs using any splint found in the player's inventory, protected by a 2-second cooldown.
  - Seamless navigation with a return button (`← Inventory`) and **`E`** key shortcut.
- **Client HUD Overlay**: Real-time display showing affected limbs, current recovery status, and remaining healing time.

### 3. Environmental Temperature System
- Dynamic body temperature mechanics influenced by biomes, weather, time of day, block heat sources, and equipment.

### 4. Weight & Inventory Encumbrance
- Realistic weight simulation based on inventory load, affecting stamina consumption and movement speed.

### 5. Physiology & Survival Metrics
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
