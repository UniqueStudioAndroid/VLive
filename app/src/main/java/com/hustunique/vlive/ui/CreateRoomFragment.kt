package com.hustunique.vlive.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hustunique.vlive.databinding.FragmentCreateRoomBinding

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/24
 */
class CreateRoomFragment : Fragment() {

    private val binding by lazy {
        FragmentCreateRoomBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}