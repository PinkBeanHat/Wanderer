package com.wanderer.client.activitiy

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.wanderer.client.*
import com.wanderer.client.databinding.ActivityFriendBinding
import com.wanderer.client.databinding.DialFriendBinding
import com.wanderer.client.databinding.DialFriendListBinding
import com.wanderer.client.databinding.DialInfoBinding
import com.wanderer.client.recycler.PlayerRecyclerAdapter
import org.json.JSONObject
import kotlin.collections.set

class FriendActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivityFriendBinding
    private val wanderer = Wanderer.instance
    private val mList = ArrayList<PlayerInfo>()
    private val mAdapter = PlayerRecyclerAdapter(mList)

    private lateinit var user: User
    private lateinit var fBinding: DialFriendListBinding
    private lateinit var fAdapter: FriendViewAdapter

    private lateinit var receiveFr: FriendReceiveFragment
    private lateinit var sendFr: FriendSendFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        user = intent.getSerializableExtra("user") as User

        setAdapter()

        mBinding.viewFriend.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val last = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                if(recyclerView.adapter!!.itemCount - 1 == last) {
                    val map = HashMap<String, String>()
                    map["what"] = "809"
                    wanderer.send(map)
                }
            }
        })

        mBinding.btnListFriend.setOnClickListener {
            showFriendListDial()
        }

        mBinding.btnFindFriend.setOnClickListener {
            showFriendDial()
        }

        mBinding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setAdapter() {
        mBinding.viewFriend.adapter = mAdapter
        mBinding.viewFriend.layoutManager = GridLayoutManager(applicationContext, 2)
    }

    private fun setFriendList() {
        fBinding = DialFriendListBinding.inflate(this.layoutInflater)
        fAdapter = FriendViewAdapter(this)
        fBinding.pagerFriend.adapter = fAdapter
        receiveFr = fAdapter.createFragment(0) as FriendReceiveFragment
        sendFr = fAdapter.createFragment(1) as FriendSendFragment

        val list = arrayOf("친구 신청 목록", "친구 요청 목록")

        TabLayoutMediator(fBinding.tabFriend, fBinding.pagerFriend) { tab, po ->
            tab.text = list[po]
        }.attach()
    }


    private fun showFriendListDial() {
        setFriendList()
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dial.setContentView(fBinding.root)
        dial.show()

        dial.setOnDismissListener {
            fBinding.root.removeAllViews()
        }
    }

    private fun setList() {
        val map = HashMap<String, String>()
        map["what"] = "801"
        wanderer.send(map)
    }

    override fun onStart() {
        super.onStart()
        wanderer.setHandler(FriendHandler())
        setList()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    private fun showFriendDial() {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialFriendBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()

        mBinding.btnX.setOnClickListener {
            dial.dismiss()
        }

        mBinding.btnSearch.setOnClickListener {

            var validate = true
            val name = mBinding.editSearch.text.toString()

            if (name.length < 2) {
                validate = false
                Toast.makeText(this, "이름이 너무 짧습니다.", Toast.LENGTH_SHORT).show()
            }
            if (name.length > 15) {
                validate = false
                Toast.makeText(this, "이름이 너무 깁니다.", Toast.LENGTH_SHORT).show()
            }

            if(validate) {
                val map = HashMap<String, String>()
                map["what"] = "812"
                map["name"] = name
                wanderer.send(map)
                dial.dismiss()
            }
        }
    }

    private fun showInfoDial(name: String, body: String, isFriend: Int, rating: String) {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialInfoBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()

        if(name == user.name) {
            mBinding.btnChange.visibility = View.INVISIBLE
        }

        mBinding.imgPlayer.setImageResource(R.drawable.img_profile_c)
        mBinding.txtPlayer.text = name

        val rate = "랭킹전 ${rating}점"
        mBinding.txtPlayerRate.text = rate
        mBinding.editInfo.setText(body)

        mBinding.txtInfoCnt.text = "${body.length} / 30"
        mBinding.editInfo.isEnabled = false

        mBinding.btnX.setOnClickListener {
            dial.dismiss()
        }

        when(isFriend) {
            0 -> {
                mBinding.btnChange.background = applicationContext.getDrawable(R.drawable.imb_add_friend)
                mBinding.btnChange.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["what"] = "807"
                    map["name"] = name
                    wanderer.send(map)
                    dial.dismiss()
                }
            }

            1 -> {
                mBinding.btnChange.background = applicationContext.getDrawable(R.drawable.imb_rm_friend)
                mBinding.btnChange.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["what"] = "802"
                    map["name"] = name
                    wanderer.send(map)
                    dial.dismiss()
                }
            }

            2 -> {
                mBinding.btnChange.background = applicationContext.getDrawable(R.drawable.imb_cancel)
                mBinding.btnChange.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["what"] = "808"
                    map["name"] = name
                    wanderer.send(map)
                    dial.dismiss()
                }
            }
        }
    }

    inner class FriendHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {
                    801 -> {
                        mList.clear()
                        mAdapter.notifyDataSetChanged()
                        val arr = receive.getJSONArray("friend")
                        val start = mList.size
                        for(i in 0 until arr.length()) {
                            mList.add(PlayerInfo(arr.getJSONObject(i).getString("name")))
                        }
                        mAdapter.notifyItemRangeInserted(start, mList.size)
                    }

                    809 -> {
                        val arr = receive.getJSONArray("friend")
                        val start = mList.size
                        for(i in 0 until arr.length()) {
                            mList.add(PlayerInfo(arr.getJSONObject(i).getString("name")))
                        }
                        mAdapter.notifyItemRangeInserted(start, mList.size)
                    }

                    803 -> {
                        receiveFr.clearList()
                        val arr = receive.getJSONArray("request")
                        val list = ArrayList<PlayerInfo>()
                        for(i in 0 until arr.length()) {
                            list.add(PlayerInfo(arr.getJSONObject(i).getString("name")))
                        }
                        receiveFr.addList(list)
                    }

                    804 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            Toast.makeText(applicationContext, "친구 신청을 수락 하였습니다.", Toast.LENGTH_SHORT).show()
                            setList()
                        }else {
                            Toast.makeText(applicationContext, "예상치 못한 오류가 발생 했습니다. 다시 시도 해 주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    805 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            Toast.makeText(applicationContext, "친구 신청을 거절 하였습니다.", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(applicationContext, "예상치 못한 오류가 발생 했습니다. 다시 시도 해 주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    807 -> {
                        val isValidate = receive.getString("isValidate")
                        when(isValidate[0]) {
                            '0' -> {
                                Toast.makeText(applicationContext, "친구 요청을 실패 했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            '1' -> {
                                Toast.makeText(applicationContext, "친구 요청을 성공 했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            '2' -> {
                                Toast.makeText(applicationContext, "친구 요청을 이미 보냈습니다.", Toast.LENGTH_SHORT).show()
                            }
                            '3' -> {
                                Toast.makeText(applicationContext, "친구로 등록 되었습니다.", Toast.LENGTH_SHORT).show()
                                setList()
                            }
                        }
                    }

                    810 -> {
                        val arr = receive.getJSONArray("request")
                        val list = ArrayList<PlayerInfo>()
                        for(i in 0 until arr.length()) {
                            list.add(PlayerInfo(arr.getJSONObject(i).getString("name")))
                        }
                        receiveFr.addList(list)
                    }
                    
                    806 -> {
                        sendFr.clearList()
                        val arr = receive.getJSONArray("request")
                        val list = ArrayList<PlayerInfo>()
                        for(i in 0 until arr.length()) {
                            list.add(PlayerInfo(arr.getJSONObject(i).getString("name")))
                        }
                        sendFr.addList(list)
                    }
                    
                    808 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            Toast.makeText(applicationContext, "친구 요청을 취소 하였습니다.", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(applicationContext, "예상치 못한 오류가 발생 했습니다. 다시 시도 해 주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    811 -> {
                        val arr = receive.getJSONArray("request")
                        val list = ArrayList<PlayerInfo>()
                        for(i in 0 until arr.length()) {
                            list.add(PlayerInfo(arr.getJSONObject(i).getString("name")))
                        }
                        sendFr.addList(list)
                    }

                    311 -> {
                        val name = receive.getString("name")
                        val body = receive.getString("body")
                        val rating = receive.getString("rating")
                        val isFriend = receive.getString("isFriend").toInt()

                        if(name == "") {
                            Toast.makeText(applicationContext, "해당 유저를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
                            showFriendDial()
                        }else {
                            showInfoDial(name, body, isFriend, rating)
                            val isValidate = receive.getString("isValidate") == "1"
                            if(isValidate) {
                                Toast.makeText(applicationContext, "친구 삭제를 성공 했습니다.", Toast.LENGTH_SHORT).show()
                                mList.clear()
                                mAdapter.notifyDataSetChanged()
                            }else {
                                Toast.makeText(applicationContext, "친구 삭제를 실패 했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}