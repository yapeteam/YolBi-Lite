package cn.yapeteam.yolbi.ui;

import cn.yapeteam.ymixin.annotations.Super;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.impl.visual.ClickUI;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class YolbiClickGui extends GuiScreen implements IMinecraft {
    public static Vector2f position = new Vector2f(-1, -1);
    public static Vector2f scale = new Vector2f(320 * 1.3f, 260 * 1.3f);

    /* Colors */
    public final Color backgroundColor = new Color(23, 26, 33);
    public static final Color sidebarColor = new Color(18, 20, 25);

    public float draggingOffsetX, draggingOffsetY;
    public boolean dragging;

    public ConcurrentLinkedQueue<Module> moduleList = new ConcurrentLinkedQueue<>();

    public Vector2f mouse;

    public static int round = 7;

    private SideBarComponent sidebar = new SideBarComponent();

    public static ModuleCategory currentCategory = ModuleCategory.COMBAT;

    @Super
    @Override
    public void initGui() {
        round = 12;

        ScaledResolution scaledResolution = new ScaledResolution(IMinecraft.mc);

        Keyboard.enableRepeatEvents(true);

        if (this.position.x < 0 || this.position.y < 0 ||
                this.position.x + this.scale.x > scaledResolution.getScaledWidth() ||
                this.position.y + this.scale.y > scaledResolution.getScaledHeight()) {
            this.position.x = scaledResolution.getScaledWidth() / 2f - this.scale.x / 2;
            this.position.y = scaledResolution.getScaledHeight() / 2f - this.scale.y / 2;
        }
    }

    @Super
    @Override
    public void onGuiClosed() {
        /* removes the blur */
        YolBi.instance.getModuleManager().getModule(ClickUI.class).toggle();
        Keyboard.enableRepeatEvents(false);
        dragging = false;
    }

    @Super
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Super
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scale = new Vector2f(400, 300);

        /* Handles dragging */
        if (dragging) {
            position.x = mouseX + draggingOffsetX;
            position.y = mouseY + draggingOffsetY;
        }

        /* Scissoring to ensure content stays within the GUI area */
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        YolBi.instance.getRenderManager().scissor(position.x, position.y, scale.x, scale.y);

        /* Background */
        YolBi.instance.getRenderManager().roundedRectangle(position.x + sidebar.sidebarWidth, position.y, scale.x - sidebar.sidebarWidth, scale.y, round, backgroundColor.getRGB());

        /* Draw Sidebar */
        YolBi.instance.getRenderManager().roundedRectangle(position.x, position.y, sidebar.sidebarWidth + round, scale.y, round, sidebarColor.getRGB());
        YolBi.instance.getRenderManager().rectangle(position.x + sidebar.sidebarWidth - round, position.y, round * 2, scale.y, backgroundColor);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        /* Sidebar */
        sidebar.renderSidebar(mouseX, mouseY);
    }

    @Super
    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        /* Registers click if you click within the window */
        if (GuiUtil.mouseOver(position.x, position.y, scale.x, 15, mouseX, mouseY)) {
            draggingOffsetX = position.x - mouseX;
            draggingOffsetY = position.y - mouseY;
            dragging = true;
        }
    }

    @Super
    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        /* Registers the mouse being released */
        dragging = false;
    }
}
