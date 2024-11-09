package com.krebet.keuangandesakrebet.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.AddPengunjungFragment
import com.krebet.keuangandesakrebet.PengunjungFragment
import com.krebet.keuangandesakrebet.ProfilFragment
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentHomeBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi
import com.krebet.keuangandesakrebet.pemasukan.AddPemasukanFragment
import com.krebet.keuangandesakrebet.pengeluaran.AddPengeluaranFragment

@Suppress("SpellCheckingInspection")
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isMenuOpen = false
    private var isListOpen = false

    private var db = FirebaseFirestore.getInstance()
    private var transactions = mutableSetOf<Transaksi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        setupSpinner()
        getPemasukan()
        getPengeluaran()

        binding.apply {
            btnList.setOnClickListener {
                if (isListOpen) {
                    closeList()
                } else {
                    openList()
                }
            }

            btnLihatPengunjung.setOnClickListener {
                loadFragment(PengunjungFragment())
                closeList()
            }

            btnProfil.setOnClickListener {
                loadFragment(ProfilFragment())
                closeList()
            }

            btnSemua.setOnClickListener {
                transactions.clear()
                getPemasukan()
                getPengeluaran()
            }

            btnPemasukan.setOnClickListener {
                transactions.clear()
                getPemasukan()
            }

            btnPengeluaran.setOnClickListener {
                transactions.clear()
                getPengeluaran()
            }

            floatingActionButton.setColorFilter(Color.WHITE)
            floatingActionButton.setOnClickListener {
                if (isMenuOpen) {
                    closeMenu()
                } else {
                    openMenu()
                }
            }

            btnInputNama.setOnClickListener {
                loadFragment(AddPengunjungFragment())
                closeMenu()
            }

            btnTambahPemasukan.setOnClickListener {
                loadFragment(AddPemasukanFragment())
                closeMenu()
            }

            btnTambahPengeluaran.setOnClickListener {
                loadFragment(AddPengeluaranFragment())
                closeMenu()
            }
        }
    }

    private fun getPengeluaran() {
        db.collection("pengeluaran").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pengeluaran")

                    db.collection("pengunjung")
                        .document(transaction.idPengunjung!!)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val visitor = snapshot.toObject(Pengunjung::class.java)
                            transaction.pengunjung = visitor
                            transactions.add(transaction)

                            setRecyclerView(transactions.toList())
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                        }
                }
                setRecyclerView(transactions.toList())
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun getPemasukan() {
        db.collection("pemasukan").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pemasukan")

                    db.collection("pengunjung")
                        .document(transaction.idPengunjung!!)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val visitor = snapshot.toObject(Pengunjung::class.java)
                            transaction.pengunjung = visitor
                            transactions.add(transaction)

                            setRecyclerView(transactions.toList())
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

    private fun setRecyclerView(transactions: List<Transaksi>) {
        if (isAdded) {
            binding.recycleView.adapter = TransactionAdapter(transactions)
        }
    }

    private fun setupSpinner() {
        val menuItems = resources.getStringArray(R.array.menu1).toList();
        val adapter = SpinnerAdapter(requireContext(), menuItems)
        binding.spinnerMenu.adapter = adapter

        binding.spinnerMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position) as String
                Toast.makeText(requireContext(), "Dipilih: $selectedItem", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun openList() {
        binding.apply {
            btnLihatPengunjung.isVisible = true
            btnProfil.isVisible = true
            isListOpen = true
        }
    }

    private fun closeList() {
        binding.apply {
            btnLihatPengunjung.isVisible = false
            btnProfil.isVisible = false
            isListOpen = false
        }
    }

    private fun openMenu() {
        binding.apply {
            btnInputNama.isVisible = true
            btnTambahPemasukan.isVisible = true
            btnTambahPengeluaran.isVisible = true
            isMenuOpen = true
        }
    }

    private fun closeMenu() {
        binding.apply {
            btnInputNama.isVisible = false
            btnTambahPemasukan.isVisible = false
            btnTambahPengeluaran.isVisible = false
            isMenuOpen = false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout ,fragment)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}