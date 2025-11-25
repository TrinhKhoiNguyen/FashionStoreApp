package com.example.fashionstoreapp.models;

/**
 * Constants for Order Status management
 */
public class OrderStatus {
    // Status constants
    public static final String PENDING = "pending"; // Chờ xác nhận
    public static final String PROCESSING = "processing"; // Đang chuẩn bị
    public static final String SHIPPING = "shipping"; // Đang giao
    public static final String DELIVERED = "delivered"; // Đã giao
    public static final String CANCELLED = "cancelled"; // Đã hủy

    /**
     * Get Vietnamese text for status
     */
    public static String getStatusText(String status) {
        if (status == null)
            return "Không xác định";
        switch (status) {
            case PENDING:
                return "Chờ xác nhận";
            case PROCESSING:
                return "Đang chuẩn bị";
            case SHIPPING:
                return "Đang giao";
            case DELIVERED:
                return "Đã giao";
            case CANCELLED:
                return "Đã hủy";
            default:
                return status;
        }
    }

    /**
     * Get all available statuses
     */
    public static String[] getAllStatuses() {
        return new String[] {
                PENDING,
                PROCESSING,
                SHIPPING,
                DELIVERED,
                CANCELLED
        };
    }

    /**
     * Get all status texts
     */
    public static String[] getAllStatusTexts() {
        return new String[] {
                "Chờ xác nhận",
                "Đang chuẩn bị",
                "Đang giao",
                "Đã giao",
                "Đã hủy"
        };
    }

    /**
     * Check if status can be updated to next status
     */
    public static boolean canUpdateTo(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null)
            return false;

        // Can always cancel
        if (newStatus.equals(CANCELLED))
            return true;

        // Cannot change from final states
        if (currentStatus.equals(DELIVERED) || currentStatus.equals(CANCELLED)) {
            return false;
        }

        // Logical progression
        switch (currentStatus) {
            case PENDING:
                return newStatus.equals(PROCESSING) || newStatus.equals(CANCELLED);
            case PROCESSING:
                return newStatus.equals(SHIPPING) || newStatus.equals(CANCELLED);
            case SHIPPING:
                return newStatus.equals(DELIVERED) || newStatus.equals(CANCELLED);
            default:
                return false;
        }
    }
}
