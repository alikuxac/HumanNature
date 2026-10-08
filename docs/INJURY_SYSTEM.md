# Injury & Bone Trauma System

## Overview
The Injury & Bone Trauma System introduces realistic human skeletal trauma across 6 body regions, dynamic debuffs, armor/enchantment mitigation, resting metabolic recovery, multi-tier splints, nutritional healing acceleration, and a standalone diagnostic GUI similar to modern accessory interfaces.

---

## Key Mechanics

### 1. 6-Limb Trauma Engine & Localized Penalties
The human anatomy is tracked across 6 distinct limbs with 2-tier severity levels:

| Limb | Tier 1 (Minor / Sprained) | Tier 2 (Severe / Fractured) | Triggers & Sources |
| :--- | :--- | :--- | :--- |
| **Head** | Mild disorientation & periodic tinnitus | Flashing Darkness & disorientation | Falling blocks (Anvil, Gravel, Sand), Elytra wall crashes |
| **Torso** | Accelerated food exhaustion on movement | Max health capped to 7 Hearts (14 HP) | Heavy blunt impact (Ravager, Iron Golem, $>12.0$ damage) |
| **Arms (Main / Off)** | $-30\%$ Attack Speed | $-30\%$ Attack Speed | Excessive blunt damage / trauma |
| **Legs (Left / Right)** | $-45\%$ Movement Speed, Sprint locked | $-85\%$ Speed, Jump disabled, Forced crawl (`Pose.SWIMMING`) | Fall damage $>5.0$ blocks |

- **Near-Death Shock**: Any fall resulting in $\le 1.0$ HP ($0.5$ heart) while fractured triggers acute Darkness, drops the held main-hand item, and plays heartbeat audio.

---

### 2. Mitigation via Armor & Enchantments
- **Feather Falling**: Boots enchanted with Feather Falling grant $+2.0$ blocks safe fall threshold per level.
- **Leggings Quality**: Diamond or Netherite leggings add $+1.5$ blocks safe fall threshold.
- **Blast Protection**: Helmets with Blast Protection mitigate $50\% - 80\%$ of head trauma chance.
- **Chestplate Quality**: Diamond and Netherite chestplates completely nullify Tier 1 torso trauma from blunt attacks.

---

### 3. Recovery Mechanics & Resting
Trauma recovery proceeds through natural resting, active sleep, or splint first-aid:

| Player State | Condition | Healing Multiplier | HUD Status |
| :--- | :--- | :--- | :--- |
| **Moving (Unsplinted)** | Walking / Sprinting | **Paused** (0 ticks/sec) | `Healing: mm:ss (Paused)` |
| **Standing Still** | No horizontal motion | **1x Standard** (1 tick/tick) | `Healing: mm:ss (Standing Still)` |
| **Active Resting** | Crouching or Sleeping | **2x Accelerated** (2 ticks/tick) | `Healing: mm:ss (Resting 2x)` |

> [!TIP]
> **Sleep Trauma Downgrade**:
> Upon waking from sleep (`PlayerWakeUpEvent`), if player nutrition is $\ge 16$ (8 drumsticks), the body automatically **downgrades the most severe trauma by 1 tier** (e.g., Tier 2 $\rightarrow$ Tier 1, or Tier 1 $\rightarrow$ Healed). This consumes 4 hunger points (2 drumsticks) as the metabolic cost of bone remodeling.

---

### 4. Death & Respawn Behavior (`keepInventory`)
- **`keepInventory = false` (Vanilla default)**:
  - Full restoration upon death. All 6-limb trauma data, timers, and lingering negative status effects (Darkness, Confusion, Slowness) are reset.
- **`keepInventory = true` (Anti-Suicide-Spam Protection)**:
  - If players retain items upon death, suicide cannot be used to bypass healing:
  - Respawn applies a **30-second penalty**: **Weakness I**, **Hunger I**, and **Tier 1 Leg Fracture** (speed penalty, sprint locked).

---

### 5. Multi-Tier Splints & First-Aid Treatment
Splints can be used directly in-world by **holding right-click for 2 seconds (40 ticks)** with `UseAnim.BRUSH` animation, wool particles, and leather rustling sounds:

| Tier | Item ID | Durability | Effect & First-Aid Buffs |
| :--- | :--- | :--- | :--- |
| **Wooden Splint** | `humannature:wooden_splint` | **1** | Instant relief of fracture, $-10\%$ speed penalty |
| **Reinforced Splint** | `humannature:reinforced_splint` | **4** | Cuts remaining recovery timer by $50\%$, **Resistance I** (30s) |
| **Golden Splint** | `humannature:golden_splint` | **8** | Cuts timer by $67\%$ ($3\times$ faster), **Absorption I** (2 hearts) + **Regeneration I** (5s) |
| **Diamond Splint** | `humannature:diamond_splint` | **16** | Instant full recovery, **Resistance II** (20s) + **Knockback Resistance +0.5** (60s) |
| **Netherite Splint** | `humannature:netherite_splint` | **24** | Instant full recovery, **Adrenaline (Speed II + Resistance I)** (45s) |

---

### 6. Nutrition & Dietary Acceleration
Consuming nutrient-dense foods instantly cuts down remaining fracture recovery time:

| Food / Consumable | Items | Recovery Reduction | Audio Effect |
| :--- | :--- | :--- | :--- |
| **Milk Bucket** | `minecraft:milk_bucket` | **50% Instant Cut** | Bone meal crunch sound |
| **Soups & Stews** | `rabbit_stew`, `mushroom_stew`, `beetroot_soup`, `suspicious_stew` | **30% Instant Cut** | Bone meal crunch sound |
| **Golden Apples** | `golden_apple`, `enchanted_golden_apple` | **75% Instant Cut** | Bone meal crunch sound |
| **Potions** | Any `PotionItem` (Healing/Regeneration) | **40% Instant Cut** | Bone meal crunch sound |

---

### 7. Diagnostics GUI & Client HUD

#### A. HUD Overlay (In-Game Screen)
- Positioned at top-left (`x=10, y=10`) with a dark translucent panel.
- Displays list of affected limbs (e.g. `Injured: Head (T1), Left Leg (T2)`) and current recovery timer formatted as `mm:ss`.

#### B. Dedicated Diagnostic Screen (`DiagnosticScreen`)
- Opened via:
  - **Hotkey `H`** (customizable under Controls > Key Binds). Pressing `H` again closes the window.
  - **Inventory Tab Button**: A $28 \times 28$ tab button attached flush to the left edge of the vanilla inventory GUI.
- **Interactive Silhouette Panel**:
  - Displays a clean pixel-art anatomy diagram of the 6 limbs.
  - Color states:
    - 🟢 **Healthy** (`0xFF2E7D32`): Tier 0 - Normal.
    - 🟡 **Minor** (`0xFFF57F17`): Tier 1 - Sprained / Minor trauma.
    - 🔴 **Severe** (`0xFFD32F2F`): Tier 2 - Fractured / Severe trauma.
- **Diagnostic Overview**: Lists all currently fractured limbs and specific penalties.
- **Inventory-Wide Quick Treatment**:
  - Automatically searches the player cursor, hotbar, and full inventory for valid splints.
  - Clicking an injured limb applies treatment and consumes 1 durability.
  - Enforces a **2-second cooldown (40 ticks)** with UI status indicators (`⌛ Treating... Please wait`) to prevent instant spamming.
- **Navigation**:
  - Click **`← Inventory`** button or press **`E`** to switch directly back to the survival inventory.
