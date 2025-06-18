package com.example.fireworkview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InfiniteHorizontalLayoutManager : LinearLayoutManager {
    
    constructor(context: Context) : super(context, HORIZONTAL, false)
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
        
        if (scrolled != 0 && itemCount > 0) {
            val firstVisible = findFirstVisibleItemPosition()
            val lastVisible = findLastVisibleItemPosition()
            
            // If we scrolled past the end, jump to beginning
            if (dx > 0 && lastVisible >= itemCount - 1) {
                val firstView = findViewByPosition(firstVisible)
                if (firstView != null && firstView.right <= 0) {
                    scrollToPosition(0)
                }
            }
            // If we scrolled past the beginning, jump to end
            else if (dx < 0 && firstVisible <= 0) {
                val lastView = findViewByPosition(lastVisible)
                if (lastView != null && lastView.left >= width) {
                    scrollToPosition(itemCount - 1)
                }
            }
        }
        
        return scrolled
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        
        // Ensure we start at position 0
        if (state.itemCount > 0 && findFirstVisibleItemPosition() == RecyclerView.NO_POSITION) {
            scrollToPosition(0)
        }
    }
} 