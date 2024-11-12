package com.krebet.keuangandesakrebet.ui.pemasukan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.databinding.FragmentPemasukanBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi

@Suppress("SpellCheckingInspection")
class PemasukanFragment : Fragment() {

    private var _binding: FragmentPemasukanBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPemasukanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        getAllIncome()

        binding.apply {
            btnSemua.setOnClickListener {
                getAllIncome()
            }

            btnBulanan.setOnClickListener {

            }
        }
    }

    private fun getAllIncome() {
        db.collection("pengunjung").get()
            .addOnSuccessListener { userSnapshot ->
                val visitors = userSnapshot.documents.map {
                    Pengunjung(id = it.id, nama = it.getString("nama"))
                }

                db.collection("pemasukan").get()
                    .addOnSuccessListener { incomeSnapshot ->
                        val transactions = incomeSnapshot.documents.map {
                            Transaksi(
                                idPengunjung = it.getString("idPengunjung") ?: "",
                                total = it.getDouble("total")?.toFloat() ?: 0F
                            )
                        }

                        val incomesTotal = visitors.map { visitor ->
                            val visitorTransactions = transactions.filter { it.idPengunjung == visitor.id }
                            val total = visitorTransactions.sumOf { it.total!!.toDouble() }
                            Pair(visitor, total)
                        }.filter { it.second > 0 }

                        if (isAdded) {
                            binding.recycleView.adapter = PemasukanAdapter(incomesTotal, requireActivity().supportFragmentManager)
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