package cn.yapeteam.yolbi.utils.render;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.math.vector.Vector3d;
import cn.yapeteam.yolbi.utils.math.vector.Vector4d;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectionUtil implements Accessor {
    private static final HashMap<Entity, Projection> nextProjections = new HashMap<>();
    private static HashMap<Entity, Projection> currentProjections = new HashMap<>();

    @Listener
    public void onRender2d(EventRender2D eventRender2D){
        final HashMap<Entity, Projection> newProjections = new HashMap<>();

        for (Map.Entry<Entity, Projection> map : nextProjections.entrySet()) {
            Projection projection = map.getValue();
            projection.position = project(map.getKey());

            newProjections.put(map.getKey(), projection);
        }

        currentProjections = newProjections;
        nextProjections.clear();
    };

    public static Vector4d get(Entity entity) {
        if (entity == null) return null;

        if (!nextProjections.containsKey(entity)) {
            nextProjections.put(entity, new Projection());
        }

        Projection projection = currentProjections.get(entity);

        if (projection == null) {
            return null;
        }

        return projection.getPosition();
    }

    private Vector3d project(final int factor, final double x, final double y, final double z) {
        if (GLU.gluProject((float) x, (float) y, (float) z, ActiveRenderInfo.MODELVIEW, ActiveRenderInfo.PROJECTION, ActiveRenderInfo.VIEWPORT, ActiveRenderInfo.OBJECTCOORDS)) {
            return new Vector3d((ActiveRenderInfo.OBJECTCOORDS.get(0) / factor), ((Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS.get(1)) / factor), ActiveRenderInfo.OBJECTCOORDS.get(2));
        }

        return null;
    }

    public Vector4d project(Entity entity) {
        final double renderX = ;
        final double renderY = mc.getRenderManager().renderPosY;
        final double renderZ = mc.getRenderManager().renderPosZ;

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - renderX;
        final double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks) - renderY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - renderZ;
        final double width = (entity.width + 0.2) / 2;
        final double height = entity.height + (entity.isSneaking() ? -0.3D : 0.2D) + 0.05;
        final AxisAlignedBB aabb = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
        final List<Vector3d> vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));



        Vector4d position = null;
        for (Vector3d vector : vectors) {
            vector = project(scaledResolution.getScaleFactor(), vector.getX(), vector.getY(), vector.getZ());

            if (vector != null && vector.getZ() >= 0.0D && vector.getZ() < 1.0D) {
                if (position == null) {
                    position = new Vector4d(vector.getX(), vector.getY(), vector.getZ(), 0.0D);
                }

                position = new Vector4d(Math.min(vector.getX(), position.x), Math.min(vector.getY(), position.y), Math.max(vector.getX(), position.z), Math.max(vector.getY(), position.w));
            }
        }

        return position;
    }

    @Getter
    private static class Projection {
        private Vector4d position;
    }
}
