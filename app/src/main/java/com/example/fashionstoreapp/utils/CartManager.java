package com.example.fashionstoreapp.utils;

import android.util.Log;

import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";
    private static CartManager instance;
    private List<CartItem> cartItems;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private CartChangeListener cartChangeListener;

    public interface CartChangeListener {
        void onCartChanged();
    }

    private CartManager() {
        cartItems = new ArrayList<>();
        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Set cart change listener
    public void setCartChangeListener(CartChangeListener listener) {
        this.cartChangeListener = listener;
    }

    // Add item to cart
    public void addItem(CartItem item) {
        if (item == null || item.getProduct() == null) return;
        
        // Check if product already exists in cart
        CartItem existingItem = findItemByProductId(item.getProduct().getId());

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            // Add new item
            cartItems.add(item);
        }

        // Save to Firestore
        saveCartToFirestore();
        notifyCartChanged();
    }

    // Remove item from cart
    public void removeItem(CartItem item) {
        cartItems.remove(item);
        saveCartToFirestore();
        notifyCartChanged();
    }

    // Update item
    public void updateItem(CartItem item) {
        CartItem existingItem = findItemById(item.getId());
        if (existingItem != null) {
            existingItem.setQuantity(item.getQuantity());
            existingItem.setSize(item.getSize());
            existingItem.setColor(item.getColor());
            existingItem.setSelected(item.isSelected());
            saveCartToFirestore();
            notifyCartChanged();
        }
    }

    // Get all cart items
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    // Get cart item count
    public int getCartItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    // Get total price
    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    // Get selected items
    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    // Clear cart
    public void clearCart() {
        cartItems.clear();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firestoreManager.clearCart(user.getUid(), new FirestoreManager.OnCartSavedListener() {
                @Override
                public void onCartSaved() {
                    Log.d(TAG, "Cart cleared in Firestore");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error clearing cart: " + error);
                }
            });
        }
        notifyCartChanged();
    }

    // Clear selected items
    public void clearSelectedItems() {
        List<CartItem> itemsToRemove = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                itemsToRemove.add(item);
            }
        }
        cartItems.removeAll(itemsToRemove);
        saveCartToFirestore();
        notifyCartChanged();
    }

    // Find item by product ID
    private CartItem findItemByProductId(String productId) {
        if (productId == null) return null;
        for (CartItem item : cartItems) {
            if (item.getProduct() != null && productId.equals(item.getProduct().getId())) {
                return item;
            }
        }
        return null;
    }

    // Find item by cart item ID
    private CartItem findItemById(String itemId) {
        if (itemId == null) return null;
        for (CartItem item : cartItems) {
            if (item.getId() != null && item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    // Check if product is in cart
    public boolean isProductInCart(String productId) {
        return findItemByProductId(productId) != null;
    }

    // Save cart to Firestore
    private void saveCartToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firestoreManager.saveCartItems(user.getUid(), cartItems, new FirestoreManager.OnCartSavedListener() {
                @Override
                public void onCartSaved() {
                    Log.d(TAG, "Cart saved to Firestore successfully");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error saving cart to Firestore: " + error);
                }
            });
        }
    }

    // Load cart from Firestore
    public void loadCartFromFirestore(OnCartLoadedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firestoreManager.loadCartItems(user.getUid(), new FirestoreManager.OnCartLoadedListener() {
                @Override
                public void onCartLoaded(List<CartItem> items) {
                    cartItems.clear();
                    cartItems.addAll(items);
                    Log.d(TAG, "Cart loaded from Firestore: " + items.size() + " items");
                    notifyCartChanged();
                    if (listener != null) {
                        listener.onCartLoaded();
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading cart from Firestore: " + error);
                    if (listener != null) {
                        listener.onError(error);
                    }
                }
            });
        }
    }

    // Notify cart changed
    private void notifyCartChanged() {
        if (cartChangeListener != null) {
            cartChangeListener.onCartChanged();
        }
    }

    // Callback interface for cart loading
    public interface OnCartLoadedListener {
        void onCartLoaded();

        void onError(String error);
    }
}
