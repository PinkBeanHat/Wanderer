package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.PlayerInfo
import com.wanderer.client.R
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ListPlayerBinding

class PlayerRecyclerAdapter(data: ArrayList<PlayerInfo>):
    RecyclerView.Adapter<PlayerRecyclerAdapter.PlayerViewHolder>(){
    private val mData: ArrayList<PlayerInfo>

    private val wanderer = Wanderer.instance

    private lateinit var context: Context

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {

        context = parent.context
        // create a new view-

        return PlayerViewHolder(ListPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: PlayerInfo, mBinding: ListPlayerBinding) {
        mBinding.imgPlayer.visibility = View.VISIBLE
        mBinding.txtPlayerName.visibility = View.VISIBLE
        mBinding.txtPlayerEmpty.visibility = View.VISIBLE

        if(item.name == "") {
            mBinding.imgPlayer.visibility = View.INVISIBLE
            mBinding.txtPlayerName.visibility = View.INVISIBLE
            mBinding.root.setOnClickListener {

            }
        }else {
            mBinding.txtPlayerEmpty.visibility = View.INVISIBLE
            mBinding.imgPlayer.setImageResource(R.drawable.img_profile_c)
            mBinding.txtPlayerName.text = item.name

            mBinding.root.setOnClickListener {

                val map = HashMap<String, String>()
                map["what"] = "311"
                map["name"] = item.name
                wanderer.send(map)
            }
        }
    }

    override fun getItemCount(): Int = mData.size

    inner class PlayerViewHolder(val mBinding: ListPlayerBinding): RecyclerView.ViewHolder(mBinding.root)
}