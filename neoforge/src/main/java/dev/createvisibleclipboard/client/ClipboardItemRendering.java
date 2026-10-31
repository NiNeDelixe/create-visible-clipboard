package dev.createvisibleclipboard.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;

import dev.createvisibleclipboard.ClipboardSurface;
import dev.createvisibleclipboard.VisibleClipboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ClipboardItemRendering {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
        ResourceLocation.fromNamespaceAndPath(VisibleClipboard.ID, "item/clipboard_in_hand"));

    public static boolean isHeldClipboard(ItemStack stack, ItemDisplayContext context) {
        return AllBlocks.CLIPBOARD.isIn(stack) && (context.firstPerson()
            || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
    }

    public static void render(ItemStack stack, PoseStack pose, MultiBufferSource buffer, int light) {
        ClipboardContent content = stack.get(AllDataComponents.CLIPBOARD_CONTENT);
        if (content == null || content.pages().isEmpty())
            return;
        pose.pushPose();
        pose.mulPose(ClipboardSurface.pageTransform());
        new ClipboardPage(content, Minecraft.getInstance().font).render(pose, buffer, light, -1, -1);
        pose.popPose();
    }
}
