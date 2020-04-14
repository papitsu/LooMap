package com.example.loomap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.row_toilets_list.view.*

class ToiletsAdapter(
    context: Context,
    private val list: List<Toilet>,
    private val ratings: MutableList<Float?>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = inflater.inflate(R.layout.row_toilets_list, parent, false)
        row.toiletName.text = "${list[position].uid} ${list[position].name}"
        row.toiletCategory.text = list[position].category
        row.toiletRating.rating = when (ratings[position] != null) {
            true -> ratings[position]!!
            false -> 0.0.toFloat()
        }
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