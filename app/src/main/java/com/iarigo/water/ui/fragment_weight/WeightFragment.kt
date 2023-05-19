package com.iarigo.water.ui.fragment_weight

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentWeightBinding
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.dialogWeight.DialogWeight
import com.iarigo.water.ui.main.MainViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class WeightFragment: Fragment() {

    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private var binding: FragmentWeightBinding? = null
    private lateinit var listAdapter: WeightAdapter
    private val aList: ArrayList<Weight> = ArrayList() // weight list

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWeightBinding.inflate(layoutInflater, container, false)

        init()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.getWeights()
        mainViewModel.getCurrentWeightFragment()
        mainViewModel.getGraphFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Weight Log
        mainViewModel.weightFragmentLog.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setWeightLog(it)
            }
        }

        // Current Weight
        mainViewModel.weightFragmentCurrent.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setCurrentWeight(it)
            }
        }

        // Graph
        mainViewModel.weightFragmentGraph.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                registryGraph(it)
            }
        }

        this.parentFragmentManager.setFragmentResultListener("dialogWeight", this ) { _, _ ->
            mainViewModel.getWeights()
            mainViewModel.getCurrentWeightFragment()
            mainViewModel.getGraphFragment()
        }
    }

    private fun init() {
        registryAdapter() // list

        // add weight
        binding?.add?.setOnClickListener { _ ->
            val dialog = DialogWeight()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWeight")
        }
    }

    /**
     * Registry list weight
     */
    private fun registryAdapter() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding?.listWeight?.setHasFixedSize(true)

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding?.listWeight?.layoutManager = layoutManager

        // specify an adapter (see also next example)
        listAdapter = WeightAdapter(aList, requireContext())

        // set list values
        binding?.listWeight?.adapter = listAdapter

        // divider between elements
        // Get drawable object
        val mDivider = ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)
        // Create a DividerItemDecoration whose orientation is Horizontal
        val hItemDecoration = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider!!)
        binding?.listWeight?.addItemDecoration(hItemDecoration) // set values
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setWeightLog(weightList: List<Weight>) {

        aList.clear() // clear list
        aList.addAll(weightList) // add new items to list

        listAdapter.notifyDataSetChanged() // update list
    }

    /**
     * current weight
     */
    private fun setCurrentWeight(weight: Weight) {
        val string: String = java.lang.String.format(Locale.US, "%.02f", weight.weight)
        binding?.weightCurrent?.text = getString(R.string.weight_current_value, string)
    }

    /**
     * Registry graph
     */
    private fun registryGraph(values: ArrayList<Entry>) {

        // background color
        binding?.graph?.setBackgroundColor(Color.WHITE)

        // disable description text
        binding?.graph?.description?.isEnabled = false

        // enable touch gestures
        // binding?.graph?.setTouchEnabled(true)

        // set listeners
        // binding?.graph?.setOnChartValueSelectedListener(this)
        binding?.graph?.setDrawGridBackground(false)

        binding?.graph?.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging
        binding?.graph?.isDragEnabled = true
        binding?.graph?.setScaleEnabled(true)
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // force pinch zoom along both axis
        binding?.graph?.setPinchZoom(true)


        var xAxis: XAxis
        run {   // // X-Axis Style // //
            xAxis = binding?.graph?.xAxis!!
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = MyXAxisValueFormatter()

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f)
        }

        setData(values)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setData(values: ArrayList<Entry>) {
        val set1: LineDataSet

        if (binding?.graph?.data != null &&
            binding?.graph?.data!!.dataSetCount > 0
        ) { // update graph
            set1 = binding?.graph?.data?.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()

            binding?.graph?.data?.notifyDataChanged()
            binding?.graph?.notifyDataSetChanged()
            binding?.graph?.invalidate()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, null)

            set1.axisDependency = YAxis.AxisDependency.LEFT
            set1.color = Color.rgb(89, 215, 102)
            set1.setCircleColor(Color.rgb(89, 215, 102))
            set1.lineWidth = 2f
            set1.circleRadius = 3f
            set1.fillAlpha = 65
            set1.fillColor = ColorTemplate.getHoloBlue()
            set1.highLightColor = Color.rgb(244, 117, 117)
            set1.setDrawCircleHole(false)

            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            binding?.graph?.data = data
        }
    }

    private fun roundTwoDecimals(d: Double): Double {
        val symbols = DecimalFormatSymbols(Locale.US)
        val twoDForm = DecimalFormat("#.##", symbols)
        return twoDForm.format(d).toDouble()
    }
}