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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.R
import com.wanderer.client.User
import com.wanderer.client.UserRank
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ActivitiyRankingBinding
import com.wanderer.client.databinding.DialInfoBinding
import com.wanderer.client.recycler.RankingRecyclerAdapter
import org.json.JSONObject

class RankingActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivitiyRankingBinding

    private val wanderer: Wanderer = Wanderer.instance

    private val mList = ArrayList<UserRank>()
    private val mAdapter = RankingRecyclerAdapter(mList)

    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitiyRankingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        user = intent.getSerializableExtra("user") as User

        setAdapter()

        mBinding.btnBack.setOnClickListener {
            finish()
        }

        mBinding.viewRank.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val last = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                if(recyclerView.adapter!!.itemCount - 1 == last) {
                    val map = HashMap<String, String>()
                    map["what"] = "211"
                    wanderer.send(map)
                }
            }
        })
    }

    private fun setAdapter() {
        mBinding.viewRank.adapter = mAdapter
        mBinding.viewRank.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
    }

    inner class RankingHandler: Handler(Looper.getMainLooper()) {
        // 아이디 확인 되면 메인 액티비티로
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {
                    210 -> {
                        mList.clear()
                        val info = receive.getJSONArray("info").getJSONObject(0)
                        val name = info.getString("name")
                        val rank = info.getString("rank")
                        val rate = info.getString("rating")
                        mBinding.txtName.text = name
                        mBinding.txtRank.text = "${rank} 위"
                        mBinding.txtRate.text = "${rate} 점"

                        val arr = receive.getJSONArray("ranking")
                        val imgs = arrayOf(R.drawable.img_result1, R.drawable.img_result2, R.drawable.img_result3)

                        for(i in 0 .. 2) {
                            val data = arr.getJSONObject(i)
                            mList.add(UserRank(name = data.getString("name"), rate = data.getString("rating").toInt(), rank = data.getString("rank").toInt(), img = imgs[i]))
                        }

                        for(i in 3 until arr.length()) {
                            val data = arr.getJSONObject(i)
                            mList.add(UserRank(name = data.getString("name"), rate = data.getString("rating").toInt(), rank = data.getString("rank").toInt(), img = 0))
                        }
                        mAdapter.notifyDataSetChanged()
                    }

                    211 -> {
                        val start = mList.size
                        val arr = receive.getJSONArray("ranking")

                        for(i in 0 until arr.length()) {
                            val data = arr.getJSONObject(i)
                            mList.add(UserRank(name = data.getString("name"), rate = data.getString("rating").toInt(), rank = data.getString("rank").toInt(), img = 0))
                        }
                        mAdapter.notifyItemRangeInserted(start, mList.size)
                    }

                    311 -> {
                        val name = receive.getString("name")
                        val body = receive.getString("body")
                        val rating = receive.getString("rating")
                        val isFriend = receive.getString("isFriend").toInt()

                        showInfoDial(name, body, isFriend, rating)
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    override fun onStart() {
        super.onStart()
        wanderer.setHandler(RankingHandler())

        val map = HashMap<String, String>()
        map["what"] = "210"
        wanderer.send(map)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }
}