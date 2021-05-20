package com.hustunique.vlive.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hustunique.vlive.R
import com.hustunique.vlive.databinding.ActivityChannelListBinding
import com.hustunique.vlive.remote.Channel
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.util.ToastUtil

class ChannelListActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityChannelListBinding.inflate(layoutInflater)
    }

    private val listAdapter = ChannelListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initData()
    }

    private fun initView() {
        binding.listRecycler.apply {
            layoutManager = LinearLayoutManager(this@ChannelListActivity)
            adapter = listAdapter
        }
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
    }

}