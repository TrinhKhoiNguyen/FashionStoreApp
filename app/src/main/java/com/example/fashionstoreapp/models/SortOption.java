package com.example.fashionstoreapp.models;

/**
 * Enum for product sorting options
 * Maps to Firestore orderBy fields and client-side comparators
 */
public enum SortOption {
    DEFAULT("Mặc định", null, null),
    PRICE_LOW_TO_HIGH("Giá thấp → cao", "currentPrice", "ASCENDING"),
    PRICE_HIGH_TO_LOW("Giá cao → thấp", "currentPrice", "DESCENDING"),
    NEWEST("Mới nhất", "createdAt", "DESCENDING"),
    POPULARITY("Phổ biến", "popularity", "DESCENDING"),
    RATING("Đánh giá cao", "averageRating", "DESCENDING");

    private final String displayName;
    private final String firestoreField; // Field name in Firestore
    private final String firestoreDirection; // ASCENDING or DESCENDING

    SortOption(String displayName, String firestoreField, String firestoreDirection) {
        this.displayName = displayName;
        this.firestoreField = firestoreField;
        this.firestoreDirection = firestoreDirection;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFirestoreField() {
        return firestoreField;
    }

    public String getFirestoreDirection() {
        return firestoreDirection;
    }

    /**
     * Check if this sort option can be done server-side (Firestore orderBy)
     */
    public boolean isServerSideSort() {
        return firestoreField != null;
    }

    /**
     * Get SortOption from display name
     */
    public static SortOption fromDisplayName(String displayName) {
        for (SortOption option : values()) {
            if (option.displayName.equals(displayName)) {
                return option;
            }
        }
        return DEFAULT;
    }
}
