package com.wanderer.client.activitiy

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.wanderer.client.*
import com.wanderer.client.databinding.ActivitiyGameBinding
import com.wanderer.client.databinding.DialDeckBinding
import com.wanderer.client.databinding.DialResultBinding
import org.json.JSONException
import org.json.JSONObject

class GameActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivitiyGameBinding

    private val wanderer: Wanderer = Wanderer.instance

    private val deck = Array(6) { 0 }

    private lateinit var btns: Array<ImageButton>
    private lateinit var user: User

    private val cards = arrayOf(R.drawable.img_card1, R.drawable.img_card2, R.drawable.img_card3, R.drawable.img_card4, R.drawable.img_card5,
                            R.drawable.img_card6, R.drawable.img_card7, R.drawable.img_card8, R.drawable.img_card9, R.drawable.img_card10,
                            R.drawable.img_card11, R.drawable.img_card12, R.drawable.img_card13, R.drawable.img_card14, R.drawable.img_card15)

    private val player = HashMap<String, Int>()

    private val chatFr = ChatFragment()
    private val logFr = LogFragment()

    private var cardPicked = false
    private var retired = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitiyGameBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        user = intent.getSerializableExtra("user") as User

        val arr = intent.getStringArrayExtra("player") as Array<String>
        for(s in arr) {
            if(s != "") {
                player[s] = 0
            }
        }

        btns = arrayOf(mBinding.btnDeck1, mBinding.btnDeck2, mBinding.btnDeck3,
            mBinding.btnDeck4, mBinding.btnDeck5, mBinding.btnDeck6)

        for(i in btns.indices) {
            btns[i].isClickable = false
            btns[i].setOnClickListener {
                if(!cardPicked) {
                    val map = HashMap<String, String>()
                    map["what"] = "704"
                    map["num"] = deck[i].toString()
                    wanderer.send(map)
                    cardPicked = true
                }
            }
        }
        setPlayer()
        showDeckDial()

        mBinding.btnLog.setOnClickListener {
            if(mBinding.btnLog.tag == 1) {
                setGameView(false, 'l')
            }else {
                setGameView(true, 'l')
            }
        }

        mBinding.btnChat.setOnClickListener {
            if(mBinding.btnChat.tag == 1) {
                setGameView(false, 'c')
            }else {
                setGameView(true, 'c')
            }
        }
    }

    private fun setGameView(show: Boolean, tag: Char) {
        if(show) {
            mBinding.viewGame.visibility = View.VISIBLE
            if(tag == 'c') {
                mBinding.btnChat.tag = 1
                mBinding.btnLog.tag = 0
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.viewGame, chatFr)
                    .commit()
            }else {
                mBinding.btnChat.tag = 0
                mBinding.btnLog.tag = 1
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.viewGame, logFr)
                    .commit()
            }
        }else {
            mBinding.viewGame.visibility = View.INVISIBLE
            mBinding.btnChat.tag = 0
            mBinding.btnLog.tag = 0
        }
    }

    private fun setPlayer() {
        val pTxt = arrayOf(mBinding.txtPlayer1, mBinding.txtPlayer2,
                mBinding.txtPlayer3, mBinding.txtPlayer4)
        val pImg = arrayOf(mBinding.cardPlayer1, mBinding.cardPlayer2,
                mBinding.cardPlayer3, mBinding.cardPlayer4)

        var cnt = 0
        for((k, v) in player) {
            pTxt[cnt].text = k
            if(v != 0) {
                pImg[cnt].setImageResource(cards[v - 1])
            }else {
                pImg[cnt].setImageResource(R.drawable.img_card_back)
            }
            cnt++
        }
    }

    private fun showDeckDial() {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialDeckBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()
        dial.setCancelable(false)

        var pickedCard = 0

        val imgs = arrayOf(mBinding.imgCard1, mBinding.imgCard2, mBinding.imgCard3, mBinding.imgCard4, mBinding.imgCard5,
                        mBinding.imgCard6, mBinding.imgCard7, mBinding.imgCard8, mBinding.imgCard9, mBinding.imgCard10,
                        mBinding.imgCard11, mBinding.imgCard12, mBinding.imgCard13, mBinding.imgCard14, mBinding.imgCard15)

        for(i in imgs.indices) {
            imgs[i].tag = 0
            imgs[i].setOnClickListener {
               if(imgs[i].tag == 1 && pickedCard > 0) {
                    imgs[i].tag = 0
                    imgs[i].colorFilter = null
                    pickedCard--
                }else if(pickedCard < 6) {
                    imgs[i].tag = 1
                    imgs[i].setColorFilter(R.color.black, PorterDuff.Mode.MULTIPLY)
                    pickedCard++
                }
            }
        }

        mBinding.btnOk.setOnClickListener {
            if(pickedCard == 6) {
                var cnt = 0
                for(i in imgs.indices) {
                    if(imgs[i].tag == 1) {
                        deck[cnt] = i + 1
                        cnt++
                    }
                }
                val map = HashMap<String, String>()
                map["what"] = "702"
                wanderer.send(map)
                dial.dismiss()
            }else {
                Toast.makeText(this, "카드를 6장 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setDeck(order: Int, token: Int) {
        cardPicked = false
        val imgs = arrayOf(R.drawable.imb_card1, R.drawable.imb_card2, R.drawable.imb_card3, R.drawable.imb_card4, R.drawable.imb_card5,
                        R.drawable.imb_card6, R.drawable.imb_card7,R.drawable.imb_card8, R.drawable.imb_card9, R.drawable.imb_card10,
                        R.drawable.imb_card11, R.drawable.imb_card12, R.drawable.imb_card13, R.drawable.imb_card14, R.drawable.imb_card15)

        val imgsp = arrayOf(R.drawable.img_card1_p, R.drawable.img_card2_p, R.drawable.img_card3_p, R.drawable.img_card4_p, R.drawable.img_card5_p,
                R.drawable.img_card6_p, R.drawable.img_card7_p,R.drawable.img_card8_p, R.drawable.img_card9_p, R.drawable.img_card10_p,
                R.drawable.img_card11_p, R.drawable.img_card12_p, R.drawable.img_card13_p, R.drawable.img_card14_p, R.drawable.img_card15_p)

        val inCondition = BooleanArray(6)
        when(token) {
            1 -> {
                for(i in deck.indices) {
                    if(deck[i] >= order) {
                        inCondition[i] = true
                    }
                }
            }

            2 -> {
                for(i in deck.indices) {
                    if(deck[i] <= order) {
                        inCondition[i] = true
                    }
                }
            }

            3 -> {
                for(i in deck.indices) {
                    if(deck[i] % 2 == 1) {
                        inCondition[i] = true
                    }
                }
            }

            4 -> {
                for(i in deck.indices) {
                    if(deck[i] % 2 == 0) {
                        inCondition[i] = true
                    }
                }
            }
        }

        for(i in deck.indices) {
            if(deck[i] != 0 && inCondition[i]) {
                btns[i].background = ResourcesCompat.getDrawable(resources, imgs[deck[i] - 1], null)
                btns[i].isClickable = true
            }else if(deck[i] != 0) {
                btns[i].background = ResourcesCompat.getDrawable(resources, imgsp[deck[i] - 1], null)
                btns[i].isClickable = false
            }else {
                btns[i].background = ResourcesCompat.getDrawable(resources, R.drawable.img_card_back, null)
                btns[i].isClickable = false
            }
        }

        for(i in btns.indices) {
            if(btns[i].isClickable) {
                return
            }
        }
        if(!retired) {
            logFr.addList("제출 할 수 있는 카드가 없어 패배 했습니다.")
            val map = HashMap<String, String>()
            map["what"] = "708"
            wanderer.send(map)
            retired = true
        }
    }

    private fun setCondition(order: Int, token: Int) {
        mBinding.imgCondition2.background = null
        var chat = ""
        when(token) {
            1 -> {
                mBinding.imgCondition.background = ResourcesCompat.getDrawable(resources, cards[order - 1], null)
                mBinding.imgCondition2.background = ResourcesCompat.getDrawable(resources, R.drawable.img_up, null)
                chat = "조건식 : '${order}' 이상의 숫자 제출"
            }

            2 -> {
                mBinding.imgCondition.background = ResourcesCompat.getDrawable(resources, cards[order - 1], null)
                mBinding.imgCondition2.background = ResourcesCompat.getDrawable(resources, R.drawable.img_down, null)
                chat = "조건식 : '${order}' 이하의 숫자 제출"
            }

            3 -> {
                mBinding.imgCondition.background = ResourcesCompat.getDrawable(resources, R.drawable.img_odd, null)
                chat = "조건식 : '홀수' 제출"
            }

            4 -> {
                mBinding.imgCondition.background = ResourcesCompat.getDrawable(resources, R.drawable.img_even, null)
                chat = "조건식 : '짝수' 제출"
            }
        }

        logFr.addList(chat)
        setDeck(order, token)
    }

    override fun onStart() {
        super.onStart()
        wanderer.setHandler(GameHandler())
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        val map = HashMap<String, String>()
        map["what"] = "710"
        wanderer.send(map)
    }

    inner class GameHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val receive = JSONObject(msg.obj.toString())
                when (msg.what) {

                    601 -> {
                        val chat = receive.getString("name") + " : " +  receive.getString("chat")
                        chatFr.addList(chat)
                    }

                    702 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                         if(!isValidate) {
                             showDeckDial()
                         }
                    }
                    703 -> {
                        val order = receive.getString("order").toInt()
                        val token = receive.getString("token").toInt()
                        setCondition(order, token)
                    }

                    704 -> {
                        val isValidate = receive.getString("isValidate") == "1"
                        if(isValidate) {
                            val num = receive.getString("num").toInt()
                            for(i in deck.indices) {
                                if(num == deck[i]) {
                                    deck[i] = 0
                                    btns[i].background = ResourcesCompat.getDrawable(resources, R.drawable.img_card_back, null)
                                    btns[i].isClickable = false
                                    break
                                }
                            }
                            logFr.addList("${num}을(를) 제출 했습니다.")
                        }
                    }

                    705 -> {
                        val winner = receive.getString("winner")
                        val num = receive.getString("num").toInt()
                        val win = receive.getString("win_num").toInt()
                        if(winner == "") {
                            logFr.addList("무승부 입니다.")
                        }else {
                            logFr.addList("${winner}님이 ${win}을(를) 제출해서 이겼습니다.")
                        }
                        if(winner == user.name) {
                            for(i in deck.indices) {
                                if(deck[i] == 0) {
                                    deck[i] = num
                                    break
                                }
                            }
                        }
                    }


                    707 -> {
                        val arr = receive.getJSONArray("player")
                        for(i in 0 until arr.length()) {
                            val data = arr.getJSONObject(i)
                            player[data.getString("name")] = data.getString("num").toInt()
                        }
                        setPlayer()
                    }

                    708 -> {
                        val name = receive.getString("name")
                        if(name == user.name) {
                            for(i in deck.indices) {
                                deck[i] = 0
                            }
                        }
                        player[name] = 0
                        setPlayer()
                    }

                    709 -> {
                        for(i in deck.indices) {
                            deck[i] = 0
                        }

                        val arr = receive.getJSONArray("player")
                        for(i in 0 until arr.length()) {
                            val data = arr.getJSONObject(i)
                            if(data.getString("name") == user.name) {
                                val grade = data.getString("grade").toInt()
                                val score = data.getString("score")
                                showResultDial(grade, score)
                                break
                            }
                        }
                    }

                    710 -> {
                        finish()
                    }

                    711 -> {
                        val arr = receive.getJSONArray("player")
                        for(i in 0 until arr.length()) {
                            val data = arr.getJSONObject(i)
                            if(data.getString("name") == user.name) {
                                for(j in deck.indices) {
                                    if(deck[j] == 0) {
                                        val num = data.getString("num").toInt()
                                        deck[j] = num
                                        logFr.addList("${num}을(를) 획득 했습니다.")
                                        setPlayer()
                                        break
                                    }
                                }
                                break
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun showResultDial(grade: Int, score: String) {
        val dial = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mBinding = DialResultBinding.inflate(this.layoutInflater)
        dial.setContentView(mBinding.root)
        dial.show()
        dial.setCancelable(false)

        val arr = arrayOf(R.drawable.img_result1, R.drawable.img_result2, R.drawable.img_result3, R.drawable.img_result4)
        mBinding.imgResult.setImageResource(arr[grade - 1])
        mBinding.btnBack.setOnClickListener {
            val map = HashMap<String, String>()
            map["what"] = "710"
            wanderer.send(map)
        }
        mBinding.txtPlayer.text = "${user.name}\n${score}"
    }
}