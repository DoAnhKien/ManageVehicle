package com.example.vehiclessale

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout

class ViewAnimationUtils {
    companion object {
        fun expand(v: View) {
            v.measure(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            val targetHeight = v.measuredHeight
            v.layoutParams.height = 0
            v.visibility = FrameLayout.VISIBLE
            val a: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    v.layoutParams.height =
                        if (interpolatedTime == 1f) FrameLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            a.duration = ((targetHeight / v.context.resources.displayMetrics.density).toLong())
            v.startAnimation(a)
        }

        fun collapse(v: View) {
            val initialHeight = v.measuredHeight
            val a: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        v.visibility = FrameLayout.GONE
                    } else {
                        v.layoutParams.height =
                            initialHeight - (initialHeight * interpolatedTime).toInt()
                        v.requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            a.duration = ((initialHeight / v.context.resources.displayMetrics.density).toLong())
            v.startAnimation(a)
        }
    }
}