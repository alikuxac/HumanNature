# Human Nature (Minecraft Mod)

**Human Nature** is a survival-realism Minecraft mod for **Forge** and **NeoForge** (1.21.1+). It enhances gameplay depth by introducing realistic human physiology, environmental survival mechanics, and injury systems.

---

## 🌟 Core Features & Modules

### 🦴 1. Injury & Bone Recovery System
- **Leg Fractures**: Falling from heights (>3 blocks) inflicts leg fractures, drastically reducing movement speed (-45% per leg) and disabling jumping capability when both legs are broken.
- **Active Resting & Healing**:
  - **Standing Still**: Standard recovery speed ($1\times$).
  - **Crouching / Sleeping**: Accelerated recovery speed ($2\times$).
  - **Moving (Unsplinted)**: Recovery timer pauses.
- **Splints & Curios Support**: Equipping a Splint (in Offhand or Curios feet/splint slot) allows movement while maintaining recovery.
- **Nutrition & Healing Acceleration**: Drinking milk, eating nutrient-rich stews/soups, golden apples, or health potions instantly reduces remaining fracture recovery time.
- **Client HUD Overlay**: Real-time display showing remaining recovery time and current status (`Resting 2x`, `Standing Still`, `Splinted`, `Paused`).

### 🌡️ 2. Environmental Temperature System
- Dynamic body temperature mechanics influenced by biomes, weather, time of day, block heat sources, and equipment.

### ⚖️ 3. Weight & Inventory Encumbrance
- Realistic weight simulation based on inventory load, affecting stamina consumption and movement speed.

### 🫀 4. Physiology & Survival Metrics
- Core human body survival stats and physiological requirements.

---

## 📚 Documentation
For detailed module specifications, refer to:
- [Injury System Documentation](docs/INJURY_SYSTEM.md)

---

## 🛠️ Build & Installation

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

## 📄 License
This project is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**. See the [LICENSE](LICENSE) file for details.
