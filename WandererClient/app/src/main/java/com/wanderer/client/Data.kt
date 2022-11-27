package com.wanderer.client

import java.io.Serializable


data class User(val name : String = "로그인을 해주세요.", val money : Int = 0): Serializable

data class UserRank(val name: String, val img: Int, val rank: Int, val rate: Int)

data class RoomInfo(val name: String, val playerInfo: ArrayList<PlayerInfo>): Serializable

data class PlayerInfo(val name: String = ""): Serializable

data class NoticeInfo(val num: String, val title: String, val date: String): Serializable