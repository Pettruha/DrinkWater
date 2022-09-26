package com.iarigo.water.ui.fragment_weight

import android.content.Context
import com.github.mikephil.charting.data.Entry
import com.iarigo.water.storage.entity.Weight

interface WeightContract {
    interface View {
        fun getFragmentContext(): Context
        fun setWeightLog(weightList: List<Weight>)
        fun setCurrentWeight(weight: Weight)
        fun setData(values: ArrayList<Entry>)
        fun registryGraph(values: ArrayList<Entry>)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getWeights()
        fun getCurrentWeight()
        fun getGraph()
    }
}