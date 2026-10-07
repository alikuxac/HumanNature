# Injury & Bone Recovery System

## Overview
The Injury & Bone Recovery System introduces realistic bone fracture mechanics, active resting recovery speedups, multi-tier splints with right-click channeling treatment, nutrition-based healing acceleration, and a real-time HUD display.

---

## Key Mechanics

### 1. Fracture Application & Penalties
- **Trigger**: Fall damage / landing from heights exceeding 3 blocks without creative or spectator modes.
- **Single Leg Fracture (Tier 1)**: Reduces movement speed by 45%.
- **Both Legs Fracture (Tier 2)**: Reduces movement speed by 85%, completely prevents jumping, and forces crawl pose (`Pose.SWIMMING`).
- **Default Recovery Duration**: 6000 ticks (5 minutes).

---

### 2. Recovery Mechanics & Conditions
Bone recovery tick count decreases under specific valid conditions:

| Player State / Action | Condition | Healing Speed | HUD Status |
| :--- | :--- | :--- | :--- |
| **Moving (Unsplinted)** | Player walking/running | **Paused** (0 ticks/sec) | `Healing: mm:ss (Paused)` |
| **Standing Still** | No horizontal movement | **1x Standard** (1 tick/tick) | `Healing: mm:ss (Standing Still)` |
| **Active Resting** | Crouching or Sleeping | **2x Accelerated** (2 ticks/tick) | `Healing: mm:ss (Resting 2x)` |

---

### 3. Multi-Tier Splints & Treatment Progression
Splints are used by **holding right-click for 2 seconds (40 ticks)** with `UseAnim.BRUSH` animation, wool particle effects, and leather rustle sounds:

| Tier | Item ID | Durability | Effect & Buff Application |
| :--- | :--- | :--- | :--- |
| **Tier 1 - Wooden Splint** | `humannature:wooden_splint` | **1** | Instant fracture relief, `-10%` movement speed |
| **Tier 2 - Reinforced Splint** | `humannature:reinforced_splint` | **4** | Cuts remaining recovery duration by 50%, **Resistance I** (30s) |
| **Tier 3 - Golden Splint** | `humannature:golden_splint` | **8** | Cuts recovery duration by 67% (3x speed), **2 Golden Hearts (Absorption I)** + **Regeneration I** (5s) |
| **Tier 4 - Diamond Splint** | `humannature:diamond_splint` | **16** | Instant full heal, **Resistance II** (20s) + **4 Golden Hearts (Absorption II)** |
| **Tier 5 - Netherite Splint** | `humannature:netherite_splint` | **24** | Instant full heal, **Adrenaline (Speed II + Resistance I)** (45s) |

---

### 4. Nutrition & Item Consumption Acceleration
Consuming specific items instantly reduces remaining fracture recovery duration:

| Item / Category | Items | Duration Reduction | Sound Effect |
| :--- | :--- | :--- | :--- |
| **Milk Bucket** | `minecraft:milk_bucket` | **50% Instant Reduction** | `entity.generic.consume` / `item.bone_meal.use` |
| **Soups & Stews** | `rabbit_stew`, `mushroom_stew`, `beetroot_soup`, `suspicious_stew` | **30% Instant Reduction** | `item.bone_meal.use` |
| **Golden Apples** | `golden_apple`, `enchanted_golden_apple` | **75% Instant Reduction** | `item.bone_meal.use` |
| **Potions** | Any `PotionItem` (Healing/Regen) | **40% Instant Reduction** | `item.bone_meal.use` |

---

### 5. Client HUD Overlay
- Located at top-left corner (`x=10, y=10`) with a dark semi-transparent container.
- Displays remaining recovery time formatted in `mm:ss` along with current status & color coding.
- Automatically clears when fracture timer reaches `00:00` or fracture is cured.
