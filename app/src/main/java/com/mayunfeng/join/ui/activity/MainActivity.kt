package com.mayunfeng.join.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.gyf.immersionbar.ImmersionBar
import com.mayunfeng.join.Application
import com.mayunfeng.join.BASE_URL
import com.mayunfeng.join.R
import com.mayunfeng.join.adapter.MainDrawerAdapter
import com.mayunfeng.join.adapter.MainMsgAdapter
import com.mayunfeng.join.api.UserApi
import com.mayunfeng.join.base.AppBaseActivity
import com.mayunfeng.join.bean.BaseBean
import com.mayunfeng.join.bean.MainMsgBean
import com.mayunfeng.join.bean.UserLoginBean
import com.mayunfeng.join.databinding.ActivityMainBinding
import com.mayunfeng.join.dialog.LoadingDialog
import com.mayunfeng.join.dialog.MsgDialog
import com.mayunfeng.join.ui.widget.MRecyclerView
import com.mayunfeng.join.utils.MyRetrofitObserver
import com.mayunfeng.join.utils.UserUtils
import com.mayunfeng.join.utils.retrofit.RetrofitManager
import com.pikachu.utils.utils.GlideUtils
import com.pikachu.utils.utils.NetUtils
import com.pikachu.utils.utils.TimeUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers


enum class UserGrade(
    val code: Int
) {
    OrdinaryUser(0), // 普通用户
    AdministratorUser(1), // 管理员
    RootUser(2) // ROOT管理员
}


@SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
class MainActivity : AppBaseActivity<ActivityMainBinding>() {

    private var mainMsgAdapter: MainMsgAdapter? = null
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var userInfo: UserLoginBean


    override fun setActivityWindowsInfo(isStatusBar: Boolean) {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .fitsSystemWindows(false)
            .navigationBarDarkIcon(isStatusBar)
            .navigationBarColor(R.color.color_bg)
            .init()
    }

    override fun onAppCreate(savedInstanceState: Bundle?) {
        initUi()
        initNavigationFragment()

        loadingDialog = LoadingDialog(context, getString(R.string.dialog_load_title))
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val loginToken = UserUtils.readLoginToken()
        if (loginToken == null || loginToken.isEmpty()) {
            showToast(R.string.login_user_token_nul)
            LoginActivity.startLoginActivity(this)
            finish()
            return
        }

        if (!NetUtils.isNetworkConnected(context)) {
            showToast(R.string.dialog_load_title_net_error)
            LoginActivity.startLoginActivity(this)
            finish()
            return
        }

        RetrofitManager.getInstance()
            .create(UserApi::class.java)
            .userInfo(loginToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MyRetrofitObserver<BaseBean<UserLoginBean>>() {
                override fun onRetrofitSubscribe(d: Disposable) {
                    loadingDialog.show()
                }

                override fun onRetrofitError(t: BaseBean<UserLoginBean>?, e: Throwable) {
                    loadingDialog.dismiss()
                    showToast(R.string.login_user_token_failure)
                    t?.let {
                        UserUtils.writeLoginToken("")
                    }
                    LoginActivity.startLoginActivity(this@MainActivity)
                    finish()
                }

                override fun onRetrofitComplete(t: BaseBean<UserLoginBean>) {
                    loadingDialog.dismiss()
                }

                override fun onRetrofitNext(t: BaseBean<UserLoginBean>) {
                    initUserInfoUi(t)
                }
            })
    }

    // 用户数据

    private fun initUserInfoUi(userInfo: BaseBean<UserLoginBean>) {

        val resultUserData = userInfo.result!!
        this.userInfo = resultUserData
        GlideUtils.initToken("token", UserUtils.readLoginToken())

        // 用户名
        binding.mainContent.mainUserName.text = resultUserData.userName
        binding.mainDrawer.tvUserName.text = resultUserData.userName

        // 用户头像
        GlideUtils.with(this)
            .loadBaseUrl(resultUserData.userImg)
            .into(binding.mainDrawer.QMUIRadiusImageView)
        GlideUtils.with(this)
            .loadBaseUrl(resultUserData.userImg)
            .into(binding.mainContent.ivBrandReturn)


        // 性别
        binding.mainDrawer.tvSex.text = if (resultUserData.userSex)
            getString(R.string.drawer_sex_boy)
        else
            getString(R.string.drawer_sex_girl)

        // 性别图标
        binding.mainDrawer.tvSex.setCompoundDrawablesRelative(
            if (resultUserData.userSex) {
                resources.getDrawable(R.drawable.ic_drawer_me_boy, null)
            } else {
                resources.getDrawable(R.drawable.ic_drawer_me_girl, null)
            }, null, null, null
        )

        // 用户等级
        binding.mainDrawer.tvAdmin.text = when (resultUserData.userGrade) {
            UserGrade.RootUser.code -> getString(R.string.login_user_grade_2)
            UserGrade.AdministratorUser.code -> getString(R.string.login_user_grade_1)
            else -> getString(R.string.login_user_grade_0)
        }

        // 用户账号
        binding.mainDrawer.tvUserId.text =
            "${getString(R.string.login_user_id)}${resultUserData.userAccount}"

        //....

    }


    private fun initUi() {
        //  binding.mainContent.recycler.setAdapter(TestAdapter(), 1)


        binding.mainContent.view2.setOnClickListener {
            binding.root.open()
        }


        // 刷新
        binding.mainContent.smartRefreshLayout.setOnRefreshListener {
            TimeUtils.timing(200) {
                binding.mainContent.smartRefreshLayout.finishRefresh()
            }
        }

        // 加载
        binding.mainContent.smartRefreshLayout.setOnLoadMoreListener {
            TimeUtils.timing(1000) {
                // 完成刷新并标记没有更多数据
                binding.mainContent.smartRefreshLayout.finishRefreshWithNoMoreData()
            }
        }

        // 退出登录
        binding.mainDrawer.appCompatTextView7.setOnClickListener {
            MsgDialog(context, getString(R.string.dialog_msg_out_login), {
                UserUtils.writeLoginToken("")
                LoginActivity.startLoginActivity(this)
                finish()
                true
            }).show()
        }


        // 用户信息
        binding.mainDrawer.QMUIRadiusImageView.setOnClickListener {
            EditUserInfoActivity.startEditUserInfoActivity(this, userInfo)
        }
        binding.mainContent.userInfo.setOnClickListener {
            EditUserInfoActivity.startEditUserInfoActivity(this, userInfo)
        }


        // 加载msg数据
        infoMsgListUi(MainMsgBean.getTestData())
    }


    // ui msg 初始化数据 or 更新数据
    private fun infoMsgListUi(`data`: MutableList<MainMsgBean>) {
        if (mainMsgAdapter == null) {
            mainMsgAdapter = MainMsgAdapter(`data`)
            binding.mainContent.recycler.adapter = mainMsgAdapter
            return
        }
        mainMsgAdapter!!.refreshData(`data`)
    }

    // ui msg 初始化数据 or 更新数据
    private fun addMsgListUi(`data`: MutableList<MainMsgBean>) {
        mainMsgAdapter?.addDataAll(`data`)
    }


    private fun initNavigationFragment() {
        val mainDrawerAdapter = MainDrawerAdapter(true)
        binding.mainDrawer.drawerRecycler.adapter = mainDrawerAdapter
        MRecyclerView.setAutoLayoutManager(context, binding.mainDrawer.drawerRecycler)
        binding.mainDrawer.outDr.setOnClickListener {
            binding.root.closeDrawers()
        }
    }
}