package com.example.kotlineatitv2client.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.asksira.loopingviewpager.LoopingViewPager
import com.example.kotlineatitv2client.Adapter.MyBestDealsAdapter
import com.example.kotlineatitv2client.Adapter.MyPopularCategoriesAdapter
import com.example.kotlineatitv2client.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel


    var recyclerView : RecyclerView?=null
    var viewPager :LoopingViewPager?=null

    var layoutAnimationController:LayoutAnimationController?=null


    @SuppressLint("FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)


        initView(root)
        homeViewModel.popularList.observe(this, Observer {
            val listData = it
            val adapter = MyPopularCategoriesAdapter(context!!, listData)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutAnimation = layoutAnimationController
        })
        homeViewModel.bestDealList.observe(this, Observer {
            val adapter = MyBestDealsAdapter(context!!, it, false)
            viewPager!!.adapter = adapter
        })
        return root
}

    private fun initView(root : View) {
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        viewPager = root.findViewById(R.id.viewpager) as LoopingViewPager
        recyclerView = root.findViewById(R.id.recycler_popular) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        viewPager!!.resumeAutoScroll()
    }


    override fun onPause() {
        viewPager!!.resumeAutoScroll()
        super.onPause()
    }
}