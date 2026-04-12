package com.gs.pickleball.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.data.MatchEntity
import com.gs.pickleball.data.PlayerEntity
import com.gs.pickleball.databinding.ActivityMatchDetailBinding
import com.gs.pickleball.ui.base.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MatchDetailActivity : BaseActivity<ActivityMatchDetailBinding>() {
    private val viewModel: MatchDetailViewModel by viewModels()

    override fun bindingProvider(inflater: LayoutInflater): ActivityMatchDetailBinding {
        return ActivityMatchDetailBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        val matchId = intent.getLongExtra(EXTRA_MATCH_ID, -1L)
        if (matchId <= 0L) {
            viewBinding.detailTitle.text = "Không tìm thấy trận đấu"
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.match.collect { match ->
                        val players = viewModel.players.value
                        render(match, players)
                    }
                }
                launch {
                    viewModel.players.collect { players ->
                        val match = viewModel.match.value
                        render(match, players)
                    }
                }
            }
        }

        viewModel.load(matchId)
    }

    private fun render(match: MatchEntity?, players: List<PlayerEntity>) {
        if (match == null) {
            viewBinding.detailTitle.text = "Không tìm thấy trận đấu"
            return
        }
        val playersById = players.associateBy { it.id }
        val p1 = playersById[match.player1Id]?.name ?: "#${match.player1Id}"
        val p2 = playersById[match.player2Id]?.name ?: "#${match.player2Id}"
        val isFour = match.matchType == 4
        val teamA = if (isFour) {
            "$p1 + $p2"
        } else {
            p1
        }
        val teamB = if (isFour) {
            val p3 = match.player3Id?.let { playersById[it]?.name } ?: "?"
            val p4 = match.player4Id?.let { playersById[it]?.name } ?: "?"
            "$p3 + $p4"
        } else {
            p2
        }
        val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(match.createdAt))

        viewBinding.detailTitle.text = "Chi tiết trận đấu"
        viewBinding.detailType.text = if (isFour) "Thể thức: 4 người" else "Thể thức: 2 người"
        viewBinding.detailTeams.text = "$teamA vs $teamB"
        viewBinding.detailScore.text = "Kết quả: ${match.scoreTeamA} - ${match.scoreTeamB}"
        viewBinding.detailDate.text = "Ngày thi đấu: $dateText"
    }

    companion object {
        const val EXTRA_MATCH_ID = "match_id"
    }
}
