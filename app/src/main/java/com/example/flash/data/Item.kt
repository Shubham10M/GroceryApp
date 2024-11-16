package com.example.flash.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Item(
    @StringRes val stringResourceId : Int,
    @StringRes val itemCategoryId : Int,
    val itemQuantity : String,
    val itemPrice : Int,
   @DrawableRes val imageResourceId : Int
)

// @StringRes --> reprents that stringResourceId,itemCategoryId
//                only accepts integer representing a String resource from
//                  resource folder
