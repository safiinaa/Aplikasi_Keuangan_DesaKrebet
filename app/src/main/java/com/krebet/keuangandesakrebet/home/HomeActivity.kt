package com.krebet.keuangandesakrebet.home

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.krebet.keuangandesakrebet.LaporanFragment
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ActivityHomeBinding
import com.krebet.keuangandesakrebet.pemasukan.PemasukanFragment
import com.krebet.keuangandesakrebet.pengeluaran.PengeluaranFragment

@Suppress("DEPRECATION" , "SpellCheckingInspection")
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

        //Ikon status bar tetap hitam walaupun menggunakan dark mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        loadFragment(HomeFragment())

        binding.apply {
            bottomNavBar.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.home_fragment -> {
                        loadFragment(HomeFragment())
                    }
                    R.id.pemasukan_fragment -> {
                        loadFragment(PemasukanFragment())
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
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout ,fragment)
        transaction.commit()
    }
}