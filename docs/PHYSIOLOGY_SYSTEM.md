# Physiology System

Tracks internal bodily state, including sleep deprivation, fatigue accumulation, and involuntary darkness blinks.

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

### 3. Recovery & Reset Triggers

- **Sleeping Through the Night**: Waking up after sleeping $\ge 90$ consecutive ticks in a bed when morning arrives resets `awakeTicks` and `fatigueScore` to 0. Exiting a bed early without passing the night does not clear fatigue.
- **Death & Respawn**: Dying resets the player's awake counter and fatigue score back to 0.

---

### 4. Diagnostic Screen Integration

The Diagnostic Screen (`H` key) displays current fatigue metrics directly below limb trauma status:

- **Fatigue Percentage & Label**: Color-coded status (`Rested`, `Mild`, `Tired`, `Drowsy`).
- **Fatigue Progress Bar**: Visual bar reflecting current accumulation towards exhaustion.
- **Detailed Tooltip**: Hovering over the fatigue section displays total awake in-game days, micro-sleep activity status, and bed resting instructions.

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
```
