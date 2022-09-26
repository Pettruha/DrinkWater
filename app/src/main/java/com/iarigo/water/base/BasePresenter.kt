package com.iarigo.water.base

open class BasePresenter<V : BaseMvpView> : BaseMvpPresenter<V> {
    protected var view: V? = null

    override fun attachView(mvpView: V) {
        view = mvpView
    }

    override fun viewIsReady() {
        TODO("Not yet implemented")
    }

    override fun detachView() {
        view = null
    }

    override fun destroy() {}

    @JvmName("getView1")
    fun getView(): V? {
        return view
    }

    protected fun isViewAttached(): Boolean {
        return view != null
    }




}