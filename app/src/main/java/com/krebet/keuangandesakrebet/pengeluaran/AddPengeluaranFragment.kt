package com.krebet.keuangandesakrebet.pengeluaran

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.krebet.keuangandesakrebet.home.HomeFragment
import com.krebet.keuangandesakrebet.model.Pengunjung
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
    private var price: Float? = null
    private var qty: Float? = null
    private var total: Float? = null
    private lateinit var visitors: List<Pengunjung>
    private lateinit var selectedVisitor: String

    private lateinit var listener: ListenerRegistration
    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPengeluaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            etNama.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence? , start: Int , count: Int , after: Int) {}

                override fun onTextChanged(s: CharSequence? , start: Int , before: Int , count: Int) {
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        searchVisitor(query)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            etNama.setOnItemClickListener { parent , _ , position , _ ->
                selectedVisitor = parent.getItemAtPosition(position).toString()
                val visitorData = visitors.find { it.nama == selectedVisitor }
                visitorData.let {
                    visitorId = it?.idPengunjung
                    etAlamat.setText(it?.alamat)
                }
                etNama.dismissDropDown()
                etNama.clearFocus()
            }

            btnTanggal.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Kunjungan")
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
                val nama = etNama.text.toString()
                val catatan = etCatatan.text.toString()

                if (nama.isEmpty()) {
                    Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (price == null) {
                    Toast.makeText(context, "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (qty == null) {
                    Toast.makeText(context, "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (catatan.isEmpty()) {
                    Toast.makeText(context, "Catatan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (tanggal == null) {
                    Toast.makeText(context, "Tanggal tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else {
                    loading.isVisible = true
                    val pemasukan = hashMapOf(
                        "idPengunjung" to visitorId,
                        "tanggal" to tanggal,
                        "nominal" to price,
                        "qty" to qty,
                        "total" to total,
                        "catatan" to catatan,
                        "createdAt" to serverTimestamp()
                    )

                    db.collection("pengeluaran")
                        .add(pemasukan)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Data berhasil disimpan", Toast.LENGTH_LONG).show()
                            loading.isVisible = false
                            etNama.text?.clear()
                            etAlamat.text?.clear()
                            btnTanggal.text = getString(R.string.tgl)
                            price = null
                            qty = null
                            total = null
                            etNominal.text?.clear()
                            etQty.text?.clear()
                            etCatatan.text?.clear()
                            tvTotal.text = getString(R.string.saldo2)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Terjadi kesalahan, silahkan ulangi kembali", Toast.LENGTH_LONG).show()
                            loading.isVisible = false
                            Log.e("Add Pengeluaran", it.message.toString())
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
                price = nominal.toFloat()
                qty = jumlah.toFloat()
                total = price!! * qty!!
                val formatRp = DecimalFormat("Rp ###,###,###").format(total)
                binding.tvTotal.text = formatRp
            }
        }
    }

    private fun searchVisitor(query: String) {
        val docRef = db.collection("pengunjung")
            .orderBy("nama")
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
                    visitor.copy(idPengunjung = it.id)
                }.distinctBy { it.nama }

                val adapter = ArrayAdapter<String>(requireContext() , R.layout.list_item , visitors.map { it.nama })
                binding.etNama.setAdapter(adapter)
                binding.etNama.showDropDown()
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