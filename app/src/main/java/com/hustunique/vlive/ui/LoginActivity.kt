package com.hustunique.vlive.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hustunique.vlive.MainActivity
import com.hustunique.vlive.databinding.ActivityLoginBinding
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.util.ToastUtil
import com.hustunique.vlive.util.startActivity

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }


    private fun initView() {
        binding.loginButton.setOnClickListener {
            if (binding.username.text.isEmpty()) {
                ToastUtil.makeLong("请输入用户名")
                return@setOnClickListener
            }
            lifecycleScope.launchWhenCreated {
                Service.userReg(binding.username.text.toString(), binding.sexSwitch.isChecked)
                    .apply {
                        if (successful) {
                            startActivity<ChannelListActivity>()
                            finish()
                        } else {
                            ToastUtil.makeLong(msg ?: "")
                        }
                    }
            }
        }
    }
}