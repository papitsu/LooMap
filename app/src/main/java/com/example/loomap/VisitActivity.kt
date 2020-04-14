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
import kotlinx.android.synthetic.main.activity_visit.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat

class VisitActivity : AppCompatActivity() {

    private var visitId: Int = 0
    private var toiletId: Int = 0
    private var toiletName: String = ""
    private var toiletCategory: String = ""
    private var photoId: Int = 0
    private var visitComment: String = ""
    private var visitTime: Long = 0
    private var visitRating: Float = 0.toFloat()
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()

        // Handle intent
        visitId = intent.getIntExtra("uid", 0)
        toiletId = intent.getIntExtra("toiletId", 0)
        photoId = intent.getIntExtra("photoId",0)
        visitComment = intent.getStringExtra("comment")!!
        visitTime = intent.getLongExtra("time", 0)
        visitRating = intent.getFloatExtra("rating",0.toFloat())

        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()

            val toilet = db.toiletDao().getById(toiletId)
            db.close()

            toiletName = toilet.name
            toiletCategory = toilet.category
            toilet_name.text = String.format(getString(R.string.toilet_name_string), toiletName, toiletCategory)
        }

        toilet_name.text = String.format(getString(R.string.toilet_name_string), toiletName, toiletCategory)
        visit_comment.text = visitComment
        visit_time.text = format.format(visitTime)
        star_visit_rating.rating = visitRating
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_visit_info, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.edit_visit_menu_action) {
            toast("Clicked edit visit!")

            intent = Intent(applicationContext, EditVisitActivity::class.java)
                .putExtra("uid", visitId)
                .putExtra("toiletId", toiletId)
                .putExtra("toiletName", toiletName)
                .putExtra("comment", visitComment)
                .putExtra("time", visitTime)
                .putExtra("rating", visitRating)
            startActivityForResult(intent, 1)

            return true
        }
        if (id == R.id.delete_visit_menu_action) {
            deleteVisitAlert()
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
            deleteThisVisit(visitId)
        }

        if (resultCode == 1) {
            intent = data
        }
    }

    private fun deleteVisitAlert() {
        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { _: DialogInterface, _: Int -> deleteThisVisit(visitId) }
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

    private fun deleteThisVisit(visitId: Int) {
        doAsync {
            val db =
                Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
                    .build()

            db.visitDao().delete(visitId)
            db.close()
        }
        finish()
    }
}


