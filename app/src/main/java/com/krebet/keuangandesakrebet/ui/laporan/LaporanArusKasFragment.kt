package com.krebet.keuangandesakrebet.ui.laporan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentHomeBinding
import com.krebet.keuangandesakrebet.databinding.FragmentLaporanArusKasBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection")
class LaporanArusKasFragment : Fragment() {
    private var _binding: FragmentLaporanArusKasBinding? = null
    private val binding get() = _binding!!

    private var saldoPemasukan = 0F
    private var saldoPengeluaran = 0F

    private var pemasukanDone = false
    private var pengeluaranDone = false

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanArusKasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSaldoBulanan()
    }

    private fun getSaldoBulanan() {
        saldoPemasukan = 0F
        saldoPengeluaran = 0F

        pemasukanDone = false
        pengeluaranDone = false

        val monthFormat = SimpleDateFormat("MM-yyyy", Locale("id", "ID"))
        val currentMonth = monthFormat.format(Date())

        db.collection("pemasukan").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tglTransaksi!!.toDate()
                    val transactionMonth = monthFormat.format(transactionDate)

                    if (transactionMonth == currentMonth) {
                        saldoPemasukan += transaction.total ?: 0F
                    }
                }
                pemasukanDone = true
                updateSaldoAkhir(pemasukanDone, pengeluaranDone)
//                if (isAdded) {
//                    binding.tvTotalSaldoMasuk.text = formatRupiah(saldoPemasukan)
//                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                pemasukanDone = true
                updateSaldoAkhir(pemasukanDone, pengeluaranDone)
            }

        db.collection("pengeluaran").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tglTransaksi!!.toDate()
                    val transactionMonth = monthFormat.format(transactionDate)

                    if (transactionMonth == currentMonth) {
                        saldoPengeluaran += transaction.total ?: 0F
                    }
                }
                pengeluaranDone = true
                updateSaldoAkhir(pemasukanDone, pengeluaranDone)
//                if (isAdded) {
//                    binding.tvTotalSaldoKeluar.text = formatRupiah(saldoPengeluaran)
//                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                pengeluaranDone = true
                updateSaldoAkhir(pemasukanDone, pengeluaranDone)
            }
    }

    private fun updateSaldoAkhir(pemasukanDone: Boolean, pengeluaranDone: Boolean) {
        if(pemasukanDone && pengeluaranDone) {
            if(isAdded) {
                binding.tvTotalSaldoMasuk.text = formatRupiah(saldoPemasukan)
                binding.tvTotalSaldoKeluar.text = formatRupiah(saldoPengeluaran)
                val kas =  saldoPemasukan - saldoPengeluaran
                binding.tvSaldoAkhir.text = formatRupiah(kas)
            }
        }
    }

    private fun formatRupiah(total: Float): String {
        val formatRp = DecimalFormat("Rp ###,###,###").format(total)
        return formatRp
    }
}