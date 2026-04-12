package com.gs.pickleball.ui.customviews.toolbar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.gs.pickleball.R
import com.gs.pickleball.databinding.CoreToolbarViewBinding
import com.gs.pickleball.utils.extensions.setOnSingleClick
import com.gs.pickleball.utils.extensions.visibleIf

class CoreToolbarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private val viewBinding = CoreToolbarViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val defaultIconMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics).toInt()

    var showBack: Boolean = true
        set(value) {
            field = value
            viewBinding.rivBack.visibleIf(value)
            invalidate()
        }

    var isEnableBack: Boolean = true
        set(value) {
            field = value
            viewBinding.rivBack.isEnabled = value
            invalidate()
        }

    var resBack: Int = R.drawable.core_selector_ic_back
        set(value) {
            field = value
            viewBinding.rivBack.iconRippleRes = value
            invalidate()
        }

    var marginStartBack: Int = defaultIconMargin
        set(value) {
            field = value
            (viewBinding.rivBack.layoutParams as? MarginLayoutParams)?.let { params ->
                params.marginStart = value
                viewBinding.rivBack.layoutParams = params
            }
            invalidate()
        }

    var title: String = ""
        set(value) {
            field = value
            viewBinding.tvTitle.text = value
            invalidate()
        }

    var showTitle: Boolean = true
        set(value) {
            field = value
            viewBinding.tvTitle.visibleIf(value)
            invalidate()
        }

    var textColorTitle: Int = Color.BLACK
        set(value) {
            field = value
            viewBinding.tvTitle.setTextColor(value)
            invalidate()
        }

    var showUpDown: Boolean = false
        set(value) {
            field = value
            viewBinding.imageUpDown.visibleIf(value)
            viewBinding.clTitle.isEnabled = value
            viewBinding.clTitle.isClickable = value
            invalidate()
        }

    var resUp: Int = R.drawable.core_icon_up
        set(value) {
            field = value
            updateUpDown()
            invalidate()
        }

    var resDown: Int = R.drawable.core_icon_down
        set(value) {
            field = value
            updateUpDown()
            invalidate()
        }

    var isUp: Boolean = true
        set(value) {
            field = value
            updateUpDown()
            invalidate()
        }

    var showTvAction: Boolean = false
        set(value) {
            field = value
            viewBinding.tvAction.visibleIf(value)
            invalidate()
        }

    var showBackgroundTvAction: Boolean = true
        set(value) {
            field = value
            if (value) {
                viewBinding.tvAction.setBackgroundResource(R.drawable.core_bg_save_language)
            } else {
                viewBinding.tvAction.setBackgroundResource(R.drawable.core_bg_save_language_none)
            }
            invalidate()
        }

    var textAction: String = ""
        set(value) {
            field = value
            viewBinding.tvAction.text = value
            invalidate()
        }

    var isEnableTvAction: Boolean = true
        set(value) {
            field = value
            viewBinding.tvAction.isEnabled = value
            invalidate()
        }

    var resBackgroundTvAction: Int = R.drawable.core_bg_save_language
        set(value) {
            field = value
            viewBinding.tvAction.setBackgroundResource(value)
            invalidate()
        }

    var textColorTvAction: Int = Color.BLACK
        set(value) {
            field = value
            viewBinding.tvAction.setTextColor(value)
            invalidate()
        }

    var textSizeTvAction: Float = viewBinding.tvAction.textSize
        set(value) {
            field = value
            // dùng PX trực tiếp để tránh scale lại
            viewBinding.tvAction.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
            invalidate()
        }

    var marginEndTvAction: Int = defaultIconMargin
        set(value) {
            field = value
            (viewBinding.tvAction.layoutParams as? MarginLayoutParams)?.let { params ->
                params.marginEnd = value
                viewBinding.tvAction.layoutParams = params
            }
            invalidate()
        }

    var showAction: Boolean = false
        set(value) {
            field = value
            viewBinding.rivAction.visibleIf(value)
            invalidate()
        }

    var isEnableAction: Boolean = true
        set(value) {
            field = value
            viewBinding.rivAction.isEnabled = value
            invalidate()
        }

    var resAction: Int = R.drawable.core_selector_ic_back
        set(value) {
            field = value
            viewBinding.rivAction.iconRippleRes = value
            invalidate()
        }

    var marginEndAction: Int = defaultIconMargin
        set(value) {
            field = value
            (viewBinding.rivAction.layoutParams as? MarginLayoutParams)?.let { params ->
                params.marginEnd = value
                viewBinding.rivAction.layoutParams = params
            }
            invalidate()
        }

    var showActionExtra: Boolean = false
        set(value) {
            field = value
            viewBinding.rivActionExtra.visibleIf(value)
            invalidate()
        }

    var isEnableActionExtra: Boolean = true
        set(value) {
            field = value
            viewBinding.rivActionExtra.isEnabled = value
            invalidate()
        }

    var resActionExtra: Int = R.drawable.core_selector_ic_back
        set(value) {
            field = value
            viewBinding.rivActionExtra.iconRippleRes = value
            invalidate()
        }

    var onToolbarListener: OnToolbarListener? = null

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.CoreToolbarView) {
                showBack = getBoolean(R.styleable.CoreToolbarView_ctv_ic_back_show, showBack)
                resBack = getResourceId(R.styleable.CoreToolbarView_ctv_ic_back_icon, resBack)
                isEnableBack = getBoolean(R.styleable.CoreToolbarView_ctv_ic_back_enable, isEnableBack)
                marginStartBack = getDimensionPixelSize(R.styleable.CoreToolbarView_ctv_ic_back_margin_start, marginStartBack)

                title = getString(R.styleable.CoreToolbarView_ctv_tv_title) ?: title
                showTitle = getBoolean(R.styleable.CoreToolbarView_ctv_tv_title_show, showTitle)
                textColorTitle = getColor(R.styleable.CoreToolbarView_ctv_tv_title_text_color, textColorTitle)

                showUpDown = getBoolean(R.styleable.CoreToolbarView_ctv_ic_up_down_up_show, showUpDown)
                resUp = getResourceId(R.styleable.CoreToolbarView_ctv_ic_up_down_up_icon, resUp)
                resDown = getResourceId(R.styleable.CoreToolbarView_ctv_ic_up_down_down_icon, resDown)

                showTvAction = getBoolean(R.styleable.CoreToolbarView_ctv_tv_action_show, showTvAction)
                isEnableTvAction = getBoolean(R.styleable.CoreToolbarView_ctv_tv_action_enable, isEnableTvAction)
                textAction = getString(R.styleable.CoreToolbarView_ctv_tv_action_text) ?: textAction
                resBackgroundTvAction = getResourceId(R.styleable.CoreToolbarView_ctv_tv_action_background, resBackgroundTvAction)
                showBackgroundTvAction = getBoolean(R.styleable.CoreToolbarView_ctv_tv_action_background_show, showBackgroundTvAction)
                textColorTvAction = getColor(R.styleable.CoreToolbarView_ctv_tv_action_text_color, textColorTvAction)
                textSizeTvAction = getDimension(R.styleable.CoreToolbarView_ctv_tv_action_text_size, textSizeTvAction)
                marginEndTvAction = getDimensionPixelSize(R.styleable.CoreToolbarView_ctv_tv_action_margin_end, marginEndTvAction)

                showAction = getBoolean(R.styleable.CoreToolbarView_ctv_ic_action_show, showAction)
                resAction = getResourceId(R.styleable.CoreToolbarView_ctv_ic_action_icon, resAction)
                isEnableAction = getBoolean(R.styleable.CoreToolbarView_ctv_ic_action_enable, isEnableAction)
                marginEndAction = getDimensionPixelSize(R.styleable.CoreToolbarView_ctv_ic_action_margin_end, marginEndAction)

                showActionExtra = getBoolean(R.styleable.CoreToolbarView_ctv_ic_action_extra_show, showActionExtra)
                resActionExtra = getResourceId(R.styleable.CoreToolbarView_ctv_ic_action_extra_icon, resActionExtra)
                isEnableActionExtra = getBoolean(R.styleable.CoreToolbarView_ctv_ic_action_extra_enable, isEnableActionExtra)
            }
        }

        viewBinding.rivBack.setOnSingleClick {
            onToolbarListener?.onBack()
        }

        viewBinding.clTitle.setOnSingleClick {
            isUp = !isUp
            onToolbarListener?.onUpDown(isUp)
        }

        viewBinding.tvAction.setOnSingleClick {
            onToolbarListener?.onTvAction()
        }

        viewBinding.rivAction.setOnSingleClick {
            onToolbarListener?.onAction()
        }

        viewBinding.rivActionExtra.setOnSingleClick {
            onToolbarListener?.onActionExtra()
        }
    }

    private fun updateUpDown() {
        if (isUp) {
            viewBinding.imageUpDown.setImageResource(resUp)
        } else {
            viewBinding.imageUpDown.setImageResource(resDown)
        }
    }

    interface OnToolbarListener {
        fun onBack() {}
        fun onUpDown(isUp: Boolean) {}
        fun onAction() {}
        fun onActionExtra() {}
        fun onTvAction() {}
    }
}