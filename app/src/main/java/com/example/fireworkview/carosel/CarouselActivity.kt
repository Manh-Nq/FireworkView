package com.example.fireworkview.carosel


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.R
import com.example.fireworkview.databinding.ActivityRecyclerViewBinding

class CarouselActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerViewBinding

    private val itemAdapter by lazy {
        ItemAdapter { position: Int, item: Item ->
            Toast.makeText(this@CarouselActivity, "Pos ${position}", Toast.LENGTH_LONG).show()
            binding.recyclerView.smoothScrollToPosition(position)
        }
    }

    private val possibleItems = listOf(
        Item("Airplanes", R.drawable.ic_airplane),
        Item("Cars", R.drawable.ic_car),
        Item("Food", R.drawable.ic_food),
        Item("Gas", R.drawable.ic_gas),
        Item("Home", R.drawable.ic_home)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.initialize(itemAdapter)
        binding.recyclerView.setViewsToChangeColor(listOf(R.id.list_item_background, R.id.left_3d_icon, R.id.right_3d_icon))
        binding.recyclerView.setOnSnapPositionChangeListener(object : HorizontalCarouselRecyclerView.OnSnapPositionChangeListener {
            override fun onSnapPositionChanged(position: Int) {
                Log.d("ManhNQ", "onSnapPositionChanged: $position")
            }

        })
        
        // Ensure RecyclerView is laid out before setting items
        binding.recyclerView.post {
            itemAdapter.setItems(getLargeListOfItems())
            binding.recyclerView.forceRefreshEffects()
        }
    }

    private fun getLargeListOfItems(): List<Item> {
        val items = mutableListOf<Item>()
        (0..40).map { items.add(possibleItems.random()) }
        return items
    }
}

data class Item(
    val title: String,
    @DrawableRes val icon: Int
)