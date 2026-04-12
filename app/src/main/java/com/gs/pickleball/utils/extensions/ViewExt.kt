package com.gs.pickleball.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import kotlin.math.hypot

/**
 * View's visibility extensions.
 */

fun View.visible() {
    changeViewVisibility(View.VISIBLE)
}

fun View.gone() {
    changeViewVisibility(View.GONE)
}

fun View.invisible() {
    changeViewVisibility(View.INVISIBLE)
}

private fun View.changeViewVisibility(newState: Int) {
    visibility = newState
}

infix fun View.visibleIf(condition: Boolean) =
    run { visibility = if (condition) View.VISIBLE else View.GONE }

infix fun View.goneIf(condition: Boolean) =
    run { visibility = if (condition) View.GONE else View.VISIBLE }

infix fun View.invisibleIf(condition: Boolean) =
    run { visibility = if (condition) View.INVISIBLE else View.VISIBLE }

fun View.visibleCircular(cx: Int = width / 2, cy: Int = height / 2) {
    try {
        // get the final radius for the clipping circle
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animator for this view (the start radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
        // make the view visible and start the animation
        visible()
        anim.start()
    } catch (e: Exception) {
        e.printStackTrace()
        visible()
    }
}

fun View.invisibleCircular(cx: Int = width / 2, cy: Int = height / 2) {
    try {
        // get the initial radius for the clipping circle
        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animation (the final radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, initialRadius, 0f)

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                invisible()
            }
        })

        // start the animation
        anim.start()
    } catch (e: Exception) {
        e.printStackTrace()
        invisible()
    }
}

private var lastClickTime = 0L
private const val THRESHOLD_DOUBLE_TIME = 500

fun View.setOnClickPreventingDouble(onClick: () -> Unit) {
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < THRESHOLD_DOUBLE_TIME) {
            return@setOnClickListener
        }
        onClick.invoke()
        lastClickTime = SystemClock.elapsedRealtime()
    }
}

fun View.setOnClickPreventingSelfDouble(interval: Int = THRESHOLD_DOUBLE_TIME, onClick: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < interval) {
            return@setOnClickListener
        }
        onClick.invoke()
        lastClickTime = SystemClock.elapsedRealtime()
    }
}

//fun View.setOnClickPreventingDouble(onClick: () -> Unit) {
//    var lastClickTime = 0L
//    val interval = 500
//
//    setOnClickListener {
//        if (SystemClock.elapsedRealtime() - lastClickTime < interval) {
//            return@setOnClickListener
//        }
//        onClick.invoke()
//        lastClickTime = SystemClock.elapsedRealtime()
//    }
//}


fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

fun View.margin(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.let { leftMargin = it }
        top?.let { topMargin = it }
        right?.let { rightMargin = it }
        bottom?.let { bottomMargin = it }
    }
}

fun View.padding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(
        left ?: paddingLeft,
        top ?: paddingTop,
        right ?: paddingRight,
        bottom ?: paddingBottom,
    )
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun View.dpToPxF(dp: Float): Float = context.dpToPxF(dp)

fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
fun Context.dpToPxF(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun View.doOnDoubleClick(onDoubleClick: (View) -> Unit) {
    val safeClickListener = DoubleClickListener {
        onDoubleClick(it)
    }
    setOnClickListener(safeClickListener)
}

class DoubleClickListener(
    private var defaultInterval: Int = 300,
    private val onDoubleClick: (View) -> Unit
) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < defaultInterval) {
            onDoubleClick(v)
            lastClickTime = 0
        }
        lastClickTime = clickTime
    }
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

public inline fun SeekBar.setOnSeekBarChangeListener(
    crossinline progressBarChanged:
        (seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit =
        { _, _, _ ->
        },
    crossinline onStartTrackingTouch: (seekBar: SeekBar) -> Unit =
        { _ ->
        },
    crossinline onStopTrackingTouch: (seekBar: SeekBar) -> Unit = {}
): SeekBar.OnSeekBarChangeListener {
    val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            progressBarChanged(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            onStopTrackingTouch(seekBar)
        }
    }

    setOnSeekBarChangeListener(seekBarChangeListener)
    return seekBarChangeListener
}

fun View.setOnSingleClick(onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnSingleClick() {
        override fun onSingleClick(view: View) {
            onClick.invoke(view)
        }
    })
}

fun View.setOnSingleClick(timeDelay: Long = 500, onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnSingleClick(timeDelay = timeDelay) {
        override fun onSingleClick(view: View) {
            onClick.invoke(view)
        }
    })
}


fun View.setOnSingleClick(onClick: View.OnClickListener) {
    this.setOnClickListener(object : OnSingleClick() {
        override fun onSingleClick(view: View) {
            onClick.onClick(view)
        }
    })
}

fun View.paddingTop(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, paddingBottom)
}

fun View.unfocus() {
    isFocusableInTouchMode = false
    isFocusable = false
    isFocusableInTouchMode = true
    isFocusable = true
}

fun View.changeSize(
    width: Int? = null,
    height: Int? = null
) {
    val lp = this.layoutParams
    width?.let {
        lp.width = width
    }
    height?.let {
        lp.height = height
    }

    if (width != null || height != null) {
        this.layoutParams = lp
    }
}