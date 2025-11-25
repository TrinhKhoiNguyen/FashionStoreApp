package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private OnAdminUserClickListener listener;

    public interface OnAdminUserClickListener {
        void onUserClick(User user);
    }

    public AdminUserAdapter(Context context, List<User> users, OnAdminUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.userName.setText(user.getDisplayName());

        // Show email if available
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            holder.userEmail.setText(user.getEmail());
            holder.userEmail.setVisibility(View.VISIBLE);
        } else {
            holder.userEmail.setVisibility(View.GONE);
        }

        // Show phone if available
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            holder.userPhone.setText(user.getPhone());
            holder.userPhone.setVisibility(View.VISIBLE);
        } else {
            holder.userPhone.setVisibility(View.GONE);
        }

        // Show admin badge if user is admin
        if (user.isAdmin()) {
            holder.userRoleBadge.setVisibility(View.VISIBLE);
            holder.userRoleBadge.setText("Admin");
        } else {
            holder.userRoleBadge.setVisibility(View.GONE);
        }

        // Load avatar if available
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.baseline_person_24)
                    .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageResource(R.drawable.baseline_person_24);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        holder.moreButton.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName, userEmail, userPhone, userRoleBadge;
        ImageButton moreButton;

        ViewHolder(View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.adminUserAvatar);
            userName = itemView.findViewById(R.id.adminUserName);
            userEmail = itemView.findViewById(R.id.adminUserEmail);
            userPhone = itemView.findViewById(R.id.adminUserPhone);
            userRoleBadge = itemView.findViewById(R.id.adminUserRoleBadge);
            moreButton = itemView.findViewById(R.id.adminUserMoreButton);
        }
    }
}
