# Addon API & Events

Public API and event system for Human Nature **v0.4.1** on Minecraft 1.21.1 (NeoForge and Forge).

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

## API Access

Access the API through `com.alikuxac.humannature.api.HumanNatureAPI`:

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

#### Limb Splints (`SplintTier`)

Read the active splint for a limb:
```java
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.item.SplintTier;

SplintTier splint = InjuryModule.getSplint(player.getUUID(), LimbType.LEFT_LEG);
boolean isSplinted = splint != null;
```

### 2. Nutrition & Physiology (`INutritionAccessor`)

Read and change nutrient reserves (clamped to 0–100), toxin buildup, and exertion.

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

#### Nutrient Reserves

Calcium, carbohydrates, and vitamins are stored as `byte` reserves (0–100) in the player's `HumanNatureNutrition` NBT. Consuming an item adds the gains defined by its server datapack entry:

| Reserve | Effect at full |
| :--- | :--- |
| Calcium | fracture healing +25%, safe fall +2 blocks, fall damage −25% |
| Carbohydrates | exertion cap 100 → 150 |
| Vitamins | cold buildup −50%, toxin decay ×2 |

Each reserve loses 1 point every 30 seconds by default (`nutrientDepletionIntervalTicks = 600`). An empty reserve disables its buff without causing damage or a debuff. Buff strength scales with the reserve level. Configure depletion and buff multipliers in `ServerConfig` under `nutrition`; food gains come from datapacks.

#### Food Datapacks (0.4.1)

For item `<namespace>:<path>`, use `data/<namespace>/humannature/nutrition/<path>.json`.
For example, `data/minecraft/humannature/nutrition/apple.json`:

```json
{
  "calcium": 0,
  "carbohydrates": 8,
  "vitamins": 20
}
```

All three fields are required integers from 0 to 100, representing reserve points gained per
consumption. The highest-priority pack's file replaces the whole entry; fields are not merged.
Use three zeros to disable gains for a food. Items without an entry give zero nutrients,
regardless of their tags. Invalid entries are logged and ignored, giving zero gains unless an
addon supplies values. `/reload` replaces the server's loaded entries, including removing stale ones.

Human Nature includes presets for vanilla foods consumed from the hand, milk, and bone broth.
Placed cake slices do not use the item-consumption hook. These values balance gameplay;
they are not dietary measurements. Addons and modpacks can define entries for other item IDs
using the same directory layout. These files change nutrient reserves, not vanilla hunger,
saturation, or food effects.

#### JEI Nutrition Category (0.4.1)

With JEI installed on the client, a food's Uses view includes a Nutrition category showing
calcium, carbohydrate and vitamin gains per consumption. Foods with three zero gains are
omitted. No nutrition text is added to normal item tooltips.

The category uses the server's datapack values, synchronized on login and `/reload`. Leaving
the server clears that data. Addon listeners that calculate consumption-specific overrides
are not evaluated for this preview. JEI is optional on both loaders; the declared minimum is
`19.21.0.247` for Minecraft `1.21.1`. See [JEI Support](JEI.md) for setup and compatibility details.

#### Custom Food Gains (0.4.1)

Register a `NutritionConsumptionEvent` listener during addon initialization. It runs on the
server before nutrients are added, for any consumed item with a nonempty stack, including
items without nutrient tags. Both physiology and nutrient reserves must be enabled;
creative and spectator players are excluded.

Call `setValues(new NutritionValues(calcium, carbohydrates, vitamins))` to replace all three
gains for that consumption. Leave it uncalled to keep the datapack values, or pass `(0, 0, 0)` to
give no nutrients. Each gain and the resulting reserves are clamped to 0–100; negative gains
become zero. Null values are rejected.

For example, this listener gives apples 8 vitamins and no calcium or carbohydrates:

```java
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.nutrition.NutritionValues;
import net.minecraft.world.item.Items;

HumanNatureEvents.registerNutritionConsumptionListener(event -> {
    if (event.getStack().is(Items.APPLE)) {
        event.setValues(new NutritionValues(0, 0, 8));
    }
});
```

| Method | Returns |
| :--- | :--- |
| `getPlayer()` | The consuming player. |
| `getStack()` | A copy of the consumed stack, including its data components. Editing it does not change the original. |
| `getDefaultValues()` | The food's datapack gains, or `(0, 0, 0)` if no entry exists. Listener replacements do not change this value. |
| `getValues()` | The replacement gains, or the defaults if no listener has replaced them. |
| `isOverridden()` | Whether a listener called `setValues()`, including with all zeros. |

Register each listener once. To remove one, keep its `Consumer<NutritionConsumptionEvent>`
reference and pass it to `unregisterNutritionConsumptionListener(listener)`. This event uses
`HumanNatureEvents` on both loaders; it has no native `@SubscribeEvent` wrapper.

Datapack gains are resolved before the hook, so addons can replace them without coordinating
listener order with Human Nature. If several addons replace the same food's gains, the last
replacement wins.

The core adds the final gains once, checks `max_nutrition`, and sends one `SyncNutritionPayload`,
even if the gains are zero. The advancement triggers only when consumption fills the last
reserve to 100. Ineligible consumptions skip the event, gains, advancement check and sync.
Supply values through the event; applying them again from a separate Finish listener would
count the food twice.

`INutritionAccessor` mutators only change stored data. They clamp reserves but do not check
gameplay eligibility, sync clients or award advancements. `Player` overloads delegate by UUID.
Call them from the server thread.

Human Delight supplies Farmer's Delight food values and tooltips.
Human Nature has no dependency on Human Delight or Farmer's Delight.

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

## Events

Events are sent through `HumanNatureEvents`. Injury, fracture, encumbrance and hypothermia events
also have native wrappers on `NeoForge.EVENT_BUS` or `MinecraftForge.EVENT_BUS`.
`NutritionConsumptionEvent` uses only the common functional bus on both loaders.

### Event Types

| Event | Cancellable | Modifiable Fields | Description |
| :--- | :--- | :--- | :--- |
| `LimbInjuryEvent` | Yes | `setSeverity(int)` | Fired before injury application to a specific limb. |
| `FractureEvent` | No | None | Fired when bone fracture starts or resolves. |
| `EncumbranceChangeEvent` | No | None | Fired when total weight transitions between encumbrance tiers. |
| `HypothermiaEvent` | Yes | None | Fired when severe cold trauma applies to a player. |
| `NutritionConsumptionEvent` | No (use zero gains) | `setValues(NutritionValues)` | Replaces all nutrient gains before a server consumption is applied. |

### Registering Without a Loader Dependency

Register listeners on `HumanNatureEvents`:

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

### Registering on the Loader Event Bus

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

Modpack scripts can use the static helpers without compile-time imports:

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

## Enums & Formatting

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

`NetworkHandler` sends the following custom payloads:

| Payload ID | Direction | Purpose | Contents |
| :--- | :--- | :--- | :--- |
| `humannature:treat_limb` (`TreatLimbPayload`) | Client $\rightarrow$ Server | Sent when a player clicks an injured limb to apply a splint from their inventory. | `byte limbIndex` |
| `humannature:sync_injury` (`SyncInjuryPayload`) | Server $\rightarrow$ Client | Updates the client's limb injuries and active splint tiers. | 6 limb severities + 6 limb splint tier indices |

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
