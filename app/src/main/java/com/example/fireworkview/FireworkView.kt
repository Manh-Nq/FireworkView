package com.example.fireworkview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.alpha
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class FireworkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Interface for completion callback
    interface OnFireworkCompletionListener {
        fun onFireworkCompleted()
    }

    private val random = Random()
    private val fireworks = mutableListOf<Firework>()
    private var maxFireworks = 15
    private val paint = Paint().apply {
        isAntiAlias = true
    }
    private var launchAnimator: ValueAnimator? = null

    // Explosion size in DP
    private val explosionSizeDp = 150f
    private val explosionSizePx by lazy { dpToPx(explosionSizeDp) }

    // Timeout handling
    private var timeoutDuration = 30_000L // Default 30 seconds
    private var timeoutHandler: Handler? = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    // Animation state
    private var isPaused = false
    private var isActive = false

    // Completion listener
    private var completionListener: OnFireworkCompletionListener? = null

    private var backgroundColor: Int = Color.BLACK // Default background color
    private var centerText: String? = null // Text to display at the center
    private val textPaint = Paint().apply {
        color = Color.WHITE // Default text color
        textSize = 50f // Default text size
        isAntiAlias = true
        textAlign = Paint.Align.CENTER // Center align text
    }


    // DP to Pixel conversion utility function
    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    // Hardware acceleration is crucial for performance
    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    // Starts the fireworks animation with an optional timeout
    fun start() {
        if (isActive) return

        isActive = true
        isPaused = false

        startFireworkLauncher()
        startTimeoutTimer()
    }

    // Pause the animation
    fun pause() {
        if (!isActive || isPaused) return

        isPaused = true
        launchAnimator?.pause()

        // Pause all active firework animations
        for (firework in fireworks) {
            firework.pause()
        }

        // Pause the timeout timer
        timeoutHandler?.removeCallbacks(timeoutRunnable ?: return)
    }

    // Resume the animation
    fun resume() {
        if (!isActive || !isPaused) return

        isPaused = false
        launchAnimator?.resume()

        // Resume all active firework animations
        for (firework in fireworks) {
            firework.resume()
        }

        // Resume the timeout timer
        startTimeoutTimer()
    }

    // Stop the animation
    fun stop() {
        if (!isActive) return

        isActive = false
        isPaused = false

        // Cancel the launch animator
        launchAnimator?.cancel()
        launchAnimator = null

        // Cancel the timeout timer
        completionListener?.onFireworkCompleted()

        timeoutHandler?.removeCallbacks(timeoutRunnable ?: return)

        // Clean up all fireworks
        for (firework in fireworks) {
            firework.cleanup()
        }
        fireworks.clear()

        // Trigger a redraw to clear the view
        invalidate()
    }

    // Restart the animation
    fun restart() {
        stop()
        start()
    }

    private fun startFireworkLauncher() {
        launchAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 1500 // Launch frequency
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                // Launch fireworks more frequently when there are fewer on screen
                if (fireworks.size < maxFireworks && random.nextFloat() > 0.7f) {
                    launchFirework()
                }
            }
        }
        launchAnimator?.start()
    }

    private fun startTimeoutTimer() {
        // Cancel any existing timeout
        timeoutHandler?.removeCallbacks(timeoutRunnable ?: return)

        timeoutRunnable = Runnable {
            // Complete the animation when timeout occurs
            finishAnimation()
        }

        // Start the timeout
        timeoutHandler?.postDelayed(timeoutRunnable!!, timeoutDuration)
    }

    private fun finishAnimation() {
        if (!isActive) return

        stop()

        // Notify the parent that animation is completed
        completionListener?.onFireworkCompleted()
    }

    private fun launchFirework() {
        val width = width.toFloat()
        val height = height.toFloat()

        if (width <= 0 || height <= 0) return

        // Launch from bottom area of screen
        val startX = random.nextFloat() * width
        val startY = height

        // Target in upper area with some variance
        val targetX = width * 0.1f + random.nextFloat() * width * 0.8f
        val targetY = height * 0.1f + random.nextFloat() * height * 0.4f

        // Choose vibrant colors for better visual effect
        val hue = random.nextFloat() * 360
        val color = getColorFromHSV(hue, 0.8f, 1.0f)

        val firework = Firework(startX, startY, targetX, targetY, color)
        fireworks.add(firework)
        firework.launch()
    }

    // Helper to get colors with HSV for more vibrant fireworks
    private fun getColorFromHSV(hue: Float, saturation: Float, value: Float): Int {
        val hsv = floatArrayOf(hue, saturation, value)
        return Color.HSVToColor(hsv)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)

        // Draw the fireworks
        super.onDraw(canvas)

        // Draw the center text if it exists
        centerText?.let {
            val x = width / 2f
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2 // Center vertically
            canvas.drawText(it, x, y, textPaint)
        }

        // Use iterator to safely remove completed fireworks
        val iterator = fireworks.iterator()
        while (iterator.hasNext()) {
            val firework = iterator.next()
            firework.draw(canvas, paint)

            if (firework.isDone()) {
                iterator.remove()
            }
        }

        // Only invalidate if we have active fireworks
        if (fireworks.isNotEmpty()) {
            invalidate()
        }
    }

    inner class Firework(
        private val startX: Float,
        private val startY: Float,
        private val targetX: Float,
        private val targetY: Float,
        private val color: Int
    ) {
        private val particles = mutableListOf<Particle>()
        private var state = FireworkState.LAUNCHING
        private var rocketProgress = 0f
        private var explosionProgress = 0f
        private var launchDuration = 1000L
        private var explosionDuration = 1800L
        private var rocketAnimator: ValueAnimator? = null
        private var explosionAnimator: ValueAnimator? = null

        // Particle counts and characteristics
        private val particleCount = 150 // More particles for better effect
        private val rocketSize = dpToPx(3f)
        private val trailSize = dpToPx(2f)
        private val minParticleSize = dpToPx(1.5f)
        private val maxParticleSize = dpToPx(3f)

        fun launch() {
            rocketAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = launchDuration
                addUpdateListener { animation ->
                    rocketProgress = animation.animatedValue as Float
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        state = FireworkState.EXPLODING
                        createExplosion()
                        explode()
                    }
                })
            }
            rocketAnimator?.start()
        }

        fun pause() {
            rocketAnimator?.pause()
            explosionAnimator?.pause()
        }

        fun resume() {
            rocketAnimator?.resume()
            explosionAnimator?.resume()
        }

        private fun createExplosion() {
            particles.clear()

            // Create particles in patterns more like real fireworks
            for (i in 0 until particleCount) {
                // Calculate explosion pattern
                val angle = random.nextFloat() * 2 * Math.PI

                // Distance from center based on desired explosion size
                val maxDistance = explosionSizePx / 2
                val distance = random.nextFloat() * maxDistance

                // Initial velocity outward from explosion point
                val velocity = dpToPx(random.nextFloat() * 2f + 0.5f)

                // Vary particle sizes
                val particleSize = minParticleSize + random.nextFloat() * (maxParticleSize - minParticleSize)

                // Particle lifetime variance
                val lifespan = 0.6f + random.nextFloat() * 0.4f

                // Create slightly different colors for a more realistic effect
                val particleColor = createParticleVariation(color)

                val particle = Particle(
                    targetX, // Start at explosion center
                    targetY,
                    velocity * cos(angle).toFloat(), // Radial velocity outward
                    velocity * sin(angle).toFloat(),
                    particleSize,
                    lifespan,
                    particleColor
                )
                particles.add(particle)
            }
        }

        // Create color variations for particles
        private fun createParticleVariation(baseColor: Int): Int {
            // Extract base color components
            val red = Color.red(baseColor)
            val green = Color.green(baseColor)
            val blue = Color.blue(baseColor)

            // Add slight variations (±15)
            return Color.rgb(
                (red + random.nextInt(31) - 15).coerceIn(0, 255),
                (green + random.nextInt(31) - 15).coerceIn(0, 255),
                (blue + random.nextInt(31) - 15).coerceIn(0, 255)
            )
        }

        private fun explode() {
            explosionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = explosionDuration
                addUpdateListener { animation ->
                    explosionProgress = animation.animatedValue as Float
                    invalidate()
                }
                doOnEnd {
                    state = FireworkState.DONE
                }
            }
            explosionAnimator?.start()
        }

        fun draw(canvas: Canvas, paint: Paint) {
            when (state) {
                FireworkState.LAUNCHING -> {
                    drawLaunchingRocket(canvas, paint)
                }

                FireworkState.EXPLODING -> {
                    drawExplosion(canvas, paint)
                }

                FireworkState.DONE -> {
                    // Nothing to draw
                }
            }
        }

        private fun drawLaunchingRocket(canvas: Canvas, paint: Paint) {
            val currentX = startX + (targetX - startX) * rocketProgress
            val currentY = startY + (targetY - startY) * rocketProgress

            // Draw the rocket
            paint.color = color
            paint.alpha = 255
            canvas.drawCircle(currentX, currentY, rocketSize, paint)

            // Draw trail with fading effect
            for (i in 1..8) {
                val trailProgress = rocketProgress - 0.05f * i
                if (trailProgress > 0) {
                    val trailX = startX + (targetX - startX) * trailProgress
                    val trailY = startY + (targetY - startY) * trailProgress
                    paint.alpha = (200 * (1 - i / 8f)).toInt()
                    canvas.drawCircle(trailX, trailY, trailSize * (1 - i / 12f), paint)
                }
            }
        }

        private fun drawExplosion(canvas: Canvas, paint: Paint) {
            // Draw explosion particles with glow effect
            for (particle in particles) {
                particle.update(explosionProgress)

                // Progressive fading based on progress and particle lifespan
                val particleAlpha = ((1f - (explosionProgress / particle.lifespan)) * 255).toInt()
                if (particleAlpha > 0) {
                    paint.color = particle.color
                    paint.alpha = particleAlpha.coerceIn(0, 255)

                    // Draw glow effect
                    paint.setShadowLayer(10f, 0f, 0f, Color.WHITE) // Glow effect
                    val currentSize = particle.size * (1f - explosionProgress * 0.7f)
                    canvas.drawCircle(particle.x, particle.y, currentSize, paint)
                    paint.clearShadowLayer() // Clear glow effect for next draw
                }
            }
        }

        fun isDone(): Boolean {
            return state == FireworkState.DONE
        }

        fun cleanup() {
            rocketAnimator?.cancel()
            explosionAnimator?.cancel()
        }
    }

    inner class Particle(
        var x: Float,
        var y: Float,
        private val vx: Float,
        private val vy: Float,
        val size: Float,
        val lifespan: Float,
        val color: Int
    ) {
        fun update(progress: Float) {
            // Apply physics: gravity and deceleration
            val adjustedProgress = progress / lifespan

            if (adjustedProgress < 1.0f) {
                // Gravity effect increases over time
                val gravity = dpToPx(0.15f) * progress * progress

                // Initial velocity is higher, then slows down
                val velocityFactor = 1.0f - adjustedProgress * 0.6f

                // Update position with physics
                x += vx * velocityFactor
                y += vy * velocityFactor + gravity
            }
        }
    }

    enum class FireworkState {
        LAUNCHING, EXPLODING, DONE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isActive && fireworks.isEmpty() && w > 0 && h > 0) {
            // Launch a firework if active and view size changed
            launchFirework()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cleanup resources to prevent memory leaks
        stop()
        timeoutHandler?.removeCallbacksAndMessages(null)
        timeoutHandler = null
        timeoutRunnable = null
    }

    // Public methods for customization

    // Set maximum concurrent fireworks
    fun setMaxFireworks(count: Int) {
        if (count > 0) {
            this.maxFireworks = count
        }
    }

    // Set timeout duration in milliseconds
    fun setTimeout(milliseconds: Long) {
        if (milliseconds > 0) {
            this.timeoutDuration = milliseconds

            // Reset timer if already running
            if (isActive && !isPaused) {
                startTimeoutTimer()
            }
        }
    }

    // Set background color
    fun setViewBackgroundColor(color: Int) {
        this.backgroundColor = color
        invalidate() // Redraw the view
    }

    // Set text to display at the center
    fun setCenterText(text: String) {
        this.centerText = text
        invalidate() // Redraw the view
    }

    fun setBackgroundColor(alphaPercent: Int, color: Any) {
        // Convert alpha percentage to a value between 0 and 255
        val alpha = (alphaPercent * 255) / 100

        // Determine the color based on input type
        val colorInt = when (color) {
            is String -> Color.parseColor(color) // Parse hex color
            is Int -> color // Use the integer color directly
            else -> Color.BLACK // Default to black if input is invalid
        }

        this.backgroundColor = Color.argb(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        invalidate() // Redraw the view
    }

    // Set completion listener
    fun setOnCompletionListener(listener: OnFireworkCompletionListener) {
        this.completionListener = listener
    }

    // Check if animation is currently active
    fun isActive(): Boolean = isActive

    // Check if animation is currently paused
    fun isPaused(): Boolean = isPaused
}