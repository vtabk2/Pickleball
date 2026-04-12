package com.gs.pickleball.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.data.PlayerEntity
import com.gs.pickleball.databinding.ActivityMainBinding
import com.gs.pickleball.ui.base.activity.BaseActivity
import com.gs.pickleball.ui.common.AccentInsensitiveAdapter
import com.gs.pickleball.ui.match.MatchActivity
import com.gs.pickleball.ui.match.MatchListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel: MainViewModel by viewModels()

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
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

        viewBinding.openMatchButton.setOnClickListener {
            startActivity(Intent(this, MatchActivity::class.java))
        }

        viewBinding.openMatchListButton.setOnClickListener {
            startActivity(Intent(this, MatchListActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect { renderPlayers(it) }
            }
        }
    }

    private fun renderPlayers(players: List<PlayerEntity>) {
        bindAdapter(viewBinding.nameInput, players.map { it.name })
        viewBinding.savedList.text = if (players.isEmpty()) {
            "(Chưa có dữ liệu)"
        } else {
            players.joinToString("\n") { "- ${it.name}" }
        }
    }

    private fun bindAdapter(view: AutoCompleteTextView, items: List<String>) {
        view.threshold = 1
        val adapter = AccentInsensitiveAdapter(this, items)
        view.setAdapter(adapter)
    }
}