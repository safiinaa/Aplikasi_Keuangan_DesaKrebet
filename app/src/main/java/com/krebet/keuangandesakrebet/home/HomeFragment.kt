package com.krebet.keuangandesakrebet.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.krebet.keuangandesakrebet.AddPengunjungFragment
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentHomeBinding
import com.krebet.keuangandesakrebet.pemasukan.AddPemasukanFragment
import com.krebet.keuangandesakrebet.pengeluaran.AddPengeluaranFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isMenuOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            btnSemua.setOnClickListener {

            }
            btnPemasukan.setOnClickListener {

            }
            btnPengeluaran.setOnClickListener {

            }

            floatingActionButton.setColorFilter(Color.WHITE)
            floatingActionButton.setOnClickListener {
                if (isMenuOpen) {
                    closeMenu()
                } else {
                    openMenu()
                }
            }

            btnInputNama.setOnClickListener {
                loadFragment(AddPengunjungFragment())
                closeMenu()
            }

            btnTambahPemasukan.setOnClickListener {
                loadFragment(AddPemasukanFragment())
                closeMenu()
            }

            btnTambahPengeluaran.setOnClickListener {
                loadFragment(AddPengeluaranFragment())
                closeMenu()
            }
        }
    }

    private fun openMenu() {
        binding.apply {
            btnInputNama.isVisible = true
            btnTambahPemasukan.isVisible = true
            btnTambahPengeluaran.isVisible = true
            isMenuOpen = true
        }
    }

    private fun closeMenu() {
        binding.apply {
            btnInputNama.isVisible = false
            btnTambahPemasukan.isVisible = false
            btnTambahPengeluaran.isVisible = false
            isMenuOpen = false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout ,fragment)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}