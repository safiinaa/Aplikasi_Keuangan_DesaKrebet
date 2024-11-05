package com.krebet.keuangandesakrebet.auth

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
import com.krebet.keuangandesakrebet.databinding.ActivityForgetPasswordBinding

@Suppress("SpellCheckingInspection")
class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }

        binding.apply {
            btnReset.setOnClickListener {
                val email = etMail.text.toString()

                if (email.isEmpty()) {
                    Toast.makeText(this@ForgetPasswordActivity, "Mohon isi email", Toast.LENGTH_SHORT).show()
                } else {
                    loading.isVisible = true
                    Firebase.auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this@ForgetPasswordActivity, "Link reset password sudah dikirim, silahkan cek email", Toast.LENGTH_SHORT).show()
                                etMail.text?.clear()
                                loading.isVisible = false
                            } else {
                                Toast.makeText(this@ForgetPasswordActivity, it.exception?.message, Toast.LENGTH_LONG).show()
                                loading.isVisible = false
                            }
                        }
                }
            }
        }
    }
}