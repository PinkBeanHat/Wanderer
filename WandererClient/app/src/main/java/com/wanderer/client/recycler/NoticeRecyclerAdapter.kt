package com.wanderer.client.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.NoticeInfo
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ListNoticeBinding

class NoticeRecyclerAdapter(data: ArrayList<NoticeInfo>):
    RecyclerView.Adapter<NoticeRecyclerAdapter.NoticeViewHolder>(){
    private val mData: ArrayList<NoticeInfo>

    private val wanderer = Wanderer.instance
    private lateinit var context: Context

    init {
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {

        context = parent.context
        // create a new view-

        return NoticeViewHolder(ListNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val mBinding = holder.mBinding
        bind(mData[position], mBinding)
    }

    private fun bind(item: NoticeInfo, mBinding: ListNoticeBinding) {
        mBinding.txtNoticeName.text = item.title
        mBinding.txtNoticeDate.text = item.date

        mBinding.root.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "502"
            map["num"] = item.num
            wanderer.send(map)
        }
    }

    override fun getItemCount(): Int = mData.size

    inner class NoticeViewHolder(val mBinding: ListNoticeBinding): RecyclerView.ViewHolder(mBinding.root) {
        init {

        }
    }
}