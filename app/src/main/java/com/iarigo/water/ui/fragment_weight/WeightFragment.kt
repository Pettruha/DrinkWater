package com.iarigo.water.ui.fragment_weight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.databinding.FragmentWeightBinding
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.dialogWeight.DialogWeight
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import android.graphics.DashPathEffect
import android.util.Log
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils
import com.iarigo.water.R




class WeightFragment: Fragment(), WeightContract.View {

    private lateinit var presenter: WeightContract.Presenter
    private var binding: FragmentWeightBinding? = null // вместо findViewById
    private lateinit var listAdapter: WeightAdapter
    private val aList: ArrayList<Weight> = ArrayList() // Список

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = WeightPresenter()
        presenter.viewIsReady(this)
        binding = FragmentWeightBinding.inflate(layoutInflater, container, false) // имя класса на основе xml layout

        init()

        return binding!!.root
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getWeights()
        presenter.getCurrentWeight()
        presenter.getGraph()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parentFragmentManager.setFragmentResultListener("dialogWeight", this ) { requestKey, bundle ->
            presenter.getWeights()
            presenter.getCurrentWeight()
            presenter.getGraph()
        }
    }

    private fun init() {
        registryAdapter() // регистрируем адаптер
        // registryGraph()// регистрируем график
        // добавить вес
        binding?.add?.setOnClickListener { _ ->
            val dialog = DialogWeight()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWeight")
        }
    }

    /**
     * Регистрируем список
     */
    private fun registryAdapter() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding?.listWeight?.setHasFixedSize(true) // без этого перемещает

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding?.listWeight?.layoutManager = layoutManager

        // specify an adapter (see also next example)
        listAdapter = WeightAdapter(aList, requireContext())

        // устанавливаем значения
        binding?.listWeight?.adapter = listAdapter

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
        binding?.listWeight?.addItemDecoration(hItemDecoration) // устанавливаем значение
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setWeightLog(weightList: List<Weight>) {

        aList.clear() // обнуляем список элементов
        aList.addAll(weightList) // добавляем новые

        listAdapter.notifyDataSetChanged() // обновляем список
    }

    /**
     * текущий вес
     */
    override fun setCurrentWeight(weight: Weight) {
        val string: String = java.lang.String.format(Locale.US, "%.02f", weight.weight)
        binding?.weightCurrent?.text = getString(R.string.weight_current_value, string)
    }

    /**
     * Регистрируем график
     */
    override fun registryGraph(values: ArrayList<Entry>) {

        // background color
        binding?.graph?.setBackgroundColor(Color.WHITE)

        // disable description text
        binding?.graph?.description?.isEnabled = false

        // enable touch gestures
        // binding?.graph?.setTouchEnabled(true)

        // set listeners
        // binding?.graph?.setOnChartValueSelectedListener(this)
        binding?.graph?.setDrawGridBackground(false)

        binding?.graph?.setDragDecelerationFrictionCoef(0.9f);

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
/*
        var yAxis: YAxis
        run {   // // Y-Axis Style // //
            yAxis = binding?.graph?.axisLeft!!

            // disable dual axis (only use LEFT axis)
            binding?.graph?.axisRight?.isEnabled = false

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f)

            // axis range
            yAxis.axisMaximum = 200f
            yAxis.axisMinimum = -50f
        }
        */

        setData(values);
        // presenter.getGraph()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setData(values: ArrayList<Entry>) {
        /*
        val values: ArrayList<Entry> = ArrayList()
        for (weight in values) {
            Log.d("myTag", "data - ${weight.x.toLong()}; value - ${weight.y}")
            values.add(
                Entry(weight.x, weight.y)
            )
        }



        val values: ArrayList<Entry> = ArrayList()

        val calendar = Calendar.getInstance()

        for (i in 0 until 54) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val value = (Math.random() * 45F).toFloat() - 30
            values.add(
                Entry(calendar.timeInMillis.toFloat(), value)
            )
        }

         */


        val set1: LineDataSet

        if (binding?.graph?.getData() != null &&
            binding?.graph?.getData()!!.getDataSetCount() > 0
        ) {
            set1 = binding?.graph?.getData()?.getDataSetByIndex(0) as LineDataSet
            set1.setValues(values)
            set1.notifyDataSetChanged()
            binding?.graph?.getData()?.notifyDataChanged()
            binding?.graph?.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, null)

            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(Color.rgb(89, 215, 102));
            set1.setCircleColor(Color.rgb(89, 215, 102));
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);

            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            binding?.graph?.setData(data)
        }
    }

    private fun roundTwoDecimals(d: Double): Double {
        val symbols = DecimalFormatSymbols(Locale.US) // приводим вид числа к варианту с сточкой
        val twoDForm = DecimalFormat("#.##", symbols)
        return twoDForm.format(d).toDouble()
    }
}