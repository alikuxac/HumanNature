package com.alikuxac.humannature.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdvancementResourcesTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final Map<String, String> EVENTS = Map.of(
            "first_aid", "splint_applied", "iron_bones", "fracture_healed",
            "heavy_is_the_head", "overburdened", "pack_mule", "max_load_bearing",
            "cold_snap", "hypothermia", "heatstroke", "heat_exhaustion",
            "life_giver", "blood_transfused", "balanced_diet", "max_nutrition");

    @Test
    void allMilestonesHaveResolvableParentsAndBothTranslations() throws Exception {
        JsonObject english = read("assets/humannature/lang/en_us.json");
        JsonObject vietnamese = read("assets/humannature/lang/vi_vn.json");
        Set<String> ids = new java.util.HashSet<>(EVENTS.keySet());
        ids.add("root");
        for (String id : ids) {
            JsonObject advancement = read("data/humannature/advancement/" + id + ".json");
            JsonObject display = advancement.getAsJsonObject("display");
            assertTrue(display.getAsJsonObject("icon").get("id").getAsString().contains(":"));
            for (String field : new String[]{"title", "description"}) {
                String key = display.getAsJsonObject(field).get("translate").getAsString();
                assertEquals("advancements.humannature." + id + "." + field, key);
                assertFalse(english.get(key).getAsString().isBlank(), key);
                assertFalse(vietnamese.get(key).getAsString().isBlank(), key);
            }
            if (!id.equals("root")) {
                String parent = advancement.get("parent").getAsString();
                assertTrue(parent.startsWith("humannature:"));
                assertTrue(ids.contains(parent.substring("humannature:".length())), parent);
                assertNotEquals("humannature:" + id, parent);
                JsonObject criterion = advancement.getAsJsonObject("criteria").entrySet()
                        .iterator().next().getValue().getAsJsonObject();
                assertEquals("humannature:gameplay_event", criterion.get("trigger").getAsString());
                assertEquals(EVENTS.get(id), criterion.getAsJsonObject("conditions").get("event_id").getAsString());
                assertFalse(display.get("hidden").getAsBoolean());
            }
        }
    }

    @Test
    void nutrientFoodTagsContainOnlyExistingItems() throws Exception {
        for (String tag : new String[]{"calcium_rich", "carbohydrates", "vegetables", "fruits"}) {
            for (var entry : read("data/c/tags/item/foods/" + tag + ".json").getAsJsonArray("values")) {
                String id = entry.getAsString();
                if (id.startsWith("minecraft:")) {
                    assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id)),
                            "Unknown nutrient food in " + tag + ": " + id);
                }
            }
        }
    }

    @Test
    void rootUnlocksOnTickWithoutAnnouncements() throws Exception {
        JsonObject root = read("data/humannature/advancement/root.json");
        assertFalse(root.has("parent"));
        JsonObject display = root.getAsJsonObject("display");
        assertFalse(display.get("show_toast").getAsBoolean());
        assertFalse(display.get("announce_to_chat").getAsBoolean());
        assertEquals("minecraft:tick", root.getAsJsonObject("criteria").entrySet()
                .iterator().next().getValue().getAsJsonObject().get("trigger").getAsString());
    }

    private JsonObject read(String path) throws Exception {
        var stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing resource: " + path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
