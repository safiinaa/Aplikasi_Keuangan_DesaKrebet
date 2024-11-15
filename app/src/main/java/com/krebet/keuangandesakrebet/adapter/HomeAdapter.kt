package com.krebet.keuangandesakrebet.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemHistoryBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import com.krebet.keuangandesakrebet.ui.home.DetailTransaksiFragment
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection")
class HomeAdapter(private val data: List<Transaksi> , private val fragmentManager: FragmentManager) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(tanggal!!.toDate())
                val formatRp = DecimalFormat("Rp ###,###,###").format(total)
                tvPengunjung.text = pengunjung?.nama
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

                holder.itemView.setOnClickListener {
                    val data = Transaksi(tanggal = tanggal, nominal = nominal, qty = qty, total = total, catatan = catatan, jenis = jenis, pengunjung = pengunjung)
                    val fragment = DetailTransaksiFragment()
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