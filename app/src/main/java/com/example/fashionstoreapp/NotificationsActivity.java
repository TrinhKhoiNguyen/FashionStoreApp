package com.example.fashionstoreapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.NotificationAdapter;
import com.example.fashionstoreapp.models.Notification;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private ImageView btnBack;
    private RecyclerView notificationsRecyclerView;
    private LinearLayout emptyLayout;
    private ProgressBar loadingProgress;

    private NotificationAdapter notificationAdapter;
    private List<Notification> notifications;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private TextView emptyTitleTxt, emptyMessageTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyLayout = findViewById(R.id.emptyLayout);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyTitleTxt = findViewById(R.id.emptyTitle);
        emptyMessageTxt = findViewById(R.id.emptyMessage);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notifications, this);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationAdapter);
    }

    private void loadNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingProgress.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        notificationsRecyclerView.setVisibility(View.GONE);

        String userId = currentUser.getUid();

        // Load user-specific notifications
        firestoreManager.loadNotifications(userId, new FirestoreManager.OnNotificationsLoadedListener() {
            @Override
            public void onNotificationsLoaded(List<Notification> userNotifications) {
                // Also load global notifications
                firestoreManager.loadGlobalNotifications(new FirestoreManager.OnNotificationsLoadedListener() {
                    @Override
                    public void onNotificationsLoaded(List<Notification> globalNotifications) {
                        // Combine both lists
                        List<Notification> allNotifications = new ArrayList<>();
                        allNotifications.addAll(userNotifications);
                        allNotifications.addAll(globalNotifications);

                        // Sort by timestamp (newest first)
                        allNotifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

                        displayNotifications(allNotifications);
                    }

                    @Override
                    public void onError(String error) {
                        // Just display user notifications if global fails
                        displayNotifications(userNotifications);
                    }
                });
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
                // Detect permission denied error from Firestore and show helpful message
                if (error != null && error.toLowerCase().contains("permission_denied")) {
                    if (emptyTitleTxt != null)
                        emptyTitleTxt.setText("Quyền truy cập bị từ chối");
                    if (emptyMessageTxt != null)
                        emptyMessageTxt.setText(
                                "Ứng dụng không có quyền đọc thông báo từ Firebase. Kiểm tra quy tắc Firestore hoặc quyền truy cập của người dùng.");
                } else {
                    if (emptyTitleTxt != null)
                        emptyTitleTxt.setText("Chưa có thông báo");
                    if (emptyMessageTxt != null)
                        emptyMessageTxt
                                .setText("Bạn sẽ nhận được thông báo về đơn hàng, khuyến mãi và cập nhật mới tại đây");
                }
                Toast.makeText(NotificationsActivity.this, "Lỗi tải thông báo: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayNotifications(List<Notification> notificationList) {
        loadingProgress.setVisibility(View.GONE);

        if (notificationList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);
            notificationAdapter.updateNotifications(notificationList);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read if not already read
        if (!notification.isRead()) {
            firestoreManager.markNotificationAsRead(notification.getId(),
                    new FirestoreManager.OnNotificationUpdatedListener() {
                        @Override
                        public void onUpdated() {
                            notification.setRead(true);
                            notificationAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String error) {
                            // Silent error
                        }
                    });
        }

        // Handle action based on notification type
        handleNotificationAction(notification);
    }

    @Override
    public void onNotificationLongClick(Notification notification) {
        // Show delete dialog
        new AlertDialog.Builder(this)
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteNotification(notification))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNotification(Notification notification) {
        firestoreManager.deleteNotification(notification.getId(),
                new FirestoreManager.OnNotificationDeletedListener() {
                    @Override
                    public void onDeleted() {
                        Toast.makeText(NotificationsActivity.this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                        loadNotifications(); // Reload
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(NotificationsActivity.this, "Lỗi xóa thông báo: " + error, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void handleNotificationAction(Notification notification) {
        String type = notification.getType();
        if (type == null)
            return;

        switch (type) {
            case "order":
                // Open order details if orderId is provided
                if (notification.getOrderId() != null) {
                    // TODO: Navigate to order details
                    Toast.makeText(this, "Mở chi tiết đơn hàng: " + notification.getOrderId(), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case "product":
                // Open product details if productId is provided
                if (notification.getProductId() != null) {
                    // TODO: Navigate to product details
                    Toast.makeText(this, "Mở sản phẩm: " + notification.getProductId(), Toast.LENGTH_SHORT).show();
                }
                break;
            case "promotion":
                // Open promotions/vouchers page
                Toast.makeText(this, "Xem khuyến mãi", Toast.LENGTH_SHORT).show();
                break;
            case "system":
            default:
                // Just show the notification, already read
                break;
        }
    }
}
