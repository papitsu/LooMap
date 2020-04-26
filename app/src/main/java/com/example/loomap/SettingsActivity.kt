package com.example.loomap

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }


        settings_delete.setOnClickListener {
            deleteAllAlert()
        }

        settings_init.setOnClickListener {
            initAlert()
        }
    }

    private fun deleteAll() {
        doAsync {
            val db =
                Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .build()

            db.toiletDao().deleteAll()
            db.visitDao().deleteAll()
        }
    }

    private fun initDatabase() {
        doAsync {
            val db =
                Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .build()

            db.toiletDao().deleteAll()
            db.visitDao().deleteAll()

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm")

            // Add sample toilets and visits

            var toilet = Toilet(
                null,
                category = "public",
                latitude = 65.057837,
                longitude = 25.467264,
                description = "Hidden behind the coat racks, on the way from Aula to Kastari, lies the cleanest, least used, most private public toilet on the ground level of the University of Oulu. A true hidden gem.",
                name = "Oulu Uni best toilet"
            )
            var lastToiletId = db.toiletDao().insert(toilet).toInt()

            var visit = Visit(
                null,
                time = format.parse("2019-10-11 13:40")!!.time,
                toiletId = lastToiletId,
                comment = "My first visit ever to this toilet. Love at first sight. A real beauty. Can't wait for the next time.",
                rating = 5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2019-12-09 11:20")!!.time,
                toiletId = lastToiletId,
                comment = "On the way to become my favourite toilet. Did not disappoint this time, either. Private, clean and always available.",
                rating = 5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-03-12 08:20")!!.time,
                toiletId = lastToiletId,
                comment = "Still good. Perfect start to a morning, just in time for the first lecture.",
                rating = 5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-03-12 15:50")!!.time,
                toiletId = lastToiletId,
                comment = "Second visit of the day. Still very clean.",
                rating = 5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-03-16 11:30")!!.time,
                toiletId = lastToiletId,
                comment = "One last visit before the campus closes. On the way to work.",
                rating = 5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)





            toilet = Toilet(
                null,
                category = "public",
                latitude = 60.171428,
                longitude = 24.948735,
                description = "Must visit when in Helsinki.",
                name = "Kaisa-talo 3rd floor"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2019-11-12 14:42")!!.time,
                toiletId = lastToiletId,
                comment = "Very busy building, so the toilets are not always as clean as one would wish.",
                rating = 3.8.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2019-11-16 12:02")!!.time,
                toiletId = lastToiletId,
                comment = "Once more before back to Oulu. Even dirtier on a Saturday.",
                rating = 3.0.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)





            toilet = Toilet(
                null,
                category = "public",
                latitude = 61.498105,
                longitude = 23.762127,
                description = "Good theatre, shitty toilets.",
                name = "Tampereen teatteri"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2019-11-15 20:25")!!.time,
                toiletId = lastToiletId,
                comment = "Friday night show, lot of people, old and not very clean toilets. Great show though.",
                rating = 1.8.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)




            toilet = Toilet(
                null,
                category = "public",
                latitude = 63.096031,
                longitude = 29.805657,
                description = "Love this place.",
                name = "Koli Nature Centre"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2019-10-10 14:08")!!.time,
                toiletId = lastToiletId,
                comment = "Lovely scenery, but the toilet's disappoint a little.",
                rating = 3.8.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)




            toilet = Toilet(
                null,
                category = "public",
                latitude = 65.011374,
                longitude = 25.472696,
                description = "Right in the center.",
                name = "Valkea Top Floor"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2019-11-25 17:45")!!.time,
                toiletId = lastToiletId,
                comment = "Convenient location, alright condition.",
                rating = 3.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-02-25 15:11")!!.time,
                toiletId = lastToiletId,
                comment = "The same as usual.",
                rating = 3.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)


            toilet = Toilet(
                null,
                category = "public",
                latitude = 65.059564,
                longitude = 25.478446,
                description = "Always empty.",
                name = "Caio"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2020-10-03 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-10-10 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-10-17 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-10-24 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "The got soap!",
                rating = 3.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-10-31 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap, again.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-11-07 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
            photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-11-14 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-11-21 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-11-28 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-11-28 20:22")!!.time,
                toiletId = lastToiletId,
                comment = "Out of soap.",
                rating = 2.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)



            toilet = Toilet(
                null,
                category = "private",
                latitude = 65.008850,
                longitude = 25.489064,
                description = "",
                name = "Kirsi's place"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2020-02-02 22:22")!!.time,
                toiletId = lastToiletId,
                comment = "Great place.",
                rating = 4.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-02-03 08:14")!!.time,
                toiletId = lastToiletId,
                comment = "Not as nice this time round, needs some cleaning.",
                rating = 3.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)



            toilet = Toilet(
                null,
                category = "public",
                latitude = 65.054270,
                longitude = 25.456271,
                description = "The necessary evil.",
                name = "Prisma Linnanmaa"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2020-01-25 19:33")!!.time,
                toiletId = lastToiletId,
                comment = "Hate to use this toilet, but sometimes it's a must. Very dirty.",
                rating = 1.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-02-04 08:33")!!.time,
                toiletId = lastToiletId,
                comment = "A bit nicer in the mornings. Still not very nice.",
                rating = 2.4.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-04-04 09:10")!!.time,
                toiletId = lastToiletId,
                comment = "During quarantine one of the few public toilets around here that's open.",
                rating = 1.8.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            visit = Visit(
                null,
                time = format.parse("2020-04-21 18:37")!!.time,
                toiletId = lastToiletId,
                comment = "Why do I always need the toilet when shopping.",
                rating = 1.2.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            toilet = Toilet(
                null,
                category = "private",
                latitude = 65.013116,
                longitude = 25.468911,
                description = "",
                name = "Joni's place"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2019-10-22 20:37")!!.time,
                toiletId = lastToiletId,
                comment = "Well this is nice, must come here again!",
                rating = 4.5.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)


            toilet = Toilet(
                null,
                category = "private",
                latitude = 60.187531,
                longitude = 24.966341,
                description = "",
                name = "Samuli's place"
            )
            lastToiletId = db.toiletDao().insert(toilet).toInt()

            visit = Visit(
                null,
                time = format.parse("2020-02-13 21:53")!!.time,
                toiletId = lastToiletId,
                comment = "Damn, Samuli should really invest in some better TP!",
                rating = 3.2.toFloat(),
                photoId = null
            )
            db.visitDao().insert(visit)

            db.close()
        }
    }

    private fun deleteAllAlert() {
        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { _: DialogInterface, _: Int -> deleteAll() }
        val negativeButtonClick = { _: DialogInterface, _: Int -> }

        with(builder)
        {
            setTitle("Confirmation")
            setMessage("Are you sure you want to delete all stored information?")
            setPositiveButton("Delete", positiveButtonClick)
            setNegativeButton("Cancel", negativeButtonClick)
            show()
        }
    }

    private fun initAlert() {
        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { _: DialogInterface, _: Int -> initDatabase() }
        val negativeButtonClick = { _: DialogInterface, _: Int -> }

        with(builder)
        {
            setTitle("Confirmation")
            setMessage("Are you sure you want to initialize the database with example data? \n\n" +
                    "This will delete all existing data from the storage.")
            setPositiveButton("Initialize", positiveButtonClick)
            setNegativeButton("Cancel", negativeButtonClick)
            show()
        }
    }
}