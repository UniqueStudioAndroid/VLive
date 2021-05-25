package com.hustunique.vlive.remote

import com.hustunique.vlive.BuildConfig
import com.hustunique.vlive.ui.ChannelListFragment
import com.hustunique.vlive.util.JsonUtil
import com.hustunique.vlive.util.UserInfoManager
import com.hustunique.vlive.util.netReq
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/20
 */
object Service {

    private val okhttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            })
            .build()
    }

    private val retrofitClient by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okhttp)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val remoteApi by lazy {
        retrofitClient.create(RemoteApi::class.java)
    }

    suspend fun userReg(userName: String, male: Boolean) = netReq {
        remoteApi.userReg(RegReq(userName, male)).apply {
            if (success) {
                UserInfoManager.saveUid(data?.uid ?: "", userName)
            }
        }
    }

    suspend fun channelJoin(channelId: String) = netReq {
        if (UserInfoManager.uid.isEmpty()) {
            return@netReq BaseRsp(-1, "do not login", null)
        }
        remoteApi.channelJoin(
            ChannelJoinReq(
                UserInfoManager.uid,
                channelId,
                ChannelListFragment.videoMode
            )
        )
    }

    suspend fun channelLeave(channelId: String) = netReq {
        if (UserInfoManager.uid.isEmpty()) {
            return@netReq BaseRsp(-1, "do not login", null)
        }
        remoteApi.channelLeave(JsonUtil.jsonReqBody {
            it["uid"] = UserInfoManager.uid
            it["cid"] = channelId
        })
    }

    suspend fun channelList() = netReq {
        remoteApi.channelList(JsonUtil.jsonReqBody { })
    }

    suspend fun createChannel(channelName: String, desc: String) = netReq {
        remoteApi.createChannel(JsonUtil.jsonReqBody {
            it["cid"] = channelName
            it["desc"] = desc
        })
    }

}