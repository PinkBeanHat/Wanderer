package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.UserRank
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ListRankBinding

class RankingRecyclerAdapter(data: ArrayList<UserRank>):
    RecyclerView.Adapter<RankingRecyclerAdapter.RankingViewHolder>(){
    private val mData: ArrayList<UserRank>
    private val wanderer = Wanderer.instance
    private lateinit var context: Context

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {

        context = parent.context
        // create a new view-

        return RankingViewHolder(ListRankBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: UserRank, mBinding: ListRankBinding) {
        mBinding.txtRank.text = "${item.rank}위"
        mBinding.imgRank.setImageResource(item.img)
        mBinding.txtPlayerName.text = item.name
        mBinding.txtRate.text = "${item.rate} 점"

        mBinding.root.setOnClickListener {

            val map = HashMap<String, String>()
            map["what"] = "311"
            map["name"] = item.name
            wanderer.send(map)
        }
    }

    override fun getItemCount(): Int = mData.size

    inner class RankingViewHolder(val mBinding: ListRankBinding): RecyclerView.ViewHolder(mBinding.root)
}