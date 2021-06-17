package com.wency.petmanager.create.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.wency.petmanager.databinding.ItemSpinnerRegularityBinding

class RegularitySpinnerAdapter(private val strings: Array<String>) : BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding =
            ItemSpinnerRegularityBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        binding.title = strings[position]
        return binding.root
    }

    override fun getItem(position: Int): Any {
        return strings[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return strings.size
    }
}