package com.example.fireworkview.carosel


import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
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

    /**
     * Force refresh 3D effects after data changes
     */
    fun forceRefreshEffects() {
        postDelayed({
            onScrollChanged()
        }, 100) // Small delay to ensure layout is complete
    }

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

                    // Force refresh effects after layout
                    forceRefreshEffects()

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

    fun snapToPosition(position: Int) {
        if (snapHelper != null) {
            smoothScrollToPosition(position)
        } else {
            scrollToPosition(position)
        }
    }


    fun snapToNext() {
        val currentPosition = getCurrentSnappedPosition()
        val itemCount = adapter?.itemCount ?: 0
        if (itemCount > 0) {
            val nextPosition = (currentPosition + 1) % itemCount
            snapToPosition(nextPosition)
        }
    }

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
            val maxRotation = 15f
            val maxDistance = width / 2f
            (0 until childCount).forEach { position ->
                val child = getChildAt(position)
                if (child != null) {
                    val childCenterX = (child.left + child.right) / 2
                    val minScale = 0.95f
                    val scaleOffset = 0.2f

                    val maxScale = minScale + scaleOffset
                    val scaleValue = getGaussianScale(childCenterX, minScale, scaleOffset, 300.0)
                    child.scaleX = scaleValue
                    child.scaleY = scaleValue
                    colorView(child, scaleValue, minScale, maxScale)

                    val distanceFromCenter = (childCenterX - recyclerCenterX).toFloat()
                    val rotationY = (maxRotation * distanceFromCenter / maxDistance).coerceIn(-maxRotation, maxRotation)
                    child.rotationY = rotationY

                }
            }
        }
    }

    private fun colorView(child: View, scaleValue: Float, minScale: Float, maxScale: Float) {

        val alpha = convertValue(minScale, maxScale, 0.6f, 1f, scaleValue)
        val alphaImage = convertValue(minScale, maxScale, 1f, 0f, scaleValue)
        val width = convertValue(minScale, maxScale, 8f, 0f, scaleValue)

        Log.d("ManhNQ", "colorView: $alphaImage")

        viewsToChangeColor.forEach { viewId ->
            if (viewId == R.id.left_3d_icon || viewId == R.id.right_3d_icon) {
                val view = child.findViewById<ImageView>(viewId)
                val params = view.layoutParams
                params.width = convertDpToPixel(width, context).toInt()
                view.layoutParams = params

                view.alpha = alphaImage
            } else {
                val viewToChangeColor = child.findViewById<View>(viewId)
                if (viewToChangeColor is ImageView) {
                    viewToChangeColor.imageAlpha = (255 * alpha).toInt()
                }
            }

            /* val viewToChangeColor = child.findViewById<View>(viewId)
             when (viewToChangeColor) {
                 is ImageView -> {
                     viewToChangeColor.imageAlpha = (255 * alpha).toInt()
                 }
             }*/
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

    fun convertValue(min1: Float, max1: Float, min2: Float, max2: Float, value: Float): Float {
        return ((value - min1) * ((max2 - min2) * 1f / (max1 - min1)) + min2)
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

}