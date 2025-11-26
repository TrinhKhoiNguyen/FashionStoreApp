package com.example.fashionstoreapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.adapters.AdminUserAdapter;
import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment implements AdminUserAdapter.OnAdminUserClickListener {

    private TextInputEditText searchUserInput;
    private RecyclerView usersRecyclerView;
    private AdminUserAdapter adapter;
    private FirestoreManager firestoreManager;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadUsers();

        return view;
    }

    private void initViews(View view) {
        searchUserInput = view.findViewById(R.id.searchUserInput);
        usersRecyclerView = view.findViewById(R.id.adminUsersRecyclerView);
        firestoreManager = FirestoreManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(getContext(), filteredUsers, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Search functionality
        searchUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadUsers() {
        firestoreManager.getAllUsers(new FirestoreManager.OnUsersLoadedListener() {
            @Override
            public void onUsersLoaded(List<User> users) {
                allUsers.clear();
                allUsers.addAll(users);
                filteredUsers.clear();
                filteredUsers.addAll(users);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải người dùng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (User user : allUsers) {
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                String phone = user.getPhone() != null ? user.getPhone().toLowerCase() : "";

                if (name.contains(lowerCaseQuery) ||
                        email.contains(lowerCaseQuery) ||
                        phone.contains(lowerCaseQuery)) {
                    filteredUsers.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUserClick(User user) {
        // Show user options dialog
        String[] options = { "Xem chi tiết", "Thay đổi quyền", "Vô hiệu hóa tài khoản", "Xóa tài khoản" };

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(user.getDisplayName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // View details
                            showUserDetails(user);
                            break;
                        case 1: // Change role
                            changeUserRole(user);
                            break;
                        case 2: // Disable account
                            toggleUserActive(user);
                            break;
                        case 3: // Delete account
                            confirmAndDeleteUser(user);
                            break;
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void changeUserRole(User user) {
        String[] roles = { "user", "admin" };
        String currentRole = user.getRole() != null ? user.getRole() : "user";
        int currentIndex = "admin".equals(currentRole) ? 1 : 0;

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Thay đổi quyền")
                .setSingleChoiceItems(roles, currentIndex, null)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    int selectedPosition = ((androidx.appcompat.app.AlertDialog) dialog).getListView()
                            .getCheckedItemPosition();
                    String newRole = roles[selectedPosition];

                    firestoreManager.updateUserRole(user.getId(), newRole,
                            new FirestoreManager.OnUserRoleUpdatedListener() {
                                @Override
                                public void onRoleUpdated() {
                                    Toast.makeText(getContext(), "Đã cập nhật quyền", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showUserDetails(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tên: ").append(user.getDisplayName() == null ? "-" : user.getDisplayName()).append("\n");
        sb.append("Email: ").append(user.getEmail() == null ? "-" : user.getEmail()).append("\n");
        sb.append("Số điện thoại: ").append(user.getPhone() == null ? "-" : user.getPhone()).append("\n");
        sb.append("Vai trò: ").append(user.getRole() == null ? "user" : user.getRole()).append("\n");
        sb.append("Trạng thái: ").append(user.isActive() ? "Hoạt động" : "Vô hiệu hóa");

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Chi tiết người dùng")
                .setMessage(sb.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void toggleUserActive(User user) {
        boolean currentlyActive = user.isActive();
        String verb = currentlyActive ? "Vô hiệu hóa" : "Kích hoạt";
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(verb + " tài khoản")
                .setMessage(
                        "Bạn có chắc muốn " + verb.toLowerCase() + " tài khoản của '" + user.getDisplayName() + "'?")
                .setPositiveButton(verb, (dialog, which) -> {
                    firestoreManager.setUserActive(user.getId(), !currentlyActive,
                            new FirestoreManager.OnUserRoleUpdatedListener() {
                                @Override
                                public void onRoleUpdated() {
                                    Toast.makeText(getContext(), verb + " thành công", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmAndDeleteUser(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa tài khoản")
                .setMessage(
                        "Xóa người dùng sẽ xóa hồ sơ trong Firestore nhưng KHÔNG xóa tài khoản Auth. Bạn có chắc muốn xóa '"
                                + user.getDisplayName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    firestoreManager.deleteUser(user.getId(), new FirestoreManager.OnProductDeletedListener() {
                        @Override
                        public void onProductDeleted() {
                            Toast.makeText(getContext(), "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
