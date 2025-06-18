package com.example.fireworkview.carosel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fireworkview.databinding.ListItemBinding

class InfiniteCarouselAdapter(
    private val itemClick: (position: Int, item: Item) -> Unit
) : RecyclerView.Adapter<InfiniteCarouselViewHolder>() {

    private var originalItems: List<Item> = listOf()
    private var infiniteItems: List<Item> = listOf()
    
    companion object {
        private const val MULTIPLIER = 3 // Số lần nhân đôi dữ liệu để tạo hiệu ứng vô hạn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfiniteCarouselViewHolder =
        InfiniteCarouselViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: InfiniteCarouselViewHolder, position: Int) {
        val item = infiniteItems[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            // Tính toán vị trí thực tế trong danh sách gốc
            val actualPosition = position % originalItems.size
            itemClick(actualPosition, item)
        }
    }

    override fun getItemCount(): Int = infiniteItems.size

    fun setItems(newItems: List<Item>) {
        originalItems = newItems
        // Tạo danh sách vô hạn bằng cách lặp lại dữ liệu
        infiniteItems = List(MULTIPLIER) { newItems }.flatten()
        notifyDataSetChanged()
    }

    /**
     * Lấy vị trí bắt đầu ở giữa để tạo hiệu ứng vô hạn
     */
    fun getCenterPosition(): Int {
        return if (originalItems.isNotEmpty()) {
            originalItems.size * (MULTIPLIER / 2)
        } else {
            0
        }
    }

    /**
     * Lấy vị trí thực tế từ vị trí trong danh sách vô hạn
     */
    fun getActualPosition(infinitePosition: Int): Int {
        return infinitePosition % originalItems.size
    }

    /**
     * Lấy item gốc từ vị trí trong danh sách vô hạn
     */
    fun getOriginalItem(infinitePosition: Int): Item? {
        return if (originalItems.isNotEmpty()) {
            originalItems[getActualPosition(infinitePosition)]
        } else {
            null
        }
    }
}

class InfiniteCarouselViewHolder(private val view: ListItemBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(item: Item) {
        view.listItemText.text = "${item.title}"
        view.listItemIcon.setImageResource(item.icon)
    }
}

/**
 * SnapHelper tùy chỉnh để snap item về center
 */
class CenterSnapHelper : LinearSnapHelper() {
    
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager !is androidx.recyclerview.widget.LinearLayoutManager) {
            return super.findSnapView(layoutManager)
        }
        
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        
        if (firstVisibleItemPosition == RecyclerView.NO_POSITION) {
            return null
        }
        
        val centerPosition = (firstVisibleItemPosition + lastVisibleItemPosition) / 2
        return layoutManager.findViewByPosition(centerPosition)
    }
    
    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray? {
        if (layoutManager !is androidx.recyclerview.widget.LinearLayoutManager) {
            return super.calculateDistanceToFinalSnap(layoutManager, targetView)
        }
        
        val out = IntArray(2)
        
        if (layoutManager.orientation == androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL) {
            out[0] = calculateHorizontalDistanceToSnap(layoutManager, targetView)
        } else {
            out[1] = calculateVerticalDistanceToSnap(layoutManager, targetView)
        }
        
        return out
    }
    
    private fun calculateHorizontalDistanceToSnap(
        layoutManager: androidx.recyclerview.widget.LinearLayoutManager,
        targetView: View
    ): Int {
        val centerX = layoutManager.width / 2
        val targetCenterX = targetView.left + targetView.width / 2
        return targetCenterX - centerX
    }
    
    private fun calculateVerticalDistanceToSnap(
        layoutManager: androidx.recyclerview.widget.LinearLayoutManager,
        targetView: View
    ): Int {
        val centerY = layoutManager.height / 2
        val targetCenterY = targetView.top + targetView.height / 2
        return targetCenterY - centerY
    }
} 