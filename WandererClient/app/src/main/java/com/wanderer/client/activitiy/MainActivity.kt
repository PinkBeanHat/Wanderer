package com.wanderer.client.activitiy

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wanderer.client.R
import com.wanderer.client.User
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ActivityMainBinding
import com.wanderer.client.databinding.DialInfoBinding
import com.wanderer.client.databinding.DialSettingBinding
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityMainBinding

    val wanderer: Wanderer = Wanderer.instance
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val activitys = arrayOf(
            Intent(this, FriendActivity :: class.java),
            Intent(this, RoomSearchActivity :: class.java),
            Intent(this, RoomSearchActivity :: class.java),
            Intent(this, NoticeActivity :: class.java),
            Intent(this, RankingActivity :: class.java),
        )
        activitys[1].putExtra("rank", 0)
        activitys[2].putExtra("rank", 1)

        val imbs = arrayOf(
            mBinding.imbFriend,
            mBinding.imbFriendlyGame,
            mBinding.imbRankGame,
            mBinding.imbNotice,
            mBinding.imbRanking,
        )

        for(i in activitys.indices) {
            imbs[i].setOnClickListener {
                activitys[i].addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                activitys[i].putExtra("user", user)
                startActivity(activitys[i])
            }
        }

        mBinding.imbInfo.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "201"
            wanderer.send(map)
        }

        mBinding.imbSetting.setOnClickListener {
            showSettingDial()
        }
    }

    private fun setUser(context: Context) {
        mBinding.txtName.text = ""
        mBinding.imgProfile.setImageResource(R.drawable.img_profile_c)
        mBinding.txtMoney.text = ""

        if(wanderer.isUser(context)) {
            val user = wanderer.getUser(context)
            val map = HashMap<String, String>()
            map["what"] = "203"
            map["name"] = user.name
            wanderer.send(map)
        }else {
            val intent = Intent(this, TitleActivity :: class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // 초기 핸들러 설정
        Thread {
            while (true) {
                try {
                    Thread.sleep(1)
                    if (wanderer.isConnected()) {
                        wanderer.setHandler(MainHandler())
                        setUser(applicationContext)
                        break
                    }
                } catch (ignored: Exception) {
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    inner class MainHandler: Handler(Looper.getMainLooper()) {
        // 아이디 확인 되면 메인 액티비티로
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {
                    101 -> {
                        val isUser = receive.getString("isUser").toInt()
                        if (isUser == 1) {
                            val name = receive.getString("name")
                            val money = receive.getString("money").toInt()
                            val user = User(name, money)
                            wanderer.updateUser(applicationContext, user)
                            mBinding.txtName.text = user.name
                            mBinding.imgProfile.setImageResource(R.drawable.img_profile_c)
                            mBinding.txtMoney.text = user.money.toString()
                        } else {
                            val intent = Intent(applicationContext, TitleActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intent)
                        }
                    }

                    102 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            wanderer.deleteUser(applicationContext)
                            setUser(applicationContext)
                            Toast.makeText(applicationContext, "회원 탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()
                        }else {
                            showSettingDial()
                        }
                    }

                    103 -> {
                        val isUser = receive.getString("isUser")
                        if (isUser == "1") {
                            val name = receive.getString("name")
                            val money = receive.getString("money").toInt()
                            user = User(name, money)
                            if (wanderer.isUser(applicationContext)) {
                                wanderer.updateUser(applicationContext, user)
                            } else {
                                wanderer.addUser(applicationContext, user)
                            }
                            mBinding.txtName.text = name
                            mBinding.txtMoney.text = money.toString()
                        }
                    }

                    104 -> {
                        wanderer.deleteUser(applicationContext)
                        setUser(applicationContext)
                        Toast.makeText(applicationContext, "로그 아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    }

                    201 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val name = receive.getString("name")
                            val body = receive.getString("body")
                            val rating = receive.getString("rating")
                            showInfoDial(name, body, rating)
                        } else {
                            Toast.makeText(applicationContext, "로그인 정보를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    202 -> {
                        Toast.makeText(applicationContext, "정보가 수정 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun showSettingDial() {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialSettingBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()

        mBinding.btnQuit.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "102"
            wanderer.send(map)
            dial.dismiss()
        }

        mBinding.btnLogOut.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "104"
            wanderer.send(map)
            dial.dismiss()
        }
    }

    private fun showInfoDial(name: String, body: String, rating: String) {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialInfoBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()

        mBinding.imgPlayer.setImageResource(R.drawable.img_profile_c)
        mBinding.txtPlayer.text = name

        val rate = "랭킹전 ${rating}점"
        mBinding.txtPlayerRate.text = rate
        mBinding.editInfo.setText(body)

        mBinding.txtInfoCnt.text = "${body.length} / 30"

        mBinding.editInfo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val cnt = p0.toString().length
                if(cnt > 30) {
                    mBinding.txtInfoCnt.setTextColor(resources.getColor(R.color.red))
                }else {
                    mBinding.txtInfoCnt.setTextColor(resources.getColor(R.color.black))
                }
                mBinding.txtInfoCnt.text = "${cnt} / 30"
            }

        })

        mBinding.btnX.setOnClickListener {
            dial.dismiss()
        }

        mBinding.btnChange.setOnClickListener {
            val changed = mBinding.editInfo.text.toString()
            if(changed.length <= 30) {
                val map = HashMap<String, String>()
                map["what"] = "202"
                map["body"] = changed
                wanderer.send(map)
                dial.dismiss()
            }else {
                Toast.makeText(applicationContext, "글자가 너무 깁니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}