package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.R
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ListRoomBinding

class RoomSearchRecyclerAdapter(data: ArrayList<Room>):
    RecyclerView.Adapter<RoomSearchRecyclerAdapter.RoomSearchViewHolder>(){
    private val mData: ArrayList<Room>
    private lateinit var context: Context
    private val wanderer = Wanderer.instance

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomSearchViewHolder {

        context = parent.context
        // create a new view-

        return RoomSearchViewHolder(ListRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RoomSearchViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: Room, mBinding: ListRoomBinding) {
        mBinding.imgRoomName.setImageResource(R.drawable.img_profile_c)
        mBinding.txtRoomName.text = item.name
        val txt = "${item.curP} / ${item.maxP}"
        mBinding.txtRoomPlayer.text = txt

        mBinding.root.setOnClickListener {
            val map = HashMap<String, String>()
            if(item.pw) {
                map["what"] = "309"
            }else {
                map["what"] = "303"
            }
            map["num"] = item.num.toString()
            wanderer.send(map)
        }
    }

    override fun getItemCount(): Int = mData.size

    inner class RoomSearchViewHolder(val mBinding: ListRoomBinding): RecyclerView.ViewHolder(mBinding.root)
}

data class Room(val num: Int, val name: String = "제목", val curP: Int = 1, val maxP: Int = 4, val pw: Boolean = false)