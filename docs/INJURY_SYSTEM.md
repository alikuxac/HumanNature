# Injury & Bone Trauma System

Tracks skeletal trauma across 6 body regions with localized penalties, armor mitigation, resting recovery rates, craftable splints, and diagnostic interfaces.

---

## Mechanics

### 1. Limbs & Penalties

| Limb | Tier 1 (Sprain) | Tier 2 (Fracture) | Sources |
| :--- | :--- | :--- | :--- |
| **Head** | Periodic tinnitus, mild disorientation | Flashing darkness, disorientation | Falling blocks (anvil, sand, gravel), Elytra wall crashes |
| **Torso** | Increased hunger exhaustion on move | Max health capped at 14 HP (7 hearts) | Heavy hits $\ge 12.0$ damage |
| **Main Arm** | $-50\%$ attack/mining speed, $15\%$ drop chance on attack/mine, partial bow draw | Same | Elytra wall crashes, mining blocks with hardness $\ge 10.0$ |
| **Off Arm** | Offhand slot locked, shield blocking disabled | Same | Heavy blocked hits ($\ge 10.0$ damage), shield disabling strikes, Elytra crashes |
| **Legs** | $-45\%$ movement speed, sprint locked | $-85\%$ movement speed, jump disabled | Fall distance exceeding safe threshold |

**Near-Death Shock**: Falling to $\le 1.0$ HP ($0.5$ heart) while injured forces Darkness, drops the held mainhand item, and plays heartbeat audio.

---

### 2. Mitigation

- **Feather Falling**: Adds $+2.0$ blocks safe fall distance per enchantment level.
- **Leggings**: Diamond or Netherite leggings add $+1.5$ blocks safe fall distance.
- **Blast Protection**: Reduces head concussion chance by $50\%$ to $80\%$ based on helmet enchantment level.
- **Chestplate**: Diamond and Netherite chestplates negate Tier 1 torso trauma from blunt hits.

---

### 3. Recovery & Resting

| State | Condition | Healing Rate | HUD Display |
| :--- | :--- | :--- | :--- |
| **Moving** | Walking or sprinting without a splint | Paused | `Healing: mm:ss (Paused)` |
| **Standing Still** | No horizontal velocity | $1\times$ (1 tick/tick) | `Healing: mm:ss (Standing Still)` |
| **Resting** | Crouching or sleeping | $2\times$ (2 ticks/tick) | `Healing: mm:ss (Resting 2x)` |

**Sleep Downgrade**: Waking from a bed with $\ge 16$ hunger points (8 drumsticks) downgrades the player's worst injury by 1 tier and consumes 4 hunger points.

---

### 4. Death & Respawn

- **`keepInventory = false` (Default)**: Clears all injuries, timers, and active trauma debuffs upon respawn.
- **`keepInventory = true`**: Prevents suicide healing by applying Weakness I, Hunger I, and a 30-second Tier 1 leg sprain on respawn.

---

### 5. Splints

Hold right-click for 2 seconds (40 ticks) to apply in-world, or click an injured limb in the diagnostic screen.

| Tier | Item ID | Durability | Effects |
| :--- | :--- | :--- | :--- |
| **Wooden Splint** | `humannature:wooden_splint` | 1 | Heals fracture; applies $-10\%$ movement speed penalty |
| **Reinforced Splint** | `humannature:reinforced_splint` | 4 | Cuts timer by $50\%$; grants Resistance I (30s) |
| **Golden Splint** | `humannature:golden_splint` | 8 | Cuts timer by $67\%$; grants Absorption I (2 hearts) and Regeneration I (5s) |
| **Diamond Splint** | `humannature:diamond_splint` | 16 | Instant full recovery; grants Resistance II (20s) and Knockback Resistance $+0.5$ (60s) |
| **Netherite Splint** | `humannature:netherite_splint` | 24 | Instant full recovery; grants Speed II and Resistance I (45s) |

---

### 6. Food Recovery Reductions

Consuming recovery items instantly cuts the remaining healing timer:

| Item | Reduction | Audio |
| :--- | :--- | :--- |
| **Milk Bucket** | $50\%$ | Bone meal sound |
| **Soups & Stews** (`rabbit_stew`, `mushroom_stew`, `beetroot_soup`, `suspicious_stew`) | $30\%$ | Bone meal sound |
| **Golden Apples** (`golden_apple`, `enchanted_golden_apple`) | $75\%$ | Bone meal sound |
| **Potions** (Healing or Regeneration) | $40\%$ | Bone meal sound |

---

### 7. Diagnostic UI

- **HUD Overlay**: Anchored at `x=10, y=10`. Shows active limb injuries and remaining healing time.
- **Diagnostic Screen**:
  - Opened with `H` key or the inventory tab button.
  - Pixel-art silhouette showing limb status: Green (Healthy), Yellow (Minor), Red (Severe).
  - Hovering a limb shows active debuffs.
  - Clicking an injured limb uses an available splint from inventory (2-second cooldown).
  - Press `E` or click `← Inventory` to return to the inventory screen.
