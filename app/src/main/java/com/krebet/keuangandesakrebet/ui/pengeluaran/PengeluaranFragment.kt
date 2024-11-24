package com.krebet.keuangandesakrebet.ui.pengeluaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.adapter.PengeluaranAdapter
import com.krebet.keuangandesakrebet.adapter.PengeluaranBulananAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentPemasukanPengeluaranBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.SaldoBulanan
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("SpellCheckingInspection")
class PengeluaranFragment : Fragment() {

    private var _binding: FragmentPemasukanPengeluaranBinding? = null
    private val binding get() = _binding!!

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

        getPengeluaran()

        binding.apply {
            btnSemua.setOnClickListener {
                getPengeluaran()
            }

            btnBulanan.setOnClickListener {
                getPengeluaranBulanan()
            }
        }
    }

    private fun getPengeluaranBulanan() {
        db.collection("pengeluaran").get()
            .addOnSuccessListener { documents ->
                val transaksiBulanan = mutableMapOf<String, Double>()

                for (document in documents) {
                    val transaction = document.toObject(Transaksi::class.java)
                    transaction.let {
                        val calendar = Calendar.getInstance()
                        calendar.time = it.tanggal!!.toDate()

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

    private fun getPengeluaran() {
        db.collection("pengunjung").get()
            .addOnSuccessListener { userSnapshot ->
                val visitors = userSnapshot.documents.map {
                    Pengunjung(
                        id = it.id,
                        namaInstansi = it.getString("namaInstansi") ?: ""
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
                        val totalPengeluaran = visitors.map { visitor ->
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