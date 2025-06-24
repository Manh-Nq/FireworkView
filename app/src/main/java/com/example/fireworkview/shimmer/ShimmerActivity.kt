package com.example.fireworkview.shimmer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.carosel.CarouselActivity
import com.example.fireworkview.databinding.ShimmerActivityBinding

class ShimmerActivity : AppCompatActivity() {
    private lateinit var binding: ShimmerActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShimmerActivityBinding.inflate(layoutInflater)
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
    }

    private fun playShimmerOnce() {
        binding.shimmerView.playShimmerOnce { Log.d("ManhNQ","Shimmer animation completed!") }
    }

}