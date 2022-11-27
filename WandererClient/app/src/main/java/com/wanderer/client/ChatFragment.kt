package com.wanderer.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.databinding.FragmentChatBinding
import com.wanderer.client.recycler.ChatRecyclerAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBinding: FragmentChatBinding
    private val mList = ArrayList<String>()
    private val mAdapter = ChatRecyclerAdapter(mList)
    private val wanderer: Wanderer = Wanderer.instance

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
        mBinding = FragmentChatBinding.inflate(inflater, container, false)
        setAdapter()

        mBinding.btnChat.setOnClickListener {
            // 욕설 필터링 추가 해야 함
            val chat = mBinding.editChat.text.toString()
            var validate = true

            if(chat.isEmpty()) {
                Toast.makeText(context, "채팅을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                validate = false
            }

            if(chat.length >= 30) {
                Toast.makeText(context, "채팅이 너무 깁니다.", Toast.LENGTH_SHORT).show()
                validate = false
            }

            if(validate) {
                val map = HashMap<String, String>()
                map["what"] = "601"
                map["chat"] = chat
                mBinding.editChat.setText("")
                wanderer.send(map)
            }
        }

        return mBinding.root
    }

    private fun setAdapter() {
        mBinding.viewChat.adapter = mAdapter
        mBinding.viewChat.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    fun addList(s : String) {
        val start = mList.size
        mList += s
        mAdapter.notifyItemRangeInserted(start, mList.size)
        try {
            mBinding.viewChat.scrollToPosition(mList.size - 1)
        }catch (_: Exception) {
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}