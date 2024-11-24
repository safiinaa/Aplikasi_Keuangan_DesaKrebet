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
import com.google.firebase.firestore.FieldValue
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
            lateinit var tanggal: String
            lateinit var tanggalAcara: String

            btnTanggal.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Kunjungan")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy" , Locale("id" , "ID"))
                    tanggal = dateFormat.format(Date(it)).toString()
                    btnTanggal.text = SimpleDateFormat("dd MMMM yyyy" , Locale("id" , "ID")).format(it)
                }
                datePicker.addOnNegativeButtonClickListener {
                    datePicker.dismiss()
                }
            }

            btnTanggalAcara.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Pelaksanaan")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy" , Locale("id" , "ID"))
                    tanggalAcara = dateFormat.format(Date(it)).toString()
                    btnTanggalAcara.text = SimpleDateFormat("dd MMMM yyyy" , Locale("id" , "ID")).format(it)
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
                val dp = etNominal.text.toString()

                if (namaCp.isEmpty()) {
                    Toast.makeText(context , "Nama CP tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (namaInstansi.isEmpty()) {
                    Toast.makeText(context, "Nama Instansi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (noTelp.isEmpty()) {
                    Toast.makeText(context , "Nomor telepon tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (alamat.isEmpty()) {
                    Toast.makeText(context , "Alamat tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (tanggal.isEmpty()) {
                    Toast.makeText(context , "Tanggal tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (tanggalAcara.isEmpty()) {
                    Toast.makeText(context , "Tanggal tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (dp.isEmpty()) {
                    Toast.makeText(context , "DP tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else {
                    loading.isVisible = true
                    val pengunjung = hashMapOf(
                        "namaCp" to namaCp,
                        "namaInstansi" to namaInstansi,
                        "noTelp" to noTelp,
                        "alamat" to alamat,
                        "tanggal" to SimpleDateFormat("dd-MM-yyyy hh:mm:ss a" , Locale("id" , "ID")).parse("$tanggal 00:00:00 AM"),
                        "dp" to dp,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("pengunjung")
                        .add(pengunjung)
                        .addOnSuccessListener {
                            Toast.makeText(context , "Data berhasil disimpan" , Toast.LENGTH_LONG).show()
                            loading.isVisible = false
                            etNamaCp.text?.clear()
                            etNamaInstansi.text?.clear()
                            etNoTelp.text?.clear()
                            etAlamat.text?.clear()
                            etNominal.text?.clear()
                            tanggal = ""
                            tanggalAcara = ""
                            btnTanggal.text = getString(R.string.tgl)
                            btnTanggalAcara.text = getString(R.string.tanggal_pelaksanaan)
                        }
                        .addOnFailureListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}