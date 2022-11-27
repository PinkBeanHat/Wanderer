package com.wanderer.client.activitiy

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderer.client.NoticeInfo
import com.wanderer.client.Wanderer
import com.wanderer.client.databinding.ActivitiyNoticeBinding
import com.wanderer.client.databinding.DialNoticeBinding
import com.wanderer.client.recycler.NoticeRecyclerAdapter
import org.json.JSONException
import org.json.JSONObject

class NoticeActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivitiyNoticeBinding
    private val wanderer = Wanderer.instance
    private val mList = ArrayList<NoticeInfo>()
    private val mAdapter = NoticeRecyclerAdapter(mList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitiyNoticeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setAdapter()

        mBinding.btnX.setOnClickListener {
            finish()
        }
        mBinding.viewNotice.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val last = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                if(recyclerView.adapter!!.itemCount - 1 == last) {
                    val map = HashMap<String, String>()
                    map["what"] = "506"
                    wanderer.send(map)
                }
            }
        })
    }

    private fun setAdapter() {
        mBinding.viewNotice.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        mBinding.viewNotice.adapter = mAdapter
    }

    private fun setList() {
        val map = HashMap<String, String>()
        map["what"] = "501"
        wanderer.send(map)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onStart() {
        super.onStart()
        wanderer.setHandler(NoticeHandler())
        setList()
    }

    inner class NoticeHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {
                    501 -> {
                        mList.clear()
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val arr = receive.getJSONArray("notice")
                            for (i in 0 until arr.length()) {
                                val data = arr.getJSONObject(i)
                                val num = data.getString("num")
                                val title = data.getString("title")
                                val date = data.getString("date")
                                mList.add(NoticeInfo(num, title, date))
                            }
                            mAdapter.notifyDataSetChanged()
                        }else {
                            Toast.makeText(applicationContext, "공지사항을 불러오는데 실패 했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    502 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val title = receive.getString("title")
                            val date = receive.getString("date")
                            val writer = receive.getString("writer")
                            val body = receive.getString("body")
                            showNoticeDial(title, date, writer, body)
                        }else {
                            Toast.makeText(applicationContext, "공지사항 세부 내용을 불러오는데 실패 했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    506 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val arr = receive.getJSONArray("notice")
                            val start = mList.size
                            for (i in 0 until arr.length()) {
                                val data = arr.getJSONObject(i)
                                val num = data.getString("num")
                                val title = data.getString("title")
                                val date = data.getString("date")
                                mList.add(NoticeInfo(num, title, date))
                            }
                            mAdapter.notifyItemRangeInserted(start, mList.size)
                        }else {
                            Toast.makeText(applicationContext, "공지사항을 불러오는데 실패 했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun showNoticeDial(title: String, date: String, writer: String, body: String) {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialNoticeBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.window!!.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        dial.show()
        mBinding.txtNoticeName.text = title
        mBinding.txtNoticeDate.text = date
        mBinding.txtNoticeWriter.text = writer
        mBinding.txtNoticeBody.text = body

        mBinding.btnBack.setOnClickListener {
            dial.dismiss()
        }

        mBinding.btnX.setOnClickListener {
            finish()
        }
    }

}