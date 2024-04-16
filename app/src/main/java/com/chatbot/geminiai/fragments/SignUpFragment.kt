package com.chatbot.geminiai.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.chatbot.geminiai.R
import com.chatbot.geminiai.databinding.FragmentSignUpBinding
import com.chatbot.geminiai.utils.AuthState
import com.chatbot.geminiai.utils.User
import com.chatbot.geminiai.viewmodel.AuthViewModel
import com.chatbot.geminiai.viewmodel.UserViewModel
import com.google.android.gms.auth.api.identity.SignInPassword
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var binding: FragmentSignUpBinding ?= null
    @Inject
    lateinit var authViewModel: AuthViewModel
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    @Inject
    lateinit var userViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSignUp()
        handleGoogleSignIn()
        goToSignInFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun handleSignUp() {
        binding?.SignUpbtn?.setOnClickListener {
            val email = binding?.inputEmail?.text.toString()
            val password = binding?.inputPassword?.text.toString()
            val confirmPassword = binding?.inputConfirmPassword?.text.toString()
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill all details!", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Invalid Email!", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(
                    context,
                    "Password and Confirm Password should be same",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password.length < 8) {
                Toast.makeText(
                    context,
                    "Password should be more than 8 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                AuthState.Loading
                authViewModel.signUp(email, password)
            }
            authViewModel.authState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is AuthState.Error -> {
                        hideLoading()
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }

                    AuthState.Loading -> {
                        showLoading()
                    }

                    AuthState.Success -> {
                        hideLoading()
                        val currentUser = authViewModel.currentUser()
                        val user = User(currentUser?.uid, currentUser?.email, password, currentUser?.displayName, currentUser?.photoUrl.toString())
                        userViewModel.saveUser(user)
                        Toast.makeText(context, "Registration Successfull!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
                    }
                }
            }
        }
    }
    private fun hideLoading() {
        binding?.progressBar?.visibility = View.GONE
    }
    private fun showLoading() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    private fun handleGoogleSignIn(){
        binding?.googleBtn?.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
            authViewModel.authState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is AuthState.Error -> {
                        hideLoading()
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }

                    AuthState.Loading -> {
                    }

                    AuthState.Success -> {
                        hideLoading()
                        val currentUser = authViewModel.currentUser()
                        val user = User(currentUser?.uid, currentUser?.email, null, currentUser?.displayName, currentUser?.photoUrl.toString())
                        userViewModel.saveUser(user)
                        Toast.makeText(context, "SignIn Successfull!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
                    }
                }
            }
        }
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            val accountData = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (accountData.isSuccessful){
                val account: GoogleSignInAccount ?= accountData.result
                val token = account?.idToken
                authViewModel.googleSignIn(token)
            }else{
                Toast.makeText(context, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun goToSignInFragment(){
        binding?.signinbtn?.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }
}