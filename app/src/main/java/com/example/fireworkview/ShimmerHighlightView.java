package com.example.fireworkview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public class ShimmerHighlightView extends View {
    
    public interface ShimmerCompletionListener {
        void onShimmerCompleted();
    }
    
    private Paint paint;
    private LinearGradient shimmerGradient;
    private Matrix gradientMatrix;
    private ValueAnimator shimmerAnimator;
    private ShimmerCompletionListener completionListener;
    
    // Background properties
    private Drawable backgroundDrawable;
    private int backgroundColor = Color.TRANSPARENT;
    
    // Shimmer properties
    private float shimmerWidth = 0.5f; // Width of shimmer as fraction of view width
    private int shimmerColor = Color.WHITE;
    private float shimmerAlpha = 0.8f;
    private long shimmerDuration = 1500; // Animation duration in milliseconds
    
    // Animation state
    private float shimmerOffset = 0f;
    private boolean isShimmering = false;
    private boolean isPlayingOnce = false;
    
    public ShimmerHighlightView(Context context) {
        super(context);
        init();
    }
    
    public ShimmerHighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ShimmerHighlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        
        gradientMatrix = new Matrix();
        
        // Create shimmer animator
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(shimmerDuration);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        shimmerAnimator.addUpdateListener(animation -> {
            shimmerOffset = (Float) animation.getAnimatedValue();
            invalidate();
        });
        
        shimmerAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isShimmering = true;
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                isShimmering = false;
                if (isPlayingOnce && completionListener != null) {
                    completionListener.onShimmerCompleted();
                    isPlayingOnce = false;
                }
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createShimmerGradient(w, h);
    }
    
    private void createShimmerGradient(int width, int height) {
        if (width <= 0 || height <= 0) return;
        
        float shimmerWidthPixels = width * shimmerWidth;
        
        // Calculate diagonal distance for shimmer effect
        float diagonalDistance = (float) Math.sqrt(width * width + height * height);
        float shimmerWidthDiagonal = diagonalDistance * shimmerWidth;
        
        shimmerGradient = new LinearGradient(
            -shimmerWidthDiagonal, -shimmerWidthDiagonal,
            shimmerWidthDiagonal, shimmerWidthDiagonal,
            new int[]{
                Color.TRANSPARENT,
                Color.argb((int) (255 * shimmerAlpha), Color.red(shimmerColor), Color.green(shimmerColor), Color.blue(shimmerColor)),
                Color.TRANSPARENT
            },
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        
        paint.setShader(shimmerGradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (shimmerGradient == null) return;
        
        // Draw background based on settings
        if (backgroundDrawable != null) {
            // Draw custom drawable background
            backgroundDrawable.setBounds(0, 0, getWidth(), getHeight());
            backgroundDrawable.draw(canvas);
        } else if (backgroundColor != Color.TRANSPARENT) {
            // Draw solid color background only if not transparent
            paint.setShader(null);
            paint.setColor(backgroundColor);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
        // If backgroundColor is Color.TRANSPARENT, don't draw anything
        
        // Draw shimmer effect
        if (isShimmering) {
            paint.setShader(shimmerGradient);
            
            // Calculate diagonal shimmer position
            float diagonalDistance = (float) Math.sqrt(getWidth() * getWidth() + getHeight() * getHeight());
            float shimmerWidthDiagonal = diagonalDistance * shimmerWidth;
            float translateX = (getWidth() + shimmerWidthDiagonal) * shimmerOffset - shimmerWidthDiagonal;
            float translateY = (getHeight() + shimmerWidthDiagonal) * shimmerOffset - shimmerWidthDiagonal;
            
            gradientMatrix.setTranslate(translateX, translateY);
            shimmerGradient.setLocalMatrix(gradientMatrix);
            
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
    }
    
    /**
     * Start the shimmer animation
     */
    public void startShimmer() {
        if (!isShimmering) {
            shimmerAnimator.start();
        }
    }
    
    /**
     * Stop the shimmer animation
     */
    public void stopShimmer() {
        if (isShimmering) {
            shimmerAnimator.cancel();
        }
    }
    
    /**
     * Set shimmer color
     */
    public void setShimmerColor(int color) {
        this.shimmerColor = color;
        if (getWidth() > 0 && getHeight() > 0) {
            createShimmerGradient(getWidth(), getHeight());
        }
    }
    
    /**
     * Set shimmer width as fraction of view width (0.0 to 1.0)
     */
    public void setShimmerWidth(float width) {
        this.shimmerWidth = Math.max(0.1f, Math.min(1.0f, width));
        if (getWidth() > 0 && getHeight() > 0) {
            createShimmerGradient(getWidth(), getHeight());
        }
    }
    
    /**
     * Set shimmer alpha (0.0 to 1.0)
     */
    public void setShimmerAlpha(float alpha) {
        this.shimmerAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        if (getWidth() > 0 && getHeight() > 0) {
            createShimmerGradient(getWidth(), getHeight());
        }
    }
    
    /**
     * Set animation duration in milliseconds
     */
    public void setShimmerDuration(long duration) {
        this.shimmerDuration = duration;
        shimmerAnimator.setDuration(duration);
    }
    
    /**
     * Check if shimmer is currently running
     */
    public boolean isShimmering() {
        return isShimmering;
    }
    
    /**
     * Play shimmer animation once and call completion listener when done
     */
    public void playShimmerOnce() {
        playShimmerOnce(null);
    }
    
    /**
     * Play shimmer animation once with custom completion listener
     */
    public void playShimmerOnce(ShimmerCompletionListener listener) {
        if (isShimmering) {
            stopShimmer();
        }
        
        this.completionListener = listener;
        this.isPlayingOnce = true;
        
        // Create a one-time animator
        ValueAnimator oneTimeAnimator = ValueAnimator.ofFloat(0f, 1f);
        oneTimeAnimator.setDuration(shimmerDuration);
        oneTimeAnimator.setRepeatCount(0); // No repeat for one-time play
        
        oneTimeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shimmerOffset = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        
        oneTimeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isShimmering = true;
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                isShimmering = false;
                if (completionListener != null) {
                    completionListener.onShimmerCompleted();
                }
                isPlayingOnce = false;
            }
        });
        
        oneTimeAnimator.start();
    }
    
    /**
     * Set shimmer completion listener
     */
    public void setShimmerCompletionListener(ShimmerCompletionListener listener) {
        this.completionListener = listener;
    }
    
    /**
     * Set background drawable
     */
    public void setBackgroundDrawable(Drawable drawable) {
        this.backgroundDrawable = drawable;
        invalidate();
    }
    
    /**
     * Set background drawable by resource ID
     */
    public void setBackgroundDrawable(int resourceId) {
        this.backgroundDrawable = getContext().getDrawable(resourceId);
        invalidate();
    }
    
    /**
     * Set background - works like standard Android View background
     */
    @Override
    public void setBackground(Drawable background) {
        this.backgroundDrawable = background;
        invalidate();
    }
    
    /**
     * Set background color
     */
    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        this.backgroundDrawable = null;
        invalidate();
    }
    
    /**
     * Set background resource
     */
    @Override
    public void setBackgroundResource(int resid) {
        this.backgroundDrawable = getContext().getDrawable(resid);
        invalidate();
    }
    
    /**
     * Get current background color
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }
    
    /**
     * Debug method to check shimmer status
     */
    public String getShimmerDebugInfo() {
        return "Shimmering: " + isShimmering + 
               ", Offset: " + shimmerOffset + 
               ", Background: " + (backgroundColor == Color.TRANSPARENT ? "TRANSPARENT" : "COLOR") +
               ", Gradient: " + (shimmerGradient != null ? "CREATED" : "NULL");
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
    }
}