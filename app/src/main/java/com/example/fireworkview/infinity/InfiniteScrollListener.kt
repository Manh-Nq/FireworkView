package com.example.fireworkview.infinity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InfiniteScrollListener(private val originalItemCount: Int) : RecyclerView.OnScrollListener() {
    
    companion object {
        private const val MULTIPLIER = 5
        private const val MIN_ITEMS_FOR_INFINITE = 3
    }
    
    init {
        require(originalItemCount >= MIN_ITEMS_FOR_INFINITE) {
            "InfiniteScrollListener requires at least $MIN_ITEMS_FOR_INFINITE items, but got $originalItemCount"
        }
    }
    
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        
        // Only apply infinite scrolling if we have enough items
        if (originalItemCount < MIN_ITEMS_FOR_INFINITE) return
        
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val currentPosition = layoutManager.findFirstVisibleItemPosition()
        
        // If we're at the beginning of the list (first copy), jump to the middle
        if (currentPosition < originalItemCount) {
            recyclerView.post {
                layoutManager.scrollToPosition(originalItemCount * 3)
            }
        }
        // If we're at the end of the list (last copy), jump to the middle
        else if (currentPosition >= originalItemCount * 4) {
            recyclerView.post {
                layoutManager.scrollToPosition(originalItemCount * 2)
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        
        // Only apply infinite scrolling if we have enough items
        if (originalItemCount < MIN_ITEMS_FOR_INFINITE) return
        
        // Handle scroll state changes to ensure smooth infinite scrolling
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val currentPosition = layoutManager.findFirstVisibleItemPosition()
            
            // If we're at the beginning of the list (first copy), jump to the middle
            if (currentPosition < originalItemCount) {
                recyclerView.post {
                    layoutManager.scrollToPosition(originalItemCount * 3)
                }
            }
            // If we're at the end of the list (last copy), jump to the middle
            else if (currentPosition >= originalItemCount * 4) {
                recyclerView.post {
                    layoutManager.scrollToPosition(originalItemCount * 2)
                }
            }
        }
    }
} 