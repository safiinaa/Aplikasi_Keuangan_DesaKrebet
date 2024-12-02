package com.krebet.keuangandesakrebet.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemPengeluaranBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.ui.pengeluaran.SemuaPengeluaranFragment
import java.text.DecimalFormat

@Suppress("SpellCheckingInspection")
class PengeluaranAdapter(private val data: List<Pair<Pengunjung, Double>> , private val fragmentManager: FragmentManager) : RecyclerView.Adapter<PengeluaranAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPengeluaranBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemPengeluaranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val formatRp = DecimalFormat("Rp ###,###,###").format(second)

                tvId.text = first.id
                tvNama.text = first.namaInstansi
                tvJumlah.text = formatRp

                holder.itemView.setOnClickListener {
                    val data = Pengunjung(
                        id = first.id ,
                        namaInstansi = first.namaInstansi ,
                        alamat = first.alamat
                    )
                    val fragment = SemuaPengeluaranFragment()
                    val bundle = Bundle()
                    bundle.putParcelable("data", data)
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