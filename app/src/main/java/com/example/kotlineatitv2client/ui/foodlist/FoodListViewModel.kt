package com.example.kotlineatitv2client.ui.foodlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.FoodModel

class FoodListViewModel : ViewModel() {

    private var mutableFoodListData : MutableLiveData<List<FoodModel>>?=null

            fun getMutableFoodModelListData() : MutableLiveData<List<FoodModel>>{
                if(mutableFoodListData == null)
                    mutableFoodListData = MutableLiveData()
                mutableFoodListData!!.value = Common.categorySelected!!.foods
                return mutableFoodListData!!
            }
}