package com.krebet.keuangandesakrebet.ui.pengeluaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.adapter.PemasukanPengeluaranBulananAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentSemuaPemasukanPengeluaranBulananBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection")
class SemuaPengeluaranBulananFragment : Fragment() {

    private var _binding: FragmentSemuaPemasukanPengeluaranBulananBinding? = null
    private val binding get() = _binding!!

    private lateinit var bulan: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemuaPemasukanPengeluaranBulananBinding.inflate(inflater, container, false)

        bulan = requireArguments().getString("bulan")!!

        binding.tvBulan.text = bulan

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        val data = mutableListOf<Transaksi>()

        db.collection("pengeluaran").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pengeluaran")
                    transaction.let {
                        val transactionDate = it.tglTransaksi!!.toDate()
                        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                        val transactionMonth = monthFormat.format(transactionDate)

                        if (transactionMonth == bulan) {
                            db.collection("pengunjung")
                                .document(transaction.idPengunjung.toString())
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    val visitor = snapshot.toObject(Pengunjung::class.java)
                                    transaction.pengunjung = visitor
                                    data.add(transaction)

                                    val dataSorted = data.sortedByDescending { it.tglTransaksi }

                                    if (isAdded) {
                                        binding.recyclerView.adapter = PemasukanPengeluaranBulananAdapter(dataSorted)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }

        binding.btnKembali.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout , PengeluaranFragment())
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}