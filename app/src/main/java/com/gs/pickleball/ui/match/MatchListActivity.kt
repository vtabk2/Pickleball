package com.gs.pickleball.ui.match

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gs.pickleball.R
import com.gs.pickleball.databinding.ActivityMatchListBinding
import com.gs.pickleball.ui.base.activity.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MatchListActivity : BaseActivity<ActivityMatchListBinding>() {
    private val viewModel: MatchViewModel by viewModels()
    private var matches: List<com.gs.pickleball.data.MatchEntity> = emptyList()
    private var players: Map<Long, com.gs.pickleball.data.PlayerEntity> = emptyMap()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun bindingProvider(inflater: LayoutInflater): ActivityMatchListBinding {
        return ActivityMatchListBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.matchListView.setOnItemClickListener { _, _, position, _ ->
            val match = matches.getOrNull(position) ?: return@setOnItemClickListener
            val intent = Intent(this, MatchDetailActivity::class.java)
            intent.putExtra(MatchDetailActivity.EXTRA_MATCH_ID, match.id)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.players.collect { list ->
                        players = list.associateBy { it.id }
                        render()
                    }
                }
                launch {
                    viewModel.matches.collect { list ->
                        matches = list
                        render()
                    }
                }
            }
        }
    }

    private fun render() {
        if (matches.isEmpty()) {
            viewBinding.emptyText.visibility = View.VISIBLE
            viewBinding.matchListView.adapter = null
            return
        }
        viewBinding.emptyText.visibility = View.GONE

        val items = matches.map { match ->
            val p1 = players[match.player1Id]?.name ?: "#${match.player1Id}"
            val p2 = players[match.player2Id]?.name ?: "#${match.player2Id}"
            val title = if (match.matchType == 2) {
                "$p1 vs $p2"
            } else {
                val p3 = match.player3Id?.let { players[it]?.name } ?: "?"
                val p4 = match.player4Id?.let { players[it]?.name } ?: "?"
                "$p1 + $p2 vs $p3 + $p4"
            }
            val dateText = dateFormat.format(Date(match.createdAt))
            val subtitle = "Kết quả: ${match.scoreTeamA}-${match.scoreTeamB} • $dateText"
            ListItem(title, subtitle)
        }

        val adapter = object : ArrayAdapter<ListItem>(this, R.layout.item_match_list, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView
                    ?: layoutInflater.inflate(R.layout.item_match_list, parent, false)
                val item = getItem(position) ?: return view
                view.findViewById<TextView>(R.id.itemTitle).text = item.title
                view.findViewById<TextView>(R.id.itemSubtitle).text = item.subtitle
                return view
            }
        }
        viewBinding.matchListView.adapter = adapter
    }

    private data class ListItem(
        val title: String,
        val subtitle: String
    )
}
