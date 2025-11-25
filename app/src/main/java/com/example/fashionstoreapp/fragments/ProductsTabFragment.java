package com.example.fashionstoreapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.AddEditProductActivity;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.adapters.AdminProductAdapter;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab fragment for managing products (CRUD operations)
 */
public class ProductsTabFragment extends Fragment implements AdminProductAdapter.OnAdminProductClickListener {

    private TextInputEditText searchProductInput;
    private FloatingActionButton addProductButton;
    private RecyclerView productsRecyclerView;
    private AdminProductAdapter adapter;
    private FirestoreManager firestoreManager;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_tab, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadProducts();

        return view;
    }

    private void initViews(View view) {
        searchProductInput = view.findViewById(R.id.searchProductInput);
        addProductButton = view.findViewById(R.id.addProductButton);
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        firestoreManager = FirestoreManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(getContext(), filteredProducts, this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Search functionality
        searchProductInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Add product button
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts(); // Reload when returning from AddEditProductActivity
    }

    private void loadProducts() {
        firestoreManager.getAllProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                allProducts.clear();
                allProducts.addAll(products);
                filteredProducts.clear();
                filteredProducts.addAll(products);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterProducts(String query) {
        filteredProducts.clear();
        if (query.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : allProducts) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                        product.getCategoryName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditProduct(Product product) {
        Intent intent = new Intent(getActivity(), AddEditProductActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        intent.putExtra("EDIT_MODE", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteProduct(Product product) {
        if (getContext() == null)
            return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa sản phẩm \"" + product.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct(Product product) {
        firestoreManager.deleteProduct(product.getProductId(), new FirestoreManager.OnProductDeletedListener() {
            @Override
            public void onProductDeleted() {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    loadProducts();
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi xóa sản phẩm: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
