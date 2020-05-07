package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.CategoryModel
import com.example.kotlineatitv2client.Model.PopularCategoryModel

interface ICategoryCallbackListener {
    fun onCategoryLoadSuccess(categoriesList:List<CategoryModel>)
    fun onCategoryLoadFailed(message : String)
}