package com.krebet.keuangandesakrebet.ui.pengeluaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.adapter.PengeluaranAdapter
import com.krebet.keuangandesakrebet.adapter.PengeluaranBulananAdapter
import com.krebet.keuangandesakrebet.adapter.SemuaPemasukanPengeluaranAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentPemasukanPengeluaranBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.SaldoBulanan
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection")
class PengeluaranFragment : Fragment() {

    private var _binding: FragmentPemasukanPengeluaranBinding? = null
    private val binding get() = _binding!!

    private var namaInstansi: String? = ""
    private var isFilterOpen: Boolean = false
    private var tglAwal: Date? = null
    private var tglAkhir: Date? = null

    private lateinit var totalPengeluaran: List<Pair<Pengunjung, Double>>
    private var listPemasukan = mutableListOf<Transaksi>()
    private var listFilterPemasukan = mutableSetOf<Transaksi>()


    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPemasukanPengeluaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        getPengeluaranPerPengunjung()

        binding.apply {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()
                    namaInstansi = query
                    filterNama()
                    btnSemua.isChecked = true
                    closeFilter()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        searchView.clearFocus()
                        namaInstansi = ""
                        filterNama()
                        closeFilter()
                    } else {
//                        namaInstansi = newText
//                        filterNama()
//                        btnSemua.isChecked = true
                        closeFilter()
                    }
                    return false
                }
            })

            btnFilter.setOnClickListener {
                if (isFilterOpen) {
                    closeFilter()
                } else {
                    openFilter()
                }
            }

            btnSemua.setOnClickListener {
                getPengeluaranPerPengunjung()
                closeFilter()
            }

            btnBulanan.setOnClickListener {
                getPengeluaranBulanan()
                closeFilter()
            }

            btnFilterTanggal.setOnClickListener {
                searchView.clearFocus()
                searchView.setQuery("", false)

                listFilterPemasukan.clear()
                getSemuaPengeluaran()

                val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTheme(R.style.ThemeMaterialCalendar)
                    .setTitleText("Pilih Tanggal")
                    .setSelection(androidx.core.util.Pair(null , null))
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.isCancelable = false
                datePicker.addOnPositiveButtonClickListener {
                    tglAwal = Date(it.first)
                    tglAkhir = Date(it.second)

                    listPemasukan.forEach { transaksi ->
                        val tglTransaksi = transaksi.tglTransaksi!!.toDate()
                        if (tglTransaksi in tglAwal!!..tglAkhir!!) {
                            listFilterPemasukan.add(transaksi)
                        }
                    }

                    val dataSorted = listFilterPemasukan.sortedByDescending { it.tglTransaksi }.toMutableList()

                    if (isAdded) {
                        binding.recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(dataSorted, requireActivity().supportFragmentManager)
                    }
                datePicker.addOnNegativeButtonClickListener {
                    datePicker.dismiss()
                    btnSemua.isChecked = true
                }
                    closeFilter()
                }
            }
        }
    }

    private fun getSemuaPengeluaran() {
        db.collection("pengeluaran").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = document.toObject(Transaksi::class.java).also { it.idTransaksi = document.id }

                    db.collection("pengunjung")
                        .document(transaction.idPengunjung.toString())
                        .get()
                        .addOnSuccessListener {snapshot ->
                            val visitor = snapshot.toObject(Pengunjung::class.java)
                            transaction.pengunjung = visitor

                            listPemasukan.add(transaction)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterNama() {
        val filterNama = totalPengeluaran.filter {
            it.first.namaInstansi?.contains(
                namaInstansi.toString(),
                ignoreCase = true
            ) == true
        }

        if (filterNama.isEmpty()) {
            Toast.makeText(context, "Nama instansi tidak ditemukan", Toast.LENGTH_SHORT).show()
        } else {
            if (isAdded) {
                binding.recyclerView.adapter = PengeluaranAdapter(filterNama, requireActivity().supportFragmentManager)
            }
        }
    }

    private fun openFilter() {
        binding.layoutMenu.isVisible = true
        isFilterOpen = true
    }

    private fun closeFilter() {
        binding.layoutMenu.isVisible = false
        isFilterOpen = false
    }

    private fun getPengeluaranBulanan() {
        db.collection("pengeluaran").get()
            .addOnSuccessListener { documents ->
                val transaksiBulanan = mutableMapOf<String, Double>()

                for (document in documents) {
                    val transaction = document.toObject(Transaksi::class.java)
                    transaction.let {
                        val calendar = Calendar.getInstance()
                        calendar.time = it.tglTransaksi!!.toDate()

                        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                        val monthYearKey = monthFormat.format(calendar.time)

                        transaksiBulanan[monthYearKey] = (transaksiBulanan[monthYearKey] ?: 0.0) + it.total!!
                    }
                }

                val saldoBulanan = transaksiBulanan.map { (month, balance) ->
                    SaldoBulanan(month, balance)
                }.sortedBy { it.bulan }

                if (isAdded) {
                    binding.recyclerView.adapter = PengeluaranBulananAdapter(saldoBulanan, requireActivity().supportFragmentManager)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun getPengeluaranPerPengunjung() {
        db.collection("pengunjung").get()
            .addOnSuccessListener { userSnapshot ->
                val visitors = userSnapshot.documents.map {
                    Pengunjung(
                        id = it.id,
                        namaInstansi = it.getString("namaInstansi") ?: "",
                        alamat = it.getString("alamat") ?: "",
                    )
                }

                db.collection("pengeluaran").get()
                    .addOnSuccessListener { expenseSnapshot ->
                        val transactions = expenseSnapshot.documents.map {
                            Transaksi(
                                idPengunjung = it.getString("idPengunjung") ?: "",
                                total = it.getDouble("total")?.toFloat() ?: 0F
                            )
                        }

                        //Total transaksi pengeluaran per pengunjung
                        totalPengeluaran = visitors.map { visitor ->
                            val visitorTransactions = transactions.filter { it.idPengunjung == visitor.id }
                            val total = visitorTransactions.sumOf { it.total!!.toDouble() }
                            Pair(visitor, total)
                        }

                        if (isAdded) {
                            binding.recyclerView.adapter = PengeluaranAdapter(totalPengeluaran, requireActivity().supportFragmentManager)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}