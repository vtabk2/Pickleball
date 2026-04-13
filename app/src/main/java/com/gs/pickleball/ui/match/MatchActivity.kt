package com.gs.pickleball.ui.match

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.R
import com.gs.pickleball.data.MatchEntity
import com.gs.pickleball.data.PlayerEntity
import com.gs.pickleball.databinding.ActivityMatchBinding
import com.gs.pickleball.ui.base.activity.CoreActivity
import com.gs.pickleball.ui.common.AccentInsensitiveAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MatchActivity : CoreActivity<ActivityMatchBinding>() {
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
        viewBinding.matchTypeFour.isChecked = true
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
                viewBinding.player1Layout.error = getString(R.string.error_select_player_1)
                return@setOnClickListener
            }
            if (p2.isBlank()) {
                viewBinding.player2Layout.error = getString(R.string.error_select_player_2)
                return@setOnClickListener
            }
            if (isFour && p3.isBlank()) {
                viewBinding.player3Layout.error = getString(R.string.error_select_player_3)
                return@setOnClickListener
            }
            if (isFour && p4.isBlank()) {
                viewBinding.player4Layout.error = getString(R.string.error_select_player_4)
                return@setOnClickListener
            }
            val scoreA = sA.toIntOrNull() ?: run {
                viewBinding.scoreTeamALayout.error = getString(R.string.error_enter_team_a_score)
                return@setOnClickListener
            }
            val scoreB = sB.toIntOrNull() ?: run {
                viewBinding.scoreTeamBLayout.error = getString(R.string.error_enter_team_b_score)
                return@setOnClickListener
            }

            resolvePlayersAndSave(
                isFour = isFour,
                p1 = p1,
                p2 = p2,
                p3 = p3,
                p4 = p4,
                scoreA = scoreA,
                scoreB = scoreB
            )
        }

        viewBinding.openMatchButton.setOnClickListener {
            startActivity(Intent(this, MatchActivity::class.java))
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
        view.threshold = 1
        val adapter = AccentInsensitiveAdapter(this, items)
        view.setAdapter(adapter)
    }

    private fun renderMatches(matches: List<MatchEntity>) {
        viewBinding.matchList.text = if (matches.isEmpty()) {
            getString(R.string.empty_matches)
        } else {
            val playersById = players.associateBy { it.id }
            matches.joinToString("\n\n") { match ->
                val p1 = playersById[match.player1Id]?.name ?: fallbackPlayerName(match.player1Id)
                val p2 = playersById[match.player2Id]?.name ?: fallbackPlayerName(match.player2Id)
                val teams = if (match.matchType == 2) {
                    joinTwoTeams(p1, p2)
                } else {
                    val p3 = match.player3Id?.let { playersById[it]?.name } ?: "?"
                    val p4 = match.player4Id?.let { playersById[it]?.name } ?: "?"
                    joinFourTeams(p1, p2, p3, p4)
                }
                teams + "\n" + buildResultText(match.scoreTeamA, match.scoreTeamB)
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
        viewBinding.player1Input.text?.clear()
        viewBinding.player2Input.text?.clear()
        viewBinding.player3Input.text?.clear()
        viewBinding.player4Input.text?.clear()
        viewBinding.scoreTeamA.text?.clear()
        viewBinding.scoreTeamB.text?.clear()
    }

    private fun resolvePlayersAndSave(
        isFour: Boolean,
        p1: String,
        p2: String,
        p3: String,
        p4: String,
        scoreA: Int,
        scoreB: Int
    ) {
        viewModel.findOrCreatePlayer(p1) { player1 ->
            viewModel.findOrCreatePlayer(p2) { player2 ->
                if (isFour) {
                    viewModel.findOrCreatePlayer(p3) { player3 ->
                        viewModel.findOrCreatePlayer(p4) { player4 ->
                            saveMatchWithPlayers(
                                isFour,
                                player1.id,
                                player2.id,
                                player3.id,
                                player4.id,
                                scoreA,
                                scoreB
                            )
                        }
                    }
                } else {
                    saveMatchWithPlayers(
                        isFour,
                        player1.id,
                        player2.id,
                        null,
                        null,
                        scoreA,
                        scoreB
                    )
                }
            }
        }
    }

    private fun saveMatchWithPlayers(
        isFour: Boolean,
        p1Id: Long,
        p2Id: Long,
        p3Id: Long?,
        p4Id: Long?,
        scoreA: Int,
        scoreB: Int
    ) {
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
        runOnUiThread { clearInputs() }
    }

    private fun fallbackPlayerName(playerId: Long): String = "#$playerId"

    private fun joinTwoTeams(player1: String, player2: String): String {
        return "$player1 vs $player2"
    }

    private fun joinFourTeams(player1: String, player2: String, player3: String, player4: String): String {
        return "$player1 + $player2 vs $player3 + $player4"
    }

    private fun buildResultText(scoreTeamA: Int, scoreTeamB: Int): String {
        return getString(R.string.label_result_prefix) + " " + scoreTeamA + " - " + scoreTeamB
    }
}