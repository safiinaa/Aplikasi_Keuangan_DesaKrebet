package com.krebet.keuangandesakrebet.ui.pemasukan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.model.Transaksi
import com.krebet.keuangandesakrebet.adapter.SemuaPemasukanPengeluaranAdapter
import com.krebet.keuangandesakrebet.databinding.FragmentSemuaPemasukanPengeluaranBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.ui.home.HomeFragment

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class SemuaPemasukanFragment : Fragment() {

    private var _binding: FragmentSemuaPemasukanPengeluaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var pengunjung: Pengunjung
    private lateinit var backTo: String

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemuaPemasukanPengeluaranBinding.inflate(inflater, container, false)

        pengunjung = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BundleCompat.getParcelable(requireArguments(), "data", Pengunjung::class.java)!!
        } else {
            arguments?.getParcelable("data")!!
        }

        backTo = arguments?.getString("backTo") ?: ""

        binding.tvNama.text = pengunjung.namaInstansi

        onBackPressed()

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        val id = pengunjung.id
        val data = mutableListOf<Transaksi>()

        binding.apply {
            db.collection("pemasukan")
                .whereEqualTo("idPengunjung", id)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val transaction = document.toObject(Transaksi::class.java).copy(jenis = "pemasukan", pengunjung = pengunjung).also { it.idTransaksi = document.id }
                        data.add(transaction)
                    }

                    val dataSorted = data.sortedByDescending { it.idTransaksi }

                    if (isAdded) {
                        recyclerView.adapter = SemuaPemasukanPengeluaranAdapter(dataSorted, requireActivity().supportFragmentManager)
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                    }
                }

            val color = ContextCompat.getColor(requireContext(), R.color.green3)
            btnTambahTransaksi.backgroundTintList = ColorStateList.valueOf(color)
            btnTambahTransaksi.setColorFilter(Color.WHITE)

            btnTambahTransaksi.setOnClickListener {
                val fragment = AddPemasukanFragment()
                val mBundle = Bundle()
                mBundle.putParcelable("data", pengunjung)
                fragment.arguments = mBundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit()
            }

            btnKembali.setOnClickListener {
                setBackState()
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setBackState()
            }
        })
    }

    private fun setBackState() {
        if (backTo == "home") {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout , HomeFragment())
            transaction.commit()
        } else {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout , PemasukanFragment())
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}