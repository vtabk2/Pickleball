package com.gs.pickleball

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.data.PlayerEntity
import com.gs.pickleball.databinding.ActivityMainBinding
import com.gs.pickleball.ui.base.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel: MainViewModel by viewModels()

    override fun bindingProvider(inflater: android.view.LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.saveButton.setOnClickListener {
            val name = viewBinding.nameInput.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                viewBinding.nameInputLayout.error = "Vui lòng nhập tên"
                return@setOnClickListener
            }
            viewBinding.nameInputLayout.error = null
            viewModel.savePlayer(name)
            viewBinding.nameInput.setText("")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect { renderPlayers(it) }
            }
        }
    }

    private fun renderPlayers(players: List<PlayerEntity>) {
        viewBinding.savedList.text = if (players.isEmpty()) {
            "(Chưa có dữ liệu)"
        } else {
            players.joinToString("\n") { "- ${it.name}" }
        }
    }
}
