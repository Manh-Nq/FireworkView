package com.example.fireworkview.firework

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.fireworkview.R
import com.example.fireworkview.databinding.FireworkActivityBinding

class FireWorkActivity : AppCompatActivity() {
    private lateinit var binding: FireworkActivityBinding
    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var count = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FireworkActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.prepareBtn.setOnClickListener {
            startCountDown()
        }
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

        binding.fireWorkView.setCenterText("Congratulation!")
        binding.fireWorkView.setMaxFireworks(100000)
        binding.fireWorkView.setBackgroundColor(75, R.color.black)
    }

    private fun startCountDown() {
        if (count > 0) {
            binding.countDownTxt.visibility = View.VISIBLE
            binding.countDownTxt.text = "$count"
            handler.postDelayed({
                count--
                startCountDown()
            }, 1000)
        } else {
            binding.countDownTxt.visibility = View.GONE
            binding.fireWorkView.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.fireWorkView.stop()
    }
}