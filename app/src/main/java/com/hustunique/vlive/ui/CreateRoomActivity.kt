package com.hustunique.vlive.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustunique.vlive.databinding.ActivityCreateRoomBinding
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.util.ToastUtil

class CreateRoomActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCreateRoomBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.createRoomBtn.setOnClickListener {
            if (binding.roomName.text.isEmpty()) {
                ToastUtil.makeShort("请输入房间名")
                return@setOnClickListener
            }
            if (binding.roomDesc.text.isEmpty()) {
                ToastUtil.makeShort("请输入房间描述")
                return@setOnClickListener
            }
            lifecycleScope.launchWhenCreated {
                Service.createChannel(
                    binding.roomName.text.toString(),
                    binding.roomDesc.text.toString()
                ).let {
                    if (it.successful) {
                        finish()
                    } else {
                        ToastUtil.makeShort("创建失败 ${it.msg}")
                    }
                }
            }
        }
    }
}