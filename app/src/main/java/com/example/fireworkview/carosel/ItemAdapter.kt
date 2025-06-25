package com.example.fireworkview.carosel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fireworkview.databinding.ListItemBinding

class ItemAdapter(val itemClick: (position: Int, item: Item) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items: List<Item> = listOf()
    private var itemWidth: Int = 0
    private var itemHeight: Int = 0
    private var dimensionsCalculated: Boolean = false
    private var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        // Store reference to RecyclerView for dimension calculation
        if (recyclerView == null && parent is RecyclerView) {
            recyclerView = parent
            setupDimensionCalculation(parent)
        }

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], itemWidth, itemHeight)
        holder.itemView.setOnClickListener {
            itemClick(position, items[position])
        }
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<Item>) {
        items = newItems
        dimensionsCalculated = false // Reset flag to recalculate dimensions
        notifyDataSetChanged()
    }

    private fun setupDimensionCalculation(recyclerView: RecyclerView) {
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!dimensionsCalculated && recyclerView.width > 0) {
                calculateItemDimensions(recyclerView)
                // Force rebind all visible items to apply new dimensions
                notifyDataSetChanged()
            }
        }
    }

    private fun calculateItemDimensions(recyclerView: RecyclerView) {
        // Get RecyclerView width
        val recyclerViewWidth = recyclerView.width
        if (recyclerViewWidth > 0) {
            // Calculate background width: 0.53 * RecyclerView width
            val backgroundWidth = (recyclerViewWidth * 0.53).toInt()

            // Calculate background height: width / 0.9 (aspect ratio)
            val backgroundHeight = (backgroundWidth / 0.63).toInt()

            // Set item dimensions
            itemWidth = backgroundWidth
            itemHeight = backgroundHeight
            dimensionsCalculated = true
        }
    }

    // Public method to force dimension calculation (can be called from activity)
    fun calculateDimensions(recyclerView: RecyclerView) {
        if (!dimensionsCalculated) {
            calculateItemDimensions(recyclerView)
            notifyDataSetChanged()
        }
    }
}

class ItemViewHolder(
    private val view: ListItemBinding
) : RecyclerView.ViewHolder(view.root) {

    fun bind(item: Item, itemWidth: Int, itemHeight: Int) {
        view.listItemText.text = "${item.title}"
        view.listItemIcon.setImageResource(item.icon)

        // Apply calculated dimensions to the background
        if (itemWidth > 0 && itemHeight > 0) {
            val layoutParams = view.listItemBackground.layoutParams
            layoutParams.width = itemWidth
            layoutParams.height = itemHeight
            view.listItemBackground.layoutParams = layoutParams
        }
    }
}
