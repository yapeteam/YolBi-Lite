package cn.yapeteam.yolbi.module.api.value.impl;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.api.value.Value;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.math.vector.Vector2d;
import cn.yapeteam.yolbi.utils.render.animation.Animation;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

import java.util.List;
import java.util.function.BooleanSupplier;

import static cn.yapeteam.yolbi.utils.render.animation.Easing.EASE_OUT_EXPO;
import static cn.yapeteam.yolbi.utils.render.animation.Easing.LINEAR;

/**
 * @author Alan
 * @since 10/19/2021
 */
@Setter
public class DragValue extends Value<Vector2d> implements Accessor {

    public Vector2d position = new Vector2d(100, 100), targetPosition = new Vector2d(100, 100), scale = new Vector2d(100, 100), lastScale = new Vector2d(-1, -1);
    public Animation animationPosition = new Animation(LINEAR, 600), smoothAnimation = new Animation(EASE_OUT_EXPO, 300);
    public ScaledResolution lastScaledResolution = new ScaledResolution(mc);
    public boolean render = true, structure;

    public DragValue(final String name, final Module parent, final Vector2d defaultValue) {
        super(name, parent, defaultValue);
    }

    public DragValue(final String name, final Module parent, final Vector2d defaultValue, final boolean render) {
        super(name, parent, defaultValue);
        this.render = render;
    }

    public DragValue(final String name, final Module parent, final Vector2d defaultValue, final boolean render, final boolean structure) {
        super(name, parent, defaultValue);
        this.render = render && !structure;
        this.structure = structure;
    }

    public DragValue(final String name, final Mode<?> parent, final Vector2d defaultValue) {
        super(name, parent, defaultValue);
    }

    public DragValue(final String name, final Module parent, final Vector2d defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    public DragValue(final String name, final Mode<?> parent, final Vector2d defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }

    public void setScale(Vector2d scale) {
        this.scale = scale;
        if (lastScale.x == -1 && lastScale.y == -1) {
            this.lastScale = this.scale;
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        if (this.position.x > scaledResolution.getScaledWidth() / 2f) {
            this.targetPosition.x += this.lastScale.x - this.scale.x;
            this.position = targetPosition;
        }

        if (this.position.y > scaledResolution.getScaledHeight() / 2f) {
            this.targetPosition.y += this.lastScale.y - this.scale.y;
            this.position = targetPosition;
        }

//        if (lastScaledResolution != null) {
//            this.position.x /= ((double) lastScaledResolution.getScaledWidth() / scaledResolution.getScaledWidth()) / scaledResolution.getScaleFactor();
//            this.position.y /= ((double) lastScaledResolution.getScaledHeight() / scaledResolution.getScaledHeight()) / scaledResolution.getScaleFactor();
//        }

        this.lastScale = scale;
        this.lastScaledResolution = scaledResolution;
    }
}