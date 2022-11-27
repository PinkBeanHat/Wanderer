package com.wanderer.client

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.wanderer.client.ClientService.MyBinder
import org.json.JSONException
import org.json.JSONObject

class Wanderer : Application() {
    lateinit var service: ClientService
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            service as MyBinder
            instance.service = service.service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i("SocketManager", "onServiceDisconnected()")
        }
    }

    fun send(map: HashMap<String, String>) {
        try {
            val send = JSONObject()
            for((k, v) in map) {
                send.put(k, v)
            }
            service.send(send.toString())
        }catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        service.disconnect()
    }

    fun setHandler(handler: Handler) {
        service.setHandler(handler)
    }

    fun isConnected(): Boolean = service.isConnected

    fun isUser(context: Context): Boolean {
        val myDb = MyDBHelper(context)
        val sqlDb = myDb.readableDatabase
        val cur = sqlDb.rawQuery("SELECT * FROM userTB", null)
        val check = cur.count > 0
        cur.close()
        sqlDb.close()
        myDb.close()
        return check
    }

    fun getUser(context: Context): User {
        val myDb = MyDBHelper(context)
        val sqlDb = myDb.readableDatabase
        val cur = sqlDb.rawQuery("SELECT * FROM userTB", null)
        return if (cur.count > 0) {
            cur.moveToFirst()
            val arr = intArrayOf(
                cur.getColumnIndex("name"),
                cur.getColumnIndex("money")
            )
            val curUser = User(cur.getString(arr[0]), cur.getInt(arr[1]))
            cur.close()
            sqlDb.close()
            myDb.close()
            curUser
        }else {
            sqlDb.close()
            myDb.close()
            User()
        }
    }
    fun addUser(context: Context, user: User) {
        val myDb = MyDBHelper(context)
        val sqlDb = myDb.writableDatabase
        sqlDb.execSQL("INSERT INTO userTB VALUES ('${user.name}', ${user.money});")
        sqlDb.close()
        myDb.close()
    }

    fun updateUser(context: Context, user: User) {
        val myDb = MyDBHelper(context)
        val sqlDb = myDb.writableDatabase
        sqlDb.execSQL("UPDATE userTB SET money = ${user.money} WHERE name = '${user.name}'")
        sqlDb.close()
        myDb.close()
    }

    fun deleteUser(context: Context) {
        val myDb = MyDBHelper(context)
        val sqlDb = myDb.writableDatabase
        myDb.onUpgrade(sqlDb, 0, 1)
        sqlDb.close()
        myDb.close()
    }

    override fun onCreate() {
        super.onCreate()
        val context = applicationContext
        val intent = Intent(context, ClientService::class.java)
        context.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    companion object {
        val instance = Wanderer()
    }

    init {
        Log.i("SocketManager", "SocketManager()")
    }
}