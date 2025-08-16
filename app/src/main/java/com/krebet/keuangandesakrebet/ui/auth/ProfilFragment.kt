package com.krebet.keuangandesakrebet.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.krebet.keuangandesakrebet.R
import com.krebet.keuangandesakrebet.databinding.FragmentProfilBinding
import com.krebet.keuangandesakrebet.prefs.Prefs
import com.krebet.keuangandesakrebet.ui.home.HomeFragment

@Suppress("SpellCheckingInspection")
class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)

        onBackPressed()

        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        binding.apply {
            val user = auth.currentUser
            if (user != null) {
                tvMail.text = user.email
                binding.btnLogout.setOnClickListener {
                    auth.signOut() //untuk logout
                    Prefs.saveLoginStatus(requireContext(), false)
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finishAffinity()
                }
            }

            btnKembali.setOnClickListener {
                setBackState()
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setBackState()
            }
        })
    }

    private fun setBackState() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout , HomeFragment())
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}