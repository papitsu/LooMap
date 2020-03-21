package com.example.loomap

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_toilet.*
import org.jetbrains.anko.toast

class ToiletActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet)

        val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.title = null
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })

        add_visit_button.setOnClickListener {
            startActivity(Intent(applicationContext, AddVisitActivity::class.java))
        }

        val data = arrayOf("Toilets visited", "Total visits", "Average visits per month", "Additional stat", "One more stat", "Yet another stat", "One can't have too many stats", "I like stats", "Can there be any more stats", "Yes there can", "Shouldn't these end at some point?", "Won't somebody please end these stats", "This is the last stat", "Just kidding")
        val visitsAdapter = VisitsAdapter(applicationContext, data)
        lv_toilet_visits.adapter = visitsAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_toilet_info, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_toilet_menu_action) {
            toast("Clicked edit toilet!")
            startActivity(Intent(applicationContext, EditToiletActivity::class.java))
            return true
        }
        if (id == R.id.add_photos_menu_action) {
            toast("Clicked add photos!")
            return true
        }
        if (id == R.id.delete_toilet_menu_action) {
            toast("Clicked delete toilet!")
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}