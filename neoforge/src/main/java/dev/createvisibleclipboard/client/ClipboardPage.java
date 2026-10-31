package dev.createvisibleclipboard.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;

import dev.createvisibleclipboard.ClipboardLayout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;

public class ClipboardPage {
    private static final int INK = 0xff311a00;
    private static final int GREEN = 0xff31b25d;

    private final List<Row> rows = new ArrayList<>();
    private final int pageIndex;
    private final int pageCount;
    private final float scale;

    public ClipboardPage(ClipboardContent content, Font font) {
        pageCount = content.pages().size();
        pageIndex = ClipboardLayout.pageIndex(content.previouslyOpenedPage(), pageCount);
        int y = 0;
        if (pageCount != 0) {
            for (ClipboardEntry entry : content.pages().get(pageIndex)) {
                int iconWidth = entry.icon.isEmpty() ? 0 : 18;
                List<FormattedCharSequence> lines = font.split(entry.text, ClipboardLayout.TEXT_WIDTH - iconWidth);
                int height = ClipboardLayout.rowHeight(lines.size(), iconWidth != 0);
                rows.add(new Row(entry, lines, y, height));
                y += height;
            }
        }
        scale = ClipboardLayout.contentScale(y);
    }

    public int entryAt(double x, double y) {
        x /= scale;
        y /= scale;
        if (x < 0 || x >= ClipboardLayout.CONTROL_SIZE)
            return -1;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (y >= row.y && y < row.y + row.height)
                return i;
        }
        return -1;
    }

    public void render(PoseStack pose, MultiBufferSource buffer, int light, double hoverX, double hoverY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int hoveredEntry = entryAt(hoverX, hoverY);
        pose.pushPose();
        pose.scale(scale, scale, scale);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            ClipboardEntry entry = row.entry;
            draw(font, "\u25a1", 0, row.y, i == hoveredEntry ? GREEN : 0xff8d7f6b, pose, buffer, light);
            if (entry.checked)
                draw(font, "\u2714", 0, row.y - 1, GREEN, pose, buffer, light);

            int textX = ClipboardLayout.TEXT_X;
            if (!entry.icon.isEmpty()) {
                pose.pushPose();
                pose.translate(textX + 8, row.y + 8, 0.1f);
                pose.scale(16, -16, 0.01f);
                mc.getItemRenderer().renderStatic(entry.icon, ItemDisplayContext.GUI, light,
                    OverlayTexture.NO_OVERLAY, pose, buffer, mc.level, 0);
                pose.popPose();
                if (entry.itemAmount > 1) {
                    String amount = Integer.toString(entry.itemAmount);
                    pose.pushPose();
                    pose.translate(textX + 17 - font.width(amount) * 0.5f, row.y + 12, 0.3f);
                    pose.scale(0.5f, 0.5f, 0.5f);
                    draw(font, amount, 0, 0, INK, pose, buffer, light);
                    pose.popPose();
                }
                textX += 18;
            }

            int y = row.y;
            for (FormattedCharSequence line : row.lines) {
                font.drawInBatch(line, textX, y, entry.checked ? GREEN : INK, false,
                    pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
                y += 9;
            }
        }
        pose.popPose();

        if (pageCount == 0)
            return;
        int hoverDirection = ClipboardLayout.pageDirection(hoverX, hoverY);
        if (pageIndex > 0)
            draw(font, "<", 2, ClipboardLayout.FOOTER_Y, hoverDirection == -1 ? GREEN : INK, pose, buffer, light);
        if (pageIndex + 1 < pageCount)
            draw(font, ">", ClipboardLayout.WIDTH - 10, ClipboardLayout.FOOTER_Y,
                hoverDirection == 1 ? GREEN : INK, pose, buffer, light);
        Component indicator = Component.translatable("book.pageIndicator", pageIndex + 1, pageCount);
        font.drawInBatch(indicator, (ClipboardLayout.WIDTH - font.width(indicator)) / 2f,
            ClipboardLayout.FOOTER_Y, INK, false, pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
    }

    private static void draw(Font font, String text, float x, float y, int color, PoseStack pose,
        MultiBufferSource buffer, int light) {
        font.drawInBatch(text, x, y, color, false, pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
    }

    private record Row(ClipboardEntry entry, List<FormattedCharSequence> lines, int y, int height) {}
}
