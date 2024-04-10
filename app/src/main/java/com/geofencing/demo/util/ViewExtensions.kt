package com.geofencing.demo.util

import android.view.View

object ViewExtensions {

    fun View.enable(){
        this.isEnabled = true
    }

    fun View.disable(){
        this.isEnabled = false
    }

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.hide(){
        this.visibility = View.GONE
    }

}