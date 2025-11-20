package com.example.fashionstoreapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.adapters.ReviewAdapter;
import com.example.fashionstoreapp.adapters.ReviewImageAdapter;
import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.utils.AnimationHelper;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.Review;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 imageViewPager;
    private LinearLayout imageIndicator;
    private TextView tvProductName, tvCategory, tvProductId;
    private TextView tvCurrentPrice, tvOriginalPrice, tvDiscountBadge;
    private TextView tvRating, tvReviewCount, tvDescription;
    private RatingBar ratingBar;
    private LinearLayout voucherSection, sizeContainer;
    private TextView tvVoucherText;
    private RecyclerView reviewsRecyclerView;
    private TextView tvNoReviews;
    private Button btnAddToCart, btnBuyNow, btnWriteReview;
    private ImageView btnBack;
    private FloatingActionButton fabFavorite;

    private Product product;
    private String selectedSize = "M"; // Default size
    private List<String> productImages;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews;
    private FirestoreManager firestoreManager;

    // Image picker for reviews
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private List<Uri> selectedReviewImages = new ArrayList<>();
    private ReviewImageAdapter reviewImageAdapter;
    private RecyclerView rvReviewImagesDialog;
    private AlertDialog reviewDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize FirestoreManager
        firestoreManager = FirestoreManager.getInstance();

        // Initialize image picker
        setupImagePicker();

        // Get product from intent
        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupImageGallery();
        setupProductInfo();
        setupSizeSelection();
        setupReviews();
        setupClickListeners();
    }

    private void initViews() {
        imageViewPager = findViewById(R.id.imageViewPager);
        imageIndicator = findViewById(R.id.imageIndicator);
        tvProductName = findViewById(R.id.tvProductName);
        tvCategory = findViewById(R.id.tvCategory);
        tvProductId = findViewById(R.id.tvProductId);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvDiscountBadge = findViewById(R.id.tvDiscountBadge);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvDescription = findViewById(R.id.tvDescription);
        ratingBar = findViewById(R.id.ratingBar);
        voucherSection = findViewById(R.id.voucherSection);
        tvVoucherText = findViewById(R.id.tvVoucherText);
        sizeContainer = findViewById(R.id.sizeContainer);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        btnBack = findViewById(R.id.btnBack);
        fabFavorite = findViewById(R.id.fabFavorite);
    }

    private void setupImageGallery() {
        // For now, use single product image (can be extended to multiple images)
        productImages = Arrays.asList(product.getImageUrl());

        // Setup ViewPager2 adapter
        ImageGalleryAdapter adapter = new ImageGalleryAdapter(productImages);
        imageViewPager.setAdapter(adapter);

        // Setup indicators if multiple images
        if (productImages.size() > 1) {
            setupImageIndicators(productImages.size());
            imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateImageIndicators(position);
                }
            });
        }
    }

    private void setupImageIndicators(int count) {
        imageIndicator.removeAllViews();
        ImageView[] dots = new ImageView[count];

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(android.R.drawable.presence_invisible);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            imageIndicator.addView(dots[i], params);
        }

        if (count > 0) {
            dots[0].setImageResource(android.R.drawable.presence_online);
        }
    }

    private void updateImageIndicators(int position) {
        int childCount = imageIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView dot = (ImageView) imageIndicator.getChildAt(i);
            if (i == position) {
                dot.setImageResource(android.R.drawable.presence_online);
            } else {
                dot.setImageResource(android.R.drawable.presence_invisible);
            }
        }
    }

    private void setupProductInfo() {
        tvProductName.setText(product.getName());
        tvCategory.setText(getCategoryDisplayName(product.getCategory()));
        tvProductId.setText(product.getId().toUpperCase());
        tvCurrentPrice.setText(product.getFormattedCurrentPrice());

        // Original price with strikethrough
        if (product.hasDiscount()) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(product.getFormattedOriginalPrice());
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            tvDiscountBadge.setVisibility(View.VISIBLE);
            tvDiscountBadge.setText("-" + product.getDiscountPercent() + "%");
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountBadge.setVisibility(View.GONE);
        }

        // Rating
        ratingBar.setRating((float) product.getRating());
        tvRating.setText(String.format("%.1f", product.getRating()));
        tvReviewCount.setText("(" + product.getReviewCount() + " đánh giá)");

        // Description
        String description = product.getDescription();
        if (description == null || description.isEmpty()) {
            description = "Sản phẩm chất lượng cao, thiết kế hiện đại, phù hợp với mọi phong cách.";
        }
        tvDescription.setText(description);

        // Voucher
        if (product.isHasVoucher() && product.getVoucherText() != null && !product.getVoucherText().isEmpty()) {
            voucherSection.setVisibility(View.VISIBLE);
            tvVoucherText.setText(product.getVoucherText());
        } else {
            voucherSection.setVisibility(View.GONE);
        }

        // Favorite status
        updateFavoriteButton();
    }

    private String getCategoryDisplayName(String category) {
        if (category == null)
            return "Sản phẩm";

        switch (category.toLowerCase()) {
            case "ao-polo":
                return "Áo Polo";
            case "ao-so-mi":
                return "Áo Sơ Mi";
            case "ao-thun":
                return "Áo Thun";
            case "retro-sports":
                return "Retro Sports";
            default:
                return category;
        }
    }

    private void setupSizeSelection() {
        List<String> sizes = product.getAvailableSizes();
        if (sizes == null || sizes.isEmpty()) {
            sizes = Arrays.asList("S", "M", "L", "XL");
        }

        for (String size : sizes) {
            Button sizeButton = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            params.setMargins(8, 0, 8, 0);
            sizeButton.setLayoutParams(params);
            sizeButton.setText(size);
            sizeButton.setTextSize(16);
            sizeButton.setAllCaps(true);

            // Set initial state
            if (size.equals(selectedSize)) {
                sizeButton.setBackgroundColor(Color.BLACK);
                sizeButton.setTextColor(Color.WHITE);
            } else {
                sizeButton.setBackgroundColor(Color.parseColor("#F5F5F5"));
                sizeButton.setTextColor(Color.BLACK);
            }

            // Click listener
            sizeButton.setOnClickListener(v -> {
                selectedSize = size;
                updateSizeSelection();
            });

            sizeContainer.addView(sizeButton);
        }
    }

    private void updateSizeSelection() {
        for (int i = 0; i < sizeContainer.getChildCount(); i++) {
            Button btn = (Button) sizeContainer.getChildAt(i);
            if (btn.getText().toString().equals(selectedSize)) {
                btn.setBackgroundColor(Color.BLACK);
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundColor(Color.parseColor("#F5F5F5"));
                btn.setTextColor(Color.BLACK);
            }
        }
    }

    private void setupReviews() {
        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviews);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        loadReviews();
    }

    private void loadReviews() {
        FirestoreManager.getInstance().loadProductReviews(product.getId(),
                new FirestoreManager.OnReviewsLoadedListener() {
                    @Override
                    public void onReviewsLoaded(List<Review> loadedReviews) {
                        android.util.Log.d("ProductDetail", "Loaded " + loadedReviews.size() + " reviews");
                        reviews.clear();
                        reviews.addAll(loadedReviews);

                        if (reviews.isEmpty()) {
                            tvNoReviews.setVisibility(View.VISIBLE);
                            reviewsRecyclerView.setVisibility(View.GONE);
                        } else {
                            tvNoReviews.setVisibility(View.GONE);
                            reviewsRecyclerView.setVisibility(View.VISIBLE);
                            reviewAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("ProductDetail", "Error loading reviews: " + error);
                        tvNoReviews.setVisibility(View.VISIBLE);
                        reviewsRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Common favorite click handler
        View.OnClickListener favoriteClickListener = v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            String productId = product.getId();
            boolean newFavoriteState = !product.isFavorite();

            product.setFavorite(newFavoriteState);
            updateFavoriteButton();

            if (newFavoriteState) {
                // Add to favorites
                firestoreManager.saveFavorite(userId, productId, new FirestoreManager.OnFavoriteSavedListener() {
                    @Override
                    public void onFavoriteSaved() {
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        product.setFavorite(false); // Revert on error
                        updateFavoriteButton();
                        Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Remove from favorites
                firestoreManager.removeFavorite(userId, productId, new FirestoreManager.OnFavoriteRemovedListener() {
                    @Override
                    public void onFavoriteRemoved() {
                        Toast.makeText(ProductDetailActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        product.setFavorite(true); // Revert on error
                        updateFavoriteButton();
                        Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        if (fabFavorite != null) {
            fabFavorite.setOnClickListener(favoriteClickListener);
        }

        btnAddToCart.setOnClickListener(v -> {
            AnimationHelper.animateBounceIn(v);
            CartItem cartItem = new CartItem(product, 1, selectedSize, "");
            CartManager.getInstance().addItem(cartItem);
            Toast.makeText(this, "Đã thêm vào giỏ hàng (Size: " + selectedSize + ")", Toast.LENGTH_SHORT).show();
        });

        btnBuyNow.setOnClickListener(v -> {
            AnimationHelper.animateButtonPress(v, () -> {
                CartItem cartItem = new CartItem(product, 1, selectedSize, "");
                CartManager.getInstance().addItem(cartItem);
                Toast.makeText(this, "Chức năng mua ngay đang phát triển", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to checkout
            });
        });

        btnWriteReview.setOnClickListener(v -> {
            AnimationHelper.animateScaleUpSmall(v);
            showWriteReviewDialog();
        });
    }

    private void showWriteReviewDialog() {
        SessionManager sessionManager = new SessionManager(this);

        // Get user info (use guest if not logged in)
        String userId = sessionManager.isLoggedIn() ? sessionManager.getUserId()
                : "guest_" + System.currentTimeMillis();
        String userName = sessionManager.isLoggedIn() ? sessionManager.getUserName() : "Khách hàng";

        // Reset selected images
        selectedReviewImages.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_write_review, null);
        builder.setView(dialogView);

        reviewDialog = builder.create();

        RatingBar ratingBarDialog = dialogView.findViewById(R.id.ratingBarDialog);
        EditText etReviewComment = dialogView.findViewById(R.id.etReviewComment);
        rvReviewImagesDialog = dialogView.findViewById(R.id.rvReviewImages);
        Button btnAddImage = dialogView.findViewById(R.id.btnAddImage);
        Button btnCancelReview = dialogView.findViewById(R.id.btnCancelReview);
        Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);

        // Setup image RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvReviewImagesDialog.setLayoutManager(layoutManager);

        List<String> imageUris = new ArrayList<>();
        reviewImageAdapter = new ReviewImageAdapter(this, imageUris, true);
        reviewImageAdapter.setOnImageClickListener(new ReviewImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(String imageUrl, int position) {
                // View full image
                Toast.makeText(ProductDetailActivity.this, "Xem ảnh", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoveClick(int position) {
                selectedReviewImages.remove(position);
                updateReviewImagesUI();
            }
        });
        rvReviewImagesDialog.setAdapter(reviewImageAdapter);

        // Add image button
        btnAddImage.setOnClickListener(v -> {
            if (selectedReviewImages.size() >= 5) {
                Toast.makeText(this, "Chỉ được chọn tối đa 5 ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        btnCancelReview.setOnClickListener(v -> {
            selectedReviewImages.clear();
            reviewDialog.dismiss();
        });

        btnSubmitReview.setOnClickListener(v -> {
            float rating = ratingBarDialog.getRating();
            String comment = etReviewComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nhận xét của bạn", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            btnSubmitReview.setEnabled(false);
            btnSubmitReview.setText("Đang gửi...");

            // Create review object
            Review review = new Review();
            review.setProductId(product.getId());
            review.setUserId(userId);
            review.setUserName(userName);
            review.setRating(rating);
            review.setComment(comment);
            review.setTimestamp(System.currentTimeMillis());

            // Upload images if any
            if (!selectedReviewImages.isEmpty()) {
                uploadReviewImages(review, reviewDialog, btnSubmitReview);
            } else {
                // Submit without images
                submitReview(review, reviewDialog, btnSubmitReview);
            }
        });

        reviewDialog.show();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // Handle multiple images
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count && selectedReviewImages.size() < 5; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                selectedReviewImages.add(imageUri);
                            }
                        } else if (data.getData() != null) {
                            // Single image
                            Uri imageUri = data.getData();
                            selectedReviewImages.add(imageUri);
                        }

                        updateReviewImagesUI();
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
    }

    private void updateReviewImagesUI() {
        List<String> uriStrings = new ArrayList<>();
        for (Uri uri : selectedReviewImages) {
            uriStrings.add(uri.toString());
        }
        reviewImageAdapter.updateImages(uriStrings);
    }

    private void uploadReviewImages(Review review, AlertDialog dialog, Button submitButton) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        List<String> uploadedUrls = new ArrayList<>();
        final int[] uploadCount = { 0 };
        final int totalImages = selectedReviewImages.size();

        for (Uri imageUri : selectedReviewImages) {
            // Create unique filename
            String filename = "reviews/" + product.getId() + "/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageRef.child(filename);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedUrls.add(uri.toString());
                            uploadCount[0]++;

                            // Check if all images uploaded
                            if (uploadCount[0] == totalImages) {
                                review.setImageUrls(uploadedUrls);
                                submitReview(review, dialog, submitButton);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        submitButton.setEnabled(true);
                        submitButton.setText("Gửi đánh giá");
                    });
        }
    }

    private void submitReview(Review review, AlertDialog dialog, Button submitButton) {
        // Submit to Firestore
        FirestoreManager.getInstance().addReview(review, new FirestoreManager.OnReviewAddedListener() {
            @Override
            public void onReviewAdded(String reviewId) {
                Toast.makeText(ProductDetailActivity.this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                selectedReviewImages.clear();
                dialog.dismiss();

                // Set review ID
                review.setId(reviewId);

                // Add review to list immediately for instant UI update
                reviews.add(0, review); // Add at top (newest first)

                // Update UI immediately
                if (reviews.size() == 1) {
                    // First review, show RecyclerView
                    tvNoReviews.setVisibility(View.GONE);
                    reviewsRecyclerView.setVisibility(View.VISIBLE);
                }
                reviewAdapter.notifyItemInserted(0);
                reviewsRecyclerView.smoothScrollToPosition(0);

                // Update rating count immediately
                int newReviewCount = product.getReviewCount() + 1;
                product.setReviewCount(newReviewCount);
                tvReviewCount.setText("(" + newReviewCount + " đánh giá)");

                // Reload product info from Firestore to get accurate rating
                reloadProductInfo();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

    }

    private void reloadProductInfo() {
        // Reload product to get updated rating
        FirestoreManager.getInstance().loadProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                for (Product p : products) {
                    if (p.getId().equals(product.getId())) {
                        product.setRating(p.getRating());
                        product.setReviewCount(p.getReviewCount());

                        // Update UI
                        ratingBar.setRating((float) product.getRating());
                        tvRating.setText(String.format("%.1f", product.getRating()));
                        tvReviewCount.setText("(" + product.getReviewCount() + " đánh giá)");
                        break;
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Ignore error
            }
        });
    }

    private void updateFavoriteButton() {
        if (fabFavorite != null) {
            int favoriteIcon = product.isFavorite() 
                ? R.drawable.baseline_favorite_24 
                : R.drawable.baseline_favorite_border_24;
            fabFavorite.setImageResource(favoriteIcon);
        }
    }

    // Inner class for Image Gallery Adapter
    private class ImageGalleryAdapter
            extends androidx.recyclerview.widget.RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {
        private List<String> images;

        ImageGalleryAdapter(List<String> images) {
            this.images = images;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            String imageUrl = images.get(position);

            if (imageUrl.startsWith("http")) {
                Glide.with(ProductDetailActivity.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.imageView);
            } else {
                // Load from drawable
                int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
                if (resId != 0) {
                    holder.imageView.setImageResource(resId);
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ImageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(ImageView itemView) {
                super(itemView);
                this.imageView = itemView;
            }
        }
    }
}
