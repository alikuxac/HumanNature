# Advancements

Human Nature adds one advancement tab with nine milestones. All of them run through a single trigger, `humannature:gameplay_event`, so the data files live in `common/src/main/resources/data/humannature/advancement/` and work the same on Forge and NeoForge.

## Milestones

| ID | Unlocks when | Parent |
| --- | --- | --- |
| `root` | Joining a world (`minecraft:tick`) | — |
| `first_aid` | A splint is applied, by item use or diagnostic | `root` |
| `iron_bones` | A fracture finishes healing naturally after calcium sped it up | `first_aid` |
| `heavy_is_the_head` | Entering the Overburdened tier | `root` |
| `pack_mule` | Equipping armor with Load Bearing at its max level | `heavy_is_the_head` |
| `cold_snap` | Hypothermia debuff turns on | `root` |
| `heatstroke` | Reserved, see below | `cold_snap` |
| `life_giver` | Drawing your own blood, or a compatible transfusion that raises someone's volume | `root` |
| `balanced_diet` | Calcium, carbohydrates and vitamins all at 100 | `life_giver` |

Parents only draw the tree; a milestone does not need its parent to be earned. Custom milestones are server-side and skip creative/spectator players. The root still goes to everyone.

A few details worth knowing:

- **Iron Bones** needs real calcium-assisted natural healing during that fracture. The buff can wear off before the fracture finishes and it still counts. A splint cure, death or a command clear wipes the eligibility.
- **Pack Mule** checks equipped armor on equipment changes and on login, using the enchantment's registry max level. Carrying the armor in your inventory does nothing.
- **Life Giver** ignores self-transfusion, drawing from a teammate, incompatible blood, and recipients whose volume cannot go up.
- **Balanced Diet** fires on the food consumption that fills the last reserve, not on a tick that happens to see all three full.

## Data-pack contract

Every custom milestone uses the same criterion shape:

```json
{
  "trigger": "humannature:gameplay_event",
  "conditions": {
    "event_id": "splint_applied"
  }
}
```

`event_id` is required and matched exactly. `player` is optional and takes a vanilla entity predicate. Addons can emit events from common server code with `HumanNatureAdvancements.trigger(player, eventId)`; unknown IDs are fine and can be used by addon-defined criteria. There is no polling loop and no network packet for this.

Built-in IDs: `splint_applied`, `fracture_healed`, `overburdened`, `max_load_bearing`, `hypothermia`, `heat_exhaustion`, `blood_transfused`, `max_nutrition`.

`heat_exhaustion` has no emitter yet. There is no hot-climate body-temperature model, so heat, fire and hot biomes will never award `heatstroke` in this release. The definition and translations are there for when that mechanic lands, and `/advancement grant @s only humannature:heatstroke` can verify the resource itself.

Titles and descriptions use `advancements.humannature.<id>.title` / `.description`, translated in `en_us` and `vi_vn`.

## Verification

```
rtk .\gradlew.bat :common:test :forge:build :neoforge:build
```

In game, on both loaders: join a survival world, check `L` shows the tree, then trigger a few milestones and make sure the negative cases above do not award anything. Reconnect and `/reload` to confirm progress persists and the log stays clean.
