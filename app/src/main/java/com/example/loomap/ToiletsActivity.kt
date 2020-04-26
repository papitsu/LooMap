package com.example.loomap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_toilets.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ToiletsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilets)

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
            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                .build()
            var toilets = db.toiletDao().getAllToilets()
            toilets = toilets.sortedWith(compareBy { it.name })

            var ratings: MutableList<Float?> = mutableListOf()

            if (toilets.isNotEmpty()) {
                for (toilet in toilets) {
                    val visits = db.visitDao().getByToilet(toilet.uid!!)
                    if (visits.isNotEmpty()) {
                        ratings.add(visits.map{it.rating}.average().toFloat())
                    } else {
                        ratings.add(null)
                    }
                }
            }
            db.close()
            uiThread {

                if (toilets.isNotEmpty()) {
                    val adapter = ToiletsAdapter(applicationContext, toilets, ratings)
                    toilets_list_view.adapter = adapter

                    toilets_list_view.setOnItemClickListener { _, _, position, _ ->
                        val element = adapter.getItem(position) as Toilet // The item that was clicked
                        val intent = Intent(applicationContext, ToiletActivity::class.java)
                            .putExtra("uid", element.uid)
                            .putExtra("name", element.name)
                            .putExtra("category", element.category)
                            .putExtra("description", element.description)
                            .putExtra("latitude", element.latitude)
                            .putExtra("longitude", element.longitude)
                        startActivity(intent)
                    }
                }
            }
        }
    }

}