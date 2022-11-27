package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.PlayerInfo
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ListFriendReceiveBinding

class FriendReceiveRecyclerAdapter(data: ArrayList<PlayerInfo>):
    RecyclerView.Adapter<FriendReceiveRecyclerAdapter.FriendReceiveViewHolder>(){
    private val mData: ArrayList<PlayerInfo>
    private val wanderer = Wanderer.instance
    private lateinit var context: Context

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendReceiveViewHolder {

        context = parent.context
        // create a new view-

        return FriendReceiveViewHolder(ListFriendReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FriendReceiveViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: PlayerInfo, mBinding: ListFriendReceiveBinding) {

        mBinding.txtPlayerName.text = item.name

        mBinding.btnAccept.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "804"
            map["name"] = item.name
            wanderer.send(map)
        }

        mBinding.btnRefuse.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "805"
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

    inner class FriendReceiveViewHolder(val mBinding: ListFriendReceiveBinding): RecyclerView.ViewHolder(mBinding.root)
}