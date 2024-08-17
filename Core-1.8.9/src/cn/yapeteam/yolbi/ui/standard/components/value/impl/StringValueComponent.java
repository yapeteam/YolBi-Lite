package cn.yapeteam.yolbi.ui.standard.components.value.impl;


import cn.yapeteam.yolbi.font.Fonts;
import cn.yapeteam.yolbi.font.Weight;
import cn.yapeteam.yolbi.ui.standard.components.value.ValueComponent;
import cn.yapeteam.yolbi.ui.standard.screen.Colors;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.render.textbox.TextAlign;
import cn.yapeteam.yolbi.utils.render.textbox.TextBox;
import cn.yapeteam.yolbi.utils.vector.Vector2d;
import cn.yapeteam.yolbi.value.Value;
import cn.yapeteam.yolbi.value.impl.StringValue;

import java.awt.*;

public class StringValueComponent extends ValueComponent {

    public final TextBox textBox = new TextBox(new Vector2d(200, 200), Fonts.MAIN.get(16, Weight.REGULAR), Color.WHITE, TextAlign.LEFT, "", 20);

    public StringValueComponent(final Value<?> value) {
        super(value);

        final StringValue stringValue = (StringValue) value;
        textBox.setText(stringValue.getValue());
        textBox.setCursor(stringValue.getValue().length());
    }

    @Override
    public void draw(Vector2d position, int mouseX, int mouseY, float partialTicks) {
        this.position = position;
        final StringValue stringValue = (StringValue) this.value;

        this.height = 28;

        // Draws name
        Fonts.MAIN.get(16, Weight.REGULAR).draw(this.value.getName(), this.position.x, this.position.y, Colors.SECONDARY_TEXT.getRGBWithAlpha(opacity));

        // Draws value
        this.textBox.setColor(ColorUtil.withAlpha(this.textBox.getColor(), opacity));
        this.position = new Vector2d(this.position.x, this.position.y + 14);
        this.textBox.setPosition(this.position);
        this.textBox.setWidth(242.5f - 12);
        this.textBox.draw();
        stringValue.setValue(textBox.getText());
    }

    @Override
    public boolean click(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.position == null) {
            return false;
        }

        textBox.click(mouseX, mouseY, mouseButton);
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
        if (this.position == null) {
            return;
        }

        textBox.key(typedChar, keyCode);
    }
}
