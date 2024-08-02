package cn.yapeteam.yolbi.managers;

import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.utils.StreamUtils;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Typeface;
import lombok.Getter;

import java.io.InputStream;

@Getter
public class SkijaFontManager {
    public static final SkijaFontManager instance = new SkijaFontManager();
    private final Font Nunito20 = makeFromStream(ResourceManager.resources.getStream("fonts/Nunito-Regular.ttf"), 20);
    private final Font Sans16 = makeFromStream(ResourceManager.resources.getStream("fonts/product_sans_regular.ttf"), 16);
    private final Font Sans32 = makeFromStream(ResourceManager.resources.getStream("fonts/product_sans_regular.ttf"), 32);
    private final Font JelloRegular18 = makeFromStream(ResourceManager.resources.getStream("fonts/JelloRegular.ttf"), 18);
    private final Font PingFang18 = makeFromStream(ResourceManager.resources.getStream("fonts/PingFang_Normal.ttf"), 18);

    public static Font makeFromStream(InputStream in, float size) {
        try {
            byte[] bytes = StreamUtils.readStream(in);
            System.out.println(bytes);
            Data data = Data.makeFromBytes(bytes);
            Typeface typeface = Typeface.makeFromData(data);
            return new Font(typeface, size);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
