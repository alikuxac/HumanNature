# Public Extensibility API & Event Pipeline

Public API and event system for Human Nature **v0.3.0** on Minecraft 1.21.1 (NeoForge and Forge).

---

## Dependency Setup

### JitPack

[![JitPack](https://img.shields.io/badge/JitPack-Available-brightgreen.svg?style=flat-square)](https://jitpack.io/#alikuxac/HumanNature)

Add the JitPack repository and API dependency to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly "com.github.alikuxac.HumanNature:humannature-api:${humannature_version}"
}
```

---

## Core API Access

The singleton entry point is `com.alikuxac.humannature.api.HumanNatureAPI`:

```java
import com.alikuxac.humannature.api.HumanNatureAPI;

if (HumanNatureAPI.isAvailable()) {
    HumanNatureAPI api = HumanNatureAPI.getInstance();
}
```

---

## Accessors

### 1. Injury (`IInjuryAccessor`)

Query and modify limb trauma and fractures across 6 body parts (`HEAD`, `TORSO`, `MAIN_ARM`, `OFF_ARM`, `LEFT_LEG`, `RIGHT_LEG`).

```java
import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.api.injury.InjuryTier;

var injury = HumanNatureAPI.getInstance().getInjury();

int headSeverity = injury.getLimbSeverity(player, LimbType.HEAD);
injury.setLimbSeverity(player, LimbType.HEAD, 2);

boolean hasFracture = injury.hasFracture(player);
InjuryTier tier = injury.getFractureTier(player);

injury.treatLimb(player, LimbType.LEFT_LEG);
injury.clearInjuries(player);
```

### 2. Nutrition & Physiology (`INutritionAccessor`)

Read and mutate macro-nutrients (clamped to 0–100), toxin level, and physical exertion score.

```java
import com.alikuxac.humannature.api.HumanNatureAPI;

var nutrition = HumanNatureAPI.getInstance().getNutrition();

byte calcium = nutrition.getCalcium(player);
nutrition.modifyCalcium(player, 15);

byte carbs = nutrition.getCarbohydrates(player);
byte vitamins = nutrition.getVitamins(player);

float toxin = nutrition.getToxinBuildup(player);
float exertion = nutrition.getExertionScore(player);
```

### 3. Temperature (`ITemperatureAccessor`)

Query core body temperature, wetness buffer, and environmental heat proximity.

```java
import com.alikuxac.humannature.api.HumanNatureAPI;

var temp = HumanNatureAPI.getInstance().getTemperature();

float coreTemp = temp.getCoreTemperature(player);
boolean freezing = temp.isFreezing(player);
boolean overheating = temp.isOverheating(player);
boolean nearHeat = temp.isNearHeatSource(player);

float wetness = temp.getWetnessBuffer(player);
temp.setWetnessBuffer(player, 0.5f);
```

### 4. Weight & Encumbrance (`IWeightAccessor`)

Query current carried weight, max carrying capacity, and encumbrance tier.

```java
import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.weight.EncumbranceTier;

var weight = HumanNatureAPI.getInstance().getWeight();

float carried = weight.getCarriedWeight(player);
float max = weight.getMaxCapacity(player);
EncumbranceTier tier = weight.getEncumbranceTier(player);
boolean encumbered = weight.isEncumbered(player);
```

---

## Event Pipeline

Human Nature dispatches events through both its internal functional bus and native mod loader event buses (`NeoForge.EVENT_BUS` on NeoForge, `MinecraftForge.EVENT_BUS` on Forge).

### Event Types

| Event | Cancellable | Modifiable Fields | Description |
| :--- | :--- | :--- | :--- |
| `LimbInjuryEvent` | Yes | `setSeverity(int)` | Fired before injury application to a specific limb. |
| `FractureEvent` | No | None | Fired when bone fracture starts or resolves. |
| `EncumbranceChangeEvent` | No | None | Fired when total weight transitions between encumbrance tiers. |
| `HypothermiaEvent` | Yes | None | Fired when severe cold trauma applies to a player. |

### Functional Registration (Loader-Agnostic)

Register consumers directly on `HumanNatureEvents`:

```java
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.event.LimbInjuryEvent;
import com.alikuxac.humannature.api.injury.LimbType;

HumanNatureEvents.registerInjuryListener(event -> {
    if (event.getLimb() == LimbType.HEAD) {
        event.setCanceled(true);
    }
});
```

### Native Mod Loader Registration

#### NeoForge

```java
import net.neoforged.bus.api.SubscribeEvent;
import com.alikuxac.humannature.api.event.LimbInjuryEvent;

@SubscribeEvent
public void onLimbInjury(LimbInjuryEvent event) {
    if (event.getSeverity() > 1) {
        event.setSeverity(1);
    }
}
```

#### Forge

```java
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.alikuxac.humannature.api.event.HypothermiaEvent;

@SubscribeEvent
public void onHypothermia(HypothermiaEvent event) {
    event.setCanceled(true);
}
```

---

## KubeJS Integration

Third-party pack developers can access Human Nature via static helpers without compile-time imports:

```javascript
PlayerEvents.tick(event => {
    let player = event.player
    let severity = HumanNature.getLimbSeverity(player, 'HEAD')
    if (severity > 0) {
        HumanNature.modifyCalcium(player, 5)
    }
})
```
