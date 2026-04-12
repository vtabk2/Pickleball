package com.gs.pickleball.ui.base.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield


abstract class CoreActivity<VB : ViewBinding> : BaseActivity<VB>() {

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setupAfterOnBackPressed()
            }
        }
        onBackPressedCallback?.let {
            onBackPressedDispatcher.addCallback(this, it)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            val config = it.resources.configuration
            if (config.fontScale != 1.0f) {
                config.fontScale = 1.0f
                val newContext = it.createConfigurationContext(config)
                super.attachBaseContext(newContext)
                return
            }
        }
        super.attachBaseContext(newBase)
    }

    /**
     * Hàm này cho phép Activity con override để custom logic.
     */
    open fun setupAfterOnBackPressed() {
        callSystemBack()
        showAdsAfterBack()
    }

    /**
     * Hàm này dùng để hiển thị quảng cáo khi ấn nút back
     */
    open fun showAdsAfterBack() {}

    /**
     * Hàm gọi Back hệ thống an toàn (tránh loop).
     */
    protected fun callSystemBack() {
        onBackPressedCallback?.isEnabled = false
        lifecycleScope.launch(Dispatchers.Main) {
            // nhường 1 frame cho FragmentManager hoàn tất
            yield()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Cho phép bật/tắt OnBackPressedCallback ở Activity con.
     */
    fun setBackPressEnabled(enabled: Boolean) {
        onBackPressedCallback?.isEnabled = enabled
    }

    /**
     * Handle back in fragment
     */
    fun addOnBackPressedCallback(owner: LifecycleOwner, action: () -> Unit) {
        onBackPressedDispatcher.addCallback(owner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = action()
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return try {
            super.dispatchKeyEvent(event)
        } catch (e: SecurityException) {
            if (e.message?.contains("CLOSE_SYSTEM_DIALOGS") == true) {
                true
            } else {
                throw e
            }
        }
    }

    /**
     * Hides the soft keyboard
     */
    private fun View.hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(windowToken, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowInsetsController?.hide(WindowInsetsCompat.Type.ime())
        } else {
            ViewCompat.getWindowInsetsController(this)?.hide(WindowInsetsCompat.Type.ime())
        }
        ViewCompat.setWindowInsetsAnimationCallback(window.decorView.rootView, null)
    }

    /**
     * Shows the soft keyboard
     */
    @JvmName("showSoftKeyboardExt")
    fun View.showSoftKeyboard() {
        lifecycleScope.launch {
            delay(200)
            (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(this@showSoftKeyboard, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsetsController?.show(WindowInsetsCompat.Type.ime())
            } else {
                ViewCompat.getWindowInsetsController(this@showSoftKeyboard)?.show(WindowInsetsCompat.Type.ime())
            }
            ViewCompat.setWindowInsetsAnimationCallback(window.decorView.rootView, object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (window.decorView.rootView.rootWindowInsets?.isVisible(WindowInsetsCompat.Type.ime()) == false) {
                            // hide keyboard
                            hideKeyboardApi33(true)
                        }
                    } else {
                        // nothing
                    }
                }
            })
        }
    }

    fun showSoftKeyboard(view: View) {
        view.showSoftKeyboard()
    }

    fun View.hideKeyboardFull() {
        hideSoftKeyboard()
        hideKeyboardApi33(false)
    }

    open fun hideKeyboardApi33(canCheck: Boolean) {

    }
}