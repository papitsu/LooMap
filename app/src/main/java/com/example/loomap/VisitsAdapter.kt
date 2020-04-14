package com.example.loomap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.row_all_visits.view.*
import java.text.SimpleDateFormat

class VisitsAdapter(context: Context, private val list: List<Visit>, private val names: List<String>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = inflater.inflate(R.layout.row_all_visits, parent, false)

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")

        row.visitTime.text = format.format(list[position].time)
        row.visitToilet.text = names[position]
        row.visitRating.rating = list[position].rating
        return row
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return list[position].uid!!.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
}