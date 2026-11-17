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

#### Thermal & Leather Armor Insulation
Items tagged `#humannature:insulating_armor` or `#minecraft:freeze_immune_wearables` reduce wetness accumulation up to 65%. Each equipped piece grants a 16.25% reduction:
$$\text{Reduction} = \text{equippedPieces} \times 16.25\%$$
Full insulating armor extends rain soaking time from 20 seconds to ~57 seconds. Custom modpack gear can be added via the `#humannature:insulating_armor` item tag.

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

#### Server (`humannature-server.toml`)

Master module switch under `[general]`:
- `enableTemperature` (boolean, default: `true`): Master toggle for the body temperature and climate module.

Core thresholds and rate scaling under `[temperature]`:

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `temperatureChangeRateMultiplier` | Double | `1.0` (0.1–10.0) | Global multiplier for temperature change rates, wetness accumulation, and heating/cooling speed. |
| `hypothermiaThreshold` | Double | `0.60` (0.1–1.0) | Fraction of max freeze ticks required to trigger hypothermia physical penalties and shivering (60%). |
| `heatExhaustionThreshold` | Double | `39.5` (37.5–43.0) | Core body temperature (°C) triggering heat exhaustion debuffs (panting, slowness). |
| `heatDamageThreshold` | Double | `42.0` (39.0–45.0) | Core body temperature (°C) triggering periodic lethal hyperthermia damage. |

> Baseline physics values (drying duration: 12.5s, thaw multiplier: 5.0x, rain wetness: 0.0025/tick, water wetness: 0.015/tick) are hardcoded constants scaled by `temperatureChangeRateMultiplier`.

#### Client (`humannature-client.toml`)

Configured under `[hud]` and `[camera]`:

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableShiveringCamera` | Boolean | `true` | Shivering screen tremor during hypothermia. |
| `showActionBarEnvironmentCues` | Boolean | `true` | Environmental action bar warnings in ENVIRONMENT mode. |
| `defaultHudMode` | String | `"COMBINED"` | Initial HUD mode (`OFF`, `PHYSIOLOGY`, `ENVIRONMENT`, `INJURY`, `WEIGHT`, `COMBINED`). |
