package com.example.loomap

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_toilet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class ToiletActivity : AppCompatActivity(), OnMapReadyCallback {

    private var gMap: GoogleMap? = null
    private var toiletLocation: LatLng? = null
    private var toiletId: Int = 0
    private var toiletName: String? = ""
    private var toiletCategory: String? = ""
    private var toiletDescription: String? = ""
    private var visits = emptyList<Visit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet)
        (map_fragment_view_toilet as SupportMapFragment).getMapAsync(this)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        add_visit_button.setOnClickListener {
            val visitIntent = Intent(applicationContext, AddVisitActivity::class.java)
                .putExtra("toiletId", toiletId)
                .putExtra("toiletName", toiletName)

            startActivity(visitIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Handle intent
        toiletId = intent.getIntExtra("uid", 0)
        toiletName = intent.getStringExtra("name")
        toiletCategory = intent.getStringExtra("category")
        toiletDescription = intent.getStringExtra("description")
        val toiletLatitude = intent.getDoubleExtra("latitude", 0.toDouble())
        val toiletLongitude = intent.getDoubleExtra("longitude", 0.toDouble())

        toilet_name.text =
            String.format(getString(R.string.toilet_name_string), toiletName, toiletCategory)
        toilet_description.text = toiletDescription
        toiletLocation = LatLng(toiletLatitude, toiletLongitude)

        refreshList()
        if (gMap != null) {
            refreshMap()
        }
    }

    private fun sortData() {
        visits = visits.sortedWith(compareByDescending { it.time })
    }

    private fun refreshList() {
        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()
            visits = db.visitDao().getByToilet(toiletId)
            sortData()
            db.close()
            uiThread {

                if (visits.isNotEmpty()) {
                    tv_visits_label.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.black
                        )
                    )
                    // List all visits
                    val visitsAdapter = ToiletVisitsAdapter(applicationContext, visits)
                    lv_toilet_visits.adapter = visitsAdapter

                    // Update rating
                    star_toilet_rating.rating = visits.map { it.rating }.average().toFloat()

                    // Add click handler to list items
                    lv_toilet_visits.setOnItemClickListener { _, _, position, _ ->
                        val element =
                            visitsAdapter.getItem(position) as Visit // The item that was clicked
                        val intent = Intent(applicationContext, VisitActivity::class.java)
                            .putExtra("uid", element.uid)
                            .putExtra("toiletId", element.toiletId)
                            .putExtra("comment", element.comment)
                            .putExtra("rating", element.rating)
                            .putExtra("photoId", element.photoId)
                            .putExtra("time", element.time)
                        startActivity(intent)
                    }
                } else {
                    tv_visits_label.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.gray
                        )
                    )
                    val visitsAdapter = ToiletVisitsAdapter(applicationContext, visits)
                    lv_toilet_visits.adapter = visitsAdapter
                }
            }
        }
    }

    private fun refreshMap() {

        if (toiletLocation != null) {
            val tempLocation: LatLng = toiletLocation!!
            with(gMap!!) {
                clear()
                moveCamera(CameraUpdateFactory.newLatLngZoom(tempLocation, 13f))
                addMarker(MarkerOptions()
                    .position(tempLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(getString(R.string.marker_color).toFloat()))
                )
            }
        } else {
            with(gMap!!) {
                clear()
            }
        }
    }

    private fun deleteToiletAlert() {
        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { _: DialogInterface, _: Int -> deleteThisToilet(toiletId) }
        val negativeButtonClick = { _: DialogInterface, _: Int -> }

        with(builder)
        {
            setTitle("Confirmation")
            setMessage("Are you sure you want to delete this toilet?")
            setPositiveButton("Delete", positiveButtonClick)
            setNegativeButton("Cancel", negativeButtonClick)
            show()
        }
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

            val editIntent = Intent(applicationContext, EditToiletActivity::class.java)
                .putExtra("uid", toiletId)
                .putExtra("name", toiletName)
                .putExtra("category", toiletCategory)
                .putExtra("description", toiletDescription)
                .putExtra("latitude", toiletLocation!!.latitude)
                .putExtra("longitude", toiletLocation!!.longitude)

            startActivityForResult(editIntent, 1)

            return true
        }
        /*
        if (id == R.id.add_photos_menu_action) {
            toast("Clicked add photos!")
            return true
        }
        */
        if (id == R.id.delete_toilet_menu_action) {
            deleteToiletAlert()
            return true

        }
        return super.onOptionsItemSelected(item)
    }

    // This method is called when the Edit Toilet Activity finishes.
    // If toilet is Deleted on Edit Toilet Activity, intent.keyCode == -1
    // and Toilet Activity will be finished. Otherwise, remain on Toilet Activity.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (resultCode == -1) {
            deleteThisToilet(toiletId)
        }

        if (resultCode == 1) {
            intent = data
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return
        gMap!!.apply { uiSettings.isMapToolbarEnabled = false } // Remove navigation option

        if (toiletLocation != null) {
            val tempLocation: LatLng = toiletLocation!!
            with(gMap!!) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(tempLocation, 13f))
                addMarker(MarkerOptions()
                    .position(tempLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(getString(R.string.marker_color).toFloat()))
                )
            }
        }
    }

    private fun deleteThisToilet(toiletId: Int) {
        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()

            db.toiletDao().delete(toiletId)
            db.close()
        }
        finish()
    }
}