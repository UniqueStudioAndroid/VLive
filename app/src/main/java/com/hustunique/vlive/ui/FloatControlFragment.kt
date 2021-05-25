package com.hustunique.vlive.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hustunique.vlive.R
import com.hustunique.vlive.databinding.FragmentFloatControlBinding

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/25
 */
class FloatControlFragment : Fragment() {

    companion object {
        private const val TAG = "FloatControlFragment"
    }

    private val binding by lazy {
        FragmentFloatControlBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<SceneViewModel>({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.rocker.onUpdate = { radians, progress ->
            viewModel.eventData.postValue(RockerEvent(radians, progress))
        }
        binding.modeSwitcher.setOnClickListener {
            val curState = binding.rocker.enable
            viewModel.eventData.postValue(ModeSwitchEvent(!curState))
            if (!curState) {
                binding.modeSwitcher.background =
                    requireActivity().getDrawable(R.drawable.round_btn_bg)
            } else {
                binding.modeSwitcher.background =
                    requireActivity().getDrawable(R.drawable.round_btn_bg_light)
            }
            binding.rocker.enable = !curState
        }
        binding.reset.setOnClickListener {
            viewModel.eventData.postValue(ResetEvent())
        }
        return binding.root
    }
}