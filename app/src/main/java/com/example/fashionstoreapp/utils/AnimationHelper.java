package com.example.fashionstoreapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.fashionstoreapp.R;

public class AnimationHelper {
    
    /**
     * Apply scale down animation when button is pressed
     */
    public static void animateButtonPress(View view, Runnable action) {
        Animation scaleDown = AnimationUtils.loadAnimation(view.getContext(), R.anim.scale_down);
        Animation scaleUp = AnimationUtils.loadAnimation(view.getContext(), R.anim.scale_up);
        
        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleUp);
                if (action != null) {
                    action.run();
                }
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        view.startAnimation(scaleDown);
    }
    
    /**
     * Apply bounce animation to view
     */
    public static void animateBounceIn(View view) {
        Animation bounce = AnimationUtils.loadAnimation(view.getContext(), R.anim.bounce_in);
        view.startAnimation(bounce);
    }
    
    /**
     * Apply fade in scale animation
     */
    public static void animateFadeInScale(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in_scale);
        view.startAnimation(fadeIn);
    }
    
    /**
     * Apply slide in from left animation
     */
    public static void animateSlideInLeft(View view) {
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_left);
        view.startAnimation(slideIn);
    }
    
    /**
     * Apply zoom in with rotation animation
     */
    public static void animateZoomInRotate(View view) {
        Animation zoom = AnimationUtils.loadAnimation(view.getContext(), R.anim.zoom_in_rotate);
        view.startAnimation(zoom);
    }
    
    /**
     * Apply shake animation
     */
    public static void animateShake(View view) {
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake);
        view.startAnimation(shake);
    }
    
    /**
     * Apply scale up small animation
     */
    public static void animateScaleUpSmall(View view) {
        Animation scaleUp = AnimationUtils.loadAnimation(view.getContext(), R.anim.scale_up_small);
        view.startAnimation(scaleUp);
    }
    
    /**
     * Apply rotation animation
     */
    public static void animateRotate360(View view) {
        Animation rotate = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_360);
        view.startAnimation(rotate);
    }
    
    /**
     * Apply pulse animation using ObjectAnimator
     */
    public static void animatePulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f, 1.0f);
        
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setRepeatCount(1);
        scaleY.setRepeatCount(1);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());
        
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * Apply fade in animation
     */
    public static void animateFadeIn(View view) {
        view.setAlpha(0f);
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    /**
     * Apply slide up animation
     */
    public static void animateSlideUp(View view) {
        view.setTranslationY(100f);
        view.setAlpha(0f);
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    /**
     * Apply scale animation with callback
     */
    public static void animateScale(View view, float fromScale, float toScale, Runnable onEnd) {
        view.setScaleX(fromScale);
        view.setScaleY(fromScale);
        view.animate()
            .scaleX(toScale)
            .scaleY(toScale)
            .setDuration(200)
            .setInterpolator(new OvershootInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onEnd != null) {
                        onEnd.run();
                    }
                }
            })
            .start();
    }
}
