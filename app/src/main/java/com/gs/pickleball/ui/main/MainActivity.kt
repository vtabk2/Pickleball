package com.gs.pickleball.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.gs.pickleball.databinding.ActivityMainBinding
import com.gs.pickleball.ui.base.activity.CoreActivity
import com.gs.pickleball.ui.match.MatchActivity
import com.gs.pickleball.ui.match.MatchListActivity
import com.gs.pickleball.ui.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : CoreActivity<ActivityMainBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.openPlayerButton.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        viewBinding.openMatchButton.setOnClickListener {
            startActivity(Intent(this, MatchActivity::class.java))
        }

        viewBinding.openMatchListButton.setOnClickListener {
            startActivity(Intent(this, MatchListActivity::class.java))
        }
    }
}
