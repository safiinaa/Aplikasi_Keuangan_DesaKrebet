package com.krebet.keuangandesakrebet.ui.laporan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentLaporanBinding
import com.krebet.keuangandesakrebet.databinding.FragmentPengunjungBinding
import com.krebet.keuangandesakrebet.ui.home.HomeFragment
import com.krebet.keuangandesakrebet.ui.pengunjung.PengunjungFragment

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnArusKas.setOnClickListener {
            loadFragment(LaporanArusKasFragment())
        }
    }
    private fun loadFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout ,fragment)
        transaction.commit()
    }
}