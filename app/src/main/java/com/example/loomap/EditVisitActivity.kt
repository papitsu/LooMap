package com.example.loomap

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_edit_visit.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class EditVisitActivity : AppCompatActivity() {

    private var uid: Int? = null
    private var rating: Float? = null
    private var toiletId: Int = 0
    private var toiletName: String? = ""
    private var visitComment: String? = ""
    private var selectedTime: Long = 0
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_visit)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { confirmCancel() }

        save_button.setOnClickListener {

            val tempRating = rating!!

            val visit = Visit(
                uid = uid,
                toiletId = toiletId,
                comment = inputComment.text.toString(),
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
                db.visitDao().update(visit)
                db.close()
            }

            val returnIntent = Intent()
                .putExtra("uid", visit.uid)
                .putExtra("toiletId", visit.toiletId)
                .putExtra("comment", visit.comment)
                .putExtra("time", visit.time)
                .putExtra("rating", visit.rating)
                .putExtra("photoId", visit.photoId)

            setResult(1, returnIntent)
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

    override fun onResume() {
        super.onResume()

        // Handle intent
        uid = intent.getIntExtra("uid", 0)
        toiletId = intent.getIntExtra("toiletId", 0)
        toiletName = intent.getStringExtra("toiletName")
        visitComment = intent.getStringExtra("comment")
        selectedTime = intent.getLongExtra("time", 0)
        rating = intent.getFloatExtra("rating", 0.toFloat())

        val tempRating = rating
        inputToilet.text = toiletName
        inputComment.setText(visitComment)
        inputTime.text = format.format(selectedTime)
        add_visit_rating.rating = tempRating!!
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_edit_visit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        super.onOptionsItemSelected(item)
        val id = item.itemId

        if (id == R.id.delete_visit_from_edit_menu) {
            deleteVisitAlert()
        }
        return true
    }

    private fun deleteVisitAlert() {
        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { _: DialogInterface, _: Int -> deleteThisVisit(uid!!) }
        val negativeButtonClick = { _: DialogInterface, _: Int -> }

        with(builder)
        {
            setTitle("Confirmation")
            setMessage("Are you sure you want to delete this visit?")
            setPositiveButton("Delete", positiveButtonClick)
            setNegativeButton("Cancel", negativeButtonClick)
            show()
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

    private fun deleteThisVisit(visitId: Int) {
        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()
            db.visitDao().delete(visitId)
            db.close()
        }

        // Send result -1 to tell the Toilet View that toilet is being deleted
        setResult(-1)
        finish()
    }

    private fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        currentDateTime.timeInMillis = selectedTime
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



}