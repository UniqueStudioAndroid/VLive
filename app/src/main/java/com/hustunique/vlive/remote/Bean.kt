package com.hustunique.vlive.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/20
 */

@JsonClass(generateAdapter = true)
data class BaseRsp<T>(
    @Json(name = "code")
    val code: Int,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "data")
    val data: T?
) {
    val success = code == 0
}

@JsonClass(generateAdapter = true)
data class RegReq(
    @Json(name = "name")
    val userName: String,
    @Json(name = "male")
    val male: Boolean
)

data class NetRsp<T>(
    val data: T? = null,
    val successful: Boolean = true,
    val msg: String? = ""
)

@JsonClass(generateAdapter = true)
data class RegRsp(
    @Json(name = "uid")
    val uid: String
)

@JsonClass(generateAdapter = true)
data class JoinRspData(
    @Json(name = "uid")
    val uid: String,
    @Json(name = "channel_id")
    val channelId: String,
)

@JsonClass(generateAdapter = true)
data class Channel(
    @Json(name = "id")
    val id: String,
    @Json(name = "desc")
    val desc: String,
    @Json(name = "member_count")
    val memberCount: Int
)