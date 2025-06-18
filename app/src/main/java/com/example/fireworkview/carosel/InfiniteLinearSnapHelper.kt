package com.example.fireworkview.carosel

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class InfiniteLinearSnapHelper(
    private val getCenterPosition: () -> Int,
    private val getRealItemCount: () -> Int
) : LinearSnapHelper() {
    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val centerView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val position = layoutManager.getPosition(centerView)
        val itemCount = layoutManager.itemCount
        val realCount = getRealItemCount()
        val centerPos = getCenterPosition()

        if (position < realCount || position > itemCount - realCount) {
            return centerPos
        }
        return super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
    }
} 