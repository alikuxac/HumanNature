# Physiology System

The physiology module tracks sleep deprivation, handles stamina and muscle fatigue, replaces instant poison/wither damage with an accumulating toxin gauge, simulates digestion, stress, and trauma shock, and models blood volume, hemorrhage, hemostasis, and transfusions.

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

### Blood Circulation, Hemorrhage & Transfusion

Every player has a circulatory system with a baseline volume of 5,000 ml and an ABO/Rh blood type assigned on first spawn.

#### Blood Volume & Hypovolemic Shock

Blood volume directly caps maximum health. When volume drops below 5,000 ml, an attribute modifier lowers maximum health proportionally down to a hard floor of 4.0 HP (2 hearts):

$$\text{MaxHealthTarget} = \max\left(4.0, 20.0 \times \frac{\text{BloodVolume}}{5000}\right)$$

Active bleeding or blood volume below 70% (3,500 ml) blocks natural food health regeneration. Potion effects (Regeneration, Instant Health) bypass this restriction.

Dropping through blood loss thresholds causes progressive hypovolemic shock:

| Blood Volume | Fraction | Shock State & Symptoms |
| :--- | :--- | :--- |
| **5,000 ml** | 100% | Normal circulation. |
| **< 4,000 ml** | < 80% | Tachycardia. Heartbeat audio plays every 25 ticks with pitch scaling from 1.0 to 1.5. Stamina drain doubles. |
| **< 3,000 ml** | < 60% | Hypoxic vision. Darkness effect applies continuously (tunnel vision and desaturation). |
| **< 2,000 ml** | < 40% | Circulatory collapse and syncope. Forces Darkness, Blindness, and Slowness IV; cancels sprinting. |
| **0 ml** | 0% | Fatal cardiac arrest. Immediate player death. |

#### Hemorrhage Tiers

Physical trauma causes bleeding at different rates based on injury severity:

| Tier | Bleed Rate | Cause | Clotting / Resolution |
| :--- | :--- | :--- | :--- |
| **Tier 1 (Minor)** | 2 ml/s | Physical damage $\ge 2.0$ HP (excluding freeze and magic). | Clots automatically after 35 seconds (700 ticks), or treated with bandages/tourniquets. |
| **Tier 2 (Arterial)** | 20 ml/s | Severe limb trauma or Tier 2 double leg fractures. | Does not clot naturally. Spawns redstone drip particles every 8 ticks. Requires medical intervention. |
| **Tier 3 (Massive)** | 40 ml/s | Critical open trauma. | Does not clot naturally. Requires immediate medical intervention. |

#### Hemostasis & Field Treatments

Players can stop hemorrhage using dedicated bandages or makeshift items:

- **Cloth Bandage** (`humannature:cloth_bandage`):
  - Stacks to 16.
  - Channels for 1.5 seconds (30 ticks) with brush animation and wool particle cues.
  - Clears Tier 1 bleeding with zero side effects.
  - Clears Tier 2 arterial bleeding and applies 5 seconds (100 ticks) of Slowness I to simulate local tissue numbness.
  - Used on self by holding right-click in air. Applied to teammates by holding right-click while targeting the player.
  - Crafted shapelessly from 2 Wool (yields 4) or 4 String (yields 2).

- **Emergency Tourniquet (Makeshift)**:
  - Consumes 1 Leather or 1 String from the hand on right-click.
  - Instantly stops active bleeding of any tier.
  - Tight constriction applies Slowness II and Mining Fatigue I for 8 seconds (160 ticks).
  - Can be applied to oneself (right-click in air) or to a teammate (interact while targeting player).

#### Blood Regeneration & Nutrition

When not bleeding and with hunger $\ge 18$ (9 drumsticks), blood replenishes naturally:
- **Baseline recovery**: +1 ml/tick (+20 ml/s).
- **Nutritional boost**: Eating Beef, Cooked Beef, or drinking Bone Broth grants a 60-second (1,200 ticks) boost, increasing recovery to 3 ml/tick (+60 ml/s).
- **Milk hydration**: Drinking a Milk Bucket instantly restores 500 ml of blood.

---

### Co-op Blood Transfusion Engine

The Blood Bag (`humannature:blood_bag`) stores and transfers blood between players. Empty bags stack to 16. Bags holding blood become unstackable (stack size 1) and store the donor blood type and volume.

#### Specifications & Thresholds
- **Capacity**: 1,000 ml maximum.
- **Transfer step**: 500 ml per extraction or transfusion.
- **Safe extraction floor**: Donors must have $> 3,500$ ml blood. Extraction attempts at or below 3,500 ml fail to prevent donor collapse.
- **Extraction cost**: Drawing blood inflicts 1.0 HP generic needle damage and plays bottle fill audio.
- **Type locking**: A bag containing blood only accepts additional blood of the exact same ABO/Rh type.

#### Controls Matrix

| Action | Input Target | Mouse Interaction | Result |
| :--- | :--- | :--- | :--- |
| **Self Draw** | Air | Right-click (empty/partial bag) | Channels 1.5s (30 ticks, bow aim pose). Extracts 500 ml from self into bag. |
| **Self Transfuse** | Air | Shift + Right-click (filled bag) | Transfuses stored blood into self. Returns empty bag. |
| **Teammate Transfuse** | Teammate | Right-click (filled bag) | Transfuses stored blood into target player. Returns empty bag. |
| **Teammate Draw** | Teammate | Shift + Right-click (empty/partial bag) | Extracts 500 ml from target player into bag. |

#### ABO/Rh Blood Types & Compatibility

Every player is assigned one of 8 blood types on spawn:

| Recipient Type | Compatible Donor Blood Types |
| :--- | :--- |
| **O-** | O- only |
| **O+** | O-, O+ |
| **A-** | O-, A- |
| **A+** | O-, O+, A-, A+ |
| **B-** | O-, B- |
| **B+** | O-, O+, B-, B+ |
| **AB-** | O-, A-, B-, AB- |
| **AB+** | Universal Recipient (accepts all types) |

`O-` is the Universal Donor and can donate to all types.

#### Acute Hemolytic Transfusion Reaction (AHTR)

Transfusing an incompatible blood type into yourself or a teammate triggers immediate acute hemolytic shock:
- Spikes toxin buildup to 100% (`POISON` type), triggering an immediate toxic eruption.
- Inflicts 6.0 magic damage directly to the recipient.
- Applies Nausea II for 15 seconds (300 ticks), Slowness III for 15 seconds (300 ticks), and Blindness I for 5 seconds (100 ticks).

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
- Press `E` (the Inventory key) to return to the player inventory.
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
    # Ticks of continuous chopping or mining required to trigger muscle failure (200 ticks = 10s)
    exertionContinuousMiningTicks = 200
    # Ticks standing still without actions to recover 100% muscle capacity (70 ticks = 3.5s)
    exertionRestRecoveryTicks = 70
    # Attack speed penalty fraction during muscle failure (0.50 = -50%)
    exertionAttackSpeedPenalty = 0.50
    # Mining break speed penalty fraction during muscle failure (0.50 = -50%)
    exertionMiningSpeedPenalty = 0.50
    # Stamina / exertion drain per tick while sprinting (0.08 = ~1.6% per sec, ~60s to deplete)
    exertionSprintDrainPerTick = 0.08

    # Enable temporary swimming cramps from prolonged continuous swimming/diving
    enableSwimmingCramps = true
    # Ticks of continuous swimming before leg cramp occurs (600 ticks = 30s)
    swimmingCrampThresholdTicks = 600
    # Swimming speed penalty fraction during cramp (0.40 = -40%)
    swimmingCrampSpeedPenalty = 0.40
    # Ticks resting on land or boat to recover from leg cramp (60 ticks = 3s)
    swimmingCrampRecoveryTicks = 60

    # Enable gradual digestion absorption window for eaten food
    enableDigestionWindow = true
    # Interval in ticks between digestion nutrition absorption (30 ticks = 1.5s)
    digestionIntervalTicks = 30
    # Enable overeating hiccups, speed debuff, and vomiting reflex when gorging food
    enableOvereatingReflex = true
    # Enable indigestion accumulation and stomach ache from raw/rotten meat
    enableIndigestion = true
    # Enable calcium absorption from bone broth and milk accelerating bone healing
    enableCalciumHealing = true
    # Healing rate multiplier for Tier 1 fractures with active calcium buff
    calciumHealingMultiplier = 2.0

    # Enable underground claustrophobia panic, heart rate spike, and aim tremor in dark caves (Y < 0, 0 lux)
    enableClaustrophobiaPanic = true
    # Ticks in pitch dark deep underground before panic triggers (100 ticks = 5s)
    panicThresholdTicks = 100

    # Enable 4-second adrenaline surge burst upon severe bone fracture
    enableAdrenalineSurge = true
    # Duration in ticks of the adrenaline surge (80 ticks = 4.0s)
    adrenalineSurgeDurationTicks = 80

    # Enable blood circulation, hemorrhage, and transfusion system
    enableBloodSystem = true
    # Blood volume delta required before dispatching a network sync packet to client (ml)
    bloodSyncDeltaThreshold = 25
    # Natural blood recovery per second when well-fed (>= 18 hunger)
    bloodNaturalRegenPerSec = 2
    # Blood volume regenerated per tick when well-fed (>= 18 hunger)
    bloodRegenPerTick = 1
    # Blood volume restored immediately upon drinking a Milk Bucket (ml)
    bloodMilkRestoreAmount = 500
    # Blood regeneration multiplier when boosted by meat/bone broth
    bloodMeatBoostMultiplier = 3
    # Duration in ticks of the blood regeneration boost from meat/bone broth (1200 ticks = 60s)
    bloodMeatBoostDurationTicks = 1200
    # Minimum safe health floor below which blood loss cannot reduce max health
    bloodSafeMinHealth = 4.0
    # Bleeding rate per second for Tier 1 minor bleeding (ml/s)
    bleedingTier1LossPerSec = 2
    # Bleeding rate per second for Tier 2 arterial bleeding (ml/s)
    bleedingTier2LossPerSec = 20
    # Bleeding rate per second for Tier 3 massive bleeding (ml/s)
    bleedingTier3LossPerSec = 40
    # Enable craftable cloth bandages
    enableClothBandage = true
    # Enable craftable blood bags and transfusion mechanics
    enableBloodBag = true
    # Enable emergency makeshift tourniquets using string or leather
    enableEmergencyTourniquet = true
    # Duration in ticks of Slowness I numbness after bandaging Tier 2 arterial bleeding (100 ticks = 5s)
    bandageNumbnessDurationTicks = 100
    # Duration in ticks of Slowness II and Mining Fatigue I from emergency tourniquet application (160 ticks = 8s)
    tourniquetNumbnessDurationTicks = 160
    # Stored blood volume transfer capacity for blood bags (ml)
    bloodBagTransferVolume = 1000
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
