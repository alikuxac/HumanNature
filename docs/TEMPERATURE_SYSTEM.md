# Temperature & Moisture System

Tracks wetness accumulation from rain and water, applies clothing insulation, detects nearby heat sources for rapid drying, and drives vanilla freezing when soaked.

---

## Mechanics

### 1. Rain Soaking & Wetness Accumulation

The server tracks moisture each tick on a hidden `wetnessBuffer` gauge (0.0 to 1.0).

| Exposure Type | Condition | Rate | Time to 100% |
| :--- | :--- | :--- | :--- |
| **Water Submersion** | `player.isInWater()` or `player.isUnderWater()` | +0.015 / tick | ~3.3 seconds (67 ticks) |
| **Rain (Helmet Equipped)** | `level.isRainingAt(pos)` | +0.0025 / tick | 20.0 seconds (400 ticks) |
| **Rain (No Helmet)** | `level.isRainingAt(pos)` with empty `HEAD` slot | +0.0050 / tick (2.0x) | 10.0 seconds (200 ticks) |

#### Helmet Protection
- An empty helmet slot doubles (2.0x) rain accumulation.
- Any helmet or wearable in the `HEAD` slot negates this penalty.

#### Leather Armor Insulation
- Items tagged `#minecraft:freeze_immune_wearables` cut wetness accumulation by up to 65%.
- Each equipped piece (helmet, chestplate, leggings, boots) grants a flat 16.25% reduction:
  $$\text{Reduction} = \text{equippedPieces} \times 16.25\%$$
- Wearing full leather armor extends rain soaking time from 20 seconds to ~57 seconds.

---

### 2. Heat Sources & Drying

The server scans blocks within 2 to 3 blocks of the player once every 20 ticks (1 second).

#### Recognized Heat Sources
- **Campfires**: Any block tagged `#minecraft:campfires` with `lit=true`.
- **Active Furnaces**: Furnace, blast furnace, or smoker with `lit=true`.
- **Lava**: Lava source or flowing blocks (`Blocks.LAVA` or fluid tag `#minecraft:lava`).
- **Fire**: Fire and Soul Fire (`Blocks.FIRE`, `Blocks.SOUL_FIRE`).
- **Magma Blocks**: `Blocks.MAGMA_BLOCK`.
- **Direct Burning**: Player on fire (`player.isOnFire()`) or submerged in lava (`player.isInLava()`).

#### Rewarming Rates
- **Fast Drying**: Standing within radius drains `wetnessBuffer` at ~0.004 / tick, clearing 100% wetness in 12.5 seconds (250 ticks).
- **Rapid Thaw**: Decrements vanilla `ticksFrozen` by 10 ticks per tick (5x the natural thaw rate of 2 ticks/tick).
- **Evaporation Sound**: Plays `SoundEvents.FIRE_EXTINGUISH` (volume 0.2, pitch 1.1) every 35 ticks while drying or thawing.

#### Natural Air Drying
Sheltered players out of rain and water dry passively at 0.0005 / tick (~100 seconds to clear full wetness).

---

### 3. Hypothermia & Freezing

When `wetnessBuffer` reaches 1.0 (100% soaked) away from heat sources, hypothermia begins:

#### Vanilla Freezing Integration
- Adds +3 to `player.setTicksFrozen(...)` each tick, capping at `ticksRequiredToFreeze + 80` (220 ticks).
- Triggers vanilla freezing cues:
  - Frosted screen vignette at `ticksFrozen > 0`.
  - Shivering model animation.
  - Frozen hearts and 1 point of freeze damage every 40 ticks once `ticksFrozen >= 140`.

#### Sluggish Stumbling
- When `ticksFrozen >= 70` and moving on ground, the player stumbles every 80 to 120 ticks.
- Applies `Slowness I` (`MobEffects.MOVEMENT_SLOWDOWN`) for 1.25 seconds (25 ticks).

---

### 4. HUD & Diagnostics

#### Swappable HUD Modes
Toggle with the HUD cycle key:
- **`TEMPERATURE` Mode**: Displays dedicated wetness gauge, exposure state (Submerged, Outdoor Rain, Sheltered), heat source proximity, core freezing ticks, and gear insulation percentages.
- **`COMBINED` Mode**: Displays active wetness and drying status alongside physiology and trauma lines. Displays `Temperature: Warm (Dry)` when dry.

#### Diagnostic Screen (`H` Key)
- Lists active temperature tags (`Wet (X%)`, `Near Heat`, `❄ Freezing`).
- Hovering the status row shows detailed tooltip data: current wetness percentage, heat proximity, freeze progress, and armor insulation values.

---

### 5. Configuration Options

Configured in `humannature-server.toml` under `[temperature]`:

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableWetnessAccumulation` | Boolean | `true` | Enables rain soaking and water wetness accumulation |
| `wetnessRainAccumulationRate` | Double | `0.0025` | Base wetness added per tick under rain |
| `wetnessWaterAccumulationRate` | Double | `0.015` | Wetness added per tick while in water |
| `wetnessNoHelmetMultiplier` | Double | `2.0` | Multiplier for rain soaking with an empty helmet slot |
| `wetnessFreezeImmuneReduction` | Double | `0.65` | Maximum wetness reduction from full freeze-immune gear |
| `enableHeatRewarming` | Double | `true` | Enables heat source detection and accelerated drying |
| `heatSourceRadius` | Double | `3.0` | Search radius in blocks for active heat sources |
| `heatDryingTimeSeconds` | Double | `12.5` | Seconds required to dry 100% wetness near heat |
| `heatThawMultiplier` | Double | `5.0` | Thaw rate multiplier for frozen ticks near heat |
| `enableHypothermia` | Boolean | `true` | Enables freeze damage and stumbling at 100% wetness |
