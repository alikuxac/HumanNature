# JEI Support

Human Nature 0.4.1 adds an optional JEI Nutrition category for Minecraft 1.21.1.

## Versions & Installation

| Component | Version |
| :--- | :--- |
| Minecraft | 1.21.1 |
| JEI API used to build the integration | 19.21.0.247 |
| Declared JEI dependency range | 19.21.0.247 and newer |
| Loaders | Forge and NeoForge |

Install the JEI build for Minecraft 1.21.1 and your loader in the client's `mods` folder. JEI is not bundled with Human Nature and is not required on a dedicated server. Human Nature's nutrition system works without JEI.

Both loader metadata files accept JEI `19.21.0.247` and newer. This range does not mean every later JEI release has been tested.

## Viewing Nutrition

Open a food's **Uses** view in JEI and select **Nutrition**. It shows reserve points gained per consumption:

- Calcium
- Carbohydrates
- Vitamins

These are Human Nature's gameplay values, not real-world dietary measurements or vanilla hunger and saturation values. Normal item tooltips are unchanged.

The category reads the server's food nutrition datapacks. Data syncs on login and after `/reload`, so modpack overrides also appear in JEI. Leaving the server clears the displayed data.

An item appears only when it exists in the client registry and has at least one positive nutrient gain. Items without an entry, and entries with three zeros, are omitted. Reloading can add or hide entries as their values change.

The display previews datapack values. It does not run `NutritionConsumptionEvent` listeners, so an addon's per-player or per-consumption overrides may differ from the values shown. See [Food Datapacks](API.md#food-datapacks-041) and [Custom Food Gains](API.md#custom-food-gains-041) for those contracts.

## Other Recipe Viewers

Human Nature currently includes a JEI plugin only. There is no native EMI or REI integration. This category has not been verified through other viewers or compatibility bridges.

## Development

The common module compiles against the JEI API without bundling it. Add `-PwithJei=true` to a loader's development client command to include JEI at runtime:

```powershell
.\gradlew.bat :forge:runClient -PwithJei=true
.\gradlew.bat :neoforge:runClient -PwithJei=true
```

Without that property, the development runs do not add JEI. The version comes from `jei_version` in `gradle.properties`.
