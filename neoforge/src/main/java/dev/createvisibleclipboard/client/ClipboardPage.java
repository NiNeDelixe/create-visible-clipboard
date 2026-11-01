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
                List<FormattedCharSequence> lines = font.split(entry.text, ClipboardLayout.textWidth(iconWidth));
                rows.add(new Row(entry, lines, y));
                y += ClipboardLayout.rowHeight(lines.size(), iconWidth != 0);
            }
        }
        scale = ClipboardLayout.contentScale(y);
    }

    public int entryAt(double x, double y) {
        x /= scale;
        y /= scale;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (ClipboardLayout.checkboxContains(x, y, row.y))
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
            pose.pushPose();
            pose.translate(ClipboardLayout.CHECKBOX_LEFT_OFFSET, row.y, 0);
            float checkboxScale = (float) ClipboardLayout.CHECKBOX_SIZE / font.lineHeight;
            pose.scale(checkboxScale, checkboxScale, checkboxScale);
            draw(font, "\u25a1", 0, 0, i == hoveredEntry ? GREEN : 0xff8d7f6b, pose, buffer, light);
            if (entry.checked)
                draw(font, "\u2714", 0, -1, GREEN, pose, buffer, light);
            pose.popPose();

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

            pose.pushPose();
            pose.translate(textX, row.y, 0);
            pose.scale(ClipboardLayout.TEXT_SCALE, ClipboardLayout.TEXT_SCALE, ClipboardLayout.TEXT_SCALE);
            float y = 0;
            for (FormattedCharSequence line : row.lines) {
                font.drawInBatch(line, 0, y, entry.checked ? GREEN : INK, false,
                    pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
                y += ClipboardLayout.LINE_HEIGHT / ClipboardLayout.TEXT_SCALE;
            }
            pose.popPose();
        }
        pose.popPose();

        if (pageCount == 0)
            return;
        int hoverDirection = ClipboardLayout.pageDirection(hoverX, hoverY);
        if (pageIndex > 0)
            drawArrow(font, "<", 0, hoverDirection == -1 ? GREEN : INK, pose, buffer, light);
        if (pageIndex + 1 < pageCount)
            drawArrow(font, ">", ClipboardLayout.WIDTH - ClipboardLayout.ARROW_SIZE,
                hoverDirection == 1 ? GREEN : INK, pose, buffer, light);
        Component indicator = Component.translatable("book.pageIndicator", pageIndex + 1, pageCount);
        float indicatorScale = Math.min(ClipboardLayout.TEXT_SCALE,
            (ClipboardLayout.WIDTH - 2 * ClipboardLayout.ARROW_SIZE - 8f) / font.width(indicator));
        pose.pushPose();
        pose.translate(ClipboardLayout.WIDTH / 2f,
            ClipboardLayout.FOOTER_Y + (ClipboardLayout.FOOTER_HEIGHT - font.lineHeight * indicatorScale) / 2f, 0);
        pose.scale(indicatorScale, indicatorScale, indicatorScale);
        font.drawInBatch(indicator, -font.width(indicator) / 2f, 0, INK, false,
            pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
        pose.popPose();
    }

    private static void drawArrow(Font font, String arrow, float x, int color, PoseStack pose,
        MultiBufferSource buffer, int light) {
        float scale = (float) ClipboardLayout.ARROW_SIZE / font.lineHeight;
        pose.pushPose();
        pose.translate(x + (ClipboardLayout.ARROW_SIZE - font.width(arrow) * scale) / 2f, ClipboardLayout.ARROW_Y, 0);
        pose.scale(scale, scale, scale);
        draw(font, arrow, 0, 0, color, pose, buffer, light);
        pose.popPose();
    }

    private static void draw(Font font, String text, float x, float y, int color, PoseStack pose,
        MultiBufferSource buffer, int light) {
        font.drawInBatch(text, x, y, color, false, pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
    }

    private record Row(ClipboardEntry entry, List<FormattedCharSequence> lines, int y) {}
}
