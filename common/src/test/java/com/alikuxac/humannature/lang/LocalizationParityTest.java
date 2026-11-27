package com.alikuxac.humannature.lang;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationParityTest {

    private static final String[] LOCALES = {
            "en_us",
            "vi_vn",
            "zh_cn",
            "ru_ru",
            "es_es",
            "pt_br",
            "de_de"
    };

    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Gson GSON = new Gson();

    private Map<String, String> loadLocale(String locale) {
        String path = "/assets/humannature/lang/" + locale + ".json";
        InputStream stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Localization file not found on classpath: " + path);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Map<String, String> map = GSON.fromJson(reader, MAP_TYPE);
            assertNotNull(map, "Failed to parse JSON for locale: " + locale);
            return map;
        } catch (Exception e) {
            fail("Exception reading locale " + locale + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Test
    void testAllLocalesExistAndHaveExactKeyParity() {
        Map<String, String> enUs = loadLocale("en_us");
        assertFalse(enUs.isEmpty(), "en_us.json must not be empty");
        Set<String> baseKeys = enUs.keySet();

        for (String locale : LOCALES) {
            Map<String, String> current = loadLocale(locale);
            assertFalse(current.isEmpty(), locale + ".json must not be empty");

            Set<String> missing = new TreeSet<>(baseKeys);
            missing.removeAll(current.keySet());
            assertTrue(missing.isEmpty(), "Locale [" + locale + "] is missing keys: " + missing);

            Set<String> extra = new TreeSet<>(current.keySet());
            extra.removeAll(baseKeys);
            assertTrue(extra.isEmpty(), "Locale [" + locale + "] has extra keys: " + extra);

            for (Map.Entry<String, String> entry : current.entrySet()) {
                assertNotNull(entry.getValue(), "Locale [" + locale + "] key " + entry.getKey() + " has null value");
                assertFalse(entry.getValue().trim().isEmpty(), "Locale [" + locale + "] key " + entry.getKey() + " is blank");
            }
        }
    }

    @Test
    void testFormatSpecifierParity() {
        Map<String, String> enUs = loadLocale("en_us");

        for (String locale : LOCALES) {
            if ("en_us".equals(locale)) continue;
            Map<String, String> current = loadLocale(locale);

            for (Map.Entry<String, String> entry : enUs.entrySet()) {
                String key = entry.getKey();
                String enVal = entry.getValue();
                String locVal = current.get(key);

                int enCount = countPercentTokens(enVal);
                int locCount = countPercentTokens(locVal);

                assertEquals(enCount, locCount,
                        "Format token count mismatch in [" + locale + "] for key [" + key + "]. Expected " + enCount + " but got " + locCount +
                                "\nEN: " + enVal + "\nLOC: " + locVal);
            }
        }
    }

    private static int countPercentTokens(String text) {
        if (text == null) return 0;
        int count = 0;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) == '%') {
                if (i + 1 < len && text.charAt(i + 1) == '%') {
                    i++;
                } else {
                    count++;
                }
            }
        }
        return count;
    }
}

