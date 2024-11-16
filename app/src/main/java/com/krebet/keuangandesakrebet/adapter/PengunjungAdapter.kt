package com.krebet.keuangandesakrebet.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemPengunjungBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.ui.pengunjung.EditPengunjungFragment

@Suppress("SpellCheckingInspection")
class PengunjungAdapter(private val data: List<Pengunjung>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<PengunjungAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPengunjungBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemPengunjungBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvNama.text = nama

                holder.itemView.setOnClickListener {
                    val data = Pengunjung(id = id, nama = nama, noTelp = noTelp, alamat = alamat, tanggal = tanggal)
                    val fragment = EditPengunjungFragment()
                    val mBundle = Bundle()
                    mBundle.putParcelable("data", data)
                    fragment.arguments = mBundle
                    fragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .commit()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}