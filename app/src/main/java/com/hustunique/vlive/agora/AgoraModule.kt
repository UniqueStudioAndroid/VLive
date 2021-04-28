package com.hustunique.vlive.agora

import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.hustunique.vlive.R
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.RtcEngineConfig
import io.agora.rtc.RtcEngineConfig.LogConfig
import io.agora.rtc.mediaio.IVideoSource
import io.agora.rtc.video.VideoCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 4/27/21
 */
class AgoraModule(
    private val activity: ComponentActivity,
    private val onUserJoinedAction: (Int) -> Unit = {},
    private val onUserLeaveAction: () -> Unit = {}
) {

    companion object {
        private const val TAG = "AgoraModule"
    }

    private var mRtcEngine: RtcEngine? = null


    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            activity.lifecycleScope.launchWhenCreated {
                withContext(Dispatchers.Main) {
                    onUserJoinedAction(uid)
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            activity.lifecycleScope.launchWhenCreated {
                withContext(Dispatchers.Main) {
                    onUserLeaveAction()
                }
            }
        }

        override fun onWarning(warn: Int) {
            super.onWarning(warn)
            Log.w(TAG, "onWarning: $warn")
        }

        override fun onError(err: Int) {
            super.onError(err)
            Log.e(TAG, "onError: $err")
        }

        override fun onLocalVideoStats(stats: LocalVideoStats?) {
            super.onLocalVideoStats(stats)
            Log.i(TAG, "onLocalVideoStats() called with: stats = ${stats?.codecType} ${stats?.encodedBitrate}")
        }

        override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
            super.onFirstLocalVideoFrame(width, height, elapsed)
            Log.i(
                TAG,
                "onFirstLocalVideoFrame() called with: width = $width, height = $height, elapsed = $elapsed"
            )
        }

        override fun onFirstLocalVideoFramePublished(elapsed: Int) {
            super.onFirstLocalVideoFramePublished(elapsed)
            Log.i(TAG, "onFirstLocalVideoFramePublished() called with: elapsed = $elapsed")

        }

        override fun onLocalVideoStat(sentBitrate: Int, sentFrameRate: Int) {
            super.onLocalVideoStat(sentBitrate, sentFrameRate)
            Log.i(
                TAG,
                "onLocalVideoStat() called with: sentBitrate = $sentBitrate, sentFrameRate = $sentFrameRate"
            )
        }

        override fun onLocalVideoStateChanged(localVideoState: Int, error: Int) {
            super.onLocalVideoStateChanged(localVideoState, error)
            Log.i(
                TAG,
                "onLocalVideoStateChanged() called with: localVideoState = $localVideoState, error = $error"
            )
        }
    }


    fun initAgora(videoSource: IVideoSource): View {
        initializeAgoraEngine()
        val view = setupLocalVideo()
        mRtcEngine?.setVideoSource(videoSource)
        joinChannel()
        return view
    }


    private fun setupLocalVideo(): View {
        val surface = RtcEngine.CreateRendererView(activity).apply {
            setZOrderMediaOverlay(true)
        }
        mRtcEngine?.apply {
            enableVideo()
            setupLocalVideo(VideoCanvas(surface, VideoCanvas.RENDER_MODE_FIT, 0))
        }
        return surface
    }

    private fun joinChannel() {
        mRtcEngine?.joinChannel(AgoraActivity.TOKEN, "test1", "Extra Optional Data", 0)
    }

    private fun leaveChannel() {
        mRtcEngine?.leaveChannel()
    }

    private fun initializeAgoraEngine() {
        try {
            val logConfig = LogConfig()
            logConfig.level = Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_INFO)
            val ts: String = SimpleDateFormat("yyyyMMdd").format(Date())
            logConfig.filePath = "/sdcard/$ts.log"
            logConfig.fileSize = 2048 * 10

            val config = RtcEngineConfig()
            config.mAppId = activity.getString(R.string.agora_app_id)
            config.mEventHandler = mRtcEventHandler
            config.mContext = activity.applicationContext
            config.mLogConfig = logConfig
            mRtcEngine =
                RtcEngine.create(config)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))

            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(
                    e
                )
            )
        }
    }

    fun destroyAgora() {
        leaveChannel()
        RtcEngine.destroy()
    }

}