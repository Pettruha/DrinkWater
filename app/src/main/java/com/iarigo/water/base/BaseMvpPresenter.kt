package com.iarigo.water.base

interface BaseMvpPresenter<in V: BaseMvpView> {
    fun attachView(mvpView: V);

    fun viewIsReady();

    fun detachView();

    fun destroy();
}