package com.gs.pickleball

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.data.MatchEntity
import com.gs.pickleball.data.PlayerEntity
import com.gs.pickleball.databinding.ActivityMatchBinding
import com.gs.pickleball.ui.base.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MatchActivity : BaseActivity<ActivityMatchBinding>() {
    private val viewModel: MatchViewModel by viewModels()
    private var players: List<PlayerEntity> = emptyList()

    override fun bindingProvider(inflater: LayoutInflater): ActivityMatchBinding {
        return ActivityMatchBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        setupMatchType()
        setupSave()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.players.collect {
                        players = it
                        bindPlayerAdapters(it)
                    }
                }
                launch {
                    viewModel.matches.collect { renderMatches(it) }
                }
            }
        }
    }

    private fun setupMatchType() {
        viewBinding.matchTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val isFour = checkedId == R.id.matchTypeFour
            viewBinding.player3Layout.visibility = if (isFour) View.VISIBLE else View.GONE
            viewBinding.player4Layout.visibility = if (isFour) View.VISIBLE else View.GONE
        }
        viewBinding.matchTypeTwo.isChecked = true
    }

    private fun setupSave() {
        viewBinding.saveMatchButton.setOnClickListener {
            clearErrors()
            val isFour = viewBinding.matchTypeFour.isChecked
            val p1 = viewBinding.player1Input.text?.toString()?.trim().orEmpty()
            val p2 = viewBinding.player2Input.text?.toString()?.trim().orEmpty()
            val p3 = viewBinding.player3Input.text?.toString()?.trim().orEmpty()
            val p4 = viewBinding.player4Input.text?.toString()?.trim().orEmpty()
            val sA = viewBinding.scoreTeamA.text?.toString()?.trim().orEmpty()
            val sB = viewBinding.scoreTeamB.text?.toString()?.trim().orEmpty()

            if (p1.isBlank()) {
                viewBinding.player1Layout.error = "Chọn người chơi 1"
                return@setOnClickListener
            }
            if (p2.isBlank()) {
                viewBinding.player2Layout.error = "Chọn người chơi 2"
                return@setOnClickListener
            }
            if (isFour && p3.isBlank()) {
                viewBinding.player3Layout.error = "Chọn người chơi 3"
                return@setOnClickListener
            }
            if (isFour && p4.isBlank()) {
                viewBinding.player4Layout.error = "Chọn người chơi 4"
                return@setOnClickListener
            }
            val scoreA = sA.toIntOrNull() ?: run {
                viewBinding.scoreTeamALayout.error = "Nhập điểm đội A"
                return@setOnClickListener
            }
            val scoreB = sB.toIntOrNull() ?: run {
                viewBinding.scoreTeamBLayout.error = "Nhập điểm đội B"
                return@setOnClickListener
            }

            val p1Id = findPlayerId(p1) ?: return@setOnClickListener.also {
                viewBinding.player1Layout.error = "Người chơi không tồn tại"
            }
            val p2Id = findPlayerId(p2) ?: return@setOnClickListener.also {
                viewBinding.player2Layout.error = "Người chơi không tồn tại"
            }
            val p3Id = if (isFour) findPlayerId(p3) else null
            if (isFour && p3Id == null) {
                viewBinding.player3Layout.error = "Người chơi không tồn tại"
                return@setOnClickListener
            }
            val p4Id = if (isFour) findPlayerId(p4) else null
            if (isFour && p4Id == null) {
                viewBinding.player4Layout.error = "Người chơi không tồn tại"
                return@setOnClickListener
            }

            val match = MatchEntity(
                matchType = if (isFour) 4 else 2,
                player1Id = p1Id,
                player2Id = p2Id,
                player3Id = p3Id,
                player4Id = p4Id,
                scoreTeamA = scoreA,
                scoreTeamB = scoreB
            )
            viewModel.saveMatch(match)
            clearInputs()
        }
    }

    private fun bindPlayerAdapters(players: List<PlayerEntity>) {
        val names = players.map { it.name }
        bindAdapter(viewBinding.player1Input, names)
        bindAdapter(viewBinding.player2Input, names)
        bindAdapter(viewBinding.player3Input, names)
        bindAdapter(viewBinding.player4Input, names)
    }

    private fun bindAdapter(view: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        view.setAdapter(adapter)
    }

    private fun renderMatches(matches: List<MatchEntity>) {
        viewBinding.matchList.text = if (matches.isEmpty()) {
            "(Chưa có trận đấu)"
        } else {
            val playersById = players.associateBy { it.id }
            matches.joinToString("\n\n") { match ->
                val p1 = playersById[match.player1Id]?.name ?: "#${match.player1Id}"
                val p2 = playersById[match.player2Id]?.name ?: "#${match.player2Id}"
                if (match.matchType == 2) {
                    "$p1 vs $p2\nKết quả: ${match.scoreTeamA} - ${match.scoreTeamB}"
                } else {
                    val p3 = match.player3Id?.let { playersById[it]?.name } ?: "?"
                    val p4 = match.player4Id?.let { playersById[it]?.name } ?: "?"
                    "$p1 + $p2 vs $p3 + $p4\nKết quả: ${match.scoreTeamA} - ${match.scoreTeamB}"
                }
            }
        }
    }

    private fun clearErrors() {
        viewBinding.player1Layout.error = null
        viewBinding.player2Layout.error = null
        viewBinding.player3Layout.error = null
        viewBinding.player4Layout.error = null
        viewBinding.scoreTeamALayout.error = null
        viewBinding.scoreTeamBLayout.error = null
    }

    private fun clearInputs() {
        viewBinding.player1Input.setText("")
        viewBinding.player2Input.setText("")
        viewBinding.player3Input.setText("")
        viewBinding.player4Input.setText("")
        viewBinding.scoreTeamA.setText("")
        viewBinding.scoreTeamB.setText("")
    }

    private fun findPlayerId(name: String): Long? {
        return players.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id
    }
}
