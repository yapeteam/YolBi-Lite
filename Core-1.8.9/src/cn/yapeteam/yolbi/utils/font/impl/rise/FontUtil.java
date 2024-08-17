package cn.yapeteam.yolbi.utils.font.impl.rise;


import cn.yapeteam.loader.ResourceManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FontUtil {

    /**
     * Method which gets a font by a resource name
     *
     * @param resource resource name
     * @param size     font size
     * @return font by resource
     */
    public static Font getResource(final String resource, final int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(ResourceManager.resources.getStream(resource))).deriveFont((float) size);
        } catch (final FontFormatException | IOException ignored) {
            return null;
        }
    }

    /**
     * Method which gets a font by a resource name
     *
     * @param resource resource name
     * @param size     font size
     * @return font by resource
     */
    public static Font getDiskResource(final String resource, final int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(resource)).deriveFont((float) size);
        } catch (final FontFormatException | IOException ignored) {
            return null;
        }
    }
}
