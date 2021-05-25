package com.hustunique.vlive.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hustunique.vlive.databinding.FragmentActorChooseBinding

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/24
 */
class ActorChooseFragment : Fragment() {

    private val binding by lazy { FragmentActorChooseBinding.inflate(layoutInflater) }
    private val args by navArgs<ActorChooseFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.confirmButton.setOnClickListener {
            findNavController().navigate(
                ActorChooseFragmentDirections.actionActorChooseFragmentToSceneActivity(
                    args.cid
                )
            )
        }
        return binding.root
    }
}