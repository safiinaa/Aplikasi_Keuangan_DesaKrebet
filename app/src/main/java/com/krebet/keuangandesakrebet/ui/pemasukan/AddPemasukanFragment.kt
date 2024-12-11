package com.krebet.keuangandesakrebet.ui.pemasukan

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentAddPemasukanBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class AddPemasukanFragment : Fragment() {

    private var _binding: FragmentAddPemasukanBinding? = null
    private val binding get() = _binding!!

    private lateinit var pengunjung: Pengunjung
    private var tglTransaksi: Date? = null

    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPemasukanBinding.inflate(inflater , container, false)

        pengunjung = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BundleCompat.getParcelable(requireArguments(), "data", Pengunjung::class.java)!!
        } else {
            arguments?.getParcelable("data")!!
        }

        onBackPressed()

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            tvNamaInstansi.text = pengunjung.namaInstansi
            tvAlamat.text = pengunjung.alamat

            btnTglTransaksi.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Transaksi")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd MMMM yyyy" , Locale("id", "ID"))
                    val date = Date(it)
                    tglTransaksi = date
                    btnTglTransaksi.text = sdf.format(date).toString()
                }
                datePicker.addOnNegativeButtonClickListener {
                    datePicker.dismiss()
                }
            }

            btnSimpan.setOnClickListener {
                val nominal = etNominal.text.toString()
                val catatan = etCatatan.text.toString()

               if (tglTransaksi == null) {
                    showToast("Tanggal tidak boleh kosong")
                } else if (nominal.isEmpty()) {
                    showToast("Nominal tidak boleh kosong")
                } else if (catatan.isEmpty()) {
                    showToast("Catatan tidak boleh kosong")
                } else {
                    showLoading()

                   val lastIdDocRef = db.collection("lastId").document("lastIdPemasukan")

                   db.runTransaction {  transaction ->
                       val snapshot = transaction.get(lastIdDocRef)
                       val lastId = snapshot.getString("id") ?: "M01"
                       val nextId = generateNextId(lastId)

                       val pemasukan = hashMapOf(
                           "idPengunjung" to pengunjung.id,
                           "tglTransaksi" to tglTransaksi,
                           "total" to nominal.toFloat(),
                           "catatan" to catatan,
                           "createdAt" to serverTimestamp()
                       )

                       val pemasukanRef = db.collection("pemasukan").document(nextId)
                       transaction.set(pemasukanRef, pemasukan)

                       transaction.update(lastIdDocRef, "id", nextId)

                   }.addOnSuccessListener {
                       showToast("Data berhasil disimpan")
                       dismissLoading()
                       if (isAdded) {
                           tglTransaksi = null
                           btnTglTransaksi.text = getString(R.string.tgl)
                           etNominal.text?.clear()
                           etCatatan.text?.clear()
                       }
                   }.addOnFailureListener {
                       showToast("Terjadi kesalahan, silahkan ulangi kembali")
                       dismissLoading()
                   }
                }
            }

            btnKembali.setOnClickListener {
                setBackState()
            }
        }
    }

    private fun generateNextId(lastId: String): String {
        val number = lastId.substring(1).toInt() // Ambil angka setelah M
        val nextNumber = number + 1
        return "M" + nextNumber.toString().padStart(2, '0') // Format menjadi M01 dst
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setBackState()
            }
        })
    }

    private fun setBackState() {
        val data = Pengunjung(
            id = pengunjung.id ,
            namaInstansi = pengunjung.namaInstansi ,
            alamat = pengunjung.alamat
        )
        val fragment = SemuaPemasukanFragment()
        val mBundle = Bundle()
        mBundle.putParcelable("data", data)

        fragment.arguments = mBundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    private fun showLoading() {
        if (isAdded) {
            binding.loading.isVisible = true
        }
    }

    private fun dismissLoading() {
        if (isAdded) {
            binding.loading.isVisible = false
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}