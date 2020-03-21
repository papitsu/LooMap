package com.example.loomap

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_toilet.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class AddToiletActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_toilet)

        back_button.setOnClickListener {
            finish()
        }
    }
}