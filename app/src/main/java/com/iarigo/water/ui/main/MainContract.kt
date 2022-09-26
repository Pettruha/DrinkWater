package com.iarigo.water.ui.main

import com.iarigo.water.base.BaseMvpPresenter
import com.iarigo.water.base.BaseMvpView

interface MainContract {
    interface View: BaseMvpView {
        fun showFirstLaunch() // показ информации при первом запуске
        fun showGradeUs() // оцените нас
    }

    interface Presenter: BaseMvpPresenter<View> {
        fun onResume()
        fun saveFirstLaunch()
    }
}