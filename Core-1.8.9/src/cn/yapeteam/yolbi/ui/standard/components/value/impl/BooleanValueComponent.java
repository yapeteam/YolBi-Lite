package cn.yapeteam.yolbi.ui.standard.components.value.impl;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.Fonts;
import cn.yapeteam.yolbi.font.Weight;
import cn.yapeteam.yolbi.ui.standard.components.value.ValueComponent;
import cn.yapeteam.yolbi.ui.standard.screen.Colors;
import cn.yapeteam.yolbi.utils.StopWatch;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.vector.Vector2d;
import cn.yapeteam.yolbi.value.Value;
import cn.yapeteam.yolbi.value.impl.BooleanValue;

public class BooleanValueComponent extends ValueComponent {

    private final StopWatch stopwatch = new StopWatch();
    private double scale;

    public BooleanValueComponent(final Value<?> value) {
        super(value);
    }

    @Override
    public void draw(final Vector2d position, final int mouseX, final int mouseY, final float partialTicks) {
        this.position = position;
        final BooleanValue booleanValue = (BooleanValue) value;

        // Draws name
        Fonts.MAIN.get(16, Weight.REGULAR).draw(this.value.getName(), this.position.x, this.position.y, Colors.SECONDARY_TEXT.getRGBWithAlpha(opacity));
        final double positionX = this.position.x + Fonts.MAIN.get(16, Weight.REGULAR).width(this.value.getName()) + 3;

        if (booleanValue.getValue()) {
            scale = Math.min(5, scale + stopwatch.getElapsedTime() / 20f);
        } else {
            scale = Math.max(0, scale - stopwatch.getElapsedTime() / 20f);
        }

        YolBi.instance.getRenderManager().roundedRectangle(positionX - 5f / 2f + 5, this.position.y - 5f / 2f + 2.5, 5, 5, 2.5F, Colors.BACKGROUND.getWithAlpha(opacity));

        if (scale != 0) {
            YolBi.instance.getRenderManager().roundedRectangle(positionX - scale / 2 + 4, this.position.y - scale / 2 + 2.5, scale, scale, scale / 2.0F, ColorUtil.withAlpha(this.getTheme().getFirstColor(), opacity));
        }

        stopwatch.reset();
    }

    @Override
    public boolean click(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.position == null) {
            return false;
        }

        final BooleanValue booleanValue = (BooleanValue) value;

        if (GuiUtil.mouseOver(position.x, this.position.y - 3.5f, getClickGUI().width - 70, this.height, mouseX, mouseY)) {
            booleanValue.setValue(!booleanValue.getValue());
            return true;
        }

        return false;
    }

    @Override
    public void released() {

    }

    @Override
    public void bloom() {
        if (this.position == null) {
            return;
        }

        final double positionX = this.position.x + Fonts.MAIN.get(16, Weight.REGULAR).width(this.value.getName()) + 2;
        YolBi.instance.getRenderManager().roundedRectangle(positionX - scale / 2 + 4, this.position.y - scale / 2 + 2.5, scale, scale, scale / 2.0F, ColorUtil.withAlpha(this.getTheme().getFirstColor(), opacity));
    }

    @Override
    public void key(final char typedChar, final int keyCode) {

    }
}
