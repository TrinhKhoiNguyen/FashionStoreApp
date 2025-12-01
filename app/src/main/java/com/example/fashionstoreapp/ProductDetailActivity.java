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

import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.adapters.ReviewAdapter;
import com.example.fashionstoreapp.adapters.ReviewImageAdapter;
import com.example.fashionstoreapp.adapters.ProductImageAdapter;
import com.example.fashionstoreapp.adapters.RecentProductAdapter;
import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.Review;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.example.fashionstoreapp.utils.RecentViewManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.example.fashionstoreapp.adapters.ProductAdapter;

public class ProductDetailActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

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
    private ImageView btnBack, btnFavorite;

    // Related Products
    private RecyclerView relatedProductsRecyclerView;
    private ProgressBar relatedProductsLoading;
    private TextView tvNoRelatedProducts;
    private ProductAdapter relatedProductsAdapter;

    // Recently Viewed Products
    private RecyclerView recentProductsRecyclerView;
    private TextView tvRecentProductsTitle;
    private RecentProductAdapter recentProductsAdapter;
    private RecentViewManager recentViewManager;

    private Product product;
    private String selectedSize = "M"; // Default size
    private List<String> productImages;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews;
    private FirestoreManager firestoreManager;

    // Auto-slide for images
    private Handler autoSlideHandler;
    private Runnable autoSlideRunnable;
    private static final long AUTO_SLIDE_DELAY = 3000; // 3 seconds

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

        // Initialize RecentViewManager
        recentViewManager = RecentViewManager.getInstance(this);

        // Initialize image picker
        setupImagePicker();

        // Get product from intent
        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Save to recent viewed
        recentViewManager.addRecentProduct(product.getId());

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
        btnFavorite = findViewById(R.id.btnFavorite);

        // Related Products
        relatedProductsRecyclerView = findViewById(R.id.relatedProductsRecyclerView);
        relatedProductsLoading = findViewById(R.id.relatedProductsLoading);
        tvNoRelatedProducts = findViewById(R.id.tvNoRelatedProducts);

        // Recent Products
        recentProductsRecyclerView = findViewById(R.id.recentProductsRecyclerView);
        tvRecentProductsTitle = findViewById(R.id.tvRecentProductsTitle);
    }

    private void setupImageGallery() {
        // Load multiple images from Firebase or use single image
        productImages = product.getImageUrls();
        if (productImages == null || productImages.isEmpty()) {
            // Fallback to default
            productImages = new ArrayList<>();
            productImages.add("product1");
        }

        // Setup ViewPager2 with zoom-enabled adapter
        ProductImageAdapter adapter = new ProductImageAdapter(this, productImages);
        imageViewPager.setAdapter(adapter);

        // Setup indicators if multiple images
        if (productImages.size() > 1) {
            setupImageIndicators(productImages.size());
            imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateImageIndicators(position);
                    // Reset auto-slide timer when user manually swipes
                    resetAutoSlide();
                }
            });

            // Start auto-slide
            startAutoSlide();
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

    private void startAutoSlide() {
        if (productImages == null || productImages.size() <= 1) {
            return;
        }

        // Stop existing handler if any
        stopAutoSlide();

        autoSlideHandler = new Handler(Looper.getMainLooper());
        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                // Check if activity is still alive
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                // Check if ViewPager is still valid
                if (imageViewPager == null || productImages == null || productImages.isEmpty()) {
                    return;
                }

                int currentItem = imageViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % productImages.size();
                imageViewPager.setCurrentItem(nextItem, true);

                // Only continue if handler is not null
                if (autoSlideHandler != null) {
                    autoSlideHandler.postDelayed(this, AUTO_SLIDE_DELAY);
                }
            }
        };
        autoSlideHandler.postDelayed(autoSlideRunnable, AUTO_SLIDE_DELAY);
    }

    private void resetAutoSlide() {
        stopAutoSlide();
        startAutoSlide();
    }

    private void stopAutoSlide() {
        if (autoSlideHandler != null && autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
            autoSlideHandler = null;
            autoSlideRunnable = null;
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

        // Load Related Products
        loadRelatedProducts();

        // Load Recently Viewed Products
        loadRecentProducts();
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

        btnFavorite.setOnClickListener(v -> {
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
        });

        btnAddToCart.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(product, 1, selectedSize, "");
            CartManager.getInstance().addItem(cartItem);

            // Animate add to cart
            View cartIconView = findViewById(R.id.btnFavorite).getRootView().findViewById(R.id.cartIcon);
            if (cartIconView != null) {
                com.example.fashionstoreapp.utils.AnimationUtils.flyToCart(
                        this,
                        imageViewPager,
                        cartIconView,
                        () -> Toast
                                .makeText(this, "Đã thêm vào giỏ hàng (Size: " + selectedSize + ")", Toast.LENGTH_SHORT)
                                .show());
            } else {
                Toast.makeText(this, "Đã thêm vào giỏ hàng (Size: " + selectedSize + ")", Toast.LENGTH_SHORT).show();
            }
        });

        btnBuyNow.setOnClickListener(v -> {
            // Create a single cart item and open Checkout for a single-item purchase flow
            CartItem cartItem = new CartItem(product, 1, selectedSize, "");
            android.content.Intent intent = new android.content.Intent(ProductDetailActivity.this,
                    CheckoutActivity.class);
            intent.putExtra("single_item", cartItem);
            startActivity(intent);
        });

        btnWriteReview.setOnClickListener(v -> {
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
        if (product.isFavorite()) {
            btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
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

    // Load Related Products
    private void loadRelatedProducts() {
        if (product == null || product.getCategory() == null) {
            tvNoRelatedProducts.setVisibility(View.VISIBLE);
            return;
        }

        relatedProductsLoading.setVisibility(View.VISIBLE);
        tvNoRelatedProducts.setVisibility(View.GONE);

        // Load products from same category
        firestoreManager.loadProductsByCategory(product.getCategory(), new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                relatedProductsLoading.setVisibility(View.GONE);

                // Filter out current product and limit to 6 items
                List<Product> relatedProducts = new ArrayList<>();
                for (Product p : products) {
                    if (!p.getId().equals(product.getId())) {
                        relatedProducts.add(p);
                        if (relatedProducts.size() >= 6)
                            break;
                    }
                }

                if (relatedProducts.isEmpty()) {
                    tvNoRelatedProducts.setVisibility(View.VISIBLE);
                    relatedProductsRecyclerView.setVisibility(View.GONE);
                } else {
                    tvNoRelatedProducts.setVisibility(View.GONE);
                    relatedProductsRecyclerView.setVisibility(View.VISIBLE);

                    // Setup RecyclerView with GridLayoutManager
                    androidx.recyclerview.widget.GridLayoutManager layoutManager = new androidx.recyclerview.widget.GridLayoutManager(
                            ProductDetailActivity.this, 2);
                    relatedProductsRecyclerView.setLayoutManager(layoutManager);

                    relatedProductsAdapter = new ProductAdapter(ProductDetailActivity.this,
                            relatedProducts, ProductDetailActivity.this);
                    relatedProductsRecyclerView.setAdapter(relatedProductsAdapter);
                }
            }

            @Override
            public void onError(String error) {
                relatedProductsLoading.setVisibility(View.GONE);
                tvNoRelatedProducts.setVisibility(View.VISIBLE);
                relatedProductsRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    // Load Recently Viewed Products
    private void loadRecentProducts() {
        List<String> recentIds = recentViewManager.getRecentProductIds();

        // Filter out current product and limit to 10
        List<String> filteredIds = new ArrayList<>();
        for (String id : recentIds) {
            if (!id.equals(product.getId())) {
                filteredIds.add(id);
                if (filteredIds.size() >= 10)
                    break;
            }
        }

        if (filteredIds.isEmpty()) {
            tvRecentProductsTitle.setVisibility(View.GONE);
            recentProductsRecyclerView.setVisibility(View.GONE);
            return;
        }

        // Load products from Firestore
        List<Product> recentProducts = new ArrayList<>();
        final int[] loadedCount = { 0 };

        for (String productId : filteredIds) {
            firestoreManager.loadProducts(new FirestoreManager.OnProductsLoadedListener() {
                @Override
                public void onProductsLoaded(List<Product> products) {
                    for (Product p : products) {
                        if (p.getId().equals(productId)) {
                            recentProducts.add(p);
                            break;
                        }
                    }
                    loadedCount[0]++;

                    // When all loaded, display
                    if (loadedCount[0] == filteredIds.size()) {
                        displayRecentProducts(recentProducts);
                    }
                }

                @Override
                public void onError(String error) {
                    loadedCount[0]++;
                    if (loadedCount[0] == filteredIds.size()) {
                        displayRecentProducts(recentProducts);
                    }
                }
            });
        }
    }

    private void displayRecentProducts(List<Product> recentProducts) {
        if (recentProducts.isEmpty()) {
            tvRecentProductsTitle.setVisibility(View.GONE);
            recentProductsRecyclerView.setVisibility(View.GONE);
            return;
        }

        tvRecentProductsTitle.setVisibility(View.VISIBLE);
        recentProductsRecyclerView.setVisibility(View.VISIBLE);

        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recentProductsRecyclerView.setLayoutManager(layoutManager);

        recentProductsAdapter = new RecentProductAdapter(this, recentProducts, recentProduct -> {
            // Open product detail
            Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
            intent.putExtra("product", recentProduct);
            startActivity(intent);
            finish();
        });
        recentProductsRecyclerView.setAdapter(recentProductsAdapter);
    }

    // ProductAdapter.OnProductClickListener implementation
    @Override
    public void onProductClick(Product product) {
        // Reload this activity with new product
        Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
        finish(); // Close current activity to avoid stack buildup
    }

    @Override
    public void onAddToCartClick(Product product) {
        // Add to cart functionality
        CartManager cartManager = CartManager.getInstance();
        CartItem cartItem = new CartItem(product, 1, selectedSize, "Default");
        cartManager.addItem(cartItem);
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Product product) {
        // Handle favorite toggle
        product.setFavorite(!product.isFavorite());
        updateFavoriteIcon();

        // Save to Firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String productId = product.getId();

            if (product.isFavorite()) {
                firestoreManager.saveFavorite(userId, productId, new FirestoreManager.OnFavoriteSavedListener() {
                    @Override
                    public void onFavoriteSaved() {
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                firestoreManager.removeFavorite(userId, productId, new FirestoreManager.OnFavoriteRemovedListener() {
                    @Override
                    public void onFavoriteRemoved() {
                        Toast.makeText(ProductDetailActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void updateFavoriteIcon() {
        if (product.isFavorite()) {
            btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
            btnFavorite.setColorFilter(0xFFFF0000);
        } else {
            btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
            btnFavorite.setColorFilter(0xFFFFFFFF);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide(); // Stop auto-slide when activity is paused
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productImages != null && productImages.size() > 1) {
            startAutoSlide(); // Resume auto-slide when activity is resumed
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoSlide(); // Clean up when activity is destroyed
    }
}
