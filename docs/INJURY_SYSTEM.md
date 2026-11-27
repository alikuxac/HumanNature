# Injury & Bone Trauma System

Injuries affect 6 body regions. Each has its own penalties, protection and recovery rules. Press `H` to check injuries and apply treatment.

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

Hold right-click for 2 seconds (40 ticks) to apply in-world, or click an injured limb in the Diagnostic Screen (`H` key).

Each splint stabilizes one limb. On a broken leg, it reduces the movement speed penalty from $-45\%$/$-85\%$ to $-10\%$ (`SPLINT_SPEED_REDUCTION`). It is removed when the limb heals to severity 0.

| Tier | Item ID | Max Durability | Sprint Dislodge | Light Fall Dislodge | Natural Heal Mult | Sprint Drain Interval | Application Effects |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Wooden Splint** | `humannature:wooden_splint` | 1 | $15\%$ | $40\%$ | $1.0\times$ | 20 ticks | Fracture stabilized; $-10\%$ speed |
| **Reinforced Splint** | `humannature:reinforced_splint` | 4 | $2\%$ | $5\%$ | $1.25\times$ | 100 ticks | $-50\%$ timer cut; Resistance I (30s) |
| **Golden Splint** | `humannature:golden_splint` | 8 | $1\%$ | $2\%$ | $1.50\times$ | 150 ticks | $-67\%$ timer cut; Absorption I (2 hearts), Regen I (5s) |
| **Diamond Splint** | `humannature:diamond_splint` | 16 | $0\%$ | $0\%$ | $1.75\times$ | 400 ticks | Instant full recovery; Resistance II (20s), Knockback Resist $+0.5$ (60s) |
| **Netherite Splint** | `humannature:netherite_splint` | 24 | $0\%$ | $0\%$ | $2.0\times$ | 0 (Unbreakable) | Instant full recovery; Speed II, Resistance I (45s) |

#### Splint Stability & Dislodging
- **Sprint dislodge**: Sprinting rolls a check per step to dislodge the splint ($15\%$ Wooden, $2\%$ Reinforced, $1\%$ Golden).
- **Light fall dislodge**: Falls between 1.5 blocks and the safe damage threshold risk knocking the splint off ($40\%$ Wooden, $5\%$ Reinforced, $2\%$ Golden).
- **Durability drain**: Sprinting drains splint durability every `sprintDurabilityDrainInterval` ticks. At 0 durability, the splint snaps.
- Diamond and Netherite splints never dislodge ($0\%$) and do not wear down from sprinting.

---

### 6. Osteogenesis & Food Recovery

#### Osteogenesis Buff
Consuming Milk Buckets, Bone Broth, or `#humannature:calcium_foods` grants **Osteogenesis** for 3 minutes (3,600 ticks):
- **Healing speed**: Splinted fractures heal $2.0\times$ faster (`ServerConfig.CALCIUM_HEALING_MULTIPLIER`), stacking with the splint tier multiplier (up to $4.0\times$ on Netherite).
- **Instant reduction**: Eating calcium food with a splinted fracture immediately cuts remaining healing time by $35\%-50\%$.
- **Requirement**: Fractures must be splinted to benefit from Osteogenesis.

#### Food Recovery Reductions
Recovery foods reduce remaining fracture time on consumption:

| Item | Reduction | Audio |
| :--- | :--- | :--- |
| **Milk Bucket / Bone Broth** | $50\%$ (triggers Osteogenesis) | Bone meal sound |
| **Soups & Stews** (`rabbit_stew`, `mushroom_stew`, `beetroot_soup`, `suspicious_stew`) | $30\%$ | Bone meal sound |
| **Golden Apples** (`golden_apple`, `enchanted_golden_apple`) | $75\%$ | Bone meal sound |
| **Potions** (Healing or Regeneration) | $40\%$ | Bone meal sound |

---

### 7. Diagnostic UI

- **Body Silhouette**:
  - Open with `H` (`DiagnosticScreen`); press `E` to return to the inventory.
  - Silhouette colors show limb status: Green (Healthy), Yellow (Minor / Sprain), Red (Severe / Fracture).
  - Splinted limbs display a cyan border (`#00FFFF`), the splint tier, and remaining durability.
  - Clicking an injured limb uses the first suitable splint or bandage from inventory via `TreatLimbPayload`.
  - Hovering a limb shows `[Click to Treat with Splint]` or `[Need Splint to Treat]`.
  - Active Osteogenesis shows a bone icon with remaining duration.
- **Auto-Hide Healthy HUD (`ClientConfig.AUTO_HIDE_HEALTHY_HUD`)**:
  - When enabled, hides the HUD overlay whenever the player is fully healthy (no active injuries, fractures, temperature debuffs, or physiological stress).
- **Localization**:
  - All limb names, severities, splint tiers, and action hints support 7 locales (`en_us`, `vi_vn`, `zh_cn`, `ru_ru`, `es_es`, `pt_br`, `de_de`).

---

### 8. Totems After Fatal Falls

Surviving a fatal fall with a Totem of Undying has these effects:

- **Health**: Removes the totem's Regeneration and Absorption effects, locks health at $1.0$ HP ($0.5$ heart), and applies `Darkness` for 3 seconds.
- **Tier 2 Double Leg Fracture**: Applies Tier 2 fractures to both legs ($-85\%$ movement speed, jumping disabled, sprint locked).
- **Dropped Items**: Bypasses `keepInventory`, dropping items held in both hands (consuming 1 totem), the 9-slot hotbar, and $\lceil 30\% \rceil$ of occupied storage slots within a 4-block radius with a 40-tick pickup cooldown.

---

### 9. Elytra Flight & Crashes

- **G-Force Overload**:
  - Activating $\ge 2$ firework rockets within 40 ticks (2 seconds) while gliding applies `Darkness` for 1 second (20 ticks) and plays wind shear audio.
- **Crashes**:
  - Colliding with walls or hitting the ground while gliding:
    - **Moderate Impact** ($\ge 4.0$ damage or impact speed $\ge 0.7$ blocks/tick): Applies a Tier 1 fracture to one leg ($-45\%$ movement speed, sprint locked).
    - **Extreme Impact** ($\ge 12.0$ damage, dive angle $\ge 35^\circ$ with speed $\ge 1.4$, or speed $\ge 1.8$):
      - Applies Tier 2 fractures to both legs ($-85\%$ movement speed, jumping disabled, sprint locked).
      - Drops any firework rockets held in either hand.

---

### 10. Bleeding & Treatment

Injuries can cause bleeding:

- **Tier 1 (Minor Bleeding)**: Any non-magic, non-freeze hit $\ge 2.0$ damage triggers minor capillary bleeding (2 ml/s). Clots automatically after 35 seconds.
- **Tier 2 (Arterial Bleeding)**: Severe trauma, including Tier 2 double leg fractures, triggers arterial bleeding (20 ml/s). Does not clot naturally; emits falling blood particles in-world.
- **Cloth Bandage** (`humannature:cloth_bandage`): Takes 1.5 seconds to apply. Stops minor bleeding without side effects; stopping arterial bleeding also applies Slowness I for 5 seconds.
- **Emergency Tourniquet**: Uses 1 Leather or String to stop bleeding immediately. Applies Slowness II and Mining Fatigue I for 8 seconds.

See [PHYSIOLOGY_SYSTEM.md](./PHYSIOLOGY_SYSTEM.md) for blood loss thresholds and transfusions.

---

## Configuration

### Server (`humannature-server.toml`)

Module switch under `[general]`:
- `enableInjury` (boolean, default: `true`): Enable injuries and trauma.

Core keys under `[injury]`:

| Key | Default | Description |
| :--- | :--- | :--- |
| `enableSplint` | `true` | Enable splint mechanics and limb stabilization. |
| `boneFractureDurationTicks` | `6000` (100–72000) | Base natural recovery duration for bone fractures in ticks (6000 ticks = 5 minutes). |
| `enableTotemBypass` | `true` | Enable trauma totem bypass on lethal fall or blunt trauma. |
| `enableGForceBlackout` | `true` | Enable high G-force blackout on sharp turns during high-speed elytra flight. |
| `enableKineticCrash` | `true` | Enable kinetic crash trauma when impacting blocks at high speed with elytra. |

> Splint speed penalty (10%), durability per 50 steps (1 point), crouch recovery multiplier (0.5x), and calcium healing multiplier (2.0x) are internal balance constants in code.

### Client (`humannature-client.toml`)

Configured under `[hud]`:

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableHudOverlay` | Boolean | `true` | Show injury & trauma status HUD overlay on screen. |
| `autoHideHealthyHud` | Boolean | `false` | Automatically suppress HUD rendering when the player is completely healthy. |
| `hudX` | Integer | `10` | Screen X position of the injury HUD overlay. |
| `hudY` | Integer | `10` | Screen Y position of the injury HUD overlay. |




