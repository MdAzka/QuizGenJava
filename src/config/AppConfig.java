package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppConfig {

    private static Properties props = new Properties();
    private static boolean loaded = false;

    private static void load() {
        if (loaded) return;
        try (FileInputStream fis = new FileInputStream("../config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("config.properties tidak ditemukan. API Gemini tidak akan berfungsi.");
        }
        loaded = true;
    }

    public static List<String> getApiKeys() {
    load();
    List<String> keys = new ArrayList<>();
    for (int i = 1; i <= 5; i++) { // dukung sampai 5 key cadangan
        String key = props.getProperty("gemini.api.key." + i, "").trim();
        if (!key.isEmpty()) keys.add(key);
    }
    // Fallback: kalau format lama "gemini.api.key" masih dipakai, ikutkan juga
    String legacyKey = props.getProperty("gemini.api.key", "").trim();
    if (!legacyKey.isEmpty() && !keys.contains(legacyKey)) {
        keys.add(legacyKey);
    }
    return keys;
}
}