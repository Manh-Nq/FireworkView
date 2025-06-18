package com.example.fireworkview

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.databinding.MainActivity2Binding
import com.example.fireworkview.infinity.RecyclerViewActivity
import com.example.fireworkview.shimmer.ShimmerHighlightView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivity2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivity2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ShimmerHighlightView
        setupShimmerView()

        // Setup button click listeners
        setupButtons()
    }

    private fun setupShimmerView() {
        // Configure shimmer properties
        binding.shimmerView.apply {
            setShimmerColor(Color.parseColor("#FFE87C"))
            backgroundColor = Color.parseColor("#73777777")
            setShimmerWidth(0.1f)
            setShimmerDuration(500)
        }
    }

    private fun setupButtons() {
        binding.btnStartShimmer.setOnClickListener {
            binding.shimmerView.startShimmer()
        }

        binding.btnStopShimmer.setOnClickListener {
            binding.shimmerView.stopShimmer()
        }

        binding.btnPlayOnce.setOnClickListener {
            playShimmerOnce()
        }

        binding.btnRecyclerView.setOnClickListener {
            val intent = Intent(this, RecyclerViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun playShimmerOnce() {
        binding.shimmerView.playShimmerOnce(object : ShimmerHighlightView.ShimmerCompletionListener {
            override fun onShimmerCompleted() {
                println("Shimmer animation completed!")
            }
        })
    }

}