package com.krebet.keuangandesakrebet.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ItemTransactionBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import com.krebet.keuangandesakrebet.ui.pemasukan.EditPemasukanFragment
import com.krebet.keuangandesakrebet.ui.pengeluaran.EditPengeluaranFragment
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection")
class SemuaPemasukanPengeluaranAdapter(private val data: List<Transaksi>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<SemuaPemasukanPengeluaranAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTransactionBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(tglTransaksi!!.toDate())
                val formatRp = DecimalFormat("Rp ###,###,###").format(total)

                tvId.text = idTransaksi
                tvNama.text = pengunjung?.namaInstansi
                tvTanggal.text = date
                tvCatatan.text = catatan
                tvJumlah.text = formatRp

                val context = holder.itemView.context
                val itemColor = if (jenis == "pemasukan") {
                    ContextCompat.getColor(context, R.color.green2)
                } else {
                    ContextCompat.getColor(context, R.color.blue2)
                }
                root.setCardBackgroundColor(itemColor)

                holder.itemView.setOnClickListener {
                    val data = Transaksi(
                        idTransaksi = idTransaksi ,
                        idPengunjung = idPengunjung ,
                        tglTransaksi = tglTransaksi ,
                        nominal = nominal ,
                        qty = qty ,
                        total = total ,
                        catatan = catatan ,
                        pengunjung = pengunjung
                    )
                    val fragmentPemasukan = EditPemasukanFragment()
                    val fragmentPengeluaran = EditPengeluaranFragment()
                    val mBundle = Bundle()
                    mBundle.putParcelable("data", data)

                    if (jenis == "pemasukan") {
                        fragmentPemasukan.arguments = mBundle
                        fragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, fragmentPemasukan)
                            .commit()
                    } else {
                        fragmentPengeluaran.arguments = mBundle
                        fragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, fragmentPengeluaran)
                            .commit()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}