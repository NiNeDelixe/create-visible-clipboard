package dev.createvisibleclipboard;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

public class ClipboardSurface {
    public static final float SCALE = 0.5f / ClipboardLayout.WIDTH;

    public static Matrix4f blockTransform(AttachFace face, Direction facing) {
        float yaw = facing.toYRot() + (face == AttachFace.CEILING ? 0 : 180);
        float pitch = switch (face) {
            case FLOOR -> 0;
            case WALL -> 90;
            case CEILING -> 180;
        };
        return new Matrix4f().translate(0.5f, 0.5f, 0.5f)
            .rotateY((float) Math.toRadians(-yaw))
            .rotateX((float) Math.toRadians(-pitch))
            .translate(-0.5f, -0.5f, -0.5f);
    }

    public static Matrix4f pageTransform() {
        // The clip is at the south end of Create's unrotated floor model.
        return new Matrix4f().translate(12f / 16, 1f / 16 + 0.0005f, 13f / 16)
            .rotateX((float) Math.PI / 2)
            .scale(-SCALE);
    }

    public static Vector3f toPage(AttachFace face, Direction facing, Vec3 localHit) {
        return blockTransform(face, facing).mul(pageTransform()).invert()
            .transformPosition((float) localHit.x, (float) localHit.y, (float) localHit.z, new Vector3f());
    }

    public static Direction front(AttachFace face, Direction facing) {
        return switch (face) {
            case FLOOR -> Direction.UP;
            case WALL -> facing;
            case CEILING -> Direction.DOWN;
        };
    }
}
