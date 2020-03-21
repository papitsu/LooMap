package com.example.loomap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.row_toilet_visit.view.*

class VisitsAdapter(context: Context, private val list: Array<String>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = inflater.inflate(R.layout.row_toilet_visit, parent, false)
        row.visitTime.text = list[position]
        row.visitRating.rating = 3.7.toFloat()
        return row
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}