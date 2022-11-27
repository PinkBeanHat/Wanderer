package com.wanderer.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.databinding.FragmentListBinding
import com.wanderer.client.recycler.FriendSendRecyclerAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendSendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendSendFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBinding: FragmentListBinding
    private val mList = ArrayList<PlayerInfo>()
    private val mAdapter = FriendSendRecyclerAdapter(mList)
    private val wanderer = Wanderer.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentListBinding.inflate(inflater, container, false)
        setAdapter()

        mBinding.viewList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val last = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                if(recyclerView.adapter!!.itemCount - 1 == last) {
                    val map = HashMap<String, String>()
                    map["what"] = "811"
                    wanderer.send(map)
                }
            }
        })

        return mBinding.root
    }

    private fun setAdapter() {
        mBinding.viewList.adapter = mAdapter
        mBinding.viewList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    fun addList(list : ArrayList<PlayerInfo>) {
        val start = mList.size
        mList += list
        mAdapter.notifyItemRangeInserted(start, mList.size)
    }

    fun clearList() {
        mList.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun setList() {
        val map = HashMap<String, String>()
        map["what"] = "806"
        wanderer.send(map)
    }

    override fun onStart() {
        super.onStart()
        setList()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Friend2Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendSendFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}