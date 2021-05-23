package com.hustunique.vlive.ui

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hustunique.vlive.R
import com.hustunique.vlive.SceneActivity
import com.hustunique.vlive.databinding.FragmentChannelListBinding
import com.hustunique.vlive.remote.Channel
import com.hustunique.vlive.remote.Service
import com.hustunique.vlive.util.ToastUtil

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/23
 */
class ChannelListFragment : Fragment() {

    private val binding by lazy {
        FragmentChannelListBinding.inflate(layoutInflater)
    }

    private val listAdapter = ChannelListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.actor_img_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.channelListRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = listAdapter
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.root.transitionToEnd()
        initData()
    }

    private fun initData() {
        lifecycleScope.launchWhenCreated {
            Service.channelList().let {
                if (it.successful) {
                    it.data
                } else {
                    ToastUtil.makeShort(it.msg ?: "")
                    null
                }
            }?.let {
                if (it.isNotEmpty()) {
                    binding.actorImg.visibility = View.GONE
                }
                listAdapter.setList(it)
            }
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