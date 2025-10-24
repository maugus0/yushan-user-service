package com.yushan.user_service.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NovelStatus enum.
 */
class NovelStatusTest {

    @Test
    void testDraftStatus() {
        // Given & When
        NovelStatus status = NovelStatus.DRAFT;

        // Then
        assertEquals(0, status.getValue());
        assertEquals("Draft", status.getDescription());
    }

    @Test
    void testUnderReviewStatus() {
        // Given & When
        NovelStatus status = NovelStatus.UNDER_REVIEW;

        // Then
        assertEquals(1, status.getValue());
        assertEquals("Under Review", status.getDescription());
    }

    @Test
    void testPublishedStatus() {
        // Given & When
        NovelStatus status = NovelStatus.PUBLISHED;

        // Then
        assertEquals(2, status.getValue());
        assertEquals("Published", status.getDescription());
    }

    @Test
    void testHiddenStatus() {
        // Given & When
        NovelStatus status = NovelStatus.HIDDEN;

        // Then
        assertEquals(3, status.getValue());
        assertEquals("Hidden", status.getDescription());
    }

    @Test
    void testArchivedStatus() {
        // Given & When
        NovelStatus status = NovelStatus.ARCHIVED;

        // Then
        assertEquals(4, status.getValue());
        assertEquals("Archived", status.getDescription());
    }

    @Test
    void testFromValueValid() {
        // Test all valid values
        assertEquals(NovelStatus.DRAFT, NovelStatus.fromValue(0));
        assertEquals(NovelStatus.UNDER_REVIEW, NovelStatus.fromValue(1));
        assertEquals(NovelStatus.PUBLISHED, NovelStatus.fromValue(2));
        assertEquals(NovelStatus.HIDDEN, NovelStatus.fromValue(3));
        assertEquals(NovelStatus.ARCHIVED, NovelStatus.fromValue(4));
    }

    @Test
    void testFromValueInvalid() {
        // Test invalid values
        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.fromValue(-1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.fromValue(5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.fromValue(999);
        });
    }

    @Test
    void testFromNameValid() {
        // Test valid names (case insensitive)
        assertEquals(NovelStatus.DRAFT, NovelStatus.fromName("DRAFT"));
        assertEquals(NovelStatus.DRAFT, NovelStatus.fromName("draft"));
        assertEquals(NovelStatus.DRAFT, NovelStatus.fromName("Draft"));

        assertEquals(NovelStatus.UNDER_REVIEW, NovelStatus.fromName("UNDER_REVIEW"));
        assertEquals(NovelStatus.UNDER_REVIEW, NovelStatus.fromName("under_review"));
        assertEquals(NovelStatus.UNDER_REVIEW, NovelStatus.fromName("Under_Review"));

        assertEquals(NovelStatus.PUBLISHED, NovelStatus.fromName("PUBLISHED"));
        assertEquals(NovelStatus.PUBLISHED, NovelStatus.fromName("published"));
        assertEquals(NovelStatus.PUBLISHED, NovelStatus.fromName("Published"));

        assertEquals(NovelStatus.HIDDEN, NovelStatus.fromName("HIDDEN"));
        assertEquals(NovelStatus.HIDDEN, NovelStatus.fromName("hidden"));
        assertEquals(NovelStatus.HIDDEN, NovelStatus.fromName("Hidden"));

        assertEquals(NovelStatus.ARCHIVED, NovelStatus.fromName("ARCHIVED"));
        assertEquals(NovelStatus.ARCHIVED, NovelStatus.fromName("archived"));
        assertEquals(NovelStatus.ARCHIVED, NovelStatus.fromName("Archived"));
    }

    @Test
    void testFromNameNull() {
        // Test null name
        assertNull(NovelStatus.fromName(null));
    }

    @Test
    void testFromNameInvalid() {
        // Test invalid names
        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.fromName("INVALID_STATUS");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.fromName("DRAFT_INVALID");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.fromName("");
        });
    }

    @Test
    void testNovelStatusValues() {
        // Given & When
        NovelStatus[] values = NovelStatus.values();

        // Then
        assertEquals(5, values.length);
        assertArrayEquals(new NovelStatus[]{
            NovelStatus.DRAFT,
            NovelStatus.UNDER_REVIEW,
            NovelStatus.PUBLISHED,
            NovelStatus.HIDDEN,
            NovelStatus.ARCHIVED
        }, values);
    }

    @Test
    void testNovelStatusValueOf() {
        // Given & When
        NovelStatus draft = NovelStatus.valueOf("DRAFT");
        NovelStatus published = NovelStatus.valueOf("PUBLISHED");
        NovelStatus archived = NovelStatus.valueOf("ARCHIVED");

        // Then
        assertEquals(NovelStatus.DRAFT, draft);
        assertEquals(NovelStatus.PUBLISHED, published);
        assertEquals(NovelStatus.ARCHIVED, archived);
    }

    @Test
    void testNovelStatusValueOfInvalid() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            NovelStatus.valueOf("INVALID_STATUS");
        });
    }
}
