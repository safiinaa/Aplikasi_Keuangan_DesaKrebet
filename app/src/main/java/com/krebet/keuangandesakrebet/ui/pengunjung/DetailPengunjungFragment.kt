package com.krebet.keuangandesakrebet.ui.pengunjung

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentDetailPengunjungBinding
import com.krebet.keuangandesakrebet.model.Pengunjung
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("SpellCheckingInspection" , "DEPRECATION")
class DetailPengunjungFragment : Fragment() {

    private var _binding: FragmentDetailPengunjungBinding? = null
    private val binding get() = _binding!!

    private lateinit var pengunjung: Pengunjung

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPengunjungBinding.inflate(inflater, container, false)

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
            pengunjung.let {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = dateFormat.format(it.tanggal!!.toDate())

                tvNama.text = it.nama
                tvNoTelp.text = it.noTelp
                tvAlamat.text = it.alamat
                tvTanggal.text = date
            }

            btnKembali.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.frameLayout , PengunjungFragment())
                transaction.commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}