package com.example.fireworkview.carosel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import com.example.fireworkview.R;
import java.util.List;

public class HorizontalCarouselRecyclerViewJava extends RecyclerView {

    private List<Integer> viewsToChangeColor;
    private boolean isInfiniteCarousel = false;
    private SnapHelper snapHelper;
    
    // Callback interface for snap position
    public interface OnSnapPositionChangeListener {
        void onSnapPositionChanged(int position);
    }
    
    private OnSnapPositionChangeListener snapPositionListener;
    private int lastSnappedPosition = -1;
    private boolean isScrolling = false;

    public HorizontalCarouselRecyclerViewJava(@NonNull Context context) {
        super(context);
    }

    public HorizontalCarouselRecyclerViewJava(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalCarouselRecyclerViewJava(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Set listener for snap position changes
     */
    public void setOnSnapPositionChangeListener(OnSnapPositionChangeListener listener) {
        this.snapPositionListener = listener;
    }

    /**
     * Force refresh 3D effects after data changes
     */
    public void forceRefreshEffects() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                onScrollChanged();
            }
        }, 100); // Small delay to ensure layout is complete
    }

    public <T extends ViewHolder> void initialize(Adapter<T> newAdapter) {
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Setup snap helper if not infinite carousel
        if (!isInfiniteCarousel) {
            snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(this);
        }

        newAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        View firstChild = getChildAt(0);
                        int childWidth = firstChild != null ? firstChild.getWidth() : 0;
                        int sidePadding = (getWidth() / 2) - (childWidth / 2);
                        setPadding(sidePadding, 0, sidePadding, 0);

                        if (!isInfiniteCarousel) {
                            scrollToPosition(0);
                        }

                        // Force refresh effects after layout
                        forceRefreshEffects();

                        addOnScrollListener(new OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                
                                switch (newState) {
                                    case SCROLL_STATE_IDLE:
                                        // Check if scrolling has stopped and notify position change
                                        if (isScrolling) {
                                            isScrolling = false;
                                            checkAndNotifySnapPosition();
                                        }
                                        break;
                                    case SCROLL_STATE_DRAGGING:
                                    case SCROLL_STATE_SETTLING:
                                        isScrolling = true;
                                        break;
                                }
                            }
                            
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                onScrollChanged();
                            }
                        });
                        
                        // Trigger initial position callback after layout is complete
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkAndNotifySnapPosition();
                            }
                        }, 200);
                    }
                });
            }
        });
        setAdapter(newAdapter);
    }

    /**
     * Check current snapped position and notify listener if changed
     */
    private void checkAndNotifySnapPosition() {
        int currentPosition = getCurrentSnappedPosition();
        if (currentPosition != lastSnappedPosition) {
            lastSnappedPosition = currentPosition;
            if (snapPositionListener != null) {
                snapPositionListener.onSnapPositionChanged(currentPosition);
            }
        }
    }

    /**
     * Mark this as infinite carousel to avoid conflicts with CenterSnapHelper
     */
    public void setInfiniteCarousel(boolean enabled) {
        isInfiniteCarousel = enabled;

        // Remove snap helper if infinite carousel is enabled
        if (enabled) {
            if (snapHelper != null) {
                snapHelper.attachToRecyclerView(null);
                snapHelper = null;
            }
        } else {
            // Add snap helper if it was removed
            if (snapHelper == null) {
                snapHelper = new PagerSnapHelper();
                snapHelper.attachToRecyclerView(this);
            }
        }
    }

    public void setViewsToChangeColor(List<Integer> viewIds) {
        this.viewsToChangeColor = viewIds;
    }

    private int getCurrentSnappedPosition() {
        if (snapHelper != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            View view = snapHelper.findSnapView(layoutManager);
            return view != null ? layoutManager.getPosition(view) : 0;
        } else {
            // Fallback for infinite carousel
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            return layoutManager != null ? layoutManager.findFirstVisibleItemPosition() : 0;
        }
    }

    public void snapToPosition(int position) {
        if (snapHelper != null) {
            smoothScrollToPosition(position);
        } else {
            scrollToPosition(position);
        }
        // For programmatic snap, check position after a short delay
        postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndNotifySnapPosition();
            }
        }, 300);
    }

    public void snapToNext() {
        int currentPosition = getCurrentSnappedPosition();
        int itemCount = getAdapter() != null ? getAdapter().getItemCount() : 0;
        if (itemCount > 0) {
            int nextPosition = (currentPosition + 1) % itemCount;
            snapToPosition(nextPosition);
        }
    }

    public void snapToPrevious() {
        int currentPosition = getCurrentSnappedPosition();
        int itemCount = getAdapter() != null ? getAdapter().getItemCount() : 0;
        if (itemCount > 0) {
            int previousPosition = currentPosition > 0 ? currentPosition - 1 : itemCount - 1;
            snapToPosition(previousPosition);
        }
    }

    private void onScrollChanged() {
        post(new Runnable() {
            @Override
            public void run() {
                int recyclerCenterX = (getLeft() + getRight()) / 2;
                float maxRotation = 10f;
                float maxDistance = getWidth() / 2f;
                
                for (int position = 0; position < getChildCount(); position++) {
                    View child = getChildAt(position);
                    if (child != null) {
                        int childCenterX = (child.getLeft() + child.getRight()) / 2;
                        float minScale = 0.95f;
                        float scaleOffset = 0.15f;

                        float maxScale = minScale + scaleOffset;
                        float scaleValue = getGaussianScale(childCenterX, minScale, scaleOffset, 300.0);
                        child.setScaleX(scaleValue);
                        child.setScaleY(scaleValue);
                        colorView(child, scaleValue, minScale, maxScale);

                        float distanceFromCenter = (childCenterX - recyclerCenterX);
                        float rotationY = Math.max(-maxRotation, Math.min(maxRotation, 
                            (maxRotation * distanceFromCenter / maxDistance)));
                        child.setRotationY(rotationY);
                    }
                }
            }
        });
    }

    private void colorView(View child, float scaleValue, float minScale, float maxScale) {
        float alpha = convertValue(minScale, maxScale, 0.6f, 1f, scaleValue);
        float alphaImage = convertValue(minScale, maxScale, 1f, 0f, scaleValue);
        float width = convertValue(minScale, maxScale, 4f, 0f, scaleValue);

        if (viewsToChangeColor != null) {
            for (Integer viewId : viewsToChangeColor) {
                if (viewId == R.id.left_3d_icon || viewId == R.id.right_3d_icon) {
                    ImageView view = child.findViewById(viewId);
                    if (view != null) {
                        ViewGroup.LayoutParams params = view.getLayoutParams();
                        params.width = (int) convertDpToPixel(width, getContext());
                        view.setLayoutParams(params);
                        view.setAlpha(alphaImage);
                    }
                } else {
                    View viewToChangeColor = child.findViewById(viewId);
                    if (viewToChangeColor instanceof ImageView) {
                        ((ImageView) viewToChangeColor).setImageAlpha((int) (255 * alpha));
                    }
                }
            }
        }
    }

    private float getGaussianScale(int childCenterX, float minScaleOffset, float scaleFactor, double spreadFactor) {
        int recyclerCenterX = (getLeft() + getRight()) / 2;
        double distance = childCenterX - recyclerCenterX;
        double exponent = -(distance * distance) / (2 * spreadFactor * spreadFactor);
        return (float) (Math.exp(exponent) * scaleFactor + minScaleOffset);
    }

    public float convertValue(float min1, float max1, float min2, float max2, float value) {
        return ((value - min1) * ((max2 - min2) / (max1 - min1)) + min2);
    }

    public float convertDpToPixel(float dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT);
    }

    public void resetSnapPositionState() {
        lastSnappedPosition = -1;
        isScrolling = false;
    }

    public void triggerInitialPositionCallback() {
        post(new Runnable() {
            @Override
            public void run() {
                checkAndNotifySnapPosition();
            }
        });
    }
} 