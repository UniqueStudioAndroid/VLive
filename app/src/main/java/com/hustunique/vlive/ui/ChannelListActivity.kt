package com.hustunique.vlive.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hustunique.vlive.R
import com.hustunique.vlive.SceneActivity
import com.hustunique.vlive.databinding.ActivityChannelListBinding
import com.hustunique.vlive.remote.Channel
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.util.ToastUtil
import com.hustunique.vlive.util.UserInfoManager
import com.hustunique.vlive.util.startActivity

class ChannelListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChannelListActivity"

        var videoMode = false
    }

    private val binding by lazy {
        ActivityChannelListBinding.inflate(layoutInflater)
    }

    private val listAdapter = ChannelListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (UserInfoManager.uid.isEmpty()) {
            startActivity<LoginActivity>()
            finish()
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