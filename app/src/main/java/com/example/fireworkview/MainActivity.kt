package com.example.fireworkview

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.databinding.MainActivity2Binding

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
            setShimmerColor(Color.parseColor("#FFE87C")) // Warm light bulb yellow
            setShimmerWidth(0.1f)
            setShimmerAlpha(0.9f)
            setShimmerDuration(1000)
        }
    }

    private fun setupButtons() {
        // Start shimmer animation (loops infinitely)
        binding.btnStartShimmer.setOnClickListener {
            binding.shimmerView.startShimmer()
        }

        // Stop shimmer animation
        binding.btnStopShimmer.setOnClickListener {
            binding.shimmerView.stopShimmer()
        }

        // Play shimmer once (custom implementation)
        binding.btnPlayOnce.setOnClickListener {
            playShimmerOnce()
        }
        
        // Background control buttons
        binding.btnTransparentBg.setOnClickListener {
            binding.shimmerView.setBackgroundColor(Color.TRANSPARENT)
            println("Set transparent background")
            println("Debug: ${binding.shimmerView.getShimmerDebugInfo()}")
        }
        
        binding.btnWhiteBg.setOnClickListener {
            binding.shimmerView.setBackgroundColor(Color.WHITE)
            println("Set white background")
        }
        
        binding.btnGrayBg.setOnClickListener {
            binding.shimmerView.setBackgroundColor(Color.parseColor("#F5F5F5"))
            println("Set gray background")
        }
        
        // Sun-like shimmer color buttons
        binding.btnGoldenSun.setOnClickListener {
            binding.shimmerView.setShimmerColor(Color.parseColor("#FFE87C")) // Warm light bulb yellow
            println("Set warm light bulb shimmer")
        }
        
        binding.btnOrangeSun.setOnClickListener {
            binding.shimmerView.setShimmerColor(Color.parseColor("#FFFACD")) // Bright light bulb
            println("Set bright light bulb shimmer")
        }
        
        binding.btnYellowSun.setOnClickListener {
            binding.shimmerView.setShimmerColor(Color.parseColor("#FFEFD5")) // Incandescent light
            println("Set incandescent light shimmer")
        }
    }

    private fun playShimmerOnce() {
        // Play shimmer once with completion callback
        binding.shimmerView.playShimmerOnce(object : ShimmerHighlightView.ShimmerCompletionListener {
            override fun onShimmerCompleted() {
                // This callback will be called when the shimmer animation completes
                // You can add any logic here, such as showing a toast, updating UI, etc.
                println("Shimmer animation completed!")
            }
        })
    }

    private fun updateWidgetButtonText(newText: String) {
        val intent = Intent(this, AppWidget::class.java).apply {
            action = AppWidget.ACTION_UPDATE_DATA
            putExtra(AppWidget.EXTRA_DATA_TEXT, newText)
        }
        sendBroadcast(intent)
    }
}