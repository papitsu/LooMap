package com.example.loomap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        map_button.setOnClickListener {
            finish()
        }

        stats_button.setOnClickListener {
            startActivity(Intent(applicationContext, StatsActivity::class.java))
            finish()
        }
    }
}