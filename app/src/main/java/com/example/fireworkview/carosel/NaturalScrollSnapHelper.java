package com.example.fireworkview.carosel;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/**
 * Custom SnapHelper that allows natural scrolling based on velocity
 * but snaps to center when scrolling stops
 */
public class NaturalScrollSnapHelper extends SnapHelper {
    
    private OrientationHelper mVerticalHelper;
    private OrientationHelper mHorizontalHelper;

    @Override
    public int[] calculateDistanceToFinalSnap(RecyclerView.LayoutManager layoutManager, View targetView) {
        int[] out = new int[2];
        
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(targetView, getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(targetView, getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        
        return out;
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager));
        } else if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager));
        }
        return null;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return RecyclerView.NO_POSITION;
        }

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        int itemCount = linearLayoutManager.getItemCount();
        
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        // Get current visible position
        int currentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        // Calculate how many items to scroll based on velocity
        int itemsToScroll = calculateItemsToScroll(velocityX, velocityY);
        
        // Calculate target position
        int targetPosition;
        if (layoutManager.canScrollHorizontally()) {
            if (velocityX > 0) {
                // Scrolling right
                targetPosition = Math.min(currentPosition + itemsToScroll, itemCount - 1);
            } else if (velocityX < 0) {
                // Scrolling left
                targetPosition = Math.max(currentPosition - itemsToScroll, 0);
            } else {
                // No velocity, snap to current center
                targetPosition = currentPosition;
            }
        } else if (layoutManager.canScrollVertically()) {
            if (velocityY > 0) {
                // Scrolling down
                targetPosition = Math.min(currentPosition + itemsToScroll, itemCount - 1);
            } else if (velocityY < 0) {
                // Scrolling up
                targetPosition = Math.max(currentPosition - itemsToScroll, 0);
            } else {
                // No velocity, snap to current center
                targetPosition = currentPosition;
            }
        } else {
            targetPosition = currentPosition;
        }

        return targetPosition;
    }

    private int calculateItemsToScroll(int velocityX, int velocityY) {
        int velocity = Math.max(Math.abs(velocityX), Math.abs(velocityY));
        
        // Define velocity thresholds for different scroll distances
        if (velocity > 3000) {
            return 4; // Very fast scroll - 4 items
        } else if (velocity > 2000) {
            return 3; // Fast scroll - 3 items
        } else if (velocity > 1000) {
            return 2; // Medium scroll - 2 items
        } else {
            return 1; // Slow scroll - 1 item
        }
    }

    private int distanceToCenter(View targetView, OrientationHelper helper) {
        return helper.getDecoratedStart(targetView) + (helper.getDecoratedMeasurement(targetView) / 2) - (helper.getStartAfterPadding() + helper.getTotalSpace() / 2);
    }

    private View findCenterView(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        int center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child) + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }

        return closestChild;
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }
} 