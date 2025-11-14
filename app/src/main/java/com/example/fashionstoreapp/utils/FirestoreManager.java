package com.example.fashionstoreapp.utils;

import android.util.Log;

import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.models.Product;
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
        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("category", categoryId)
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
                    Log.d(TAG, "Loaded " + products.size() + " products for category: " + categoryId);
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products by category", e);
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

    // ==================== CATEGORIES ====================

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
}
