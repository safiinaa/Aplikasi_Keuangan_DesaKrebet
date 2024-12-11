package com.krebet.keuangandesakrebet.ui.pengunjung

import android.app.AlertDialog
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
import com.krebet.keuangandesakrebet.databinding.FragmentEditPengunjungBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class EditPengunjungFragment : Fragment() {

    private var _binding: FragmentEditPengunjungBinding? = null
    private val binding get() = _binding!!

    private lateinit var pengunjung: Pengunjung

    private var tglKunjungan: Date? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPengunjungBinding.inflate(inflater, container, false)

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
            val formatDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

            pengunjung.let {
                tglKunjungan = it.tglKunjungan?.toDate()
                etNamaCp.setText(it.namaCp)
                etNamaInstansi.setText(it.namaInstansi)
                etNoTelp.setText(it.noTelp)
                etAlamat.setText(it.alamat)
                btnTanggalKunjungan.text = formatDate.format(tglKunjungan!!)
//                etDp.setText(it.dp?.toInt().toString())
            }

            btnTanggalKunjungan.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Kunjungan")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd MMMM yyyy" , Locale("id", "ID"))
                    val date = Date(it)
                    tglKunjungan = date
                    btnTanggalKunjungan.text = sdf.format(date).toString()
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
//                val dp = etDp.text.toString()

                if (namaCp.isEmpty()) {
                    showToast("Nama CP tidak boleh kosong")
                } else if (noTelp.isEmpty()) {
                    showToast("Nomor telepon tidak boleh kosong")
                } else if (namaInstansi.isEmpty()) {
                    showToast("Nama Instansi tidak boleh kosong")
                } else if (alamat.isEmpty()) {
                    showToast("Alamat tidak boleh kosong")
                } else if (tglKunjungan == null) {
                    showToast("Tanggal tidak boleh kosong")
//                } else if (dp.isEmpty()) {
//                    Toast.makeText(context, "DP tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else {
                    showLoading()
                    val pengunjung = hashMapOf(
                        "namaCp" to namaCp,
                        "namaInstansi" to namaInstansi,
                        "noTelp" to noTelp,
                        "alamat" to alamat,
                        "tglKunjungan" to tglKunjungan,
//                        "dp" to dp.toFloat(),
                        "updatedAt" to serverTimestamp()
                    )

                    db.collection("pengunjung")
                        .document(this@EditPengunjungFragment.pengunjung.id!!)
                        .update(pengunjung)
                        .addOnSuccessListener {
                            showToast("Data berhasil diperbarui")
                            dismissLoading()
                        }
                        .addOnFailureListener {
                            showToast("Terjadi kesalahan, silahkan ulangi kembali")
                            dismissLoading()
                        }
                }
            }

            btnHapus.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Hapus Data")
                    .setMessage("Anda yakin ingin menghapus? Data ini tidak dapat dipulihkan setelah dihapus")
                    .setPositiveButton("Hapus") { _, _ ->

                        db.collection("pengunjung")
                            .document(pengunjung.id!!)
                            .delete()
                            .addOnSuccessListener {
                                showToast("Data berhasil dihapus")
                                setBackState()
                            }
                            .addOnFailureListener {
                                showToast("Terjadi kesalahan, silahkan ulangi kembali")
                            }

                    }
                    .setNegativeButton("Batal", null)
                    .show()
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
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout , PengunjungFragment())
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