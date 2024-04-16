package com.chatbot.geminiai.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.chatbot.geminiai.R
import com.chatbot.geminiai.databinding.FragmentProfileBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation


class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding ?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moveToHomeFragment()
        getUserInfo()
        goBackToHomeFragment()
    }

    private fun moveToHomeFragment(){
        binding?.profileToolbar?.backBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }
    private fun getUserInfo(){
        val auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null && currentUser.photoUrl != null){
            Picasso.get().load(currentUser.photoUrl).transform(CropCircleTransformation()).into(binding?.userPfp)
        }else{
            binding?.userPfp?.setImageResource(R.drawable.user_pfp)
        }

        if(currentUser != null && currentUser.displayName.toString() != ""){
            binding?.userName?.text = currentUser.displayName
        }else{
            binding?.userName?.text = currentUser?.email
        }
        if(currentUser != null && currentUser.email != null){
            binding?.email?.text = currentUser.email
        }else{
            binding?.email?.text = currentUser?.email
        }

        binding?.constraintLayout4?.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                Uri.parse("mailto:")
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("anonymouslike083@gmail.com"))
                `package` = "com.google.android.gm"
            }
            startActivity(Intent.createChooser(intent, "Send email to: "))
        }
        binding?.signOut?.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_signInFragment)
        }
    }

    private fun goBackToHomeFragment(){
        val callback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}