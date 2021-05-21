package com.hustunique.vlive.util

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hustunique.vlive.VLiveApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/20
 */
object UserInfoManager {

    const val UID = "uid"

    var uid: String = ""

    private val uidKey by lazy { stringPreferencesKey(UID) }

    fun refreshUid() {
        GlobalScope.launch {
            uid = VLiveApplication.application.dataStore.data.map { it[uidKey] }.first() ?: ""
        }
    }

    suspend fun blockRefreshUid() {
        uid = VLiveApplication.application.dataStore.data.map { it[uidKey] }.first() ?: ""
    }

    fun saveUid(uid: String) {
        this.uid = uid
        GlobalScope.launch {
            VLiveApplication.application.dataStore.edit {
                it[uidKey] = uid
            }
        }
    }


}