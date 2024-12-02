package com.krebet.keuangandesakrebet.ui.pemasukan

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentEditPemasukanBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class EditPemasukanFragment : Fragment() {

    private var _binding: FragmentEditPemasukanBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaksi: Transaksi

    private var tanggal: Date? = null
    private var alamat: String? = null
    private var idPengunjung: String? = null
    private lateinit var visitors: List<Pengunjung>
    private lateinit var selectedVisitor: String

    private lateinit var listener: ListenerRegistration
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPemasukanBinding.inflate(inflater, container, false)

        transaksi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BundleCompat.getParcelable(requireArguments(), "data", Transaksi::class.java)!!
        } else {
            arguments?.getParcelable("data")!!
        }

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            val formatDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

            transaksi.let {
                idPengunjung = it.idPengunjung
                alamat = it.pengunjung?.alamat
                tanggal = it.tglTransaksi?.toDate()
                etNamaInstansi.setText(it.pengunjung?.namaInstansi)
                tvAlamat.text = it.pengunjung?.alamat
                btnTanggal.text = formatDate.format(tanggal!!)
                etNominal.setText(it.total?.toInt().toString())
                etCatatan.setText(it.catatan)
            }

            etNamaInstansi.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence? , start: Int , count: Int , after: Int) {}

                override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        alamat = null
                        tvAlamat.text = getString(R.string.alamat)
                        searchVisitor(query)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            etNamaInstansi.setOnItemClickListener { parent , _ , position , _ ->
                selectedVisitor = parent.getItemAtPosition(position).toString()
                val visitorData = visitors.find { it.namaInstansi == selectedVisitor }
                visitorData.let {
                    idPengunjung = it?.id
                    alamat = it?.alamat
                    tvAlamat.text = alamat
                }
                etNamaInstansi.dismissDropDown()
                etNamaInstansi.clearFocus()
            }

            btnTanggal.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Transaksi")
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
                val nama = etNamaInstansi.text.toString()
                val nominal = etNominal.text.toString()
                val catatan = etCatatan.text.toString()

                if (nama.isEmpty()) {
                    Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (alamat == null) {
                    Toast.makeText(context, "Nama belum disimpan, simpan pada menu tambah pengunjung", Toast.LENGTH_LONG).show()
                } else if (tanggal == null) {
                    Toast.makeText(context, "Tanggal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (nominal.isEmpty()) {
                    Toast.makeText(context, "Nominal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (catatan.isEmpty()) {
                    Toast.makeText(context, "Catatan tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else {
                    loading.isVisible = true
                    val pemasukan = hashMapOf(
                        "idPengunjung" to idPengunjung,
                        "tglTransaksi" to tanggal,
                        "total" to nominal.toFloat(),
                        "catatan" to catatan,
                        "updatedAt" to serverTimestamp()
                    )

                    db.collection("pemasukan")
                        .document(transaksi.idTransaksi!!)
                        .update(pemasukan)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Data berhasil diperbarui", Toast.LENGTH_LONG).show()
                            loading.isVisible = false
                            loadFragment()
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

                        db.collection("pemasukan")
                            .document(transaksi.idTransaksi!!)
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

    private fun searchVisitor(query: String) {
        val docRef = db.collection("pengunjung")
            .orderBy("namaInstansi")
            .startAt(query)
            .endAt(query + "\uf8ff")
        listener = docRef.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(context, "Terjadi kesalahan, silahkan ulangi kembali", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (value != null) {
                visitors = value.mapNotNull {
                    val visitor = it.toObject(Pengunjung::class.java)
                    visitor.copy(id = it.id)
                }.distinctBy { it.namaInstansi }

                val adapter = ArrayAdapter<String>(requireContext() , R.layout.list_item , visitors.map { it.namaInstansi })
                if (isAdded) {
                    binding.etNamaInstansi.setAdapter(adapter)
                    binding.etNamaInstansi.showDropDown()
                }
            } else {
                Toast.makeText(context, "Nama tidak ditemukan", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFragment() {
        val pengunjung = transaksi.pengunjung
        val data = Pengunjung(
            id = pengunjung?.id ,
            namaInstansi = pengunjung?.namaInstansi ,
            alamat = pengunjung?.alamat
        )
        val fragment = SemuaPemasukanFragment()
        val mBundle = Bundle()
        mBundle.putParcelable("data", data)

        fragment.arguments = mBundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    override fun onStop() {
        super.onStop()
        if (::listener.isInitialized) {
            listener.remove()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::listener.isInitialized) {
            listener.remove()
        }
        _binding = null
    }
}