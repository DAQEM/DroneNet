package com.daqem.irobot.client.renderer;

import com.daqem.irobot.item.TaskItem;
import com.daqem.irobot.item.TaskMarkerItem;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class OutlineRenderer {

    /**
     * Renders an outline for the area defined by an TaskMarkerItem held by the player.
     *
     * @param poseStack The pose stack for rendering transformations.
     */
    public static void renderOutline(PoseStack poseStack) {
        ItemStack itemStack = getValidTaskMarkerItem();
        if (itemStack == null) return;

        TaskMarkerDataComponent data = getTaskMarkerData(itemStack);
        if (data == null) return;

        GlobalPos firstPos = data.firstPos();
        GlobalPos secondPos = data.secondPos();
        if (!isValidBox(firstPos, secondPos)) return;

        renderBoxOutline(poseStack, firstPos, secondPos);
    }

    /**
     * Retrieves a valid TaskMarkerItem from the player's main or offhand.
     *
     * @return The ItemStack containing an TaskMarkerItem, or null if none found.
     */
    private static ItemStack getValidTaskMarkerItem() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null) {
            return null;
        }

        ItemStack mainHand = localPlayer.getMainHandItem();
        if (mainHand.getItem() instanceof TaskMarkerItem || mainHand.getItem() instanceof TaskItem) {
            return mainHand;
        }

        ItemStack offHand = localPlayer.getOffhandItem();
        if (offHand.getItem() instanceof TaskMarkerItem || mainHand.getItem() instanceof TaskItem) {
            return offHand;
        }

        return null;
    }

    /**
     * Gets the TaskMarkerDataComponent from the given ItemStack.
     *
     * @param itemStack The ItemStack to check for the component.
     * @return The TaskMarkerDataComponent, or null if not present.
     */
    private static TaskMarkerDataComponent getTaskMarkerData(ItemStack itemStack) {
        DataComponentType<TaskMarkerDataComponent> component = IRobotDataComponents.TASK_MARKER_DATA.get();
        if (!itemStack.has(component)) {
            return null;
        }
        return itemStack.get(component);
    }

    /**
     * Checks if the box defined by two BlockPos is valid for rendering.
     *
     * @param firstPos  The first position of the box.
     * @param secondPos The second position of the box.
     * @return True if the box is valid, false otherwise.
     */
    private static boolean isValidBox(GlobalPos firstPos, GlobalPos secondPos) {
        return firstPos != null && !firstPos.pos().equals(BlockPos.ZERO) &&
                secondPos != null && !secondPos.pos().equals(BlockPos.ZERO) &&
                firstPos.dimension().equals(secondPos.dimension());
    }

    /**
     * Renders the outline of the box defined by two BlockPos.
     *
     * @param poseStack The pose stack for rendering transformations.
     * @param firstPos  The first position of the box.
     * @param secondPos The second position of the box.
     */
    private static void renderBoxOutline(PoseStack poseStack, GlobalPos firstPos, GlobalPos secondPos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !firstPos.dimension().equals(minecraft.level.dimension())) {
            return;
        }
        AABB box = createBoundingBox(firstPos.pos(), secondPos.pos());
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        OutlineBufferSource buffer = minecraft.renderBuffers().outlineBufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.debugQuads());

        float inflate = box.contains(camera) ? -1 / 128f : 1 / 128f;
        box = box.move(camera.scale(-1));

        Vector3f minPos = new Vector3f(
                (float) box.minX - inflate,
                (float) box.minY - inflate,
                (float) box.minZ - inflate
        );
        Vector3f maxPos = new Vector3f(
                (float) box.maxX + inflate,
                (float) box.maxY + inflate,
                (float) box.maxZ + inflate
        );

        renderBoxEdges(poseStack, consumer, minPos, maxPos);
        buffer.endOutlineBatch();
        poseStack.popPose();
    }

    /**
     * Creates an AABB from two BlockPos, ensuring both positions are included regardless of positive or negative coordinates.
     * Applies slight offsets to avoid z-fighting.
     *
     * @param firstPos  The first position of the box.
     * @param secondPos The second position of the box.
     * @return The constructed AABB.
     */
    public static AABB createBoundingBox(BlockPos firstPos, BlockPos secondPos) {
        double minX = Math.min(firstPos.getX(), secondPos.getX()) + 0.001;
        double minY = Math.min(firstPos.getY(), secondPos.getY()) + 0.001;
        double minZ = Math.min(firstPos.getZ(), secondPos.getZ()) + 0.001;
        double maxX = Math.max(firstPos.getX(), secondPos.getX()) + 1 - 0.001;
        double maxY = Math.max(firstPos.getY(), secondPos.getY()) + 1 - 0.001;
        double maxZ = Math.max(firstPos.getZ(), secondPos.getZ()) + 1 - 0.001;

        return new AABB(
                new Vec3(minX, minY, minZ),
                new Vec3(maxX, maxY, maxZ)
        );
    }

    protected static void renderBoxEdges(PoseStack ms, VertexConsumer consumer,
                                         Vector3f minPos, Vector3f maxPos) {

        PoseStack.Pose pose = ms.last();

        float lenX = maxPos.x() - minPos.x();
        float lenY = maxPos.y() - minPos.y();
        float lenZ = maxPos.z() - minPos.z();

        class Edge {
            final float x, y, z, len;
            final Direction dir;

            Edge(float x, float y, float z, Direction dir, float len) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.dir = dir;
                this.len = len;
            }
        }

        List<Edge> edges = List.of(
                new Edge(minPos.x(), minPos.y(), minPos.z(), Direction.EAST, lenX),
                new Edge(minPos.x(), minPos.y(), minPos.z(), Direction.UP, lenY),
                new Edge(minPos.x(), minPos.y(), minPos.z(), Direction.SOUTH, lenZ),

                new Edge(maxPos.x(), minPos.y(), minPos.z(), Direction.UP, lenY),
                new Edge(maxPos.x(), minPos.y(), minPos.z(), Direction.SOUTH, lenZ),

                new Edge(minPos.x(), maxPos.y(), minPos.z(), Direction.EAST, lenX),
                new Edge(minPos.x(), maxPos.y(), minPos.z(), Direction.SOUTH, lenZ),

                new Edge(minPos.x(), minPos.y(), maxPos.z(), Direction.EAST, lenX),
                new Edge(minPos.x(), minPos.y(), maxPos.z(), Direction.UP, lenY),

                new Edge(minPos.x(), maxPos.y(), maxPos.z(), Direction.EAST, lenX),
                new Edge(maxPos.x(), minPos.y(), maxPos.z(), Direction.UP, lenY),
                new Edge(maxPos.x(), maxPos.y(), minPos.z(), Direction.SOUTH, lenZ)
        );

        Vector3f origin = new Vector3f();
        for (Edge e : edges) {
            origin.set(e.x, e.y, e.z);
            bufferCuboidLine(pose, consumer, origin, e.dir, e.len);
        }
    }


    public static void bufferCuboidLine(PoseStack.Pose pose, VertexConsumer consumer, Vector3f origin, Direction direction,
                                        float length) {
        Vector3f minPos = new Vector3f();
        Vector3f maxPos = new Vector3f();

        float halfWidth = 0.0625F / 2;
        minPos.set(origin.x() - halfWidth, origin.y() - halfWidth, origin.z() - halfWidth);
        maxPos.set(origin.x() + halfWidth, origin.y() + halfWidth, origin.z() + halfWidth);

        switch (direction) {
            case DOWN -> minPos.add(0, -length, 0);
            case UP -> maxPos.add(0, length, 0);
            case NORTH -> minPos.add(0, 0, -length);
            case SOUTH -> maxPos.add(0, 0, length);
            case WEST -> minPos.add(-length, 0, 0);
            case EAST -> maxPos.add(length, 0, 0);
        }

        bufferCuboid(pose, consumer, minPos, maxPos);
    }

    public static void bufferCuboid(PoseStack.Pose pose, VertexConsumer consumer,
                                    Vector3f minPos, Vector3f maxPos) {
        Matrix4f posMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        Vector3f[] corners = {
                new Vector3f(minPos.x(), minPos.y(), maxPos.z()),
                new Vector3f(minPos.x(), minPos.y(), minPos.z()),
                new Vector3f(maxPos.x(), minPos.y(), minPos.z()),
                new Vector3f(maxPos.x(), minPos.y(), maxPos.z()),
                new Vector3f(minPos.x(), maxPos.y(), minPos.z()),
                new Vector3f(minPos.x(), maxPos.y(), maxPos.z()),
                new Vector3f(maxPos.x(), maxPos.y(), maxPos.z()),
                new Vector3f(maxPos.x(), maxPos.y(), minPos.z())
        };

        Vector4f temp = new Vector4f();
        float[][] worldCorners = new float[corners.length][3];
        for (int i = 0; i < corners.length; i++) {
            temp.set(corners[i].x(), corners[i].y(), corners[i].z(), 1.0f);
            temp.mul(posMatrix);
            worldCorners[i][0] = temp.x();
            worldCorners[i][1] = temp.y();
            worldCorners[i][2] = temp.z();
        }

        int[][] faces = {
                {0, 1, 2, 3},
                {4, 5, 6, 7},
                {7, 2, 1, 4},
                {5, 0, 3, 6},
                {4, 1, 0, 5},
                {6, 3, 2, 7}
        };

        Vector3f[] normals = {
                new Vector3f(0, -1, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, -1),
                new Vector3f(0, 0, 1),
                new Vector3f(-1, 0, 0),
                new Vector3f(1, 0, 0)
        };

        float[][] uvs = {
                {0, 0}, {0, 1}, {1, 1}, {1, 0}
        };

        Vector3f normalTemp = new Vector3f();
        for (int f = 0; f < faces.length; f++) {
            normalTemp.set(normals[f]);
            normalTemp.mul(normalMatrix);

            float nx = normalTemp.x();
            float ny = normalTemp.y();
            float nz = normalTemp.z();

            for (int v = 0; v < 4; v++) {
                int idx = faces[f][v];
                float[] pos = worldCorners[idx];
                float[] uv = uvs[v];

                consumer.addVertex(pos[0], pos[1], pos[2])
                        .setColor(64, 229, 242, 255)
                        .setUv(uv[0], uv[1])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(1)
                        .setNormal(nx, ny, nz);
            }
        }
    }
}
