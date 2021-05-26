package com.hustunique.vlive.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hustunique.vlive.R
import com.hustunique.vlive.databinding.FragmentActorChooseBinding

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/24
 */
class ActorChooseFragment : Fragment() {

    private val binding by lazy { FragmentActorChooseBinding.inflate(layoutInflater) }
    private val args by navArgs<ActorChooseFragmentArgs>()

    private val listAdapter = ImgListAdapter().apply {
        setList(listOf("1", "2"))
    }

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
                    args.cid,
                    binding.actorImgPager.currentItem + 1
                )
            )
        }
        binding.actorImgPager.apply {
            adapter = listAdapter
        }
        binding.indicator.setViewPager2(binding.actorImgPager)
        return binding.root
    }
}

class ImgListAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_image) {

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.getView<ImageView>(R.id.item_image)
            .setImageDrawable(context.getDrawable(R.drawable.actor))
    }
}