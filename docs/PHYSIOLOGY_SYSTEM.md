# Physiology System

The physiology module tracks sleep deprivation, handles stamina and muscle fatigue, replaces instant poison/wither damage with an accumulating toxin gauge, and simulates digestion, stress, and trauma shock.

---

## Mechanics

### Sleep Deprivation & Fatigue

The server tracks consecutive awake ticks for survival and adventure players outside of bed. Every 24,000 ticks (one in-game day) adds 33.3% to the hidden `fatigue score`, reaching 100% (1.0) after 72,000 ticks (three in-game days). Sleeping in bed resets this counter.

| Awake Time | Server Ticks | Fatigue Score | Symptoms |
| :--- | :--- | :--- | :--- |
| **0 Days** | 0 | 0% | Rested |
| **1 Day** | 24,000 | 33.3% | Mild fatigue |
| **2 Days** | 48,000 | 66.7% | Moderate fatigue |
| **2.4 Days** | 57,600 | 80.0% | Severe fatigue (involuntary blinks start) |
| **3+ Days** | 72,000+ | 100.0% | Complete exhaustion (2x toxin vulnerability) |

#### Involuntary Darkness Blinks
At 80% fatigue, moving players trigger a 1-second (20-tick) Darkness blink every 300 to 800 ticks (15 to 40 seconds). Stationary players do not blink.

---

### Toxin Buildup (Poison & Wither Interception)

Incoming `minecraft:poison` and `minecraft:wither` attacks add to a hidden 0–100% buildup gauge instead of applying immediate status effects. Other status effects apply normally.

#### Buildup Calculation & State Scaling
Each exposure calculates base buildup from effect duration and amplifier, with a baseline minimum of 25%:

$$\text{BaseValue} = \max(\text{baseHit}, \min(100, \text{durationFactor} \times \text{amplifierFactor}))$$

Exhaustion and physical injuries scale this base value:

$$\Delta\text{Buildup} = \text{BaseValue} \times \left(1.0 + \frac{\text{FatigueScore}}{100}\right) \times (\text{isFractured} \mathbin{?} 1.5 : 1.0)$$

- **Fatigue scaling**: Rested players take 1.0x buildup. Full sleep deprivation (100% fatigue) doubles incoming buildup (2.0x).
- **Fracture multiplier**: Any active bone fracture adds a 1.5x multiplier.
- **Combined vulnerability**: A player both fully exhausted and fractured accumulates toxins 3.0x faster.

#### Eruption
At 100% buildup, the intercepted effect triggers with full accumulated duration and amplifier, resetting the gauge to 0%.

#### Passive Decay & Heat Acceleration
After 80 ticks (4 seconds) without new toxin hits, buildup drains at 2.0% per second.

Resting (sleeping or crouching while still) or standing within 2 blocks of heat sources (fire, soul fire, campfires, lava, magma blocks) triples decay to 6.0% per second.

#### Item Counterplay
- **Milk Bucket**: Clears 100% of accumulated buildup.
- **Honey Bottle**: Reduces current buildup by 50%.

---

### Physical Exertion & Stamina

Physical actions drain stamina from a 0–100% capacity gauge. Reaching 100% exertion triggers muscle failure.

#### Exertion Triggers & Costs
- **Continuous Mining / Chopping**: Breaking blocks continuously adds 0.5% exertion per tick, triggering muscle failure at 10 seconds (200 ticks). Guarded per tick against multi-hit exploits.
- **Sprinting**: Adds 0.08% exertion per tick (~1.6% per second), depleting full stamina in ~60 seconds.
- **Consecutive Jumping**:
  - Base jump cost: 10.0% exertion per jump.
  - Bunny hopping: Jumps within 40 ticks (2 seconds) of each other increase cost by +1% per jump after the second jump, capping at +6% (10% on jumps 1–2, 11% on jump 3, up to 16% on jump 8).
  - 8 consecutive jumps guarantee 100% exertion and immediate muscle failure.
  - Jump threshold: Jumping requires at least 10% stamina (<90% exertion) and no active muscle failure. Otherwise, vertical jump velocity zeroes out and `generic.jump_strength` drops by 100%.
- **Attacking**: Swings add 4.0% exertion per hit.
- **Upstream Swimming**: Moving against fluid flow (`fluid.getFlow()`) adds 0.75% exertion per tick.
- **Heavy-Load Heat Stress**: Moving while overburdened ($\ge 85\%$ carry load) in hot biomes (Nether, Badlands, Desert) adds $+0.10\%$ exertion per tick, accelerating stamina depletion alongside rising core body temperature.

#### Muscle Failure Threshold
At 100% exertion:
- **Attack Speed**: -50% (`generic.attack_speed`).
- **Mining Speed**: -50% block break speed.
- **Sprint Lockdown**: Cancels sprinting immediately and blocks sprint input.
- **Jump Lockdown**: Zeroes vertical jump velocity and applies a -100% modifier to `generic.jump_strength`.

#### Stamina Recovery
- **Stationary Rest**: Standing still on ground (`!isMoving && onGround`) without mining or jumping for 3.5 seconds (70 ticks) restores full stamina and clears muscle failure.
- **Walking Decay**: Moving without sprinting or muscle failure clears exertion at 0.2% per tick (4.0% per second).

---

### Swimming Cramps

#### Triggers & Consequences
- **Trigger**: Swimming or diving continuously for more than 30 seconds (600 ticks) triggers a leg cramp.
- **Speed Penalty**: Reduces horizontal swimming speed by 40%.
- **Audio Feedback**: Plays water struggle and pain sounds.

#### Recovery
- Move onto dry ground (`!isInWater && onGround`) or enter a vehicle.
- Resting on land or in a boat for 3 seconds (60 ticks) clears the cramp.

---

### Metabolism & Gut Reflex

#### Digestion Window (Absorption Buffer)
- Eaten food points and saturation enter an absorption queue instead of loading instantly into the hunger bar.
- Pre-eating snapshots capture initial food and saturation levels at the start of consumption. If the player eats with a full hunger bar ($\ge 20$), the system marks the action as overeating before vanilla food application runs.
- Digestion drip-feeds 1 hunger point (and proportional saturation) every 1.5 seconds (30 ticks) until the queue empties.

#### Overeating & Vomit Reflex
- Consuming food when the stomach is already full triggers digestive discomfort:
  - **1st–2nd overeat**: Triggers hiccup/burp audio and a -15% movement slowdown.
  - **3rd consecutive overeat**: Triggers the vomit reflex. The player drops regurgitated food items, loses 5 hunger points (down to a minimum of 10), clears buffered digestion, and receives `Nausea` for 2 seconds.

#### Indigestion from Raw & Spoiled Meat
- Consuming raw chicken or rotten flesh adds +35% to an indigestion gauge (0–100%).
- At $\ge 50\%$ indigestion, the player suffers stomach cramps, belly rumbles, and accelerated hunger drain (+0.025 exhaustion/tick).
- Indigestion decays at 1% every 40 ticks (2 seconds), tracked by a dedicated per-player counter.
- Milk clears 100% of accumulated indigestion; honey clears 30%.

#### Osteogenesis Buff
Consuming Milk, Bone Broth, or `#humannature:calcium_foods` grants **Osteogenesis** for 3 minutes (3,600 ticks):
- **Healing speed**: Splinted fractures heal $2.0\times$ faster (`ServerConfig.CALCIUM_HEALING_MULTIPLIER`), stacking with the splint tier multiplier.
- **Instant reduction**: Eating calcium food with a splinted fracture immediately cuts remaining healing time by $35\%-50\%$.
- **Requirement**: Fractures must be splinted to receive this bonus.

#### Fracture Natural Regeneration Suppression
- Active bone fractures cancel passive food-based health regeneration (`amount <= 1.0F`).
- External medical healing (healing potions, golden apples, regeneration beacons) and resting/crouching recovery function normally.

---

### Panic, Stress & Sensory Cues

#### Deep Underground Claustrophobia
- **Trigger**: Spending more than 5 seconds (100 ticks) at $Y < 0$ in absolute darkness (0 lux).
- **Sensory Cues**: Heart rate increases (+45 BPM), rhythmic heartbeat audio plays, and ambient sounds muffle.
- **Aim Tremor**: Drawing a bow or crossbow during high panic causes harmonic camera jitter (~4.5–9.6 Hz, ~1.35° pitch/yaw displacement), throwing off aim.

#### Calming Therapy
- Panic drops immediately and heart rate normalizes when:
  1. Holding a light-emitting item (torch, lantern, soul lantern) in either hand.
  2. Standing in ambient light (> 0 lux) or near any lit block.
  3. Standing within 8 blocks of an active Jukebox playing music.
  4. Staying within 10 blocks of an owned tamed pet (wolf, cat, parrot).

---

### Pain Shock & Adrenaline Surge

#### Adrenaline Surge
- **Trigger**: Sustaining a severe Tier 2 bone fracture (both legs broken from high falls, or severe torso trauma).
- **Burst**: Grants a 4-second (80-tick) emergency survival rush:
  - Temporarily suppresses the -85% limping penalty.
  - Grants +35% movement speed and restores sprinting.
  - Spikes heart rate to 170–190+ BPM (+60 BPM surge).

#### Pain Shock & Collapse
- When the 4-second surge ends:
  - Screen goes dark (`Darkness` and `Blindness` for 1.5–2s).
  - The body collapses into complete muscle failure (100% exertion, 0 stamina).
  - Tier 2 injury penalties reassert immediately in the same tick, returning the player to an agonizing crawl.

---

### HUD & Diagnostic Interface

#### Swappable HUD Overlay
A compact overlay displaying real-time survival stats, toggleable between six modes:
- `OFF`: Hides the overlay.
- `PHYSIOLOGY`: Displays fatigue, toxicity, heart rate, stamina, and digestion.
- `ENVIRONMENT`: Displays wetness and temperature in the action bar or compact readout.
- `INJURY`: Displays active trauma, fractures, and limb penalties.
- `WEIGHT`: Displays detailed weight, encumbrance bar, and capacity buffs.
- `COMBINED`: Shows all active survival lines in a standardized layout:
  - `Fatigue`: `Fatigue: %s (%s %s)` (colored awake duration, status tag, and percentage).
  - `Toxicity`: `Toxicity: %s (%s)` (compact representation with active status).
  - `Heart`: `Heart: %s (%s)` (colored BPM and cardiac rate status).
  - `Stamina`: `Stamina: %s (%s)` (percentage and muscle/cramp condition).
  - `Digestion`: `Digestion: %s` (Active, Resting, or Hungry).
  - `Temperature`: `Temperature: %s (%s)` (core temp and climate tier).
  - `Weight`: `Weight: %s/%skg (%s %s)` (current/max load and encumbrance tier).

#### Auto-Hide Healthy HUD (`ClientConfig.AUTO_HIDE_HEALTHY_HUD`)
Hides the overlay when all vitals are nominal:
- Rested (`fatigueScore <= 0.0f`), no toxins (`toxinBuildup <= 0.0f`), and full stamina (`exertionScore <= 0.0f`).
- No active muscle failure, cramps, panic, or adrenaline surge.
- Normal temperature, light/normal encumbrance, and no limb injuries.

#### Transient HUD Overlays
- **Stamina Crosshair Arc**: When stamina drops below 100%, a radial arc renders right of the crosshair (radius 19–23 px, angle -55° to +55°). Fades out over 1.5 seconds once stamina reaches 100%.
  - **Colors**: `#FFD23F` (yellow, normal), `#FF7043` (orange, < 25%), pulsing `#FF0000` (red, muscle failure), `#00E5FF` (cyan, swimming cramp).
- **Toxin Buildup Bar**: When toxin buildup exceeds 0%, renders a horizontal bar above the hotbar (green for Poison, purple for Wither).

#### Body Diagnostics Screen (`H` Key)
The diagnostic screen provides a detailed medical readout:
- Interactive limb hitboxes showing bone fractures, trauma severity, and splint status.
- Real-time cardiac heart rate, fatigue rating, muscle status, and digestion buffer.
- `← Inventory` navigation button to return to the player inventory.
- Full localization across 7 locales (`en_us`, `vi_vn`, `zh_cn`, `ru_ru`, `es_es`, `pt_br`, `de_de`).

---

### Reset Triggers & Session Cleanup

- **Sleeping through the night**: Sleeping in a bed for at least 90 ticks resets awake ticks, fatigue, exertion, and cramps to 0.
- **Death & respawn**: Resets awake ticks, fatigue score, active toxin buildup, eruption flags, muscle failure, and cramps. Evicts cached food snapshots.
- **Disconnect**: Player disconnect purges cached food snapshots from memory to prevent memory leaks across sessions.

---

## Configuration

### Server (`humannature-server.toml`)

Master module switch under `[general]`:
- `enablePhysiology = true`: Master toggle for the physiology module (sleep deprivation, stamina, toxin buildup).

Core settings under `[physiology]`:

```toml
[physiology]
    # Enable sleep deprivation and fatigue buildup over time
    enableSleepDeprivation = true
    # Ticks of being awake required to reach 100% fatigue (72000 ticks = 3 in-game days)
    # Range: 20 ~ 720000
    sleepDeprivationFullFatigueTicks = 72000
    # Enable soulslike bad effect (poison/wither) buildup and transient gauge
    enableToxinBuildup = true
    # Enable physical exertion and muscle failure from continuous mining and sprinting
    enablePhysicalExertion = true
```

> **Physics Constants**: Subsystem parameters (such as the 80% blink threshold, 25% minimum toxin hit, 80-tick decay delay, 200-tick continuous mining limit, 600-tick swimming cramp threshold, 30-tick digestion cadence, 100-tick dark claustrophobia panic, and 80-tick adrenaline surge) are internal balance constants in code.

### Client (`humannature-client.toml`)

```toml
[hud]
    # Show injury & trauma status HUD overlay on screen
    enableHudOverlay = true
    # Automatically suppress HUD rendering when fully healthy
    autoHideHealthyHud = false
    # Screen X position of the injury HUD overlay
    hudX = 10
    # Screen Y position of the injury HUD overlay
    hudY = 10
    # Show transient soulslike toxin buildup bar above hotbar
    enableToxinHudOverlay = true
    # Show transient stamina / exertion bar above hotbar
    enableStaminaHudOverlay = true
    # Enable swappable HUD module cycling
    enableSwappableHud = true
    # Default HUD mode on launch (OFF, PHYSIOLOGY, ENVIRONMENT, INJURY, WEIGHT, COMBINED)
    defaultHudMode = "COMBINED"
    # Screen X position of the swappable HUD mini box
    swappableHudX = 10
    # Screen Y position of the swappable HUD mini box
    swappableHudY = 10
    # Show detailed rows for digestion buffer, calcium bone healing, and cramps
    showMetabolicDetails = true
    # Show real-time cardiac heart rate BPM on the HUD overlay
    showCardiacHud = true

[camera]
    # Enable camera wobble/tilt effect when walking with fractured leg(s)
    enableCameraLimping = true
```
