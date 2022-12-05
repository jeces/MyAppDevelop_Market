package com.example.applicationjeces.user

import androidx.databinding.BindingAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

object DataLoginPageBinding {
    @JvmStatic
    @BindingAdapter("onNavigationItemSelected")
    fun bindOnNavigationItemSelectedListener(
        view: BottomNavigationView, listener: NavigationBarView.OnItemSelectedListener
    ) {
        view.setOnItemSelectedListener(listener)
    }
}