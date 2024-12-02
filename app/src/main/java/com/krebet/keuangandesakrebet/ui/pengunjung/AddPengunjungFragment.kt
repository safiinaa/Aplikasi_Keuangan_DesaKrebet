package com.krebet.keuangandesakrebet.ui.pengunjung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.firestore
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentAddPengunjungBinding
import com.krebet.keuangandesakrebet.ui.home.HomeFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection")
class AddPengunjungFragment : Fragment() {

    private var _binding: FragmentAddPengunjungBinding? = null
    private val binding get() = _binding!!

    private var tglKunjungan: String? = null

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPengunjungBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {

            btnTanggalKunjungan.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Kunjungan")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy" , Locale("id" , "ID"))
                    tglKunjungan = dateFormat.format(Date(it)).toString()
                    btnTanggalKunjungan.text = SimpleDateFormat("dd MMMM yyyy" , Locale("id" , "ID")).format(it)
                }
                datePicker.addOnNegativeButtonClickListener {
                    datePicker.dismiss()
                }
            }

            btnSimpan.setOnClickListener {
                val namaCp = etNamaCp.text.toString()
                val namaInstansi = etNamaInstansi.text.toString()
                val noTelp = etNoTelp.text.toString()
                val alamat = etAlamat.text.toString()
                val dp = etDp.text.toString()

                if (namaCp.isEmpty()) {
                    Toast.makeText(context , "Nama CP tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (noTelp.isEmpty()) {
                    Toast.makeText(context , "Nomor telepon tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (namaInstansi.isEmpty()) {
                    Toast.makeText(context, "Nama instansi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (alamat.isEmpty()) {
                    Toast.makeText(context , "Alamat tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (tglKunjungan == null) {
                    Toast.makeText(context , "Tanggal kunjungan tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (dp.isEmpty()) {
                    Toast.makeText(context , "DP tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else {
                    loading.isVisible = true

                    val lastIdPengunjungRef = db.collection("lastId").document("lastIdPengunjung")
                    val lastIdPemasukanRef = db.collection("lastId").document("lastIdPemasukan")

                    db.runTransaction { transaction ->
                        val pengunjungSnapshot = transaction.get(lastIdPengunjungRef)
                        val pemasukanSnapshot = transaction.get(lastIdPemasukanRef)
                        val lastIdPengunjung = pengunjungSnapshot.getString("id") ?: "P01"
                        val lastIdPemasukan = pemasukanSnapshot.getString("id") ?: "M01"
                        val nextIdPengunjung = generateNextIdPengunjung(lastIdPengunjung)
                        val nextIdPemasukan = generateNextIdPemasukan(lastIdPemasukan)

                        val pengunjung = hashMapOf(
                            "namaCp" to namaCp,
                            "namaInstansi" to namaInstansi,
                            "noTelp" to noTelp,
                            "alamat" to alamat,
                            "tglTransaksi" to serverTimestamp() ,
                            "tglKunjungan" to SimpleDateFormat("dd-MM-yyyy hh:mm:ss a" , Locale("id" , "ID")).parse("$tglKunjungan 00:00:00 AM"),
                        )

                        val pengunjungRef = db.collection("pengunjung").document(nextIdPengunjung)
                        transaction.set(pengunjungRef, pengunjung)

                        val pemasukan = hashMapOf(
                            "idPengunjung" to nextIdPengunjung,
                            "tglTransaksi" to serverTimestamp(),
                            "total" to dp.toFloat(),
                            "catatan" to "DP",
                            "createdAt" to serverTimestamp()
                        )

                        val pemasukanRef = db.collection("pemasukan").document(nextIdPemasukan)
                        transaction.set(pemasukanRef, pemasukan)

                        transaction.update(lastIdPengunjungRef, "id", nextIdPengunjung)

                    }.addOnSuccessListener {
                        Toast.makeText(context , "Data berhasil disimpan" , Toast.LENGTH_LONG).show()
                        loading.isVisible = false
                        etNamaCp.text?.clear()
                        etNamaInstansi.text?.clear()
                        etNoTelp.text?.clear()
                        etAlamat.text?.clear()
                        etDp.text?.clear()
                        tglKunjungan = null
                        btnTanggalKunjungan.text = getString(R.string.tanggal_kunjungan)
                    }.addOnFailureListener {
                        Toast.makeText(context , "Terjadi kesalahan, silahkan ulangi kembali" , Toast.LENGTH_LONG).show()
                        loading.isVisible = false
                    }
                }
            }

            btnKembali.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.frameLayout , HomeFragment())
                transaction.commit()
            }
        }
    }

    private fun generateNextIdPengunjung(lastId: String): String {
        val number = lastId.substring(1).toInt() // Ambil angka setelah P
        val nextNumber = number + 1
        return "P" + nextNumber.toString().padStart(2, '0') // Format menjadi P01 dst
    }

    private fun generateNextIdPemasukan(lastId: String): String {
        val number = lastId.substring(1).toInt() // Ambil angka setelah M
        val nextNumber = number + 1
        return "M" + nextNumber.toString().padStart(2, '0') // Format menjadi M01 dst
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}