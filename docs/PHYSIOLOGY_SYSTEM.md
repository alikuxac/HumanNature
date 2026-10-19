# Physiology System

The physiology module tracks sleep deprivation, handles stamina and muscle fatigue, and replaces instant poison/wither damage with an accumulating toxin gauge.

---

## Mechanics

### Sleep Deprivation & Fatigue

The server tracks consecutive awake ticks for survival and adventure players who are out of bed. Every 24,000 ticks (one in-game day) adds 33.3% to the player's hidden `fatigue score`, capping at 100% (1.0) after 72,000 ticks (three in-game days). Sleeping in bed pauses this counter.

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

Incoming `minecraft:poison` and `minecraft:wither` hits add to a hidden 0–100% buildup gauge instead of applying status effects immediately. Other status effects apply normally.

#### Buildup Calculation & State Scaling
Each exposure calculates base buildup from the effect duration and amplifier, with a configurable minimum (default 25%):

$$\text{BaseValue} = \max(\text{baseHit}, \min(100, \text{durationFactor} \times \text{amplifierFactor}))$$

Exhaustion and physical injuries scale this base value:

$$\Delta\text{Buildup} = \text{BaseValue} \times \left(1.0 + \frac{\text{FatigueScore}}{100}\right) \times (\text{isFractured} \mathbin{?} 1.5 : 1.0)$$

- **Fatigue scaling**: Rested players take 1.0x buildup. Full sleep deprivation (100% fatigue) doubles incoming buildup (2.0x).
- **Fracture multiplier**: Having any active bone fracture adds a 1.5x multiplier.
- **Combined vulnerability**: A player who is both fully exhausted and fractured accumulates toxins 3.0x faster than normal.

#### Eruption
At 100% buildup, the intercepted effect triggers with full accumulated duration and amplifier, resetting the gauge to 0%.

#### Passive Decay & Heat Acceleration
After 80 ticks (4 seconds) without new toxin hits, buildup drains at 2.0% per second.

Resting (sleeping or crouching while still) or standing within 2 blocks of heat sources (fire, soul fire, campfires, lava, magma blocks) triples decay to 6.0% per second.

#### Item Counterplay
- **Milk Bucket**: Clears 100% of accumulated buildup instantly.
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
- Move onto dry ground (`!isInWater && onGround`) or into a vehicle.
- Resting on land or in a boat for 3 seconds (60 ticks) clears the cramp.

---

### HUD & Diagnostic Interface

#### Transient HUD Overlays
- **Stamina Crosshair Arc**: When stamina is below 100%, a radial arc renders right of the crosshair (radius 19–23 px, angle -55° to +55°), draining top to bottom. Fades out over 1.5 seconds once stamina reaches 100%.
  - **Colors**: `#FFD23F` (yellow, normal), `#FF7043` (orange, < 25%), pulsing `#FF0000` (red, muscle failure), `#00E5FF` (cyan, swimming cramp).
- **Toxin Buildup Bar**: When toxin buildup is above 0%, renders a horizontal bar above the hotbar (green for Poison, purple for Wither). Renders independently of the stamina arc.

#### Diagnostic Tab (`H` Key)
The inventory diagnostic panel displays:
- **Fatigue**: Awake time percentage, severity rating, and progress bar.
- **Toxin Buildup**: Current percentage, active toxin type, and decay countdown.
- **Stamina & Muscles**: Muscle status (Rested, Strained, Muscle Failure), remaining stamina bar, and swimming cramp indicator.
- **Vulnerability Multipliers**: Active buildup scaling from fatigue or bone fractures.

---

### Reset Triggers

- **Sleeping through the night**: Passing the night in a bed (at least 90 ticks) resets awake ticks, fatigue, exertion, and cramps to 0. Waking up early before morning does not clear fatigue.
- **Death & respawn**: Dying clears awake ticks, fatigue score, active toxin buildup, eruption flags, muscle failure, and leg cramps.

---

## Configuration

Server settings are in `config/humannature-server.toml`:

```toml
[physiology]
    # Enable sleep deprivation and fatigue buildup
    enableSleepDeprivation = true
    # Ticks of being awake required to reach 100% fatigue (72000 ticks = 3 in-game days)
    # Range: 1200 ~ 720000
    sleepDeprivationFullFatigueTicks = 72000
    # Fatigue score threshold (0.0 to 1.0) above which darkness blinks begin occurring
    # Range: 0.0 ~ 1.0
    sleepFatigueBlinkThreshold = 0.8
    # Minimum interval in ticks between darkness blinks (20 ticks = 1s)
    # Range: 20 ~ 72000
    sleepFatigueBlinkMinIntervalTicks = 300
    # Maximum interval in ticks between darkness blinks
    # Range: 40 ~ 72000
    sleepFatigueBlinkMaxIntervalTicks = 800

    # Enable soulslike bad effect (poison/wither) buildup and transient gauge
    enableToxinBuildup = true
    # Base minimum toxin buildup percentage added per exposure
    # Range: 1.0 ~ 100.0
    toxinBaseBuildupPerHit = 25.0
    # Ticks of delay without exposure before toxin buildup begins decaying (80 ticks = 4s)
    # Range: 1 ~ 72000
    toxinDecayDelayTicks = 80
    # Passive percentage decay of toxin buildup per second
    # Range: 0.1 ~ 100.0
    toxinDecayRatePerSecond = 2.0
    # Toxin decay rate multiplier when near heat sources (campfires, lava) or resting
    # Range: 1.0 ~ 20.0
    toxinRestingHeatDecayMultiplier = 3.0
    # Enable toxin buildup rate scaling based on sleep deprivation and fatigue score
    enableToxinFatigueScaling = true
    # Toxin buildup multiplier when the player suffers from bone fractures or trauma
    # Range: 1.0 ~ 5.0
    toxinFractureBuildupMultiplier = 1.5

    # Enable physical exertion and muscle failure
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
```

Client settings are in `config/humannature-client.toml`:

```toml
[hud]
    # Show injury & trauma status HUD overlay on screen
    enableHudOverlay = true
    # Screen X position of the injury HUD overlay
    hudX = 10
    # Screen Y position of the injury HUD overlay
    hudY = 10
    # Show transient soulslike toxin buildup bar above hotbar
    enableToxinHudOverlay = true
    # Show transient stamina crosshair arc overlay
    enableStaminaHudOverlay = true

[camera]
    # Enable camera wobble/tilt effect when walking with fractured leg(s)
    enableCameraLimping = true
```
