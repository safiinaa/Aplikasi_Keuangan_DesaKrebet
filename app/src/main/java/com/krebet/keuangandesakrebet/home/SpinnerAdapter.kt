package com.krebet.keuangandesakrebet.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.krebet.keuangandesakrebet.databinding.ItemSpinnerBinding

class SpinnerAdapter(context: Context , private val items: List<String>) :
    ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
        val binding = if (convertView == null) {
            ItemSpinnerBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemSpinnerBinding.bind(convertView)
        }
        binding.tvSpinner.text = items[position]
        return binding.root
    }


    override fun getDropDownView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
        val binding = if (convertView == null) {
            ItemSpinnerBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemSpinnerBinding.bind(convertView)
        }
        binding.tvSpinner.text = items[position]
        return binding.root
    }
}