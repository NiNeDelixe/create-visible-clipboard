package dev.createvisibleclipboard.client;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlock;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;

import dev.createvisibleclipboard.ClipboardSurface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ClipboardBlockRenderer implements BlockEntityRenderer<ClipboardBlockEntity> {
    public ClipboardBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ClipboardBlockEntity clipboard, float partialTick, PoseStack pose,
        MultiBufferSource buffer, int light, int overlay) {
        ClipboardContent content = clipboard.components().get(AllDataComponents.CLIPBOARD_CONTENT);
        if (content == null || content.pages().isEmpty())
            return;
        Minecraft mc = Minecraft.getInstance();
        BlockState state = clipboard.getBlockState();
        var face = state.getValue(ClipboardBlock.FACE);
        var facing = state.getValue(ClipboardBlock.FACING);
        Vector3f hover = new Vector3f(-1, -1, 0);
        if (mc.hitResult instanceof BlockHitResult hit && hit.getBlockPos().equals(clipboard.getBlockPos())
            && hit.getDirection() == ClipboardSurface.front(face, facing))
            hover = ClipboardSurface.toPage(face, facing, hit.getLocation().subtract(Vec3.atLowerCornerOf(clipboard.getBlockPos())));

        pose.pushPose();
        pose.mulPose(ClipboardSurface.blockTransform(face, facing));
        pose.mulPose(ClipboardSurface.pageTransform());
        new ClipboardPage(content, mc.font).render(pose, buffer, light, hover.x, hover.y);
        pose.popPose();
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
