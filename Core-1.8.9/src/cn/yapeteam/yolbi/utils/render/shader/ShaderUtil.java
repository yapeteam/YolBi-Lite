package cn.yapeteam.yolbi.utils.render.shader;

import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.utils.StreamUtils;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.*;

public class ShaderUtil implements Accessor {
    public static int createShader(final String fragmentResource, final String vertexResource) {
        System.out.println("[DEBUG] Creating shader with fragment: " + fragmentResource + " and vertex: " + vertexResource);
        final String fragmentSource = getShaderResource(fragmentResource);
        final String vertexSource = getShaderResource(vertexResource);

        if (fragmentResource == null || vertexResource == null) {
            Logger.error("An error occurred whilst creating shader");
            Logger.error("Fragment: {}", fragmentSource == null);
            Logger.error("Vertex: {}", vertexSource == null);
            return -1;
        }

        Logger.info("Compiling Shader: {}", fragmentResource);

        final int fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        final int vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

        GL20.glShaderSource(fragmentId, fragmentSource);
        GL20.glShaderSource(vertexId, vertexSource);
        GL20.glCompileShader(fragmentId);
        GL20.glCompileShader(vertexId);

        if (!compileShader(fragmentId)) return -1;
        if (!compileShader(vertexId)) return -1;

        final int programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, fragmentId);
        GL20.glAttachShader(programId, vertexId);
        GL20.glLinkProgram(programId);
        GL20.glValidateProgram(programId);

        System.out.println("[DEBUG] Created shader program ID: " + programId);
        return programId;
    }

    private static boolean compileShader(final int shaderId) {
        final boolean compiled = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE;
        if (compiled) return true;

        final String shaderLog = GL20.glGetShaderInfoLog(shaderId, 8192);
        Logger.error("Error while compiling shader: ");
        Logger.error("-------------------------------");
        Logger.error(shaderLog);
        return false;
    }

    public static String getShaderResource(final String resource) {
        try {
            final InputStream inputStream = ResourceManager.resources.getStream("shader/" + resource);
            if (inputStream == null) return null;
            return new String(StreamUtils.readStream(inputStream), StandardCharsets.UTF_8);
        } catch (final IOException | NullPointerException e) {
            Logger.error("An error occurred while getting a shader resource");
            Logger.exception(e);
            return null;
        }
    }

    public static void drawQuads(final ScaledResolution sr) {
        if (ReflectionManager.hasOptifine && Minecraft.getMinecraft().gameSettings.ofFastRender) return;
        final float width = (float) sr.getScaledWidth_double();
        final float height = (float) sr.getScaledHeight_double();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }
}
