package com.example.burn_it.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.burn_it.R
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.overlay.BalloonOverlayRect

class IntroBalloonFactory {
    companion object {
        fun balloonBuilder(
            context: Context,
            lifecycleOwner: LifecycleOwner,
        ): Balloon {
            val balloon = createBalloon(context) {
                setLayout(R.layout.balloon_layout)
                setWidthRatio(.5f)
                setHeight(90)
                setArrowPosition(0.7f)
                setCornerRadius(4f)
                setAlpha(0.9f)
                setTextColorResource(R.color.primaryDark)
                setBackgroundColorResource(R.color.colorSecondary)
                setBalloonAnimation(BalloonAnimation.FADE)
                setLifecycleOwner(lifecycleOwner)
                setOverlayShape(BalloonOverlayRect)
                iconColor = ContextCompat.getColor(context, R.color.colorAccent)
            }
            return balloon
        }


    }

}