# Injury & Bone Recovery System (AIO-490)

## Overview
The Injury & Bone Recovery System introduces realistic bone fracture mechanics, active resting recovery speedups, splint support, nutrition-based healing acceleration, and a real-time HUD display.

---

## Key Mechanics

### 1. Fracture Application & Penalties
- **Trigger**: Fall damage / landing from heights exceeding 3 blocks without creative or spectator modes.
- **Single Leg Fracture**: Reduces movement speed by 45%.
- **Both Legs Fracture**: Reduces movement speed further and completely prevents jumping (-100% jump strength).
- **Default Recovery Duration**: 6000 ticks (5 minutes).

---

### 2. Recovery Mechanics & Conditions
Bone recovery tick count decreases only under specific valid conditions:

| Player State / Action | Condition | Healing Speed | HUD Status |
| :--- | :--- | :--- | :--- |
| **Moving (No Splint)** | Player walking/running | **Paused** (0 ticks/sec) | `Healing: mm:ss (Paused)` |
| **Standing Still** | No horizontal movement | **1x Standard** (1 tick/tick) | `Healing: mm:ss (Standing Still)` |
| **Active Resting** | Crouching or Sleeping | **2x Accelerated** (2 ticks/tick) | `Healing: mm:ss (Resting 2x)` |
| **Equipped Splint** | Offhand / Curios Feet slot | **1x Constant** (even while moving) | `Healing: mm:ss (Splinted)` |

---

### 3. Nutrition & Item Consumption Acceleration
Consuming specific items instantly reduces remaining fracture recovery duration:

| Item / Category | Items | Duration Reduction | Sound Effect |
| :--- | :--- | :--- | :--- |
| **Milk Bucket** | `minecraft:milk_bucket` | **50% Instant Reduction** | `entity.generic.consume` / `item.bone_meal.use` |
| **Soups & Stews** | `rabbit_stew`, `mushroom_stew`, `beetroot_soup`, `suspicious_stew` | **30% Instant Reduction** | `item.bone_meal.use` |
| **Golden Apples** | `golden_apple`, `enchanted_golden_apple` | **75% Instant Reduction** | `item.bone_meal.use` |
| **Potions** | Any `PotionItem` (Healing/Regen) | **40% Instant Reduction** | `item.bone_meal.use` |

---

### 4. Client HUD Overlay
- Located at top-left corner (`x=10, y=10`) with a dark semi-transparent container.
- Displays remaining recovery time formatted in `mm:ss` along with current status & color coding.
- Automatically clears when fracture timer reaches `00:00` or fracture is cured.
