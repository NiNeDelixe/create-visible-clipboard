package dev.createvisibleclipboard;

import com.simibubi.create.AllBlockEntityTypes;

import dev.createvisibleclipboard.client.ClipboardBlockRenderer;
import dev.createvisibleclipboard.client.ClipboardInteraction;
import dev.createvisibleclipboard.client.ClipboardItemRendering;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = VisibleClipboard.ID, dist = Dist.CLIENT)
public class VisibleClipboard {
    public static final String ID = "create_visible_clipboard";

    public VisibleClipboard(IEventBus modBus) {
        modBus.addListener(this::registerRenderers);
        modBus.addListener(this::registerModels);
        NeoForge.EVENT_BUS.addListener(ClipboardInteraction::onUse);
        NeoForge.EVENT_BUS.addListener(ClipboardInteraction::onTick);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AllBlockEntityTypes.CLIPBOARD.get(), ClipboardBlockRenderer::new);
    }

    private void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ClipboardItemRendering.MODEL);
    }
}
