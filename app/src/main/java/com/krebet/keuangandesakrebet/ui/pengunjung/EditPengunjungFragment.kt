package com.krebet.keuangandesakrebet.ui.pengunjung

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private var tanggal: Date? = null

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

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            val formatDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

            pengunjung.let {
                tanggal = it.tanggal?.toDate()
                etNamaCp.setText(it.namaCp)
                etNamaInstansi.setText(it.namaInstansi)
                etNoTelp.setText(it.noTelp)
                etAlamat.setText(it.alamat)
                btnTanggal.text = formatDate.format(tanggal!!)
                etNominal.setText(it.dp?.toInt().toString())
            }

            btnTanggal.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd MMMM yyyy" , Locale("id", "ID"))
                    val date = Date(it)
                    tanggal = date
                    btnTanggal.text = sdf.format(date).toString()
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
                    Toast.makeText(context, "Nama CP tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (namaInstansi.isEmpty()) {
                    Toast.makeText(context, "Nama Instansi tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (noTelp.isEmpty()) {
                    Toast.makeText(context , "Nomor telepon tidak boleh kosong" , Toast.LENGTH_SHORT).show()
                } else if (alamat.isEmpty()) {
                    Toast.makeText(context, "Alamat tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (tanggal == null) {
                    Toast.makeText(context, "Tanggal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (dp.isEmpty()) {
                    Toast.makeText(context, "DP tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else {
                    loading.isVisible = true
                    val pengunjung = hashMapOf(
                        "namaCp" to namaCp,
                        "namaInstansi" to namaInstansi,
                        "noTelp" to noTelp,
                        "alamat" to alamat,
                        "tanggal" to tanggal,
                        "dp" to dp,
                        "updatedAt" to serverTimestamp()
                    )

                    db.collection("pengunjung")
                        .document(this@EditPengunjungFragment.pengunjung.id!!)
                        .update(pengunjung)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Data berhasil diperbarui", Toast.LENGTH_LONG).show()
                            loading.isVisible = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Terjadi kesalahan, silahkan ulangi kembali", Toast.LENGTH_LONG).show()
                            loading.isVisible = false
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
                                Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_LONG).show()
                                loadFragment()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Terjadi kesalahan, silahkan ulangi kembali", Toast.LENGTH_LONG).show()
                            }

                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }

            btnKembali.setOnClickListener {
                loadFragment()
            }
        }
    }

    private fun loadFragment() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout , PengunjungFragment())
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}