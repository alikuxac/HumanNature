# Advancements

Human Nature has nine advancements in one tab. Both loaders use `humannature:gameplay_event`, with data files in `common/src/main/resources/data/humannature/advancement/`.

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

Parent links control the tree layout; advancements can be earned in any order. Custom triggers run on the server for survival and adventure players. Everyone receives the root.

Some conditions need more than the action listed above:

- **Iron Bones** requires calcium to have sped up natural healing for that fracture. The buff may expire before healing finishes. A splint cure, death or clearing the fracture with a command resets eligibility.
- **Pack Mule** checks equipped armor on login and equipment changes against the enchantment's registry max level. Armor carried in the inventory does not count.
- **Life Giver** excludes self-transfusion, drawing from a teammate, incompatible blood and transfusions that cannot raise the recipient's blood volume.
- **Balanced Diet** is awarded when eating fills the last nutrient reserve to 100.

## Datapack Criteria

Custom milestones use this criterion:

```json
{
  "trigger": "humannature:gameplay_event",
  "conditions": {
    "event_id": "splint_applied"
  }
}
```

`event_id` is required and matched exactly. The optional `player` field takes a vanilla entity predicate. Addons can call `HumanNatureAdvancements.trigger(player, eventId)` from common server code, including with their own event IDs.

Built-in IDs: `splint_applied`, `fracture_healed`, `overburdened`, `max_load_bearing`, `hypothermia`, `heat_exhaustion`, `blood_transfused`, `max_nutrition`.

`heatstroke` is reserved: no code emits `heat_exhaustion` yet, so heat, fire and hot biomes do not award it. Use `/advancement grant @s only humannature:heatstroke` to check its definition and translations.

Titles and descriptions use `advancements.humannature.<id>.title` / `.description`, translated in `en_us` and `vi_vn`.

## Verification

```
rtk .\gradlew.bat :common:test :forge:build :neoforge:build
```

On each loader, join a survival world and press `L` to check the tree. Test the milestone conditions and exclusions above, then reconnect and run `/reload`. Check that progress is saved and no errors appear in the log.
