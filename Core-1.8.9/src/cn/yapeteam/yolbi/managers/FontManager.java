package cn.yapeteam.yolbi.managers;

import cn.yapeteam.loader.utils.StreamUtils;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Typeface;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.InputStream;

@Getter
public class FontManager {
    public static final FontManager instance = new FontManager();
    private final Font Nunito20 = makeFromStream(FontManager.class.getResourceAsStream("/fonts/Nunito-Regular.ttf"), 20);
    private final Font Sans16 = makeFromStream(FontManager.class.getResourceAsStream("/fonts/product_sans_regular.ttf"), 16);
    private final Font Sans32 = makeFromStream(FontManager.class.getResourceAsStream("/fonts/product_sans_regular.ttf"), 32);

    @SneakyThrows
    public static Font makeFromStream(InputStream in, float size) {
        return new Font(Typeface.makeFromData(Data.makeFromBytes(StreamUtils.readStream(in))), size);
    }
}
