package com.mayunfeng.join.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.mayunfeng.join.R
import com.pikachu.utils.base.BaseActivity
import com.pikachu.utils.utils.UiUtils

/**
 *
 * @ProjectName:    考勤记录管理系统
 * @Package:        com.mayunfeng.join.base
 * @Author:         pkpk.run
 * @Description:    null
 */
abstract class AppBaseActivity<T : ViewBinding> : BaseActivity<T>() {
    abstract fun onAppCreate(savedInstanceState: Bundle?)
    override fun initActivity(savedInstanceState: Bundle?) {
        setActivityWindowsInfo(resources.getBoolean(R.bool.isStatusBar))
        try {
            findViewById<View>(R.id.app_back)?.setOnClickListener {
                finish()
            }
            findViewById<View>(R.id.app_status)?.let {
                it.layoutParams.height =
                UiUtils.getStatusBarHeight(this@AppBaseActivity)
            }
        } catch (_: Exception) {
        }
        onAppCreate(savedInstanceState)
    }


    open fun setActivityWindowsInfo(isStatusBar: Boolean) {
        ImmersionBar.with(this)
            .navigationBarDarkIcon(isStatusBar)
            .statusBarDarkFont(true)
            .fitsSystemWindows(true)
            .navigationBarColor(R.color.color_bg)
            .statusBarColor(R.color.color_bg)
            .init()
    }


}