package cn.yapeteam.yolbi.utils.profiling.localization;


import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Hazsi
 * @since 10/31/2022
 */
public class Localization {

    private static boolean populated = false;

    /**
     * Gets a translated string using the Client's current locale
     *
     * @param key The key of the string to translate
     * @return The translated string of the key, using the Client's current locale
     */
    public static String get(String key) {
        return get(key, YolBi.instance.getLocale());
    }

    /**
     * Gets a translated string using a specified locale
     *
     * @param key    The key of the string to translate
     * @param locale The locale to use when translating the given key
     * @return The translated string of the key, using the specified locale. If the key is not found in the specified
     * locale's strings, en_US is used a fallback. If the string is missing in en_US, the key itself is used as a
     * final fallback.
     */
    public static String get(String key, Locale locale) {
        if (!populated) populate();
        String translated = locale.getStrings().get(key);
        if (translated == null) translated = Locale.EN_US.getStrings().get(key);
        return translated == null ? key : translated;
    }

    /**
     * Called on client startup. Populates the maps for each locale by reading and parsing their properties files
     * located in rise/text
     */
    public static void populate() {
        for (Locale locale : Locale.values()) {
            String resourcePath = "text/" + locale.getFile() + ".properties";
            Logger.info("Loading localization file: " + resourcePath);
            try (InputStream stream = ResourceManager.resources.getStream(resourcePath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        locale.getStrings().put(parts[0], parts[1]);
                    }
                }
            } catch (Exception exception) {
                System.out.println("Localization exception");
                exception.printStackTrace();
            }
        }
        populated = true;
    }
}