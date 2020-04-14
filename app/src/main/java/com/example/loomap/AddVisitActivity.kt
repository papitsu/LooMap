package com.example.loomap

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_add_visit.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class AddVisitActivity : AppCompatActivity() {

    var rating: Float? = null
    var toiletId: Int = 0
    var toiletName: String = ""
    var selectedTime: Long = 0
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_visit)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { confirmCancel() }

        toiletId = intent.getIntExtra("toiletId", 0)
        toiletName = intent.getStringExtra("toiletName")!!
        selectedTime = System.currentTimeMillis()

        inputToilet.text = toiletName
        inputTime.text = format.format(selectedTime)

        save_button.setOnClickListener {

            val name = inputToilet.text.toString()
            val comment = inputComment.text.toString()

            if (name.isEmpty()) {
                toast("Please select toilet")
                return@setOnClickListener
            }

            if (rating == null) {
                toast("Please give a rating")
                return@setOnClickListener
            }

            val tempRating = rating!!

            val visit = Visit(
                uid = null,
                toiletId = toiletId,
                comment = comment,
                time = selectedTime,
                photoId = null,
                rating = tempRating
            )

            doAsync {
                val db =
                    Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .build()

                db.visitDao().insert(visit)
                db.close()
            }
            finish()
        }

        add_visit_rating.setOnRatingBarChangeListener { _, _, _ ->
            rating = add_visit_rating.rating
        }

        inputTime.setOnClickListener {
            toast("Clicked!")
            pickDateTime()
        }
    }

    private fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            currentDateTime.set(Calendar.YEAR, year)
            currentDateTime.set(Calendar.MONTH, month)
            currentDateTime.set(Calendar.DAY_OF_MONTH, day)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            currentDateTime.set(Calendar.HOUR_OF_DAY, hour)
            currentDateTime.set(Calendar.MINUTE, minute)
            selectedTime = currentDateTime.timeInMillis
            inputTime.text = format.format(selectedTime)
        }

        TimePickerDialog(this, timeSetListener, startHour, startMinute, true).show()
        DatePickerDialog(this, dateSetListener, startYear, startMonth, startDay).show()
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