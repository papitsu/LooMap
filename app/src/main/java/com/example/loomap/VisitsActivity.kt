package com.example.loomap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_visits.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

private var visits = emptyList<Visit>()

class VisitsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visits)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun sortData() {
        visits = visits.sortedWith(compareByDescending {it.time})
    }

    private fun refreshList() {
        var toiletNames = mutableListOf<String>()
        doAsync {
            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                .build()
            visits = db.visitDao().getVisits()
            sortData()
            for (visit in visits) {
                toiletNames.add(db.toiletDao().getById(visit.toiletId).name)
            }
            db.close()
            uiThread {

                if (visits.isNotEmpty()) {
                    val adapter = VisitsAdapter(applicationContext, visits, toiletNames)
                    visits_list_view.adapter = adapter

                    visits_list_view.setOnItemClickListener { _, _, position, _ ->
                        val element = adapter.getItem(position) as Visit // The item that was clicked
                        val intent = Intent(applicationContext, VisitActivity::class.java)
                            .putExtra("uid", element.uid)
                            .putExtra("toiletId", element.toiletId)
                            .putExtra("time", element.time)
                            .putExtra("comment", element.comment)
                            .putExtra("rating", element.rating)
                        startActivity(intent)
                    }

                }
            }
        }
    }
}