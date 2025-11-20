package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.CartAdapter;
import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.AnimationHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private MaterialToolbar toolbar;
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView emptyCartText, totalPriceText, totalItemsText;
    private Button checkoutButton;
    private View emptyCartLayout, cartBottomLayout;

    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupCartChangeListener();
        loadCartFromFirestore();
    }

    private void initViews() {
        toolbar = findViewById(R.id.cartToolbar);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartText = findViewById(R.id.emptyCartText);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        cartBottomLayout = findViewById(R.id.cartBottomLayout);
        totalPriceText = findViewById(R.id.totalPriceText);
        totalItemsText = findViewById(R.id.totalItemsText);
        checkoutButton = findViewById(R.id.checkoutButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        cartRecyclerView.setLayoutManager(layoutManager);

        List<CartItem> cartItems = cartManager.getCartItems();
        cartAdapter = new CartAdapter(this, cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);
        
        // Setup checkout button once
        checkoutButton.setOnClickListener(v -> {
            AnimationHelper.animateButtonPress(v, () -> {
                List<CartItem> selectedItems = cartAdapter.getSelectedItems();
                if (selectedItems.size() > 0) {
                    // Navigate to checkout activity
                    Intent intent = new Intent(this, CheckoutActivity.class);
                    startActivity(intent);
                } else {
                    AnimationHelper.animateShake(v);
                    Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupCartChangeListener() {
        cartManager.setCartChangeListener(() -> {
            runOnUiThread(() -> {
                loadCartItems();
                updateUI();
            });
        });
    }

    private void loadCartFromFirestore() {
        cartManager.loadCartFromFirestore(new CartManager.OnCartLoadedListener() {
            @Override
            public void onCartLoaded() {
                refreshCartItems();
                updateUI();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, "Lỗi tải giỏ hàng: " + error, Toast.LENGTH_SHORT).show();
                refreshCartItems();
                updateUI();
            }
        });
    }

    private void refreshCartItems() {
        List<CartItem> cartItems = cartManager.getCartItems();
        cartAdapter.updateCartItems(cartItems);
    }

    private void loadCartItems() {
        List<CartItem> cartItems = cartManager.getCartItems();
        cartAdapter.updateCartItems(cartItems);
    }

    private void updateUI() {
        List<CartItem> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            // Show empty cart
            emptyCartLayout.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
            cartBottomLayout.setVisibility(View.GONE);
        } else {
            // Show cart items
            emptyCartLayout.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
            cartBottomLayout.setVisibility(View.VISIBLE);

            // Update total
            updateTotal();
        }
    }

    private void updateTotal() {
        List<CartItem> selectedItems = cartAdapter.getSelectedItems();
        double total = cartAdapter.getTotalPrice();
        int itemCount = selectedItems.size();

        totalPriceText.setText(String.format("%,.0f₫", total));
        totalItemsText.setText(itemCount + " sản phẩm");

        checkoutButton.setEnabled(itemCount > 0);
    }

    // CartAdapter.OnCartItemListener implementation
    @Override
    public void onQuantityChanged(CartItem item) {
        // Update UI immediately for responsiveness
        updateTotal();
        
        // Save to Firestore in background
        new Thread(() -> {
            cartManager.updateItem(item);
        }).start();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        cartManager.removeItem(item);
        updateUI();
        Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(CartItem item, boolean isSelected) {
        updateTotal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
        updateUI();
    }
}
