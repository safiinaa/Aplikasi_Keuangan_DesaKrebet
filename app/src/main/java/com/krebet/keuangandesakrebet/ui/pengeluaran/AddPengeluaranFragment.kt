package com.krebet.keuangandesakrebet.ui.pengeluaran

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.krebet.keuangandesakrebet.databinding.FragmentAddPengeluaranBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class AddPengeluaranFragment : Fragment() {

    private var _binding: FragmentAddPengeluaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var pengunjung: Pengunjung

    private var tglTransaksi: Date? = null
    private var total: Float? = null

    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPengeluaranBinding.inflate(inflater, container, false)

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
                    .setTitleText("Pilih Tanggal")
                    .build()
                datePicker.show(parentFragmentManager , "DatePicker")
                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd MMMM yyyy" , Locale("id", "ID"))
                    val date = Date(it)
                    tglTransaksi = date
                    btnTglTransaksi.text = sdf.format(date).toString()
                    Toast.makeText(context, tglTransaksi.toString(), Toast.LENGTH_LONG).show()
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
                val catatan = etCatatan.text.toString()
                val nominal = etNominal.text.toString()
                val jumlah = etQty.text.toString()

                if (tglTransaksi == null) {
                    Toast.makeText(context, "Tanggal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (nominal.isEmpty()) {
                    Toast.makeText(context, "Nominal tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (jumlah.isEmpty()) {
                    Toast.makeText(context, "Jumlah tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else if (catatan.isEmpty()) {
                    Toast.makeText(context, "Catatan tidak boleh kosong", Toast.LENGTH_LONG).show()
                } else {
                    loading.isVisible = true

                    val lastIdDocRef = db.collection("lastId").document("lastIdPengeluaran")

                    db.runTransaction {  transaction ->
                        val snapshot = transaction.get(lastIdDocRef)
                        val lastId = snapshot.getString("id") ?: "K01"
                        val nextId = generateNextId(lastId)

                        val pengeluaran = hashMapOf(
                            "idPengunjung" to pengunjung.id,
                            "tglTransaksi" to tglTransaksi,
                            "nominal" to nominal.toFloat(),
                            "qty" to jumlah.toFloat(),
                            "total" to total,
                            "catatan" to catatan,
                            "createdAt" to serverTimestamp()
                        )

                        val pengeluaranRef = db.collection("pengeluaran").document(nextId)
                        transaction.set(pengeluaranRef, pengeluaran)

                        transaction.update(lastIdDocRef, "id", nextId)

                    }.addOnSuccessListener {
                        Toast.makeText(context, "Data berhasil disimpan", Toast.LENGTH_LONG).show()
                        loading.isVisible = false
                        btnTglTransaksi.text = getString(R.string.tgl)
                        tglTransaksi = null
                        total = null
                        etNominal.text?.clear()
                        etQty.text?.clear()
                        etCatatan.text?.clear()
                        tvTotal.text = getString(R.string.saldo2)
                    }.addOnFailureListener {
                        Toast.makeText(context, "Terjadi kesalahan, silahkan ulangi kembali", Toast.LENGTH_LONG).show()
                        loading.isVisible = false
                    }
                }
            }

            btnKembali.setOnClickListener {
                setBackState()
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

    private fun generateNextId(lastId: String): String {
        val number = lastId.substring(1).toInt() // Ambil angka setelah K
        val nextNumber = number + 1
        return "K" + nextNumber.toString().padStart(2, '0') // Format menjadi K01 dst
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
        val fragment = SemuaPengeluaranFragment()
        val mBundle = Bundle()
        mBundle.putParcelable("data", data)

        fragment.arguments = mBundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}