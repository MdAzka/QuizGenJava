package config;

import java.io.FileInputStream;
import java.io.IOException;
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

    public static String getApiKey() {
        load();
        return props.getProperty("gemini.api.key", "");
    }
}