package com.example.fireworkview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fireworkview.databinding.ItemColorViewBinding

class InfiniteColorAdapter : ListAdapter<ColorItem, InfiniteColorAdapter.ColorViewHolder>(ColorItemDiffCallback()) {

    companion object {
        private const val MULTIPLIER = 5 // Show 5 copies of the original list for better infinite scrolling
        private const val MIN_ITEMS_FOR_INFINITE = 3 // Minimum items needed for infinite scrolling
    }

    private var isInfiniteMode = false
    private var originalList = listOf<ColorItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorViewBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val item = if (isInfiniteMode && originalList.size >= MIN_ITEMS_FOR_INFINITE) {
            val originalPosition = position % originalList.size
            originalList[originalPosition]
        } else {
            getItem(position)
        }
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return if (isInfiniteMode && originalList.size >= MIN_ITEMS_FOR_INFINITE) {
            originalList.size * MULTIPLIER
        } else {
            super.getItemCount()
        }
    }

    fun submitListWithInfiniteMode(list: List<ColorItem>, enableInfinite: Boolean) {
        originalList = list
        isInfiniteMode = enableInfinite && list.size >= MIN_ITEMS_FOR_INFINITE
        
        if (isInfiniteMode) {
            // For infinite mode, we don't submit the multiplied list to avoid confusion
            // The adapter will handle the multiplication internally
            super.submitList(list)
        } else {
            // For normal mode, submit the list as usual
            super.submitList(list)
        }
    }

    fun getOriginalItemCount(): Int = originalList.size

    class ColorViewHolder(private val binding: ItemColorViewBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(colorItem: ColorItem) {
            binding.colorView.apply {
                setBackgroundColor(colorItem.color)
                layoutParams.width = colorItem.width
            }
        }
    }

    private class ColorItemDiffCallback : DiffUtil.ItemCallback<ColorItem>() {
        override fun areItemsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
            return oldItem == newItem
        }
    }
} 