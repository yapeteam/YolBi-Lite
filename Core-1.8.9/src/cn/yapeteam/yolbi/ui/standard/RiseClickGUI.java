package cn.yapeteam.yolbi.ui.standard;


import cn.yapeteam.ymixin.annotations.Super;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.layer.Layer;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.impl.render.ClickGUI;
import cn.yapeteam.yolbi.ui.standard.components.ModuleComponent;
import cn.yapeteam.yolbi.ui.standard.components.category.SidebarCategory;
import cn.yapeteam.yolbi.ui.standard.components.value.ValueComponent;
import cn.yapeteam.yolbi.ui.standard.components.value.impl.BoundsNumberValueComponent;
import cn.yapeteam.yolbi.ui.standard.components.value.impl.NumberValueComponent;
import cn.yapeteam.yolbi.ui.standard.components.value.impl.StringValueComponent;
import cn.yapeteam.yolbi.ui.standard.screen.Colors;
import cn.yapeteam.yolbi.ui.standard.screen.Screen;
import cn.yapeteam.yolbi.ui.standard.screen.impl.SearchScreen;
import cn.yapeteam.yolbi.ui.standard.screen.impl.ThemeScreen;
import cn.yapeteam.yolbi.utils.StopWatch;
import cn.yapeteam.yolbi.utils.animation.Animation;
import cn.yapeteam.yolbi.utils.animation.Easing;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.interfaces.IMinecraft;
import cn.yapeteam.yolbi.utils.interfaces.ThreadAccess;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.render.shader.base.ShaderRenderType;
import cn.yapeteam.yolbi.utils.render.shader.impl.AlphaShader;
import cn.yapeteam.yolbi.utils.vector.Vector2d;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.Collator;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cn.yapeteam.yolbi.layer.Layers.BLOOM;

@Getter
public class RiseClickGUI extends GuiScreen implements Accessor, IMinecraft, ThreadAccess {

    public Vector2f position = new Vector2f(-1, -1);
    public Vector2f scale = new Vector2f(320 * 1.3f, 260 * 1.3f);

    /* Sidebar */
    public SidebarCategory sidebar = new SidebarCategory();

    /* Selected Screen */
    public Screen selectedScreen = Category.SEARCH.getClickGUIScreen();
    public Screen renderedScreen = selectedScreen;
    public Screen lastScreen = selectedScreen;

    public float draggingOffsetX, draggingOffsetY;
    public boolean dragging;
    public StopWatch timeInCategory = new StopWatch();
    public StopWatch stopwatch = new StopWatch();

    public ConcurrentLinkedQueue<ModuleComponent> moduleList = new ConcurrentLinkedQueue<>();

    public Vector2f mouse;
    public double animationTime, opacity, animationVelocity;

    public int round = 7;

    Vector2d translate;
    public ValueComponent overlayPresent;
    public Vector2f moduleDefaultScale = new Vector2f(283, 38);
    public Animation scaleAnimation = new Animation(Easing.EASE_IN_EXPO, 300);
    public Animation opacityAnimation = new Animation(Easing.EASE_IN_EXPO, 300);

    public void rebuildModuleCache() {
        moduleList.clear();

        java.util.List<Module> sortedModules = YolBi.instance.getModuleManager().getAll();
        sortedModules.sort((o1, o2) -> Collator.getInstance().compare(o1.getName(), o2.getName()));
        sortedModules.forEach(module -> moduleList.add(new ModuleComponent(module)));
    }

    @Super
    @Override
    public void initGui() {
        if (moduleList == null || moduleList.isEmpty()) {
            rebuildModuleCache();
        }

        //ThreadAccess.threadPool.execute(() -> {
        round = 12;
        scaleAnimation.reset();
        scaleAnimation.setValue(0);

        ScaledResolution scaledResolution = new ScaledResolution(IMinecraft.mc);

        lastScreen = selectedScreen;
        timeInCategory.reset();
        timeInCategory.setMillis(System.currentTimeMillis() - 150);

        Keyboard.enableRepeatEvents(true);
        stopwatch.reset();
        selectedScreen.onInit();

        if (this.position.x < 0 || this.position.y < 0 ||
                this.position.x + this.scale.x > scaledResolution.getScaledWidth() ||
                this.position.y + this.scale.y > scaledResolution.getScaledHeight()) {
            this.position.x = scaledResolution.getScaledWidth() / 2f - this.scale.x / 2;
            this.position.y = scaledResolution.getScaledHeight() / 2f - this.scale.y / 2;
        }

        moduleList.forEach(moduleComponent -> {
            moduleComponent.getValueList().forEach(valueComponent -> {
                if (valueComponent instanceof NumberValueComponent) {
                    ((NumberValueComponent) valueComponent).updateSliders();
                } else if (valueComponent instanceof BoundsNumberValueComponent) {
                    ((BoundsNumberValueComponent) valueComponent).updateSliders();
                }
            });
        });
        //});
    }

    @Super
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        dragging = false;
    }

    Layer alpha = new Layer(new AlphaShader());

    /**
     * If you remove this, expect to see me at your house within 24 hours. I found Alan's house
     * from a sunset picture. Don't think you're safe if you think it's a good idea to remove
     * this again.
     */
    @Super
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void render() {
        scale = new Vector2f(400, 300);

        if (animationTime > 0.99) {
            renderGUI();
        } else {
            ((AlphaShader) alpha.getShader()).setAlpha((float) opacity);
            alpha.add(this::renderGUI);
            alpha.run(ShaderRenderType.OVERLAY);
            alpha.clear();
        }
    }

    @Listener
    public void onRender2D(EventRender2D event) {
        if (animationTime <= 0.99) renderGUI();
    }

    public void renderGUI() {
        if (mouse == null) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        //Information from gui draw screen to use in this event, we use this event instead of gui draw screen because it allows the clickgui to have an outro animation
        final int mouseX = (int) mouse.x;
        final int mouseY = (int) mouse.y;
        final float partialTicks = Objects.requireNonNull(ReflectionManager.Minecraft$getTimer(mc)).renderPartialTicks;

        /* Handles dragging */
        if (dragging) {

            // I'm a horrible programmer and can't think of a better way to fix this bug
            if (this.selectedScreen instanceof ThemeScreen) {
                ((ThemeScreen) selectedScreen).resetAnimations();
            }

            position.x = mouseX + draggingOffsetX;
            position.y = mouseY + draggingOffsetY;
        }

        opacityAnimation.setEasing(mc.currentScreen == YolBi.instance.getClickGUI() ? Easing.EASE_OUT_EXPO : Easing.LINEAR);
        opacityAnimation.setDuration(mc.currentScreen == YolBi.instance.getClickGUI() ? 300 : 100);
        opacityAnimation.run(mc.currentScreen == YolBi.instance.getClickGUI() ? 1 : 0);
        opacity = opacityAnimation.getValue();

        scaleAnimation.setEasing(mc.currentScreen == YolBi.instance.getClickGUI() ? Easing.EASE_OUT_EXPO : Easing.LINEAR);
        scaleAnimation.run(mc.currentScreen == YolBi.instance.getClickGUI() ? 1 : 0);
        animationTime = scaleAnimation.getValue();

        if (mc.currentScreen == YolBi.instance.getClickGUI() && animationTime == 0) animationTime = 0.01;

        // Makes it not render the ClickGUI if it's animation is 0
        if (animationTime == 0) {
            YolBi.instance.getModuleManager().get(ClickGUI.class).setEnabled(false);
            return;
        }

        // Opening and closing animation gl
        translate = new Vector2d((position.x + scale.x / 2f) * (1 - animationTime), (position.y + scale.y / 2f) * (1 - animationTime));

        Runnable updatePositionAndScale = () -> {
            GlStateManager.pushMatrix();

            if (animationTime != 1) {
                GlStateManager.translate(translate.x, translate.y, 0);
                GlStateManager.scale(animationTime, animationTime, 0);
            }
        };

        updatePositionAndScale.run();
        getLayer(BLOOM, 2).add(updatePositionAndScale);

        /* Drop Shadow */
        if (animationTime > 0.993) {
            RenderManager.dropShadow(18, position.x, position.y, scale.x, scale.y, 30, round * 1.3);
        }

        /* Background */
        RenderManager.roundedRectangle(position.x, position.y, scale.x, scale.y, round, Colors.BACKGROUND.get());

        /* Stop objects from going outside the ClickGUI */
        Runnable startScissor = () -> {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int padding = 1;
            RenderManager.scissor(position.x * animationTime + translate.x + padding, position.y * animationTime + translate.y + padding, scale.x * animationTime - padding * 2, scale.y * animationTime - padding * 2);
        };

        startScissor.run();
        getLayer(BLOOM, 2).add(startScissor);

        Runnable translateAnimation = () -> {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, 0);
        };

        getLayer(BLOOM, 2).add(translateAnimation);

        int length = 200;

        /* Renders screen depending on selected category */
        (renderedScreen = timeInCategory.finished(length) ? selectedScreen : lastScreen)
                .onRender(mouseX, mouseY, partialTicks);

        final int opacity2 = 255 - (int) Math.max(0, Math.min(255, timeInCategory.getElapsedTime() < length ? 255 - (timeInCategory.getElapsedTime() * (255f / length)) : ((timeInCategory.getElapsedTime() - length) * (255f / length))));

        if (timeInCategory.getElapsedTime() <= length * 2) {
            RenderManager.roundedRectangle(position.x, position.y, scale.x, scale.y, round, Colors.BACKGROUND.getWithAlpha(opacity2));
        }

        sidebar.preRenderClickGUI();

        // for (int i = 0; i <= 8; i++) {
        //     double radius = i * 50;
        //     RenderManager.circle(position.x + sidebar.sidebarWidth - radius / 2, position.y + scale.y / 2 - radius / 2,
        //             radius, ColorUtil.withAlpha(getTheme().getFirstColor(), 1));
        // }

        /* Sidebar */
        sidebar.renderSidebar(mouseX, mouseY);

        Runnable endScissor = () -> {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager.popMatrix();
        };

        endScissor.run();
        getLayer(BLOOM, 2).add(endScissor);

        Runnable pop = GL11::glPopMatrix;
        getLayer(BLOOM, 2).add(pop);

        stopwatch.reset();
    }

    @Super
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.mouse = new Vector2f(mouseX, mouseY);
    }

    public void bloom() {
        translate = new Vector2d((position.x + scale.x / 2f) * (1 - animationTime), (position.y + scale.y / 2f) * (1 - animationTime));

        GlStateManager.pushMatrix();

        if (animationTime != 1) {
            GlStateManager.translate(translate.x, translate.y, 0);
            GlStateManager.scale(animationTime, animationTime, 0);
        }

        /* Stop objects from going outside the ClickGUI */
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderManager.scissor(position.x * animationTime + translate.x, position.y * animationTime + translate.y, scale.x * animationTime, (scale.y - 4) * animationTime);

        renderedScreen.onBloom();
        sidebar.bloom();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();
    }

    @Super
    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        /* Registers click if you click within the window */

        if (GuiUtil.mouseOver(position.x, position.y, scale.x, 15, mouseX, mouseY) && overlayPresent == null) {
            draggingOffsetX = position.x - mouseX;
            draggingOffsetY = position.y - mouseY;
            dragging = true;
        }

        // Only register click if within the ClickGUI
        else if (GuiUtil.mouseOver(position.getX(), position.getY(), scale.getX(), scale.getY(), mouseX, mouseY)) {
            if (overlayPresent == null) sidebar.clickSidebar(mouseX, mouseY, mouseButton);
            selectedScreen.onClick(mouseX, mouseY, mouseButton);
        }

        overlayPresent = null;
    }

    @Super
    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        /* Registers the mouse being released */
        dragging = false;

        selectedScreen.onMouseRelease();
    }

    @Super
    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        if ("abcdefghijklmnopqrstuvwxyz1234567890 ".contains(String.valueOf(typedChar).toLowerCase()) && selectedScreen.automaticSearchSwitching() && !getClickGUI().activeTextBox()) {
            this.switchScreen(Category.SEARCH);
        }

        super.keyTyped(typedChar, keyCode);
        selectedScreen.onKey(typedChar, keyCode);
    }

    public void switchScreen(final Category category) {
        if (!category.getClickGUIScreen().equals(this.selectedScreen)) {
            lastScreen = this.getClickGUI().selectedScreen;
            selectedScreen = category.getClickGUIScreen();

            this.timeInCategory.reset();
            selectedScreen.onInit();

            final SearchScreen search = ((SearchScreen) Category.SEARCH.getClickGUIScreen());
            search.relevantModules = search.getRelevantModules(search.searchBar.getText());
        }
    }

    public void switchScreen(final Screen screen) {
        if (!this.selectedScreen.getClass().getSimpleName().equals(screen.getClass().getSimpleName())) {
            lastScreen = this.getClickGUI().selectedScreen;
            selectedScreen = screen;

            this.timeInCategory.reset();
            selectedScreen.onInit();

            final SearchScreen search = ((SearchScreen) Category.SEARCH.getClickGUIScreen());
            search.relevantModules = search.getRelevantModules(search.searchBar.getText());
        }
    }

    public boolean activeTextBox() {
        for (final ModuleComponent moduleComponent : moduleList) {
            for (final ValueComponent value : moduleComponent.getValueList()) {
                if (value instanceof StringValueComponent && value.position != null && ((StringValueComponent) value).textBox.selected && !((StringValueComponent) value).textBox.drawn.finished(50)) {
                    return true;
                } else if (value instanceof NumberValueComponent && ((NumberValueComponent) value).valueDisplay.isSelected() && !((NumberValueComponent) value).valueDisplay.drawn.finished(50)) {
                    return true;
                } else if (value instanceof BoundsNumberValueComponent && ((BoundsNumberValueComponent) value).valueDisplay.isSelected() && !((BoundsNumberValueComponent) value).valueDisplay.drawn.finished(50)) {
                    return true;
                }
            }
        }

        return false;
    }
}
