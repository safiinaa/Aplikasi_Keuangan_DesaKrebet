package com.krebet.keuangandesakrebet.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.krebet.keuangandesakrebet.LaporanFragment
import com.krebet.keuangandesakrebet.pendapatan.PendapatanFragment
import com.krebet.keuangandesakrebet.pengeluaran.PengeluaranFragment
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadFragment(HomeFragment())

        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_fragment -> {
                    loadFragment(HomeFragment())
                }
                R.id.pendapatan_fragment -> {
                    loadFragment(PendapatanFragment())
                }
                R.id.pengeluaran_fragment -> {
                    loadFragment(PengeluaranFragment())
                }
                R.id.laporan_fragment -> {
                    loadFragment(LaporanFragment())
                }
                else -> {

                }
            }
            true
        }

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout ,fragment)
        transaction.commit()
    }
}