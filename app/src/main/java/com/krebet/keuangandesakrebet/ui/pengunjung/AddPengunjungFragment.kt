package com.krebet.keuangandesakrebet.ui.pengunjung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

        onBackPressed()

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            val locale = Locale("id", "ID")

            //tambah titik & Rp saat mngetik nominal
            etDp.addTextChangedListener (object :android.text.TextWatcher{
                private var current = ""

                override fun  beforeTextChanged(s: CharSequence?, start: Int, count: Int, sfter: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: android.text.Editable?) {
                    if (s.toString() != current) {
                        etDp.removeTextChangedListener(this)

                        val cleanString = s.toString()
                            .replace("Rp", "")
                            .replace(".", "")
                            .replace(",", "")
                            .trim()

                        if (cleanString.isNotEmpty()) {
                            try {
                                val parsed = cleanString.toLong()
                                val formatter = java.text.NumberFormat.getInstance(locale)
                                val formatted = "Rp" + formatter.format(parsed)

                                current = formatted
                                etDp.setText(formatted)
                                etDp.setSelection(formatted.length)
                            } catch (e: NumberFormatException) {
                                // biarkan jika error
                            }
                        }

                        etDp.addTextChangedListener(this)
                    }
                }
            })
            btnTanggalKunjungan.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Kunjungan")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy" , locale)
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
                    .replace("Rp", "") //Hilangkan simbol mata uang jika tersimpan di database agar tidak eror
                    .replace(".", "") //Hilangkan pemisah ribuan jika tersimpan di database agar tidak eror
                    .replace(",", "") //(Opsional) Hilangkan koma jika ada yang copy-paste format asing saat disimpan di database
                    .trim()

                // tampilan data tidak boleh ada yang kosong
                if (namaCp.isEmpty()) {
                    showToast("Nama CP tidak boleh kosong")
                } else if (noTelp.isEmpty()) {
                    showToast("Nomor telepon tidak boleh kosong")
                } else if (namaInstansi.isEmpty()) {
                    showToast("Nama instansi tidak boleh kosong")
                } else if (alamat.isEmpty()) {
                    showToast("Alamat tidak boleh kosong")
                } else if (tglKunjungan == null) {
                    showToast("Tanggal kunjungan tidak boleh kosong")
                } else if (dp.isEmpty()) {
                    showToast("DP tidak boleh kosong")
                } else if (dp.toFloat() <=0){
                    showToast("Nominal harus lebih dari 0")//nominal lebih dari 0 jika nominal 0 akan gagal menyimpan
                } else {
                    showLoading()

                    val dp = dp.toFloat()
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
                        transaction.set(pengunjungRef, pengunjung)  //Masukkan data pengunjung ke db pengujung

                        transaction.update(lastIdPengunjungRef, "id", nextIdPengunjung)     //Update id pengunjung di db lastId

                        val pemasukan = hashMapOf(
                            "idPengunjung" to nextIdPengunjung,
                            "tglTransaksi" to serverTimestamp(),
                            "total" to dp.toFloat(),
                            "catatan" to "DP",
                            "createdAt" to serverTimestamp()
                        )

                        val pemasukanRef = db.collection("pemasukan").document(nextIdPemasukan)
                        transaction.set(pemasukanRef, pemasukan)  //Masukkan data pengunjung ke db pemasukan

                        transaction.update(lastIdPemasukanRef, "id", nextIdPemasukan)     //Update id pemasukan di db lastId

                    }.addOnSuccessListener {
                        showToast("Data berhasil disimpan")
                        dismissLoading()
                        if (isAdded) {
                            etNamaCp.text?.clear()
                            etNamaInstansi.text?.clear()
                            etNoTelp.text?.clear()
                            etAlamat.text?.clear()
                            etDp.text?.clear()
                            tglKunjungan = null
                            btnTanggalKunjungan.text = getString(R.string.tanggal_kunjungan)
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

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setBackState()
            }
        })
    }

    private fun setBackState() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout , HomeFragment())
        transaction.commit()
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