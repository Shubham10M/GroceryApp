package com.example.flash.data

import androidx.annotation.StringRes
import com.example.flash.R

object DataSource {
    fun loadCategories() : List<Categories>{
        return listOf<Categories>(
            Categories(stringResourceId = R.string.fresh_fruits, imageResourceId = R.drawable.market_basket),
            Categories(R.string.bath_body, R.drawable.market),
            Categories(R.string.bread_biscuits,R.drawable.market_basket),
            Categories(R.string.kitchen_essentials, R.drawable.market),
            Categories(R.string.munchies, R.drawable.market_basket),
            Categories(R.string.packaged_food, R.drawable.market),
            Categories(R.string.stationery, R.drawable.market_basket),
            Categories(R.string.pet_food, R.drawable.market),
            Categories(R.string.sweet_tooth, R.drawable.market_basket),
            Categories(R.string.vegetables, R.drawable.market),
            Categories(R.string.beverages, R.drawable.market_basket)
        )
    }

    fun loadItems(
        @StringRes categoryName : Int
    ) : List<Item>{
        return listOf<Item>(
            Item(R.string.banana_robusta,R.string.fresh_fruits,"1 Kg", 100,R.drawable.banana),
            Item(R.string.shimla_apple,R.string.fresh_fruits,"1 Kg", 130,R.drawable.apple),
            Item(R.string.papaya_semi_ripe,R.string.fresh_fruits,"1 Kg", 120,R.drawable.corn),
            Item(R.string.pomegranate,R.string.fresh_fruits,"500 g", 103,R.drawable.pomegrante),
            Item(R.string.avocado,R.string.fresh_fruits,"1 Kg", 190,R.drawable.avocado),
            Item(R.string.pepsi_can,R.string.beverages,"1 liter", 80,R.drawable.pepsi)
        ).filter {
            it.itemCategoryId == categoryName
        }
    }
}