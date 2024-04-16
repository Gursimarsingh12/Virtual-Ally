package com.chatbot.geminiai.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.chatbot.geminiai.R
import com.chatbot.geminiai.databinding.RvItemBotBinding
import com.chatbot.geminiai.databinding.RvItemUserBinding
import com.chatbot.geminiai.utils.MessageRvModel
import com.chatbot.geminiai.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject

class MessageRvAdapter(private val messageList: ArrayList<MessageRvModel>, private val chatViewModel: ChatViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val auth: FirebaseAuth = Firebase.auth
    var prompt: String ?= null

    class UserMessageViewHolder(var binding: RvItemUserBinding): RecyclerView.ViewHolder(binding.root)
    class BotMessageViewHolder(var binding: RvItemBotBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0){
            val binding = RvItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            UserMessageViewHolder(binding)
        } else {
            val binding = RvItemBotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            BotMessageViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sender = messageList[position].sender
        when(sender){
            "user" -> {
                (holder as UserMessageViewHolder).apply {
                    prompt = messageList[position].message
                    binding.userResponse.text = messageList[position].message
                    if(messageList[position].photoUrl != null && messageList[position].photoUrl!!.isNotEmpty()){
                       Picasso.get().load(auth.currentUser?.photoUrl).transform(CropCircleTransformation()).into(binding.userImg)
                    }else{
                        binding.userImg.setImageResource(R.drawable.user_pfp)
                    }
                    binding.userCopyBtn.setOnClickListener {
                        copyToClipboard(it.context, "message", messageList[position].message)
                    }
                }
            }
            "bot" -> {
                (holder as BotMessageViewHolder).apply {
                    binding.botResponse.text = messageList[position].message
                    binding.copyBtn.setOnClickListener {
                        if(messageList[position].message.isNotEmpty()){
                            copyToClipboard(it.context, "message", messageList[position].message)
                        }
                    }
                    binding.shareBtn.setOnClickListener {
                        if(messageList[position].messageShare != null && messageList[position].message.isNotEmpty()){
                            shareMessage(it.context, messageList[position].message)
                        }
                    }
                    binding.regenerateBtn.setOnClickListener {
                        if(messageList[position].messageShare != null && messageList[position].message.isNotEmpty()){
                            chatViewModel.regenerateResponseAndStore(prompt!!)
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(messageList[position].sender){
            "user" -> 0
            "bot" -> 1
            else -> -1
        }
    }
    private fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    private fun shareMessage(context: Context, message: String){
        val intent = Intent().apply{
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Share message via"))
    }

    private fun regenerateResponseAndStore(prompt: String){
        chatViewModel.regenerateResponseAndStore(prompt)
    }
}