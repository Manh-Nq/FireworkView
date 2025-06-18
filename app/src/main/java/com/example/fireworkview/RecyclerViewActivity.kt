package com.example.fireworkview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fireworkview.databinding.ActivityRecyclerViewBinding

class RecyclerViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerViewBinding
    private lateinit var adapter: InfiniteColorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Calculate 80% of screen width
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemWidth = (screenWidth * 0.8).toInt()

        // Create single adapter
        adapter = InfiniteColorAdapter()

        // Generate sample data
        val colorItems = generateRandomColorItems(20, itemWidth)

        // Setup RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecyclerViewActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = this@RecyclerViewActivity.adapter

            // Determine if infinite scrolling should be enabled
            val enableInfinite = colorItems.size > 2

            // Submit data with infinite mode setting
            this@RecyclerViewActivity.adapter.submitListWithInfiniteMode(colorItems, enableInfinite)

            // Add infinite scroll listener only if needed
            if (enableInfinite) {
                addOnScrollListener(InfiniteScrollListener(colorItems.size))

                // Start at the middle of the infinite list for seamless scrolling
                post {
                    val middlePosition = colorItems.size * 2
                    scrollToPosition(middlePosition)
                }
            }
        }
    }

    private fun generateRandomColorItems(count: Int, itemWidth: Int): List<ColorItem> {
        val colors = listOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
            Color.MAGENTA, Color.parseColor("#FF6B35"), Color.parseColor("#F7931E"),
            Color.parseColor("#FFD23F"), Color.parseColor("#5390D9"),
            Color.parseColor("#7400B8"), Color.parseColor("#6930C3"),
            Color.parseColor("#5E60CE"), Color.parseColor("#64DFDF"),
            Color.parseColor("#72EFDD"), Color.parseColor("#80FFDB")
        )

        return List(count) { index ->
            ColorItem(
                id = index,
                color = colors.random(),
                width = itemWidth
            )
        }
    }
}

data class ColorItem(
    val id: Int,
    val color: Int,
    val width: Int
) 