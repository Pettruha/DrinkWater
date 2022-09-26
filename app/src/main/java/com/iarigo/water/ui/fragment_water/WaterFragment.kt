package com.iarigo.water.ui.fragment_water

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentWaterBinding
import com.iarigo.water.storage.entity.Water
import com.github.mikephil.charting.components.Legend.LegendForm

import com.github.mikephil.charting.components.Legend

import com.github.mikephil.charting.components.YAxis

import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition

import com.github.mikephil.charting.components.XAxis.XAxisPosition

import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData

import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.iarigo.water.ui.dialogWater.DialogWater
import com.iarigo.water.ui.dialogWeight.DialogWeight
import com.iarigo.water.ui.fragment_weight.MyXAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class WaterFragment: Fragment(), WaterContract.View {

    private lateinit var presenter: WaterContract.Presenter
    private var binding: FragmentWaterBinding? = null // вместо findViewById
    private lateinit var listAdapter: WaterAdapter
    private val aList: ArrayList<Water> = ArrayList() // Список

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = WaterPresenter()
        presenter.viewIsReady(this)
        binding = FragmentWaterBinding.inflate(layoutInflater, container, false) // имя класса на основе xml layout

        init()

        return binding!!.root
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getWaters()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parentFragmentManager.setFragmentResultListener("dialogWater", this ) { requestKey, bundle ->
            presenter.getWaters()
            presenter.getGraph()
        }
    }

    private fun init() {
        registryAdapter() // регистрируем адаптер
        presenter.getGraph() // график
        // добавить воду
        binding?.add?.setOnClickListener { _ ->
            val dialog = DialogWater()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWater")
        }
    }

    /**
     * Регистрируем список
     */
    private fun registryAdapter() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding?.listWater?.setHasFixedSize(true) // без этого перемещает

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding?.listWater?.layoutManager = layoutManager

        // specify an adapter (see also next example)
        listAdapter = WaterAdapter(aList, requireContext())

        // устанавливаем значения
        binding?.listWater?.adapter = listAdapter

        // разделитель между элементами
        // Get drawable object
        val mDivider = ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)
        // Create a DividerItemDecoration whose orientation is Horizontal
        val hItemDecoration = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider!!)
        binding?.listWater?.addItemDecoration(hItemDecoration) // устанавливаем значение
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setWaterLog(waterList: List<Water>) {

        aList.clear() // обнуляем список элементов
        aList.addAll(waterList) // добавляем новые

        listAdapter.notifyDataSetChanged() // обновляем список
    }

    override fun registryGraph(waterList: ArrayList<BarEntry>) {
        val title = "Вода, мл"
        /*
        val valueList = ArrayList<Double>()
        val entries: ArrayList<BarEntry> = ArrayList()


        //input data
        for (i in 0..3) {
            valueList.add(i * 100.1)
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)


        //fit the data into a bar
        for (i in 0 until valueList.size) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)

            val barEntry = BarEntry(TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis).toFloat(), valueList[i].toFloat())
            // val barEntry = BarEntry(i.toFloat(), valueList[i].toFloat())
            entries.add(barEntry)
        }

         */
        val barDataSet = BarDataSet(waterList, title)

        /*
        //Changing the color of the bar
    barDataSet.setColor(Color.parseColor("#304567"));    //Setting the size of the form in the legend
    barDataSet.setFormSize(15f);    //showing the value of the bar, default true if not set
    barDataSet.setDrawValues(false);    //setting the text size of the value of the bar
    barDataSet.setValueTextSize(12f);
         */

        // val xAxis: XAxis? = binding?.graph?.getXAxis()!!
        // xAxis?.position = XAxisPosition.BOTTOM
        // xAxis?.typeface = tfLight
        // xAxis?.setDrawGridLines(false)
        //       xAxis?.granularity = 1f // only intervals of 1 day
        // xAxis?.axisMinimum = 0f

        // xAxis?.labelCount = 7
        // xAxis?.valueFormatter = MyXAxisValueFormatter()
        // xAxis?.setValueFormatter(xAxisFormatter)

        initBarChart()

        val data = BarData(barDataSet)
        data.barWidth = 0.5f
        binding?.graph?.setData(data)
        binding?.graph?.invalidate()
    }


    private fun initBarChart() {
        //hiding the grey background of the chart, default false if not set
        binding?.graph?.setDrawGridBackground(false);
        //remove the bar shadow, default false if not set
        binding?.graph?.setDrawBarShadow(false);
        //remove border of the chart, default false if not set
        binding?.graph?.setDrawBorders(false);

        //remove the description label text located at the lower right corner
        val description = Description();
        description.setEnabled(false);
        binding?.graph?.setDescription(description);

        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        binding?.graph?.animateY(1000);
        //setting animation for x-axis, the bar will pop up separately within the time we set
        binding?.graph?.animateX(1000);

        val xAxis: XAxis = binding?.graph?.getXAxis()!!
        //change the position of x-axis to the bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //set the horizontal distance of the grid line
        xAxis.setGranularity(1f);
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false);
        xAxis.valueFormatter = MyXAxisShortValueFormatter()
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false);

        val leftAxis: YAxis = binding?.graph?.getAxisLeft()!!
        //hiding the left y-axis line, default true if not set
        leftAxis.setDrawAxisLine(false);

        val rightAxis: YAxis = binding?.graph?.getAxisRight()!!
        //hiding the right y-axis line, default true if not set
        rightAxis.setDrawAxisLine(false);

        val legend: Legend = binding?.graph?.getLegend()!!
        //setting the shape of the legend form to line, default square shape
        legend.setForm(Legend.LegendForm.LINE);
        //setting the text size of the legend
        legend.setTextSize(11f);
        //setting the alignment of legend toward the chart
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        //setting the stacking direction of legend
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //setting the location of legend outside the chart, default false if not set
        legend.setDrawInside(false);
    }

}