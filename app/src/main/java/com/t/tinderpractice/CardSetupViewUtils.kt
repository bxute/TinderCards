/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */
package com.t.tinderpractice

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics

object CardSetupViewUtils {
    lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    var displayMetrics: DisplayMetrics? = null
    /**
     * Returns Device Height
     * */
    fun getScreenHeight(): Int {
        if (displayMetrics == null) {
            displayMetrics = DisplayMetrics()
            (context as Activity).windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
        }
        return displayMetrics!!.heightPixels
    }

    /**
     * Returns Device Width
     * */
    fun getScreenWidth(): Int {
        if (displayMetrics == null) {
            displayMetrics = DisplayMetrics()
            (context as Activity).windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
        }
        return displayMetrics!!.widthPixels
    }
}