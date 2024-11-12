package com.krebet.keuangandesakrebet.ui.pemasukan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.databinding.ItemTransactionBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection")
class DetailPemasukanAdapter(private val data: List<Transaksi>) : RecyclerView.Adapter<DetailPemasukanAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTransactionBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(tanggal!!.toDate())
                val formatRp = DecimalFormat("Rp ###,###,###").format(total)
                tvCatatan.text = catatan
                tvTanggal.text = date
                tvJumlah.text = formatRp
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}