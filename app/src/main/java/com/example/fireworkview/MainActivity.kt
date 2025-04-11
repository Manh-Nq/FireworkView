package com.example.fireworkview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding :MainActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener {
            binding.fireWorkView.restart()
        }
        binding.pauseBtn.setOnClickListener {
            binding.fireWorkView.pause()
        }
        binding.resumeBtn.setOnClickListener {
            binding.fireWorkView.resume()
        }
        binding.stopBtn.setOnClickListener {
            binding.fireWorkView.stop()
        }

        binding.fireWorkView.setOnCompletionListener(object : FireworkView.OnFireworkCompletionListener {
            override fun onFireworkCompleted() {
                Log.d("ManhNQ", "onFireworkCompleted: ")
            }

        })

    }
}