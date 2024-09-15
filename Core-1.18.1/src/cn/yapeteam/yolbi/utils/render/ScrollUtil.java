package cn.yapeteam.yolbi.utils.render;

import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.utils.StopWatch;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.math.MathUtils;
import cn.yapeteam.yolbi.utils.math.vector.Vector2d;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Mouse;

import java.awt.*;

@Getter
@Setter
public class ScrollUtil implements Accessor {

    public double target, scroll, max = 25;
    public StopWatch stopwatch = new StopWatch();
    public StopWatch stopWatch2 = new StopWatch();
    public boolean scrollingIsAllowed, active, animating;
    private boolean reverse;

    public void onRender() {
        onRender(true);
    }

    public void onRender(boolean update) {
        //Sets target scroll every tick, this way scrolling will only change if there's less than 1 frame per tick
        if (stopWatch2.finished(50)) {
            final float wheel = update ? Mouse.getDWheel() * (reverse ? -1 : 1) : 0;
            double stretch = 30;
            active = wheel != 0;
            target = Math.min(Math.max(target + wheel / 2, max - (wheel == 0 ? 0 : stretch)), (wheel == 0 ? 0 : stretch));

            stopWatch2.reset();
        }

        //Moving render scroll towards target
        for (int i = 0; i < stopwatch.getElapsedTime(); ++i) {
            scroll = MathUtils.lerp(scroll, target, 1E-2F);
        }

        animating = Math.abs(scroll - target) > 0.5;

        //resetting stopwatch
        stopwatch.reset();
    }

    public void renderScrollBar(Vector2d position, double maxHeight) {
        double percentage = (reverse ? (getMax() - getScroll()) : getScroll()) / getMax();
        double scrollBarHeight = maxHeight - ((getMax() / (getMax() - maxHeight)) * maxHeight);

        scrollingIsAllowed = scrollBarHeight < maxHeight;
        if (!scrollingIsAllowed) return;

        double scrollX = position.x;
        double scrollY = position.y + maxHeight * percentage - scrollBarHeight * percentage;
        Color color = ColorUtil.withAlpha(Color.WHITE, 60);

        RenderManager.roundedRectangle(scrollX, scrollY, 1, scrollBarHeight, 0.5f,
                color);
    }

    public void reset() {
        this.scroll = 0;
        this.target = 0;
    }
}
