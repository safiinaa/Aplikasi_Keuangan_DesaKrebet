package com.krebet.keuangandesakrebet.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemPengeluaranBinding
import com.krebet.keuangandesakrebet.model.SaldoBulanan
import com.krebet.keuangandesakrebet.ui.pengeluaran.SemuaPengeluaranBulananFragment
import java.text.DecimalFormat

@Suppress("SpellCheckingInspection")
class PengeluaranBulananAdapter(private val data: List<SaldoBulanan> , private val fragmentManager: FragmentManager) : RecyclerView.Adapter<PengeluaranBulananAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPengeluaranBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemPengeluaranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val formatRp = DecimalFormat("Rp ###,###,###").format(saldo)
                tvJumlah.text = formatRp
                tvNama.text = bulan

                holder.itemView.setOnClickListener {
                    val fragment = SemuaPengeluaranBulananFragment()
                    val bundle = Bundle()
                    bundle.putString("bulan", bulan)
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