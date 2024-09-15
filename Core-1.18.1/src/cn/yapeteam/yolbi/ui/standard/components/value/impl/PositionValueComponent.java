package cn.yapeteam.yolbi.ui.standard.components.value.impl;


import cn.yapeteam.yolbi.module.api.value.Value;
import cn.yapeteam.yolbi.ui.standard.components.value.ValueComponent;
import cn.yapeteam.yolbi.utils.math.vector.Vector2d;
import net.minecraft.util.ResourceLocation;

public class PositionValueComponent extends ValueComponent {

    private final ResourceLocation image = new ResourceLocation("rise/icons/click.png");

    public PositionValueComponent(final Value<?> value) {
        super(value);
    }

    @Override
    public void draw(final Vector2d position, final int mouseX, final int mouseY, final float partialTicks) {
        this.height = 0;
    }

    @Override
    public boolean click(final int mouseX, final int mouseY, final int mouseButton) {
        return false;
    }

    @Override
    public void released() {
    }

    @Override
    public void bloom() {
    }

    @Override
    public void key(final char typedChar, final int keyCode) {

    }
}
