package dev.createvisibleclipboard.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.createvisibleclipboard.client.ClipboardItemRendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private BakedModel visibleClipboard$heldModel(BakedModel model, ItemStack stack, ItemDisplayContext context,
        boolean leftHand, PoseStack pose, MultiBufferSource buffer, int light, int overlay, BakedModel original) {
        if (ClipboardItemRendering.isHeldClipboard(stack, context))
            return Minecraft.getInstance().getModelManager().getModel(ClipboardItemRendering.MODEL);
        return model;
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void visibleClipboard$renderPage(ItemStack stack, ItemDisplayContext context, boolean leftHand,
        PoseStack pose, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (ClipboardItemRendering.isHeldClipboard(stack, context))
            ClipboardItemRendering.render(stack, pose, buffer, light);
    }
}
