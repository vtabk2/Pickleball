package com.gs.pickleball.ui.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.databinding.ActivityPlayerBinding
import com.gs.pickleball.ui.base.activity.CoreActivity
import com.gs.pickleball.ui.common.AccentInsensitiveAdapter
import com.gs.pickleball.ui.match.MatchListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerActivity : CoreActivity<ActivityPlayerBinding>() {
    private val viewModel: PlayerViewModel by viewModels()
    private var players: List<com.gs.pickleball.data.PlayerEntity> = emptyList()

    override fun bindingProvider(inflater: LayoutInflater): ActivityPlayerBinding {
        return ActivityPlayerBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.saveButton.setOnClickListener {
            val name = viewBinding.nameInput.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                viewBinding.nameInputLayout.error = getString(
                    com.gs.pickleball.R.string.error_player_name_required
                )
                return@setOnClickListener
            }
            viewBinding.nameInputLayout.error = null
            viewModel.savePlayer(name)
            viewBinding.nameInput.setText("")
        }

        viewBinding.playerListView.setOnItemClickListener { _, _, position, _ ->
            val player = players.getOrNull(position) ?: return@setOnItemClickListener
            val intent = Intent(this, MatchListActivity::class.java)
            intent.putExtra(MatchListActivity.EXTRA_PLAYER_ID, player.id)
            intent.putExtra(MatchListActivity.EXTRA_PLAYER_NAME, player.name)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect { renderPlayers(it) }
            }
        }
    }

    private fun renderPlayers(items: List<com.gs.pickleball.data.PlayerEntity>) {
        players = items
        bindAdapter(viewBinding.nameInput, items.map { it.name })

        if (items.isEmpty()) {
            viewBinding.emptyText.visibility = View.VISIBLE
            viewBinding.playerListView.adapter = null
            return
        }

        viewBinding.emptyText.visibility = View.GONE
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            items.map { it.name }
        )
        viewBinding.playerListView.adapter = adapter
    }

    private fun bindAdapter(view: AutoCompleteTextView, items: List<String>) {
        view.threshold = 1
        val adapter = AccentInsensitiveAdapter(this, items)
        view.setAdapter(adapter)
    }
}
