package cn.yapeteam.yolbi.ui.standard.screen.impl;

import com.alan.clients.Client;
import com.alan.clients.ui.click.standard.RiseClickGUI;
import com.alan.clients.ui.click.standard.components.value.ValueComponent;
import com.alan.clients.ui.click.standard.screen.Colors;
import com.alan.clients.ui.click.standard.screen.Screen;
import com.alan.clients.ui.click.standard.screen.impl.aiscreen.Graph;
import com.alan.clients.util.Accessor;
import com.alan.clients.util.gui.textbox.TextBox;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Getter
@Setter
public final class AIScreen implements Screen, Accessor {
    private RiseClickGUI clickGUI;
    private Graph real = new Graph(new ArrayList<>(), new Vector2f(0, 0), getTheme().getFirstColor());
    private Graph ai = new Graph(new ArrayList<>(), new Vector2f(0, 0), getTheme().getFirstColor());
    private float padding = 10;
//    private AI module;
    public ArrayList<ValueComponent> valueList = new ArrayList<>();

    @Override
    public void onInit() {
//        module = getModule(AI.class);

        clickGUI = Client.INSTANCE.getClickGUI();

        real.getPoints().clear();

        int distance = 15;
        int count = (int) Math.floor((clickGUI.scale.x - padding * 4) / distance);

        for (int x = 0; x <= count; x++) {
            real.getPoints().add(new float[]{x * distance, (float) (Math.random() * 30f) + 50});
        }

        ai.getPoints().clear();
        for (int x = 0; x <= count; x++) {
            ai.getPoints().add(new float[]{x * distance, (float)
                    (real.getPoints().get(x)[1] + (Math.random() - 0.5) * 30)});
        }

        valueList.clear();
//        module.getAllValues().forEach(value -> {
//            ValueComponent component = value.createUIComponent();
//            if (component != null) valueList.add(component);
//        });
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        Vector2f position = new Vector2f(clickGUI.getPosition()).add(padding, 0);

        RenderUtil.roundedRectangle(position.getX(), position.getY() + padding,
                clickGUI.scale.x - padding * 2, 100, clickGUI.getRound(),
                Colors.SECONDARY.get());

        real.setColor(getTheme().getFirstColor());
        real.setPosition(new Vector2f(position.add(padding, 0)));
        real.onRender();

        ai.setColor(getTheme().getSecondColor());
        ai.setPosition(new Vector2f(position.add(padding, 0)));
        ai.onRender();

        position = position.add(0, 100);
        position = position.add(0, padding * 2);

        RenderUtil.roundedRectangle(position.getX(), position.getY(),
                clickGUI.scale.x - padding * 2, clickGUI.getScale().getY() - (position.getY() - clickGUI.getPosition().getY()) - padding, clickGUI.getRound(),
                Colors.SECONDARY.get());

        position = position.add(0, padding);

        for (final ValueComponent valueComponent : this.getValueList()) {
            if (valueComponent.getValue() != null && valueComponent.getValue().getHideIf() != null && valueComponent.getValue().getHideIf().getAsBoolean()) {
                continue;
            }

            valueComponent.setOpacity(200);

            valueComponent.draw(new Vector2d(position.x + 1 + padding +
                    (valueComponent.getValue().getHideIf() == null ? 0 : 10),
                    position.y), mouseX, mouseY, partialTicks);

            position = position.add(0, (float) valueComponent.getHeight());
        }
    }

    @Override
    public void onKey(char typedChar, int keyCode) {
        for (final ValueComponent valueComponent : this.getValueList()) {
            valueComponent.key(typedChar, keyCode);
        }
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {
        for (final ValueComponent valueComponent : this.getValueList()) {
            if (valueComponent.getValue() != null && valueComponent.getValue().getHideIf() != null &&
                    valueComponent.getValue().getHideIf().getAsBoolean()) {
                continue;
            }

            if (valueComponent.click(mouseX, mouseY, mouseButton)) {
                break;
            }
        }
    }

    @Override
    public void onMouseRelease() {
        for (final ValueComponent valueComponent : this.getValueList()) {
            valueComponent.released();
        }
    }

    @Override
    public void onBloom() {
        Vector2f position = new Vector2f(clickGUI.getPosition());

        real.onBloom();
        ai.onBloom();
    }

    @Override
    public boolean hideSideBar() {
        return true;
    }

    @Override
    public boolean automaticSearchSwitching() {
        return valueList.stream().noneMatch(valueComponent -> {
            for (Field field : valueComponent.getClass().getDeclaredFields()) {
                if (field.getType().equals(TextBox.class)) {
                    try {
                        return ((TextBox) field.get(valueComponent)).isSelected();
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }

            return false;
        });
    }
}