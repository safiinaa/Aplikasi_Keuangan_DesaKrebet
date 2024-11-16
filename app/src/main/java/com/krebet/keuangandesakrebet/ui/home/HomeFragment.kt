package com.krebet.keuangandesakrebet.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.adapter.HomeAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentHomeBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi
import com.krebet.keuangandesakrebet.ui.auth.ProfilFragment
import com.krebet.keuangandesakrebet.ui.pemasukan.AddPemasukanFragment
import com.krebet.keuangandesakrebet.ui.pengeluaran.AddPengeluaranFragment
import com.krebet.keuangandesakrebet.ui.pengunjung.AddPengunjungFragment
import com.krebet.keuangandesakrebet.ui.pengunjung.PengunjungFragment
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection")
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isMenuOpen = false
    private var isListOpen = false

    private val db = FirebaseFirestore.getInstance()
    private var transactions = mutableSetOf<Transaksi>()

    private var saldoPemasukan = 0F
    private var saldoPengeluaran = 0F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        setupSpinner()

        transactions.clear()
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
                    val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pengeluaran").also { it.idTransaksi = document.id }

                    db.collection("pengunjung")
                        .document(transaction.idPengunjung.toString())
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
                    val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pemasukan").also { it.idTransaksi = document.id }

                    db.collection("pengunjung")
                        .document(transaction.idPengunjung.toString())
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
        val transactionsSorted = transactions.sortedBy { it.tanggal }
        if (isAdded) {
            binding.recyclerView.adapter = HomeAdapter(transactionsSorted, requireActivity().supportFragmentManager)
        }
    }

    private fun getSaldoTahunan() {
        saldoPemasukan = 0F
        saldoPengeluaran = 0F

        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        val yearFormat = SimpleDateFormat("yyyy", Locale("id", "ID"))

        db.collection("pemasukan").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tanggal!!.toDate()
                    val transactionYear = yearFormat.format(transactionDate)

                    if (transactionYear == currentYear) {
                        saldoPemasukan += transaction.total ?: 0F
                    }
                }
                binding.tvSaldoMasuk.text = formatRupiah(saldoPemasukan)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }

        db.collection("pengeluaran").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tanggal!!.toDate()
                    val transactionYear = yearFormat.format(transactionDate)

                    if (transactionYear == currentYear) {
                        saldoPengeluaran += transaction.total ?: 0F
                    }
                }
                binding.tvSaldoKeluar.text = formatRupiah(saldoPengeluaran)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun getSaldoBulanan() {
        saldoPemasukan = 0F
        saldoPengeluaran = 0F

        val monthFormat = SimpleDateFormat("MM-yyyy", Locale("id", "ID"))
        val currentMonth = monthFormat.format(Date())

        db.collection("pemasukan").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tanggal!!.toDate()
                    val transactionMonth = monthFormat.format(transactionDate)

                    if (transactionMonth == currentMonth) {
                        saldoPemasukan += transaction.total ?: 0F
                    }
                }
                binding.tvSaldoMasuk.text = formatRupiah(saldoPemasukan)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }

        db.collection("pengeluaran").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tanggal!!.toDate()
                    val transactionMonth = monthFormat.format(transactionDate)

                    if (transactionMonth == currentMonth) {
                        saldoPengeluaran += transaction.total ?: 0F
                    }
                }
                binding.tvSaldoKeluar.text = formatRupiah(saldoPengeluaran)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun getSaldoHarian() {
        saldoPemasukan = 0F
        saldoPengeluaran = 0F

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())

        db.collection("pemasukan").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tanggal!!.toDate()
                    val transactionDateFormat = dateFormat.format(transactionDate)

                    if (transactionDateFormat == currentDate) {
                        saldoPemasukan += transaction.total ?: 0F
                    }
                }
                binding.tvSaldoMasuk.text = formatRupiah(saldoPemasukan)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }

        db.collection("pengeluaran").get()
            .addOnSuccessListener {
                for (document in it) {
                    val transaction = document.toObject(Transaksi::class.java)

                    val transactionDate = transaction.tanggal!!.toDate()
                    val transactionMonthFormat = dateFormat.format(transactionDate)

                    if (transactionMonthFormat == currentDate) {
                        saldoPengeluaran += transaction.total ?: 0F
                    }
                }
                binding.tvSaldoKeluar.text = formatRupiah(saldoPengeluaran)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
    }

    private fun formatRupiah(total: Float): String {
        val formatRp = DecimalFormat("Rp ###,###,###").format(total)
        return formatRp
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

    private fun setupSpinner() {
        val menuItems = arrayOf("Harian", "Bulanan", "Tahunan")
        val adapter: ArrayAdapter<*> = ArrayAdapter<Any?>(requireContext(), R.layout.item_spinner, menuItems)
        binding.spinnerMenu.adapter = adapter

        binding.spinnerMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = menuItems[position]
                when(selectedItem) {
                    "Harian" -> {
                        getSaldoHarian()
                    }
                    "Bulanan" -> {
                        getSaldoBulanan()
                    }
                    "Tahunan" -> {
                        getSaldoTahunan()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
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