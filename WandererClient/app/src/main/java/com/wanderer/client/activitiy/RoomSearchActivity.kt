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
import androidx.recyclerview.widget.GridLayoutManager
import com.wanderer.client.PlayerInfo
import com.wanderer.client.RoomInfo
import com.wanderer.client.User
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ActivityRoomSearchBinding
import com.wanderer.client.databinding.DialAddRoomBinding
import com.wanderer.client.databinding.DialEnterRoomBinding
import com.wanderer.client.recycler.Room
import com.wanderer.client.recycler.RoomSearchRecyclerAdapter
import org.json.JSONException
import org.json.JSONObject

class RoomSearchActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivityRoomSearchBinding
    private val mList = ArrayList<Room>()
    private val mAdapter = RoomSearchRecyclerAdapter(mList)

    private val wanderer: Wanderer = Wanderer.instance
    private lateinit var user: User
    private var game = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRoomSearchBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setAdapter()

        user = intent.getSerializableExtra("user") as User
        game = intent.getIntExtra("rank", 0)

        mBinding.btnAddRoom.setOnClickListener {
            showAddRoomDial()
        }
        mBinding.btnBack.setOnClickListener {
            finish()
        }
        mBinding.btnRefresh.setOnClickListener {
            setList()
        }
    }

    private fun setList() {
        val map = HashMap<String, String>()
        map["what"] = "301"
        map["rank"] = game.toString()
        wanderer.send(map)
    }

    private fun setAdapter() {
        mBinding.viewRoom.adapter = mAdapter
        mBinding.viewRoom.layoutManager = GridLayoutManager(applicationContext, 2)
    }

    private fun showAddRoomDial() {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialAddRoomBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()
        mBinding.editEnterRoomPw.visibility = View.INVISIBLE
        mBinding.chkRoomLock.setOnCheckedChangeListener {view, isChecked ->
            if(isChecked) {
                mBinding.editEnterRoomPw.visibility = View.VISIBLE
            }else {
                mBinding.editEnterRoomPw.visibility = View.INVISIBLE
                mBinding.editEnterRoomPw.setText("")
            }
        }

        mBinding.btnEnterRoomYes.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "302"
            map["name"] = mBinding.editRoomName.text.toString()
            map["roomPW"] = mBinding.editEnterRoomPw.text.toString()
            map["max"] = mBinding.txtRoomMax.text.toString()
            wanderer.send(map)
            dial.dismiss()
        }
        mBinding.btnEnterRoomNo.setOnClickListener {
            dial.dismiss()
        }
    }

    private fun showEnterRoomDial(num: String, pw: String) {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialEnterRoomBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()

        mBinding.btnERoomYes.setOnClickListener {
            if(mBinding.editERoomPw.text.toString() == pw) {
                val map = HashMap<String, String>()
                map["what"] = "303"
                map["num"] = num
                wanderer.send(map)
                dial.dismiss()
            }else {
                Toast.makeText(applicationContext, "비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
            }
        }
        mBinding.btnERoomNo.setOnClickListener {
            dial.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        wanderer.setHandler(RoomSearchHandler())
        setList()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    inner class RoomSearchHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {
                    301 -> {
                        val arr = receive.getJSONArray("detail")
                        mList.clear()

                        for(i in 0 until arr.length()) {
                            val data = arr.getJSONObject(i)
                            val num = data.getString("num").toInt()
                            val cur = data.getString("cur").toInt()
                            val max = data.getString("max").toInt()
                            val name = data.getString("name")
                            val pw = data.getString("pw") == "1"
                            mList.add(Room(num = num, curP = cur, maxP = max, name = name, pw = pw))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                    302 -> {
                        showAddRoomDial()
                        Toast.makeText(applicationContext, "생성 중 오류가 발생했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                    }

                    303 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val arr = receive.getJSONArray("player")
                            val list = ArrayList<PlayerInfo>()
                            val name = receive.getString("name")
                            for(i in 0 until arr.length()) {
                                val data = arr.getJSONObject(i)
                                list.add(PlayerInfo(data.getString("name").toString()))
                            }

                            val intent = Intent(applicationContext, RoomActivity::class.java)
                            intent.putExtra("user", user)
                            intent.putExtra("roomInfo", RoomInfo(name, list))
                            startActivity(intent)
                        } else {
                            Toast.makeText(applicationContext, "입장에 실패 했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    309 -> {
                        val pw = receive.getString("pw")
                        val num = receive.getString("num")
                        showEnterRoomDial(num, pw)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}