package com.krebet.keuangandesakrebet.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ActivityLoginBinding
import com.krebet.keuangandesakrebet.prefs.Prefs
import com.krebet.keuangandesakrebet.ui.home.HomeActivity

@Suppress("SpellCheckingInspection")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Prefs.isLogin(this)) {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        binding.apply {
            btnLogin.setOnClickListener {
                val email = etMail.text.toString()
                val password = etPass.text.toString()

                if (email.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else {
                    loading.isVisible = true
                    Firebase.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                loading.isVisible = false
                                Prefs.saveLoginStatus(this@LoginActivity, true)
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                finishAffinity()
                            } else {
                                loading.isVisible = false
                                Toast.makeText(this@LoginActivity, it.exception?.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }

            btnForget.setOnClickListener {
                startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
            }
        }
    }
}