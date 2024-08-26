package cn.yapeteam.yolbi.utils.render.shader.base;

import org.lwjgl.opengl.GL20;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

public class ShaderUniforms {

    private static final Logger logger = Logger.getLogger(ShaderUniforms.class.getName());

    public static void uniformFB(final int programId, final String name, final FloatBuffer floatBuffer) {
        logger.info("uniformFB called with programId: " + programId + ", name: " + name + ", floatBuffer: " + floatBuffer);
        GL20.glUniform1(getLocation(programId, name), floatBuffer);
    }

    public static void uniform1i(final int programId, final String name, final int i) {
        logger.info("uniform1i called with programId: " + programId + ", name: " + name + ", i: " + i);
        GL20.glUniform1i(getLocation(programId, name), i);
    }

    public static void uniform2i(final int programId, final String name, final int i, final int j) {
        logger.info("uniform2i called with programId: " + programId + ", name: " + name + ", i: " + i + ", j: " + j);
        GL20.glUniform2i(getLocation(programId, name), i, j);
    }

    public static void uniform1f(final int programId, final String name, final float f) {
        logger.info("uniform1f called with programId: " + programId + ", name: " + name + ", f: " + f);
        GL20.glUniform1f(getLocation(programId, name), f);
    }

    public static void uniform2f(final int programId, final String name, final float f, final float g) {
        logger.info("uniform2f called with programId: " + programId + ", name: " + name + ", f: " + f + ", g: " + g);
        GL20.glUniform2f(getLocation(programId, name), f, g);
    }

    public static void uniform3f(final int programId, final String name, final float f, final float g, final float h) {
        logger.info("uniform3f called with programId: " + programId + ", name: " + name + ", f: " + f + ", g: " + g + ", h: " + h);
        GL20.glUniform3f(getLocation(programId, name), f, g, h);
    }

    public static void uniform4f(final int programId, final String name, final float f, final float g, final float h, final float i) {
        logger.info("uniform4f called with programId: " + programId + ", name: " + name + ", f: " + f + ", g: " + g + ", h: " + h + ", i: " + i);
        GL20.glUniform4f(getLocation(programId, name), f, g, h, i);
    }

    private static int getLocation(final int programId, final String name) {
        int location = GL20.glGetUniformLocation(programId, name);
        logger.info("getLocation called with programId: " + programId + ", name: " + name + ", location: " + location);
        return location;
    }
}