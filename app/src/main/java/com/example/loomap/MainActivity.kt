package com.example.loomap

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var gMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var selectedLocation: LatLng
    lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (map_fragment_main as SupportMapFragment).getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        add_toilet_button.setOnClickListener {
            startActivity(Intent(applicationContext, AddToiletActivity::class.java))
        }

        stats_button.setOnClickListener {
            startActivity(Intent(applicationContext, StatsActivity::class.java))
        }

        settings_button.setOnClickListener {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
        }

        filter_button.setOnClickListener {
            startActivity(Intent(applicationContext, FilterActivity::class.java))
        }

        /*
        filter_button.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_filter_category -> {
                        toast("Clicked category")
                        true
                    }
                    R.id.menu_filter_rating -> {
                        toast("Clicked rating")
                        true
                    }
                    R.id.menu_filter_distance -> {
                        toast("Clicked distance")
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.menu_filter)
            popupMenu.show()
        }
        */


    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return
        gMap.apply {uiSettings.isMapToolbarEnabled = false} // Remove navigation option

        if ((ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
            || (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            gMap.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    var latLong = LatLng(location.latitude, location.longitude)
                    with(gMap) {
                        animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))
                    }
                }
            }
        } else {
            var permission = mutableListOf<String>()
            permission.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            permission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permission.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this,
                permission.toTypedArray(),
                123
            )
        }

        gMap.setOnMapLongClickListener { location: LatLng ->
            with(gMap) {
                clear()
                animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                var title = ""
                var city = ""
                try {
                    val addressList =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    city = addressList[0].locality
                    title = addressList[0].getAddressLine(0)
                } catch (e: Exception) {

                }

                val marker =
                    addMarker(MarkerOptions().position(location).snippet(title).title(city))
                marker.showInfoWindow()

                addCircle(
                    CircleOptions()
                        .center(location)
                        .strokeColor(Color.argb(50, 70, 70, 70))
                        .fillColor(Color.argb(100, 150, 150, 150))
                )

                selectedLocation = location
            }
        }


    }
}
