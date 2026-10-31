package dev.createvisibleclipboard.client;

import org.joml.Vector3f;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlock;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket;
import com.simibubi.create.content.equipment.clipboard.ClipboardScreen;

import dev.createvisibleclipboard.ClipboardLayout;
import dev.createvisibleclipboard.ClipboardSurface;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class ClipboardInteraction {
    private static boolean useHeld;

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.options.keyUse.isDown())
            useHeld = false;
    }

    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (!event.isUseItem() || mc.screen != null || mc.player == null || mc.level == null
            || mc.player.isSpectator() || !(mc.hitResult instanceof BlockHitResult hit)
            || !mc.player.canInteractWithBlock(hit.getBlockPos(), 0)
            || !(mc.level.getBlockEntity(hit.getBlockPos()) instanceof ClipboardBlockEntity clipboard))
            return;

        // Let Create's RightClickBlock handler copy into the held clipboard.
        if (!mc.player.isShiftKeyDown() && AllBlocks.CLIPBOARD.isIn(mc.player.getItemInHand(event.getHand())))
            return;

        // Cancel before vanilla sends use-on-block: sneaking would otherwise remove the clipboard.
        event.setCanceled(true);
        event.setSwingHand(false);
        if (useHeld)
            return;
        useHeld = true;
        if (mc.player.isShiftKeyDown()) {
            ScreenOpener.open(new ClipboardScreen(mc.player.getInventory().selected, clipboard.components(), hit.getBlockPos()));
            return;
        }

        BlockState state = clipboard.getBlockState();
        var face = state.getValue(ClipboardBlock.FACE);
        var facing = state.getValue(ClipboardBlock.FACING);
        if (hit.getDirection() != ClipboardSurface.front(face, facing))
            return;
        Vector3f point = ClipboardSurface.toPage(face, facing, hit.getLocation().subtract(Vec3.atLowerCornerOf(hit.getBlockPos())));
        ClipboardContent content = clipboard.components().getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
        int entryIndex = new ClipboardPage(content, mc.font).entryAt(point.x, point.y);
        ClipboardContent updated;
        SoundEvent sound;
        if (entryIndex >= 0) {
            updated = ClipboardEdits.toggle(content, entryIndex);
            boolean checked = updated.pages().get(updated.previouslyOpenedPage()).get(entryIndex).checked;
            sound = (checked ? AllSoundEvents.CLIPBOARD_CHECKMARK : AllSoundEvents.CLIPBOARD_ERASE).getMainEvent();
        } else {
            updated = ClipboardEdits.turnPage(content, ClipboardLayout.pageDirection(point.x, point.y));
            sound = SoundEvents.BOOK_PAGE_TURN;
        }
        if (updated == content || ClipboardEditPacket.clipboardProcessor(updated) == null)
            return;

        // shortcut: Create sends full snapshots; simultaneous editors can overwrite each other until server-side actions exist.
        CatnipServices.NETWORK.sendToServer(new ClipboardEditPacket(mc.player.getInventory().selected, updated, hit.getBlockPos()));
        PatchedDataComponentMap components = new PatchedDataComponentMap(clipboard.components());
        components.set(AllDataComponents.CLIPBOARD_CONTENT, updated);
        clipboard.setComponents(components);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1));
    }
}
