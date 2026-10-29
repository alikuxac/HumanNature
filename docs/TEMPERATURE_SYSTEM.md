# Temperature & Moisture System

Tracks wetness from rain and water, gear insulation, nearby heat sources, and freezing penalties.

---

## Mechanics

### 1. Wetness Accumulation

The server tracks moisture per tick on `wetnessBuffer` (0.0 to 1.0).

| Exposure | Condition | Rate | Time to 100% |
| :--- | :--- | :--- | :--- |
| **Water Submersion** | `player.isInWater()` or `player.isUnderWater()` | +0.015 / tick | ~3.3s (67 ticks) |
| **Rain (Helmet Equipped)** | `level.isRainingAt(pos)` | +0.0025 / tick | 20.0s (400 ticks) |
| **Rain (No Helmet)** | `level.isRainingAt(pos)` with empty `HEAD` slot | +0.0050 / tick (2.0x) | 10.0s (200 ticks) |

#### Helmet Protection
An empty helmet slot doubles rain accumulation (2.0x). Any helmet or wearable in the `HEAD` slot prevents this penalty.

#### Leather Armor Insulation
Items tagged `#minecraft:freeze_immune_wearables` reduce wetness accumulation up to 65%. Each equipped piece grants 16.25% reduction:
$$\text{Reduction} = \text{equippedPieces} \times 16.25\%$$
Full leather armor extends rain soaking time from 20 seconds to ~57 seconds.

---

### 2. Heat Sources & Drying

The server scans blocks within 3 blocks of the player every 20 ticks (1 second).

#### Heat Sources
- **Campfires**: Block tagged `#minecraft:campfires` with `lit=true`.
- **Furnaces**: Furnace, blast furnace, or smoker with `lit=true`.
- **Lava**: Lava source or flowing blocks (`Blocks.LAVA` or fluid tag `#minecraft:lava`).
- **Fire**: Fire and Soul Fire (`Blocks.FIRE`, `Blocks.SOUL_FIRE`).
- **Magma Blocks**: `Blocks.MAGMA_BLOCK`.
- **Direct Burning**: `player.isOnFire()` or `player.isInLava()`.

#### Rewarming Rates
- **Heat Drying**: Drains `wetnessBuffer` at ~0.004 / tick (12.5 seconds to dry 100%).
- **Rapid Thaw**: Decrements vanilla `ticksFrozen` by 10 ticks per tick (5x natural thaw rate).
- **Sound**: Plays `SoundEvents.FIRE_EXTINGUISH` every 35 ticks while drying or thawing near heat.
- **Air Drying**: Sheltered players out of rain and water dry passively at 0.0005 / tick (~100s for full drying).

---

### 3. Hot Stews & Internal Warming

Eating hot stew (`Mushroom Stew`, `Rabbit Stew`, `Beetroot Soup`, `Suspicious Stew`, or items tagged `#c:foods/soups` and `#humannature:hot_stews`):
- Cuts `wetnessBuffer` by 50% immediately.
- Clears all `ticksFrozen` (`player.setTicksFrozen(0)`).
- Grants `Internal Warming` for 80 seconds (1600 ticks).
- Blocks freeze accumulation while exposed to rain during the buff duration.

---

### 4. Hypothermia & Freezing Penalties

When `wetnessBuffer` reaches 1.0 (100% soaked) away from heat sources, hypothermia accumulates vanilla `ticksFrozen` (+3 per tick, capped at `ticksRequiredToFreeze + 80`).

Once `ticksFrozen` reaches 60% of `ticksRequiredToFreeze` (84/140 ticks by default):
- **Mining Fatigue I**: Applied continuously while hypothermic.
- **Physical Penalties**: -35% attack speed (`Attributes.ATTACK_SPEED`) and -1.0 block interaction range (`Attributes.BLOCK_INTERACTION_RANGE`).
- **Camera Tremor**: High-frequency screen shivering on client (`enableShiveringCamera`).
- **Cold Stumble**: Periodic stumble every 15 to 20 seconds while walking on ground (brief `Slowness I` and freeze sound).

Penalties clear as soon as `ticksFrozen` drops below 60%.

---

### 5. HUD & Diagnostics

#### Swappable HUD Modes
- **`ENVIRONMENT` Mode**: Minimalist Action Bar cue. Shows `[HUD Mode]: ENVIRONMENT` on switch, then outputs `Wetness: X% | Temp: Normal/Chilly/Freezing` only when wet, cold, or buffed. Auto-hides when dry and warm.
- **`COMBINED` Mode**: Displays active wetness progress bar and temperature state inside the top-left HUD box alongside physiology and trauma lines.
- **`OFF` / `PHYSIOLOGY` / `INJURY`**: Hides temperature lines.

#### Diagnostic Screen (`H` Key)
- Quick overview panel displays warning tags (`Wetness: Damp/Soaked`, `Freezing`, `Internal Warming`) when abnormal.
- Detailed temperature and wetness rows with exact percentages and stage indicators appear in the body metrics breakdown.

---

### 6. Configuration Options

Configured in `humannature-server.toml` under `[temperature]`:

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableWetnessAccumulation` | Boolean | `true` | Enables rain soaking and water wetness accumulation |
| `wetnessRainAccumulationRate` | Double | `0.0025` | Base wetness added per tick under rain |
| `wetnessWaterAccumulationRate` | Double | `0.015` | Wetness added per tick while in water |
| `wetnessNoHelmetMultiplier` | Double | `2.0` | Multiplier for rain soaking with empty helmet slot |
| `wetnessFreezeImmuneReduction` | Double | `0.65` | Maximum wetness reduction from full freeze-immune gear |
| `enableHeatRewarming` | Boolean | `true` | Enables heat source detection and rapid drying |
| `heatSourceRadius` | Double | `3.0` | Search radius in blocks for active heat sources |
| `heatDryingTimeSeconds` | Double | `12.5` | Seconds required to dry 100% wetness near heat |
| `heatThawMultiplier` | Double | `5.0` | Thaw rate multiplier for frozen ticks near heat |
| `enableHypothermia` | Boolean | `true` | Enables freeze accumulation when 100% soaked |
| `internalWarmingDurationTicks` | Integer | `1600` | Duration of internal warming buff from hot stews (80s) |
| `hypothermiaPenaltyThreshold` | Double | `0.60` | Fraction of max freeze ticks triggering debuffs |
| `hypothermiaMiningFatigue` | Boolean | `true` | Applies Mining Fatigue I during hypothermia |
| `hypothermiaAttackSpeedPenalty` | Double | `0.35` | Attack speed penalty fraction during hypothermia |
| `hypothermiaInteractionRangePenalty` | Double | `1.0` | Block reach penalty during hypothermia (blocks) |

Configured in `humannature-client.toml` under `[hud]` and `[camera]`:

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableShiveringCamera` | Boolean | `true` | Enables screen shivering tremor during hypothermia |
| `showActionBarEnvironmentCues` | Boolean | `true` | Enables action bar cues in ENVIRONMENT mode |
| `defaultHudMode` | String | `"COMBINED"` | Initial HUD mode (`OFF`, `PHYSIOLOGY`, `ENVIRONMENT`, `INJURY`, `COMBINED`) |
