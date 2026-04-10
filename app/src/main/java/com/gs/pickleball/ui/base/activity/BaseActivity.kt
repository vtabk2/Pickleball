package com.gs.pickleball.ui.base.activity

import android.app.Activity
import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.graphics.Insets
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.gs.pickleball.extensions.getCurrentLanguageCode
import com.gs.pickleball.extensions.hideNavigationBar
import com.gs.pickleball.extensions.showNavigationBar
import com.gs.pickleball.extensions.viewBinding
import com.gs.pickleball.ui.base.viewmodel.InsetsViewModel
import com.gs.pickleball.ui.base.viewmodel.KeyboardChangeViewModel
import com.gs.pickleball.ui.customviews.keyboard.KeyboardHeightProviderApi30Below
import com.gs.pickleball.ui.customviews.keyboard.KeyboardHeightProviderApi30Plus
import com.gs.pickleball.ui.customviews.keyboard.KeyboardListener
import com.gs.pickleball.ui.customviews.keyboard.KeyboardProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(), CoroutineScope {
    private lateinit var askCheckPermission: ActivityResultLauncher<Array<String>>
    private val insetsViewModel: InsetsViewModel by viewModels()
    private val keyboardChangeViewModel: KeyboardChangeViewModel by viewModels()
    private var _onPermissionResult: ((Map<String, Boolean>) -> Unit)? = null
    private lateinit var startActivityIntent: ActivityResultLauncher<Intent>
    private var onResult: ((ActivityResult) -> Unit)? = null
    open protected val isRegisterOnKeyboardListener = false

    private var keyboardHeightProvider: KeyboardProvider? = null
    private var onChangeKeyBoardHeight = ArrayList<(Int) -> Unit>()

    fun addOnChangeKeyBoardHeightListener(listener: (Int) -> Unit) {
        onChangeKeyBoardHeight.add(listener)
    }

    fun removeOnChangeKeyBoardHeightListener(listener: (Int) -> Unit) {
        onChangeKeyBoardHeight.remove(listener)
    }

    private fun getKeyboardListener() = object : KeyboardListener {
        override fun onHeightChanged(height: Int) {
            val _height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                height
            } else {
                if (isHideNavigationBar) {
                    if (height == 0) {
                        0
                    } else {
                        height + (getNavigationBarSize())
                    }
                } else {
                    height
                }
            }
            if (isHideNavigationBar && height == 0) {
                hideNavigationBar()
            }

            if (isHideStatusBar) {
                window?.let {
                    hideSystemBars(it)
                }
            }

            if (height > 0) {
                showNavigationBar()
            }

            keyboardChangeViewModel.updateKeyboardHeight(_height)

            onChangeKeyBoardHeight.forEach {
                it.invoke(_height)
            }
        }
    }

    open val isHideNavigationBar = true

    protected abstract fun bindingProvider(inflater: LayoutInflater): T

    protected val viewBinding by viewBinding { inflater ->
        bindingProvider(inflater)
    }

    private val job = Job()

    open val isHideStatusBar = true
    open val isSpaceStatusBar = true
    open val isSpaceDisplayCutout = true


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    open fun showFirstScreen() {}

    /**
     * Kiểm tra xem người dùng có từ chối quyền vĩnh viễn không
     *
     * @param permissions Danh sách các quyền cần kiểm tra
     * @return Trả về true nếu người dùng từ chối quyền vĩnh viễn, ngược lại trả về false
     */
    fun requiredOpenSettingPermission(permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission
                )
            ) return true
        }
        return false
    }

    fun Activity.getNavigationBarSize(): Int {
        val metrics = resources.displayMetrics
        val usableHeight = metrics.heightPixels
        val realMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(realMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(realMetrics)
        }

        val realHeight = realMetrics.heightPixels

        return if (realHeight > usableHeight) {
            realHeight - usableHeight // Chiều cao navigation bar
        } else {
            0 // Không có navigation bar
        }
    }

    private var _innerPadding: Insets? = null

    /**Store system padding*/
    val innerPadding: Insets?
        get() {
            return _innerPadding
        }

    private var listVipListener: MutableList<() -> Unit> = mutableListOf()

    open fun init(savedInstanceState: Bundle?) {}

    open var isAwaitCallInitView = false

    open fun getSurfaceView(): View = viewBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isRegisterOnKeyboardListener) {
            keyboardHeightProvider =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) KeyboardHeightProviderApi30Plus(
                    this
                ) else KeyboardHeightProviderApi30Below(this)
            keyboardHeightProvider?.addKeyboardListener(getKeyboardListener())
        }
        askCheckPermission =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                _onPermissionResult?.invoke(permissions)
            }

        startActivityIntent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onResult?.invoke(result)
        }
        setContentView(viewBinding.root)
        if (isHideNavigationBar) {
            hideNavigationBar()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(getCurrentLanguageCode())
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(
                    getCurrentLanguageCode()
                )
            )
        }
        enableEdgeToEdge()

        if (isHideStatusBar) {
            hideSystemBars(window)
        }
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { _, windowInsets ->
            // Lấy thông tin về kích thước của các thanh hệ thống
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            // Áp dụng padding cho view để nó không bị che
            // Ở đây, ta thêm padding ở trên cùng và dưới cùng của layout
            val topPadding =
                if (isSpaceDisplayCutout && cutoutInsets.top > 0) cutoutInsets.top else if (isSpaceStatusBar) systemBarInsets.top else 0
            val bottomPadding = if (isHideNavigationBar) 0 else systemBarInsets.bottom
            getSurfaceView().setPadding(
                systemBarInsets.left,
                topPadding,
                systemBarInsets.right,
                bottomPadding
            )

            insetsViewModel.updateInsets(
                InsetsViewModel.WrapInsets(
                    insets = Insets.of(
                        systemBarInsets.left,
                        max(cutoutInsets.top, systemBarInsets.top),
                        systemBarInsets.right,
                        bottomPadding
                    ),
                    hideStatusBar = isHideStatusBar,
                    isActivitySpaceStatusBar = isSpaceStatusBar || isSpaceDisplayCutout,
                )
            )
            // Trả về windowInsets để các view con có thể tiếp tục xử lý
            windowInsets // Hoặc windowInsets nếu muốn các view con tiếp tục nhận
        }
        showFirstScreen()
        init(savedInstanceState)
        if (!isAwaitCallInitView) {
            initViews(savedInstanceState)
        }
        preloadData()
        handleObservable()
    }

    fun addListenerVipChange(listener: () -> Unit) {
        listener.invoke()
    }

    fun removeListenerVipChange(listener: () -> Unit) {
        listVipListener.remove(listener)
    }

    open fun onChangeVipState(isVip: Boolean) {

    }

    private fun hideSystemBars(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController ?: return
            insetsController.hide(WindowInsets.Type.statusBars())
            insetsController.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            val insetsController = WindowInsetsControllerCompat(
                window,
                window.decorView
            )
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    open fun preloadData() {}

    open fun initViews(savedInstanceState: Bundle?) {}

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isHideNavigationBar) {
            hideNavigationBar()
        }

        if (isHideStatusBar) {
            window?.let {
                hideSystemBars(it)
            }
        }
    }

    /**
     * Yêu cầu quyền từ người dùng
     *
     * @param permissions Danh sách các quyền cần yêu cầu
     * @param onPermissionGrant Callback được gọi khi người dùng cấp quyền hoặc từ chối quyền
     * @see _onPermissionResult
     */
    fun requestPermission(
        permissions: Array<String>,
        onPermissionGrant: (Map<String, Boolean>) -> Unit
    ) {
        _onPermissionResult = onPermissionGrant
        askCheckPermission.launch(
            permissions
        )
    }

    /**
     * Khởi chạy một Activity để lấy kết quả trả về.
     *
     * @param intent Intent để khởi chạy Activity.
     * @param onResult Callback được gọi khi Activity trả về kết quả.
     */
    fun launchForResult(intent: Intent, onResult: (ActivityResult) -> Unit) {
        this.onResult = onResult
        startActivityIntent.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider?.onResume()
    }

    override fun onPause() {
        try {
            super.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        keyboardHeightProvider?.onPause()
    }

    open fun handleObservable() {

    }
}