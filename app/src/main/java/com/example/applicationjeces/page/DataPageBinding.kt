package com.example.applicationjeces.page

import androidx.databinding.BindingAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

object DataPageBinding {
    @JvmStatic
    @BindingAdapter("onNavigationItemSelected")
    fun bindOnNavigationItemSelectedListener(
        view: BottomNavigationView, listener: NavigationBarView.OnItemSelectedListener
    ) {
        view.setOnItemSelectedListener(listener)
    }
}