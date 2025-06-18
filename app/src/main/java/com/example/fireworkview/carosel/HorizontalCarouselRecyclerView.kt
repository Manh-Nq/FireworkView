package com.example.fireworkview.carosel


import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.fireworkview.R
import kotlin.math.pow

class HorizontalCarouselRecyclerView(
    context: Context,
    attrs: AttributeSet
) : RecyclerView(context, attrs) {

    private val activeColor by lazy { ContextCompat.getColor(context, R.color.blue) }
    private val inactiveColor by lazy { ContextCompat.getColor(context, R.color.gray) }
    private var viewsToChangeColor: List<Int> = listOf()
    private var isInfiniteCarousel = false
    private var snapHelper: SnapHelper? = null

    fun <T : ViewHolder> initialize(newAdapter: Adapter<T>) {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        // Setup snap helper if not infinite carousel
        if (!isInfiniteCarousel) {
            snapHelper = PagerSnapHelper()
            snapHelper?.attachToRecyclerView(this)
        }

        newAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                post {
                    val sidePadding = (width / 2) - (getChildAt(0)?.width ?: 0) / 2
                    setPadding(sidePadding, 0, sidePadding, 0)

                    if (!isInfiniteCarousel) {
                        scrollToPosition(0)
                    }

                    addOnScrollListener(object : OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            onScrollChanged()
                        }
                    })
                }
            }
        })
        adapter = newAdapter
    }

    /**
     * Đánh dấu đây là infinite carousel để tránh xung đột với CenterSnapHelper
     */
    fun setInfiniteCarousel(enabled: Boolean) {
        isInfiniteCarousel = enabled

        // Remove snap helper if infinite carousel is enabled
        if (enabled) {
            snapHelper?.attachToRecyclerView(null)
            snapHelper = null
        } else {
            // Add snap helper if it was removed
            if (snapHelper == null) {
                snapHelper = PagerSnapHelper()
                snapHelper?.attachToRecyclerView(this)
            }
        }
    }

    fun setViewsToChangeColor(viewIds: List<Int>) {
        viewsToChangeColor = viewIds
    }

    /**
     * Lấy vị trí item đang được snap (center)
     */
    fun getCurrentSnappedPosition(): Int {
        return if (snapHelper != null) {
            val layoutManager = layoutManager as? LinearLayoutManager
            val view = snapHelper?.findSnapView(layoutManager)
            view?.let { layoutManager?.getPosition(it) } ?: 0
        } else {
            // Fallback cho infinite carousel
            val layoutManager = layoutManager as? LinearLayoutManager
            layoutManager?.findFirstVisibleItemPosition() ?: 0
        }
    }

    /**
     * Snap đến vị trí cụ thể
     */
    fun snapToPosition(position: Int) {
        if (snapHelper != null) {
            smoothScrollToPosition(position)
        } else {
            scrollToPosition(position)
        }
    }

    /**
     * Snap đến item tiếp theo
     */
    fun snapToNext() {
        val currentPosition = getCurrentSnappedPosition()
        val itemCount = adapter?.itemCount ?: 0
        if (itemCount > 0) {
            val nextPosition = (currentPosition + 1) % itemCount
            snapToPosition(nextPosition)
        }
    }

    /**
     * Snap đến item trước đó
     */
    fun snapToPrevious() {
        val currentPosition = getCurrentSnappedPosition()
        val itemCount = adapter?.itemCount ?: 0
        if (itemCount > 0) {
            val previousPosition = if (currentPosition > 0) currentPosition - 1 else itemCount - 1
            snapToPosition(previousPosition)
        }
    }

    private fun onScrollChanged() {
        post {
            val recyclerCenterX = (left + right) / 2
            val maxRotation = 10f
            val maxDistance = width / 2f
            (0 until childCount).forEach { position ->
                val child = getChildAt(position)
                if (child != null) {
                    val childCenterX = (child.left + child.right) / 2
                    val scaleValue = getGaussianScale(childCenterX, 0.95f, 0.15f, 300.0)
                    child.scaleX = scaleValue
                    child.scaleY = scaleValue
                    colorView(child, scaleValue)

                    val distanceFromCenter = (childCenterX - recyclerCenterX).toFloat()
                    val rotationY = (maxRotation * distanceFromCenter / maxDistance).coerceIn(-maxRotation, maxRotation)
                    child.rotationY = rotationY
                }
            }
        }
    }

    private fun colorView(child: View, scaleValue: Float) {
        val saturationPercent = (scaleValue - 1) / 1f
        val alphaPercent = scaleValue / 2f
        val matrix = ColorMatrix()
        matrix.setSaturation(saturationPercent)

        viewsToChangeColor.forEach { viewId ->
            val viewToChangeColor = child.findViewById<View>(viewId)
            when (viewToChangeColor) {
                is ImageView -> {
                    viewToChangeColor.colorFilter = ColorMatrixColorFilter(matrix)
//                    viewToChangeColor.imageAlpha = (255 * alphaPercent).toInt()
                }

                is TextView -> {
                    val textColor = ArgbEvaluator().evaluate(saturationPercent, inactiveColor, activeColor) as Int
                    viewToChangeColor.setTextColor(textColor)
                }

                is View -> {
                    viewToChangeColor.background.colorFilter = ColorMatrixColorFilter(matrix)
                    viewToChangeColor.alpha = alphaPercent
                }
            }
        }
    }

    private fun getGaussianScale(
        childCenterX: Int,
        minScaleOffest: Float,
        scaleFactor: Float,
        spreadFactor: Double
    ): Float {
        val recyclerCenterX = (left + right) / 2
        return (Math.E.pow(
            -(childCenterX - recyclerCenterX.toDouble()).pow(2.toDouble()) / (2 * spreadFactor.pow(2.toDouble()))
        ) * scaleFactor + minScaleOffest).toFloat()
    }

}