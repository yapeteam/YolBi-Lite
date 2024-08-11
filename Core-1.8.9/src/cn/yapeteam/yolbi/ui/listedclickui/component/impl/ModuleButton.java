package cn.yapeteam.yolbi.ui.listedclickui.component.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.setting.Setting;
import cn.yapeteam.yolbi.ui.listedclickui.ImplScreen;
import cn.yapeteam.yolbi.ui.listedclickui.component.AbstractComponent;
import cn.yapeteam.yolbi.ui.listedclickui.component.Limitation;
import cn.yapeteam.yolbi.utils.animation.Animation;
import cn.yapeteam.yolbi.utils.animation.Easing;
import cn.yapeteam.yolbi.utils.render.GradientBlur;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.stream.Collectors;

/**
 * @author TIMER_err
 */
@Getter
public class ModuleButton extends AbstractComponent {
    private final Module module;
    @Setter
    private float realY;

    public ModuleButton(AbstractComponent parent, Module module) {
        super(parent);
        this.module = module;
    }

    private boolean extended = false;
    private float extend = 0;
    private final Animation extendAnimation = new Animation(Easing.EASE_OUT_EXPO, 400);

    public float getExtend() {
        return (float) extendAnimation.animate(extend);
    }

    @Override
    public void init() {
        getChildComponents().clear();
        float y = getY() + getHeight();
        KeyBindingButton keyBindingButton = new KeyBindingButton(this, module);
        keyBindingButton.setX(getX());
        keyBindingButton.setY(y);
        keyBindingButton.setWidth(getWidth());
        keyBindingButton.setHeight(ImplScreen.keyBindHeight);
        getChildComponents().add(keyBindingButton);
        y += ImplScreen.keyBindHeight + ImplScreen.valueSpacing;
        for (Setting value : module.getSettings()) {
            ValueButton valueButton = new ValueButton(this, value);
            valueButton.setX(getX());
            valueButton.setY(y);
            valueButton.setWidth(getWidth());
            valueButton.setHeight();
            getChildComponents().add(valueButton);
            y += valueButton.getHeight() + ImplScreen.valueSpacing;
        }
        super.init();
    }

    @Override
    public void update() {
        extend = 0;
        float y = getY() + getHeight();
        for (AbstractComponent component : getChildComponents()) {
            component.setX(getX());
            component.setY(y);
            if (!(component instanceof ValueButton && !((ValueButton) component).getValue().visibleCheck.get()))
                y += component.getHeight() + ImplScreen.valueSpacing;
            if (extended && !(component instanceof ValueButton && !((ValueButton) component).getValue().visibleCheck.get()))
                extend += component.getHeight() + ImplScreen.valueSpacing;
        }
        blur.update(getX(), getY(), getWidth(), getHeight());
        super.update();
    }

    private final GradientBlur blur = new GradientBlur(GradientBlur.Type.TB);
    private final Animation alphaAnimation = new Animation(Easing.EASE_OUT_QUAD, 200);

    @Override
    public void drawComponent(int mouseX, int mouseY, float partialTicks, Limitation limitation) {
        if (!(
                getX() + getWidth() < limitation.getX() ||
                        getX() > limitation.getX() + limitation.getWidth() ||
                        getY() + getHeight() < limitation.getY() ||
                        getY() > limitation.getY() + limitation.getHeight()
        )) {
            GlStateManager.color(1, 1, 1, 1);
            blur.render(getX(), getY(), getWidth(), getHeight(), partialTicks, 1);
            int color = ImplScreen.getComponentColor((int) (getY() * 10));
            boolean hovering = isHovering(mouseX, mouseY) && getParent().isHovering(mouseX, mouseY);
            RenderUtil.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), new Color(0, 0, 0, (float) alphaAnimation.animate(hovering ? 0.4 : 0.1)).getRGB());
            AbstractFontRenderer font = YolBi.instance.getFontManager().getPingFang14();
            font.drawString(module.getName(), getX() + 5, getY() + (getHeight() - font.getStringHeight(module.getName())) / 2f, module.isEnabled() ? color : -1);
            if (getChildComponents().size() > 1) {
                float x = getX() + getWidth() - 6;
                float top_bottom = 6.5f;
                float y = getY() + top_bottom;
                for (int i = 0; i < 3; i++) {
                    RenderUtil.circle(x, y, 0.09f, -1);
                    y += (getHeight() - top_bottom * 2) / 2f;
                }
            }
        }
        if (extended)
            getChildComponents().stream().filter(c -> !(c instanceof ValueButton && !((ValueButton) c).getValue().visibleCheck.get())).collect(Collectors.toList()).forEach(c -> c.drawComponent(mouseX, mouseY, partialTicks, limitation));
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int mouseButton) {
        if (isHovering(getParent().getX(), getParent().getY() + ImplScreen.panelTopHeight, getParent().getWidth(), getParent().getHeight() - ImplScreen.panelTopHeight, mouseX, mouseY))
            if (isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY)) {
                if (mouseButton == 0) module.setEnabled(!module.isEnabled());
                if (mouseButton == 1 && !getChildComponents().isEmpty())
                    extended = !extended;
            }
        if (extended)
            getChildComponents().stream().filter(c -> !(c instanceof ValueButton && !((ValueButton) c).getValue().visibleCheck.get())).collect(Collectors.toList()).forEach(c -> c.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int state) {
        if (extended)
            getChildComponents().stream().filter(c -> !(c instanceof ValueButton && !((ValueButton) c).getValue().visibleCheck.get())).collect(Collectors.toList()).forEach(c -> c.mouseReleased(mouseX, mouseY, state));
    }
}
