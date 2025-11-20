package com.example.fashionstoreapp.utils;

import android.util.Log;

import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.Review;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
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
        Map<String, Object> favoriteData = new java.util.HashMap<>();
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

    // ==================== INTERFACES ====================

    /**
     * Load all categories from Firestore
     */
    public void loadCategories(OnCategoriesLoadedListener listener) {
        db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("isActive", true)
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
        Map<String, Object> profileData = new java.util.HashMap<>();
        profileData.put("name", name);
        profileData.put("birthday", birthday);
        profileData.put("gender", gender);
        if (phone != null && !phone.isEmpty()) {
            profileData.put("phone", phone);
        }
        profileData.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved successfully");
                    listener.onProfileSaved();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Update user photo URL in Firestore
     */
    public void updateUserPhotoUrl(String userId, String photoUrl) {
        Map<String, Object> updates = new java.util.HashMap<>();
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
        List<Map<String, Object>> cartData = new ArrayList<>();
        for (com.example.fashionstoreapp.models.CartItem item : cartItems) {
            Map<String, Object> itemData = new java.util.HashMap<>();
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

        Map<String, Object> data = new java.util.HashMap<>();
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
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> cartData = (List<Map<String, Object>>) documentSnapshot
                                .get("items");

                        if (cartData != null) {
                            List<String> productIds = new ArrayList<>();
                            for (Map<String, Object> itemData : cartData) {
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
                                    for (Map<String, Object> itemData : cartData) {
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
        Map<String, Object> orderData = new java.util.HashMap<>();
        orderData.put("orderId", order.getOrderId());
        orderData.put("userId", order.getUserId());
        orderData.put("totalAmount", order.getTotalAmount());
        orderData.put("status", order.getStatus());
        orderData.put("paymentMethod", order.getPaymentMethod());
        orderData.put("recipientName", order.getRecipientName());
        orderData.put("recipientPhone", order.getRecipientPhone());
        orderData.put("shippingAddress", order.getShippingAddress());
        orderData.put("note", order.getNote());
        orderData.put("createdAt", order.getCreatedAt());
        orderData.put("updatedAt", order.getUpdatedAt());

        // Convert order items to map format
        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (com.example.fashionstoreapp.models.CartItem item : order.getItems()) {
            Map<String, Object> itemData = new java.util.HashMap<>();
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
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving order", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Load orders from Firestore for a specific user
     */
    public void loadOrders(String userId, OnOrdersLoadedListener listener) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {
                    List<com.example.fashionstoreapp.models.Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        com.example.fashionstoreapp.models.Order order = new com.example.fashionstoreapp.models.Order();
                        order.setOrderId(document.getString("orderId"));
                        order.setUserId(document.getString("userId"));
                        order.setTotalAmount(document.getDouble("totalAmount"));
                        order.setStatus(document.getString("status"));
                        order.setPaymentMethod(document.getString("paymentMethod"));
                        order.setRecipientName(document.getString("recipientName"));
                        order.setRecipientPhone(document.getString("recipientPhone"));
                        order.setShippingAddress(document.getString("shippingAddress"));
                        order.setNote(document.getString("note"));
                        order.setCreatedAt(document.getLong("createdAt"));
                        order.setUpdatedAt(document.getLong("updatedAt"));

                        // Load order items
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) document
                                .get("items");
                        if (itemsData != null) {
                            List<com.example.fashionstoreapp.models.CartItem> items = new ArrayList<>();
                            for (Map<String, Object> itemData : itemsData) {
                                // Create a simplified product for the cart item
                                Product product = new Product();
                                product.setId((String) itemData.get("productId"));
                                product.setName((String) itemData.get("productName"));
                                product.setImageUrl((String) itemData.get("productImage"));
                                product.setCurrentPrice(((Number) itemData.get("productPrice")).doubleValue());

                                com.example.fashionstoreapp.models.CartItem item = new com.example.fashionstoreapp.models.CartItem();
                                item.setProduct(product);
                                item.setQuantity(((Number) itemData.get("quantity")).intValue());
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
        void onOrdersLoaded(List<com.example.fashionstoreapp.models.Order> orders);

        void onError(String error);
    }
}
