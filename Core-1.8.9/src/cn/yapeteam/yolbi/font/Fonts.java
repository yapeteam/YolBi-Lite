package cn.yapeteam.yolbi.font;

import cn.yapeteam.yolbi.utils.font.Font;
import cn.yapeteam.yolbi.utils.font.impl.rise.FontRenderer;
import cn.yapeteam.yolbi.utils.font.impl.rise.FontUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

@Getter
public enum Fonts {
    MAIN("SF-Pro-Rounded-%s", "otf"),
    MINECRAFT("Minecraft", () -> new MinecraftFontWrapper(Minecraft.getMinecraft().fontRendererObj)),
    ICONS_1("Icon-1", "ttf"),
    ICONS_2("Icon-3", "ttf"),
    CUSTOM("", "ttf");

    Fonts(String name, String extension) {
        this.name = name;
        this.extention = extension;
    }

    Fonts(String name, Supplier<Font> get) {
        this.name = name;
        this.extention = "";
        this.font = get.get();
        this.get = get;
    }

    private Supplier<Font> get;
    private Font font;

    private String name;
    final String extention;
    private final HashMap<Integer, FontRenderer> sizes = new HashMap<>();

    private static final boolean ORACAL = System.getProperty("java.vm.vendor").equals("Oracle Corporation");

    @SneakyThrows
    public Font get(int size) {
        return get(size, Weight.NONE);
    }

    @SneakyThrows
    public Font get() {
        if (get == null) throw new Exception("Please specify a size, this doesn't have a predefined font");
        return get(0, Weight.NONE);
    }

    @SneakyThrows
    public Font get(int size, Weight weight) {
        if (get != null) {
            if (font == null) font = get.get();
            return font;
        }

        // Oracle bug fix
        if (ORACAL) if (extention.equals("otf")) size /= 2;
        int key = Integer.parseInt(size + "" + weight.getNum());

        if (!sizes.containsKey(key)) {
            java.awt.Font font = null;
            String location = "unknown";

            if (name.contains(":")) {
                location = name;
                font = FontUtil.getDiskResource(location, size);
            } else {
                for (String alias : weight.getAliases()) {
                    location = "fonts/" + String.format(name, alias) + "." + extention;
                    font = FontUtil.getResource(location, size);

                    if (font != null) break;
                }
            }

            if (font != null) {
                sizes.put(key, new FontRenderer(font, true, true, false));
            } else {
                throw new Exception("Unknown Font " + location);
            }
        }

        return sizes.get(key);
    }

    public static ArrayList<String> getFontPaths() {
        ArrayList<String> fontPaths = new ArrayList<>();

        // macOS font paths
        addFontPaths(fontPaths, "/System/Library/Fonts");
        addFontPaths(fontPaths, "/Library/Fonts");
        addFontPaths(fontPaths, System.getProperty("user.home") + "/Library/Fonts");

        // Windows font paths
        addFontPaths(fontPaths, "C:\\Windows\\Fonts");
        addFontPaths(fontPaths, System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Windows\\Fonts");

        // Linux font paths
        addFontPaths(fontPaths, "/usr/share/fonts");
        addFontPaths(fontPaths, "/usr/local/share/fonts");
        addFontPaths(fontPaths, System.getProperty("user.home") + "/.fonts");
        addFontPaths(fontPaths, System.getProperty("user.home") + "/.local/share/fonts");

        return fontPaths;
    }

    private static void addFontPaths(ArrayList<String> fontPaths, String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".ttf")) {
                        fontPaths.add(file.getAbsolutePath());
                    } else if (file.isDirectory()) {
                        addFontPaths(fontPaths, file.getAbsolutePath());
                    }
                }
            }
        }
    }
}
