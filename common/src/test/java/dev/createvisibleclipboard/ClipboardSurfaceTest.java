package dev.createvisibleclipboard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

class ClipboardSurfaceTest {
    @Test
    void matchesCreatesTwelveBlockstateRotations() {
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (AttachFace face : AttachFace.values()) {
            for (int i = 0; i < directions.length; i++) {
                int pitch = switch (face) {
                    case FLOOR -> 0;
                    case WALL -> 90;
                    case CEILING -> 180;
                };
                int yaw = (i * 90 + (face == AttachFace.CEILING ? 180 : 0)) % 360;
                Matrix4f expected = new Matrix4f().translate(0.5f, 0.5f, 0.5f)
                    .mul(BlockModelRotation.by(pitch, yaw).getRotation().getMatrix())
                    .translate(-0.5f, -0.5f, -0.5f);
                Vector3f point = new Vector3f(0.2f, 0.0625f, 0.8f);
                assertVector(expected.transformPosition(new Vector3f(point)),
                    ClipboardSurface.blockTransform(face, directions[i]).transformPosition(point));
            }
        }
    }

    @Test
    void clickingTheRenderedCheckboxReturnsItsPageCoordinatesForEveryPlacement() {
        for (AttachFace face : AttachFace.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                Matrix4f transform = ClipboardSurface.blockTransform(face, facing).mul(ClipboardSurface.pageTransform());
                for (Vector3f point : new Vector3f[] {new Vector3f(6, 30, 0), new Vector3f(158, 195, 0)}) {
                    Vector3f world = transform.transformPosition(new Vector3f(point));
                    Vector3f actual = ClipboardSurface.toPage(face, facing, new Vec3(world.x, world.y, world.z));
                    assertVector(point, actual);
                }
            }
        }
    }

    @Test
    void floorTextStartsUnderTheClipAndReadsTowardTheBottom() {
        Matrix4f transform = ClipboardSurface.pageTransform();
        Vector3f origin = transform.transformPosition(new Vector3f());
        assertVector(new Vector3f(0.75f, 0.063f, 0.8125f), origin);
        Vector3f right = transform.transformDirection(new Vector3f(1, 0, 0)).normalize();
        Vector3f down = transform.transformDirection(new Vector3f(0, 1, 0)).normalize();
        assertVector(new Vector3f(-1, 0, 0), right);
        assertVector(new Vector3f(0, 0, -1), down);
    }

    private static void assertVector(Vector3f expected, Vector3f actual) {
        assertEquals(expected.x, actual.x, 0.001);
        assertEquals(expected.y, actual.y, 0.001);
        assertEquals(expected.z, actual.z, 0.001);
    }
}
