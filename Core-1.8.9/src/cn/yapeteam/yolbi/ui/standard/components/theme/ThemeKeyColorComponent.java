package cn.yapeteam.yolbi.ui.standard.components.theme;


import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.ui.theme.Themes;
import cn.yapeteam.yolbi.utils.render.animation.Animation;
import cn.yapeteam.yolbi.utils.render.animation.Easing;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.math.vector.Vector3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

import static cn.yapeteam.yolbi.utils.render.layer.Layers.BLOOM;

@Getter
@RequiredArgsConstructor
public class ThemeKeyColorComponent implements Accessor {
    private final Themes.KeyColors color;

    private Vector3d lastDraw = new Vector3d(0, 0, 0);
    private final Animation dimAnimation = new Animation(Easing.EASE_OUT_QUINT, 500);
    private final Animation bloomAnimation = new Animation(Easing.EASE_OUT_QUINT, 500);

    public void draw(double x, double y, double width, boolean selected) {
        RenderManager.roundedRectangle(x, y, width, 17, 5, new Color(18, 21, 30));
        RenderManager.roundedRectangle(x + 0.5, y + 0.5, width - 1, 16, 4, color.getColor());

        RenderManager.roundedRectangle(x, y, width, 17, 5, new Color(25, 25, 25,
                (int) ((1 - dimAnimation.getValue()) * 128)));

        getLayer(BLOOM).add(() -> {
            RenderManager.roundedRectangle(x, y, width, 17, 5, new Color(18, 21, 30,
                    (int) (bloomAnimation.getValue() * 255)));
            RenderManager.roundedRectangle(x + 0.5, y + 0.5, width - 1, 16, 4,
                    ColorUtil.withAlpha(color.getColor(), (int) (bloomAnimation.getValue() * 255)));
        });

        this.lastDraw = new Vector3d(x, y, width);
    }
}
