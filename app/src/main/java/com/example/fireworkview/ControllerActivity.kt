package com.example.fireworkview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.carosel.CarouselActivity
import com.example.fireworkview.databinding.ControllerActivityBinding

class ControllerActivity : AppCompatActivity() {
    private lateinit var binding: ControllerActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ControllerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup button click listeners
        setupButtons()
    }



    private fun setupButtons() {
        binding.shimmerBtn.setOnClickListener {
            val intent = Intent(this, ShimmerActivity::class.java)
            startActivity(intent)
        }

        binding.listBtn.setOnClickListener {
            val intent = Intent(this, CarouselActivity::class.java)
            startActivity(intent)
        }


    }


}