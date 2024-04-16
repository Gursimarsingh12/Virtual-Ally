package com.chatbot.geminiai.fragments

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatbot.geminiai.R
import com.chatbot.geminiai.adapter.MessageRvAdapter
import com.chatbot.geminiai.api.ChatRequest
import com.chatbot.geminiai.api.Content
import com.chatbot.geminiai.api.Part
import com.chatbot.geminiai.databinding.FragmentHomeBinding
import com.chatbot.geminiai.repository.ChatRepository
import com.chatbot.geminiai.utils.ApiState
import com.chatbot.geminiai.utils.ChatList
import com.chatbot.geminiai.utils.MessageRvModel
import com.chatbot.geminiai.utils.User
import com.chatbot.geminiai.viewmodel.AuthViewModel
import com.chatbot.geminiai.viewmodel.ChatViewModel
import com.chatbot.geminiai.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.*

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var chatRepository: ChatRepository
    @Inject
    lateinit var chatViewModel: ChatViewModel
    @Inject
    lateinit var userViewModel: UserViewModel
    @Inject
    lateinit var authViewModel: AuthViewModel
    private var binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var messageRvModelArrayList: ArrayList<MessageRvModel>
    private lateinit var messageRvAdapter: MessageRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        setUpRecyclerView()
        getPreviousChats()
        setupInputQueryButton()
        observeKeyboard()
        moveToProfileFragment()
    }

    private fun setUpRecyclerView() {
        messageRvModelArrayList = arrayListOf()
        messageRvAdapter = MessageRvAdapter(messageRvModelArrayList, chatViewModel)
        binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = messageRvAdapter
        }
    }

    private fun getPreviousChats() {
        val currentUser = auth.currentUser
        val user = User(currentUser?.uid, currentUser?.displayName, currentUser?.email, currentUser?.photoUrl.toString())
        chatViewModel.retrieveChatsFromDatabase(Firebase.database.reference, user)
        hideLoading()
        binding?.bgText?.visibility = View.GONE
        binding?.bgLogo?.visibility = View.GONE
        binding?.recyclerView?.visibility = View.VISIBLE
    }

    private fun setupInputQueryButton() {
        binding?.sendBtn?.setOnClickListener {
            val API_KEY = "AIzaSyCS87bapnHyyTuJ5mp0vcHwMNVoaLIpjzw"
            val prompt = binding?.inputQuery?.text.toString()
            if (prompt.isNotEmpty()) {
                val request = ChatRequest(
                    listOf(
                        Content(
                            listOf(
                                Part(
                                    prompt
                                )
                            )
                        )
                    )
                )
                chatViewModel.getAndSaveApiResponse(API_KEY, request)
                binding?.inputQuery?.text = null
            } else {
                Toast.makeText(context, "Please enter some input", Toast.LENGTH_SHORT).show()
            }
        }

        chatViewModel.responseData.observe(viewLifecycleOwner) { apiState ->
            when (apiState) {
                is ApiState.Error -> Toast.makeText(context, "Api Error", Toast.LENGTH_SHORT).show()

                ApiState.Loading ->{

                }

                is ApiState.Success -> {
                    hideLoading()
                }
            }
        }

        chatViewModel.chatList.observe(viewLifecycleOwner) { chatList ->
            updateUi(chatList)
        }
    }

    private fun hideLoading(){
        binding?.progressBar?.visibility = View.GONE
    }
    private fun updateUi(chatList: List<ChatList>){
        val currentUser = auth.currentUser
        messageRvModelArrayList.clear()
        for (chat in chatList) {
            messageRvModelArrayList.add(
                MessageRvModel(chat.request.toString(), currentUser?.photoUrl?.toString(), "user", currentUser?.uid, chat.request.toString(), "")
            )
            messageRvModelArrayList.add(
                MessageRvModel(chat.response.toString(), null, "bot", null, chat.response.toString(), chat.response.toString())
            )
        }
//        messageRvModelArrayList.reverse()
        if (messageRvModelArrayList.isNotEmpty()) {
            messageRvAdapter.notifyDataSetChanged()
            binding?.recyclerView?.smoothScrollToPosition(messageRvAdapter.itemCount - 1)
        }

        if (chatList.isNotEmpty()) {
            val latestResponse = chatList.last().response.toString()
            val isLatestResponseDisplayed = messageRvModelArrayList.any { it.message == latestResponse }
            if (!isLatestResponseDisplayed) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(4000)
                    messageRvModelArrayList.add(
                        MessageRvModel(latestResponse, null, "bot", null, latestResponse, latestResponse)
                    )
                    messageRvAdapter.notifyDataSetChanged()
                    if (messageRvAdapter.itemCount > 0) {
                        binding?.recyclerView?.smoothScrollToPosition(messageRvAdapter.itemCount - 1)
                    }
                }
            }
            binding?.bgText?.visibility = View.GONE
            binding?.bgLogo?.visibility = View.GONE
            binding?.recyclerView?.visibility = View.VISIBLE
        }else{
            binding?.bgText?.visibility = View.VISIBLE
            binding?.bgLogo?.visibility = View.VISIBLE
            binding?.recyclerView?.visibility = View.GONE
        }
    }

    private fun observeKeyboard() {
        val rootView = binding?.root
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                if (messageRvAdapter.itemCount > 0) {
                    binding?.recyclerView?.smoothScrollToPosition(messageRvAdapter.itemCount - 1)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    private fun moveToProfileFragment(){
        binding?.materialToolbar?.menuBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }
}