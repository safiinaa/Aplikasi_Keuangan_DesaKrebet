package com.krebet.keuangandesakrebet.ui.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.R.string.arus_kas_bersih_dan_saldo_kas_bulan
import com.krebet.keuangandesakrebet.R.string.untuk_bulan_yang_berakhir_pada
import com.krebet.keuangandesakrebet.databinding.FragmentLaporanArusKasBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection", "SetTextI18n")
class LaporanArusKasFragment : Fragment() {

    private var _binding: FragmentLaporanArusKasBinding? = null
    private val binding get() = _binding!!

    private var saldoPemasukan = 0F
    private var saldoPengeluaran = 0F

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanArusKasBinding.inflate(inflater, container, false)
        onBackPressed()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSaldoBulanan()
    }

    private fun getSaldoBulanan() {
        binding.loading.isVisible = true

        saldoPemasukan = 0F
        saldoPengeluaran = 0F

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
                updateSaldoAkhir()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                binding.loading.isVisible = false
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
                updateSaldoAkhir()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                binding.loading.isVisible = false
            }

        binding.btnKembali.setOnClickListener {
            setBackState()
        }
    }

    private fun updateSaldoAkhir() {
        val kas = saldoPemasukan - saldoPengeluaran
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        val nextMonth = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(calendar.time)

        if(isAdded) {
            binding.apply {
                tvKet.text = getString(untuk_bulan_yang_berakhir_pada) + " " + lastDate
                tvBlnBerikutnya.text = getString(arus_kas_bersih_dan_saldo_kas_bulan) + " " + nextMonth
                tvTotalSaldoMasuk.text = formatRupiah(saldoPemasukan)
                tvTotalSaldoKeluar.text = formatRupiah(saldoPengeluaran)
                tvSaldoAkhir.text = formatRupiah(kas)
                loading.isVisible = false
            }
        }
    }

    private fun formatRupiah(total: Float): String {
        val formatRp = DecimalFormat("Rp ###,###,###").format(total)
        return formatRp
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setBackState()
            }
        })
    }

    private fun setBackState() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, LaporanFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}