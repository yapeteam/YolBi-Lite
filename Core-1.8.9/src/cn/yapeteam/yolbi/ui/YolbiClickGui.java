package cn.yapeteam.yolbi.ui;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

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
    public final Color logoColor = new Color(255, 255, 255);
    public final Color fontColor = new Color(255, 255, 255);
    public final Color fontDarkColor = new Color(255, 255, 255, 220);
    public final Color fontDarkerColor = new Color(255, 255, 255, 40);
    public final Color opaqueAccentColor = new Color(68, 134, 240, 160);

    public float draggingOffsetX, draggingOffsetY;
    public boolean dragging;

    public ConcurrentLinkedQueue<Module> moduleList = new ConcurrentLinkedQueue<>();

    public Vector2f mouse;

    public static int round = 7;

    private SideBarComponent sidebar = new SideBarComponent();

    @Override
    public void initGui() {
        round = 12;

        Keyboard.enableRepeatEvents(true);

        if (this.position.x < 0 || this.position.y < 0 ||
                this.position.x + this.scale.x > IMinecraft.ScaledResolution.getScaledWidth() ||
                this.position.y + this.scale.y > IMinecraft.ScaledResolution.getScaledHeight()) {
            this.position.x = IMinecraft.ScaledResolution.getScaledWidth() / 2f - this.scale.x / 2;
            this.position.y = IMinecraft.ScaledResolution.getScaledHeight() / 2f - this.scale.y / 2;
        }

    }

    @Override
    public void onGuiClosed() {
        /* removes the blur */
        YolBi.instance.getModuleManager().getModule("ClickGui").disable();
        Keyboard.enableRepeatEvents(false);
        dragging = false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void render() {
        scale = new Vector2f(400, 300);

        if (mouse == null) {
            return;
        }

        //Information from gui draw screen to use in this event, we use this event instead of gui draw screen because it allows the clickgui to have an outro animation
        final int mouseX = (int) mouse.x;
        final int mouseY = (int) mouse.y;

        /* Handles dragging */
        if (dragging) {
            position.x = mouseX + draggingOffsetX;
            position.y = mouseY + draggingOffsetY;
        }

        /* Background */
        YolBi.instance.getRenderManager().roundedRectangle(position.x + sidebar.sidebarWidth, position.y, scale.x - sidebar.sidebarWidth, scale.y, round, backgroundColor);
        YolBi.instance.getRenderManager().rectangle(position.x + sidebar.sidebarWidth, position.y, round * 2, scale.y, backgroundColor);
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        /* Registers click if you click within the window */

        if (GuiUtil.mouseOver(position.x, position.y, scale.x, 15, mouseX, mouseY)) {
            draggingOffsetX = position.x - mouseX;
            draggingOffsetY = position.y - mouseY;
            dragging = true;
        }

        // Only register click if within the ClickGUI
        else if (GuiUtil.mouseOver(position.getX(), position.getY(), scale.getX(), scale.getY(), mouseX, mouseY)) {
            //mean clicking in the sidebar
        }
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        /* Registers the mouse being released */
        dragging = false;
    }

}
