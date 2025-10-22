package com.yushan.user_service.enums;

/**
 * Enum representing the status of a novel.
 * Maps to integer values in the database.
 */
public enum NovelStatus {
    DRAFT(0, "Draft"),
    UNDER_REVIEW(1, "Under Review"),
    PUBLISHED(2, "Published"),
    HIDDEN(3, "Hidden"),
    ARCHIVED(4, "Archived");

    private final int value;
    private final String description;

    NovelStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get NovelStatus by integer value
     */
    public static NovelStatus fromValue(int value) {
        for (NovelStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid novel status value: " + value);
    }

    /**
     * Get NovelStatus by name (case-insensitive)
     */
    public static NovelStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        for (NovelStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid novel status name: " + name);
    }
}
