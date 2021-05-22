package com.hustunique.vlive.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hustunique.vlive.MainActivity
import com.hustunique.vlive.R
import com.hustunique.vlive.SceneActivity
import com.hustunique.vlive.databinding.ActivityChannelListBinding
import com.hustunique.vlive.remote.Channel
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.util.ToastUtil
import com.hustunique.vlive.util.UserInfoManager
import com.hustunique.vlive.util.startActivity
import java.util.ArrayList

class ChannelListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChannelListActivity"

        private const val PERMISSION_REQUESTS = 1
        var videoMode = false
    }

    private val binding by lazy {
        ActivityChannelListBinding.inflate(layoutInflater)
    }

    private val listAdapter = ChannelListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            UserInfoManager.blockRefreshUid()
            if (UserInfoManager.uid.isEmpty()) {
                startActivity<LoginActivity>()
                finish()
            }
        }
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
            Log.i(TAG, "onCreate: Request permission")
        }
        setContentView(binding.root)
        initView()
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    private fun initView() {
        binding.listRecycler.apply {
            layoutManager = LinearLayoutManager(this@ChannelListActivity)
            adapter = listAdapter
        }
        binding.logout.setOnClickListener {
            UserInfoManager.saveUid("")
            startActivity<LoginActivity>()
            finish()
        }
        binding.createRoom.setOnClickListener {
            startActivity<CreateRoomActivity>()
        }
        binding.cbMode.setOnCheckedChangeListener { _, isChecked -> videoMode = isChecked }
    }

    private fun initData() {
        lifecycleScope.launchWhenCreated {
            val data = Service.channelList().let {
                if (it.successful) {
                    it.data
                } else {
                    ToastUtil.makeShort(it.msg ?: "")
                    null
                }
            }
            listAdapter.setList(data)
        }
    }


    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS
            )
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }


}

class ChannelListAdapter : BaseQuickAdapter<Channel, BaseViewHolder>(R.layout.item_channel) {

    override fun convert(holder: BaseViewHolder, item: Channel) {
        holder.setText(R.id.channel_name, item.id)
            .setText(R.id.channel_desc, item.desc)

        holder.itemView.setOnClickListener {
            SceneActivity.startActivity(context, item.id)
        }
    }

}