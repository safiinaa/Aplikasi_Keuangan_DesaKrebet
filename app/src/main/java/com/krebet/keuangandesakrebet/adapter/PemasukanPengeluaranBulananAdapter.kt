package com.krebet.keuangandesakrebet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemHistoryBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection")
class PemasukanPengeluaranBulananAdapter(private val data: List<Transaksi>) : RecyclerView.Adapter<PemasukanPengeluaranBulananAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(tglTransaksi!!.toDate())
                val formatRp = DecimalFormat("Rp ###,###,###").format(total)

                tvPengunjung.text = pengunjung?.namaInstansi
                tvCatatan.text = catatan
                tvTanggal.text = date
                tvJumlah.text = formatRp

                val context = holder.itemView.context
                val itemColor = if (jenis == "pemasukan") {
                    ContextCompat.getColor(context, R.color.green2)
                } else {
                    ContextCompat.getColor(context, R.color.blue2)
                }
                root.setCardBackgroundColor(itemColor)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}