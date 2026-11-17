# Weight & Encumbrance System

Tracks inventory load, armor weight, container contents, and applies graduated encumbrance penalties. Introduces capacity-expanding potions and armor enchantments.

---

## Mechanics

### 1. Weight Calculation

The server calculates total carry weight across inventory, armor, and offhand slots.

$$\text{Total Weight} = \sum \text{Stack Weight} + \sum \text{Contained Item Weight}$$

| Category | Tag / Condition | Base Weight | Stack (64x) |
| :--- | :--- | :--- | :--- |
| **Heavy Blocks** | `#c:heavy_blocks`, `#humannature:heavy_blocks` (stone, cobble, ores, obsidian, anvils) | 0.8 kg | 51.2 kg |
| **Metallic Items** | `#c:metallic_items`, `#humannature:metallic_items` (ingots, raw metals, chains, armor) | 0.4 kg | 25.6 kg |
| **Light Items** | `#c:light_items`, `#humannature:light_items` (feathers, seeds, paper, sticks) | 0.05 kg | 3.2 kg |
| **Default Items** | Unclassified items | 0.1 kg | 6.4 kg |

#### Nested Containers
Shulker boxes and bundles evaluate `DataComponents.CONTAINER` and `DataComponents.BUNDLE_CONTENTS`. The total weight includes the container base item plus all stored contents recursively.

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
- **Water Sink Pull**: In water, heavier encumbrance pulls players downward per tick, counteracting buoyancy.
- **Fall Damage**: Scales impact damage upon landing based on momentum and load mass.

---

### 3. Capacity Boosts

Default base capacity is **500.0 kg**. Players can raise this limit through potions and enchantments:

$$\text{Max Capacity} = \text{Base} (500\text{kg}) + \text{Burden Bearer Bonus} + \text{Load Bearing Bonus}$$

#### Mob Effect: Burden Bearer (`humannature:burden_bearer`)
Grants temporary carry capacity bonus: **+150.0 kg per level**.

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
Data-driven armor enchantment applicable to helmets, chestplates, leggings, and boots. Max level III.

- **Bonus**: **+50.0 kg capacity per level per piece**.
- A full set of Level III armor grants $+600.0\text{ kg}$ capacity (4 pieces $\times$ 3 levels $\times$ 50 kg), bringing total capacity to **1100.0 kg**.

---

### 4. UI & Diagnostics

#### Swappable HUD (`WEIGHT` Mode)
Cycle with the HUD keybind (`H` or configured key). Shows a dedicated weight panel in the top-left overlay:
- Header: `[WEIGHT & ENCUMBRANCE]`
- 12-segment progress bar with tier color coding (`[██████░░░░░░] 391.0/800kg (49%)`).
- Active tier status and penalty readouts.
- Load breakdown: `Load: Inv XX.Xkg | Armor XX.Xkg`.
- Active capacity buffs: `Buffs: +XXXkg bonus capacity`.

#### Item Tooltips
Hovering any item or stack displays weight metrics:
- Single item: `Weight: 0.8 kg`
- Stack: `Weight: 51.2 kg (0.8 kg each)`
- Containers: `Weight: 1383.2 kg (Contents: 1382.4 kg)`

#### Diagnostic Screen
The Diagnostic Screen displays total carried weight against maximum capacity alongside limb health and vital signs.

---

### 5. Configuration

#### Server (`humannature-server.toml`)

Master module switch under `[general]`:
- `enableWeight` (boolean, default: `true`): Master toggle for the inventory weight and encumbrance module.

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
| `enableSwappableHud` | `true` | Enable cycling HUD modes (including `WEIGHT`). |
| `defaultHudMode` | `"COMBINED"` | Default mode on launch (`OFF`, `PHYSIOLOGY`, `ENVIRONMENT`, `INJURY`, `WEIGHT`, `COMBINED`). |

