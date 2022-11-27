package com.wanderer.client.activitiy

import android.app.Dialog
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.*
import com.wanderer.client.databinding.ActivityRoomBinding
import com.wanderer.client.databinding.DialAddRoomBinding
import com.wanderer.client.databinding.DialPlayerInfoBinding
import com.wanderer.client.recycler.ChatRecyclerAdapter
import com.wanderer.client.recycler.PlayerRecyclerAdapter
import org.json.JSONException
import org.json.JSONObject

class RoomActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivityRoomBinding
    private val mList = ArrayList<PlayerInfo>()
    private val chatList = ArrayList<String>()
    private val mAdapter = PlayerRecyclerAdapter(mList)
    private val chatAdapter = ChatRecyclerAdapter(chatList)

    private val wanderer: Wanderer = Wanderer.instance
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setAdapter()

        user = intent.getSerializableExtra("user") as User

        val roomInfo = intent.getSerializableExtra("roomInfo") as RoomInfo
        setRoomInfo(roomInfo)

        if(user.name == mList[0].name) {
            mBinding.btnStart.visibility = View.VISIBLE
            mBinding.btnRoomSetting.visibility = View.VISIBLE
        }else {
            mBinding.btnStart.visibility = View.INVISIBLE
            mBinding.btnRoomSetting.visibility = View.INVISIBLE
        }

        mBinding.btnRoomChat.setOnClickListener {
            // 욕설 필터링 추가 해야 함
            val chat = mBinding.editRoomChat.text.toString()
            var validate = true

            if(chat.isEmpty()) {
                Toast.makeText(applicationContext, "채팅을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                validate = false
            }

            if(chat.length >= 30) {
                Toast.makeText(applicationContext, "채팅이 너무 깁니다.", Toast.LENGTH_SHORT).show()
                validate = false
            }

            if(validate) {
                val map = HashMap<String, String>()
                map["what"] = "601"
                map["chat"] = chat
                mBinding.editRoomChat.setText("")
                wanderer.send(map)
            }
        }
        mBinding.btnBack.setOnClickListener {
            quitRoom()
        }

        mBinding.btnStart.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "701"
            wanderer.send(map)
        }

        mBinding.btnRoomSetting.setOnClickListener {
            showAlterRoomDial()
        }

        wanderer.setHandler(RoomHandler())
    }

    override fun onBackPressed() {
        quitRoom()
    }

    private fun quitRoom() {
        val map = HashMap<String, String>()
        map["what"] = "304"
        wanderer.send(map)
    }

    private fun setRoomInfo(roomInfo: RoomInfo) {
        mBinding.txtRoomName.text = roomInfo.name
        mList.clear()
        mList.addAll(roomInfo.playerInfo)
        mAdapter.notifyDataSetChanged()
    }

    private fun setAdapter() {
        mBinding.viewRoomUser.adapter = mAdapter
        mBinding.viewRoomUser.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)

        mBinding.viewChat.adapter = chatAdapter
        mBinding.viewChat.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
    }


    private fun showPlayerInfoDial(name: String, isFriend: Int, rating: String) {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialPlayerInfoBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        mBinding.txtPlayerName.text = name
        mBinding.txtPlayerRate.text = rating

        mBinding.btnAddFriend.visibility = View.VISIBLE
        mBinding.btnKick.visibility = View.VISIBLE

        if(user.name != mList[0].name) {
            mBinding.btnKick.visibility = View.INVISIBLE
        }

        if(user.name == name) {
            mBinding.btnAddFriend.visibility = View.INVISIBLE
            mBinding.btnKick.visibility = View.INVISIBLE
        }

        when(isFriend) {
            0 -> {
                mBinding.btnAddFriend.background = applicationContext.getDrawable(R.drawable.imb_add_friend)
                mBinding.btnAddFriend.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["what"] = "807"
                    map["name"] = name
                    wanderer.send(map)
                    dial.dismiss()
                }
            }

            1 -> {
                mBinding.btnAddFriend.background = applicationContext.getDrawable(R.drawable.imb_rm_friend)
                mBinding.btnAddFriend.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["what"] = "802"
                    map["name"] = name
                    wanderer.send(map)
                    dial.dismiss()
                }
            }

            2 -> {
                mBinding.btnAddFriend.background = applicationContext.getDrawable(R.drawable.imb_cancel)
                mBinding.btnAddFriend.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["what"] = "808"
                    map["name"] = name
                    wanderer.send(map)
                    dial.dismiss()
                }
            }
        }

        mBinding.btnX.setOnClickListener {
            dial.dismiss()
        }

        mBinding.btnKick.setOnClickListener {
            Toast.makeText(applicationContext, "해당 플레이어를 추방 했습니다.", Toast.LENGTH_SHORT).show()
            val map = HashMap<String, String>()
            map["what"] = "312"
            map["name"] = name
            wanderer.send(map)
            dial.dismiss()
        }
        dial.show()
    }


    private fun showAlterRoomDial() {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialBinding = DialAddRoomBinding.inflate(this.layoutInflater)
        dial.setContentView(dialBinding.root)
        dial.show()
        dialBinding.editEnterRoomPw.visibility = View.INVISIBLE
        dialBinding.chkRoomLock.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                dialBinding.editEnterRoomPw.visibility = View.VISIBLE
            }else {
                dialBinding.editEnterRoomPw.visibility = View.INVISIBLE
                dialBinding.editEnterRoomPw.setText("")
            }
        }
        dialBinding.txtEnterRoom.text = "방 변경하기"
        dialBinding.editRoomName.setText(mBinding.txtRoomName.text)
        dialBinding.editRoomName.setSelection(dialBinding.editRoomName.length())
        dialBinding.btnEnterRoomYes.background = applicationContext.getDrawable(R.drawable.imb_alter)
        dialBinding.btnEnterRoomYes.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "302"
            map["name"] = dialBinding.editRoomName.text.toString()
            map["roomPW"] = dialBinding.editEnterRoomPw.text.toString()
            map["max"] = dialBinding.txtRoomMax.text.toString()
            wanderer.send(map)
            dial.dismiss()
        }
        dialBinding.btnEnterRoomNo.setOnClickListener {
            dial.dismiss()
        }
    }

    override fun onRestart() {
        super.onRestart()
        wanderer.setHandler(RoomHandler())
        val map = HashMap<String, String>()
        map["what"] = "305"
        wanderer.send(map)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    inner class RoomHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {
                    303 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val arr = receive.getJSONArray("player")
                            mList.clear()
                            val name = receive.getString("name")
                            mBinding.txtRoomName.text = name
                            for(i in 0 until arr.length()) {
                                val data = arr.getJSONObject(i)
                                mList.add(PlayerInfo(data.getString("name").toString()))
                            }
                            if(user.name == mList[0].name) {
                                mBinding.btnStart.visibility = View.VISIBLE
                                mBinding.btnRoomSetting.visibility = View.VISIBLE
                            }else {
                                mBinding.btnStart.visibility = View.INVISIBLE
                                mBinding.btnRoomSetting.visibility = View.INVISIBLE
                            }
                            mAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(applicationContext, "다시 입장해 주세요.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    304 -> {
                        finish()
                    }

                    311 -> {
                        val isFriend = receive.getString("isFriend").toInt()
                        val name = receive.getString("name")
                        val rating = receive.getString("rating")

                        showPlayerInfoDial(name, isFriend, rating)
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            Toast.makeText(applicationContext, "친구 삭제를 성공 했습니다.", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(applicationContext, "친구 삭제를 실패 했습니다.", Toast.LENGTH_SHORT).show()
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
                            }
                        }
                    }

                    808 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            Toast.makeText(applicationContext, "친구 요청을 취소 하였습니다.", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(applicationContext, "예상치 못한 오류가 발생 했습니다. 다시 시도 해 주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    312 -> {
                        val name = receive.getString("name")
                        if(user.name == name) {
                            Toast.makeText(applicationContext, "방에서 추방 당했습니다.", Toast.LENGTH_SHORT).show()
                            quitRoom()
                        }
                    }

                    601 -> {
                        val chat = receive.getString("name") + " : " +  receive.getString("chat")
                        val size = chatList.size
                        chatList.add(chat)
                        chatAdapter.notifyItemInserted(size)
                        mBinding.viewChat.scrollToPosition(size)
                    }

                    701 -> {
                        // 게임 시작
                        val intent = Intent(applicationContext, GameActivity :: class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        val playerList = Array(4) { mList[it].name }
                        intent.putExtra("user", user)
                        intent.putExtra("player", playerList)
                        startActivity(intent)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}