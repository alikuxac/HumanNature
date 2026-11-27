# Weight & Encumbrance System

Items in the inventory, armor slots, offhand and containers add to carried weight. Heavy loads slow movement, pull the player down in water and increase fall damage. Potions and enchantments raise carrying capacity.

---

## Mechanics

### 1. Weight Calculation

The server calculates total carry weight across inventory, armor, and offhand slots.

$$\text{Total Weight} = \sum \text{Stack Weight} + \sum \text{Contained Item Weight}$$

| Category | Tag / Condition | Base Weight | Stack (64x) |
| :--- | :--- | :--- | :--- |
| **Heavy Blocks** | `#c:heavy_blocks`, `#humannature:heavy_blocks` (stone, cobble, ores, obsidian, anvils) | 0.8 kg | 51.2 kg |
| **Metallic Items** | `#c:metallic_items`, `#humannature:metallic_items` (ingots, raw metals, chains, armor) | 0.4 kg | 25.6 kg |
| **Default Items** | Unclassified general items | 0.05 kg | 3.2 kg |
| **Light Items** | `#c:light_items`, `#humannature:light_items` (feathers, seeds, paper, sticks) | 0.005 kg | 0.32 kg |

#### Nested Containers
Shulker boxes and bundles evaluate `DataComponents.CONTAINER` and `DataComponents.BUNDLE_CONTENTS` recursively:
- Contained items add directly to the total carried weight.
- Shulker boxes and bundles accept the `Load Bearing` enchantment (`#humannature:enchantable/load_bearing`).
- Each level of `Load Bearing` on a container cuts its contents weight by $20\%$ (`LOAD_BEARING_CONTAINER_REDUCTION = 0.20`), up to $60\%$ at Level III.
- Base container multiplier is $1.0\times$ (`WEIGHT_CONTAINER_RATIO`).

---

### 2. Encumbrance Tiers & Penalties

Carried weight is compared against player maximum capacity:

$$\text{Weight Ratio} = \frac{\text{Current Weight}}{\text{Max Capacity}}$$

| Tier | Ratio Threshold | Speed Penalty | Water Sink Pull | Fall Damage | Action Bar Cue |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Normal** | $< 0.50$ | 0% | 0 | 1.00x | None |
| **Light** | $0.50 - 0.70$ | -10% | +0.015 / tick | 1.25x | None |
| **Medium** | $0.70 - 0.85$ | -25% | +0.035 / tick | 1.50x | None |
| **Overburdened** | $\ge 0.85$ | -50% | +0.065 / tick | 2.00x | `⚠ Overburdened: -50% Speed` |

- **Speed Penalty**: Applied as a persistent attribute modifier on `Attributes.MOVEMENT_SPEED`.
- **Water Sink Pull**: Heavier loads pull players downward in water each tick.
- **Fall Damage**: Scales landing damage with momentum and carried weight.

---

### 3. Capacity Boosts

Base capacity is **500.0 kg**, plus potion and enchantment bonuses:

$$\text{Max Capacity} = \text{Base} (500\text{kg}) + \text{Burden Bearer Bonus} + \text{Load Bearing Bonus}$$

#### Mob Effect: Burden Bearer (`humannature:burden_bearer`)
Temporarily adds **+150.0 kg per level** to carrying capacity.

| Potion Variant | Duration | Amplifier | Capacity Bonus |
| :--- | :--- | :--- | :--- |
| **Burden Bearer** | 3:00 | Level I | +150.0 kg |
| **Long Burden Bearer** | 8:00 | Level I | +150.0 kg |
| **Strong Burden Bearer** | 1:30 | Level II | +300.0 kg |

**Brewing Recipes**:
- `Awkward Potion` + `Armadillo Scute` or `Iron Ingot` $\rightarrow$ `Potion of Burden Bearing`
- `Potion of Burden Bearing` + `Redstone Dust` $\rightarrow$ `Long Potion of Burden Bearing`
- `Potion of Burden Bearing` + `Glowstone Dust` $\rightarrow$ `Strong Potion of Burden Bearing`

#### Enchantment: Load Bearing (`humannature:load_bearing`)
Max level III. Applies to armor and containers (`#humannature:enchantable/load_bearing`):
- **Armor**: $+50.0\text{ kg}$ capacity per level per piece (full Level III set = $+600.0\text{ kg}$, bringing total to **1100.0 kg**).
- **Containers**: Cuts contents weight by $20\%$ per level on Shulker Boxes and Bundles (up to $-60\%$ at Level III).

---

### 4. UI & Diagnostics

#### HUD Modes (`WEIGHT` & `COMBINED`)
- **`WEIGHT` Mode** shows:
  - Header: `[WEIGHT & ENCUMBRANCE]`
  - 12-segment progress bar with tier color coding (`[██████░░░░░░] 391.0/800kg (49%)`).
  - Current tier and penalties.
  - Load breakdown: `Load: Inv XX.Xkg | Armor XX.Xkg`.
  - Active capacity buffs: `Buffs: +XXXkg bonus capacity`.
- **`COMBINED` Mode** uses `Weight: %s/%skg (%s %s)` (e.g. `Weight: 391.0/800.0kg (Normal 49%)`).

#### Auto-Hide Healthy HUD (`ClientConfig.AUTO_HIDE_HEALTHY_HUD`)
Hides the overlay when carry load is in the `NORMAL` or `LIGHT` tier (ratio $< 0.70$) and other vitals are healthy.

#### Item Tooltips
Item tooltips show:
- Single item: `Weight: 0.8 kg`
- Stack: `Weight: 51.2 kg (0.8 kg each)`
- Containers: `Weight: 1383.2 kg (Contents: 1382.4 kg)`
- Hold `Shift` to show total load: ` (Total: 49% - Light)`

#### Diagnostic Screen
The Diagnostic Screen shows weight, capacity, limb health and vital signs in 7 languages.

---

### 5. Configuration

#### Server (`humannature-server.toml`)

`enableWeight` under `[general]` enables inventory weight and encumbrance (default: `true`).

Core keys under `[weight]`:

| Key | Default | Description |
| :--- | :--- | :--- |
| `maxCarryCapacityKg` | `500.0` (10.0–10000.0) | Baseline maximum carrying capacity in kilograms before encumbrance penalties apply. |
| `weightPenaltyMultiplier` | `1.0` (0.0–10.0) | Global multiplier for encumbrance penalties (movement speed reduction, water sinking pull, and fall damage). Set to `0.0` to disable movement penalties while keeping weight tracking active. |

#### Data Tags & Item Weights
Item weights are managed through JSON data tags rather than config files:
- `#c:heavy_blocks` / `#humannature:heavy_blocks`: 0.8 kg/item (anvil, stone, iron blocks).
- `#c:metallic_items` / `#humannature:metallic_items`: 0.4 kg/item (ingots, armor, metal tools).
- `#c:light_items` / `#humannature:light_items`: 0.005 kg/item (feathers, paper, string).
- Default items: 0.05 kg/item fallback.

Encumbrance thresholds (Light: 50%, Medium: 70%, Overburdened: 85%) and capacity bonuses (Burden Bearer: +150 kg/lvl, Load Bearing: +50 kg/lvl) are internal constants. All debuff severities scale with `weightPenaltyMultiplier`.

#### Client (`humannature-client.toml`)

| Key | Default | Description |
| :--- | :--- | :--- |
| `enableWeightTooltip` | `true` | Show unit and stack weight in item tooltips. |
| `showActionBarWeightWarnings` | `true` | Show action bar warnings when entering Overburdened tier. |
| `autoHideHealthyHud` | `false` | Automatically suppress HUD rendering when the player is completely healthy. |
| `enableSwappableHud` | `true` | Enable cycling HUD modes (including `WEIGHT`). |
| `defaultHudMode` | `"COMBINED"` | Default mode on launch (`OFF`, `PHYSIOLOGY`, `ENVIRONMENT`, `INJURY`, `WEIGHT`, `COMBINED`). |

