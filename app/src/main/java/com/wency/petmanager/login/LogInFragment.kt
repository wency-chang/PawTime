package com.wency.petmanager.login

import android.os.Bundle
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentLoadingBinding
import com.wency.petmanager.databinding.FragmentLoginBinding
import com.wency.petmanager.ext.getVmFactory

class LogInFragment: Fragment() {

    lateinit var binding : FragmentLoginBinding
    private val viewModel by viewModels<LogInViewModel>(){getVmFactory()}
    private val loginActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            viewModel.getGoogleSignInActivityResult(it)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)



        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.secreteButton.setOnClickListener {
            findNavController().navigate(NavHostDirections.actionGlobalToLoadingFragment())
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        com.wency.petmanager.profile.UserManager.gso = gso

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val signInIntent = googleSignInClient.signInIntent
        binding.googleSignInButton.setOnClickListener {
            loginActivity.launch(signInIntent)

        }

        viewModel.logInSuccess.observe(viewLifecycleOwner, Observer {
            if (it){
                findNavController().navigate(NavHostDirections.actionGlobalToLoadingFragment())
            }
        })


    }


}