package com.example.loomap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.lang.Float.parseFloat
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var gMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var selectedLocation: LatLng
    private lateinit var locationManager: LocationManager
    private var listOfToilets: MutableList<Toilet> = mutableListOf()
    private var listOfToiletNames: MutableList<String> = mutableListOf()
    private var listOfMarkers: MutableList<Marker> = mutableListOf()
    private var selectedToilet: Toilet? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var customMarker: Marker? = null

    private var filterMenuOpen: Boolean = false

    private var match: MutableList<String> = mutableListOf()

    var categoryFilter: String? = null // Can be "Private" or "Public"
    var ratingFilter: Float? = null // Can be between 0 and 5
    var distanceFilter: Float? = null // Can be between 0 km and Inf km

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (map_fragment_main as SupportMapFragment).getMapAsync(this)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        addBottomSheetCallbacks()

        add_toilet_button.setOnClickListener {
            intent = Intent(applicationContext, AddToiletActivity::class.java)
            if (customMarker != null) {
                val marker = customMarker!!
                intent
                    .putExtra("latitude", marker.position.latitude)
                    .putExtra("longitude", marker.position.longitude)
            } else {
                intent
                    .putExtra("latitude", currentLatitude)
                    .putExtra("longitude", currentLongitude)
            }
            startActivity(intent)
        }
    }

    private fun addBottomSheetCallbacks() {

        open_filter_menu.setOnClickListener {
            filterMenuOpen = if (filterMenuOpen) {
                bottom_sheet_filter.animate()
                    .translationY(-resources.getDimension(R.dimen.standard_42))
                false
            } else {
                bottom_sheet_filter.animate()
                    .translationY(-resources.getDimension(R.dimen.standard_230))
                true
            }
        }

        clear_category_filter.setOnClickListener {
            categoryFilter = null
            category_filter_info.text = "-"
            conditionalClearFilters()
        }

        clear_rating_filter.setOnClickListener {
            ratingFilter = null
            rating_filter_info.text = "-"
            conditionalClearFilters()
        }

        clear_distance_filter.setOnClickListener {
            distanceFilter = null
            distance_filter_info.text = "-"
            conditionalClearFilters()
        }

        bottom_sheet_filter.setOnClickListener {
        }

        category_filter_layout.setOnClickListener {
            categoryDialog()
        }
        rating_filter_layout.setOnClickListener {
            ratingDialog()
        }
        distance_filter_layout.setOnClickListener {
            checkLocationPermissions()
            if (checkLocation() && gMap.isMyLocationEnabled) {
                getLastLocation()
                distanceDialog()
            } else {
                val errorBuilder = AlertDialog.Builder(this)
                with(errorBuilder)
                {
                    setTitle("Error!")
                    setMessage("Location must be enabled to use distance filtering")
                    setPositiveButton("OK") { errorDialog, _ -> errorDialog.dismiss() }
                    show()
                }
            }
        }

        clear_filters_button.setOnClickListener {
            categoryFilter = null
            ratingFilter = null
            distanceFilter = null
            conditionalClearFilters()
        }
    }

    private fun conditionalClearFilters() {
        if (categoryFilter == null && ratingFilter == null && distanceFilter == null) {
            category_filter_info.text = "-"
            rating_filter_info.text = "-"
            distance_filter_info.text = "-"
            val params: ViewGroup.LayoutParams = clear_filters_button.layoutParams
            params.width = resources.getDimensionPixelSize(R.dimen.standard_0)
            clear_filters_button.layoutParams = params
        }
        refreshMap()
    }

    private fun maximizeClearFiltersButton() {
        val params: ViewGroup.LayoutParams = clear_filters_button.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        clear_filters_button.layoutParams = params
    }

    private fun categoryDialog() {
        val items = arrayOf("Public", "Private")
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Select categories")
        builder.setMultiChoiceItems(items, null) { dialog, which, isChecked ->
            if (isChecked) {
                selectedList.add(which)
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
            }
        }

        builder.setPositiveButton("Ok") { _, _ ->
            if (selectedList.count() == 2) {
                categoryFilter = null
            } else if (0 in selectedList) {
                categoryFilter = "Public"
            } else if (1 in selectedList) {
                categoryFilter = "Private"
            }


            if (categoryFilter == null) {
                category_filter_info.text = "-"
            } else {
                category_filter_info.text = categoryFilter
                maximizeClearFiltersButton()
                refreshMap()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun ratingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set minimum rating")
        val input = EditText(this)
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        input.hint = "Enter rating (0-5)"
        builder.setView(input)

        builder.setPositiveButton("Ok") { dialog, _ ->
            var isValid = true
            var num: Float? = null
            if (input.text.isBlank()) {
                isValid = false
            }

            try {
                num = parseFloat(input.text.toString().replace(",", "."))
                if (num !in 0.0..5.0) {
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                isValid = false
            }

            if (!isValid) {
                val errorBuilder = AlertDialog.Builder(this)

                with(errorBuilder)
                {
                    setTitle("Error!")
                    setMessage("Minimum rating must be between 0 and 5!")
                    setPositiveButton("OK") { errorDialog, _ -> errorDialog.dismiss() }
                    show()
                }
            } else {
                // If all validations went well, here we finally do the actual stuff!
                ratingFilter = num
                rating_filter_info.text = "Minimum rating: $ratingFilter stars"
                maximizeClearFiltersButton()
                refreshMap()
                dialog.dismiss()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun distanceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set maximum distance")
        val input = EditText(this)
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        input.hint = "Enter distance (km)"
        builder.setView(input)

        builder.setPositiveButton("Ok") { dialog, _ ->
            var isValid = true
            var num: Float? = null
            if (input.text.isBlank()) {
                isValid = false
            }

            try {
                num = parseFloat(input.text.toString().replace(",", "."))
                if (num < 0) {
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                isValid = false
            }

            if (!isValid) {
                val errorBuilder = AlertDialog.Builder(this)

                with(errorBuilder)
                {
                    setTitle("Error!")
                    setMessage("Maximum distance must be a positive number!")
                    setPositiveButton("OK") { errorDialog, _ -> errorDialog.dismiss() }
                    show()
                }
            } else {
                // If all validations went well, here we finally do the actual stuff!
                distanceFilter = num
                distance_filter_info.text = "Maximum distance: $distanceFilter km"
                maximizeClearFiltersButton()
                refreshMap()
                dialog.dismiss()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun getLastLocation() {
        checkLocationPermissions()
        if (checkLocation() && gMap.isMyLocationEnabled) {
            fusedLocationClient.lastLocation.addOnCompleteListener(this) {
                var location: Location? = it.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
            }
        }
    }

    private fun requestNewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()

        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            currentLatitude = lastLocation.latitude
            currentLongitude = lastLocation.longitude
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }

    override fun onResume() {
        super.onResume()
        customMarker = null
        refreshMap()
    }

    private fun checkLocationPermissions() {
        gMap.isMyLocationEnabled = false
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
        }
    }

    private fun checkLocation(): Boolean {
        return isLocationEnabled()
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_appbar_menu, menu)
        val searchItem = menu.findItem(R.id.app_bar_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard()

                if (query != null) {
                    match = listOfToiletNames.filter { it.contains(query, true) } as MutableList<String>
                    Log.d("TESTING", match.toString())

                    if (match.count() > 0) {
                        val toiletIdx = listOfToiletNames.indexOf(match[0])
                        val selectedToilet = listOfToilets[toiletIdx]
                        val intent = Intent(applicationContext, ToiletActivity::class.java)
                            .putExtra("uid", selectedToilet.uid)
                            .putExtra("name", selectedToilet.name)
                            .putExtra("category", selectedToilet.category)
                            .putExtra("description", selectedToilet.description)
                            .putExtra("latitude", selectedToilet.latitude)
                            .putExtra("longitude", selectedToilet.longitude)

                        startActivity(intent)
                    }
                }

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null && query != "") {
                    match = listOfToiletNames.filter { it.contains(query, true) } as MutableList<String>
                    val adapter = SearchSuggestionAdapter(applicationContext, match)
                    suggestions_list_view.adapter = adapter

                    suggestions_list_view.setOnItemClickListener { _, _, position, _ ->
                        val element = adapter.getItem(position) as String // The item that was clicked
                        val toiletIdx = listOfToiletNames.indexOf(element)
                        val selectedToilet = listOfToilets[toiletIdx]

                        val intent = Intent(applicationContext, ToiletActivity::class.java)
                            .putExtra("uid", selectedToilet.uid)
                            .putExtra("name", selectedToilet.name)
                            .putExtra("category", selectedToilet.category)
                            .putExtra("description", selectedToilet.description)
                            .putExtra("latitude", selectedToilet.latitude)
                            .putExtra("longitude", selectedToilet.longitude)
                        startActivity(intent)
                    }

                    Log.d("TESTING", match.toString())
                }
                return true
            }

        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                val adapter = SearchSuggestionAdapter(applicationContext, match)
                suggestions_list_view.adapter = adapter
                match.clear()
                adapter.notifyDataSetChanged()

                return true
            }
        })

        return true
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.main_menu_filter) {
            startActivity(Intent(applicationContext, FilterActivity::class.java))
            return true
        }
        if (id == R.id.main_menu_toilets) {
            startActivity(Intent(applicationContext, ToiletsActivity::class.java))
            return true
        }
        if (id == R.id.main_menu_visits) {
            startActivity(Intent(applicationContext, VisitsActivity::class.java))
            return true
        }
        if (id == R.id.main_menu_stats) {
            startActivity(Intent(applicationContext, StatsActivity::class.java))
            return true
        }
        if (id == R.id.main_menu_settings) {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun refreshMap() {
        doAsync {
            val db =
                Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                    .build()
            listOfToilets.clear()
            listOfToiletNames.clear()
            listOfMarkers.clear()

            for (toilet in db.toiletDao().getAllToilets()) {
                var ratingFilteringResult = true
                var distanceFilteringResult = true
                var categoryFilteringResult = true

                if (categoryFilter != null) {
                    val tempCategoryFilter = categoryFilter!!
                    categoryFilteringResult = false
                    if (toilet.category.toLowerCase() == tempCategoryFilter.toLowerCase()) {
                        categoryFilteringResult = true
                    }
                }

                if (ratingFilter != null) {
                    val tempRatingFilter = ratingFilter!!
                    ratingFilteringResult = false
                    val visits = db.visitDao().getByToilet(toilet.uid!!)
                    if (visits.isNotEmpty()) {
                        val rating = visits.map { it.rating }.average().toFloat()
                        if (rating >= tempRatingFilter) {
                            ratingFilteringResult = true
                        }
                    }
                }

                if (distanceFilter != null) {
                    val tempDistanceFilter = distanceFilter!! * 1000
                    distanceFilteringResult = false
                    val distance = calculateDistance(
                        currentLatitude,
                        currentLongitude,
                        toilet.latitude,
                        toilet.longitude
                    )
                    if (distance <= tempDistanceFilter) {
                        distanceFilteringResult = true
                    }
                }

                if (ratingFilteringResult && distanceFilteringResult && categoryFilteringResult) {
                    listOfToilets.add(toilet)
                    listOfToiletNames.add(toilet.name)
                }
            }

            if (db.toiletDao().getAllToilets().isEmpty()) {

                // Add sample words.
                var toilet = Toilet(
                    null,
                    category = "private",
                    latitude = 64.999,
                    longitude = 25.479,
                    description = "Tähän tulee sitten helvetin pitkä kuvaus. Ainakin monelle riville menee tämä kuvaus. Jospa ainakin kahdelle, ehkä jopa kolmelle.",
                    name = "Testi I"
                )
                db.toiletDao().insert(toilet)
                toilet = Toilet(
                    null,
                    category = "private",
                    latitude = 64.549,
                    longitude = 25.479,
                    description = "toka",
                    name = "Testi II"
                )
                db.toiletDao().insert(toilet)
                toilet = Toilet(
                    null,
                    category = "private",
                    latitude = 64.049,
                    longitude = 25.479,
                    description = "kolkki",
                    name = "Testi 3"
                )
                db.toiletDao().insert(toilet)
                toilet = Toilet(
                    null,
                    category = "private",
                    latitude = 63.849,
                    longitude = 25.479,
                    description = "nelkki",
                    name = "Testi 4"
                )
                var lastToiletId = db.toiletDao().insert(toilet).toInt()

                val format = SimpleDateFormat("yyyy-MM-dd HH:mm")

                var visit = Visit(
                    null,
                    time = format.parse("2016-04-20 04:20")!!.time,
                    toiletId = lastToiletId,
                    comment = "testikommentti 1",
                    rating = 2.5.toFloat(),
                    photoId = null
                )
                db.visitDao().insert(visit)

                visit = Visit(
                    null,
                    time = format.parse("2017-06-20 04:20")!!.time,
                    toiletId = lastToiletId,
                    comment = "testikommentti 2",
                    rating = 1.5.toFloat(),
                    photoId = null
                )
                db.visitDao().insert(visit)

                visit = Visit(
                    null,
                    time = format.parse("2020-01-01 00:05")!!.time,
                    toiletId = lastToiletId,
                    comment = "testikommentti 3",
                    rating = 3.5.toFloat(),
                    photoId = null
                )
                db.visitDao().insert(visit)

                visit = Visit(
                    null,
                    time = format.parse("2019-01-11 15:55")!!.time,
                    toiletId = lastToiletId,
                    comment = "testikommentti 3",
                    rating = 3.5.toFloat(),
                    photoId = null
                )
                db.visitDao().insert(visit)

                visit = Visit(
                    null,
                    time = format.parse("2010-01-11 15:55")!!.time,
                    toiletId = lastToiletId,
                    comment = "testikommentti 4",
                    rating = 0.5.toFloat(),
                    photoId = null
                )
                db.visitDao().insert(visit)

                visit = Visit(
                    null,
                    time = format.parse("2000-01-11 23:55")!!.time,
                    toiletId = lastToiletId,
                    comment = "testikommentti 5",
                    rating = 5.0.toFloat(),
                    photoId = null
                )
                db.visitDao().insert(visit)

            }
            db.close()

            uiThread {
                if (listOfToilets.isNotEmpty()) {
                    with(gMap) {
                        clear()
                        for (toilet in listOfToilets) {
                            val marker = addMarker(
                                MarkerOptions().position(
                                        LatLng(
                                            toilet.latitude,
                                            toilet.longitude
                                        )
                                    )
                                    .title(toilet.name)
                            )
                            marker.tag = toilet.uid!!.toInt()
                            listOfMarkers.add(marker)
                        }
                    }
                } else {
                    toast(getString(R.string.no_toilets))
                    gMap.clear()
                }
            }
        }
    }

    private fun removeOldMarker() {
        if (customMarker != null) {
            val tempMarker = customMarker
            tempMarker!!.remove()
            customMarker = null
        }
    }

    private fun closeFilterMenu() {
        if (filterMenuOpen) {
            bottom_sheet_filter.animate()
                .translationY(-resources.getDimension((R.dimen.standard_42)))
            filterMenuOpen = false
        }
    }

    private fun animateToLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latLong = LatLng(location.latitude, location.longitude)
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                with(gMap) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkLocationPermissions()
                    animateToLocation()
                }
                return
            }
        }
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        // distance in meter
        return results[0]

    }


    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return
        gMap.apply { uiSettings.isMapToolbarEnabled = false } // Remove navigation option
        checkLocationPermissions()

        if (gMap.isMyLocationEnabled) {
            animateToLocation()
        } else {
            val permission = mutableListOf<String>()
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
            closeFilterMenu()
            removeOldMarker()

            with(gMap) {
                animateCamera(CameraUpdateFactory.newLatLng(location))

                val tempMarker =
                    addMarker(
                        MarkerOptions().position(location)
                            .title(getString(R.string.new_toilet_snippet))
                    )
                tempMarker.showInfoWindow()

                customMarker = tempMarker
                selectedLocation = location
            }
        }

        gMap.setOnMarkerClickListener { marker ->
            closeFilterMenu()
            if (marker != customMarker) {
                removeOldMarker()
            }
            marker.showInfoWindow()
            gMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            true
        }

        gMap.setOnMapClickListener {
            closeFilterMenu()
            removeOldMarker()
        }

        gMap.setOnInfoWindowClickListener { marker ->
            if (marker.title == getString(R.string.new_toilet_snippet)) {
                val intent = Intent(applicationContext, AddToiletActivity::class.java)
                    .putExtra("latitude", marker.position.latitude)
                    .putExtra("longitude", marker.position.longitude)
                startActivity(intent)
            } else {
                val toilet = listOfToilets.find { it.uid == marker.tag }
                if (toilet != null) {
                    val intent = Intent(applicationContext, ToiletActivity::class.java)
                        .putExtra("uid", toilet.uid)
                        .putExtra("name", toilet.name)
                        .putExtra("category", toilet.category)
                        .putExtra("description", toilet.description)
                        .putExtra("latitude", toilet.latitude)
                        .putExtra("longitude", toilet.longitude)

                    startActivity(intent)
                }
            }
            customMarker = null
        }
    }
}
