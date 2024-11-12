package com.krebet.keuangandesakrebet.ui.pengunjung

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.databinding.ItemPengunjungBinding
import com.krebet.keuangandesakrebet.model.Pengunjung

@Suppress("SpellCheckingInspection")
class PengunjungAdapter(private val data: List<Pengunjung>) : RecyclerView.Adapter<PengunjungAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPengunjungBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemPengunjungBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvNama.text = nama
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}