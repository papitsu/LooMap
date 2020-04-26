package com.example.loomap

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_edit_toilet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class EditToiletActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private var toiletId: Int = 0
    private var toiletLocation: LatLng? = null
    private var toiletCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_toilet)
        (map_fragment_add_toilet as SupportMapFragment).getMapAsync(this)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { confirmCancel() }

        toiletId = intent.getIntExtra("uid", 0)
        inputName.setText(intent.getStringExtra("name"))
        inputDescription.setText(intent.getStringExtra("description"))
        toiletCategory = intent.getStringExtra("category")
        val toiletLatitude = intent.getDoubleExtra("latitude", 0.toDouble())
        val toiletLongitude = intent.getDoubleExtra("longitude", 0.toDouble())
        toiletLocation = LatLng(toiletLatitude, toiletLongitude)

        if (toiletCategory == "private") {
            privateButton.isChecked = true
        } else {
            publicButton.isChecked = true
        }

        save_button.setOnClickListener {

            var toiletCategory = "public"
            if (privateButton.isChecked) {
                toiletCategory = "private"
            }

            val tempToiletLocation = toiletLocation!!

            val toilet = Toilet(
                uid = toiletId,
                name = inputName.text.toString(),
                description = inputDescription.text.toString(),
                category = toiletCategory,
                latitude = tempToiletLocation.latitude,
                longitude = tempToiletLocation.longitude
            )

            doAsync {
                val db =
                    Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                        .build()
                db.toiletDao().update(toilet)
                db.close()
            }

            val returnIntent = Intent()
                .putExtra("uid", toiletId)
                .putExtra("name", toilet.name)
                .putExtra("category", toilet.category)
                .putExtra("description", toilet.description)
                .putExtra("latitude", toilet.latitude)
                .putExtra("longitude", toilet.longitude)

            setResult(1, returnIntent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_edit_toilet, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return
        gMap.apply { uiSettings.isMapToolbarEnabled = false } // Remove navigation option

        if (toiletLocation != null) {
            val tempLocation: LatLng = toiletLocation!!
            with(gMap) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(tempLocation, 13f))
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

    private fun deleteThisToilet(toiletId: Int) {
        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()

            db.toiletDao().delete(toiletId)
            db.close()
        }

        // Send result -1 to tell the Toilet View that toilet is being deleted
        setResult(-1)
        finish()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        super.onOptionsItemSelected(item)
        val id = item.itemId

        if (id == R.id.delete_toilet_from_edit_menu) {
            deleteToiletAlert()
        }
        return true
    }
}
