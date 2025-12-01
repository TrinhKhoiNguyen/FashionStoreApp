package com.example.fashionstoreapp.utils;

import android.util.Log;

import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.Review;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static FirestoreManager instance;
    private FirebaseFirestore db;

    // Collection names
    private static final String COLLECTION_PRODUCTS = "products";
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final String COLLECTION_BANNERS = "banners";
    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_CARTS = "carts";
    private static final String COLLECTION_FAVORITES = "favorites";
    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_VOUCHERS = "vouchers";

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // ==================== PRODUCTS ====================

    /**
     * Load all products from Firestore
     */
    public void loadProducts(OnProductsLoadedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            products.add(product);
                        }
                    }
                    Log.d(TAG, "Loaded " + products.size() + " products");
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load products by category
     */
    public void loadProductsByCategory(String categoryId, OnProductsLoadedListener listener) {
        Log.d(TAG, "Loading products for category: " + categoryId);
        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("category", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    Log.d(TAG, "Total documents found: " + queryDocumentSnapshots.size());
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            products.add(product);
                            Log.d(TAG, "Product loaded: " + product.getName() + " (ID: " + document.getId() + ")");
                        } else {
                            Log.w(TAG, "Failed to parse document: " + document.getId());
                        }
                    }
                    Log.d(TAG, "Loaded " + products.size() + " products for category: " + categoryId);
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products by category: " + categoryId, e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load new arrival products
     */
    public void loadNewProducts(int limit, OnProductsLoadedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("isNew", true)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            products.add(product);
                        }
                    }
                    Log.d(TAG, "Loaded " + products.size() + " new products");
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading new products", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load products with vouchers
     */
    public void loadVoucherProducts(int limit, OnProductsLoadedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("hasVoucher", true)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            products.add(product);
                        }
                    }
                    Log.d(TAG, "Loaded " + products.size() + " voucher products");
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading voucher products", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Add a product to Firestore
     */
    public void addProduct(Product product, OnProductAddedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    product.setId(id);
                    Log.d(TAG, "Product added with ID: " + id);
                    listener.onProductAdded(id);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding product", e);
                    listener.onError(e.getMessage());
                });
    }

    // ==================== FAVORITES ====================

    /**
     * Save favorite product to Firestore
     */
    public void saveFavorite(String userId, String productId, OnFavoriteSavedListener listener) {
        java.util.Map<String, Object> favoriteData = new java.util.HashMap<>();
        favoriteData.put("userId", userId);
        favoriteData.put("productId", productId);
        favoriteData.put("addedAt", System.currentTimeMillis());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_FAVORITES)
                .document(productId)
                .set(favoriteData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Favorite saved successfully");
                    if (listener != null) {
                        listener.onFavoriteSaved();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving favorite", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Remove favorite product from Firestore
     */
    public void removeFavorite(String userId, String productId, OnFavoriteRemovedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_FAVORITES)
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Favorite removed successfully");
                    if (listener != null) {
                        listener.onFavoriteRemoved();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing favorite", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Load all favorite product IDs for a user
     */
    public void loadUserFavorites(String userId, OnFavoritesLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_FAVORITES)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> favoriteProductIds = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String productId = document.getString("productId");
                        if (productId != null) {
                            favoriteProductIds.add(productId);
                        }
                    }
                    Log.d(TAG, "Loaded " + favoriteProductIds.size() + " favorites");
                    listener.onFavoritesLoaded(favoriteProductIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading favorites", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load favorite products with full product details
     */
    public void loadFavoriteProducts(String userId, OnFavoriteProductsLoadedListener listener) {
        loadUserFavorites(userId, new OnFavoritesLoadedListener() {
            @Override
            public void onFavoritesLoaded(List<String> favoriteProductIds) {
                if (favoriteProductIds.isEmpty()) {
                    listener.onFavoriteProductsLoaded(new ArrayList<>());
                    return;
                }

                // Load full product details for each favorite
                List<Product> favoriteProducts = new ArrayList<>();
                int[] loadedCount = { 0 };

                for (String productId : favoriteProductIds) {
                    db.collection(COLLECTION_PRODUCTS)
                            .document(productId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Product product = documentSnapshot.toObject(Product.class);
                                    if (product != null) {
                                        product.setFavorite(true);
                                        favoriteProducts.add(product);
                                    }
                                }
                                loadedCount[0]++;
                                if (loadedCount[0] == favoriteProductIds.size()) {
                                    listener.onFavoriteProductsLoaded(favoriteProducts);
                                }
                            })
                            .addOnFailureListener(e -> {
                                loadedCount[0]++;
                                if (loadedCount[0] == favoriteProductIds.size()) {
                                    listener.onFavoriteProductsLoaded(favoriteProducts);
                                }
                            });
                }
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    /**
     * Check if a product is in user's favorites
     */
    public void checkIfFavorite(String userId, String productId, OnFavoriteCheckListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_FAVORITES)
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isFavorite = documentSnapshot.exists();
                    listener.onFavoriteChecked(isFavorite);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking favorite", e);
                    listener.onFavoriteChecked(false);
                });
    }

    // ==================== NOTIFICATIONS ====================

    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    /**
     * Load notifications for a specific user
     */
    public void loadNotifications(String userId, OnNotificationsLoadedListener listener) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.models.Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.models.Notification notification = document
                                .toObject(com.example.fashionstoreapp.models.Notification.class);
                        if (notification != null) {
                            notification.setId(document.getId());
                            // If timestamp wasn't set yet (serverTimestamp pending), use now as fallback
                            if (notification.getTimestamp() == null) {
                                java.util.Date ts = document.getDate("timestamp");
                                if (ts != null) {
                                    notification.setTimestamp(ts);
                                } else {
                                    notification.setTimestamp(new java.util.Date());
                                }
                            }
                            notifications.add(notification);
                        }
                    }

                    // Sort client-side by timestamp desc to avoid relying on server index/order
                    notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

                    Log.d(TAG, "Loaded " + notifications.size() + " notifications for user: " + userId);
                    listener.onNotificationsLoaded(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load all global notifications (for all users)
     */
    public void loadGlobalNotifications(OnNotificationsLoadedListener listener) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", "") // Empty userId means global notification
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.models.Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.models.Notification notification = document
                                .toObject(com.example.fashionstoreapp.models.Notification.class);
                        if (notification != null) {
                            notification.setId(document.getId());
                            if (notification.getTimestamp() == null) {
                                java.util.Date ts = document.getDate("timestamp");
                                if (ts != null) {
                                    notification.setTimestamp(ts);
                                } else {
                                    notification.setTimestamp(new java.util.Date());
                                }
                            }
                            notifications.add(notification);
                        }
                    }

                    // Sort client-side by timestamp desc
                    notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

                    Log.d(TAG, "Loaded " + notifications.size() + " global notifications");
                    listener.onNotificationsLoaded(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading global notifications", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get unread notifications count
     */
    public void getUnreadNotificationsCount(String userId, OnUnreadCountLoadedListener listener) {
        // Count user-specific unread notifications
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    int userCount = userSnapshot.size();
                    // Also count global (userId == "") unread notifications
                    db.collection(COLLECTION_NOTIFICATIONS)
                            .whereEqualTo("userId", "")
                            .whereEqualTo("isRead", false)
                            .get()
                            .addOnSuccessListener(globalSnapshot -> {
                                int globalCount = globalSnapshot.size();
                                int total = userCount + globalCount;
                                listener.onCountLoaded(total);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting global unread count", e);
                                // If global query fails, still return userCount
                                listener.onCountLoaded(userCount);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting unread count for user", e);
                    // Try falling back to counting only global unread notifications
                    db.collection(COLLECTION_NOTIFICATIONS)
                            .whereEqualTo("userId", "")
                            .whereEqualTo("isRead", false)
                            .get()
                            .addOnSuccessListener(globalSnapshot -> listener.onCountLoaded(globalSnapshot.size()))
                            .addOnFailureListener(err -> listener.onError(err.getMessage()));
                });
    }

    /**
     * Mark notification as read
     */
    public void markNotificationAsRead(String notificationId, OnNotificationUpdatedListener listener) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification marked as read: " + notificationId);
                    listener.onUpdated();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking notification as read", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId, OnNotificationDeletedListener listener) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification deleted: " + notificationId);
                    listener.onDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting notification", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Create a new notification (admin only)
     */
    public void createNotification(com.example.fashionstoreapp.models.Notification notification,
            OnNotificationCreatedListener listener) {
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("userId", notification.getUserId() == null ? "" : notification.getUserId());
            data.put("title", notification.getTitle());
            data.put("message", notification.getMessage());
            data.put("type", notification.getType());
            data.put("imageUrl", notification.getImageUrl());
            data.put("isRead", notification.isRead());
            data.put("actionUrl", notification.getActionUrl());
            data.put("orderId", notification.getOrderId());
            data.put("productId", notification.getProductId());
            // Use server timestamp to ensure consistent ordering/indexing
            data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection(COLLECTION_NOTIFICATIONS)
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        String notificationId = documentReference.getId();
                        Log.d(TAG, "Notification created (serverTimestamp): " + notificationId);
                        listener.onCreated(notificationId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating notification", e);
                        listener.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception creating notification", e);
            listener.onError(e.getMessage());
        }
    }

    // ==================== INTERFACES ====================

    /**
     * Load all categories from Firestore
     */
    public void loadCategories(OnCategoriesLoadedListener listener) {
        // Query Firestore field name 'active' (Category uses @PropertyName("active")
        // mapping)
        db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categories = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        if (category != null) {
                            category.setId(document.getId());
                            categories.add(category);
                        }
                    }

                    // Sort by displayOrder manually in Java
                    categories.sort((c1, c2) -> Integer.compare(c1.getDisplayOrder(), c2.getDisplayOrder()));

                    Log.d(TAG, "Loaded " + categories.size() + " categories");
                    listener.onCategoriesLoaded(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading categories", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Add a category to Firestore
     */
    public void addCategory(Category category, OnCategoryAddedListener listener) {
        db.collection(COLLECTION_CATEGORIES)
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    category.setId(id);
                    Log.d(TAG, "Category added with ID: " + id);
                    listener.onCategoryAdded(id);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding category", e);
                    listener.onError(e.getMessage());
                });
    }

    // ==================== REVIEWS ====================

    /**
     * Load reviews for a product
     */
    public void loadProductReviews(String productId, OnReviewsLoadedListener listener) {
        Log.d(TAG, "Loading reviews for product: " + productId);
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        if (review != null) {
                            review.setId(document.getId());
                            reviews.add(review);
                        }
                    }

                    // Sort by timestamp descending in Java
                    reviews.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

                    Log.d(TAG, "Loaded " + reviews.size() + " reviews for product: " + productId);
                    listener.onReviewsLoaded(reviews);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews: " + e.getMessage(), e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Add a review
     */
    public void addReview(Review review, OnReviewAddedListener listener) {
        db.collection(COLLECTION_REVIEWS)
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    String reviewId = documentReference.getId();
                    Log.d(TAG, "Review added with ID: " + reviewId);

                    // Update product rating
                    updateProductRating(review.getProductId());

                    listener.onReviewAdded(reviewId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding review", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Update product rating based on reviews
     */
    private void updateProductRating(String productId) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    double totalRating = 0;
                    int count = 0;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        if (review != null) {
                            totalRating += review.getRating();
                            count++;
                        }
                    }

                    final double averageRating = count > 0 ? totalRating / count : 0;
                    final int reviewCount = count;

                    // Update product
                    db.collection(COLLECTION_PRODUCTS)
                            .document(productId)
                            .update("rating", averageRating, "reviewCount", reviewCount)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG,
                                        "Product rating updated: " + averageRating + " (" + reviewCount + " reviews)");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating product rating", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating product rating", e);
                });
    }

    // ==================== USERS ====================

    /**
     * Save user profile to Firestore
     */
    public void saveUserProfile(String userId, String name, String birthday, String gender, String phone,
            OnUserProfileSavedListener listener) {
        java.util.Map<String, Object> profileData = new java.util.HashMap<>();
        profileData.put("name", name);
        profileData.put("birthday", birthday);
        profileData.put("gender", gender);
        profileData.put("phone", phone != null ? phone : ""); // Luôn lưu phone, cho phép rỗng
        profileData.put("updatedAt", System.currentTimeMillis());

        // Diagnostic: log current auth UID before write
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser current = auth.getCurrentUser();
        String currentUid = current != null ? current.getUid() : "null";
        Log.d(TAG,
                "Attempting to save user profile (merge). targetUserId=" + userId + ", currentAuthUid=" + currentUid);

        // redact any sensitive fields for logs
        java.util.Map<String, Object> safeLog = new java.util.HashMap<>(profileData);
        if (safeLog.containsKey("passwordHash")) {
            safeLog.put("passwordHash", "<redacted>");
        }
        Log.d(TAG, "Profile merge keys to write=" + safeLog.keySet());

        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(tt -> {
                boolean tokenPresent = tt.isSuccessful() && tt.getResult() != null && tt.getResult().getToken() != null;
                String token = tt.isSuccessful() && tt.getResult() != null ? tt.getResult().getToken() : null;
                int tokenLen = token != null ? token.length() : 0;
                Log.d(TAG, "Merge write - ID token present=" + tokenPresent + ", tokenLength=" + tokenLen
                        + ", currentUidEqualsTarget=" + current.getUid().equals(userId));

                db.collection(COLLECTION_USERS)
                        .document(userId)
                        .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User profile saved successfully");
                            listener.onProfileSaved();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG,
                                    "Error saving user profile (merge). targetUserId=" + userId + ", currentAuthUid="
                                            + currentUid
                                            + ", profileKeys=" + profileData.keySet(),
                                    e);
                            listener.onError(e.getMessage());
                        });
            }).addOnFailureListener(err -> {
                Log.e(TAG, "Failed to fetch ID token before merge write: " + err.getMessage(), err);
                // Try write anyway (will likely be denied)
                db.collection(COLLECTION_USERS)
                        .document(userId)
                        .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User profile saved successfully (token fetch failed path)");
                            listener.onProfileSaved();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG,
                                    "Error saving user profile (merge, token fetch failed). targetUserId=" + userId
                                            + ", profileKeys=" + profileData.keySet(),
                                    e);
                            listener.onError(e.getMessage());
                        });
            });
        } else {
            Log.w(TAG,
                    "No current FirebaseAuth user present before saving user profile (merge). targetUserId=" + userId);
            db.collection(COLLECTION_USERS)
                    .document(userId)
                    .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User profile saved successfully (no-auth fallback)");
                        listener.onProfileSaved();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG,
                                "Error saving user profile (merge, no-auth). targetUserId=" + userId + ", profileKeys="
                                        + profileData.keySet(),
                                e);
                        listener.onError(e.getMessage());
                    });
        }
    }

    /**
     * Save user profile with email to Firestore (for registration)
     */
    public void saveUserProfileWithEmail(String userId, String name, String email, String passwordHash, String birthday,
            String gender, String phone,
            OnUserProfileSavedListener listener) {
        java.util.Map<String, Object> profileData = new java.util.HashMap<>();
        profileData.put("name", name);
        profileData.put("email", email);
        profileData.put("passwordHash", passwordHash); // Lưu password đã hash SHA-256
        profileData.put("birthday", birthday);
        profileData.put("gender", gender);
        profileData.put("phone", phone != null ? phone : "");
        // Ensure default role and flags are present so Firestore rules that require
        // these fields on create won't reject the write.
        profileData.put("role", "user");
        profileData.put("isAdmin", false);
        profileData.put("isBlocked", false);
        profileData.put("createdAt", System.currentTimeMillis());
        profileData.put("updatedAt", System.currentTimeMillis());

        // Diagnostic: log current auth UID and token presence before attempting write
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser current = auth.getCurrentUser();
        String currentUid = current != null ? current.getUid() : "null";
        Log.d(TAG, "Attempting to save user profile (with email). targetUserId=" + userId + ", currentAuthUid="
                + currentUid);

        // Create a safe copy for logging (omit sensitive fields like passwordHash)
        java.util.Map<String, Object> safeLog = new java.util.HashMap<>(profileData);
        if (safeLog.containsKey("passwordHash")) {
            safeLog.put("passwordHash", "<redacted>");
        }
        Log.d(TAG, "Profile data keys to write=" + safeLog.keySet());

        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(tt -> {
                boolean tokenPresent = tt.isSuccessful() && tt.getResult() != null && tt.getResult().getToken() != null;
                String token = tt.isSuccessful() && tt.getResult() != null ? tt.getResult().getToken() : null;
                int tokenLen = token != null ? token.length() : 0;
                Log.d(TAG, "Current ID token present=" + tokenPresent + ", tokenLength=" + tokenLen
                        + ", currentUidEqualsTarget=" + current.getUid().equals(userId));

                // Now attempt write (we waited for a fresh token)
                db.collection(COLLECTION_USERS)
                        .document(userId)
                        .set(profileData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User profile with email saved successfully");
                            listener.onProfileSaved();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG,
                                    "Error saving user profile with email. targetUserId=" + userId
                                            + ", currentAuthUid=" + currentUid + ", profileKeys="
                                            + profileData.keySet(),
                                    e);
                            listener.onError(e.getMessage());
                        });
            }).addOnFailureListener(ttErr -> {
                Log.e(TAG, "Failed to fetch ID token before saving user profile: " + ttErr.getMessage(), ttErr);
                // Still attempt the write (will likely fail if token missing)
                db.collection(COLLECTION_USERS)
                        .document(userId)
                        .set(profileData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User profile with email saved successfully (token fetch failed path)");
                            listener.onProfileSaved();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG,
                                    "Error saving user profile with email (token fetch failed). targetUserId=" + userId
                                            + ", currentAuthUid=" + currentUid + ", profileKeys="
                                            + profileData.keySet(),
                                    e);
                            listener.onError(e.getMessage());
                        });
            });
        } else {
            // No authenticated user present; log and attempt write (will likely be denied
            // by rules)
            Log.w(TAG, "No current FirebaseAuth user present before saving user profile (targetUserId=" + userId
                    + ")");
            db.collection(COLLECTION_USERS)
                    .document(userId)
                    .set(profileData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User profile with email saved successfully (no-auth fallback)");
                        listener.onProfileSaved();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG,
                                "Error saving user profile with email (no-auth). targetUserId=" + userId
                                        + ", profileKeys="
                                        + profileData.keySet(),
                                e);
                        listener.onError(e.getMessage());
                    });
        }
    }

    /**
     * Update user photo URL in Firestore
     */
    public void updateUserPhotoUrl(String userId, String photoUrl) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("photoUrl", photoUrl);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Photo URL updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating photo URL", e));
    }

    /**
     * Load user profile from Firestore
     */
    public void loadUserProfile(String userId, OnUserProfileLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String birthday = documentSnapshot.getString("birthday");
                        String gender = documentSnapshot.getString("gender");
                        String phone = documentSnapshot.getString("phone");
                        Log.d(TAG, "User profile loaded successfully");
                        listener.onProfileLoaded(name, birthday, gender, phone);
                    } else {
                        Log.d(TAG, "User profile not found");
                        listener.onProfileLoaded(null, null, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user profile", e);
                    listener.onError(e.getMessage());
                });
    }

    // ==================== CART ====================

    /**
     * Save cart items to Firestore
     */
    public void saveCartItems(String userId, List<com.example.fashionstoreapp.models.CartItem> cartItems,
            OnCartSavedListener listener) {
        // Convert cart items to map format
        List<java.util.Map<String, Object>> cartData = new ArrayList<>();
        for (com.example.fashionstoreapp.models.CartItem item : cartItems) {
            java.util.Map<String, Object> itemData = new java.util.HashMap<>();
            itemData.put("productId", item.getProduct().getId());
            itemData.put("productName", item.getProduct().getName());
            itemData.put("productImage", item.getProduct().getImageUrl());
            itemData.put("productPrice", item.getProduct().getCurrentPrice());
            itemData.put("quantity", item.getQuantity());
            itemData.put("size", item.getSize());
            itemData.put("color", item.getColor());
            itemData.put("isSelected", item.isSelected());
            cartData.add(itemData);
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("items", cartData);
        data.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_CARTS)
                .document(userId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cart saved successfully");
                    listener.onCartSaved();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving cart", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load cart items from Firestore
     */
    public void loadCartItems(String userId, OnCartLoadedListener listener) {
        db.collection(COLLECTION_CARTS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<java.util.Map<String, Object>> cartData = (List<java.util.Map<String, Object>>) documentSnapshot
                                .get("items");

                        if (cartData != null) {
                            List<String> productIds = new ArrayList<>();
                            for (java.util.Map<String, Object> itemData : cartData) {
                                String productId = (String) itemData.get("productId");
                                if (productId != null) {
                                    productIds.add(productId);
                                }
                            }

                            // Load full product details
                            loadProductsByIds(productIds, new OnProductsLoadedListener() {
                                @Override
                                public void onProductsLoaded(List<Product> products) {
                                    // Create cart items with full product data
                                    List<com.example.fashionstoreapp.models.CartItem> cartItems = new ArrayList<>();
                                    for (java.util.Map<String, Object> itemData : cartData) {
                                        String productId = (String) itemData.get("productId");
                                        Product product = findProductById(products, productId);

                                        if (product != null) {
                                            com.example.fashionstoreapp.models.CartItem cartItem = new com.example.fashionstoreapp.models.CartItem();
                                            cartItem.setProduct(product);
                                            cartItem.setQuantity(((Long) itemData.get("quantity")).intValue());
                                            cartItem.setSize((String) itemData.get("size"));
                                            cartItem.setColor((String) itemData.get("color"));
                                            cartItem.setSelected((Boolean) itemData.get("isSelected"));
                                            cartItems.add(cartItem);
                                        }
                                    }
                                    Log.d(TAG, "Cart loaded successfully: " + cartItems.size() + " items");
                                    listener.onCartLoaded(cartItems);
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Error loading products for cart", new Exception(error));
                                    listener.onError(error);
                                }
                            });
                        } else {
                            listener.onCartLoaded(new ArrayList<>());
                        }
                    } else {
                        Log.d(TAG, "Cart not found");
                        listener.onCartLoaded(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load products by IDs
     */
    private void loadProductsByIds(List<String> productIds, OnProductsLoadedListener listener) {
        if (productIds.isEmpty()) {
            listener.onProductsLoaded(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .whereIn("__name__", productIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            products.add(product);
                        }
                    }
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products by IDs", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Find product by ID in list
     */
    private Product findProductById(List<Product> products, String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Clear cart in Firestore
     */
    public void clearCart(String userId, OnCartSavedListener listener) {
        db.collection(COLLECTION_CARTS)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cart cleared successfully");
                    listener.onCartSaved();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error clearing cart", e);
                    listener.onError(e.getMessage());
                });
    }

    // ==================== CALLBACKS ====================

    public interface OnProductsLoadedListener {
        void onProductsLoaded(List<Product> products);

        void onError(String error);
    }

    public interface OnProductAddedListener {
        void onProductAdded(String productId);

        void onError(String error);
    }

    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<Category> categories);

        void onError(String error);
    }

    public interface OnCategoryAddedListener {
        void onCategoryAdded(String categoryId);

        void onError(String error);
    }

    public interface OnReviewsLoadedListener {
        void onReviewsLoaded(List<Review> reviews);

        void onError(String error);
    }

    public interface OnReviewAddedListener {
        void onReviewAdded(String reviewId);

        void onError(String error);
    }

    public interface OnUserProfileSavedListener {
        void onProfileSaved();

        void onError(String error);
    }

    public interface OnUserProfileLoadedListener {
        void onProfileLoaded(String name, String birthday, String gender, String phone);

        void onError(String error);
    }

    public interface OnCartSavedListener {
        void onCartSaved();

        void onError(String error);
    }

    public interface OnCartLoadedListener {
        void onCartLoaded(List<com.example.fashionstoreapp.models.CartItem> cartItems);

        void onError(String error);
    }

    public interface OnFavoriteSavedListener {
        void onFavoriteSaved();

        void onError(String error);
    }

    public interface OnFavoriteRemovedListener {
        void onFavoriteRemoved();

        void onError(String error);
    }

    public interface OnFavoritesLoadedListener {
        void onFavoritesLoaded(List<String> favoriteProductIds);

        void onError(String error);
    }

    public interface OnFavoriteProductsLoadedListener {
        void onFavoriteProductsLoaded(List<Product> favoriteProducts);

        void onError(String error);
    }

    public interface OnFavoriteCheckListener {
        void onFavoriteChecked(boolean isFavorite);
    }

    // ==================== ORDERS ====================

    /**
     * Save order to Firestore
     */
    public void saveOrder(com.example.fashionstoreapp.models.Order order, OnOrderSavedListener listener) {
        java.util.Map<String, Object> orderData = new java.util.HashMap<>();
        orderData.put("orderId", order.getOrderId());
        orderData.put("userId", order.getUserId());
        orderData.put("totalAmount", order.getTotalAmount());
        orderData.put("subtotal", order.getSubtotal());
        orderData.put("shippingFee", order.getShippingFee());
        orderData.put("voucherDiscount", order.getVoucherDiscount());
        orderData.put("status", order.getStatus());
        orderData.put("paymentMethod", order.getPaymentMethod());
        orderData.put("recipientName", order.getRecipientName());
        orderData.put("recipientPhone", order.getRecipientPhone());
        orderData.put("shippingAddress", order.getShippingAddress());
        orderData.put("note", order.getNote());
        orderData.put("createdAt", order.getCreatedAt());
        orderData.put("updatedAt", order.getUpdatedAt());

        // Convert order items to map format
        List<java.util.Map<String, Object>> itemsData = new ArrayList<>();
        for (com.example.fashionstoreapp.models.CartItem item : order.getItems()) {
            java.util.Map<String, Object> itemData = new java.util.HashMap<>();
            itemData.put("productId", item.getProduct().getId());
            itemData.put("productName", item.getProduct().getName());
            itemData.put("productImage", item.getProduct().getImageUrl());
            itemData.put("productPrice", item.getProduct().getCurrentPrice());
            itemData.put("quantity", item.getQuantity());
            itemData.put("size", item.getSize());
            itemData.put("color", item.getColor());
            itemData.put("totalPrice", item.getTotalPrice());
            itemsData.add(itemData);
        }
        orderData.put("items", itemsData);

        db.collection(COLLECTION_ORDERS)
                .document(order.getOrderId())
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order saved successfully: " + order.getOrderId());
                    listener.onOrderSaved(order.getOrderId());
                    // Create a user notification for order placed
                    try {
                        com.example.fashionstoreapp.models.Notification notif = new com.example.fashionstoreapp.models.Notification();
                        notif.setUserId(order.getUserId());
                        notif.setType("order");
                        notif.setOrderId(order.getOrderId());
                        notif.setTitle("Đặt hàng thành công");
                        notif.setMessage("Đơn hàng " + order.getOrderId() + " của bạn đã được tạo. Tổng: "
                                + String.format("%,.0f₫", order.getTotalAmount()));
                        notif.setTimestamp(new java.util.Date());
                        notif.setRead(false);
                        // Use existing createNotification helper
                        createNotification(notif, new OnNotificationCreatedListener() {
                            @Override
                            public void onCreated(String notificationId) {
                                Log.d(TAG, "Order notification created: " + notificationId);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Failed to create order notification: " + error);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating notification for order", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving order", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Increment totalSold for products in given cart items (client-side quick
     * update).
     */
    public void incrementProductsSold(List<com.example.fashionstoreapp.models.CartItem> items) {
        if (items == null || items.isEmpty())
            return;

        com.google.firebase.firestore.WriteBatch batch = db.batch();
        int ops = 0;
        for (com.example.fashionstoreapp.models.CartItem item : items) {
            if (item == null || item.getProduct() == null)
                continue;
            String productId = item.getProduct().getId();
            if (productId == null || productId.isEmpty())
                continue;
            com.google.firebase.firestore.DocumentReference prodRef = db.collection(COLLECTION_PRODUCTS)
                    .document(productId);
            batch.update(prodRef, "totalSold", com.google.firebase.firestore.FieldValue.increment(item.getQuantity()));
            ops++;
            // Firestore batch supports up to 500 operations; commit periodically if needed
            if (ops >= 450) {
                try {
                    batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Batch increment committed"))
                            .addOnFailureListener(e -> Log.e(TAG, "Batch increment failed: " + e.getMessage()));
                } catch (Exception e) {
                    Log.e(TAG, "Exception committing batch: " + e.getMessage());
                }
                // start a new batch
                ops = 0;
                batch = db.batch();
            }
        }

        // commit remaining
        try {
            batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Incremented totalSold for products"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to increment totalSold: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Exception committing final batch: " + e.getMessage());
        }
    }

    /**
     * Load orders from Firestore for a specific user
     */
    public void loadOrders(String userId, OnOrdersLoadedListener listener) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.model.Order> orders = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        com.example.fashionstoreapp.model.Order order = new com.example.fashionstoreapp.model.Order();
                        order.setOrderId(document.getString("orderId"));
                        order.setUserId(document.getString("userId"));

                        // Fix: Read totalAmount (try multiple field names for compatibility)
                        Double totalAmount = document.getDouble("totalAmount");
                        if (totalAmount == null) {
                            totalAmount = document.getDouble("total");
                        }
                        if (totalAmount == null) {
                            // Try parsing from String if stored incorrectly
                            String totalStr = document.getString("totalAmount");
                            if (totalStr != null) {
                                try {
                                    totalAmount = Double.parseDouble(totalStr.replaceAll("[^0-9.]", ""));
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to parse totalAmount string: " + totalStr, e);
                                }
                            }
                        }

                        // CRITICAL: Always set total, even if 0
                        double finalTotal = (totalAmount != null) ? totalAmount : 0.0;
                        order.setTotal(finalTotal);
                        Log.d(TAG, "Order " + order.getOrderId() + " - Setting total to: " + finalTotal
                                + " (from totalAmount: " + totalAmount + ")");

                        // Check if this is a new order with breakdown fields
                        Double subtotal = document.getDouble("subtotal");
                        Double shippingFee = document.getDouble("shippingFee");
                        Double voucherDiscount = document.getDouble("voucherDiscount");

                        if (subtotal != null) {
                            // New order format with breakdown
                            order.setSubtotal(subtotal);
                            order.setShippingFee(shippingFee != null ? shippingFee : 0);
                            order.setVoucherDiscount(voucherDiscount != null ? voucherDiscount : 0);
                            Log.d(TAG, "Order " + order.getOrderId() + " - New format: subtotal=" + subtotal +
                                    ", shippingFee=" + (shippingFee != null ? shippingFee : 0) +
                                    ", voucherDiscount=" + (voucherDiscount != null ? voucherDiscount : 0));
                        } else if (totalAmount != null) {
                            // Old order format - calculate breakdown from totalAmount
                            // Get items first to calculate
                            List<java.util.Map<String, Object>> oldOrderItems = (List<java.util.Map<String, Object>>) document
                                    .get("items");
                            double itemsTotal = 0;

                            if (oldOrderItems != null) {
                                for (java.util.Map<String, Object> itemData : oldOrderItems) {
                                    Object priceObj = itemData.get("productPrice");
                                    if (priceObj == null) {
                                        priceObj = itemData.get("price");
                                    }
                                    Object quantityObj = itemData.get("quantity");
                                    if (priceObj != null && quantityObj != null) {
                                        double price = ((Number) priceObj).doubleValue();
                                        int quantity = ((Number) quantityObj).intValue();
                                        itemsTotal += price * quantity;
                                    }
                                }
                            }

                            // Calculate shipping fee (free if > 500k, else 30k)
                            double calculatedShipping = itemsTotal >= 500000 ? 0 : 30000;

                            order.setSubtotal(itemsTotal);
                            order.setShippingFee(calculatedShipping);
                            order.setVoucherDiscount(0);

                            Log.d(TAG,
                                    "Order " + order.getOrderId() + " - Old format calculated: itemsTotal=" + itemsTotal
                                            +
                                            ", calculatedShipping=" + calculatedShipping);
                        } else {
                            // No data available, set defaults
                            order.setSubtotal(0);
                            order.setShippingFee(0);
                            order.setVoucherDiscount(0);
                            Log.w(TAG, "Order " + order.getOrderId() + " has no breakdown data");
                        }

                        order.setStatus(document.getString("status"));
                        order.setPaymentMethod(document.getString("paymentMethod"));
                        order.setShippingAddress(document.getString("shippingAddress"));
                        order.setRecipientName(document.getString("recipientName"));
                        order.setPhoneNumber(document.getString("recipientPhone"));

                        // Set timestamp directly as Long
                        Long createdAtLong = document.getLong("createdAt");
                        if (createdAtLong != null) {
                            order.setCreatedAt(createdAtLong);
                        }

                        // Load order items
                        List<java.util.Map<String, Object>> itemsData = (List<java.util.Map<String, Object>>) document
                                .get("items");
                        if (itemsData != null) {
                            List<com.example.fashionstoreapp.model.Order.OrderItem> items = new ArrayList<>();
                            for (java.util.Map<String, Object> itemData : itemsData) {
                                com.example.fashionstoreapp.model.Order.OrderItem item = new com.example.fashionstoreapp.model.Order.OrderItem();
                                item.setProductId((String) itemData.get("productId"));
                                item.setProductName((String) itemData.get("productName"));

                                // Fix: Get productImage from items
                                String imageUrl = (String) itemData.get("productImage");
                                if (imageUrl == null) {
                                    imageUrl = (String) itemData.get("imageUrl");
                                }
                                item.setImageUrl(imageUrl);
                                Log.d(TAG, "Order item: " + itemData.get("productName") + " imageUrl: " + imageUrl);

                                Object priceObj = itemData.get("productPrice");
                                if (priceObj == null) {
                                    priceObj = itemData.get("price");
                                }
                                if (priceObj != null) {
                                    item.setPrice(((Number) priceObj).doubleValue());
                                }

                                Object quantityObj = itemData.get("quantity");
                                if (quantityObj != null) {
                                    item.setQuantity(((Number) quantityObj).intValue());
                                }

                                item.setSize((String) itemData.get("size"));
                                item.setColor((String) itemData.get("color"));
                                items.add(item);
                            }
                            order.setItems(items);
                        }

                        orders.add(order);
                    }
                    Log.d(TAG, "Loaded " + orders.size() + " orders");
                    listener.onOrdersLoaded(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading orders", e);
                    listener.onError(e.getMessage());
                });
    }

    public interface OnOrderSavedListener {
        void onOrderSaved(String orderId);

        void onError(String error);
    }

    public interface OnOrdersLoadedListener {
        void onOrdersLoaded(List<com.example.fashionstoreapp.model.Order> orders);

        void onError(String error);
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Get all products (Admin)
     */
    public void getAllProducts(OnProductsLoadedListener listener) {
        loadProducts(listener);
    }

    /**
     * Get all orders (Admin)
     */
    public void getAllOrders(OnOrdersLoadedListener listener) {
        db.collection(COLLECTION_ORDERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.model.Order> orders = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.model.Order order = new com.example.fashionstoreapp.model.Order();
                        order.setOrderId(document.getId());
                        order.setUserId(document.getString("userId"));

                        // Read total from whichever field exists: prefer numeric 'total' or
                        // 'totalAmount'
                        Double totalDouble = null;
                        Object totalObj = document.get("total");
                        if (totalObj instanceof Number) {
                            totalDouble = ((Number) totalObj).doubleValue();
                        } else if (totalObj instanceof String) {
                            try {
                                totalDouble = Double.parseDouble(((String) totalObj).replaceAll("[^0-9.]", ""));
                            } catch (Exception ignored) {
                            }
                        }

                        if (totalDouble == null) {
                            // try alternative field name used elsewhere
                            Double t = document.getDouble("totalAmount");
                            if (t != null)
                                totalDouble = t;
                        }

                        if (totalDouble == null) {
                            // last resort: try 'total' as double
                            Double t2 = document.getDouble("total");
                            if (t2 != null)
                                totalDouble = t2;
                        }

                        order.setTotal(totalDouble != null ? totalDouble : 0.0);

                        order.setStatus(document.getString("status"));
                        order.setPaymentMethod(document.getString("paymentMethod"));

                        Long createdAtLong = document.getLong("createdAt");
                        if (createdAtLong != null) {
                            order.setCreatedAt(createdAtLong);
                        }

                        orders.add(order);
                    }
                    Log.d(TAG, "Admin loaded " + orders.size() + " orders");
                    listener.onOrdersLoaded(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading all orders", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get all users (Admin)
     */
    public void getAllUsers(OnUsersLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.models.User> users = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.models.User user = new com.example.fashionstoreapp.models.User();
                        user.setId(document.getId());
                        user.setEmail(document.getString("email"));
                        user.setName(document.getString("name"));
                        user.setPhone(document.getString("phone"));
                        user.setRole(document.getString("role"));
                        user.setProfileImageUrl(document.getString("profileImageUrl"));

                        // isActive flag (may be absent for legacy users)
                        Boolean isActive = document.getBoolean("isActive");
                        if (isActive != null) {
                            user.setActive(isActive);
                        } else {
                            user.setActive(true);
                        }

                        Long createdAt = document.getLong("createdAt");
                        if (createdAt != null) {
                            user.setCreatedAt(createdAt);
                        }

                        users.add(user);
                    }
                    Log.d(TAG, "Admin loaded " + users.size() + " users");
                    listener.onUsersLoaded(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading all users", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Update order status (Admin)
     */
    public void updateOrderStatus(String orderId, String newStatus, String cancelReason,
            OnOrderStatusUpdatedListener listener) {
        // First get the order to check payment method and update revenue if needed
        db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String paymentMethod = documentSnapshot.getString("paymentMethod");
                        // Read total amount robustly: support 'total', 'totalAmount', or string formats
                        Double totalAmount = null;
                        Object totalObj = documentSnapshot.get("total");
                        if (totalObj instanceof Number) {
                            totalAmount = ((Number) totalObj).doubleValue();
                        } else if (totalObj instanceof String) {
                            try {
                                totalAmount = Double.parseDouble(((String) totalObj).replaceAll("[^0-9.]", ""));
                            } catch (Exception ignored) {
                            }
                        }
                        if (totalAmount == null) {
                            Double ta = documentSnapshot.getDouble("totalAmount");
                            if (ta != null)
                                totalAmount = ta;
                        }

                        // Make final copies for use inside inner lambdas
                        final String paymentMethodFinal = paymentMethod;
                        final Double totalAmountFinal = totalAmount;

                        // Update the order status (and optional cancel reason)
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("status", newStatus);
                        if (cancelReason != null && !cancelReason.trim().isEmpty()
                                && ("cancelled".equals(newStatus) || "canceled".equals(newStatus))) {
                            updates.put("cancelReason", cancelReason.trim());
                        } else if (!"cancelled".equals(newStatus) && !"canceled".equals(newStatus)) {
                            // clear previous cancel reason when moving away from cancelled
                            updates.put("cancelReason", null);
                        }

                        db.collection(COLLECTION_ORDERS)
                                .document(orderId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Order status updated: " + orderId + " -> " + newStatus);

                                    // Diagnostic: log payment method and total before revenue update decision
                                    Log.d(TAG, "Order update diagnostics: orderId=" + orderId
                                            + ", paymentMethod=" + paymentMethodFinal
                                            + ", totalAmount=" + totalAmountFinal);

                                    // If order is delivered and payment method is COD, update revenue
                                    if ("delivered".equals(newStatus)) {
                                        boolean isCod = paymentMethodFinal != null
                                                && paymentMethodFinal.toLowerCase().contains("nhận hàng")
                                                || (paymentMethodFinal != null
                                                        && paymentMethodFinal.toLowerCase().contains("cod"))
                                                || (paymentMethodFinal != null
                                                        && paymentMethodFinal.toLowerCase().contains("cash"));
                                        if (isCod && totalAmountFinal != null) {
                                            Log.d(TAG, "Invoking updateRevenue for order " + orderId + " amount="
                                                    + totalAmountFinal);
                                            updateRevenue(totalAmountFinal);
                                        } else {
                                            Log.d(TAG,
                                                    "Skipping revenue update for order " + orderId + ", isCod=" + isCod
                                                            + ", totalAmountPresent=" + (totalAmountFinal != null));
                                        }
                                    }

                                    // Create a notification for the user about status change
                                    try {
                                        String userId = documentSnapshot.getString("userId");
                                        if (userId != null) {
                                            com.example.fashionstoreapp.models.Notification notif = new com.example.fashionstoreapp.models.Notification();
                                            notif.setUserId(userId);
                                            notif.setType("order");
                                            notif.setOrderId(orderId);
                                            notif.setTitle("Cập nhật đơn hàng");
                                            String statusText = newStatus;
                                            // Provide friendly Vietnamese status labels for common statuses
                                            if ("pending".equals(newStatus))
                                                statusText = "Đang chờ xác nhận";
                                            else if ("confirmed".equals(newStatus))
                                                statusText = "Đã xác nhận";
                                            else if ("shipping".equals(newStatus) || "shipped".equals(newStatus))
                                                statusText = "Đang giao";
                                            else if ("delivered".equals(newStatus))
                                                statusText = "Đã giao";
                                            else if ("cancelled".equals(newStatus) || "canceled".equals(newStatus))
                                                statusText = "Đã hủy";

                                            String message = "Đơn hàng " + orderId + " đã chuyển sang trạng thái: "
                                                    + statusText;
                                            if (("cancelled".equals(newStatus) || "canceled".equals(newStatus))
                                                    && cancelReason != null && !cancelReason.trim().isEmpty()) {
                                                message += "\nLý do: " + cancelReason.trim();
                                            }
                                            notif.setMessage(message);
                                            notif.setTimestamp(new java.util.Date());
                                            notif.setRead(false);

                                            createNotification(notif, new OnNotificationCreatedListener() {
                                                @Override
                                                public void onCreated(String notificationId) {
                                                    Log.d(TAG, "Order status notification created: " + notificationId);
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Log.e(TAG, "Failed to create order status notification: " + error);
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error creating status-change notification", e);
                                    }

                                    listener.onStatusUpdated();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating order status", e);
                                    listener.onError(e.getMessage());
                                });
                    } else {
                        listener.onError("Order not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading order for status update", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Update revenue counter (for COD orders when delivered)
     */
    private void updateRevenue(double amount) {
        long todayStart = getTodayStartTimestamp();
        String dateKey = String.valueOf(todayStart);

        Log.d(TAG, "updateRevenue called. dateKey=" + dateKey + ", amount=" + amount);

        db.collection("revenue")
                .document(dateKey)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    double currentRevenue = 0;
                    if (documentSnapshot.exists()) {
                        Double revenue = documentSnapshot.getDouble("amount");
                        if (revenue != null) {
                            currentRevenue = revenue;
                        }
                    }

                    double newRevenue = currentRevenue + amount;
                    Log.d(TAG, "Current revenue=" + currentRevenue + ", newRevenue=" + newRevenue + ", dateKey="
                            + dateKey);

                    db.collection("revenue")
                            .document(dateKey)
                            .set(new java.util.HashMap<String, Object>() {
                                {
                                    put("amount", newRevenue);
                                    put("date", todayStart);
                                }
                            })
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Revenue updated: " + newRevenue))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating revenue", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading revenue", e));
    }

    /**
     * Update user role (Admin)
     */
    public void updateUserRole(String userId, String newRole, OnUserRoleUpdatedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User role updated: " + userId + " -> " + newRole);
                    listener.onRoleUpdated();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user role", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Disable or enable a user account (set isActive flag)
     */
    public void setUserActive(String userId, boolean isActive, OnUserRoleUpdatedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .update("isActive", isActive)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User active flag updated: " + userId + " -> " + isActive);
                    listener.onRoleUpdated();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user active flag", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Delete a user document from Firestore. Note: this does NOT delete the
     * Firebase Auth user.
     */
    public void deleteUser(String userId, OnProductDeletedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted: " + userId);
                    listener.onProductDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Delete product (Admin)
     */
    public void deleteProduct(String productId, OnProductDeletedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product deleted: " + productId);
                    listener.onProductDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting product", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get user role
     */
    public void getUserRole(String userId, OnUserRoleLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role == null || role.isEmpty()) {
                            role = "user"; // Default role
                        }
                        listener.onRoleLoaded(role);
                    } else {
                        listener.onRoleLoaded("user"); // Default for new users
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user role", e);
                    listener.onError(e.getMessage());
                });
    }

    // ==================== ENHANCED ADMIN METHODS ====================

    /**
     * Get today's revenue
     */
    public void getTodayRevenue(OnRevenueLoadedListener listener) {
        long todayStart = getTodayStartTimestamp();
        long todayEnd = System.currentTimeMillis();

        db.collection(COLLECTION_ORDERS)
                .whereGreaterThanOrEqualTo("createdAt", todayStart)
                .whereLessThanOrEqualTo("createdAt", todayEnd)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Support different field names/formats: 'total' (number), 'totalAmount', or
                        // numeric string
                        Double total = null;
                        Object to = doc.get("total");
                        if (to instanceof Number) {
                            total = ((Number) to).doubleValue();
                        } else if (to instanceof String) {
                            try {
                                total = Double.parseDouble(((String) to).replaceAll("[^0-9.]", ""));
                            } catch (Exception ignored) {
                            }
                        }
                        if (total == null) {
                            Double ta = doc.getDouble("totalAmount");
                            if (ta != null)
                                total = ta;
                        }
                        if (total != null) {
                            totalRevenue += total;
                        }
                    }
                    Log.d(TAG, "Today's revenue: " + totalRevenue);
                    listener.onRevenueLoaded(totalRevenue);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading today's revenue", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get today's orders count
     */
    public void getTodayOrders(OnTodayOrdersLoadedListener listener) {
        long todayStart = getTodayStartTimestamp();
        long todayEnd = System.currentTimeMillis();

        db.collection(COLLECTION_ORDERS)
                .whereGreaterThanOrEqualTo("createdAt", todayStart)
                .whereLessThanOrEqualTo("createdAt", todayEnd)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Log.d(TAG, "Today's orders: " + count);
                    listener.onOrdersLoaded(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading today's orders", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get today's new users count
     */
    public void getTodayNewUsers(OnTodayUsersLoadedListener listener) {
        long todayStart = getTodayStartTimestamp();
        long todayEnd = System.currentTimeMillis();

        db.collection(COLLECTION_USERS)
                .whereGreaterThanOrEqualTo("createdAt", todayStart)
                .whereLessThanOrEqualTo("createdAt", todayEnd)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Log.d(TAG, "Today's new users: " + count);
                    listener.onUsersLoaded(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading today's new users", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get low stock products
     */
    public void getLowStockProducts(OnProductsLoadedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> lowStockProducts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            // Check if product is low stock or out of stock
                            if (product.isLowStock() || product.isOutOfStock()) {
                                lowStockProducts.add(product);
                            }
                        }
                    }
                    Log.d(TAG, "Low stock products: " + lowStockProducts.size());
                    listener.onProductsLoaded(lowStockProducts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading low stock products", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get pending orders (waiting for confirmation)
     */
    public void getPendingOrders(OnOrdersLoadedListener listener) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.model.Order> orders = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.model.Order order = document
                                .toObject(com.example.fashionstoreapp.model.Order.class);
                        if (order != null) {
                            order.setOrderId(document.getId());
                            orders.add(order);
                        }
                    }
                    Log.d(TAG, "Pending orders: " + orders.size());
                    listener.onOrdersLoaded(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pending orders", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get top selling products
     */
    public void getTopSellingProducts(int limit, OnProductsLoadedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .orderBy("totalSold", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            products.add(product);
                        }
                    }
                    Log.d(TAG, "Top selling products: " + products.size());
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading top selling products", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Update product stock for a specific size
     */
    public void updateProductStock(String productId, String size, int newStock, OnStockUpdatedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Product product = documentSnapshot.toObject(Product.class);
                    if (product != null) {
                        product.updateStockForSize(size, newStock);

                        // Update in Firestore
                        db.collection(COLLECTION_PRODUCTS)
                                .document(productId)
                                .update("sizeStocks", product.getSizeStocks())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Stock updated for product: " + productId);
                                    listener.onStockUpdated();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating stock", e);
                                    listener.onError(e.getMessage());
                                });
                    } else {
                        listener.onError("Product not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading product for stock update", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Toggle product visibility
     */
    public void toggleProductVisibility(String productId, boolean isVisible, OnVisibilityToggledListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .update("isVisible", isVisible)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product visibility toggled: " + productId + " -> " + isVisible);
                    listener.onVisibilityToggled();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error toggling product visibility", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Add or update product (Admin)
     */
    public void saveProduct(Product product, OnProductSavedListener listener) {
        String productId = product.getId();
        if (productId == null || productId.isEmpty()) {
            // Create new product
            productId = db.collection(COLLECTION_PRODUCTS).document().getId();
            product.setId(productId);
        }

        final String finalProductId = productId; // Make final for lambda

        db.collection(COLLECTION_PRODUCTS)
                .document(finalProductId)
                .set(product, com.google.firebase.firestore.SetOptions.merge()) // Use merge to preserve rating,
                                                                                // reviews, etc.
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product saved: " + finalProductId);
                    listener.onProductSaved(finalProductId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving product", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Add or update category (Admin)
     */
    public void saveCategory(Category category, OnCategorySavedListener listener) {
        String categoryId = category.getId();
        if (categoryId == null || categoryId.isEmpty()) {
            // Create new category
            categoryId = db.collection(COLLECTION_CATEGORIES).document().getId();
            category.setId(categoryId);
        }

        final String finalCategoryId = categoryId; // Make final for lambda

        db.collection(COLLECTION_CATEGORIES)
                .document(finalCategoryId)
                .set(category, com.google.firebase.firestore.SetOptions.merge()) // Use merge to preserve other fields
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Category saved: " + finalCategoryId);
                    listener.onCategorySaved(finalCategoryId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving category", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Delete category (Admin)
     */
    public void deleteCategory(String categoryId, OnCategoryDeletedListener listener) {
        db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Category deleted: " + categoryId);
                    listener.onCategoryDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting category", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Helper: Get today's start timestamp (00:00:00)
     */
    private long getTodayStartTimestamp() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // ==================== ADMIN LISTENERS ====================

    public interface OnUsersLoadedListener {
        void onUsersLoaded(List<com.example.fashionstoreapp.models.User> users);

        void onError(String error);
    }

    public interface OnOrderStatusUpdatedListener {
        void onStatusUpdated();

        void onError(String error);
    }

    public interface OnUserRoleUpdatedListener {
        void onRoleUpdated();

        void onError(String error);
    }

    public interface OnProductDeletedListener {
        void onProductDeleted();

        void onError(String error);
    }

    public interface OnUserRoleLoadedListener {
        void onRoleLoaded(String role);

        void onError(String error);
    }

    // ==================== ENHANCED ADMIN LISTENERS ====================

    public interface OnRevenueLoadedListener {
        void onRevenueLoaded(double revenue);

        void onError(String error);
    }

    public interface OnTodayOrdersLoadedListener {
        void onOrdersLoaded(int count);

        void onError(String error);
    }

    public interface OnTodayUsersLoadedListener {
        void onUsersLoaded(int count);

        void onError(String error);
    }

    public interface OnStockUpdatedListener {
        void onStockUpdated();

        void onError(String error);
    }

    public interface OnVisibilityToggledListener {
        void onVisibilityToggled();

        void onError(String error);
    }

    public interface OnProductSavedListener {
        void onProductSaved(String productId);

        void onError(String error);
    }

    public interface OnCategorySavedListener {
        void onCategorySaved(String categoryId);

        void onError(String error);
    }

    public interface OnCategoryDeletedListener {
        void onCategoryDeleted();

        void onError(String error);
    }

    // ==================== NOTIFICATION LISTENERS ====================

    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<com.example.fashionstoreapp.models.Notification> notifications);

        void onError(String error);
    }

    public interface OnUnreadCountLoadedListener {
        void onCountLoaded(int count);

        void onError(String error);
    }

    public interface OnNotificationUpdatedListener {
        void onUpdated();

        void onError(String error);
    }

    public interface OnNotificationDeletedListener {
        void onDeleted();

        void onError(String error);
    }

    public interface OnNotificationCreatedListener {
        void onCreated(String notificationId);

        void onError(String error);
    }

    // ==================== VOUCHER MANAGEMENT ====================

    /**
     * Load all vouchers (Admin)
     */
    public void getAllVouchers(OnVouchersLoadedListener listener) {
        db.collection(COLLECTION_VOUCHERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.fashionstoreapp.models.Voucher> vouchers = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.models.Voucher voucher = document
                                .toObject(com.example.fashionstoreapp.models.Voucher.class);
                        if (voucher != null) {
                            voucher.setId(document.getId());
                            vouchers.add(voucher);
                        }
                    }
                    Log.d(TAG, "Loaded " + vouchers.size() + " vouchers");
                    listener.onVouchersLoaded(vouchers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading vouchers", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Create a new voucher (Admin)
     */
    public void createVoucher(com.example.fashionstoreapp.models.Voucher voucher, OnVoucherSavedListener listener) {
        // Check if code already exists
        db.collection(COLLECTION_VOUCHERS)
                .whereEqualTo("code", voucher.getCode())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listener.onError("Mã voucher đã tồn tại");
                        return;
                    }

                    // Create new voucher
                    long now = System.currentTimeMillis();
                    voucher.setCreatedAt(now);
                    voucher.setUpdatedAt(now);
                    voucher.setUsedCount(0);

                    db.collection(COLLECTION_VOUCHERS)
                            .add(voucher)
                            .addOnSuccessListener(documentReference -> {
                                String voucherId = documentReference.getId();
                                Log.d(TAG, "Voucher created: " + voucherId);
                                listener.onVoucherSaved(voucherId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating voucher", e);
                                listener.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking voucher code", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Update voucher (Admin)
     */
    public void updateVoucher(String voucherId, com.example.fashionstoreapp.models.Voucher voucher,
            OnVoucherSavedListener listener) {
        voucher.setUpdatedAt(System.currentTimeMillis());

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("type", voucher.getType());
        updates.put("amount", voucher.getAmount());
        updates.put("maxDiscount", voucher.getMaxDiscount());
        updates.put("minOrder", voucher.getMinOrder());
        updates.put("quantity", voucher.getQuantity());
        updates.put("startAt", voucher.getStartAt());
        updates.put("endAt", voucher.getEndAt());
        updates.put("active", voucher.isActive());
        updates.put("description", voucher.getDescription());
        updates.put("updatedAt", voucher.getUpdatedAt());

        db.collection(COLLECTION_VOUCHERS)
                .document(voucherId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Voucher updated: " + voucherId);
                    listener.onVoucherSaved(voucherId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating voucher", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Delete voucher (Admin)
     */
    public void deleteVoucher(String voucherId, OnVoucherDeletedListener listener) {
        db.collection(COLLECTION_VOUCHERS)
                .document(voucherId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Voucher deleted: " + voucherId);
                    listener.onVoucherDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting voucher", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Toggle voucher active status (Admin)
     */
    public void toggleVoucherStatus(String voucherId, boolean active, OnVoucherSavedListener listener) {
        db.collection(COLLECTION_VOUCHERS)
                .document(voucherId)
                .update("active", active, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Voucher status updated: " + voucherId + " -> " + active);
                    listener.onVoucherSaved(voucherId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error toggling voucher status", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get voucher by code (for checkout)
     */
    public void getVoucherByCode(String code, OnVoucherLoadedListener listener) {
        db.collection(COLLECTION_VOUCHERS)
                .whereEqualTo("code", code.toUpperCase())
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError("Mã voucher không hợp lệ hoặc đã hết hạn");
                        return;
                    }

                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    com.example.fashionstoreapp.models.Voucher voucher = document
                            .toObject(com.example.fashionstoreapp.models.Voucher.class);
                    if (voucher != null) {
                        voucher.setId(document.getId());

                        // Check if voucher is still valid
                        if (!voucher.isValid()) {
                            if (voucher.isExpired()) {
                                listener.onError("Voucher đã hết hạn");
                            } else if (voucher.getRemainingQuantity() <= 0) {
                                listener.onError("Voucher đã hết lượt sử dụng");
                            } else {
                                listener.onError("Voucher không hợp lệ");
                            }
                            return;
                        }

                        listener.onVoucherLoaded(voucher);
                    } else {
                        listener.onError("Không thể tải thông tin voucher");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading voucher by code", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Increment voucher used count
     */
    public void incrementVoucherUsedCount(String voucherId) {
        db.collection(COLLECTION_VOUCHERS)
                .document(voucherId)
                .update("usedCount", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Voucher used count incremented: " + voucherId))
                .addOnFailureListener(e -> Log.e(TAG, "Error incrementing voucher used count", e));
    }

    // ==================== VOUCHER LISTENERS ====================

    public interface OnVouchersLoadedListener {
        void onVouchersLoaded(List<com.example.fashionstoreapp.models.Voucher> vouchers);

        void onError(String error);
    }

    public interface OnVoucherSavedListener {
        void onVoucherSaved(String voucherId);

        void onError(String error);
    }

    public interface OnVoucherDeletedListener {
        void onVoucherDeleted();

        void onError(String error);
    }

    public interface OnVoucherLoadedListener {
        void onVoucherLoaded(com.example.fashionstoreapp.models.Voucher voucher);

        void onError(String error);
    }
}
