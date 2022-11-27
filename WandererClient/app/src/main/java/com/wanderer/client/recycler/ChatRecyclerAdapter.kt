package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.databinding.ListChatBinding

class ChatRecyclerAdapter(data: ArrayList<String>):
    RecyclerView.Adapter<ChatRecyclerAdapter.ChatViewHolder>(){
    private val mData: ArrayList<String>
    private lateinit var context: Context

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        context = parent.context
        // create a new view-

        return ChatViewHolder(ListChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: String, mBinding: ListChatBinding) {
        mBinding.txtChat.text = item
    }

    override fun getItemCount(): Int = mData.size

    inner class ChatViewHolder(val mBinding: ListChatBinding): RecyclerView.ViewHolder(mBinding.root)
}