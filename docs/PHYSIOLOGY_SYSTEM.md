# Physiology System

Tracks internal bodily state, including sleep deprivation, involuntary darkness blinks, and a Soulslike bad effect accumulation gauge (Toxin Buildup).

---

## Mechanics

### 1. Sleep Deprivation & Fatigue Score

The server tracks consecutive awake ticks for each player when they are not sleeping in a bed.

- **Accumulation Rate**: Every 24,000 server ticks (1 in-game day) awake adds approximately $33.33\%$ to the hidden `fatigueScore`.
- **Full Exhaustion**: Reaches $100\%$ ($1.0$) after 72,000 server ticks (3 consecutive in-game days awake) and clamps at $100\%$.
- **Sleep Pausing**: Ticks spent actively sleeping in a bed do not increment awake time.
- **Game Mode Restrictions**: Creative and Spectator players do not accumulate fatigue.

| Awake Time | Server Ticks | Fatigue Score | Status |
| :--- | :--- | :--- | :--- |
| **0 Days** | 0 | $0.0\%$ | Rested |
| **1 Day** | 24,000 | $33.3\%$ | Mild fatigue |
| **2 Days** | 48,000 | $66.7\%$ | Moderate fatigue |
| **2.4 Days** | 57,600 | $80.0\%$ | Severe fatigue (Darkness blinks unlock) |
| **3 Days+** | 72,000+ | $100.0\%$ | Complete exhaustion |

---

### 2. Involuntary Darkness Blinks

When severe fatigue sets in, the player begins experiencing involuntary micro-sleep episodes while traversing the world.

- **Trigger Threshold**: `fatigueScore >= 80%` ($\ge 57,600$ awake ticks).
- **Movement Requirement**: Blinks only trigger while the player is moving (horizontal movement delta $> 0.001$). Stationary players do not blink.
- **Effect**: Applies `minecraft:darkness` for exactly 20 ticks (1.0 second) without particles or ambient sounds.
- **Interval & Cooldown**: Once a blink finishes, the next blink is scheduled after a random interval between 300 ticks (15 seconds) and 800 ticks (40 seconds).

---

### 3. Soulslike Toxin Buildup Gauge (Poison & Wither Interception)

Direct applications of harmful status effects (`Poison` and `Wither`) are intercepted and converted into points filling a hidden-to-transient buildup gauge ($0–100\%$).

- **Interception Scope**: Exclusively intercepts `minecraft:poison` and `minecraft:wither`. Other effects proceed normally.
- **Buildup Calculation**: Each hit adds buildup scaled with effect duration and amplifier, with a configurable minimum baseline (default: $25\%$ per exposure).
- **Full Outbreak (100% Threshold)**: Only when the buildup gauge reaches $100\%$ does the effect officially erupt onto the player with its accumulated duration and amplifier. Upon outbreak, the buildup resets to $0\%$.
- **Passive Decay**: After a delay without receiving new toxin exposure (default: 80 ticks / 4 seconds), buildup starts passively draining at $2.0\%$ per second.
- **Heat & Rest Acceleration**: Standing near active heat sources (lit campfires, fire, soul fire, lava, magma blocks) or resting (sleeping or crouching while standing still) triples the decay rate ($3.0\times$, approx. $6.0\%$ per second).
- **Item Counterplay**:
  - **Milk Bucket**: Instantly clears $100\%$ of the accumulated buildup gauge.
  - **Honey Bottle**: Reduces current buildup by $50\%$.

---

### 4. Transient HUD Overlay & Diagnostic Tab

#### Transient HUD Bar (Auto-Hide)
- **Normal State**: $100\%$ clean screen; nothing renders when buildup is zero.
- **Active State**: When buildup $> 0\%$, a minimalist $3\text{px}$ high gauge appears centered immediately above the hotbar.
  - **Poison**: Toxic Emerald gradient (`#00E676`).
  - **Wither**: Necrotic Violet gradient (`#BA68C8`).
- **Fade Out**: When buildup reaches $0\%$, the bar smoothly fades out over exactly 1.0 second ($1000\text{ms}$) before disappearing completely.

#### Diagnostic Tab (`H` Key)
Inside the player inventory diagnostic panel:
- **`Fatigue Level`**: Displays current awake percentage, color label, and visual bar.
- **`Toxin Buildup`**: Displays current toxin percentage, active type (`Poison`, `Wither`, or `Clear`), and gauge bar.
- **Interactive Tooltips**: Hovering over either section reveals comprehensive metrics (awake days, micro-sleep readiness, passive decay cooldown, heat bonuses, item counterplay hints, and outbreak warnings).

---

### 5. Recovery & Reset Triggers

- **Sleeping Through the Night**: Waking up after sleeping $\ge 90$ consecutive ticks in a bed when morning arrives resets `awakeTicks` and `fatigueScore` to 0. Exiting a bed early without passing the night does not clear fatigue.
- **Death & Respawn**: Dying resets the player's awake counter, fatigue score, and toxin buildup back to 0.

---

## Configuration

Server settings are located in `config/humannature-server.toml`:

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
```

Client settings are located in `config/humannature-client.toml`:

```toml
[hud]
    # Show transient soulslike toxin buildup bar above hotbar
    enableToxinHudOverlay = true
```
