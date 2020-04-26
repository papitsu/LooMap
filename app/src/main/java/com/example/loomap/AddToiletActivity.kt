package com.example.loomap

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_add_toilet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class AddToiletActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private var toiletLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_toilet)
        (map_fragment_add_toilet as SupportMapFragment).getMapAsync(this)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { confirmCancel() }

        save_button.setOnClickListener {

            val name = inputName.text.toString()
            val description = inputDescription.text.toString()
            val tempLocation = toiletLocation

            if (name.isEmpty()) {
                toast("Please provide reminder text")
                return@setOnClickListener
            }

            if (tempLocation == null) {
                toast("Please select a location on the map")
                return@setOnClickListener
            }

            var category = "public"

            if (privateButton.isChecked) {
                category = "private"
            }

            val toilet = Toilet(
                uid = null,
                name = name,
                description = description,
                category = category,
                latitude = tempLocation.latitude,
                longitude = tempLocation.longitude
            )

            doAsync {
                val db =
                    Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                        .build()

                db.toiletDao().insert(toilet)
                db.close()
            }
            finish()


        }

        var toiletLatitude = intent.getDoubleExtra("latitude", 0.toDouble())
        var toiletLongitude = intent.getDoubleExtra("longitude", 0.toDouble())
        toiletLocation = LatLng(toiletLatitude, toiletLongitude)
    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return
        gMap.apply { uiSettings.isMapToolbarEnabled = false } // Remove navigation option

        if (toiletLocation != null) {
            val tempLocation: LatLng = toiletLocation!!
            with(gMap) {
                moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        tempLocation,
                        13f
                    )
                )
                addMarker(MarkerOptions()
                    .position(tempLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(getString(R.string.marker_color).toFloat()))
                )
            }
        }

        gMap.setOnMapLongClickListener { location: LatLng ->
            with(gMap) {
                clear()
                animateCamera(CameraUpdateFactory.newLatLng(location))
                addMarker(MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.defaultMarker(getString(R.string.marker_color).toFloat()))
                )

                toiletLocation = location
            }
        }
    }

    private fun confirmCancel() {
        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { _: DialogInterface, _: Int -> finish() }
        val negativeButtonClick = { _: DialogInterface, _: Int -> }

        with(builder)
        {
            setTitle("Confirmation")
            setMessage("Are you sure you want to exit without saving?")
            setPositiveButton("OK", positiveButtonClick)
            setNegativeButton("Cancel", negativeButtonClick)
            show()
        }
    }
}