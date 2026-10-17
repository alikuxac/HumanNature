# Physiology System

The physiology module tracks sleep deprivation and replaces instant poison/wither damage with an accumulating toxin gauge.

---

## Mechanics

### Sleep Deprivation & Fatigue

The server tracks consecutive awake ticks for survival/adventure players who are out of bed. Every 24,000 ticks (one in-game day) adds 33.3% to the player's hidden `fatigueScore`, capping at 100% (1.0) after 72,000 ticks (three in-game days). Ticks spent sleeping in bed pause this counter.

| Awake Time | Server Ticks | Fatigue Score | Symptoms |
| :--- | :--- | :--- | :--- |
| **0 Days** | 0 | 0% | Rested |
| **1 Day** | 24,000 | 33.3% | Mild fatigue |
| **2 Days** | 48,000 | 66.7% | Moderate fatigue |
| **2.4 Days** | 57,600 | 80.0% | Severe fatigue (involuntary blinks start) |
| **3+ Days** | 72,000+ | 100.0% | Complete exhaustion (2x toxin vulnerability) |

#### Involuntary Darkness Blinks
Once fatigue hits 80%, moving players experience periodic 1-second (20-tick) Darkness blinks. Stationary players do not blink. Once a blink ends, the next blink triggers after a random cooldown between 300 and 800 ticks (15 to 40 seconds).

---

### Toxin Buildup (Poison & Wither Interception)

Incoming `minecraft:poison` and `minecraft:wither` effects do not apply status effects immediately. Instead, each hit adds points to a hidden 0–100% buildup gauge. All other status effects apply normally.

#### Buildup Calculation & State Scaling
Each exposure calculates a base buildup from the effect's duration and amplifier, with a configurable minimum (default 25%):

$$\text{BaseValue} = \max(\text{baseHit}, \min(100, \text{durationFactor} \times \text{amplifierFactor}))$$

This base value is then scaled by the player's exhaustion and physical injury:

$$\Delta\text{Buildup} = \text{BaseValue} \times \left(1.0 + \frac{\text{FatigueScore}}{100}\right) \times (\text{isFractured} \mathbin{?} 1.5 : 1.0)$$

- **Fatigue scaling**: Rested players take 1.0x buildup. Full sleep deprivation (100% fatigue) doubles incoming buildup (2.0x).
- **Fracture multiplier**: Having any active bone fracture adds a 1.5x multiplier.
- **Combined vulnerability**: A player who is both fully exhausted and fractured accumulates toxins 3.0x faster than normal.

#### Eruption
When buildup reaches 100%, the intercepted effect erupts onto the player with the full accumulated duration and amplifier, and the gauge resets to 0%.

#### Passive Decay & Heat Acceleration
If the player takes no new toxin hits for 80 ticks (4 seconds), buildup drains passively at 2.0% per second.

Resting (sleeping, or crouching while standing still) or standing within 2 blocks of heat sources (fire, soul fire, campfires, lava, magma blocks) triples the decay rate to 6.0% per second.

#### Item Counterplay
- **Milk Bucket**: Clears 100% of accumulated buildup instantly.
- **Honey Bottle**: Reduces current buildup by 50%.

---

### HUD Overlay & Diagnostic Tab

#### Transient HUD Bar
- **Clean screen**: Nothing renders when buildup is at 0%.
- **Active gauge**: When buildup exceeds 0%, a 3px bar appears centered above the hotbar (green for Poison, purple for Wither).
- **Smooth fade**: When buildup returns to 0%, the bar fades out over 1.0 second.

#### Diagnostic Tab (`H` Key)
The inventory diagnostic panel displays:
- **Fatigue Level**: Current awake percentage, severity rating, and progress bar.
- **Toxin Buildup**: Current accumulation percentage, active toxin type, and passive decay countdown.
- **Vulnerability Warning**: Highlights active buildup multipliers when the player is fatigued or fractured.

---

### Reset Triggers

- **Sleeping through the night**: Passing the night in a bed (at least 90 ticks) resets awake ticks and fatigue to 0. Waking up early before morning does not clear fatigue.
- **Death & respawn**: Dying clears awake ticks, fatigue score, active toxin buildup, and eruption flags.

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
```

Client settings are in `config/humannature-client.toml`:

```toml
[hud]
    # Show transient soulslike toxin buildup bar above hotbar
    enableToxinHudOverlay = true
```
