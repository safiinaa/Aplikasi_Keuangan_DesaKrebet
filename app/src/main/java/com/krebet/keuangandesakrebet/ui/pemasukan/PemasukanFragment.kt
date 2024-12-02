package com.krebet.keuangandesakrebet.ui.pemasukan

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
import com.krebet.keuangandesakrebet.adapter.PemasukanAdapter
import com.krebet.keuangandesakrebet.adapter.PemasukanBulananAdapter
import com.krebet.keuangandesakrebet.adapter.SemuaPemasukanPengeluaranAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentPemasukanPengeluaranBinding
import com.krebet.keuangandesakrebet.model.SaldoBulanan
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection")
class PemasukanFragment : Fragment() {

    private var _binding: FragmentPemasukanPengeluaranBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private var namaInstansi: String? = ""
    private var isFilterOpen = false
    private var tglAwal: Date? = null
    private var tglAkhir: Date? = null

    private lateinit var totalPemasukan: List<Pair<Pengunjung, Double>>
    private var listPemasukan = mutableListOf<Transaksi>()
    private var listFilterPemasukan = mutableSetOf<Transaksi>()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPemasukanPengeluaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        getPemasukanPerPengunjung()

        binding.apply {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()
                    namaInstansi = query
                    filterNama()
                    btnSemua.isChecked = true
                    closeFilter()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) {
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
                getPemasukanPerPengunjung()
                closeFilter()
            }

            btnBulanan.setOnClickListener {
                getPemasukanBulanan()
                closeFilter()
            }

            btnFilterTanggal.setOnClickListener {
                searchView.clearFocus()
                searchView.setQuery("", false)

                listFilterPemasukan.clear()
                getSemuaPemasukan()

                val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTheme(R.style.ThemeMaterialCalendar)
                    .setTitleText("Pilih Tanggal Transaksi")
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

//                    if (namaInstansi.isNullOrEmpty()) {
                        if (isAdded) {
                            binding.recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(dataSorted, requireActivity().supportFragmentManager)
                        }
//                    } else {
//                        val filterNama = listFilterPemasukan.filter { transaksi ->
//                            transaksi.pengunjung?.namaInstansi?.contains(
//                                namaInstansi.toString() ,
//                                ignoreCase = true
//                            ) == true
//                        }
//
//                        if (filterNama.isEmpty()) {
//                            Toast.makeText(context, "Nama instansi tidak ditemukan", Toast.LENGTH_LONG).show()
//                        } else {
//                            if (isAdded) {
//                                binding.recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(filterNama)
//                            }
//                        }
//                    }
                }
                datePicker.addOnNegativeButtonClickListener {
                    datePicker.dismiss()
                    btnSemua.isChecked = true
                }
                closeFilter()
            }
        }
    }

    private fun getSemuaPemasukan() {
        db.collection("pemasukan")
//            .whereGreaterThanOrEqualTo("tglTransaksi", startDate)
//            .whereLessThanOrEqualTo("tglTransaksi", endDate)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pemasukan").also { it.idTransaksi = document.id }

                    db.collection("pengunjung")
                        .document(transaction.idPengunjung.toString())
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val visitor = snapshot.toObject(Pengunjung::class.java)
                            transaction.pengunjung = visitor

                            listPemasukan.add(transaction)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                        }
                }

//                if (isAdded) {
//                    binding.recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(listPemasukan)
//                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterNama() {
        val filterNama = totalPemasukan.filter {
            it.first.namaInstansi?.contains(
                namaInstansi.toString() ,
                ignoreCase = true
            ) == true
        }

        if (filterNama.isEmpty()) {
            Toast.makeText(context, "Nama instansi tidak ditemukan", Toast.LENGTH_SHORT).show()
        } else if (tglAwal == null) {
            if (isAdded) {
                binding.recyclerView.adapter = PemasukanAdapter(filterNama , requireActivity().supportFragmentManager)
            }
        }
//        } else {
//            listFilterPemasukan = listPemasukan.filter {
//                it.pengunjung?.namaInstansi?.contains(
//                    namaInstansi.toString() ,
//                    ignoreCase = true
//                ) == true
//            }.toMutableSet()
//
//            listPemasukan.forEach { transaksi ->
//                val tglTransaksi = transaksi.tglTransaksi!!.toDate()
//                if (tglTransaksi in tglAwal!!..tglAkhir!!) {
//                    listFilterPemasukan.add(transaksi)
//                }
//            }
//
//            if (isAdded) {
//                binding.recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(listFilterPemasukan.toMutableList())
//            }
//        }
    }

    private fun getPemasukanBulanan() {
        db.collection("pemasukan").get()
            .addOnSuccessListener { documents ->
                val transaksiBulanan = mutableMapOf<String, Double>()

                for(document in documents) {
                    val transaction = document.toObject(Transaksi::class.java)
                    transaction.let {
                        val calendar = Calendar.getInstance()
                        calendar.time = it.tglTransaksi!!.toDate()

                        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                        val monthYearKey = monthFormat.format(calendar.time)

                        transaksiBulanan[monthYearKey] = (transaksiBulanan[monthYearKey] ?: 0.0) + it.total!!
                    }

                    val saldoBulanan = transaksiBulanan.map { (month, balance) ->
                        SaldoBulanan(month, balance)
                    }.sortedBy { it.bulan }

                    if (isAdded) {
                        binding.recyclerView.adapter = PemasukanBulananAdapter(saldoBulanan, requireActivity().supportFragmentManager)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun getPemasukanPerPengunjung() {
        db.collection("pengunjung").get()
            .addOnSuccessListener { userSnapshot ->
                val visitors = userSnapshot.documents.map {
                    Pengunjung(
                        id = it.id,
                        namaInstansi = it.getString("namaInstansi") ?: "",
                        alamat = it.getString("alamat") ?: "",
                    )
                }

                db.collection("pemasukan").get()
                    .addOnSuccessListener { incomeSnapshot ->
                        val transactions = incomeSnapshot.documents.map {
                            Transaksi(
                                idPengunjung = it.getString("idPengunjung") ?: "",
                                total = it.getDouble("total")?.toFloat() ?: 0F
                            )
                        }

                        //Total transaksi pemasukan per pengunjung
                        totalPemasukan = visitors.map { visitor ->
                            val visitorTransactions = transactions.filter { it.idPengunjung == visitor.id }
                            val total = visitorTransactions.sumOf { it.total!!.toDouble() }
                            Pair(visitor, total)
                        }

                        if (isAdded) {
                            binding.recyclerView.adapter = PemasukanAdapter(totalPemasukan, requireActivity().supportFragmentManager)
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

    private fun openFilter() {
        binding.apply {
            layoutMenu.isVisible = true
            isFilterOpen = true
        }
    }

    private fun closeFilter() {
        binding.apply {
            layoutMenu.isVisible = false
            isFilterOpen = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}