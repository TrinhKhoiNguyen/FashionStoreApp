package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);

        void onNotificationLongClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notifications,
            OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications != null ? newNotifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView notificationIcon;
        TextView notificationTitle, notificationMessage, notificationTime;
        View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(Notification notification) {
            notificationTitle.setText(notification.getTitle());
            notificationMessage.setText(notification.getMessage());
            notificationTime.setText(notification.getTimeAgo());

            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Set background color for unread notifications
            if (!notification.isRead()) {
                itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            } else {
                itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            }

            // Set icon based on notification type
            int iconRes = getIconForType(notification.getType());
            if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
                // Load image from URL
                Glide.with(context)
                        .load(notification.getImageUrl())
                        .placeholder(iconRes)
                        .error(iconRes)
                        .circleCrop()
                        .into(notificationIcon);
            } else {
                notificationIcon.setImageResource(iconRes);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationLongClick(notification);
                }
                return true;
            });
        }

        private int getIconForType(String type) {
            if (type == null)
                return R.drawable.baseline_notifications_24;

            switch (type) {
                case "order":
                    return R.drawable.baseline_shopping_cart_24;
                case "promotion":
                    return R.drawable.baseline_local_offer_24;
                case "product":
                    return R.drawable.baseline_shopping_bag_24;
                case "system":
                default:
                    return R.drawable.baseline_notifications_24;
            }
        }
    }
}
