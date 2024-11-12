package com.krebet.keuangandesakrebet.ui.pemasukan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemPemasukanBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import java.text.DecimalFormat

@Suppress("SpellCheckingInspection")
class PemasukanAdapter(private val data: List<Pair<Pengunjung, Double>>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<PemasukanAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPemasukanBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemPemasukanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val formatRp = DecimalFormat("Rp ###,###,###").format(second)
                tvNama.text = first.nama
                tvJumlah.text = formatRp

                holder.itemView.setOnClickListener {
                    val id = first.id
                    val nama = first.nama
                    val fragment = PemasukanBulananFragment()
                    val bundle = Bundle()
                    bundle.putString("id", id)
                    bundle.putString("nama", nama)
                    fragment.arguments = bundle
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