package com.krebet.keuangandesakrebet.ui.pengunjung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.adapter.PengunjungAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentPengunjungBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.ui.home.HomeFragment

@Suppress("SpellCheckingInspection")
class PengunjungFragment : Fragment() {

    private var _binding: FragmentPengunjungBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val listPengunjung = mutableListOf<Pengunjung>()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengunjungBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.btnKembali.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout , HomeFragment())
            transaction.commit()
        }

        db.collection("pengunjung").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val data = document.toObject(Pengunjung::class.java).also { it.id = document.id }
                    listPengunjung.add(data)
                }
                binding.recyclerView.adapter = PengunjungAdapter(listPengunjung, requireActivity().supportFragmentManager)
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