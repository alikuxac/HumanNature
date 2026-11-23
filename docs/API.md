# Public Extensibility API & Event Pipeline

Public API and event system for Human Nature **v0.4.0** on Minecraft 1.21.1 (NeoForge and Forge).

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

byte headSeverity = injury.getLimbSeverity(player, LimbType.HEAD);
injury.setLimbSeverity(player, LimbType.HEAD, 2);

boolean hasFracture = injury.hasFracture(player);
InjuryTier tier = injury.getFractureTier(player);

injury.treatLimb(player, LimbType.LEFT_LEG);
injury.clearInjuries(player);
```

#### Limb Splint Management (`SplintTier`)
Query active splints per limb (`SplintTier`):
```java
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.item.SplintTier;

SplintTier splint = InjuryModule.getSplint(player.getUUID(), LimbType.LEFT_LEG);
boolean isSplinted = splint != null;
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

#### 3-Tier Reserve Buffs

Three `byte` reserves (0–100) live in `HumanNatureNutrition` player NBT: calcium, carbohydrates, vitamins. Tagged foods fill them on `LivingEntityUseItemEvent.Finish`:

| Reserve | Tag | Effect at full |
| :--- | :--- | :--- |
| Calcium | `#c:foods/calcium_rich` | fracture healing +25%, safe fall +2 blocks, fall damage −25% |
| Carbohydrates | `#c:foods/carbohydrates` | exertion cap 100 → 150 |
| Vitamins | `#c:foods/vegetables`, `#c:foods/fruits` | cold buildup −50%, toxin decay ×2 |

Each reserve drops by 1 every `nutrientDepletionIntervalTicks` (default 150). Empty just turns the buff off — no damage or debuff. Strength scales with the current reserve; gains, intervals and multipliers are in `ServerConfig` under `nutrition`.

### 3. Blood (`IBloodAccessor`)

Query and modify blood volume, ABO/Rh type, and active bleeding tier. Volume is clamped to 0–5,000 ml.

```java
import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.blood.BloodType;

var blood = HumanNatureAPI.getInstance().getBlood();

short ml = blood.getBloodVolume(player);
blood.modifyBloodVolume(player, -500);
BloodType type = blood.getBloodType(player);
blood.setBloodType(player, BloodType.O_POSITIVE);
byte bleedingTier = blood.getBleedingTier(player);
blood.setBleedingTier(player, (byte) 0);
```

Every method also accepts a `UUID` for offline-capable data access.

### 4. Temperature (`ITemperatureAccessor`)

Read and write core body temperature, wetness buffer, and heat source proximity. Core temperature lives in `PlayerTemperatureData`, clamped to `[25.0°C, 45.0°C]` around a `37.0°C` baseline.

```java
import com.alikuxac.humannature.api.HumanNatureAPI;

var temp = HumanNatureAPI.getInstance().getTemperature();

// Core body temperature
float coreTemp = temp.getCoreTemperature(player);
temp.setCoreTemperature(player, 38.5f);
temp.modifyCoreTemperature(player, -0.5f);

boolean freezing = temp.isFreezing(player);
boolean overheating = temp.isOverheating(player);
boolean nearHeat = temp.isNearHeatSource(player);

// Wetness buffer (0.0 to 1.0)
float wetness = temp.getWetnessBuffer(player);
temp.setWetnessBuffer(player, 0.5f);
```

### 5. Weight & Encumbrance (`IWeightAccessor`)

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

---

## Domain Enums & Formatting

| Enum | Translation Key Pattern | Style / Formatting | Description |
| :--- | :--- | :--- | :--- |
| `LimbType` | `humannature.limb.<name>` | `ChatFormatting` | Represents 6 tracked body parts (`HEAD`, `TORSO`, `MAIN_ARM`, etc.). |
| `EncumbranceTier` | `humannature.encumbrance.<name>` | Green, Yellow, Gold, Red | Carried weight load tier (`NORMAL`, `LIGHT`, `MEDIUM`, `OVERBURDENED`). |
| `TraumaSeverity` | `humannature.severity.<name>` | Gray, Gold, Dark Red | Injury degree (`NONE`, `SPRAIN`, `FRACTURE`). |
| `SplintTier` | `humannature.splint_tier.<name>` | Wood, Iron, Gold, Diamond, Netherite | Medical splint grade (`WOODEN`, `REINFORCED`, `GOLDEN`, `DIAMOND`, `NETHERITE`). |
| `ToxinType` | `humannature.toxin.<name>` | Dark Green, Dark Purple | Active poison or wither toxin accumulation. |
| `HudMode` | `humannature.hud_mode.<name>` | White, Aqua, Gold | HUD overlay display mode (`OFF`, `PHYSIOLOGY`, `ENVIRONMENT`, `INJURY`, `WEIGHT`, `COMBINED`). |

---

## Network Payloads

Custom payloads dispatched via `NetworkHandler`:

| Payload ID | Direction | Purpose | Contents |
| :--- | :--- | :--- | :--- |
| `humannature:treat_limb` (`TreatLimbPayload`) | Client $\rightarrow$ Server | Dispatched when clicking an injured limb in the body silhouette UI to treat with an inventory splint. | `byte limbIndex` |
| `humannature:sync_injury` (`SyncInjuryPayload`) | Server $\rightarrow$ Client | Synchronizes limb trauma severities and active splint tiers to the client. | 6 limb severities + 6 limb splint tier indices |

---

## Data Tags

Item tags for datapacks and modpacks:

| Tag | Target | Purpose |
| :--- | :--- | :--- |
| `#humannature:calcium_foods` | Items | Foods that trigger or refresh the `Osteogenesis` fracture healing buff (3 minutes, 2.0x healing rate). |
| `#humannature:insulating_armor` | Items / Armor | Wearables providing thermal insulation against rain wetness and hypothermia freeze ticks. Defaults to `#minecraft:freeze_immune_wearables`. |
| `#c:metallic_items` / `#humannature:metallic_items` | Items / Armor | Metallic items that accelerate cold conduction in sub-zero biomes and overheat under heavy physical loads. |
| `#humannature:enchantable/load_bearing` | Items / Containers | Armor pieces, Shulker Boxes, and Bundles eligible for the `Load Bearing` enchantment. |
| `#c:heavy_blocks` / `#humannature:heavy_blocks` | Items / Blocks | High-density blocks calculated at 0.8 kg/item. |
| `#c:light_items` / `#humannature:light_items` | Items | Low-density items calculated at 0.005 kg/item. |
| `#humannature:hot_stews` | Items / Soups | Hot soups granting the `Internal Warming` buff. |
