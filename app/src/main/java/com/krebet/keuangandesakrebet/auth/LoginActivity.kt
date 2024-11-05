package com.krebet.keuangandesakrebet.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.ActivityLoginBinding
import com.krebet.keuangandesakrebet.home.HomeActivity

@Suppress("SpellCheckingInspection")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            btnLogin.setOnClickListener {
                val email = etMail.text.toString()
                val password = etPass.text.toString()

                if (email.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Mohon isi email", Toast.LENGTH_SHORT).show()
                } else if (password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Mohon isi password", Toast.LENGTH_SHORT).show()
                } else {
                    loading.isVisible = true
                    Firebase.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                loading.isVisible = false
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