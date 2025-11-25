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

import com.example.fashionstoreapp.AddEditCategoryActivity;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.adapters.AdminCategoryAdapter;
import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab fragment for managing categories (CRUD operations)
 */
public class CategoriesTabFragment extends Fragment implements AdminCategoryAdapter.OnAdminCategoryClickListener {

    private TextInputEditText searchCategoryInput;
    private FloatingActionButton addCategoryButton;
    private RecyclerView categoriesRecyclerView;
    private AdminCategoryAdapter adapter;
    private FirestoreManager firestoreManager;
    private List<Category> allCategories = new ArrayList<>();
    private List<Category> filteredCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_tab, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadCategories();

        return view;
    }

    private void initViews(View view) {
        searchCategoryInput = view.findViewById(R.id.searchCategoryInput);
        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        firestoreManager = FirestoreManager.getInstance();

        // Add realtime listener for categories
        setupRealtimeListener();
    }

    private void setupRealtimeListener() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("categories")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        android.util.Log.e("CategoriesTab", "Realtime listener error: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null) {
                        android.util.Log.e("CategoriesTab", "Snapshots is null");
                        return;
                    }

                    android.util.Log.d("CategoriesTab", "Realtime update: " + snapshots.size() + " categories");

                    List<Category> categories = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                category.setId(doc.getId());
                                categories.add(category);
                                android.util.Log.d("CategoriesTab",
                                        "Category loaded: " + category.getName() + " (id: " + doc.getId() + ")");
                            } else {
                                android.util.Log.w("CategoriesTab", "Category is null for doc: " + doc.getId());
                            }
                        } catch (Exception e) {
                            android.util.Log.e("CategoriesTab", "Error parsing category: " + doc.getId(), e);
                        }
                    }

                    allCategories.clear();
                    allCategories.addAll(categories);
                    filterCategories(searchCategoryInput.getText().toString());
                    android.util.Log.d("CategoriesTab", "Updated UI with " + filteredCategories.size() + " categories");
                });
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(getContext(), filteredCategories, this);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriesRecyclerView.setAdapter(adapter);
        android.util.Log.d("CategoriesTab",
                "RecyclerView setup complete. Adapter item count: " + adapter.getItemCount());
    }

    private void setupListeners() {
        // Search functionality
        searchCategoryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Add category button
        addCategoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditCategoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        firestoreManager.loadCategories(new FirestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                allCategories.clear();
                allCategories.addAll(categories);
                filteredCategories.clear();
                filteredCategories.addAll(categories);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterCategories(String query) {
        filteredCategories.clear();
        if (query.isEmpty()) {
            filteredCategories.addAll(allCategories);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Category category : allCategories) {
                if (category.getName() != null && category.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredCategories.add(category);
                }
            }
        }
        android.util.Log.d("CategoriesTab",
                "Filter applied. Showing " + filteredCategories.size() + " of " + allCategories.size() + " categories");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditCategory(Category category) {
        Intent intent = new Intent(getActivity(), AddEditCategoryActivity.class);
        intent.putExtra("CATEGORY_ID", category.getId());
        intent.putExtra("EDIT_MODE", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteCategory(Category category) {
        if (getContext() == null)
            return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + category.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategory(Category category) {
        firestoreManager.deleteCategory(category.getId(), new FirestoreManager.OnCategoryDeletedListener() {
            @Override
            public void onCategoryDeleted() {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                    loadCategories();
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi xóa danh mục: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
