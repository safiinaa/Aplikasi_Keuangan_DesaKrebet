package com.krebet.keuangandesakrebet.ui.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.R.string.arus_kas_dan_saldo_bulan
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

        setupSpinner()
    }

    private fun setupSpinner() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        val yearFormat = SimpleDateFormat("yyyy", Locale("id", "ID"))

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, - 1)
        val lastYear = yearFormat.format(calendar.time)

        calendar.add(Calendar.YEAR, 2)
        val nextYear = yearFormat.format(calendar.time)

        val currentMonth = monthFormat.format(Date())
        val menuItems = arrayOf(currentMonth, nextYear, lastYear)

        val adapter: ArrayAdapter<*> = ArrayAdapter<Any?>(requireContext(), R.layout.item_spinner, menuItems)
        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>? , view: View? , position: Int , id: Long) {
                val selectedItem = menuItems[position]
                when(selectedItem) {
                    currentMonth -> {
                        getKasBulanan(currentMonth)
                    }
                    nextYear -> {
                        getKasTahunan(nextYear)
                    }
                    lastYear -> {
                        getKasTahunan(lastYear)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getKasBulanan(currentMonth: String) {
        showLoading()

        saldoPemasukan = 0F
        saldoPengeluaran = 0F

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

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
                updateSaldoBulanan()
            }
            .addOnFailureListener {
                showToast()
                dismissLoading()
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
                updateSaldoBulanan()
            }
            .addOnFailureListener {
                showToast()
                dismissLoading()
            }

        binding.btnKembali.setOnClickListener {
            setBackState()
        }
    }

    private fun getKasTahunan(year: String) {
        showLoading()

        saldoPemasukan = 0F
        saldoPengeluaran = 0F

        val yearFormat = SimpleDateFormat("yyyy", Locale("id", "ID"))

        db.collection("pemasukan").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tglTransaksi!!.toDate()
                    val transactionYear = yearFormat.format(transactionDate)

                    if (transactionYear == year) {
                        saldoPemasukan += transaction.total ?: 0F
                    }
                }
                updateSaldoTahunan(year)
            }
            .addOnFailureListener {
                showToast()
                dismissLoading()
            }

        db.collection("pengeluaran").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tglTransaksi!!.toDate()
                    val transactionYear = yearFormat.format(transactionDate)

                    if (transactionYear == year) {
                        saldoPengeluaran += transaction.total ?: 0F
                    }
                }
                updateSaldoTahunan(year)
            }
            .addOnFailureListener {
                showToast()
                dismissLoading()
            }

        binding.btnKembali.setOnClickListener {
            setBackState()
        }
    }

    private fun updateSaldoBulanan() {
        val kas = saldoPemasukan - saldoPengeluaran
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        val nextMonth = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(calendar.time)

        dismissLoading()
        if(isAdded) {
            binding.apply {
                tvKet.text = getString(untuk_bulan_yang_berakhir_pada) + " " + lastDate
                tvBlnBerikutnya.text = getString(arus_kas_dan_saldo_bulan) + " " + nextMonth
                tvTotalSaldoMasuk.text = formatRupiah(saldoPemasukan)
                tvTotalSaldoKeluar.text = formatRupiah(saldoPengeluaran)
                tvSaldoAkhir.text = formatRupiah(kas)
            }
        }
    }

    private fun updateSaldoTahunan(year: String) {
        val kas = saldoPemasukan - saldoPengeluaran
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year.toInt())
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        val nextMonth = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(calendar.time)

        dismissLoading()
        if(isAdded) {
            binding.apply {
                tvKet.text = getString(untuk_bulan_yang_berakhir_pada) + " " + lastDate
                tvBlnBerikutnya.text = getString(arus_kas_dan_saldo_bulan) + " " + nextMonth
                tvTotalSaldoMasuk.text = formatRupiah(saldoPemasukan)
                tvTotalSaldoKeluar.text = formatRupiah(saldoPengeluaran)
                tvSaldoAkhir.text = formatRupiah(kas)
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

    private fun showLoading() {
        if (isAdded) {
            binding.loading.isVisible = true
        }
    }

    private fun dismissLoading() {
        if (isAdded) {
            binding.loading.isVisible = false
        }
    }

    private fun showToast() {
        if (isAdded) {
            Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}