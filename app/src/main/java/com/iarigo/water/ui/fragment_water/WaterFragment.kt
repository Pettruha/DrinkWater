package com.iarigo.water.ui.fragment_water

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentWaterBinding
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.ui.main.MainViewModel
import java.util.*
import kotlin.collections.ArrayList

class WaterFragment: Fragment() {

    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private var binding: FragmentWaterBinding? = null
    private lateinit var listAdapter: WaterAdapter
    private val aList: ArrayList<Water> = ArrayList() // List

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mainViewModel.setToday()
        binding = FragmentWaterBinding.inflate(layoutInflater, container, false)

        init()

        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Water Log
        mainViewModel.waterFragmentLog.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setWaterLog(it)
            }
        }

        // Water graph
        mainViewModel.waterFragmentGraph.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showGraph(it.getParcelableArrayList("values")!!, it.getStringArrayList("dateValues")!!)
            }
        }

        // Water Day
        mainViewModel.waterFragmentWaterDay.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setWaterDay(it)
            }
        }

        // Show Water
        mainViewModel.waterFragmentShowWater.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showAddWater(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.getWaters()
        mainViewModel.getGraph()
        mainViewModel.setCurrentDay()
    }

    private fun init() {
        registryAdapter()
        // add water click
        binding?.add?.setOnClickListener { _ ->
            mainViewModel.addDrinkFragment()
        }

        // Bar chart click. Get date
        binding?.graph?.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    mainViewModel.setNewCurrentDay(binding?.graph?.xAxis?.valueFormatter?.getFormattedValue(e.x, binding?.graph?.xAxis).toString())
                }
            }

            override fun onNothingSelected() {}
        })
    }

    /**
     * Set history date
     */
    private fun setWaterDay(day: String) {
        binding?.today?.text = requireContext().getString(R.string.water_history, day)
    }

    /**
     * List
     */
    private fun registryAdapter() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding?.listWater?.setHasFixedSize(true) // don't move items

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding?.listWater?.layoutManager = layoutManager

        // specify an adapter (see also next example)
        listAdapter = WaterAdapter(aList, requireContext())

        // set values
        binding?.listWater?.adapter = listAdapter

        // divider between items
        // Get drawable object
        val mDivider = ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)
        // Create a DividerItemDecoration whose orientation is Horizontal
        val hItemDecoration = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider!!)
        binding?.listWater?.addItemDecoration(hItemDecoration)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setWaterLog(waterList: List<Water>) {
        aList.clear() // clear list
        aList.addAll(waterList) // add new items to list

        listAdapter.notifyDataSetChanged() // update list
    }

    private fun showGraph(waterList: ArrayList<BarEntry>, datesList: ArrayList<String>) {
        val barDataSet = BarDataSet(waterList, getString(R.string.water_graph_legend))

        initBarChart(datesList)

        val data = BarData(barDataSet)
        data.barWidth = 0.5f
        binding?.graph?.data = data
        binding?.graph?.invalidate()
    }


    private fun initBarChart(datesList: ArrayList<String>) {
        //hiding the grey background of the chart, default false if not set
        binding?.graph?.setDrawGridBackground(false)
        //remove the bar shadow, default false if not set
        binding?.graph?.setDrawBarShadow(false)
        //remove border of the chart, default false if not set
        binding?.graph?.setDrawBorders(false)

        //remove the description label text located at the lower right corner
        val description = Description()
        description.isEnabled = false
        binding?.graph?.description = description

        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        binding?.graph?.animateY(1000)
        //setting animation for x-axis, the bar will pop up separately within the time we set
        binding?.graph?.animateX(1000)

        val xAxis: XAxis = binding?.graph?.xAxis!!
        //change the position of x-axis to the bottom
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //set the horizontal distance of the grid line
        xAxis.granularity = 1f
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = MyXAxisShortValueFormatter(datesList)
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false)

        val leftAxis: YAxis = binding?.graph?.axisLeft!!
        //hiding the left y-axis line, default true if not set
        leftAxis.setDrawAxisLine(false)

        val rightAxis: YAxis = binding?.graph?.axisRight!!
        //hiding the right y-axis line, default true if not set
        rightAxis.setDrawAxisLine(false)

        val legend: Legend = binding?.graph?.legend!!
        //setting the shape of the legend form to line, default square shape
        legend.form = Legend.LegendForm.LINE
        //setting the text size of the legend
        legend.textSize = 11f
        //setting the alignment of legend toward the chart
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        //setting the stacking direction of legend
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        //setting the location of legend outside the chart, default false if not set
        legend.setDrawInside(false)
    }

    /**
     * Show/Hide add water button
     */
    private fun showAddWater(show: Boolean) {
        if (show) {
            binding?.add?.isEnabled = false
            binding?.add?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.settings_text_disable), android.graphics.PorterDuff.Mode.SRC_IN)
        } else {
            binding?.add?.isEnabled = true
            binding?.add?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        binding?.add?.isEnabled = !show
    }
}