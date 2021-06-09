package com.wency.petmanager.login

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentPrivacyBinding

class PolicyFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentPrivacyBinding.inflate(layoutInflater, container, false)
        binding.policyText.text = Html.fromHtml(getString(R.string.PRIVACY_POLICY), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)

        binding.privacyBackButton.setOnClickListener {
            findNavController().navigate(NavHostDirections.actionGlobalToLoginFragment())
        }

        return binding.root
    }
}