package com.krebet.keuangandesakrebet.ui.pengeluaran

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentAddPengeluaranBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import com.krebet.keuangandesakrebet.ui.home.HomeFragment
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection")
class AddPengeluaranFragment : Fragment() {

    private var _binding: FragmentAddPengeluaranBinding? = null
    private val binding get() = _binding!!

    private var tanggal: Date? = null
    private var visitorId: String? = null
    private var alamat: String? = null
    private var total: Float? = null
    private lateinit var visitors: List<Pengunjung>
    private lateinit var selectedVisitor: String

    private lateinit var listener: ListenerRegistration
    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPengeluaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
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
                    visitorId = it?.id
                    alamat = it?.alamat
                    tvAlamat.text = alamat
                }
                etNamaInstansi.dismissDropDown()
                etNamaInstansi.clearFocus()
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

            etNominal.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence? , start: Int , count: Int , after: Int) {}

                override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    calculateTotal()
                }
            })

            etQty.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence? , start: Int , count: Int , after: Int) {}

                override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    calculateTotal()
                }
            })

            btnSimpan.setOnClickListener {
                val nama = etNamaInstansi.text.toString()
                val catatan = etCatatan.text.toString()
                val nominal = etNominal.text.toString()
                val jumlah = etQty.text.toString()

                if (nama.isEmpty()) {
                    Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (alamat == null) {
                    Toast.makeText(context, "Nama belum disimpan, simpan pada menu tambah namaInstansi pengunjung", Toast.LENGTH_LONG).show()
                } else if (tanggal == null) {
                    Toast.makeText(context, "Tanggal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (nominal.isEmpty()) {
                    Toast.makeText(context, "Nominal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (jumlah.isEmpty()) {
                    Toast.makeText(context, "Jumlah tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (catatan.isEmpty()) {
                    Toast.makeText(context, "Catatan tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else {
                    loading.isVisible = true
                    val pemasukan = hashMapOf(
                        "idPengunjung" to visitorId,
                        "tanggal" to tanggal,
                        "nominal" to nominal.toFloat(),
                        "qty" to jumlah.toFloat(),
                        "total" to total,
                        "catatan" to catatan,
                        "createdAt" to serverTimestamp()
                    )

                    db.collection("pengeluaran")
                        .add(pemasukan)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Data berhasil disimpan", Toast.LENGTH_LONG).show()
                            loading.isVisible = false
                            etNamaInstansi.text?.clear()
                            btnTanggal.text = getString(R.string.tgl)
                            tanggal = null
                            alamat = null
                            total = null
                            tvAlamat.text = getString(R.string.alamat)
                            etNominal.text?.clear()
                            etQty.text?.clear()
                            etCatatan.text?.clear()
                            tvTotal.text = getString(R.string.saldo2)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Terjadi kesalahan, silahkan ulangi kembali", Toast.LENGTH_LONG).show()
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

    @SuppressLint("SetTextI18n")
    private fun calculateTotal() {
        binding.apply {
            val nominal = etNominal.text.toString()
            val jumlah = etQty.text.toString()

            if (nominal.isNotEmpty() && jumlah.isNotEmpty()) {
                val price = nominal.toFloat()
                val qty = jumlah.toFloat()
                total = price * qty
                val formatRp = DecimalFormat("Rp ###,###,###").format(total)
                binding.tvTotal.text = formatRp
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
                binding.etNamaInstansi.setAdapter(adapter)
                binding.etNamaInstansi.showDropDown()
            } else {
                Toast.makeText(context, "Nama tidak ditemukan", Toast.LENGTH_LONG).show()
            }
        }
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