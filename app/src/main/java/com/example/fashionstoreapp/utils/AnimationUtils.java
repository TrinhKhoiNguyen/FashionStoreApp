package com.example.fashionstoreapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

/**
 * Utility class for custom animations
 */
public class AnimationUtils {

    /**
     * Create fly-to-cart animation
     * 
     * @param activity    The activity context
     * @param productView The product view to animate
     * @param cartIcon    The cart icon destination
     * @param onComplete  Callback when animation completes
     */
    public static void flyToCart(Activity activity, View productView, View cartIcon, Runnable onComplete) {
        if (productView == null || cartIcon == null) {
            if (onComplete != null)
                onComplete.run();
            return;
        }

        // Get root view
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);

        // Create flying image
        ImageView flyingImage = new ImageView(activity);
        flyingImage.setLayoutParams(new ViewGroup.LayoutParams(
                productView.getWidth() / 2,
                productView.getHeight() / 2));

        // Capture product view as bitmap
        Bitmap bitmap = createBitmapFromView(productView);
        flyingImage.setImageBitmap(bitmap);

        // Get start and end positions
        int[] startPos = new int[2];
        int[] endPos = new int[2];
        productView.getLocationOnScreen(startPos);
        cartIcon.getLocationOnScreen(endPos);

        // Adjust for smaller image size
        startPos[0] += productView.getWidth() / 4;
        startPos[1] += productView.getHeight() / 4;

        flyingImage.setX(startPos[0]);
        flyingImage.setY(startPos[1]);
        flyingImage.setAlpha(0f);

        // Add to root view
        rootView.addView(flyingImage);

        // Create path animation
        ObjectAnimator animX = ObjectAnimator.ofFloat(flyingImage, "x", startPos[0], endPos[0]);
        ObjectAnimator animY = ObjectAnimator.ofFloat(flyingImage, "y", startPos[1], endPos[1]);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(flyingImage, "alpha", 0f, 1f, 1f, 0.5f);
        ObjectAnimator animScale = ObjectAnimator.ofFloat(flyingImage, "scaleX", 1f, 0.3f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(flyingImage, "scaleY", 1f, 0.3f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY, animAlpha, animScale, animScaleY);
        animSet.setDuration(600);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootView.removeView(flyingImage);

                // Bounce cart icon
                bounceView(cartIcon);

                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        animSet.start();
    }

    /**
     * Create bounce effect for a view
     */
    public static void bounceView(View view) {
        if (view == null)
            return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 0.9f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 0.9f, 1.1f, 1f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.setDuration(500);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.start();
    }

    /**
     * Pulse animation for views
     */
    public static void pulseView(View view) {
        if (view == null)
            return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.setDuration(300);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.start();
    }

    /**
     * Fade in animation
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    /**
     * Fade out animation
     */
    public static void fadeOut(View view, long duration, Runnable onComplete) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    /**
     * Slide in from bottom
     */
    public static void slideInFromBottom(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);
    }

    /**
     * Slide out to bottom
     */
    public static void slideOutToBottom(View view, long duration, Runnable onComplete) {
        view.animate()
                .translationY(view.getHeight())
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    /**
     * Create bitmap from view
     */
    private static Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Shake animation for error feedback
     */
    public static void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }
}
