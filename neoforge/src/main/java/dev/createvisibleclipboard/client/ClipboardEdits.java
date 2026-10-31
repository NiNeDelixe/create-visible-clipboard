package dev.createvisibleclipboard.client;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;

import dev.createvisibleclipboard.ClipboardLayout;

public class ClipboardEdits {
    public static ClipboardContent toggle(ClipboardContent content, int entryIndex) {
        if (content.pages().isEmpty())
            return content;
        int pageIndex = ClipboardLayout.pageIndex(content.previouslyOpenedPage(), content.pages().size());
        List<ClipboardEntry> page = content.pages().get(pageIndex);
        if (entryIndex < 0 || entryIndex >= page.size())
            return content;

        ClipboardEntry previous = page.get(entryIndex);
        ClipboardEntry replacement = new ClipboardEntry(!previous.checked, previous.text.copy());
        replacement.displayItem(previous.icon.copy(), previous.itemAmount);
        List<ClipboardEntry> updatedPage = new ArrayList<>(page);
        updatedPage.set(entryIndex, replacement);
        List<List<ClipboardEntry>> pages = new ArrayList<>(content.pages());
        pages.set(pageIndex, List.copyOf(updatedPage));
        return content.setPages(List.copyOf(pages))
            .setPreviouslyOpenedPage(pageIndex)
            .setType(ClipboardType.WRITTEN);
    }

    public static ClipboardContent turnPage(ClipboardContent content, int direction) {
        int count = content.pages().size();
        if (count == 0 || direction == 0)
            return content;
        int current = ClipboardLayout.pageIndex(content.previouslyOpenedPage(), count);
        int next = ClipboardLayout.pageIndex(current + Integer.signum(direction), count);
        return current == next ? content : content.setPreviouslyOpenedPage(next);
    }
}
