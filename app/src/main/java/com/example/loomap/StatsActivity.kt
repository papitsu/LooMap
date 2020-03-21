package com.example.loomap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_stats.*

class StatsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        map_button.setOnClickListener {
            finish()
        }

        settings_button.setOnClickListener {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
            finish()
        }

        view_toilet_button.setOnClickListener {
            startActivity(Intent(applicationContext, ToiletActivity::class.java))
        }

        val data = arrayOf("Toilets visited", "Total visits", "Average visits per month", "Additional stat", "One more stat", "Yet another stat", "One can't have too many stats", "I like stats", "Can there be any more stats", "Yes there can", "Shouldn't these end at some point?", "Won't somebody please end these stats", "This is the last stat", "Just kidding")
        val statsAdapter = StatsAdapter(applicationContext, data)
        stats_list_view.adapter = statsAdapter

    }
}