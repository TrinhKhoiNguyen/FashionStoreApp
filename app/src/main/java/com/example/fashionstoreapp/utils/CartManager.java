package com.example.fashionstoreapp.utils;

import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Add item to cart
    public void addItem(CartItem item) {
        // Check if product already exists in cart
        CartItem existingItem = findItemByProductId(item.getProduct().getId());

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            // Add new item
            cartItems.add(item);
        }
    }

    // Remove item from cart
    public void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    // Update item
    public void updateItem(CartItem item) {
        CartItem existingItem = findItemById(item.getId());
        if (existingItem != null) {
            existingItem.setQuantity(item.getQuantity());
            existingItem.setSize(item.getSize());
            existingItem.setColor(item.getColor());
            existingItem.setSelected(item.isSelected());
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
    }

    // Find item by product ID
    private CartItem findItemByProductId(String productId) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    // Find item by cart item ID
    private CartItem findItemById(String itemId) {
        for (CartItem item : cartItems) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    // Check if product is in cart
    public boolean isProductInCart(String productId) {
        return findItemByProductId(productId) != null;
    }
}
