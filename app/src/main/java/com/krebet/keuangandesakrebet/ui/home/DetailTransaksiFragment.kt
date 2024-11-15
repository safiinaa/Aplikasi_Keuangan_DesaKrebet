package com.krebet.keuangandesakrebet.ui.home

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentDetailTransaksiBinding
import com.krebet.keuangandesakrebet.model.Transaksi
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class DetailTransaksiFragment : Fragment() {

    private var _binding: FragmentDetailTransaksiBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaksi: Transaksi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailTransaksiBinding.inflate(inflater, container, false)

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
            val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(transaksi.tanggal!!.toDate())
            val formatRp = DecimalFormat("Rp ###,###,###")

            if (transaksi.jenis == "pemasukan") {
                tvNominal.isVisible = false
                tvQty.isVisible = false
                imageView.isVisible = false
                textView3.isVisible = false
                textView5.isVisible = false
                textView5.isVisible = false
            }

            tvNama.text = transaksi.pengunjung?.nama
            tvTanggal.text = date
            tvNominal.text = formatRp.format(transaksi.nominal)
            tvQty.text = transaksi.qty?.toInt().toString()
            tvTotal.text = formatRp.format(transaksi.total)
            tvCatatan.text = transaksi.catatan

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