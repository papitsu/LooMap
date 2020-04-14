package com.example.loomap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_stats.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import java.util.concurrent.TimeUnit

private var labels = mutableListOf<String>()
private var values = mutableListOf<Any>()
private var toilets = listOf<Toilet>()
private var visits = listOf<Visit>()

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()
            toilets = db.toiletDao().getAllToilets()
            visits = db.visitDao().getVisits()

            calculateStats()

            db.close()
            uiThread {
                val statsAdapter = StatsAdapter(applicationContext, labels, values)
                stats_list_view.adapter = statsAdapter
            }
        }
    }

    private fun calculateStats() {
        if (visits.isNotEmpty()) {
            labels.add("Toilets visited")
            values.add(toilets.count())

            labels.add("Total toilet visits")
            values.add(visits.count())

            labels.add("Average toilet visits per month")

            val oldestVisit = visits.minBy { it.time }!!
            val newestVisit = visits.maxBy { it.time }!!
            var msDiff = newestVisit.time - oldestVisit.time
            values.add((30*visits.count()/(TimeUnit.MILLISECONDS.toDays(msDiff))).toFloat())

            labels.add("Days since first toilet visit")
            msDiff = Calendar.getInstance().timeInMillis - oldestVisit.time
            values.add(TimeUnit.MILLISECONDS.toDays(msDiff))
        }
    }
}