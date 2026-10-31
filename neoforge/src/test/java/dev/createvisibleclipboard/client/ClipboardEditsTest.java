package dev.createvisibleclipboard.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

class ClipboardEditsTest {
    @Test
    void togglesACopyAndPreservesTextIconsOtherPagesAndCopiedSettings() {
        ClipboardEntry original = new ClipboardEntry(false, Component.literal("Deliver gears"))
            .displayItem(Items.DIAMOND.getDefaultInstance(), 32);
        List<ClipboardEntry> otherPage = List.of(new ClipboardEntry(true, Component.literal("Other task")));
        CompoundTag copiedValues = new CompoundTag();
        copiedValues.putInt("Speed", 64);
        ClipboardContent content = new ClipboardContent(ClipboardType.WRITTEN,
            List.of(List.of(original), otherPage), true, 0, Optional.of(copiedValues));

        ClipboardContent result = ClipboardEdits.toggle(content, 0);
        ClipboardEntry changed = result.pages().getFirst().getFirst();
        assertTrue(changed.checked);
        assertFalse(original.checked);
        assertNotSame(original, changed);
        assertNotSame(original.text, changed.text);
        assertNotSame(original.icon, changed.icon);
        assertEquals(original.text, changed.text);
        assertTrue(changed.icon.is(Items.DIAMOND));
        assertEquals(32, changed.itemAmount);
        assertSame(otherPage, result.pages().get(1));
        assertEquals(content.copiedValues(), result.copiedValues());
        assertTrue(result.readOnly());
    }

    @Test
    void navigationOnlyVisitsExistingPagesAndPreservesContent() {
        ClipboardContent content = new ClipboardContent(ClipboardType.WRITTEN,
            List.of(List.of(new ClipboardEntry(false, Component.literal("First"))), List.of()), false);
        assertSame(content, ClipboardEdits.turnPage(content, -1));
        ClipboardContent next = ClipboardEdits.turnPage(content, 1);
        assertEquals(1, next.previouslyOpenedPage());
        assertSame(content.pages(), next.pages());
        assertSame(next, ClipboardEdits.turnPage(next, 1));
        assertEquals(0, ClipboardEdits.turnPage(next, -1).previouslyOpenedPage());
    }

    @Test
    void emptyOrStaleTargetsDoNotEraseAnything() {
        assertSame(ClipboardContent.EMPTY, ClipboardEdits.toggle(ClipboardContent.EMPTY, 0));
        assertSame(ClipboardContent.EMPTY, ClipboardEdits.turnPage(ClipboardContent.EMPTY, 1));
        ClipboardContent content = new ClipboardContent(ClipboardType.WRITTEN,
            List.of(List.of(new ClipboardEntry(false, Component.literal("Task")))), false)
            .setPreviouslyOpenedPage(-5);
        assertSame(content, ClipboardEdits.toggle(content, -1));
        assertSame(content, ClipboardEdits.toggle(content, 1));
        assertEquals(0, ClipboardEdits.toggle(content, 0).previouslyOpenedPage());
    }
}
