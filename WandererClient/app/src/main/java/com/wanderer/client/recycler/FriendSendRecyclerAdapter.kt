package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.PlayerInfo
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ListFriendSendBinding

class FriendSendRecyclerAdapter(data: ArrayList<PlayerInfo>):
    RecyclerView.Adapter<FriendSendRecyclerAdapter.FriendSendViewHolder>(){
    private val mData: ArrayList<PlayerInfo>
    private val wanderer = Wanderer.instance
    private lateinit var context: Context

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendSendViewHolder {

        context = parent.context
        // create a new view-

        return FriendSendViewHolder(ListFriendSendBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FriendSendViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: PlayerInfo, mBinding: ListFriendSendBinding) {
        mBinding.txtPlayerName.text = item.name

        mBinding.btnX.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "808"
            map["name"] = item.name
            wanderer.send(map)
        }

        mBinding.root.setOnClickListener {

            val map = HashMap<String, String>()
            map["what"] = "311"
            map["name"] = item.name
            wanderer.send(map)
        }
    }

    override fun getItemCount(): Int = mData.size

    inner class FriendSendViewHolder(val mBinding: ListFriendSendBinding): RecyclerView.ViewHolder(mBinding.root)
}