package com.krebet.keuangandesakrebet.ui.pengeluaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.model.Transaksi
import com.krebet.keuangandesakrebet.adapter.SemuaPemasukanPengeluaranAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentSemuaPemasukanPengeluaranBinding

@Suppress("SpellCheckingInspection")
class SemuaPengeluaranFragment : Fragment() {

    private var _binding: FragmentSemuaPemasukanPengeluaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var id: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemuaPemasukanPengeluaranBinding.inflate(inflater, container, false)

        id = requireArguments().getString("id")!!
        val nama = requireArguments().getString("nama")!!

        binding.tvNama.text = nama

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        val data = mutableListOf<Transaksi>()

        db.collection("pengeluaran")
            .whereEqualTo("idPengunjung", id)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val transaction = document.toObject(Transaksi::class.java)
                    data.add(transaction)
                    data.sortBy { it.tanggal }
                }

                if (isAdded) {
                    binding.recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(data)
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