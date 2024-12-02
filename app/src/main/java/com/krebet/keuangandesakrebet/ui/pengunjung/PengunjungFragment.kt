package com.krebet.keuangandesakrebet.ui.pengunjung

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
import com.krebet.keuangandesakrebet.adapter.PengunjungAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentPengunjungBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.ui.home.HomeFragment
import java.util.Date

@Suppress("SpellCheckingInspection")
class PengunjungFragment : Fragment() {

    private var _binding: FragmentPengunjungBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private var isFilterOpen: Boolean = false

    private val listPengunjung = mutableListOf<Pengunjung>()
    private val listFilterPengunjung = mutableSetOf<Pengunjung>()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengunjungBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            db.collection("pengunjung").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.toObject(Pengunjung::class.java).also { it.id = document.id }
                        listPengunjung.add(data)
                    }
                    setRecyclerView()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()

                    val filterList = listPengunjung.filter {
                        it.namaInstansi?.contains(
                            query.toString() ,
                            ignoreCase = true
                        ) == true
                    }

                    if (filterList.isEmpty()) {
                        Toast.makeText(context, "Nama instansi tidak ditemukan", Toast.LENGTH_LONG).show()
                    } else {
                        if (isAdded) {
                            binding.recyclerView.adapter = PengunjungAdapter(filterList, requireActivity().supportFragmentManager)
                        }
                        binding.layoutMenu.isVisible = true
                        closeFilter()
                    }

                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        searchView.clearFocus()
                        setRecyclerView()
                        binding.layoutMenu.isVisible = true
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
                setRecyclerView()
            }

            btnFilterTanggal.setOnClickListener {
                searchView.clearFocus()
                searchView.setQuery("", false)
                listFilterPengunjung.clear()

                val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTheme(R.style.ThemeMaterialCalendar)
                    .setTitleText("Pilih Tanggal Kunjungan")
                    .setSelection(androidx.core.util.Pair(null , null))
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.isCancelable = false
                datePicker.addOnPositiveButtonClickListener {
                    val tglAwal = Date(it.first)
                    val tglAkhir = Date(it.second)

                    listPengunjung.forEach { pengunjung ->
                        val tglKunjungan = pengunjung.tglKunjungan!!.toDate()
                        if (tglKunjungan in tglAwal..tglAkhir) {
                            listFilterPengunjung.add(pengunjung)
                        }
                    }

                    if (listFilterPengunjung.isEmpty()) {
                        Toast.makeText(context, "Pengunjung pada tanggal tersebut tidak ditemukan", Toast.LENGTH_LONG).show()
                    } else {
                        if (isAdded) {
                            binding.recyclerView.adapter = PengunjungAdapter(listFilterPengunjung.toMutableList(), requireActivity().supportFragmentManager)
                        }
                    }
                }
                datePicker.addOnNegativeButtonClickListener {
                    datePicker.dismiss()
                    btnSemua.isChecked = true
                }
            }

            btnKembali.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.frameLayout , HomeFragment())
                transaction.commit()
            }
        }
    }

    private fun setRecyclerView() {
        val sortedList = listPengunjung.sortedByDescending { it.tglTransaksi }
        if (isAdded) {
            binding.recyclerView.adapter = PengunjungAdapter(sortedList, requireActivity().supportFragmentManager)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}